package app.uvsy.controllers.publication.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CreateCommentPayload {
    private final String userId;
    private final String content;

    public CreateCommentPayload(@JsonProperty(value = "userId") String userId,
                                @JsonProperty(value = "content") String content) {
        this.userId = userId;
        this.content = content;
    }
}
