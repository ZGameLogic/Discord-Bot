package bot;

import javax.annotation.PostConstruct;

import bot.listeners.*;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.cardData.cards.CardData;
import data.database.cardData.cards.CardDataRepository;
import data.database.cardData.guild.GuildCardDataRepository;
import data.database.cardData.player.PlayerCardDataRepository;
import data.database.curseforge.CurseforgeRepository;
import data.database.devopsData.DevopsDataRepository;
import data.database.guildData.GuildDataRepository;
import data.database.planData.PlanRepository;
import data.database.userData.UserDataRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import application.App;
import data.ConfigLoader;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
public class Bot {

	@Autowired
	private GuildDataRepository guildData;
	@Autowired
	private PlanRepository planRepository;
	@Autowired
	private UserDataRepository userData;
	@Autowired
	private DevopsDataRepository devopsDataRepository;
	@Autowired
	private CardDataRepository cardDataRepository;
	@Autowired
	private GuildCardDataRepository guildCardDataRepository;
	@Autowired
	private PlayerCardDataRepository playerCardDataRepository;
	@Autowired
	private CurseforgeRepository curseforgeRepository;

	private final static String TITLE = "\r\n" +
			"   ____  ___  _                   _   ___      _  ______  \r\n" + 
			"  / /\\ \\|   \\(_)___ __ ___ _ _ __| | | _ ) ___| |_\\ \\ \\ \\ \r\n" + 
			" < <  > > |) | (_-</ _/ _ \\ '_/ _` | | _ \\/ _ \\  _|> > > >\r\n" + 
			"  \\_\\/_/|___/|_/__/\\__\\___/_| \\__,_| |___/\\___/\\__/_/_/_/ \r\n" + 
			"  v3.0.0\tBen Shabowski\r\n" +
			"";
	
	private final Logger logger = LoggerFactory.getLogger(Bot.class);
	private CardBot CB;
	private CurseForgeBot CFB;
	private JDA bot;

	@PostConstruct
	public void start() {
		ConfigLoader config = App.config;
		System.out.println(TITLE);
		
		// Create bot
		JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
		bot.enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT);
		bot.enableCache(CacheFlag.ACTIVITY);
		bot.enableIntents(GatewayIntent.GUILD_MEMBERS);
		bot.setMemberCachePolicy(MemberCachePolicy.ALL);
		bot.setEventPassthrough(true);

		LinkedList<AdvancedListenerAdapter> listeners = new LinkedList<>();

		listeners.add(new PartyBot(guildData));
		listeners.add(new WordleBot());
		listeners.add(new GeneralListener(guildData));
		listeners.add(new PlannerBot(planRepository, userData, guildData));
		listeners.add(new DevopsBot(devopsDataRepository, guildData));
		CB = new CardBot(guildData, cardDataRepository, guildCardDataRepository, playerCardDataRepository);
		listeners.add(CB);
		CFB = new CurseForgeBot(curseforgeRepository, guildData);
		listeners.add(CFB);
		// Add listeners
		for(ListenerAdapter a : listeners){
			bot.addEventListeners(a);
		}

		this.bot = bot.build();
		
		// Login
		try {
			this.bot.awaitReady();
		} catch (InterruptedException e) {
			logger.error("Unable to launch bot");
		}
	}

	@Scheduled(cron = "0 */5 * * * *")
	private void fiveMinuteTask() {
		CB.fiveMinuteTasks();
		CFB.update();
	}

	@PostMapping("/api/message")
	private String postMessage(@RequestBody String value) throws JSONException {
		JSONObject json = new JSONObject(value);
		if(!json.has("token")) return "Invalid token";
		if(!json.getString("token").equals(App.config.getApiToken())) return "Invalid token";
		boolean toGuild = json.getBoolean("to guild");
		if(toGuild){
			String guildId = json.getString("guild id");
			String channelId = json.getString("channel id");
			String message = json.getString("message");
			Guild guild = bot.getGuildById(guildId);
			TextChannel channel = guild.getTextChannelById(channelId);
			channel.sendMessage(message).queue();
			return "Message sent to " + guild.getName() + "/" + channel.getName();
		} else {
			String userId = json.getString("user id");
			String message = json.getString("message");
			User user = bot.getUserById(userId);
			PrivateChannel channel = user.openPrivateChannel().complete();
			channel.sendMessage(message).queue();
			return "Message sent to " + user.getName();
		}
	}

	@PostMapping("/api/cards")
	private void addCards(@RequestBody String value) throws JSONException {
		JSONObject json = new JSONObject(value);
		if(!json.has("token")) return;
		if(!json.getString("token").equals(App.config.getApiToken())) return;
		int index = json.getInt("id start");
		String collection = json.getString("collection");
		LinkedList<CardData> newCards = new LinkedList<>();
		JSONArray jsonCards = json.getJSONArray("cards");
		for(int i = 0; i < jsonCards.length(); i++){
			JSONObject card = jsonCards.getJSONObject(i);
			CardData newCard = new CardData();
			newCard.setId(index + i);
			newCard.setName(card.getString("name"));
			newCard.setCollection(collection);
			if(card.has("rarity")){
				newCard.setRarity(card.getInt("rarity"));
			} else {
				newCard.setRarity(new Random().nextInt(10) + 1);
			}
			newCards.add(newCard);
		}
		cardDataRepository.saveAll(newCards);
	}

	@PostMapping(value = "/sms")
	private void receiveMessage(@RequestBody String body) throws URISyntaxException {
		List<NameValuePair> params = URLEncodedUtils.parse(new URI("?" + body), StandardCharsets.UTF_8);
		Map<String, String> mapped = new HashMap<>();
		for (NameValuePair param : params) {
			mapped.put(param.getName(), param.getValue());
		}
		bot.getUserById(232675572772372481L).openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Text message received from number: " + mapped.get("From") + "\n" +
				"Body: " + mapped.get("Body"))
				.addActionRow(Button.primary("reply_text", "respond")).queue());
		log.info("Text message from: " + mapped.get("From"));
		log.info("Body: " + mapped.get("Body"));
	}
}
