package bencrafterred.datascript;

import net.minecraft.server.MinecraftServer;

public class Minecraft {
    private static MinecraftServer server;
    private static ScriptManager scriptManager;

    public static ScriptManager getScriptManager() {
        return scriptManager;
    }

    public static void setScriptManager(ScriptManager scriptManager) {
        Minecraft.scriptManager = scriptManager;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static void setServer(MinecraftServer server) {
        Minecraft.server = server;
    }
}
