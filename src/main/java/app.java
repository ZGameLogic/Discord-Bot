import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import bot.Bot;

@SpringBootApplication(scanBasePackages = {"webhook"})
@PropertySource("application.properties")
public class app {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(app.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "2000"));
        app.run(args);
		new Bot(args);
	}
}
