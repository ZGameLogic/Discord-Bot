package bot;

import java.awt.Color;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;

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
import data.database.arena.activity.ActivityRepository;
import data.database.arena.encounter.EncounterRepository;
import data.database.arena.misc.GameInformationRepository;
import data.database.arena.player.PlayerRepository;
import data.database.arena.shopItem.ShopItemRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import webhook.listeners.WebHookReactionListener;

@RestController
public class Bot {
	
	@Autowired
	PlayerRepository playerData;
	@Autowired
	GameInformationRepository gameData;
	@Autowired
	ShopItemRepository shopItemData;
	@Autowired
	EncounterRepository encounterData;
	@Autowired
	ActivityRepository activityData;
	
	private static String TITLE = "\r\n" + 
			"   ____  ___  _                   _   ___      _  ______  \r\n" + 
			"  / /\\ \\|   \\(_)___ __ ___ _ _ __| | | _ ) ___| |_\\ \\ \\ \\ \r\n" + 
			" < <  > > |) | (_-</ _/ _ \\ '_/ _` | | _ \\/ _ \\  _|> > > >\r\n" + 
			"  \\_\\/_/|___/|_/__/\\__\\___/_| \\__,_| |___/\\___/\\__/_/_/_/ \r\n" + 
			"  v2.0.0\tBen Shabowski\tJacob Marszalek\r\n" + 
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
		RoleBotListener RBL = new RoleBotListener(config, playerData, gameData, shopItemData, encounterData, activityData);
		
		bot.addEventListeners(PBL);
		bot.addEventListeners(WHRL);
		bot.addEventListeners(RBL);
		bot.addEventListeners(new SteamListener());
		bot.addEventListeners(new SlashBotListener(PBL, config, RBL));
		
		// Login
		try {
			JDA jdaBot = bot.build().awaitReady();
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
	
	@PostMapping("/webhook/bamboo")
	public void bambooWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		handleBamboo(JSONInformation);
	}
	
	@GetMapping("king")
	public String getKing() {
		return RoleBotListener.getKing();
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
}
