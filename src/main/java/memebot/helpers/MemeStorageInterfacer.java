package memebot.helpers;

import java.io.File;

import data.ConfigLoader;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class MemeStorageInterfacer {
	
	private File memeStorageLocation;
	
	public MemeStorageInterfacer (ConfigLoader cl) {
		memeStorageLocation = new File(cl.getMemeStorageLocation());
		if(!memeStorageLocation.exists()) {
			memeStorageLocation.mkdirs();
		}
	}
	
	/**
	 * Saves an image from a discord message
	 * @param attachment Attachment from the message
	 */
	public void saveMeme(Attachment attachment) {
		File imageFile = new File(memeStorageLocation.getAbsolutePath() + "\\" + getNextMemeID() + ".png");
		attachment.downloadToFile(imageFile).exceptionally( t -> {
			t.printStackTrace();
			return null;
		});
	}
	
	/**
	 * Retrieves an image file from a saved location via ID
	 * @param id ID of the image
	 * @return image file
	 */
	public File getMeme(long id) {
		return new File(memeStorageLocation.getAbsolutePath() + "\\" + id);
	}
	
	/**
	 * Deletes a meme based off memeID
	 * @param id ID of meme to be deleted
	 */
	public void deleteMeme(long id) {
		new File(memeStorageLocation.getAbsolutePath() + "\\" + id).delete();
	}
	
	/**
	 * Get the ID of the next available meme
	 * @return the next ID of a meme that is available 
	 */
	private long getNextMemeID() {
		long id = 0l;
		boolean found = false;
		while(true) {
			for(File x : memeStorageLocation.listFiles()) {
				if(x.getName().equals(id + ".png")) {
					id++;
					found = true;
					break;
				}
			}
			if(!found) {
				break;
			}
		}
		return id;
	}
}
