package app.uvsy.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedResponse {

    @JsonProperty(value = "count")
    private final int count;

    @JsonProperty(value = "data")
    private final List<?> data;

    private PaginatedResponse(List<?> objects, int count) {
        this.data = objects;
        this.count = count;
    }

    public static PaginatedResponse of(List<?> objects) {
        int size = objects.size();
        return new PaginatedResponse(
                objects,
                size
        );
    }
}
