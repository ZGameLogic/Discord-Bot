package test.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class TestListener extends ListenerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(TestListener.class);
	
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Started test listener");
		CommandListUpdateAction commands = event.getJDA().getGuildsByName("MemeBot test server", true).get(0).updateCommands();
		commands.addCommands(new CommandData("test", "This is a command to test buttons"));
		commands.complete();
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event){
		
	}
	
	@Override
	public void onButtonClick(ButtonClickEvent event) {
		logger.info(event.getButton().getLabel());
		event.reply("This is the reply to the button").complete();
		System.out.println("Button clicked");
	}

	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		event.reply("This is the reply message").addActionRow(
				Button.primary("hello", "Click Me"),
				Button.success("success", "we did it")).queue();
	}
}
