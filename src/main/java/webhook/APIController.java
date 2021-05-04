package webhook;

import java.awt.Color;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import webhook.listeners.WebHookListener;
import webhook.listeners.WebHookReactionListener;

/**
 * 
 * @author Ben Shabowski
 *
 */
@RestController
public class APIController {
	
	private static Stack<String> tokens = new Stack<String>();
	
	@PostMapping("/webhook/bitbucket")
	public void bitbucketWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleBitbucket(JSONInformation);
	}
	
	@PostMapping("/update")
	public void updateStatusWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		if(validate(JSONInformation.getString("token"))) {
			updateStatus(JSONInformation);
		}
	}
	
	@PostMapping("/sendmessage")
	public void sendMessageWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		if(validate(JSONInformation.getString("token"))) {
			sendMessage(JSONInformation);
		}
	}
	
	@PostMapping("/joinchannel")
	public void joinchannel(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		if(validate(JSONInformation.getString("token"))) {
			joinChannel(JSONInformation.getString("id"));
		}
	}
	
	@PostMapping("/leavechannel")
	public void leavechannel(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		if(validate(JSONInformation.getString("token"))) {
			leaveChannel();
		}
	}
	
	@PostMapping("/channellist")
	public ResponseEntity<String> getChannelList(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		if(validate(JSONInformation.getString("token"))) {
			return ResponseEntity.ok(WebHookReactionListener.getChannelList().toString());
		}
		return null;
	}
	
	@PostMapping("/voicelist")
	public ResponseEntity<String> getVoiceList(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		if(validate(JSONInformation.getString("token"))) {
			return ResponseEntity.ok(WebHookReactionListener.getVoiceList().toString());
		}
		return null;
	}
	
	@PostMapping("/webhook/bamboo")
	public void bambooWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleBamboo(JSONInformation);
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> loginWebhook(@RequestBody String body) throws JSONException {
			JSONObject JSONInformation = new JSONObject(body);
			return ResponseEntity.ok(login(JSONInformation).toString());
	}
	
	private JSONObject login(JSONObject input) throws JSONException {
		JSONObject output = new JSONObject();		
		if(input.has("password") && input.getString("password").equals(WebHookReactionListener.getPassword())) {
			String token = System.currentTimeMillis() + "";
			tokens.add(0, token);
			if(tokens.size() > 10) {
				tokens.remove(9);
			}
			
			output.put("token", token);
		}else if(input.has("token") && validate(input.getString("token"))) {
			output.put("token", input.getString("token"));
		} else {
			output.put("token", "");
		}
		return output;
	}
	
	public static boolean validate(String token) {
		return tokens.contains(token);
	}
	
	private void joinChannel(String id) {
		WebHookReactionListener.joinChannel(id);
	}
	
	private void leaveChannel() {
		WebHookReactionListener.leaveChannel();
	}
	
	private void sendMessage(JSONObject message) throws JSONException {
		WebHookReactionListener.postMessage(message.getLong("id"), message.getString("message"));
	}
	
	private void updateStatus(JSONObject message) throws JSONException {
		WebHookReactionListener.changeStatus(message.getString("status"), message.getString("activity"), "");
	}
	
	private void handleBamboo(JSONObject message) throws JSONException {
		String status = message.getJSONObject("build").getString("status");
		if(status.equals("SUCCESS")) {
			WebHookReactionListener.changeStatus(Color.GREEN);
		}else {
			WebHookReactionListener.changeStatus(Color.RED);
		}
	}
	
	private void handleBitbucket(JSONObject message) throws JSONException {
		if(message.getJSONArray("changes").getJSONObject(0).getJSONObject("ref").get("displayId").equals("development")) {
			String repoName = message.getJSONObject("repository").getString("name");
			String repoLink = message.getJSONObject("repository").getJSONObject("links").getJSONArray("self").getJSONObject(0).getString("href");
			String commiter = message.getJSONObject("actor").getString("displayName");
			String commiterLink = message.getJSONObject("actor").getJSONObject("links").getJSONArray("self").getJSONObject(0).getString("href");
		
			MessageEmbed discordMessage = buildBitbucketMessage(commiter, commiterLink, repoName, repoLink);
		
			Message discordSentMessage = WebHookListener.getChannel().sendMessage(discordMessage).complete();
			discordSentMessage.addReaction("U+1F3D7").queue();
		}
	}
	
	private MessageEmbed buildBitbucketMessage(String commiter, String commiterLink, String repoName, String repoLink) {
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setTitle("Bitbucket push to " + repoName, repoLink);
		eb.setColor(Color.GRAY);
		eb.setAuthor(commiter, commiterLink);
		
		try {
			JSONObject commits = new JSONObject(getCommitList());
			
			if(commits.has("values")) {
			
				for(int i = 0; i < 5; i++) {
					String displayID = commits.getJSONArray("values").getJSONObject(i).getString("displayId");
					String message = commits.getJSONArray("values").getJSONObject(i).getString("message");
				
					eb.addField(displayID, message, false);
				}
			}else {
				String displayID = commits.getString("displayId");
				String message = commits.getString("message");
			
				eb.addField(displayID, message, false);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return eb.build();
	}

	private static String getCommitList() {
		String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/commits/development";
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(link, String.class);
		return result;
	}
}
