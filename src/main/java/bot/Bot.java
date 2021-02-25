package bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Bot {

	/*
	 * Create chat IDS
	 */
	private LinkedList<Long> createChatIDs;

	// bot
	private JDA bot;

	// main guild
	private Guild shlongshot;

	// voice channel category
	private Category chatRoomsCat;
	LinkedList<Long> ignoredChannelIDs;
	LinkedList<Long> textChannelIDS;

	public Bot() {

		createChatIDs = new LinkedList<Long>();
		createChatIDs.add(812095961475317811l);
		createChatIDs.add(812083428686168136l);

		ignoredChannelIDs = new LinkedList<Long>();
		ignoredChannelIDs.addAll(createChatIDs);
		ignoredChannelIDs.add(371695546173358090l);

		textChannelIDS = new LinkedList<Long>();
		textChannelIDS.add(812909596175106048l);
		textChannelIDS.add(812912306357928007l);

		JDABuilder bot = JDABuilder.createDefault(BotInfo.TEST_SERVER_TOKEN);
		bot.addEventListeners(new Listener());

		try {
			this.bot = bot.build().awaitReady();
		} catch (LoginException | InterruptedException e) {
			System.out.println("Unable to launch bot");
		}

		for (Guild x : this.bot.getGuilds()) {
			if (x.getName().contains("MemeBot test server") || x.getName().contains("Shlongshot")) {
				System.out.println("Found guild");
				shlongshot = x;
				break;
			}
		}

		for (Category x : this.bot.getCategories()) {
			if (x.getName().contains("Chat Rooms")) {
				System.out.println("Found category");
				chatRoomsCat = x;
				break;
			}
		}

		new messagePoster().start();
	}

	private class Listener extends ListenerAdapter {

		@Override
		public void onReady(ReadyEvent event) {
			System.out.println("Logged in");
		}

		@SuppressWarnings("finally")
		@Override
		public void onMessageReceived(MessageReceivedEvent event) {
			if (!event.getAuthor().isBot()) {
				if (textChannelIDS.contains(event.getTextChannel().getIdLong())) {
					// TODO add new command stuff in
					boolean valid = false;
					String command = event.getMessage().getContentDisplay();
					
					if(command.startsWith("rename ") && command.split(" ").length > 1) {
						for(VoiceChannel x : shlongshot.getVoiceChannels()) {
							if(x.getParent() == chatRoomsCat && x.getMembers().contains(event.getMember())) {
								try {
									x.getManager().setName(command.replace("rename ", "")).queue();
									valid = true;
								}catch(IllegalArgumentException e1) {
									
								}finally{
									break;
								}
							}
						}
						
					}else if(command.startsWith("limit ") && command.split(" ").length > 1) {
						
						try {
							for(VoiceChannel x : shlongshot.getVoiceChannels()) {
								if(x.getParent() == chatRoomsCat && x.getMembers().contains(event.getMember())) {
									x.getManager().setUserLimit(Integer.parseInt(command.replace("limit ", ""))).queue();
									break;
								}
							}
							
							Integer.parseInt(command.split(" ")[1]);
							valid = true;
						}catch(NumberFormatException e) {
							
						}catch(IllegalArgumentException e1) {
							
						}
					}					
					
					if (!valid) {
						event.getAuthor().openPrivateChannel().complete()
						.sendMessage("Unknown or invalid command:" + event.getMessage().getContentDisplay())
						.queue();
					}

					event.getMessage().delete().queue();
				}
			}
		}

		@Override
		public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
			playerJoined(event.getChannelJoined(), event.getMember());
		}

		@Override
		public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
			playerLeft(event.getChannelLeft());
			playerJoined(event.getChannelJoined(), event.getMember());
		}

		@Override
		public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
			playerLeft(event.getChannelLeft());
		}

		private void playerLeft(VoiceChannel leftChannel) {
			if (!ignoredChannelIDs.contains(leftChannel.getIdLong()) && leftChannel.getParent() != null
					&& leftChannel.getParent().equals(chatRoomsCat) && leftChannel.getMembers().size() <= 0) {
				// delete the channel
				leftChannel.delete().queue();
				// TODO rename regular Chatrooms
			}
		}

		private void playerJoined(VoiceChannel joinChannel, Member user) {
			// joined create channel
			if (createChatIDs.contains(joinChannel.getIdLong())) {
				int number = 1;
				while (shlongshot.getVoiceChannelsByName("Chatroom " + number, true).size() > 0) {
					number++;
				}
				VoiceChannel newChannel = shlongshot.createVoiceChannel("Chatroom " + number).setParent(chatRoomsCat).complete();
				shlongshot.moveVoiceMember(user, newChannel).queue();
			}
		}
		/*
		
		public void onSlashCommand(SlashCommandEvent event) {
			  if (event.getCommandName().equals("async")) {
			    event.acknowledge(false) // log the original message, respond later (because you only have 3 seconds to respond)
			      .delay(10, TimeUnit.MINUTES)
			      .flatMap(commandThread -> command.sendMessage("asynchronous response")) // this can be done up to 15 minutes later
			      .queue();
			  } else if (event.getCommandName().equals("ping")) {
			    event.reply("Pong!").setEphemeral(true).queue();
			  }
			}
		*/
	}
	
	private class messagePoster extends Thread {
		
		public void run() {
			while(true) {
				
				File input = new File("message.txt");
				
				if(input.exists()) {
					TextChannel messageChannel = null;
					
					for(Long x : textChannelIDS) {
						if((messageChannel = shlongshot.getTextChannelById(x)) != null) {
							break;
						}
					}
					
					Scanner fileInput;
					try {
						fileInput = new Scanner(input);
						while(fileInput.hasNextLine()) {
							messageChannel.sendMessage(fileInput.nextLine()).queue();
						}
						fileInput.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					
					input.delete();
					
				}
				
				//sleep for 1 minute
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
