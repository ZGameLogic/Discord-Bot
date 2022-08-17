package controllers.atlassian;

import application.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

public abstract class JiraInterfacer {

    public static void optIn(String issue) {
        String link = "https://zgamelogic.com:8080/rest/api/2/issue/"  + issue;
        JSONObject update = new JSONObject();
        JSONArray labels = new JSONArray();
        try {
            labels.put(new JSONObject("{\"add\":\"Notify-User\"}"));
            update.put("labels", labels);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        putToJira(link, update);
    }

    public static void optOut(String issue){
        String link = "https://zgamelogic.com:8080/rest/api/2/issue/"  + issue;
        JSONObject update = new JSONObject();
        JSONArray labels = new JSONArray();
        try {
            labels.put(new JSONObject("{\"remove\":\"Notify-User\"}"));
            update.put("labels", labels);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        putToJira(link, update);
    }

    private static void putToJira(String link, JSONObject update) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + App.config.getJiraPat());
        HttpEntity<String> request = new HttpEntity<>(update.toString(), headers);
        restTemplate.put(link, request, String.class);
    }

    public static void addComment(String issue, String comment, String user){
        String link = "https://zgamelogic.com:8080/rest/api/2/issue/" + issue + "/comment";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        try {
            body.put("body", "Discord user " + user + " added a comment\n" + comment);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        headers.add("Authorization", "Bearer " + App.config.getJiraPat());
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        try {
            JSONObject result = new JSONObject(restTemplate.postForObject(link, request, String.class));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject getIssue(String issue){
        String link = "https://zgamelogic.com:8080/rest/api/2/issue/"  + issue;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + App.config.getJiraPat());
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            JSONObject result = new JSONObject(restTemplate.exchange(link, HttpMethod.GET, request, String.class).getBody());
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static MessageEmbed submitBug(String title, String description, String strc, String username, long userId, String optIn){
        String link = "https://zgamelogic.com:8080/rest/api/2/issue";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        try {
            JSONObject fields = new JSONObject();
            fields.put("project", new JSONObject("{\"key\": \"DB\"}"));
            fields.put("summary", title);
            fields.put("description", description + "\n" +
                    "Steps to recreate: " + strc + "\n" +
                    "Discord username: " + username + "\n" +
                    "Discord user ID: " + userId);
            fields.put("assignee", new JSONObject("{\"name\":\"BShabowski\"}"));
            fields.put("issuetype", new JSONObject("{\"name\": \"Bug\"}"));
            JSONArray labels = new JSONArray();
            labels.put("Discord");
            labels.put("Notify-User");
            fields.put("labels", labels);
            body.put("fields", fields);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        headers.add("Authorization", "Bearer " + App.config.getJiraPat());
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        try {
            JSONObject result = new JSONObject(restTemplate.postForObject(link, request, String.class));
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(122, 50, 168));
            eb.setTitle("Bug report submission results");
            eb.setDescription("Thank you for submitting this bug report! I hope to resolve it soon.");
            eb.setFooter("Issue: " +  result.getString("key"));
            return eb.build();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
