package my.documind.document.repository.projection;

public interface VectorSearchProjection {
    Long getChunkId();
    String getContent();
    Integer getChunkIndex();
    Double getDistance();
}
