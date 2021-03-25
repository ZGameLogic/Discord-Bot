package codeBot.runners;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class JavaRunner {
	
	public static void runJavaCode(String code, MessageReceivedEvent event, File codeBase) {
		long jobId = System.currentTimeMillis() * 3;
		long startTime = System.currentTimeMillis();

		// Key: class name
		// Data: entire class
		Map<String, String> classes = new HashMap<String, String>();

		String currentClassName = "";
		String currentClassData = "";
		String mainClassName = "";

		int open = 0;

		for (String line : code.split("\n")) {
			if (line.contains("class ") && currentClassName.equals("")) {
				if (line.indexOf(" ", line.indexOf("class ") + 6) != -1) {
					currentClassName = line.substring(line.indexOf("class ") + 6,
							line.indexOf(" ", line.indexOf("class ") + 6));
				} else if (line.indexOf("{", line.indexOf("class ") + 6) != -1) {
					currentClassName = line.substring(line.indexOf("class ") + 6,
							line.indexOf("{", line.indexOf("class ") + 6));
				} else if (line.indexOf("\n", line.indexOf("class ") + 6) != -1) {
					currentClassName = line.substring(line.indexOf("class ") + 6,
							line.indexOf("\n", line.indexOf("class ") + 6));
				}

				currentClassData += line + "\n";
				open += countChar('{', line);
				open -= countChar('}', line);
			} else if(!currentClassName.equals("")) {
				if (!line.equals("")) {
					if (line.contains("public static void main(String[] args)")) {
						mainClassName = currentClassName;
					}
					currentClassData += line + "\n";
					open += countChar('{', line);
					open -= countChar('}', line);
				}
			}
			if (open == 0 && !currentClassName.equals("")) {
				classes.put(currentClassName, currentClassData);
				currentClassName = "";
				currentClassData = "";
			}
		}
		
		if(classes.isEmpty()) {
			classes.put("App", "class App { \n public static void main(String[] args){\n" + code + "\n}\n}");
			mainClassName = "App";
		}

		File jobDir = new File(codeBase.getPath() + "\\" + jobId);
		jobDir.mkdir();

		for (String key : classes.keySet()) {

			File currentClass = new File(jobDir.getPath() + "\\" + key + ".java");
			try {
				currentClass.createNewFile();
				PrintWriter out = new PrintWriter(currentClass);
				out.print(classes.get(key));
				out.flush();
				out.close();
			} catch (IOException e) {
				event.getChannel().sendMessage(
				javaRunMessageBuilder("Unable to create the class files for running", -1, getTotalExecutionTime(startTime, System.currentTimeMillis()), jobId)
				).queue();;
			}
		}
		
		// run files
		new JavaCodeRunner(event, mainClassName, startTime, jobId, jobDir).start();
	}
	
	private static int countChar(char c, String string) {
		int count = 0;

		for (char x : string.toCharArray()) {
			if (x == c) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Creates an embedded message of the data passed in
	 * 
	 * @param codeOutput
	 * @param exitStatus
	 * @param executionTime
	 * @param jobId
	 * @return
	 */
	private static MessageEmbed javaRunMessageBuilder(String codeOutput, int exitStatus, String executionTime, long jobId) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Runtime information");
		eb.setAuthor("Java code");

		switch(exitStatus) {
		case -1:
		case 1:
			eb.setColor(Color.RED);
			break;
			default:
				eb.setColor(Color.GREEN);
		}
		
		eb.setDescription(codeOutput);
		eb.addField("Exit status", exitStatus + "", false);
		eb.addField("Execution time", executionTime, false);
		eb.setFooter("Job id:" + jobId);

		return eb.build();
	}
	
	private static String getTotalExecutionTime(long start, long end) {
		DateFormat d = new SimpleDateFormat("mm:ss:SSS");
		Date finalTime = new Date(end - start);
		return d.format(finalTime);
	}
	
	private static class JavaCodeRunner extends Thread {
		
		MessageReceivedEvent event;
		String mainClass;
		long startTime;
		long id;
		File dir;
		
		public JavaCodeRunner(MessageReceivedEvent event, String mainClass, long startTime,
				long id, File dir) {
			this.event = event;
			this.mainClass = mainClass;
			this.startTime = startTime;
			this.id = id;
			this.dir = dir;
		}
		
		// check mark U+2705
		// arrow right U+27A1
		// floppy_disk U+1F4BE
		// repeat U+1F501
		
		public void run() {
			
			try {
				Process compile = new ProcessBuilder("cmd", "/c", "javac *.java").directory(dir).start();
				compile.waitFor();
				if(compile.exitValue() == 0) {
					event.getMessage().addReaction("U+2705").complete();
				} else {
					String error = "";
					BufferedReader input = new BufferedReader(new InputStreamReader(compile.getErrorStream()));
					String line = "";
					while((line = input.readLine()) != null) {
						error += line + "\n";
					}
					event.getChannel().sendMessage(
							javaRunMessageBuilder("Unable to compile job\n" + error, compile.exitValue(), getTotalExecutionTime(startTime, System.currentTimeMillis()), id)
							).queue();
							

					compile.getErrorStream().close();
					compile.getInputStream().close();
					compile.getOutputStream().close();
					deleteAll(dir);
					return;
				}
			} catch (IOException e) {
				
			} catch (InterruptedException e) {
				
			}
			
			event.getMessage().addReaction("U+27A1").complete();
			
			try {
				Process job = new ProcessBuilder("cmd", "/c", "java " + mainClass).directory(dir).start();
				job.waitFor();
				if(job.exitValue() == 0) {
					String output = "";
					BufferedReader input = new BufferedReader(new InputStreamReader(job.getInputStream()));
					String line = "";
					while((line = input.readLine()) != null) {
						output += line + "\n";
					}
					event.getChannel().sendMessage(
							javaRunMessageBuilder("**Job output**\n" + output, job.exitValue(), getTotalExecutionTime(startTime, System.currentTimeMillis()), id)
							).complete();
					job.getErrorStream().close();
					job.getInputStream().close();
					job.getOutputStream().close();
					
				} else {
					String error = "";
					BufferedReader input = new BufferedReader(new InputStreamReader(job.getErrorStream()));
					String line = "";
					while((line = input.readLine()) != null) {
						error += line + "\n";
					}
					
					event.getChannel().sendMessage(
							javaRunMessageBuilder("Unable to compile job\n" + error, job.exitValue(), getTotalExecutionTime(startTime, System.currentTimeMillis()), id)
							).queue();
					
					job.getErrorStream().close();
					job.getInputStream().close();
					job.getOutputStream().close();
					deleteAll(dir);
					return;
				}
			} catch (IOException e) {
				
			} catch (InterruptedException e) {
				
			}
			
			deleteAll(dir);
		}
		
		private void deleteAll(File file) {
			if(file.isDirectory()) {
				for(File x : file.listFiles()) {
					deleteAll(x);
				}
				file.delete();
			}else {
				file.delete();
			}
		}
		
	}

}
