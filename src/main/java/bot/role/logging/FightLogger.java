package bot.role.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FightLogger {
	
	private static final String PATH = "arena\\logs\\fights\\";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	
	/**
	 * Writes a string of text to the end of the current days file
	 * @param line String to be written into the file
	 */
	public static void writeToFile(String line, long id) {
		File out = new File(PATH + id + ".txt");
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
			output.append(line + "\n");
			output.flush();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File getFight(long id) {
		return new File(PATH + id + ".txt");
	}
	
	public static void timestampFight(long id) {
		writeToFile("Fight time: " + DATE_FORMAT.format(new Date()), id);
	}
	
	public static boolean exists(long id) {
		File file = new File(PATH + id + ".txt");
		return file.exists();
	}

}
