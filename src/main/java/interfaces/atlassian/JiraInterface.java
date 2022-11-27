package interfaces.atlassian;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class JiraInterface {

    /**
     * Checks if the URL is valid
     * @param url URL to check if valid
     * @return true if the URL is valid
     */
    public static boolean checkURL(String url){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        try {
            HttpResponse httpresponse = httpclient.execute(httpget);
            if(httpresponse.getStatusLine().getStatusCode() != 200) return false;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
            for(int i = 0; i < 17; i++){
                in.readLine();
            }
            return in.readLine().contains("JIRA");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if a personal Access token is valid
     * @param PAT personal access token
     * @param url url to check against
     * @return true if PAT is valid
     */
    public static boolean checkPAT(String PAT, String url){
        return true;
    }
}
