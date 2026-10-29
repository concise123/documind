package my.documind.document.dto;

public record VectorSearchResult(Long chunkId, String content, int chunkIndex, double distance) {
    public String contentPreview(int maxLength) {
        if (content == null) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }
}
