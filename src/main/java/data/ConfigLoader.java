package data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
		@PropertySource("File:discordbot.properties"),
		@PropertySource("File:local.properties")})
@Getter
public class ConfigLoader {

	@Value("${bot.token}")
	private String botToken;
	
	@Value("${keystore.password}")
	private String keystorePassword;
	
	@Value("${keystore.location}")
	private String keystoreLocation;

	@Value("${use.ssl:true}")
	private boolean useSSL;
	
	@Value("${Webhook.port}")
	private int webHookPort;
	
	@Value("${chatroom.cat.id}")
	private long chatroomCatID;
	
	@Value("${create.chat.id}")
	private long createChatID;
	
	@Value("${guild.id}")
	private long guildID;
	
	@Value("${afk.id:0}")
	private long AFKID;
	
	@Value("${bitbucket.id}")
	private long bitbucketID;
	
	@Value("${admin.password}")
	private String adminPassword;

	@Value("${jira.pat}")
	private String jiraPat;

	@Value("${bitbucket.pat}")
	private String bitbucketPat;
}
