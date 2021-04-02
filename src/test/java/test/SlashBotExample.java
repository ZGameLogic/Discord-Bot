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
    	System.out.println(mergePullRequest("26"));
    }
    
	private static String mergePullRequest(String pullRequestID) {
		String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/pull-requests/" + pullRequestID + "/merge?version=0";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    
	    String plainCreds = "BShabowski:NjcwMjk5MDUxOTM3OnLnbm6v5WzJnj8LU2Q4sYn7Nvym";
	    byte[] plainCredsBytes = plainCreds.getBytes();
	    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
	    String base64Creds = new String(base64CredsBytes);

	    headers.add("Authorization", "Basic " + base64Creds);
		HttpEntity<String> request = new HttpEntity<String>("{}", headers);
	    
	    String result = restTemplate.postForObject(link, request, String.class);
	    
		return result;
	}
}