package data;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("File:discordbot.properties")
public class ConfigLoader {

	@Value("${bot.token}")
	private String botToken;
	
	@Value("${Party.Guild.IDs}")
	private String[] partyGuildIDs;
	
	@Value("${Event.Guild.IDs}")
	private String[] eventGuildIDs;
	
	@Value("${Event.Channel.IDs}")
	private String[] eventChannelIDs;
	
	@Value("${Meme.Guild.IDs}")
	private String[] memeGuildIDs;
	
	@Value("${Meme.Database.Location}")
	private String databaseLocation;
	
	@Value("${Meme.Storage.Location}")
	private String memeStorageLocation;
	
	@Value("${Code.Guild.IDs}")
	private String[] codeGuildIDs;
	
	@Value("${Java.runtime}")
	private String javaRuntime;
	
	@Value("${Admin.password}")
	private String password;
	
	@Value("${Webhook.port}")
	private int webHookPort;
	
	@Value("${Bitbucket.Guild.IDs}")
	private String[] bitbucketGuildIDs;
	
	private LinkedList<Long> stringToLongList(String[] array){
		LinkedList<Long> converted = new LinkedList<Long>();
		for(String x : array) {
			converted.add(Long.parseLong(x));
		}
		return converted;
	}
	
	public LinkedList<Long> getBitbucketGuildIDs(){
		return stringToLongList(bitbucketGuildIDs);
	}
	
	public String getJavaRuntime() {
		return javaRuntime;
	}
	
	public String getPassword() {
		return password;
	}
	
	public int getWebHookPort() {
		return webHookPort;
	}

	public LinkedList<Long> getCodeGuildIDs(){
		return stringToLongList(codeGuildIDs);
	}
	
	public LinkedList<Long> getEventGuildIDs(){
		return stringToLongList(eventGuildIDs);
	}
	
	public LinkedList<Long> getEventChannelIDs(){
		return stringToLongList(eventChannelIDs);
	}
	
	public String getDatabaseLocation() {
		return databaseLocation;
	}

	public String getMemeStorageLocation() {
		return memeStorageLocation;
	}

	public LinkedList<Long> getMemeGuildIDs(){
		return stringToLongList(memeGuildIDs);
	}
	
	public LinkedList<Long> getPartyGuildIDs(){
		return stringToLongList(partyGuildIDs);
	}
	
	public String getBotToken() {
		return botToken;
	}

	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	public void setPartyGuildIDs(String[] partyGuildIDs) {
		this.partyGuildIDs = partyGuildIDs;
	}

	public void setMemeGuildIDs(String[] memeGuildIDs) {
		this.memeGuildIDs = memeGuildIDs;
	}

	public void setDatabaseLocation(String databaseLocation) {
		this.databaseLocation = databaseLocation;
	}

	public void setMemeStorageLocation(String memeStorageLocation) {
		this.memeStorageLocation = memeStorageLocation;
	}
	
	
}
