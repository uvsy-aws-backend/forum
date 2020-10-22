package app.uvsy.apis;

import app.uvsy.apis.exceptions.APIClientException;
import app.uvsy.apis.model.ApiError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class APIClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String host;

    public APIClient(String host) {
        this.host = host;
    }

    private URL baseUrl() throws MalformedURLException {
        return new URL(String.format("https://%s", host));
    }


    private <T> Optional<T> request(HttpRequestBase request, Class<T> clazz) throws APIClientException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (statusCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                ApiError apiError = objectMapper.readValue(entity.getContent(), ApiError.class);
                System.out.println(apiError);
                throw APIClientException.from(statusCode, apiError);
            }

            if (statusCode == HttpURLConnection.HTTP_OK && clazz != Void.class) {
                return Optional.ofNullable(objectMapper.readValue(entity.getContent(), clazz));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw APIClientException.wrap(e);
        }
    }

    public <T> Optional<T> get(String resource, Class<T> clazz, Map<String, String> params) throws APIClientException {
        try {
            URL url = new URL(baseUrl(), resource);
            URIBuilder builder = new URIBuilder(url.toURI());
            params.forEach(builder::addParameter);
            HttpGet get = new HttpGet(builder.build());
            return request(get, clazz);
        } catch (MalformedURLException | URISyntaxException e) {
            throw APIClientException.wrap(e);
        }
    }

    public <T> Optional<T> post(String resource, Class<T> clazz, Map<String, String> params, Object payload) throws APIClientException {
        try {
            URL url = new URL(baseUrl(), resource);
            URIBuilder builder = new URIBuilder(url.toURI());
            params.forEach(builder::addParameter);

            HttpPost post = new HttpPost(builder.build());
            HttpEntity entity = new ByteArrayEntity(
                    objectMapper.writeValueAsBytes(payload),
                    ContentType.APPLICATION_JSON
            );
            post.setEntity(entity);
            return request(post, clazz);
        } catch (MalformedURLException | URISyntaxException | JsonProcessingException e) {
            throw APIClientException.wrap(e);
        }
    }

    public <T> Optional<T> post(String resource, Class<T> clazz, Object payload) throws APIClientException {
        return this.post(resource, clazz, new HashMap<>(), payload);
    }
}
