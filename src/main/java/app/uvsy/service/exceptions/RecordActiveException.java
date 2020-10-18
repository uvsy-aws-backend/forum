package app.uvsy.service.exceptions;

import org.github.serverless.api.exceptions.apigw.APIException;

import java.net.HttpURLConnection;

public class RecordActiveException extends APIException {


    public RecordActiveException(String recordId) {
        super(
                String.format("The record %s is active", recordId),
                HttpURLConnection.HTTP_BAD_REQUEST
        );
    }
}
