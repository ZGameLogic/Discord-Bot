package listeners;

import java.lang.reflect.Field;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventHandler extends ListenerAdapter {

	
	
	
	
	
	
	
	
	
	private MessageEmbed createEvent() {
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.addField(null);
		
		return eb.build();
	}
	
}
