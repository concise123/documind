# PDF 업로드 Executor 포화 원인 분석

## 목적

파일을 동시에 13개 이상 업로드할 때 발생하는 업로드 실패 원인을 분석한다.

## 테스트 환경

### 실행 환경

- Railway Free
- Spring Boot
- PDFBox 3.0.7

### Executor 설정

```java
corePoolSize = 2
maxPoolSize = 2
queueCapacity = 10
```

### 업로드 정책

- 최대 파일 개수: 15개
- 최대 파일 크기: 10MB
- 최대 요청 크기: 30MB

## 테스트 방법

- `PdfBatchRunner.submit()` 직전에 Executor 상태 로그 추가
- PDF 추출 시간 로그 추가
- 파일 15개 동시 업로드 및 로그 확인

## 테스트 결과

Executor 상태는 다음과 같이 변화하였다.

| 제출 순서 | Active | Pool | Queue |
|----------:|-------:|-----:|------:|
| 1 | 0 | 0 | 0 |
| 2 | 1 | 1 | 0 |
| 3 | 2 | 2 | 0 |
| 4 ~ 12 | 2 | 2 | 1 ~ 10 |
| 13 | RejectedExecutionException 발생 |

로그에서 다음과 같은 예외가 발생하였다.

```
RejectedExecutionException

ExecutorService in active state did not accept task
```

이후 PDF 추출 중인 스레드에서 다음 예외가 발생하였다.

```
NoSuchFileException
```

PDFBox에서는

```
Can't dereference COSObject
```

예외가 함께 출력되었다.

## 원인 분석

현재 `PdfBatchRunner.extractAll()`은 먼저 모든 PDF를 Executor에 제출한 후(`submit`) 완료를 기다리는(`await`) 구조이다.

```text
submit (모든 파일)

↓

await
```

13번째 작업 제출 시 Queue가 가득 차면서 `RejectedExecutionException`이 발생하였다.

예외가 발생하면 `DocumentService`에서 즉시

```java
cleanupStoredFiles(files);
```

가 호출된다.

하지만 이미 실행 중이던 PDF 추출 스레드는 아직 파일을 읽고 있었으며

파일이 삭제되면서

```
NoSuchFileException
```

이 발생하였다.

즉, 업로드 실패 이후 실행 중인 작업까지 영향을 받는 구조였다.

## 결론

이번 문제는 두 가지 원인이 함께 존재하였다.

1. Executor Queue 용량 부족으로 `RejectedExecutionException` 발생
2. 실패 처리 과정에서 실행 중인 작업까지 파일 삭제의 영향을 받아 추가 예외 발생

## 개선 사항

### 적용 예정

- `queueCapacity`
    - 10 → 20

서비스 정책(최대 15개 업로드)을 고려하면 Queue 20이면 충분한 여유를 확보할 수 있다.

### 향후 개선 검토

- 제출 작업을 일정 개수씩 나누어 처리(Batching)
- 실패 처리 과정에서 발생하는 추가 예외를 줄일 수 있는지 검토
- Executor 포화 시 사용자 친화적인 예외 메시지 제공

## 배운 점

단순히 Executor Queue 크기를 늘리는 것만으로는 문제의 원인을 모두 설명할 수 없었다.

DEBUG 로그를 통해 Executor 상태와 예외 발생 시점을 함께 분석한 결과 리소스 정리 시점이 실행 중인 작업에도 영향을 줄 수 있음을 확인하였다.

운영 환경에서는 Thread Pool 설정뿐 아니라 작업 생명주기와 리소스 정리 시점까지 함께 고려해야 한다는 점을 경험하였다.