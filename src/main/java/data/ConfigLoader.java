package data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;

@Configuration
@PropertySource("File:discordbot.properties")
@Getter
public class ConfigLoader {

	@Value("${bot.token}")
	private String botToken;

	@Value("${shlongshot.id}")
	private String shlongshotId;
	
	@Value("${keystore.password}")
	private String keystorePassword;
	
	@Value("${keystore.location}")
	private String keystoreLocation;

	@Value("${use.ssl:false}")
	private boolean useSSL;

	@Value("${Webhook.port:2001}")
	private int webHookPort;

	@Value("${database.name}")
	private String databaseName;

	@Value("${sql.username}")
	private String sqlUsername;

	@Value("${sql.password}")
	private String sqlPassword;

	@Value("${twilio.sid}")
	private String twilioSID;

	@Value("${twilio.token}")
	private String twilioToken;

	@Value("${twilio.number}")
	private String twilioNumber;
}
