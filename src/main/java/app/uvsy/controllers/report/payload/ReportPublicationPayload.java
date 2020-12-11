package app.uvsy.controllers.report.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ReportPublicationPayload {

    private final String userId;
    private final String publicationId;

    public ReportPublicationPayload(@JsonProperty(value = "userId", required = true) String userId,
                                    @JsonProperty(value = "publicationId", required = true) String publicationId) {
        this.userId = userId;
        this.publicationId = publicationId;
    }
}
