package my.documind.dto;

import lombok.Getter;
import my.documind.domain.Posts;

@Getter
public class PostsResponseDto {
    private Long id;
    private String title;
    private String content;

    public PostsResponseDto(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
    }
}
