package controllers.steam;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import bot.steam.SteamAppData;

public interface SteamAPI {
	
	String API_PATH_REVIEW_PREFIX = "http://store.steampowered.com/appreviews/";
	String API_PATH_REVIEW_SUFFIX = "?json=1";
	String API_PATH_STORE = "http://store.steampowered.com/api/appdetails?appids=";
	
	static SteamAppData appReviews(String id) {
        try {
        	HttpClient httpclient = HttpClients.createDefault();
            URIBuilder builder = new URIBuilder(API_PATH_REVIEW_PREFIX + id + API_PATH_REVIEW_SUFFIX);
            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            
            HttpClient httpclient2 = HttpClients.createDefault();
            URIBuilder builder2 = new URIBuilder(API_PATH_STORE + id);
            URI uri2 = builder2.build();
            HttpGet request2 = new HttpGet(uri2);
            HttpResponse response2 = httpclient2.execute(request2);
            HttpEntity entity2 = response2.getEntity();
            if (entity != null && entity2 != null) {
            	return new SteamAppData(new JSONObject(EntityUtils.toString(entity)), new JSONObject(EntityUtils.toString(entity2)).getJSONObject(id).getJSONObject("data"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}
}
