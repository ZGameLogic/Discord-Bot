package general.listeners;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import partybot.dataStructures.PartyGuild;

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
		
		/*
		if(event.getMessage().getAttachments().size() > 0) {
			saveLocally(event.getMessage().getAttachments().get(0));
		}
		*/
		
		if (!event.getAuthor().isBot()) {
			EmbedBuilder eb = new EmbedBuilder();

			eb.setTitle("Uh oh");
			eb.setColor(Color.magenta);
			eb.setDescription("My master hasn't programed this feature into me yet.");
			eb.addField("Heres a neat skyrim pic for ya", "Why yes, it is random each time", false);
			eb.setImage("http://zgamelogic.com/skyrim/image" + (int) ((Math.random() * 40) + 1) + ".jpg");
			
			/*
			 * File imagePNG = new File("path to png");
			 * eb.setImage("attachment://image.png");
			 * event.getAuthor().openPrivateChannel().complete().sendMessage(embed).addFile(imagePNG, "image.png").complete();
			 */
			
			MessageEmbed embed = eb.build();

			event.getAuthor().openPrivateChannel().complete().sendMessage(embed).complete();

		}
		
		
	}
	
	
	 private void saveLocally(Message.Attachment attachment) {
	     attachment.downloadToFile()
	         .exceptionally(t ->
	         { // handle failure
	             t.printStackTrace();
	             return null;
	         });
	 }
	 

}
