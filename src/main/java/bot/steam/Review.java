package bot.steam;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public class Review {
	
	private boolean purchased;
	private double playTimeHours;
	private double playTimeHoursTotal;
	private String review;
	
	public Review(JSONObject json) throws JSONException {
		purchased = json.getBoolean("steam_purchase");
		playTimeHours = json.getJSONObject("author").getDouble("playtime_at_review")/60;
		playTimeHoursTotal = json.getJSONObject("author").getDouble("playtime_forever")/60;
		review = json.getString("review");
	}
	
	public MessageEmbed buildMessage() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Review");
		eb.setDescription(review);
		eb.addField("Purchased", purchased + "", true);
		eb.addField("Playtime in hours", String.format("At review: %.2f", playTimeHours) + String.format("\nTotal: %.2f", playTimeHoursTotal), true);
		return eb.build();
	}

}
