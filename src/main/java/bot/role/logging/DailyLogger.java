package bot.role.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DailyLogger {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
	private static final String PATH = "arena\\logs\\";
	
	/**
	 * Writes a string of text to the end of the current days file
	 * @param line String to be written into the file
	 */
	public static void writeToFile(String line) {
		File out = getDayFile();
		if(!out.getParentFile().exists()) {
			out.getParentFile().mkdirs();
		}
		if(!out.exists()) {
			try {
				out.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			PrintWriter output = new PrintWriter(new FileWriter(out, true));
			output.append(DATE_FORMAT.format(new Date()) + " " + line + "\n");
			output.flush();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File getCurrentFile() {
		File file = getDayFile();
		if(file.exists()) {
			return file;
		}
		return null;
	}
	
	public static File getFile(String date) {
		if(date.length() != 12) {
			return null;
		}
		File file = new File(PATH + date.replaceAll(":", "-") + "-log.txt");
		if(file.exists()) {
			return file;
		}
		return null;
	}
	
	/**
	 * Gets the file of the current file that would be writing too based off the day 
	 * @return file that the current day should be writing too
	 */
	private static File getDayFile() {
		Calendar calendar = Calendar.getInstance();
		String fileName = "";
		fileName += FILE_FORMAT.format(calendar.getTime());
		fileName += calendar.get(Calendar.AM_PM) == Calendar.PM ? "-1" : "-0";
		File file = new File(PATH + fileName + "-log.txt");
		return file;
	}

}
