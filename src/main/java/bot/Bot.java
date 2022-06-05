package bot;

import java.awt.Color;
import java.time.Instant;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;

import bot.slashUtils.BugReport;
import data.serializing.DataCacher;
import net.dv8tion.jda.api.entities.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import application.App;
import bot.party.PartyBotListener;
import bot.role.RoleBotListener;
import bot.slashUtils.SlashBotListener;
import bot.steam.SteamListener;
import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import webhook.listeners.WebHookReactionListener;

@RestController
public class Bot {
	
	private RoleBotListener RBL;
	private JDA jdaBot;

	private static String TITLE = "\r\n" + 
			"   ____  ___  _                   _   ___      _  ______  \r\n" + 
			"  / /\\ \\|   \\(_)___ __ ___ _ _ __| | | _ ) ___| |_\\ \\ \\ \\ \r\n" + 
			" < <  > > |) | (_-</ _/ _ \\ '_/ _` | | _ \\/ _ \\  _|> > > >\r\n" + 
			"  \\_\\/_/|___/|_/__/\\__\\___/_| \\__,_| |___/\\___/\\__/_/_/_/ \r\n" + 
			"  v3.0.0\tBen Shabowski\r\n" +
			"";
	
	private Logger logger = LoggerFactory.getLogger(Bot.class);
	private TextChannel bitBucket;

	@PostConstruct
	public void start() {
		ConfigLoader config = App.config;
		System.out.println(TITLE);
		
		// Create bot
		JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
		bot.enableIntents(GatewayIntent.GUILD_PRESENCES);
		bot.enableCache(CacheFlag.ACTIVITY);
		bot.enableIntents(GatewayIntent.GUILD_MEMBERS);
		bot.setMemberCachePolicy(MemberCachePolicy.ALL);
		
		// Add listeners
		PartyBotListener PBL = new PartyBotListener(config);
		WebHookReactionListener WHRL = new WebHookReactionListener(config);
		RBL = new RoleBotListener(config);
		
		bot.addEventListeners(PBL);
		bot.addEventListeners(WHRL);
		bot.addEventListeners(RBL);
		bot.addEventListeners(new SteamListener());
		bot.addEventListeners(new SlashBotListener(PBL, config));
		
		// Login
		try {
			jdaBot = bot.build().awaitReady();
			bitBucket = jdaBot.getGuildById(config.getGuildID()).getTextChannelById(config.getBitbucketID());
		} catch (LoginException | InterruptedException e) {
			logger.error("Unable to launch bot");
		}
	}
	
	@PostMapping("/webhook/bitbucket")
	public void bitbucketWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleBitbucket(JSONInformation);
	}

	@PostMapping("/webhook/jira")
	public void jiraWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleJira(JSONInformation);
	}

	@PostMapping("/webhook/bamboo")
	public void bambooWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleBamboo(JSONInformation);
	}
	
	@GetMapping("/king")
	public String getKing() {
		return null/*RoleBotListener.getKing()*/;
	}
	
	@GetMapping("/auditPlayer")
	public String auditPlayer(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		JSONObject json = new JSONObject();
		//json.put("result", RBL.audit(JSONInformation.getLong("player_id")));
		return json.toString();
	}
	
	@GetMapping("/listMembers")
	public String listMembers() throws JSONException {
		//return RBL.getPlayerList().toString();
		return "";
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
		
			Message discordSentMessage = bitBucket.sendMessageEmbeds(discordMessage).complete();
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

	private void handleJira(JSONObject jsonInformation) throws JSONException {
		String event = jsonInformation.getString("issue_event_type_name");
		String issueKey = jsonInformation.getJSONObject("issue").getString("key");
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(7, 70, 166));
		boolean process = false;
		if(event.equals("issue_generic")){
			// moved to a different developement
			String movedTo = jsonInformation.getJSONObject("changelog").getJSONArray("items").getJSONObject(0).getString("toString");
			String movedFrom = jsonInformation.getJSONObject("changelog").getJSONArray("items").getJSONObject(0).getString("fromString");
			String transition = "Issue was moved from " + movedFrom + " to " + movedTo;
			eb.setTitle("Bug: " + issueKey + " has been moved to " + movedTo);
			eb.setDescription("A bug that you submitted has changed developement status from  " + movedFrom + " to " + movedTo);
			eb.setTimestamp(Instant.now());
			process = true;
		} else if(event.equals("issue_commented")){
			// issue was commented on
			String author = jsonInformation.getJSONObject("comment").getJSONObject("author").getString("displayName");
			String comment = jsonInformation.getJSONObject("comment").getString("body");
			eb.setTitle("Bug: " + issueKey + " had a commented added to the ticket");
			eb.setDescription(author + " commented on a bug that you submitted.\n Comment: " + comment);
			eb.setTimestamp(Instant.now());
			process = true;
		} else if(event.equals("issue_created")){
			// issue what created
			eb.setTitle("Bug has been created in the workflow.");
			eb.setDescription("Thank you for your bug report. I will get to it as soon as I can.");
			eb.setFooter("Issue key: " + issueKey);
			process = true;
		}

		if(process){
			DataCacher<BugReport> bugs = new DataCacher<>("bug reports");
			for(BugReport br : bugs.getData()){
				if(br.getIssueNumber().equals(issueKey)){
					jdaBot.getGuildById(App.config.getGuildID()).getMemberById(br.getReporterId()).getUser().openPrivateChannel().complete().sendMessageEmbeds(eb.build()).queue();
				}
			}
		}
	}

}
