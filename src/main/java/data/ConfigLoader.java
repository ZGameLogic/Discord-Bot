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
	
	@Value("${Party.Guild.IDs}")
	private String[] partyGuildIDs;
	
	@Value("${Meme.Guild.IDs}")
	private String[] memeGuildIDs;
	
	public LinkedList<Long> getMemeGuildIDs(){
		return stringToLongList(memeGuildIDs);
	}
	
	public LinkedList<Long> getPartyGuildIDs(){
		return stringToLongList(partyGuildIDs);
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
