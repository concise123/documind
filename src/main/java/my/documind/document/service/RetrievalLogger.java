package my.documind.document.service;

import lombok.extern.log4j.Log4j2;
import my.documind.document.dto.VectorSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
public class RetrievalLogger {
    public void log(String question, List<VectorSearchResult> results) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("========== RAG Retrieval 결과 ==========");
        log.debug("질문: {}", question);
        for (int i = 0; i < results.size(); i++) {
            VectorSearchResult result = results.get(i);
            log.debug("순위: {}, 청크 인덱스: {}, 거리: {}, 내용: {}", i + 1,
                    result.chunkIndex(), String.format("%.2f", result.distance()), result.contentPreview(300));
        }
    }
}
