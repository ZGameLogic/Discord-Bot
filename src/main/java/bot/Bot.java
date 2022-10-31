package bot;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import application.App;
import data.ConfigLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@RestController
public class Bot {

	private JDA jdaBot;
	private static String TITLE = "\r\n" + 
			"   ____  ___  _                   _   ___      _  ______  \r\n" + 
			"  / /\\ \\|   \\(_)___ __ ___ _ _ __| | | _ ) ___| |_\\ \\ \\ \\ \r\n" + 
			" < <  > > |) | (_-</ _/ _ \\ '_/ _` | | _ \\/ _ \\  _|> > > >\r\n" + 
			"  \\_\\/_/|___/|_/__/\\__\\___/_| \\__,_| |___/\\___/\\__/_/_/_/ \r\n" + 
			"  v3.0.0\tBen Shabowski\r\n" +
			"";
	
	private Logger logger = LoggerFactory.getLogger(Bot.class);

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
		
		// Add listeners
		
		// Login
		try {
			jdaBot = bot.build().awaitReady();
		} catch (InterruptedException e) {
			logger.error("Unable to launch bot");
		}
	}
}
