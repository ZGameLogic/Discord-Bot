package bot;

import java.awt.Color;
import java.time.Instant;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;

import bot.messageUtils.MessageListener;
import bot.minecraft.MinecraftListener;
import bot.pokemon.PokemonListener;
import controllers.atlassian.BitbucketInterfacer;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
	private NewsChannel announcments;

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
		bot.addEventListeners(new PokemonListener());
		bot.addEventListeners(new MinecraftListener());
		bot.addEventListeners(new MessageListener());
		
		// Login
		try {
			jdaBot = bot.build().awaitReady();
			bitBucket = jdaBot.getGuildById(config.getGuildID()).getTextChannelById(config.getBitbucketID());
			announcments = jdaBot.getGuildById(config.getGuildID()).getNewsChannels().get(0);
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

	@PostMapping("/webhook/jira/release")
	public void jiraWebhookRelease(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleJiraRelease(JSONInformation);
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

	@Scheduled(cron = "0 0 0,12 * * *")
	private void newDay(){
		RBL.newDay(); // new day
	}

	@Scheduled(cron = "0 0 23,11 * * *")
	private void hourBeforeNewDay(){
		RBL.hourBeforeNewDay(); // hour before new day
	}

	@Scheduled(cron = "0 * * * * *")
	private void minuteTask() throws NoSuchMethodException {
		RBL.minuteTasks(); // every minute
	}

	@Scheduled(cron = "0 0 0 25 12 ?")
	private void christmasStuff(){
		RBL.christmas(); // every christmas
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
		
			MessageEmbed discordMessage = BitbucketInterfacer.buildBitbucketMessage(commiter, commiterLink, repoName, repoLink);
		
			Message discordSentMessage = bitBucket.sendMessageEmbeds(discordMessage).complete();
			discordSentMessage.addReaction(Emoji.fromUnicode("U+1F3D7")).queue();
		}
	}

	private void handleJiraRelease(JSONObject jsonInformation) throws JSONException {
		String event = jsonInformation.getString("webhookEvent");
		if(event.equals("jira:version_released")) {
			String versionName = jsonInformation.getJSONObject("version").getString("name");
			String versionDescription = jsonInformation.getJSONObject("version").getString("description");
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(new Color(7, 70, 166));
			eb.setTitle(versionName + " update has been released!");
			eb.setDescription(versionDescription);
			eb.setTimestamp(Instant.now());
			announcments.sendMessageEmbeds(eb.build()).queue();
		}
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
			if(movedTo.equals("Done")){
				eb.setTitle("Bug: " + issueKey + " has been resovled", "https://zgamelogic.com:8080/projects/DB/issues/" + issueKey);
				eb.setDescription("Thank you so much for bringing this bug to my attention and the fix will be out in the next release.");
			} else {
				eb.setTitle("Bug: " + issueKey + " has been moved to " + movedTo, "https://zgamelogic.com:8080/projects/DB/issues/" + issueKey);
				eb.setDescription("A bug that you submitted has changed developement status from  " + movedFrom + " to " + movedTo);
			}
			eb.setTimestamp(Instant.now());
			process = true;
		} else if(event.equals("issue_commented")){
			// issue was commented on
			String author = jsonInformation.getJSONObject("comment").getJSONObject("author").getString("displayName");
			String comment = jsonInformation.getJSONObject("comment").getString("body");
			eb.setTitle("Bug: " + issueKey + " had a commented added to the ticket", "https://zgamelogic.com:8080/projects/DB/issues/" + issueKey);
			eb.setDescription(author + " commented on a bug that you submitted.\n Comment: " + comment);
			eb.setTimestamp(Instant.now());
			process = true;
		} else if(event.equals("issue_created")){
			// issue what created
			eb.setTitle("Bug has been created in the workflow.", "https://zgamelogic.com:8080/projects/DB/issues/" + issueKey);
			eb.setDescription("Thank you for your bug report. I will get to it as soon as I can.");
			eb.setFooter("Issue key: " + issueKey);
			process = true;
		}

		if(process){
			String userId = "";
			String optIn = "";
			Scanner input = new Scanner(jsonInformation.getJSONObject("issue").getJSONObject("fields").getString("description"));
			while(input.hasNextLine()){
				String line = input.nextLine();
				if(line.contains("Discord user ID: ")){
					userId = line.replace("Discord user ID: ", "");
				} else if(line.contains("Opt-in: ")){
					optIn = line.replace("Opt-in: ", "");
				}
			}
			input.close();
			if(optIn.equals("fasle")) return;
			if(!userId.equals("")) {
				User user = jdaBot.getUserById(userId);
				if (user != null) {
					user.openPrivateChannel().complete().sendMessageEmbeds(eb.build()).queue();
				} else {
					System.out.println("Cant find user: " + userId);
				}
			}
		}
	}
}
