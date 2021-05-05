package bot;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import EventBot.listeners.EventBotListener;
import codeBot.listeners.CodeBotListener;
import data.ConfigLoader;
import general.listeners.OneTimeMessageListener;
import musicBot.listeners.MusicBotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import partybot.listeners.PartyRoomListener;
import webhook.listeners.WebHookListener;
import webhook.listeners.WebHookReactionListener;

@SuppressWarnings("unused")
public class Bot {
	
	private static String TITLE = "\r\n" + 
			"   ____  ___  _                   _   ___      _  ______  \r\n" + 
			"  / /\\ \\|   \\(_)___ __ ___ _ _ __| | | _ ) ___| |_\\ \\ \\ \\ \r\n" + 
			" < <  > > |) | (_-</ _/ _ \\ '_/ _` | | _ \\/ _ \\  _|> > > >\r\n" + 
			"  \\_\\/_/|___/|_/__/\\__\\___/_| \\__,_| |___/\\___/\\__/_/_/_/ \r\n" + 
			"  v1.1.0\tBen Shabowski\tJacob Marszalek\r\n" + 
			"";
	
	private Logger logger = LoggerFactory.getLogger(Bot.class);

	public Bot(String[] args, ConfigLoader config) {
		
		System.out.println(TITLE);

		JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
		bot.enableIntents(GatewayIntent.GUILD_PRESENCES);
		bot.enableCache(CacheFlag.ACTIVITY);
		
		if(args.length > 0) {
			LinkedList<String> arguments = new LinkedList<String>(Arrays.asList(args));
			if(arguments.contains("party")) {
				bot.addEventListeners(new PartyRoomListener(config));
			}
			if(arguments.contains("code")) {
				bot.addEventListeners(new CodeBotListener(config));
			}
			if(arguments.contains("webhook")) {
				bot.addEventListeners(new WebHookReactionListener(config));
			}
			if(arguments.contains("music")) {
				bot.addEventListeners(new MusicBotListener(config));
			}
			if(arguments.contains("event")) {
				bot.addEventListeners(new EventBotListener(config));
				bot.addEventListeners(new WebHookReactionListener(config));
			}
		}else {
			bot.addEventListeners(new PartyRoomListener(config));
			bot.addEventListeners(new CodeBotListener(config));
			bot.addEventListeners(new WebHookReactionListener(config));
			bot.addEventListeners(new MusicBotListener(config));
			bot.addEventListeners(new EventBotListener(config));
		}		
		
		// Login
		try {
			JDA jdaBot = bot.build().awaitReady();
			
			if(args.length > 0) {
				LinkedList<String> arguments = new LinkedList<String>(Arrays.asList(args));
				if(arguments.contains("webhook")) {
					new WebHookListener(config, jdaBot);
				}
			}else {
				new WebHookListener(config, jdaBot);
			}
		} catch (LoginException | InterruptedException e) {
			logger.error("Unable to launch bot");
		}		
	}

	

}
