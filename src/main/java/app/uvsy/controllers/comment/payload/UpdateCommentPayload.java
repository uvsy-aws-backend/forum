package app.uvsy.controllers.comment.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class UpdateCommentPayload {

    private final String content;

    public UpdateCommentPayload(@JsonProperty(value = "content") String content) {
        this.content = content;
    }
}
