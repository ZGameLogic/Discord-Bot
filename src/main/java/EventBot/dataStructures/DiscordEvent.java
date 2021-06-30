package EventBot.dataStructures;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class DiscordEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Calendar date;
	private String name;
	private String description;
	private String messageID;
	private Set<String> userIDs;
	private String filePath;
	private String guildID;
	private String textChannelID;
	
	public DiscordEvent(Calendar date, String name, String description) {
		this.date = date;
		this.name = name;
		this.description = description;
		userIDs = new HashSet<String>();
		filePath = "BotData\\Events\\" + name + ".event";
	}
	
	private String timeRemaining() {
		long millis = date.getTime().getTime() - System.currentTimeMillis();
		return String.format("%02d day(s) %02d hour(s) and %02d minute(s)", TimeUnit.MILLISECONDS.toDays(millis),
                TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis)),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
	}
	
	public MessageEmbed getMessage() {
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setTitle(name);
		eb.setDescription(description);
		
		eb.addField(new Field("Time", date.getTime().toString(), false));
		eb.addField("React with the bell to recieve notifications.", "You will get a day of, 15 minutes before, and time change message whenever applicable.", false);
		
		eb.setFooter("Time until event:" + timeRemaining());
		
		return eb.build();
	}

	public Calendar getDate() {
		return date;
	}

	public String getMessageID() {
		return messageID;
	}

	public String getGuildID() {
		return guildID;
	}

	public String getTextChannelID() {
		return textChannelID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Set<String> getUserIDs() {
		return userIDs;
	}

	public void setUserIDs(Set<String> userIDs) {
		this.userIDs = userIDs;
	}
	
	public String getFilePath() {
		return filePath;
	}
}
