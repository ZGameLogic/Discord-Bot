package bot.slashUtils;

import java.awt.Color;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.party.PartyBotListener;
import bot.role.RoleBotListener;
import controllers.dice.DiceRollingSimulator;
import controllers.team.Team;
import controllers.team.TeamGenerator;
import controllers.team.TeamGenerator.GroupCreationException;
import controllers.team.TeamGenerator.TeamNameException;
import data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
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
		guild.addCommands(Commands.slash("create-text", "Creates a text chatroom that only people in the voice channel can see"));
		guild.addCommands(Commands.slash("rename-chatroom", "Renames chatroom to a new name")
				.addOption(OptionType.STRING, "name", "Chatroom name", true));
		guild.addCommands(Commands.slash("limit", "Limits the amount of people who can enter a chatroom")
				.addOption(OptionType.INTEGER, "count", "Number of people allowed in the chatroom", true));
		
		// Role bot listener
//		guild.addCommands(Commands.slash("stats", "Posts the players stats in chat")
//				.addOption(OptionType.USER, "player", "Player's stats to see", false));
//		guild.addCommands(Commands.slash("challenge", "Challenges a player for their role. A win switches the roles!")
//				.addOption(OptionType.USER, "player", "The player you wish to challenge", true));
//		guild.addCommands(Commands.slash("role-stats", "Lists everyone in the caste level and their stats if they can still defend for the day")
//				.addOption(OptionType.ROLE, "role", "Role to see the stats of", true)
//				.addOption(OptionType.BOOLEAN, "include-all", "Whether or not to include the people who have already defended today", false));
//		guild.addCommands(Commands.slash("leaderboard", "Get the top 10 players in a specific category")
//				.addSubcommands(new SubcommandData("strength", "Shows the strength statistic")
//						.addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
//				.addSubcommands(new SubcommandData("knowledge", "Shows the knowledge statistic")
//						.addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
//				.addSubcommands(new SubcommandData("magic", "Shows the magic statistic")
//						.addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
//				.addSubcommands(new SubcommandData("agility", "Shows the agility statistic")
//						.addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
//				.addSubcommands(new SubcommandData("stamina", "Shows the stamina statistic")
//						.addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
//				.addSubcommands(new SubcommandData("gold", "Shows the richest citizens"))
//				.addSubcommands(new SubcommandData("total", "Shows the citizens with the most stats"))
//				.addSubcommands(new SubcommandData("wins", "Shows the citizens with the most wins"))
//				.addSubcommands(new SubcommandData("losses", "Shows the citizens with the most losses"))
//				.addSubcommands(new SubcommandData("factions", "Shows the population of each faction"))
//				.addSubcommands(new SubcommandData("activities", "Shows a list of active members who still have not taken their activities for today"))
//				);
//		guild.addCommands(Commands.slash("pay-citizen", "Gives your gold to a citizen of your choice")
//				.addOption(OptionType.USER, "citizen", "The citizen to recieve your gold", true)
//				.addOption(OptionType.INTEGER, "gold", "The amount of gold to give", true));
//		guild.addCommands(Commands.slash("day-history", "Shows the event history for the day")
//				.addOption(OptionType.STRING, "specific-day", "Pick a day to show formatted as: mm:dd:yyyy:k. K being the day shlongshot is on", false));
//		guild.addCommands(Commands.slash("achievements", "Posts the players achievements in chat")
//				.addOption(OptionType.USER, "player", "Player's achievements to see", false));
//		guild.addCommands(Commands.slash("pray", "Pray to Shlongbot"));
//
//		// Role bot king
//		guild.addCommands(Commands.slash("distribute-wealth", "Gives some of your wealth to a caste system")
//				.addOption(OptionType.ROLE, "role", "The caste level of where you want your gold to go", true)
//				.addOption(OptionType.INTEGER, "gold", "The amount of gold to distribute", true));
//
//		guild.addCommands(Commands.slash("propose-tax", "Forces a caste to pay a tax at the start of the next day")
//				.addOption(OptionType.ROLE, "role", "The caste level to tax", true)
//				.addOption(OptionType.INTEGER, "gold", "The amount of gold to tax", true));
//
//		guild.addCommands(Commands.slash("honorable-promotion", "Forces two citizens to switch roles. Used once per day")
//				.addOption(OptionType.USER, "citizen-one", "One of the two citizens to switch roles", true)
//				.addOption(OptionType.USER, "citizen-two", "One of the two citizens to switch roles", true));
//		guild.addCommands(Commands.slash("pass-law", "Create a law for the kingdom to follow from now on!")
//				.addOption(OptionType.STRING, "law", "Law to be added", true));
		
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
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		logger.info("Slash command received for " + event.getName() + " by " + event.getMember().getEffectiveName());
		switch(event.getName()) {
		case "pray":
			event.reply("Thank you, my child.").queue();
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
		case "create-text":
			PBL.createTextChannel(event);
			break;
		case "rename-chatroom":
			PBL.renameChannel(event);
			break;
		case "limit":
			PBL.limit(event);
		case "roll-dice":
			rollDice(event);
			break;
//		case "stats":
//			RBL.sendStats(event);
//			break;
//		case "challenge":
//			RBL.challenge(event);
//			break;
//		case "role-stats":
//			RBL.sendRoleStats(event);
//			break;
//		case "leaderboard":
//			RBL.leaderBoard(event);
//			break;
//		case "pass-law":
//			RBL.passLaw(event);
//			break;
//		case "distribute-wealth":
//			RBL.distributeWealth(event);
//			break;
//		case "propose-tax":
//			RBL.submitTax(event);
//			break;
//		case "honorable-promotion":
//			RBL.honorablePromotion(event);
//			break;
//		case "pay-citizen":
//			RBL.payCitizen(event);
//			break;
//		case "day-history":
//			RBL.getDayHistory(event);
//			break;
//		case "achievements":
//			RBL.sendAchievements(event);
//			break;
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
				event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
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
