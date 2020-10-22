package app.uvsy.apis.students;

import app.uvsy.apis.APIClient;
import app.uvsy.apis.exceptions.APIClientException;
import app.uvsy.apis.students.model.UserAlias;
import app.uvsy.apis.students.model.UserAliasRequest;
import app.uvsy.apis.students.responses.UserAliasQueryResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudentsAPI {

    private static final String SUBJECT_RATING_RESOURCE = "/v1/alias";

    private final APIClient client;

    public StudentsAPI(String stage) {
        String host = String.format("students-api-%s.compute.universy.app", stage);
        this.client = new APIClient(host);
    }

    public List<UserAlias> postAliasQuery(List<String> subjectsId) throws APIClientException {
        UserAliasRequest request = new UserAliasRequest(subjectsId);
        return client
                .post(
                        SUBJECT_RATING_RESOURCE,
                        UserAliasQueryResponse.class,
                        request
                )
                .map(UserAliasQueryResponse::getData)
                .orElseGet(ArrayList::new);
    }


    public static void main(String[] args) throws APIClientException {
        StudentsAPI api = new StudentsAPI("dev2");
        List<UserAlias> userAliases = api.postAliasQuery(Arrays.asList("012f4610-eb90-49da-b603-cb2395157c41", "1da833d6-50cb-475e-932b-4067bc39f4df"));
        System.out.println(userAliases);
    }
}
