package controllers.minecraft;

import controllers.minecraft.structures.MinecraftServer;
import controllers.network.Network;

public abstract class MinecraftAPI {
    public static MinecraftServer serverStatus(String ip){
        return new MinecraftServer(Network.get("https://mcapi.us/server/status?ip=" + ip));
    }
}
