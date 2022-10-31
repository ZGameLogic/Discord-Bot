package bot.slashUtils;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import bot.role.RoleBotReady;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.party.PartyBotListener;
import controllers.dice.DiceRollingSimulator;
import controllers.team.Team;
import controllers.team.TeamGenerator;
import controllers.team.TeamGenerator.GroupCreationException;
import controllers.team.TeamGenerator.TeamNameException;
import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
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
		global.addCommands(Commands.slash("teams-help", "PMs the user a message for helping them generate a team"));
		global.addCommands(Commands.slash("teams-generate-again", "Runs the last team generation command again"));
		global.addCommands(Commands.slash("teams-generate", "Generates teams based off an inputted command")
				.addOption(OptionType.STRING, "command", "Command to generate teams", true));
		// Dice
		global.addCommands(Commands.slash("roll-dice", "Rolls a dice")
				.addOption(OptionType.INTEGER, "count", "Number of dice to be rolled", true)
				.addOption(OptionType.INTEGER, "faces", "Number of faces on each die", true));
		
		// Party bot commands
		guild.addCommands(Commands.slash("rename-chatroom", "Renames chatroom to a new name")
				.addOption(OptionType.STRING, "name", "Chatroom name", true));
		guild.addCommands(Commands.slash("limit", "Limits the amount of people who can enter a chatroom")
				.addOption(OptionType.INTEGER, "count", "Number of people allowed in the chatroom", true));
		
		// Role bot listener
		guild.addCommands(RoleBotReady.getCommands());

		// pokemon
		guild.addCommands(Commands.slash("pokemon", "Anything involving the pokemon functionality")
				.addSubcommands(new SubcommandData("lookup", "Lookup a pokemon by name")
						.addOption(OptionType.STRING, "name", "The name or pokedex index of the pokemon to lookup", true))
		);

		// minecraft
		guild.addCommands(Commands.slash("minecraft", "Anything involving the minecraft functionality")
				.addSubcommands(new SubcommandData("lookup", "Lookup a minecraft server")
						.addOptions(new OptionData(OptionType.STRING, "server", "The server to lookup", true)
								.addChoice("vanilla", "zgamelogic.com")
								.addChoice("ATM7", "zgamelogic.com:25566"))
		));

		// bug report
		guild.addCommands(Commands.slash("bug-report", "Submit a bug report"));
		
		try {
			guild.submit();
			guild.complete();
		} catch (Exception e) {
			logger.error("Too many guild update commands for today");
		}
		
		try {
			global.submit();
			global.complete();
		} catch (Exception e) {
			logger.error("Too many global update commands for today");
		}
	}

//	@Override
//	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
//		event.reply(event.getInteraction().getSelectedOptions().size() + "").queue();
//		event.getMessage().editMessageComponents().queue();
//	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		logger.info("Slash command received for " + event.getName() + " by " + event.getMember().getEffectiveName());
		switch(event.getName()) {
		case "pray":
			event.reply("Thank you, my child.").queue();
//			LinkedList<EntitySelectMenu.SelectTarget> stuff = new LinkedList<>();
//			stuff.add(EntitySelectMenu.SelectTarget.ROLE);
//			event.reply("Text").setActionRow(EntitySelectMenu.create("bep", stuff).build()).queue();
//			LinkedList<SelectOption> strings = new LinkedList<>();
//			strings.add(SelectOption.of("Label 1", "label1"));
//			strings.add(SelectOption.of("Label 2", "label2"));
//			event.reply("Text").setActionRow(StringSelectMenu.create("custom").addOptions(strings).setMinValues(2).setMaxValues(2).build()).queue();
			break;
		case "teams-generate":
			generateTeam(event, event.getOption("command").getAsString());
			break;
		case "teams-generate-again":
			generateTeam(event, lastTeamGen);
			break;
		case "teams-help":
			sendTeamHelp(event);
			break;
		case "rename-chatroom":
			PBL.renameChannel(event);
			break;
		case "limit":
			PBL.limit(event);
		case "roll-dice":
			rollDice(event);
			break;
		}
	}
	
	private void rollDice(SlashCommandInteractionEvent event) {
		long count = event.getOption("count").getAsLong();
		long faces = event.getOption("faces").getAsLong();
		
		if(count <= 0 || faces <= 0) {
			event.reply("Both count and faces must be a positive whole number").queue();
			return;
		}
		
		if(count >= 1000 || faces >= 1000) {
			event.reply("Both count and faces must be less than 999").queue();
			return;
		}
		
		event.reply("Rolled " + count + "d" + faces + ": **" + DiceRollingSimulator.rollDice(count, faces) + "**").queue();;
	}
	
	private void generateTeam(SlashCommandInteractionEvent event, String command) {
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
				event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();
			} catch (IllegalStateException e) {
				
			}
		} catch (GroupCreationException | TeamNameException e) {
			event.reply(e.getMessage()).queue();
		}
	}
	
	private void sendTeamHelp(SlashCommandInteractionEvent event) {
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
