package app.uvsy.service.exceptions;

import org.github.serverless.api.exceptions.apigw.APIException;

import java.net.HttpURLConnection;

public class RecordConflictException extends APIException {

    public RecordConflictException(String message) {
        super(
                "Conflict with the specified record: " + message,
                HttpURLConnection.HTTP_CONFLICT
        );
    }
}
