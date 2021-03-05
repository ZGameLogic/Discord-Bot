package twilio;
import static spark.Spark.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TextMessageHandler {
	
	private LinkedBlockingQueue<Map<String, String>> output;

	public TextMessageHandler() {
		
		output = new LinkedBlockingQueue<Map<String, String>>();

		Twilio.init("ACfadfc84818346547d5ce8034825cf69a", "f702a95966990cf94a54fe3dc619e20c");

		try {
			new ProcessBuilder("cmd.exe", "/c",
					"twilio phone-numbers:update \"+18474433756\" --sms-url=\"http://localhost:4567/sms\"").start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		post("/sms", (req, res) -> {
			output.add(parseBody(req.body()));
			return "";
		});
	}
	
	/**
	 * Get the output blocking queue
	 * @return This queue will fill up with the map of a message when they come in
	 */
	public LinkedBlockingQueue<Map<String, String>> getOutputQueue() {
		return output;
	}

	/**
	 * Sends a text message to the number with the content of the message
	 * @param toNumber Number to send message too
	 * @param messageToSend Content of the message
	 */
	public void sendMessage(String toNumber, String messageToSend) {
		Message message = Message.creator(new PhoneNumber("+" + toNumber), // to
				new PhoneNumber("+18474433756"), // from
				messageToSend).create();

		message.getSid();
	}

	private Map<String, String> parseBody(String body) throws UnsupportedEncodingException {
		String[] unparsedParams = body.split("&");
		Map<String, String> parsedParams = new HashMap<String, String>();
		for (int i = 0; i < unparsedParams.length; i++) {
			String[] param = unparsedParams[i].split("=");
			if (param.length == 2) {
				parsedParams.put(urlDecode(param[0]), urlDecode(param[1]));
			} else if (param.length == 1) {
				parsedParams.put(urlDecode(param[0]), "");
			}
		}
		return parsedParams;
	}

	private String urlDecode(String s) throws UnsupportedEncodingException {
		return URLDecoder.decode(s, "utf-8");
	}
}
