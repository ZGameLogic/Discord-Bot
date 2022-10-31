package controllers.atlassian;

import application.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

public abstract class BitbucketInterfacer {

    private static String getCommitList() {
        String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/commits/development";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(link, String.class);
        return result;
    }

    public static MessageEmbed buildBitbucketMessage(String committer, String committerLink, String repoName, String repoLink) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Bitbucket push to " + repoName, repoLink);
        eb.setColor(Color.GRAY);
        eb.setAuthor(committer, committerLink);

        try {
            JSONObject commits = new JSONObject(getCommitList());

            if(commits.has("values")) {

                for(int i = 0; i < 5; i++) {
                    String displayID = commits.getJSONArray("values").getJSONObject(i).getString("displayId");
                    String message = commits.getJSONArray("values").getJSONObject(i).getString("message");
                    eb.addField(displayID, message, false);
                }
            } else {
                String displayID = commits.getString("displayId");
                String message = commits.getString("message");
                eb.addField(displayID, message, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return eb.build();
    }

    public static String mergePullRequest(String pullRequestID) {
        String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/pull-requests/" + pullRequestID + "/merge?version=0";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String base64Creds = encodedAuthorization();

        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>("{}", headers);

        String result = restTemplate.postForObject(link, request, String.class);

        return result;
    }

    public static String createPullRequest(String requester) {
        String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/pull-requests";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String base64Creds = encodedAuthorization();

        headers.add("Authorization", "Basic " + base64Creds);

        String pullRequestTitle = "Pull request made from discord";
        String pullRequestDescription = "This pull request is being created by " + requester + " from discord.";

        HttpEntity<String> request = new HttpEntity<String>("{\r\n" +
                "  \"title\": \"" + pullRequestTitle + "\",\r\n" +
                "  \"description\": \"" + pullRequestDescription + "\",\r\n" +
                "  \"state\": \"OPEN\",\r\n" +
                "  \"open\": true,\r\n" +
                "  \"closed\": false,\r\n" +
                "  \"fromRef\": {\r\n" +
                "    \"id\": \"refs/heads/development\",\r\n" +
                "    \"repository\": {\r\n" +
                "      \"slug\": \"discord-bot\",\r\n" +
                "      \"name\": null,\r\n" +
                "      \"project\": {\r\n" +
                "        \"key\": \"BSPR\"\r\n" +
                "      }\r\n" +
                "    }\r\n" +
                "  },\r\n" +
                "  \"toRef\": {\r\n" +
                "    \"id\": \"refs/heads/master\",\r\n" +
                "    \"repository\": {\r\n" +
                "      \"slug\": \"discord-bot\",\r\n" +
                "      \"name\": null,\r\n" +
                "      \"project\": {\r\n" +
                "        \"key\": \"BSPR\"\r\n" +
                "      }\r\n" +
                "    }\r\n" +
                "  },\r\n" +
                "  \"locked\": false,\r\n" +
                "  \"reviewers\": [\r\n" +
                "    {\r\n" +
                "      \"user\": {\r\n" +
                "        \"name\": \"BShabowski\"\r\n" +
                "      }\r\n" +
                "    }\r\n" +
                "  ]\r\n" +
                "}", headers);
        String result = restTemplate.postForObject(link, request, String.class);
        return result;
    }

    private static String encodedAuthorization() {
        String plainCreds = "DBot:" + App.config.getBitbucketPat();
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        return base64Creds;
    }
}
