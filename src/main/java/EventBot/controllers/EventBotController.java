package EventBot.controllers;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import EventBot.dataStructures.DiscordEvent;
import EventBot.listeners.EventBotListener;
import webhook.APIController;

@RestController
public class EventBotController {
	
	@PostMapping("/event")
	public void updateStatusWebhook(@RequestBody String valueOne) throws JSONException {
		JSONObject JSONInformation = new JSONObject(valueOne);
		if(APIController.validate(JSONInformation.getString("token"))) {
			createEvent(JSONInformation);
		}
	}
	
	private void createEvent(JSONObject JSONInformation) throws JSONException {
		// Date format YYYY-MM-DD HH:MM
		
		String[] date = JSONInformation.getString("date").replace(" ", "-").replace(":", "-").split("-");
		
		int year = Integer.parseInt(date[0]);
		int month = Integer.parseInt(date[1]);
		int day = Integer.parseInt(date[2]);
		
		int hour = Integer.parseInt(date[3]);
		int minute = Integer.parseInt(date[4]);
		
		Calendar eventDate = Calendar.getInstance();
		eventDate.set(year, month, day, hour, minute);
		eventDate.set(Calendar.SECOND, 0);
		
		DiscordEvent event = new DiscordEvent(eventDate, JSONInformation.getString("name"), JSONInformation.getString("description"));
		
		EventBotListener.addEvent(event);
	}

}
