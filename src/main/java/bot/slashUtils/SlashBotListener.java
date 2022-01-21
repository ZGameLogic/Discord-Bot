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
	private RoleBotListener RBL;
	
	public SlashBotListener(PartyBotListener PBL, ConfigLoader CL, RoleBotListener RBL) {
		this.PBL = PBL;
		this.CL = CL;
		this.RBL = RBL;
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
		// Dice
		global.addCommands(new CommandData("roll-dice", "Rolls a dice")
				.addOption(OptionType.INTEGER, "count", "Number of dice to be rolled", true)
				.addOption(OptionType.INTEGER, "faces", "Number of faces on each die", true));
		
		// Party bot commands
		guild.addCommands(new CommandData("create-text", "Creates a text chatroom that only people in the voice channel can see"));
		guild.addCommands(new CommandData("rename-chatroom", "Renames chatroom to a new name")
				.addOption(OptionType.STRING, "name", "Chatroom name", true));
		guild.addCommands(new CommandData("limit", "Limits the amount of people who can enter a chatroom")
				.addOption(OptionType.INTEGER, "count", "Number of people allowed in the chatroom", true));
		
		// Role bot listener
		guild.addCommands(new CommandData("stats", "Posts the players stats in chat")
				.addOption(OptionType.USER, "player", "Player's stats to see", false));
		guild.addCommands(new CommandData("challenge", "Challenges a player for their role. A win switches the roles!")
				.addOption(OptionType.USER, "player", "The player you wish to challenge", true));
		guild.addCommands(new CommandData("role-stats", "Lists everyone in the caste level and their stats if they can still defend for the day")
				.addOption(OptionType.ROLE, "role", "Role to see the stats of", true)
				.addOption(OptionType.BOOLEAN, "include-all", "Weather or not to include the people who have already defended today", false));
		guild.addCommands(new CommandData("leaderboard", "Get the top 10 players in a specific category")
				.addOption(OptionType.STRING, "statistic", "Which statistic to get the leader board for", true)
				.addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false));
		guild.addCommands(new CommandData("pay-citizen", "Gives your gold to a citizen of your choice")
				.addOption(OptionType.USER, "citizen", "The citizen to recieve your gold", true)
				.addOption(OptionType.INTEGER, "gold", "The amount of gold to give", true));
		
		// Role bot king
		guild.addCommands(new CommandData("distribute-wealth", "Gives some of your wealth to a caste system")
				.addOption(OptionType.ROLE, "role", "The caste level of where you want your gold to go", true)
				.addOption(OptionType.INTEGER, "gold", "The amount of gold to distribute", true));
		
		guild.addCommands(new CommandData("propose-tax", "Forces a caste to pay a tax at the start of the next day")
				.addOption(OptionType.ROLE, "role", "The caste level to tax", true)
				.addOption(OptionType.INTEGER, "gold", "The amount of gold to tax", true));
		
		guild.addCommands(new CommandData("honorable-promotion", "Forces two citizens to switch roles. Used once per day")
				.addOption(OptionType.USER, "citizen-one", "One of the two citizens to switch roles", true)
				.addOption(OptionType.USER, "citizen-two", "One of the two citizens to switch roles", true));
		
		
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
	public void onSlashCommand(SlashCommandEvent event) {
		logger.info("Slash command recieved for " + event.getName() + " by " + event.getMember().getEffectiveName());
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
		case "limit":
			PBL.limit(event);
		case "roll-dice":
			rollDice(event);
			break;
		case "stats":
			RBL.sendStats(event);
			break;
		case "challenge":
			RBL.challenge(event);
			break;
		case "role-stats":
			RBL.sendRoleStats(event);
			break;
		case "leaderboard":
			RBL.leaderBoard(event);
			break;
		case "distribute-wealth":
			RBL.distributeWealth(event);
			break;
		case "propose-tax":
			RBL.submitTax(event);
			break;
		case "honorable-promotion":
			RBL.honorablePromotion(event);
			break;
		case "pay-citizen":
			RBL.payCitizen(event);
			break;
		}
	}
	
	private void rollDice(SlashCommandEvent event) {
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
