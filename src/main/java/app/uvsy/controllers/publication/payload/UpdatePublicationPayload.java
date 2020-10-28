package app.uvsy.controllers.publication.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdatePublicationPayload {
    private final String title;
    private final String description;
    private final List<String> tags;

    public UpdatePublicationPayload(@JsonProperty(value = "title") String title,
                                    @JsonProperty(value = "description") String description,
                                    @JsonProperty(value = "tags") List<String> tags) {
        this.title = title;
        this.description = description;
        this.tags = tags;
    }
}
