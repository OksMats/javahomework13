import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonPlaceTask {
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static void createUser(String name, String email) {
        Map<String, Object> userMap = Map.of("name", name, "email", email);
        Map<String, Object> response = send("/users", "POST", userMap);
        System.out.println("Created user: " + response);
    }
    private static void updateUser(int userId, String name, String email) {
        Map<String, Object> userMap = Map.of("id", userId, "name", name, "email", email);
        Map<String, Object> response = send("/users/" + userId, "PUT", userMap);
        System.out.println("Updated user: " + response);
    }
    private static void deleteUser(int userId) {
        send("/users/" + userId, "DELETE", null);
        System.out.println("User deleted successfully");
    }
    private static void getAllUsers() {
        list("/users").forEach(user -> System.out.println("User: " + user));
    }
    private static void getUserById(int userId) {
        System.out.println("User by ID " + userId + ": " + map("/users/" + userId));
    }
    private static void getUserByUsername(String username) {
        list("/users?username=" + username).forEach(user -> System.out.println("User by username " + username + ": " + user));
    }
    private static void getOpenTasksForUser(int userId) {
        list("/users/" + userId + "/todos").stream()
                .filter(todo -> !(boolean) todo.get("completed"))
                .map(todo -> "Task: " + todo.get("title"))
                .forEach(System.out::println);
    }

    private static void getAndSaveCommentsForLastPost(int userId) {
        List<Map<String, Object>> posts = list("/users/" + userId + "/posts");
        if (!posts.isEmpty()) {
            Map<String, Object> lastPost = posts.get(posts.size() - 1);
            int postId = (int) lastPost.get("id");
            saveCommentsToFile(userId, postId, list("/posts/" + postId + "/comments"));
        } else {
            System.out.println("No posts found for user " + userId);
        }
    }

    private static void saveCommentsToFile(int userId, int postId, List<Map<String, Object>> comments) {
        String fileName = "user-" + userId + "-post-" + postId + "-comments.json";
        System.out.println("Comments saved to file: " + fileName);
    }

    private static List<Map<String, Object>> list(String endpoint) {
        return send(endpoint, "GET", null);
    }

    private static Map<String, Object> map(String endpoint) {
        return send(endpoint, "GET", null).get(0);
    }

    private static Map<String, Object> send(String endpoint, String method, Map<String, Object> data) {
        HttpUriRequest request = RequestBuilder.create(method)
                .setUri(BASE_URL + endpoint)
                .addHeader("Content-Type", "application/json")
                .setEntity(data != null ? toJsonEntity(data) : null)
                .build();

        return readResponse(HttpClients.createDefault().execute(request)).get(0);
    }

    private static List<Map<String, Object>> readResponse(org.apache.http.HttpResponse response) {
        String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

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

    private static StringEntity toJsonEntity(Map<String, Object> data) {
        return new StringEntity(new JSONObject(data).toString(), StandardCharsets.UTF_8);
    }
}
