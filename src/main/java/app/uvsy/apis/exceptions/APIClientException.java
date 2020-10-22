package app.uvsy.apis.exceptions;

import app.uvsy.apis.model.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Optional;

@Getter
public class APIClientException extends RuntimeException {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Integer statusCode;
    private final String code;
    private final String detail;
    private final String source;


    private APIClientException(Integer statusCode, String code, String detail, String source) {
        super(String.format("(%s) %s", code, detail));
        this.statusCode = statusCode;
        this.code = code;
        this.detail = detail;
        this.source = source;
    }

    public static APIClientException from(int statusCode, ApiError apiError) {

        // Some values here will remain hardcoded until a new version
        // contract is established
        String code = "api_error";
        String detail = Optional.ofNullable(apiError)
                .map(ApiError::getMessage)
                .orElse(String.format("Error %d", statusCode));
        String source = "";
        return new APIClientException(statusCode, code, detail, source);
    }

    public static APIClientException wrap(Throwable e) {

        // Some values here will remain hardcoded until a new version
        // contract is established
        Integer statusCode = null;
        String code = "api_error";
        String detail = e.getMessage();
        String source = e.getLocalizedMessage();
        return new APIClientException(statusCode, code, detail, source);
    }
}
