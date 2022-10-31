package controllers.network;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URI;

public abstract class Network {
    public static JSONObject get(String url){
        try {
            HttpClient httpclient = HttpClients.createDefault();
            URIBuilder builder = new URIBuilder(url);
            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            request.setHeader("User-Agent", "PostmanRuntime/7.29.0");
            HttpResponse response = httpclient.execute(request);
            String stringResponse = EntityUtils.toString(response.getEntity());
            return new JSONObject(stringResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
