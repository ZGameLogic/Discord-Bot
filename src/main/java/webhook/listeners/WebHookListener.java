package webhook.listeners;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties.Server;

import data.ConfigLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class WebHookListener {
	
	private Logger logger = LoggerFactory.getLogger(WebHookListener.class);
	
	private TextChannel channel;
	
	public WebHookListener(ConfigLoader cl, JDA bot) {
		new Server(cl.getWebHookPort()).start();
		
		channel = bot.getGuildById(cl.getBitbucketGuildIDs().get(0)).getTextChannelById(cl.getBitbucketGuildIDs().get(1));
		
		logger.info("Webhook Listener started...");
	}
	
	private class Server extends Thread {
		private int port;
		
		public Server(int port) {
			this.port = port;
		}
		
		public void run() {
			try {
				ServerSocket server = new ServerSocket(port);
				while(true) {
					new WebHookHandler(server.accept()).start();
				}
			} catch (IOException e) {
				logger.error("Unable to bind to port " + port);
			}
		}
	}
	
	private class WebHookHandler extends Thread {
		Socket client;
		
		public WebHookHandler(Socket client) {
			this.client = client;
		}
		
		public void run() {
			String message = "";
			try {
				logger.info("Handeling webhook");
				OutputStream out = client.getOutputStream();
				InputStreamReader in = new InputStreamReader(client.getInputStream());
				
				write("HTTP/1.1 200 OK", out);
				write("Access-Control-Allow-Origin: *", out);
				write("Content-Type: application/json", out);
				write("Content-Length:15", out);
				write("", out);
				write("{\"response\":15}", out);
				
				char inChar = ' ';
				
				System.out.println(1);
				
				while((inChar = (char) in.read()) != -1 && inChar != 65535) {
					message += inChar;
				}
				
				System.out.println(4);
				
				out.close();
				in.close();
				client.close();
			} catch (IOException e) {
				logger.error("IO Exceptions am I right");
			}
			
			System.out.println(6);
		
			
			JSONObject JSONInformation = new JSONObject(message.substring(message.indexOf("{")));
			
			if(message.contains("Bitbucket")) {
				handleBitbucket(JSONInformation);
			}else if(message.contains("Bamboo")) {
				handleBamboo(JSONInformation);
			}
			
			
			
		}
		
		private void write(String message, OutputStream os) {
			for(char x : message.toCharArray()) {
				try {
					os.write(x);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				os.write('\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void handleBitbucket(JSONObject message) {
			System.out.println("We got here");
			String commiter = message.getJSONObject("repository").getJSONObject("links").getJSONArray("self").getJSONObject(0).getString("href");
			System.out.println(commiter);
		}
		
		private void handleBamboo(JSONObject message) {
			
		}
		
		private MessageEmbed buildBitbucketMessage(String commiter, String repoLink) {
			return null;
		}
	}
}
