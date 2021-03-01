package bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * This class handles posting a message in the bot channel. Will be removed once slash commands come out. 
 * @author Ben Shabowski
 *
 */
class MessagePoster extends Thread {
	
	private LinkedList<Long> textChannelIDS;
	private Guild shlongshot; 
	
	public MessagePoster(Guild shlongshot, LinkedList<Long> textChannelIDS) {
		this.shlongshot = shlongshot;
		this.textChannelIDS = textChannelIDS;
	}
	
	public void run() {
		while(true) {
			// message file
			File input = new File("message.txt");
			
			if(input.exists()) {
				// we get here if the file exists
				
				// text channel to send the message too
				TextChannel messageChannel = null;
				
				// get the channel by ID
				for(Long x : textChannelIDS) {
					if((messageChannel = shlongshot.getTextChannelById(x)) != null) {
						break;
					}
				}
				
				Scanner fileInput;
				try {
					// read the file, and then send out each line to the text channel
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
