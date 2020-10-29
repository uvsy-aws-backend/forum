package app.uvsy.controllers.votes.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CreateCommentVotePayload {

    private final String commentId;
    private final String userId;

    public CreateCommentVotePayload(@JsonProperty(value = "commentId") String commentId,
                                    @JsonProperty(value = "userId") String userId) {
        this.commentId = commentId;
        this.userId = userId;
    }
}
