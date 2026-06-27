# DocuMind

PDF 문서를 업로드하면 텍스트를 추출하고 AI가 문서 내용을 요약해주는 서비스입니다.

## 프로젝트 소개

긴 문서를 빠르게 파악하고 핵심 내용을 확인할 수 있도록 PDF 텍스트 추출과 AI 요약 기능을 제공합니다.  
사용자는 PDF 문서를 업로드한 후 요약 결과를 확인할 수 있으며, AI 요약은 비동기 방식으로 처리됩니다.

## 주요 기능

### 회원 기능

* 회원가입 (포트폴리오 배포 버전에서는 비활성화)
* 로그인
* 로그아웃

### 문서 관리

* PDF 파일 업로드
* PDF 텍스트 추출
* 문서 삭제
* 문서 목록 조회
* 문서 상세 조회

### AI 요약

* OpenAI API 기반 문서 요약

## 기술 스택

### Backend

* Java 21
* Spring Boot 3.5.14
* Spring Data JPA
* Spring Security

### Database

* PostgreSQL

### Frontend

* Thymeleaf
* Bootstrap

### AI

* OpenAI API
* RestClient

### Build

* Gradle

## 프로젝트 구조
전체 구조 파악에 유리한 계층형 패키지 구조를 사용했습니다.

```
controller
 └─ 요청 처리

service
 ├─ UserService
 	 └─ 회원가입
 ├─ DocumentService
 	 ├─ PDF 파일 업로드
 	 ├─ 문서 삭제
 	 ├─ 문서 목록 조회
 	 └─ 문서 상세 조회
 ├─ PdfTextExtractor
 	 └─ PDF 텍스트 추출
 ├─ LocalFileStorageService
 	 ├─ 파일 저장
 	 └─ 파일 삭제
 ├─ AsyncSummaryService
 	 └─ AI 요약 프로세스 지휘
 └─ SummaryService
 	 └─ AI 요약

repository
 └─ 데이터 접근

domain
 ├─ BaseEntity
 ├─ User
 ├─ Document
 └─ DocumentAiResult

dto
 ├─ 요청
 └─ 응답

exception
 ├─ 커스텀 예외
 ├─ ErrorMessage
 └─ GlobalExceptionHandler

config
 ├─ Spring Security 설정
 ├─ 비동기 설정
 ├─ RestClient 설정
 ├─ 비동기 작업용 스레드 풀 설정
 ├─ 메모리 사용량 관련 기능
 └─ 초기화 작업 트리거

auth
 └─ CustomUserDetailsService

client
 └─ OpenAiClient
```

## DB 테이블 설계
사용자(User)는 여러 개의 문서(Document)를 업로드할 수 있으며 각 문서는 하나 이상의 AI 처리 결과(DocumentAiResult)를 가질 수 있도록 설계했습니다.

### User 테이블
역할: 회원 정보, 인증
|컬럼명|데이터 타입|설명|
|---|---|---|
|id|bigint|아이디(PK)|
|moddate|timestamp without time zone|수정일|
|regdate|timestamp without time zone|등록일|
|email|character varying|로그인 이메일|
|nickname|character varying|닉네임|
|password|character varying|암호화 비밀번호|

### Document 테이블
역할: 업로드 문서 관리
|컬럼명|데이터 타입|설명|
|---|---|---|
|id|bigint|아이디(PK)|
|moddate|timestamp without time zone|수정일|
|regdate|timestamp without time zone|등록일|
|content_type|character varying|문서 종류|
|extracted_text|text|원본 텍스트|
|file_size|bigint|파일 크기|
|original_filename|character varying|원본 파일명|
|status|character varying|문서 처리 상태|
|stored_filename|character varying|저장 파일명|
|user_id|bigint|업로드 사용자(FK)|

### DocumentAiResult 테이블
역할: AI 처리 결과 관리
|컬럼명|데이터 타입|설명|
|---|---|---|
|id|bigint|아이디(PK)|
|moddate|timestamp without time zone|수정일|
|regdate|timestamp without time zone|등록일|
|content|text|처리 결과|
|model|character varying|AI 모델|
|total_tokens|integer|총 사용 토큰수|
|type|character varying|처리 종류|
|document_id|bigint|처리 대상 문서(FK)|

## 시스템 흐름

### 문서 업로드

```
사용자 요청
 → DocumentController
 → DocumentService
 → PDF 파일 로컬 스토리지에 저장
 → PDF 텍스트 추출 (병렬)
 → 모든 PDF 추출 완료 대기
 → Document 저장
 → 이벤트 발행
```

### AI 요약

```
이벤트 발생
 → AsyncSummaryService
 → SummaryService
 → OpenAiClient
 → OpenAI API
 → DocumentAiResult 저장
```

## 구현 과정에서 고려한 점

### AI 문서 요약 구조 설계

