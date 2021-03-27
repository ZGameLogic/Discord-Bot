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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import partybot.listeners.PartyRoomListener;
import webhook.listeners.WebHookListener;

@SuppressWarnings("unused")
public class Bot {
	
	private Logger logger = LoggerFactory.getLogger(Bot.class);

	public Bot(String[] args) {
		
		// Load config
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("data");
		context.refresh();
		ConfigLoader config = context.getBean(ConfigLoader.class);
		context.close();

		JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
		
		
		if(args.length > 0) {
			LinkedList<String> arguments = new LinkedList<String>(Arrays.asList(args));
			if(arguments.contains("party")) {
				bot.addEventListeners(new PartyRoomListener(config));
			}
			if(arguments.contains("code")) {
				bot.addEventListeners(new CodeBotListener(config));
			}
		}else {
			bot.addEventListeners(new PartyRoomListener(config));
			bot.addEventListeners(new CodeBotListener(config));
		}
		//bot.addEventListeners(new EventBotListener());
		//bot.addEventListeners(new OneTimeMessageListener());
		
		
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
