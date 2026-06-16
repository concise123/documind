package my.documind.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import my.documind.dto.SummaryResponse;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "documentAiResults")
public class DocumentAiResult extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiResultType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String model;

    private Integer totalTokens;

    public void assignDocument(Document document) {
        this.document = document;
    }

    public static DocumentAiResult summary(SummaryResponse response) {
        return DocumentAiResult.builder()
                .type(AiResultType.SUMMARY)
                .content(response.content())
                .model(response.model())
                .totalTokens(response.totalTokens())
                .build();
    }
}
