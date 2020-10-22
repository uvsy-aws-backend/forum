package app.uvsy.apis.students.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UserAlias {
    private final String userId;
    private final String alias;

    public UserAlias(@JsonProperty(value = "userId") String userId,
                     @JsonProperty(value = "alias") String alias) {
        this.userId = userId;
        this.alias = alias;
    }
}
