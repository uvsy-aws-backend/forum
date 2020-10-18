package app.uvsy.controllers.publication.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CreatePublicationPayload {
    private final String title;
    private final String description;
    private final String userId;
    private final String programId;
    private final List<String> tags;

    public CreatePublicationPayload(@JsonProperty(value = "title") String title,
                                    @JsonProperty(value = "description") String description,
                                    @JsonProperty(value = "userId") String userId,
                                    @JsonProperty(value = "programId") String programId,
                                    @JsonProperty(value = "tags") List<String> tags) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.programId = programId;
        this.tags = tags;
    }
}
