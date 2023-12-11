import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonPlaceTask {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    private static List<Map<String, Object>> sendRequest(String endpoint, String method, Map<String, Object> data) {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpUriRequest request = buildRequest(endpoint, method, data);

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return readResponse(response);
            } else {
                System.out.println("Request failed. HTTP Response Code: " + statusCode);
                return Collections.emptyList();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static HttpUriRequest buildRequest(String endpoint, String method, Map<String, Object> data) {
        RequestBuilder requestBuilder = RequestBuilder.create(method)
                .setUri(BASE_URL + endpoint)
                .addHeader("Content-Type", "application/json");

        if (data != null) {
            requestBuilder.setEntity(mapToJsonString(data));
        }

        return requestBuilder.build();
    }

    private static List<Map<String, Object>> readResponse(HttpResponse response) throws IOException {
        String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        return parseJsonArray(responseBody);
    }

    private static List<Map<String, Object>> parseJsonArray(String jsonArray) {
        JSONArray jsonArr = new JSONArray(jsonArray);
        return StreamSupport.stream(jsonArr.spliterator(), false)
                .map(JsonPlaceTask::parseJsonObject)
                .collect(Collectors.toList());
    }

    private static Map<String, Object> parseJsonObject(Object jsonObject) {
        JSONObject jsonObj = (JSONObject) jsonObject;
        return jsonObj.toMap();
    }

    private static StringEntity mapToJsonString(Map<String, Object> data) {
        JSONObject jsonObj = new JSONObject(data);
        return new StringEntity(jsonObj.toString(), ContentType.APPLICATION_JSON);
    }
}