* 문서 업로드와 AI 요약 생성을 분리하여 응답 지연을 최소화
* 업로드 완료 후 비동기 이벤트를 통해 요약 작업 수행
* 트랜잭션 커밋 이후에만 요약 작업이 실행되도록 구성하여 데이터 정합성 확보
* AI 처리 시간이 사용자 요청에 영향을 주지 않도록 설계

### 문서 처리 상태 관리

* 문서 처리 과정을 추적하기 위해 상태 기반 구조 적용
* UPLOADED → PROCESSING → COMPLETED / FAILED 흐름으로 관리
* AI 처리 결과를 상태로 표현하여 현재 진행 상황을 확인할 수 있도록 구성
* 실패 상황 발생 시 FAILED 상태로 전환하여 예외 상황을 명확하게 관리

### 확장성을 고려한 AI 연동 설계

* AI 호출 기능을 비즈니스 로직과 분리하여 결합도를 낮춤
* 향후 다른 AI 모델 또는 서비스로 교체할 수 있도록 구조 설계
* 문서 요약 기능을 기반으로 질의응답, 검색, 임베딩 저장 기능을 확장할 수 있도록 구성
* 기능 추가 시 기존 업로드 및 요약 로직의 변경을 최소화할 수 있도록 설계

### PDF 처리 구조 설계

* PDF 텍스트 추출 기능을 PdfTextExtractor로 분리
* DocumentService는 문서 업로드 흐름을 담당하고, PdfTextExtractor는 텍스트 추출만 담당하도록 단일 책임 원칙 적용
* PDF 추출 실패 시 업로드를 중단하여 데이터 일관성 보장

### 예외 처리

* ErrorMessage 기반 예외 처리 구조 적용
* GlobalExceptionHandler를 통해 예외 처리 로직 중앙화
* 사용자 친화적인 오류 메시지 제공
* 파일 업로드, 삭제, PDF 처리 과정의 예외를 일관된 방식으로 처리

### 입력값 검증

* 회원가입과 문서 업로드 기능에 Bean Validation 적용
* DTO 검증, DB 제약조건, 화면 검증을 함께 구성하여 데이터 무결성 보장
* 검증 로직 누락을 발견한 후 회원가입 검증 테스트 추가
* 사용자에게 즉시 피드백을 제공할 수 있도록 검증 메시지 표시

### 인증 및 세션 처리

* 로그인 이후 사용자 정보가 삭제되는 예외 상황 고려
* UserNotFoundException 발생 시 세션 무효화 및 SecurityContext 정리
* 사용자를 로그인 페이지로 이동시켜 인증 상태를 정상화

### 대용량 파일 업로드 처리

* 파일 크기 초과 시 Tomcat이 연결을 종료하여 예외 응답이 전달되지 않는 문제 확인
* server.tomcat.max-swallow-size=-1 설정 적용
* GlobalExceptionHandler를 통해 업로드 실패 메시지를 사용자에게 정상적으로 전달

### 파일 저장 전략 분리

* 파일 저장 기능을 FileStorage 인터페이스로 추상화
* LocalFileStorage 구현체를 통해 로컬 환경 지원
* 저장 방식 변경 시 비즈니스 로직 수정 없이 확장 가능하도록 설계

### 서비스 계층 리팩토링

* 여러 서비스에서 반복되던 사용자 조회 로직을 UserService로 캡슐화
* 중복 코드 제거를 통해 유지보수성 향상
* 서비스 계층의 책임을 명확하게 분리

### 테스트 전략
* Repository Test를 통한 JPA 쿼리 및 엔티티 매핑 검증
* Service Unit Test를 통한 비즈니스 로직 및 예외 처리 검증
* Mockito를 활용한 외부 의존성 분리
* 테스트 코드의 가독성과 유지보수성을 위해 네이밍 규칙 통일

### 사용자 경험(UX) 개선

* 업로드 실패 시 업로드 모달을 자동으로 다시 표시
* 입력값 변경 시 검증 메시지 제거
* 플래시 메시지 속성을 통일하여 일관된 사용자 피드백 제공
* 문서 상세 화면에서 추출된 텍스트를 바로 확인할 수 있도록 구성
* 업로드 정책 화면 노출
* 문서 목록 화면 반응형으로 카드 UI 분리

### Git 전략

* 기능 단위 브랜치 전략 적용
* feat, fix, refactor 등 목적에 맞는 커밋 메시지 사용

## 트러블슈팅

### 문제
* Railway에서 대용량 PDF 다중 업로드 시 컨테이너 재시작 발생

### 원인 분석
* PDFBox 텍스트 추출 과정에서 리소스 사용량 증가
* Free 플랜의 제한된 메모리 환경 영향

### 대응
* 파일 업로드 검증 강화
* HTTP/2 비활성화
* JVM 메모리 설정 및 실행 환경 개선
* ThreadPool 기반 비동기 처리 도입

### 향후 계획
* Queue 기반 처리
* Redis 도입

## 향후 계획
* 키워드 검색
* RAG 기반 검색
* 문서 기반 질의응답
* REST API 제공
* JWT 인증 방식 적용
* Swagger/OpenAPI 문서화
* AI 답변 품질 개선