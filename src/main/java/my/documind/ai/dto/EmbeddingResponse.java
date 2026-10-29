package my.documind.ai.dto;

import java.util.List;

public record EmbeddingResponse(List<EmbeddingData> data) {
    public record EmbeddingData(List<Double> embedding) {}
}