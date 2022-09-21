package webhook.listeners;

import java.awt.Color;

import controllers.atlassian.BitbucketInterfacer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import data.serializing.DataRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WebHookReactionListener extends ListenerAdapter {

private static Logger logger = LoggerFactory.getLogger(WebHookReactionListener.class);
	
	private static TextChannel channel;
	private static ConfigLoader cl;
	private static JDA bot;
	private static String password;
	private static DataRepository<MessageID> messageID;
	private static boolean botReady;
	
	public WebHookReactionListener(ConfigLoader cl) {
		WebHookReactionListener.cl = cl;
		botReady = false;
		password = cl.getAdminPassword();
		messageID = new DataRepository<>("bitbucket message");
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		bot = event.getJDA();
		logger.info("Webhook Reaction Listener activated");
		botReady = true;
		channel = bot.getGuildById(cl.getGuildID()).getTextChannelById(cl.getBitbucketID());
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(!event.getUser().isBot() && event.getChannel().getIdLong() == channel.getIdLong()) {
			if(event.getReaction().toString().contains("RE:U+1f3d7")){
				
				Message m = event.retrieveMessage().complete();
				EmbedBuilder eb = new EmbedBuilder(m.getEmbeds().get(0));
				
				eb.setColor(Color.BLUE);
				m.editMessageEmbeds(eb.build()).complete();
				m.clearReactions().queue();
				
				MessageID  mid = new MessageID(event.getMessageIdLong());
				messageID.saveSerialized(mid, "id");
				
				try {
					JSONObject resultPull = new JSONObject(BitbucketInterfacer.createPullRequest(event.retrieveMember().complete().getEffectiveName()));
					String id = resultPull.getString("id");
					BitbucketInterfacer.mergePullRequest(id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void changeStatus(Color color) {
		while(!botReady) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(messageID.getFiles().length > 0) {
			long message = messageID.loadSerialized("id").getIdLong();
			logger.info("Updating message id: " + message);
			EmbedBuilder eb = new EmbedBuilder(channel.retrieveMessageById(message).complete().getEmbeds().get(0));
			eb.setColor(color);
			channel.editMessageEmbedsById(message, eb.build()).queue();
		}
	}
}
