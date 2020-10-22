package app.uvsy.apis.students.model;

import lombok.Getter;

import java.util.List;

@Getter
public class UserAliasRequest {
    private final List<String> usersIds;

    public UserAliasRequest(List<String> usersIds) {
        this.usersIds = usersIds;
    }
}
