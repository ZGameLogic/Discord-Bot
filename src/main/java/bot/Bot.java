package bot;

import bot.listeners.*;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.cardData.cards.CardData;
import data.database.cardData.cards.CardDataRepository;
import data.database.cardData.guild.GuildCardDataRepository;
import data.database.cardData.player.PlayerCardDataRepository;
import data.database.guildData.GuildDataRepository;
import data.database.huntData.gun.HuntGunRepository;
import data.database.huntData.item.HuntItemRepository;
import data.database.huntData.randomizer.HuntRandomizerRepository;
import data.database.planData.PlanRepository;
import data.database.userAuthData.AuthData;
import data.database.userAuthData.AuthDataRepository;
import data.database.userData.UserDataRepository;
import data.intermediates.messaging.Message;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
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

	private final CardDataRepository cardDataRepository;
	private final AuthDataRepository authData;

	private final static String TITLE = "\r\n" +
			"███████╗██╗  ██╗██╗      ██████╗ ███╗   ██╗ ██████╗ ██████╗  ██████╗ ████████╗\n" +
			"██╔════╝██║  ██║██║     ██╔═══██╗████╗  ██║██╔════╝ ██╔══██╗██╔═══██╗╚══██╔══╝\n" +
			"███████╗███████║██║     ██║   ██║██╔██╗ ██║██║  ███╗██████╔╝██║   ██║   ██║   \n" +
			"╚════██║██╔══██║██║     ██║   ██║██║╚██╗██║██║   ██║██╔══██╗██║   ██║   ██║   \n" +
			"███████║██║  ██║███████╗╚██████╔╝██║ ╚████║╚██████╔╝██████╔╝╚██████╔╝   ██║   \n" +
			"╚══════╝╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚═════╝  ╚═════╝    ╚═╝   \n" +
			"                                                                              \n" +
			"  v3.0.0\tBen Shabowski";

	private final CardBot CB;
	private final JDA bot;

	@Autowired
	public Bot(
			GuildDataRepository guildData,
			PlanRepository planRepository,
			UserDataRepository userData,
			CardDataRepository cardDataRepository,
			GuildCardDataRepository guildCardDataRepository,
			PlayerCardDataRepository playerCardDataRepository,
			AuthDataRepository authData,
			HuntItemRepository huntItemRepository,
			HuntGunRepository huntGunRepository,
			HuntRandomizerRepository huntRandomizerRepository
	) {
		this.cardDataRepository = cardDataRepository;
		this.authData = authData;

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
		listeners.add(new DadBot());
		listeners.add(new GeneratorBot(guildData));
		// listeners.add(new VirusBot());
		listeners.add(new GeneralListener(guildData));
		PlannerBot PB = new PlannerBot(planRepository, userData, guildData);
		listeners.add(PB);
		CB = new CardBot(guildData, cardDataRepository, guildCardDataRepository, playerCardDataRepository);
		listeners.add(CB);
		listeners.add(new HuntShowdownBot(guildData, huntGunRepository, huntItemRepository, huntRandomizerRepository));
		// Add listeners
		for(ListenerAdapter a : listeners){
			bot.addEventListeners(a);
		}

		this.bot = bot.build();

		// Login
		try {
			this.bot.awaitReady();
		} catch (InterruptedException e) {
			log.error("Unable to launch bot");
		}
	}

	@Scheduled(cron = "0 */5 * * * *")
	private void fiveMinuteTask() {
		CB.fiveMinuteTasks();
	}

	@PostMapping("/api/verify/username")
	private String verifyUsername(@RequestBody String value) throws JSONException {
		JSONObject json = new JSONObject(value);
		AuthData ad = new AuthData();
		JSONObject returnObject = new JSONObject();

		ad.setUserId(json.getLong("userId"));
		ad.generateToken();
		User user = bot.getUserById(json.getLong("userId"));
		if(user == null) {
			returnObject.put("success", false);
			returnObject.put("message", "No user by that ID exists.");
			return returnObject.toString();
		}
		user.openPrivateChannel().queue(channel -> channel.sendMessage("A request has been received to sign into your discord account. Your identification token is: **" + ad.getToken() + "**. " +
				"If you did not request this, please ignore this message.").queue());
		returnObject.put("success", true);
		returnObject.put("message", "User has been notified on discord.");
		authData.save(ad);
		return returnObject.toString();
	}

	@PostMapping("/api/verify/token")
	private String verifyToken(@RequestBody String value) throws JSONException {
		JSONObject json = new JSONObject(value);
		AuthData ad = authData.getOne(Long.parseLong(json.getString("userId")));
		if(ad == null){ // check if the user exists
			if(ad.getToken() != -1) { // make sure they are waiting on a login token
				JSONObject returnObject = new JSONObject();
				returnObject.put("success", false);
				returnObject.put("message", "No user with that user ID is waiting to validate a token");
				return returnObject.toString();
			}
		}
		if(ad.getToken() != json.getLong("token")){ // if the token doesn't match
			JSONObject returnObject = new JSONObject();
			returnObject.put("success", false);
			returnObject.put("message", "That is the incorrect token");
			return returnObject.toString();
		}
		ad.setToken(-1);
		if(ad.getValidationCode() == null) ad.generateValidationCode();
		JSONObject returnObject = new JSONObject();
		returnObject.put("success", true);
		returnObject.put("validation code", ad.getValidationCode());
		authData.save(ad);
		return returnObject.toString();
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("message")
	private ResponseEntity sendMessage(@RequestBody Message message){
		try {
			log.info(message.toString());
			log.info(bot.getGuildById(message.getGuildId()).getName());
			bot.getGuildById(message.getGuildId())
					.getDefaultChannel().asTextChannel().sendMessage(
							EmbedMessageGenerator.message(message)
					).queue();
			return ResponseEntity.status(200).build();
		} catch (Exception ignored){
			return ResponseEntity.badRequest().build();
		}
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

	@GetMapping("health")
	private String healthCheck(){
		return "Healthy";
	}
}
