package app.uvsy.apis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public abstract class ApiResponse<T> {

    @JsonProperty(value = "data")
    private T data;

}
