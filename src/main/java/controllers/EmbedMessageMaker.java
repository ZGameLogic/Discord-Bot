package controllers;

import java.awt.Color;

import bot.role.Player;
import bot.role.RoleBotListener;
import data.DataCacher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public abstract class EmbedMessageMaker {
	
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
