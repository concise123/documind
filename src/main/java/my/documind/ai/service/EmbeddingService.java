package my.documind.ai.service;

import lombok.RequiredArgsConstructor;
import my.documind.ai.client.OpenAiClient;
import my.documind.ai.dto.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {
    private final OpenAiClient openAiClient;

    public float[] embed(String text) {
        EmbeddingResponse response =  openAiClient.createEmbedding(text);
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new IllegalStateException("임베딩 생성에 실패했습니다.");
        }
        List<Double> values = response.data()
                .getFirst()
                .embedding();
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException("임베딩 생성에 실패했습니다.");
        }
        float[] embedding = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            embedding[i] = values.get(i).floatValue();
        }
        return embedding;
    }
}
