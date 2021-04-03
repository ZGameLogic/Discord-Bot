package webhook.listeners;

import java.awt.Color;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * 
 * @author Ben Shabowski
 *
 */
public class WebHookReactionListener extends ListenerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(WebHookReactionListener.class);
	
	private TextChannel channel;
	private ConfigLoader cl;
	
	private static Message currentMessage;
	
	public WebHookReactionListener(ConfigLoader cl) {
		this.cl = cl;
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Webhook Reaction Listener started...");
		channel = event.getJDA().getGuildById(cl.getBitbucketGuildIDs().get(0)).getTextChannelById(cl.getBitbucketGuildIDs().get(1));
	}
	
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(!event.getUser().isBot() && event.getChannel().equals(channel)) {
			if(event.getReaction().toString().contains("RE:U+1f3d7")){
				
				try {
					JSONObject resultPull = new JSONObject(createPullRequest(event.retrieveMember().complete().getEffectiveName()));
					String id = resultPull.getString("id");
					JSONObject resultMerge = new JSONObject(mergePullRequest(id));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				currentMessage = event.retrieveMessage().complete();
				
				EmbedBuilder eb = new EmbedBuilder();
				
				MessageEmbed old = event.retrieveMessage().complete().getEmbeds().get(0);
				
				eb.setTitle(old.getTitle(),"https://zgamelogic.com:7990/projects/BSPR/repos/discord-bot/browse");
				eb.setAuthor(old.getAuthor().getName(), old.getAuthor().getUrl());
				
				for(Field x : old.getFields()) {
					eb.addField(x);
				}
				
				eb.setColor(Color.BLUE);				
				event.retrieveMessage().complete().editMessage(eb.build()).complete();
				currentMessage.clearReactions().complete();
			}
		}
	}
	
	public static void changeStatus(Color color) {
		
		if(currentMessage != null) {
		
		EmbedBuilder eb = new EmbedBuilder();
		MessageEmbed old = currentMessage.getEmbeds().get(0);
		
		eb.setTitle(old.getTitle(),"https://zgamelogic.com:7990/projects/BSPR/repos/discord-bot/browse");
		eb.setAuthor(old.getAuthor().getName(), old.getAuthor().getUrl());
		
		for(Field x : old.getFields()) {
			eb.addField(x);
		}
		
		eb.setColor(color);
		
		currentMessage.editMessage(eb.build()).complete();
		currentMessage = null;
		}
	}
	
	private String mergePullRequest(String pullRequestID) {
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
	
	private String createPullRequest(String requester) {
		String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/pull-requests";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    
	    String plainCreds = "BShabowski:NjcwMjk5MDUxOTM3OnLnbm6v5WzJnj8LU2Q4sYn7Nvym";
	    byte[] plainCredsBytes = plainCreds.getBytes();
	    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
	    String base64Creds = new String(base64CredsBytes);

	    headers.add("Authorization", "Basic " + base64Creds);
	    
	    String pullRequestTitle = "Pull request made from discord";
	    String pullRequestDescription = "This pull request is being created by " + requester;
	    
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
