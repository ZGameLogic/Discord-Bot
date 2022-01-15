package bot.role;

import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleBotListener extends ListenerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(RoleBotListener.class);

	private long guildID;
	private LinkedList<Long> roleIDs;
	private LinkedList<Long> adminIDs;
	
	public RoleBotListener(ConfigLoader cl) {
		guildID = cl.getGuildID();
		roleIDs = cl.getRoleIDs();
		adminIDs = cl.getAdminRoleIDs();
	}
	/**
	 * Login event
	 */
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Role bot listener activated");
		checkRoles(event.getJDA().getGuildById(guildID));
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		assignRole(event.getMember(), event.getGuild());
	}
	
	/**
	 * Assigns a random role to a member
	 * @param member
	 * @param guild
	 */
	private void assignRole(Member member, Guild guild) {
		Collections.shuffle(roleIDs);
		long randomRoleID = roleIDs.get(0);
		Role role = guild.getRoleById(randomRoleID);
		guild.addRoleToMember(member, role).queue();
		logger.info("Assinging " + member.getEffectiveName() + " to role " + role.getName());
	}
	
	/**
	 * Check a member if they are one of the roles
	 * @param member
	 * @return
	 */
	private boolean checkRoles(Member member) {
		for(Role role : member.getRoles()) {
			if(roleIDs.contains(role.getIdLong()) || adminIDs.contains(role.getIdLong())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks the roles of a guild
	 * @param guild
	 */
	private void checkRoles(Guild guild) {
		logger.info("Checking roles on shlongshot");
		for(Member m: guild.getMembers()) {
			if(!checkRoles(m)) {
				assignRole(m, guild);
			}
		}
		logger.info("No more roles to assign");
	}
	
}
