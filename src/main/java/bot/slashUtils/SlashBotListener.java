package bot.slashUtils;

import java.awt.Color;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.party.PartyBotListener;
import controllers.team.Team;
import controllers.team.TeamGenerator;
import controllers.team.TeamGenerator.GroupCreationException;
import controllers.team.TeamGenerator.TeamNameException;
import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class SlashBotListener extends ListenerAdapter {
	private Logger logger = LoggerFactory.getLogger(SlashBotListener.class);
	
	private String lastTeamGen;
	
	private PartyBotListener PBL;
	private ConfigLoader CL;
	
	public SlashBotListener(PartyBotListener PBL, ConfigLoader CL) {
		this.PBL = PBL;
		this.CL = CL;
	}
	
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		lastTeamGen = "";
		logger.info("Slash bot listener activated");
		CommandListUpdateAction guild = event.getJDA().getGuildById(CL.getGuildID()).updateCommands();
		CommandListUpdateAction global = event.getJDA().updateCommands();
		
		// Team commands
		global.addCommands(new CommandData("teams-help", "PMs the user a message for helping them generate a team"));
		global.addCommands(new CommandData("teams-generate-again", "Runs the last team generation command again"));
		global.addCommands(new CommandData("teams-generate", "Generates teams based off an inputted command")
				.addOption(OptionType.STRING, "command", "Command to generate teams", true));
		
		// Party bot commands
		guild.addCommands(new CommandData("create-text", "Creates a text chatroom that only people in the voice channel can see"));
		guild.addCommands(new CommandData("rename-chatroom", "Renames chatroom to a new name")
				.addOption(OptionType.STRING, "name", "Chatroom name", true));
		try {
			guild.submit();
			guild.complete();
		} catch (Exception e) {
			logger.info("Too many guild update commands for today");
		}
		
		try {
			global.submit();
			global.complete();
		} catch (Exception e) {
			logger.info("Too many global update commands for today");
		}
	}
	
	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		logger.info("Slash command recieved");
		switch(event.getName()) {
		case "teams-generate":
			generateTeam(event, event.getOption("command").getAsString());
			break;
		case "teams-generate-again":
			generateTeam(event, lastTeamGen);
			break;
		case "teams-help":
			sendTeamHelp(event);
			break;
		case "create-text":
			PBL.createTextChannel(event);
			break;
		case "rename-chatroom":
			PBL.renameChannel(event);
			break;
		}
	}
	
	private void generateTeam(SlashCommandEvent event, String command) {
		LinkedList<Team> teams;
		try {
			teams = TeamGenerator.generateTeams(command);
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.magenta);
			eb.setTitle("Team Generator");
			eb.setFooter(command);
			lastTeamGen = command;
			for(Team t : teams) {
				eb.addField(t.getTb().getName(), t.toPlayerString(), true);
			};
			
			event.reply("Generated teams").queue();
			try {
				event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
			} catch (IllegalStateException e) {
				
			}
		} catch (GroupCreationException | TeamNameException e) {
			event.reply(e.getMessage()).queue();
		}
	}
	
	private void sendTeamHelp(SlashCommandEvent event) {
		event.reply("Help message sent to your inbox").complete();
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.magenta);
		eb.setTitle("Team generator help");
		
		eb.addField("Simple", 
				"Simply type everyones name in as the command space delimited.\n"
				+ "Example: Ben Reba Jason Rob"
				, false);
		
		eb.addField("Complex", 
				"[team names/arguments] players space delmited {groups of players} <avoid>\n<team name> -o -m[max number]\n"
				+ "Lets say we want 2 teams, with a max 3 people each, and a spectator team to handle all the overflow. We also want Ben and Reba always on the same team. "
				+ "We also do not want Rob and Gred to be on the same team, and we dont want Anthony and Charlie on the same team either.\n"
				+ "That command would look like this:\n"
				+ "[One -m3, Two -m3, Spectators -o] {Ben Reba} Jason Rob Charlie Anthony Ethan Greg JJ <3 7, 5 4>\n"
				+ "Its important to note that the numbers in the <> represent the indecies of the players/groups we want to avoid, with any group counting as one (reguardless of the number of names in it)"
				, false);
		
		MessageEmbed embed = eb.build();
		event.getUser().openPrivateChannel().complete().sendMessageEmbeds(embed).complete();
	}
}
