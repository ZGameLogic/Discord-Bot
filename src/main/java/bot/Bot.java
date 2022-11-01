package bot;

import javax.annotation.PostConstruct;

import bot.listeners.GeneralListener;
import bot.listeners.PartyBot;
import bot.utils.AdvancedListenerAdapter;
import bot.utils.EmbedMessageGenerator;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import application.App;
import data.ConfigLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

@RestController
public class Bot {

	@Autowired
	GuildDataRepository guildData;

	private JDA jdaBot;
	private static String TITLE = "\r\n" + 
			"   ____  ___  _                   _   ___      _  ______  \r\n" + 
			"  / /\\ \\|   \\(_)___ __ ___ _ _ __| | | _ ) ___| |_\\ \\ \\ \\ \r\n" + 
			" < <  > > |) | (_-</ _/ _ \\ '_/ _` | | _ \\/ _ \\  _|> > > >\r\n" + 
			"  \\_\\/_/|___/|_/__/\\__\\___/_| \\__,_| |___/\\___/\\__/_/_/_/ \r\n" + 
			"  v3.0.0\tBen Shabowski\r\n" +
			"";
	
	private Logger logger = LoggerFactory.getLogger(Bot.class);

	@PostConstruct
	public void start() {
		ConfigLoader config = App.config;
		System.out.println(TITLE);
		
		// Create bot
		JDABuilder bot = JDABuilder.createDefault(config.getBotToken());
		bot.enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT);
		bot.enableCache(CacheFlag.ACTIVITY);
		bot.enableIntents(GatewayIntent.GUILD_MEMBERS);
		bot.setMemberCachePolicy(MemberCachePolicy.ALL);

		LinkedList<AdvancedListenerAdapter> listeners = new LinkedList<>();

		listeners.add(new PartyBot(guildData));
		listeners.add(new GeneralListener());

		// Add listeners
		for(ListenerAdapter a : listeners){
			bot.addEventListeners(a);
		}
		
		// Login
		try {
			jdaBot = bot.build().awaitReady();
		} catch (InterruptedException e) {
			logger.error("Unable to launch bot");
		}

		// Add slash commands
		HashMap<String, LinkedList<CommandData>> guildCommands = new HashMap<>();
		LinkedList<CommandData> globalCommands = new LinkedList<>();
		for(AdvancedListenerAdapter listener : listeners){
			globalCommands.addAll(listener.getGlobalSlashCommands());
			for(String key : listener.getGuildSlashCommands().keySet()){
				if(guildCommands.containsKey(key)){
					guildCommands.get(key).addAll(listener.getGuildSlashCommands().get(key));
				}else {
					guildCommands.put(key, listener.getGuildSlashCommands().get(key));
				}
			}
		}

		//update guilds
		for(Guild guild : jdaBot.getGuilds()){
			if(guildData.existsById(guild.getIdLong())){

			} else {
				guild.createTextChannel("shlongbot")
						.addRolePermissionOverride(guild.getPublicRole().getIdLong(),
								new LinkedList<>(),
								new LinkedList<>(Arrays.asList(new Permission[] {Permission.VIEW_CHANNEL})
						))
						.setTopic("This is a channel made by shlongbot")
						.queue(textChannel -> {
							GuildData newGuild = new GuildData();
							newGuild.setId(guild.getIdLong());
							newGuild.setConfigChannelId(textChannel.getIdLong());
							newGuild.setChatroomEnabled(false);
							Message m = textChannel.sendMessageEmbeds(EmbedMessageGenerator.welcomeMessage(guild.getOwner().getEffectiveName(), guild.getName()))
											.setActionRow(Button.danger("enable_party", "Party Bot"))
									.complete();
							newGuild.setConfigMessageId(m.getIdLong());
							guildData.save(newGuild);
						});
			}
		}

		// Update global
		CommandListUpdateAction global = jdaBot.updateCommands();
		global.addCommands(globalCommands).submit();
		// Update guild
		guildCommands.forEach((id, commands) -> {
			CommandListUpdateAction guild = jdaBot.getGuildById(id).updateCommands();
			guild.addCommands(commands);
			guild.queue();
		});
	}
}
