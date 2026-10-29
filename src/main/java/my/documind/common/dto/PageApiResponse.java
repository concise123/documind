package my.documind.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class PageApiResponse<E> {
    @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
    private int page;

    @Schema(description = "페이지당 데이터 개수", example = "5")
    private int pageSize;

    @Schema(description = "전체 데이터 개수", example = "35")
    private int totalElements;

    @Schema(description = "현재 페이지의 데이터 리스트")
    private List<E> content;
}
