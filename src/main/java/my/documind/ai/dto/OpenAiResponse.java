package my.documind.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAiResponse(List<Choice> choices, String model, Usage usage) {
    public record Choice(Message message) {}

    public record Message(String role, String content) {}

    public record Usage(@JsonProperty("total_tokens") Integer totalTokens,
                        @JsonProperty("prompt_tokens") Integer inputTokens,
                        @JsonProperty("completion_tokens") Integer outputTokens) {}

    public String getContent() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        return choices.getFirst().message().content();
    }
}
