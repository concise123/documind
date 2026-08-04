package my.documind.ai.service;

import my.documind.ai.client.OpenAiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QaServiceTests {
    @Mock
    private ContextBuilder contextBuilder;

    @Mock
    private OpenAiClient openAiClient;

    @InjectMocks
    private QaService qaService;

    @Test
    @DisplayName("문서 내용과 질문에 대한 AI 답변을 요청한다")
    void shouldReturnAnswer_whenContentAndQuestionAreProvided() {
        // given
        when(openAiClient.ask(any(), any()))
                .thenReturn("답변");

        // when
        String answer = qaService.ask("문서 내용", "질문");

        // then
        assertThat(answer).isEqualTo("답변");
    }
}
