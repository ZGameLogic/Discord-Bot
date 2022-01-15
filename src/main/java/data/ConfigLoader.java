package data;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;

@Configuration
@PropertySource("File:discordbot.properties")
@Getter
public class ConfigLoader {

	@Value("${bot.token}")
	private String botToken;
	
	@Value("${keystore.password}")
	private String keystorePassword;
	
	@Value("${keystore.location}")
	private String keystoreLocation;
	
	@Value("${Admin.password}")
	private String admin;
	
	@Value("${Webhook.port}")
	private int webHookPort;
	
	@Value("${chatroom.cat.id}")
	private long chatroomCatID;
	
	@Value("${create.chat.id}")
	private long createChatID;
	
	@Value("${guild.id}")
	private long guildID;
	
	@Value("${afk.id:0}")
	private long AFKID;
	
	@Value("${bitbucket.id}")
	private long bitbucketID;
	
	@Value("${admin.password}")
	private String adminPassword;
	
	@Value("${role.ids}")
	private String[] roleIDs;
	
	@Value("${admin.role.ids}")
	private String[] adminRoleIDs;
	
	public LinkedList<Long> getRoleIDs(){
		LinkedList<Long> ids = new LinkedList<>();
		for(String id : roleIDs ) {
			ids.add(Long.parseLong(id));
		}
		return ids;
	}
	
	public LinkedList<Long> getAdminRoleIDs(){
		LinkedList<Long> ids = new LinkedList<>();
		for(String id : adminRoleIDs) {
			ids.add(Long.parseLong(id));
		}
		return ids;
	}
}
