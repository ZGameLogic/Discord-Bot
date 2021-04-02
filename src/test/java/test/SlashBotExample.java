package test;

import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class SlashBotExample
{
    public static void main(String[] args) throws JSONException
    {
    	System.out.println(createPullRequest());
    }
    
    private static String createPullRequest() {
		String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/pull-requests";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    
	    String plainCreds = "BShabowski:NjcwMjk5MDUxOTM3OnLnbm6v5WzJnj8LU2Q4sYn7Nvym";
	    byte[] plainCredsBytes = plainCreds.getBytes();
	    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
	    String base64Creds = new String(base64CredsBytes);

	    headers.add("Authorization", "Basic " + base64Creds);
	    
	    String pullRequestTitle = "This is the title";
	    String pullRequestDescription = "This is the description";
	    
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
}