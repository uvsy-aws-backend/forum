package app.uvsy.service.exceptions;

import org.github.serverless.api.exceptions.apigw.APIException;

import java.net.HttpURLConnection;

public class RecordNotFoundException extends APIException {
    public RecordNotFoundException(String recordId) {
        super(
                String.format("%s does not exist", recordId),
                HttpURLConnection.HTTP_NOT_FOUND
        );
    }
}
