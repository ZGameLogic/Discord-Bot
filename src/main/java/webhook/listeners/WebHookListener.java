package webhook.listeners;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
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
		
		@SuppressWarnings("resource")
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
				
				while((inChar = (char) in.read()) != -1 && inChar != 65535) {
					message += inChar;
				}
				
				out.close();
				in.close();
				client.close();
			} catch (IOException e) {
				logger.error("IO Exceptions am I right");
			}
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
				
			}
		}
		
		private void handleBitbucket(JSONObject message) {
			String repoName = message.getJSONObject("repository").getString("name");
			String repoLink = message.getJSONObject("repository").getJSONObject("links").getJSONArray("self").getJSONObject(0).getString("href");
			String commiter = message.getJSONObject("actor").getString("displayName");
			String commiterLink = message.getJSONObject("actor").getJSONObject("links").getJSONArray("self").getJSONObject(0).getString("href");
			
			MessageEmbed discordMessage = buildBitbucketMessage(commiter, commiterLink, repoName, repoLink);
			
			channel.sendMessage(discordMessage).queue();
		}
		
		private void handleBamboo(JSONObject message) {
			
		}
		
		private MessageEmbed buildBitbucketMessage(String commiter, String commiterLink, String repoName, String repoLink) {
			EmbedBuilder eb = new EmbedBuilder();
			
			eb.setTitle("Bitbucket push to " + repoName, repoLink);
			eb.setColor(Color.GRAY);
			eb.setAuthor(commiter, commiterLink);
			
			try {
				JSONObject commits = new JSONObject(getCommitList());
				
				for(int i = 0; i < 5; i++) {
					String displayID = commits.getJSONArray("values").getJSONObject(i).getString("displayId");
					String message = commits.getJSONArray("values").getJSONObject(i).getString("message");
					
					eb.addField(displayID, message, false);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return eb.build();
		}
		
		private String getCommitList() throws IOException {
			String link = "https://zgamelogic.com:7990/rest/api/1.0/projects/BSPR/repos/discord-bot/commits/development?exclude=master";
			
			StringBuilder result = new StringBuilder();
		      URL url = new URL(link);
		      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		      conn.setRequestMethod("GET");
		      try (BufferedReader reader = new BufferedReader(
		                  new InputStreamReader(conn.getInputStream()))) {
		          for (String line; (line = reader.readLine()) != null; ) {
		              result.append(line);
		          }
		      }
		      return result.toString();
		}
	}
}
