import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import bot.Bot;
import data.ConfigLoader;

@SpringBootApplication(scanBasePackages = {"webhook"})
public class App {
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(App.class);

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
}
