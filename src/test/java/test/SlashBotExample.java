package test;

import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import bot.Bot;
import data.ConfigLoader;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class SlashBotExample {
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SlashBotExample.class);

		// Load config
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("data");
		context.refresh();
		ConfigLoader config = context.getBean(ConfigLoader.class);
		context.close();

		Properties props = new Properties();
		props.setProperty("server.port", config.getWebHookPort() + "");
		props.setProperty("spring.main.banner-mode", "off");
		props.setProperty("logging.level.root", "INFO");

		// SSL stuff

		props.setProperty("server.ssl.enabled", "true");

		props.setProperty("server.ssl.key-store", config.getKeystoreLocation());

		props.setProperty("server.ssl.key-alias", "tomcat");
		props.setProperty("server.ssl.key-store-password", config.getKeystorePassword());

		app.setDefaultProperties(props);
		app.run(args);
		new Bot(args, config);
	}
	
    public void BotTest(String[] args, ConfigLoader config) { 
    	
    	JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
		bot.enableIntents(GatewayIntent.GUILD_PRESENCES);
		bot.enableCache(CacheFlag.ACTIVITY);
		
    }
}