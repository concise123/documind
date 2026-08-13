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
        List<Double> values = response.data()
                        .getFirst()
                        .embedding();
        float[] embedding = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            embedding[i] = values.get(i).floatValue();
        }
        return embedding;
    }
}
