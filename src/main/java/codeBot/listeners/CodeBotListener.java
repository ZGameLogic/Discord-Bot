package codeBot.listeners;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codeBot.runners.JavaRunner;
import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CodeBotListener extends ListenerAdapter {

	private Logger logger = LoggerFactory.getLogger(CodeBotListener.class);
	private File codeBase;
	private File runtime;
	private LinkedList<Long> CodeGuildIds;

	public CodeBotListener(ConfigLoader cl) {
		codeBase = new File("BotData\\Code base");
		if (!codeBase.exists())
			codeBase.mkdir();
		CodeGuildIds = cl.getCodeGuildIDs();
		runtime = new File(cl.getJavaRuntime());
	}

	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Code bot Listener started...");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!event.getAuthor().isBot() && event.getMessage().getContentDisplay().startsWith("compile")) {
			
			if(event.isFromGuild()) {
				if(CodeGuildIds.contains(event.getGuild().getIdLong())){
					if (event.getMessage().getContentDisplay().contains("```")) {
						handleCodeInput(event);
					}
				}
			}else {
				if (event.getMessage().getContentDisplay().contains("```")) {
					handleCodeInput(event);
				} else {
					privateMessageNotReady(event);
				}
			}
		}
	}

	private void handleCodeInput(MessageReceivedEvent event) {
		String message = event.getMessage().getContentDisplay();
		int startIndex = message.indexOf("```") + 3;
		int endIndex = message.lastIndexOf("```");

		String code = message.substring(startIndex, endIndex);

		if (code.startsWith("java")) {
			JavaRunner.runJavaCode(code.replaceFirst("java\n", ""), event, codeBase, runtime);
		} else {
			privateMessageNotReady(event);
		}
	}

	private void privateMessageNotReady(MessageReceivedEvent event) {
		EmbedBuilder eb = new EmbedBuilder();

		eb.setTitle("Uh oh");
		eb.setColor(Color.magenta);
		eb.setDescription("My master hasn't programed this feature into me yet.");
		eb.addField("Heres a neat skyrim pic for ya", "Why yes, it is random each time", false);
		eb.setImage("http://zgamelogic.com/skyrim/image" + (int) ((Math.random() * 40) + 1) + ".jpg");

		MessageEmbed embed = eb.build();

		event.getAuthor().openPrivateChannel().complete().sendMessage(embed).complete();
	}
}
