package bot;
import javax.security.auth.login.LoginException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import EventBot.listeners.EventBotListener;
import data.ConfigLoader;
import general.listeners.PrivateMessageListener;
import net.dv8tion.jda.api.JDABuilder;
import oneTimeMessageListener.OneTimeMessageListener;
import partybot.listeners.PartyRoomListener;

public class Bot {

	public Bot() {
		
		// Load config
		
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("data");
		context.refresh();
		ConfigLoader config = context.getBean(ConfigLoader.class);
		context.close();

		JDABuilder bot = JDABuilder.createDefault(config.getBotToken());

		bot.addEventListeners(new PartyRoomListener(config));
		bot.addEventListeners(new PrivateMessageListener());
		bot.addEventListeners(new EventBotListener());
		//bot.addEventListeners(new OneTimeMessageListener());
		
		// Login
		try {
			bot.build().awaitReady();
		} catch (LoginException | InterruptedException e) {
			System.out.println("Unable to launch bot");
		}		
	}

	

}
