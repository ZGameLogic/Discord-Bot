package bot;
import javax.security.auth.login.LoginException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import data.ConfigLoader;
import listeners.PrivateMessage;
import net.dv8tion.jda.api.JDABuilder;
import partybot.listeners.PartyRoom;
import twilio.TextMessageHandler;

public class Bot {

	public Bot() {
		
		// Load config
		
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("data");
		context.refresh();
		ConfigLoader config = context.getBean(ConfigLoader.class);
		context.close();

		JDABuilder bot = JDABuilder.createDefault(config.getBotToken());

		bot.addEventListeners(new PartyRoom(config));
		bot.addEventListeners(new PrivateMessage());
		
		TextMessageHandler TMH = new TextMessageHandler();
		
		// Login
		try {
			bot.build().awaitReady();
		} catch (LoginException | InterruptedException e) {
			System.out.println("Unable to launch bot");
		}
		
		try {
			System.out.println(TMH.getOutputQueue().take().get("Body"));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
	}

	

}
