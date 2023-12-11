import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class JsonPlaceTask {
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/users";
    private static User createUser(User user) {
        return sendRequest(BASE_URL, "POST", new Gson().toJson(user), User.class);
    }

    private static void updateUser(User user) {
        sendRequest(BASE_URL + "/" + user.getId(), "PUT", new Gson().toJson(user), User.class);
    }

    private static void deleteUser(int userId) {
        sendRequest(BASE_URL + "/" + userId, "DELETE", null, Void.class);
    }

    private static void getAllUsers() {
        User[] users = sendRequest(BASE_URL, "GET", null, User[].class);
        for (User user : users) {
            System.out.println(user);
        }
    }

    private static void getUserById(int userId) {
        User user = sendRequest(BASE_URL + "/" + userId, "GET", null, User.class);
        System.out.println("User by ID " + userId + ": " + user);
    }

    private static void getUserByUsername(String username) {
        User[] users = sendRequest(BASE_URL + "?username=" + username, "GET", null, User[].class);
        for (User user : users) {
            System.out.println("User by username " + username + ": " + user);
        }
    }
    private static void getAndSaveCommentsForLastPost(int userId) {
        Map<String, Object>[] posts = sendRequest(BASE_URL + "/users/" + userId + "/posts", "GET", null, Map[].class);
        Map<String, Object> lastPost = posts[posts.length - 1];
        Map<String, Object>[] comments = sendRequest(BASE_URL + "/posts/" + lastPost.get("id") + "/comments", "GET", null, Map[].class);
        String fileName = "user-" + userId + "-post-" + lastPost.get("id") + "-comments.json";
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            new Gson().toJson(comments, fileWriter);
            System.out.println("Comments saved to file: " + fileName);
        }
    }
    private static void getOpenTasksForUser(int userId) {
        try {
            Map<String, Object>[] todos = sendRequest(BASE_URL + "/users/" + userId + "/todos", "GET", null, Map[].class);

            System.out.println("Open tasks for user " + userId + ":");
            for (Map<String, Object> todo : todos) {
                boolean completed = (boolean) todo.get("completed");
                if (!completed) {
                    System.out.println("Task: " + todo.get("title"));
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
