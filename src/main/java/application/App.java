package application;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import data.ConfigLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 
 * @author Ben Shabowski
 *
 */
@SpringBootApplication(scanBasePackages = {"bot", "controllers"})
@EnableJpaRepositories({"data.database"})
@EntityScan({"data.database"})
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

		// Stuff for SQL
		props.setProperty("spring.datasource.url", "jdbc:sqlserver://zgamelogic.com;databaseName=" + config.getDatabaseName());
		props.setProperty("spring.datasource.username", config.getSqlUsername());
		props.setProperty("spring.datasource.password", config.getSqlPassword());
		props.setProperty("spring.datasource.driver-class-name", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		props.setProperty("spring.jpa.hibernate.ddl-auto", "update");
		props.setProperty("spring.jpa.show-sql", "true");
		props.setProperty("org.hibernate.dialect.MySQLInnoDBDialect", "true");
		props.setProperty("spring.jpa.properties.hibernate.enable_lazy_load_no_trans", "true");
		props.setProperty("spring.jpa.properties.hibernate.show_sql", "false");

		app.setDefaultProperties(props);
		app.run(args);
	}
}
