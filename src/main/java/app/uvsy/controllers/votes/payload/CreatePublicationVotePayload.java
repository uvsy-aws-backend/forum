package app.uvsy.controllers.votes.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CreatePublicationVotePayload {

    private final String publicationId;
    private final String userId;

    public CreatePublicationVotePayload(@JsonProperty(value = "publicationId") String publicationId,
                                        @JsonProperty(value = "userId") String userId) {
        this.publicationId = publicationId;
        this.userId = userId;
    }
}
