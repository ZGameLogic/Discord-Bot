package test;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import data.ConfigLoader;

public class TestTester {
	
	private static ConfigLoader config;
	
	@BeforeClass
	public static void createConfig() {
		// Load config
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("data");
		context.refresh();
		config = context.getBean(ConfigLoader.class);
		context.close();
	}

	@Test
	public void test() {
		assertEquals(true, true);
	}

}
