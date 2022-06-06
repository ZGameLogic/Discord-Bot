package bot.role;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.LinkedList;
import java.util.List;

public abstract class RoleBotSlashCommands {
    public static List<SlashCommandData> getCommands(){
        List<SlashCommandData> commands = new LinkedList<>();
        // Role bot listener
        commands.add(Commands.slash("stats", "Posts the players stats in chat")
                .addOption(OptionType.USER, "player", "Player's stats to see", false));
        commands.add(Commands.slash("challenge", "Challenges a player for their role. A win switches the roles!")
                .addOption(OptionType.USER, "player", "The player you wish to challenge", true));
        commands.add(Commands.slash("role-stats", "Lists everyone in the caste level and their stats if they can still defend for the day")
                .addOption(OptionType.ROLE, "role", "Role to see the stats of", true)
                .addOption(OptionType.BOOLEAN, "include-all", "Whether or not to include the people who have already defended today", false));
        commands.add(Commands.slash("leaderboard", "Get the top 10 players in a specific category")
                .addSubcommands(new SubcommandData("strength", "Shows the strength statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("knowledge", "Shows the knowledge statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("magic", "Shows the magic statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("agility", "Shows the agility statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("stamina", "Shows the stamina statistic")
                        .addOption(OptionType.BOOLEAN, "show-all", "Show all stats, or just the one for the leader board", false))
                .addSubcommands(new SubcommandData("gold", "Shows the richest citizens"))
                .addSubcommands(new SubcommandData("total", "Shows the citizens with the most stats"))
                .addSubcommands(new SubcommandData("wins", "Shows the citizens with the most wins"))
                .addSubcommands(new SubcommandData("losses", "Shows the citizens with the most losses"))
                .addSubcommands(new SubcommandData("castes", "Shows the population of each castes"))
                .addSubcommands(new SubcommandData("activities", "Shows a list of active members who still have not taken their activities for today"))
        );
        commands.add(Commands.slash("pay-citizen", "Gives your gold to a citizen of your choice")
                .addOption(OptionType.USER, "citizen", "The citizen to recieve your gold", true)
                .addOption(OptionType.INTEGER, "gold", "The amount of gold to give", true));
        commands.add(Commands.slash("pray", "Pray to Shlongbot"));
        commands.add(Commands.slash("fight-stats", "View a more detailed breakdown of a fight between players")
                .addOption(OptionType.STRING, "id", "id of the fight to get more details on"));

        // Role bot king
        commands.add(Commands.slash("distribute-wealth", "Gives some of your wealth to a caste system")
                .addOption(OptionType.ROLE, "role", "The caste level of where you want your gold to go", true)
                .addOption(OptionType.INTEGER, "gold", "The amount of gold to distribute", true));

        commands.add(Commands.slash("propose-tax", "Forces a caste to pay a tax at the start of the next day")
                .addOption(OptionType.ROLE, "role", "The caste level to tax", true)
                .addOption(OptionType.INTEGER, "gold", "The amount of gold to tax", true));

        commands.add(Commands.slash("honorable-promotion", "Forces two citizens to switch roles. Used once per day")
                .addOption(OptionType.USER, "citizen-one", "One of the two citizens to switch roles", true)
                .addOption(OptionType.USER, "citizen-two", "One of the two citizens to switch roles", true));
        commands.add(Commands.slash("pass-law", "Create a law for the kingdom to follow from now on!")
                .addOption(OptionType.STRING, "law", "Law to be added", true));

        return commands;
    }
}
