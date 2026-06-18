package my.documind.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "documents")
public class Document extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, unique = true, length = 255)
    private String storedFilename;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Builder.Default
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentAiResult> aiResults = new ArrayList<>();

    public void startProcessing() {
        this.status = DocumentStatus.PROCESSING;
    }

    public void complete() {
        this.status = DocumentStatus.COMPLETED;
    }

    public void fail() {
        this.status = DocumentStatus.FAILED;
    }

    public void addAiResult(DocumentAiResult aiResult) {
        aiResults.add(aiResult);
        aiResult.assignDocument(this);
    }
}