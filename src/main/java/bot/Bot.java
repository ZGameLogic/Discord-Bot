package bot;

import javax.annotation.PostConstruct;

import bot.listeners.GeneralListener;
import bot.listeners.PartyBot;
import bot.listeners.PlannerBot;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.guildData.GuildDataRepository;
import data.database.planData.PlanRepository;
import data.database.userData.UserDataRepository;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import application.App;
import data.ConfigLoader;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.LinkedList;

@RestController
public class Bot {

	@Autowired
	private GuildDataRepository guildData;
	@Autowired
	private PlanRepository planRepository;
	@Autowired
	private UserDataRepository userData;

	private final static String TITLE = "\r\n" +
			"   ____  ___  _                   _   ___      _  ______  \r\n" + 
			"  / /\\ \\|   \\(_)___ __ ___ _ _ __| | | _ ) ___| |_\\ \\ \\ \\ \r\n" + 
			" < <  > > |) | (_-</ _/ _ \\ '_/ _` | | _ \\/ _ \\  _|> > > >\r\n" + 
			"  \\_\\/_/|___/|_/__/\\__\\___/_| \\__,_| |___/\\___/\\__/_/_/_/ \r\n" + 
			"  v3.0.0\tBen Shabowski\r\n" +
			"";
	
	private final Logger logger = LoggerFactory.getLogger(Bot.class);

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

		LinkedList<AdvancedListenerAdapter> listeners = new LinkedList<>();

		listeners.add(new PartyBot(guildData));
		listeners.add(new GeneralListener(guildData));
		listeners.add(new PlannerBot(planRepository, userData, guildData));

		// Add listeners
		for(ListenerAdapter a : listeners){
			bot.addEventListeners(a);
		}
		
		// Login
		try {
			bot.build().awaitReady();
		} catch (InterruptedException e) {
			logger.error("Unable to launch bot");
		}
	}
}
