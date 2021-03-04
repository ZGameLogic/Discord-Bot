package data;

import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class EventField {

	private String name;
	private String value;
	private boolean inLine;
	
	public EventField(String name, String value, boolean inLine) {
		this.name = name;
		this.value = value;
		this.inLine = inLine;
	}
	
	public EventField(String name, String value) {
		this(name, value, false);
	}
	
	public Field getField() {
		return null;
	}
}
