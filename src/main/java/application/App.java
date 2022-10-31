package application;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import data.ConfigLoader;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 
 * @author Ben Shabowski
 *
 */
@SpringBootApplication(scanBasePackages = {"bot"})
@EnableScheduling
public class App {
	public static ConfigLoader config;
	private static Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(App.class);
		
		// Load config
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("data");
		context.refresh();
		config = context.getBean(ConfigLoader.class);
		context.close();

		Properties props = new Properties();
		props.setProperty("server.port", config.getWebHookPort() + "");
		props.setProperty("spring.main.banner-mode", "off");
		props.setProperty("logging.level.root", "INFO");

		// SSL stuff
		if(config.isUseSSL()) {
			logger.info("Turning SSL on");
			props.setProperty("server.ssl.enabled", config.isUseSSL() + "");
			props.setProperty("server.ssl.key-store", config.getKeystoreLocation());
			props.setProperty("server.ssl.key-alias", "tomcat");
			props.setProperty("server.ssl.key-store-password", config.getKeystorePassword());
		}
		app.setDefaultProperties(props);
		app.run(args);
	}
}
