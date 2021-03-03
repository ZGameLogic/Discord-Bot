package data;

import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class DiscordEventField {

	private String name;
	private String value;
	private boolean inLine;
	
	public DiscordEventField(String name, String value, boolean inLine) {
		this.name = name;
		this.value = value;
		this.inLine = inLine;
	}
	
	public DiscordEventField(String name, String value) {
		this(name, value, false);
	}
	
	public Field getField() {
		return null;
	}
}
