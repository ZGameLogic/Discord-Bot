import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import bot.Bot;

@SpringBootApplication(scanBasePackages = {"webhook"})
@PropertySource("application.properties")
public class app {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(app.class);
		Properties props = new Properties();
		props.setProperty("server.port", "2000");
		props.setProperty("spring.main.banner-mode", "off");
		props.setProperty("logging.pattern.console", "");
		props.setProperty("logging.level.root", "INFO");
        app.setDefaultProperties(props);
        app.run(args);
		new Bot(args);
	}
}
