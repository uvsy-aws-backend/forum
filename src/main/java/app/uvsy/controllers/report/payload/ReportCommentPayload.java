package app.uvsy.controllers.report.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ReportCommentPayload {

    private final String userId;
    private final String commentId;

    public ReportCommentPayload(@JsonProperty(value = "userId", required = true) String userId,
                                @JsonProperty(value = "commentId", required = true) String commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }
}
