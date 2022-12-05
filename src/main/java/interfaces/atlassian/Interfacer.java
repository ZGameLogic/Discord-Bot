package interfaces.atlassian;

import interfaces.atlassian.data.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public abstract class Interfacer {

    protected static JSONObject makeGetRequest(Server server, String api) {
        String url = server.getBaseUrl() + api;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + server.getPersonalAccessToken());
        try {
            HttpResponse httpresponse = httpclient.execute(httpget);
            if (httpresponse.getStatusLine().getStatusCode() != 200) return null;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
            return new JSONObject(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkPAT(Server server){
        try {
            JSONObject loggedIn = makeGetRequest(server, "/rest/api/2/mypermissions");
            return loggedIn.getJSONObject("permissions").getJSONObject("ADMINISTER").getBoolean("havePermission");
        } catch (JSONException e) {
            log.debug("Error checking a personal access token", e);
            return false;
        }
    }
}
