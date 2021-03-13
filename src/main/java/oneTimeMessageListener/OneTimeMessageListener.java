package oneTimeMessageListener;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OneTimeMessageListener extends ListenerAdapter {

	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		
		EmbedBuilder eb = new EmbedBuilder();

		eb.setTitle("I need a new name!");
		eb.setColor(Color.magenta);
		eb.setDescription("I am in need of a new name at the request of my owner. Please select a name that you would like from the list bellow and react with your favorite name's number.");
		eb.addField("0) Keep it shlongbot", "Old tried and true am I right", false);
		eb.addField("1) zBot", "This one sparks joy", false);
		eb.addField("2) BotZ", "This one does not spark joy", false);
		eb.addField("4) Botty", "Thanks Reba for the suggestion", false);
		eb.addField("5) leBot", "Multi-language support coming soon", false);
		eb.addField("6) Im in love with you and I dont know how to tell you", "Uh, thanks", false);
		eb.addField("Reply to this post with your own name if you dont like any of these", "", false);
		eb.setFooter("Thanks for participating!");
		
		eb.setImage("http://zgamelogic.com/skyrim/image" + (int) ((Math.random() * 40) + 1) + ".jpg");
		
		
		MessageEmbed embed = eb.build();

		
		event.getJDA().getGuildById(330751526735970305l).getTextChannelById(330751526735970305l).sendMessage(embed).complete();

	
		
	}
}
