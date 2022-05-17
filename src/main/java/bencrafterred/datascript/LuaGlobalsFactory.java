package bencrafterred.datascript;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

import bencrafterred.datascript.api.NbtLib;
import bencrafterred.datascript.api.ScoreboardLib;
import bencrafterred.datascript.api.ServerLib;
import net.minecraft.resource.ResourceManager;

public class LuaGlobalsFactory {
    public static Globals createGlobals(ResourceManager manager, boolean debug) {
        Globals globals;
        if (debug) {
            globals = JsePlatform.debugGlobals();
        } else {
            globals = JsePlatform.standardGlobals();
        }
        MinecraftLuaResourceFinder finder = new MinecraftLuaResourceFinder(manager);
        globals.finder = finder;
        globals.load(new ServerLib());
        globals.load(new ScoreboardLib());
        globals.load(new NbtLib());
        return globals;
    }
}
