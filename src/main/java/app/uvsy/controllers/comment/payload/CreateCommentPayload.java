package app.uvsy.controllers.comment.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CreateCommentPayload {
    private final String publicationId;
    private final String userId;
    private final String content;

    public CreateCommentPayload(@JsonProperty(value = "publicationId") String publicationId,
                                @JsonProperty(value = "userId") String userId,
                                @JsonProperty(value = "content") String content) {
        this.publicationId = publicationId;
        this.userId = userId;
        this.content = content;
    }
}
