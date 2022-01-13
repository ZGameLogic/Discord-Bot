package bot;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import bot.party.PartyBotListener;
import bot.slashUtils.SlashBotListener;
import data.ConfigLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import webhook.listeners.WebHookReactionListener;


@SuppressWarnings("unused")
public class Bot {
	
	private static String TITLE = "\r\n" + 
			"   ____  ___  _                   _   ___      _  ______  \r\n" + 
			"  / /\\ \\|   \\(_)___ __ ___ _ _ __| | | _ ) ___| |_\\ \\ \\ \\ \r\n" + 
			" < <  > > |) | (_-</ _/ _ \\ '_/ _` | | _ \\/ _ \\  _|> > > >\r\n" + 
			"  \\_\\/_/|___/|_/__/\\__\\___/_| \\__,_| |___/\\___/\\__/_/_/_/ \r\n" + 
			"  v2.0.0\tBen Shabowski\tJacob Marszalek\r\n" + 
			"";
	
	private Logger logger = LoggerFactory.getLogger(Bot.class);

	public Bot(String[] args, ConfigLoader config) {
		
		System.out.println(TITLE);

		// Create bot
		JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
		bot.enableIntents(GatewayIntent.GUILD_PRESENCES);
		bot.enableCache(CacheFlag.ACTIVITY);		
		
		// Add listeners
		PartyBotListener PBL = new PartyBotListener(config);
		WebHookReactionListener WHRL = new WebHookReactionListener(config);
		
		bot.addEventListeners(PBL);
		bot.addEventListeners(WHRL);
		bot.addEventListeners(new SlashBotListener(PBL, config));
		
		// Login
		try {
			JDA jdaBot = bot.build().awaitReady();
		} catch (LoginException | InterruptedException e) {
			logger.error("Unable to launch bot");
		}		
	}
}
