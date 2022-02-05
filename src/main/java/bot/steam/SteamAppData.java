package bot.steam;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.ToString;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@ToString
public class SteamAppData {
	
	private static final int REVIEW_COUNT = 3;
	
	// Review stuff
	private final String reviewScoreDescription;
	private final long totalPositive;
	private final long totalNegative;
	private LinkedList<Review> reviews;
	
	// Store stuff
	private String releaseDate;
	private String currency;
	private double finalPrice;
	private int discount;
	private String name;
	
	public SteamAppData(JSONObject reviewJson, JSONObject storeJson) throws JSONException {
		
		JSONObject querySummary = reviewJson.getJSONObject("query_summary");
		reviewScoreDescription = querySummary.getString("review_score_desc");
		totalPositive = querySummary.getLong("total_positive");
		totalNegative = querySummary.getLong("total_negative");
		
		JSONArray jsonReviews = reviewJson.getJSONArray("reviews");
		reviews = new LinkedList<>();
		for(int i = 0; i < REVIEW_COUNT; i++) {
			reviews.add(new Review(jsonReviews.getJSONObject(i)));
		}
		
		releaseDate = storeJson.getJSONObject("release_date").getString("date");
		currency = storeJson.getJSONObject("price_overview").getString("currency");
		finalPrice = storeJson.getJSONObject("price_overview").getInt("final") / 100;
		discount = storeJson.getJSONObject("price_overview").getInt("discount_percent");
		name = storeJson.getString("name");
	}
	
	public LinkedList<MessageEmbed> generateEmbeds(){
		LinkedList<MessageEmbed> messages = new LinkedList<>();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Shlongbot steam info for: " + name);
		eb.addField("Release date", releaseDate, true);
		eb.addField("Price information", String.format("Current price: %.2f", finalPrice)
				+ (discount > 0 ? ("\nCurrent discount: " + discount + "%") : "") , true);
		eb.addField("Review summary", reviewScoreDescription + "\nPosative: " + totalPositive + "\nNegative: " + totalNegative, true);
		messages.add(eb.build());
		for(Review r : reviews) {
			messages.add(r.buildMessage());
		}
		return messages;
	}
}
