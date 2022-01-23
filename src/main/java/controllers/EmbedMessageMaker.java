package controllers;

import java.awt.Color;
import java.util.HashMap;

import bot.role.Player;
import bot.role.RoleBotListener;
import data.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public abstract class EmbedMessageMaker {
	
	public static EmbedBuilder goodMorningMessage(String message) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(119, 21, 161));
		eb.setTitle("A message from the king/queen");
		eb.setDescription(message);
		return eb;
	}
	
	public static EmbedBuilder giveGold(String giver, String taker, long amount) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(new Color(252, 211, 3));
		eb.setTitle(giver + " has given some gold to " + taker);
		eb.setDescription("Amount: " + amount);
		return eb;
	}
	
	public static EmbedBuilder leaderboardFaction(HashMap<String, Integer> rolePop) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Leaderboard for caste population ");
		eb.setColor(new Color(102, 107, 14));
		for(String key : rolePop.keySet()) {
			eb.addField(key, rolePop.get(key) + "", true);
		}
		return eb;
	}
	
	public static EmbedBuilder honorablePromotion(Member member1, Member member2) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen has decreed a role change!");
		eb.setDescription("By the command of our king/queen: " + member1.getEffectiveName() + " and " + member2.getEffectiveName() + " have had their roles swapped!");
		eb.setColor(new Color(252, 211, 3));
		return eb;
	}
	
	public static EmbedBuilder newKing(String kingName, String memberIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("All hail the new King/Queen " + kingName + "!");
		eb.setImage(memberIcon);
		eb.setColor(new Color(252, 211, 3));
		return eb;
	}
	
	public static EmbedBuilder proposeTax(long amount, String roleName, String roleIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen has proposed a tax on the caste of: " + roleName);
		eb.setDescription("At the begining of the next day, everyone in that caste will pay the king/queen " + amount + " gold");
		if(!roleIcon.equals("")) {
			eb.setThumbnail(roleIcon);
		}
		eb.setColor(new Color(252, 211, 3));
		return eb;
	}
	
	public static EmbedBuilder distributeGold(long initialAmount, String roleName, String roleIcon) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("The King/Queen bequeathes the caste of " + roleName + " with " + initialAmount + " gold!");
		if(!roleIcon.equals("")) {
			eb.setThumbnail(roleIcon);
		}
		eb.setDescription("Go now! Check your banks! All hail the King/Queen!!!");
		eb.setColor(new Color(252, 211, 3));
		return eb;
	}
	
	public static EmbedBuilder roleStats(SlashCommandEvent event, DataCacher<Player> data) {
		Role role = event.getOption("role").getAsRole();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Fighter stats for " + role.getName() + "s");
		eb.setDescription("Stats are encoded as Strength:Knowledge:Magic:Agility:Stamina Gold Wins/Losses");
		eb.setColor(new Color(113, 94, 115));
		if(role.getIcon() != null) {
			eb.setThumbnail(role.getIcon().getIconUrl());
		}
		
		boolean includeAll = false;
		
		if(event.getOption("include-all") != null) {
			includeAll = event.getOption("include-all").getAsBoolean();
		}
		
		for(Member m : event.getGuild().getMembersWithRoles(role)) {
			if(!m.getUser().isBot()) {
				Player p = data.loadSerialized(m.getId());
				if(p.canDefend() || includeAll) {
					eb.addField(m.getEffectiveName(), p.getCompactStats(), true);
				}
			}
		}
		return eb;
	}
	
	public static EmbedBuilder playerStats(Member member, DataCacher<Player> data, String icon) {
		EmbedBuilder eb = new EmbedBuilder();
		Player player = data.loadSerialized(member.getIdLong() + "");
		eb.setTitle("Fighter stats for " + member.getEffectiveName() + " " + icon);
		eb.setColor(new Color(113, 94, 115));
		eb.setThumbnail(member.getEffectiveAvatarUrl());
		
		eb.addField("Strength", player.getStrength() + "", true);
		eb.addField("Knowledge", player.getKnowledge() + "", true);
		eb.addField("Magic", player.getMagic() + "", true);
		eb.addField("Agility", player.getAgility() + "", true);
		eb.addField("Stamina", player.getStamina() + "", true);
		
		eb.addField("Statistics", "Gold: " + player.getGold() + "\nTournament victories: " + player.getTournamentWins() + "\nVictories: " + player.getWins()
			+ "\nDefeats: " + player.getLosses(), false);
		
		eb.setFooter("Can attack " + (RoleBotListener.dailyChallengeLimit - player.getHasChallengedToday()) + " more time(s) today\n"
				+ "Can defend " + (RoleBotListener.dailyDefendLimit - player.getChallengedToday()) + " more time(s) today");
		return eb;
	}
}
