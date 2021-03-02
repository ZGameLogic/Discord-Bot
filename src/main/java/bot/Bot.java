package bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import listeners.PartyRoom;
import listeners.PrivateMessage;
import net.dv8tion.jda.api.JDABuilder;

public class Bot {

	public Bot() {
		
		File botInfoFile = new File("bot.info");
		
		String token = "";
		
		try {
			Scanner in = new Scanner(botInfoFile);
			token = in.nextLine();
			in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		JDABuilder bot = JDABuilder.createDefault(token);

		bot.addEventListeners(new PartyRoom());
		bot.addEventListeners(new PrivateMessage());
		
		// Login
		try {
			bot.build().awaitReady();
		} catch (LoginException | InterruptedException e) {
			System.out.println("Unable to launch bot");
		}
		
	}

	

}
