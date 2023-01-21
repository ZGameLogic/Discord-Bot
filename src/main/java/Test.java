import interfaces.VirusTotalInterface;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Test {
    public static void main(String[] args) throws JSONException {
        File f = new File("C:\\Users\\bensh\\Desktop\\fff.txt");
        JSONObject json1 = VirusTotalInterface.uploadFile(f);
        System.out.println(VirusTotalInterface.getAnalysis(json1.getJSONObject("data").getString("id")));
    }
}
