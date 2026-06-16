package my.documind.domain;

import lombok.Getter;

@Getter
public enum DocumentStatus {
    UPLOADED,
    PROCESSING,
    COMPLETED,
    FAILED
}
