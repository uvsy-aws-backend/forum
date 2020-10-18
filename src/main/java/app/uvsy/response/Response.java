package app.uvsy.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class Response implements Serializable {

    @JsonProperty(value = "data")
    private final Object data;

    private Response(Object data) {
        this.data = data;
    }

    public static Response of(Object object) {
        return new Response(object);
    }
}
