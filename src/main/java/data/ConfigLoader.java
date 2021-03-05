package data;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:discordbot.properties")
public class ConfigLoader {

	@Value("${bot.token}")
	private String botToken;
	
	@Value("${Create.Voice.IDs}")
	private String[] createChatIDs;
	
	@Value("${Ignored.Voice.Channel.IDs}")
	private String[] ignoredChannelIDs;
	
	@Value("${Command.Text.Channel.IDs}")
	private String[] textChannelIDs;
	
	public LinkedList<Long> getTextChannelIDs(){
		return stringToLongList(textChannelIDs);
	}
	
	public LinkedList<Long> getCreateChatIDs(){
		return stringToLongList(createChatIDs);
	}
	
	public LinkedList<Long> getIgnoredChannelIDs(){
		LinkedList<Long> allIgnore = new LinkedList<Long>();
		allIgnore.addAll(stringToLongList(ignoredChannelIDs));
		allIgnore.addAll(stringToLongList(createChatIDs));
		return allIgnore;
	}
	
	public String getBotToken() {
		return botToken;
	}
	
	private LinkedList<Long> stringToLongList(String[] array){
		LinkedList<Long> converted = new LinkedList<Long>();
		for(String x : array) {
			converted.add(Long.parseLong(x));
		}
		return converted;
	}
}
