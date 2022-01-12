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
	
	@Value("${keystore.password}")
	private String keystorePassword;
	
	@Value("${keystore.location}")
	private String keystoreLocation;
	
	@Value("${Meme.Database.Location}")
	private String databaseLocation;
	
	@Value("${Meme.Storage.Location}")
	private String memeStorageLocation;
	
	@Value("${Java.runtime}")
	private String javaRuntime;
	
	@Value("${Admin.password}")
	private String admin;
	
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
	
	public String getPassword() {
		return admin;
	}
	
	public LinkedList<Long> getBitbucketGuildIDs(){
		return stringToLongList(bitbucketGuildIDs);
	}
	
	public String getJavaRuntime() {
		return javaRuntime;
	}
	
	public String getKeystorePassword() {
		return keystorePassword;
	}
	
	public String getKeystoreLocation() {
		return keystoreLocation;
	}
	
	public int getWebHookPort() {
		return webHookPort;
	}
	
	public String getDatabaseLocation() {
		return databaseLocation;
	}

	public String getMemeStorageLocation() {
		return memeStorageLocation;
	}
	
	public String getBotToken() {
		return botToken;
	}

	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	public void setDatabaseLocation(String databaseLocation) {
		this.databaseLocation = databaseLocation;
	}

	public void setMemeStorageLocation(String memeStorageLocation) {
		this.memeStorageLocation = memeStorageLocation;
	}
	
	
}
