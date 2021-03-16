package general.listeners;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PrivateMessageListener extends ListenerAdapter {
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		System.out.println("Private Message Listner started...");
	}
	
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		
		if (!event.getAuthor().isBot()) {
			
			EmbedBuilder eb = new EmbedBuilder();

			eb.setTitle("Uh oh");
			eb.setColor(Color.magenta);
			eb.setDescription("My master hasn't programed this feature into me yet.");
			eb.addField("Heres a neat skyrim pic for ya", "Why yes, it is random each time", false);
			eb.setImage("http://zgamelogic.com/skyrim/image" + (int) ((Math.random() * 40) + 1) + ".jpg");
			
			MessageEmbed embed = eb.build();

			event.getAuthor().openPrivateChannel().complete().sendMessage(embed).complete();

		}
	}
}
