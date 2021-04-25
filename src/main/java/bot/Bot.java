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
	
	private static String TITLE = "   _________ _                       _  ______       _  ____   \r\n" + 
			"  / / /  _  (_)                     | | | ___ \\     | | \\ \\ \\  \r\n" + 
			" / / /| | | |_ ___  ___ ___  _ __ __| | | |_/ / ___ | |_ \\ \\ \\ \r\n" + 
			"< < < | | | | / __|/ __/ _ \\| '__/ _` | | ___ \\/ _ \\| __| > > >\r\n" + 
			" \\ \\ \\| |/ /| \\__ \\ (_| (_) | | | (_| | | |_/ / (_) | |_ / / / \r\n" + 
			"  \\_\\_\\___/ |_|___/\\___\\___/|_|  \\__,_| \\____/ \\___/ \\__/_/_/  \r\n" + 
			"v. 1.1.0                                                      \r\n" + 
			"                                                              ";
	
	private Logger logger = LoggerFactory.getLogger(Bot.class);

	public Bot(String[] args) {
		
		System.out.println(TITLE);
		
		// Load config
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("data");
		context.refresh();
		ConfigLoader config = context.getBean(ConfigLoader.class);
		context.close();

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
		}else {
			bot.addEventListeners(new PartyRoomListener(config));
			bot.addEventListeners(new CodeBotListener(config));
			bot.addEventListeners(new WebHookReactionListener(config));
			bot.addEventListeners(new MusicBotListener(config));
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
