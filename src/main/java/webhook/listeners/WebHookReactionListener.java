package webhook.listeners;

import java.awt.Color;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import data.ConfigLoader;
import data.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WebHookReactionListener extends ListenerAdapter {

private static Logger logger = LoggerFactory.getLogger(WebHookReactionListener.class);
	
	private static TextChannel channel;
	private static ConfigLoader cl;
	
	private static JDA bot;
	
	private static String password;
	
	private static DataCacher<MessageID> messageID;
	
	private static boolean botReady;
	
	public WebHookReactionListener(ConfigLoader cl) {
		WebHookReactionListener.cl = cl;
		botReady = false;
		password = cl.getAdminPassword();
		messageID = new DataCacher<>("bitbucket message");
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		bot = event.getJDA();
		logger.info("Webhook Reaction Listener activated");
		botReady = true;
		channel = bot.getGuildById(cl.getGuildID()).getTextChannelById(cl.getBitbucketID());
	}
	
	/**
	 * Get the channel list
	 * @return
	 * @throws JSONException
	 */
	public static JSONArray getChannelList() throws JSONException {
		JSONArray channelNames = new JSONArray();
		
		for(TextChannel x : bot.getGuildById(330751526735970305l).getTextChannels()) {
			JSONObject channel = new JSONObject();
			channel.put("name", x.getName());
			channel.put("id", x.getIdLong());
			channelNames.put(channel);
		}
		
		return channelNames;
	}
	
	public static JSONArray getVoiceList() throws JSONException {
		JSONArray channelNames = new JSONArray();
		
		for(VoiceChannel x : bot.getGuildById(330751526735970305l).getVoiceChannels()) {
			JSONObject channel = new JSONObject();
			channel.put("name", x.getName());
			channel.put("id", x.getIdLong());
			channelNames.put(channel);
		}
		
		return channelNames;
	}
	
	public static String getPassword() {
		return password;
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(!event.getUser().isBot() && event.getChannel().getIdLong() == channel.getIdLong()) {
			if(event.getReaction().toString().contains("RE:U+1f3d7")){
				
				Message m = event.retrieveMessage().complete();
				EmbedBuilder eb = new EmbedBuilder(m.getEmbeds().get(0));
				
				eb.setColor(Color.BLUE);
				m.editMessageEmbeds(eb.build()).complete();
				m.clearReactions().queue();
				
				MessageID  mid = new MessageID(event.getMessageIdLong());
				messageID.saveSerialized(mid, "id");
				
				try {
					JSONObject resultPull = new JSONObject(createPullRequest(event.retrieveMember().complete().getEffectiveName()));
					String id = resultPull.getString("id");
					mergePullRequest(id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void changeStatus(Color color) {
		while(!botReady) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(messageID.getFiles().length > 0) {
			long message = messageID.loadSerialized("id").getId();
			logger.info("Updating message id: " + message);
			EmbedBuilder eb = new EmbedBuilder(channel.retrieveMessageById(message).complete().getEmbeds().get(0));
			eb.setColor(color);
			channel.editMessageEmbedsById(message, eb.build()).queue();
		}
	}
	
	private String mergePullRequest(String pullRequestID) {
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
	
	private String createPullRequest(String requester) {
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

	/**
	 * @return
	 */
	private String encodedAuthorization() {
		String plainCreds = "BShabowski:NjcwMjk5MDUxOTM3OnLnbm6v5WzJnj8LU2Q4sYn7Nvym";
	    byte[] plainCredsBytes = plainCreds.getBytes();
	    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
	    String base64Creds = new String(base64CredsBytes);
		return base64Creds;
	}
	
}
