package webhook;

import java.awt.Color;

import org.json.JSONException;
import org.json.JSONObject;
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

	@PostMapping("/webhook/bitbucket")
	public void bitbucketWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleBitbucket(JSONInformation);
	}
	
	@PostMapping("/webhook/bamboo")
	public void bambooWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleBamboo(JSONInformation);
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
		
			Message discordSentMessage = WebHookListener.getChannel().sendMessageEmbeds(discordMessage).complete();
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
