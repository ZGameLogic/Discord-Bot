package interfaces;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public abstract class VirusTotalInterface {

    //private static final String API_TOKEN = App.config.getVirusTotalApiToken();
    private static final String API_TOKEN = "50fb18f9a31f93fe644c8999c4f268b1769f930c4cf93d73895ec024455ca4c4";

    public static JSONObject uploadFile(File file){
        String status = "queued";
        while(!status.equals("completed")) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost request = new HttpPost("https://www.virustotal.com/api/v3/files");
            HttpEntity entity = MultipartEntityBuilder.create().addPart("file", new FileBody(file)).build();
            request.setEntity(entity);
            request.addHeader("x-apikey", API_TOKEN);
            try {
                CloseableHttpResponse res = httpclient.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
                String line = "";
                String total = "";
                while ((line = in.readLine()) != null) {
                    total += "\n" + line;
                }
                JSONObject json = new JSONObject(total.trim());
                if(json.has("data")){
                    status = json.getJSONObject("data").getJSONObject("attributes").getString("status");
                    System.out.println(status);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static JSONObject getAnalysis(String id){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://www.virustotal.com/api/v3/analyses/" + id);
        request.addHeader("x-apikey", API_TOKEN);
        try{
            CloseableHttpResponse res = httpclient.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
            String line = "";
            String total = "";
            while((line = in.readLine()) != null){
                total += "\n" + line;
            }
            return new JSONObject(total.trim());
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
