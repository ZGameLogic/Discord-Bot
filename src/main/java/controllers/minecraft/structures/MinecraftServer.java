package controllers.minecraft.structures;

import lombok.Getter;
import lombok.ToString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

@Getter
@ToString
public class MinecraftServer {

    private boolean online;
    private List<String> playersOnline;
    private int playerCount;
    private String serverName;

    public MinecraftServer(JSONObject jsonObject) {
        playersOnline = new LinkedList<>();
        try {
            online = jsonObject.getBoolean("online");
            if(online){
                playerCount = jsonObject.getJSONObject("players").getInt("now");
                serverName = jsonObject.getJSONObject("server").getString("name");
                JSONArray playersSample = jsonObject.getJSONObject("players").getJSONArray("sample");
                for(int i = 0; i < playersSample.length(); i++){
                    playersOnline.add(playersSample.getJSONObject(i).getString("name"));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String playerListString(){
        String list = "";
        for(String name : playersOnline){
            list += name + ", ";
        }

        return list.length() > 0 ? list.substring(0, list.length() - 2) : "";
    }
}
