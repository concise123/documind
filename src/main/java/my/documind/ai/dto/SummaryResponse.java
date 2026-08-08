package my.documind.ai.dto;

public record SummaryResponse(String content, String model, Integer totalTokens) {
}
