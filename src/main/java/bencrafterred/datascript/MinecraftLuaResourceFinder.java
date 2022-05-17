package bencrafterred.datascript;

import java.io.InputStream;

import org.luaj.vm2.lib.ResourceFinder;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class MinecraftLuaResourceFinder implements ResourceFinder {

    private ResourceManager manager;

    public MinecraftLuaResourceFinder(ResourceManager manager) {
        this.manager = manager;
    }

    @Override
    public InputStream findResource(String filename) {
        Identifier id;
        if (filename.startsWith("api::")) {
            id = new Identifier(DataScriptMod.NAMESPACE, "api/" + filename.substring(5));
        } else {
            id = new Identifier(filename);
            id = new Identifier(id.getNamespace(), "datascripts/" + id.getPath());
        }
        try {
            return manager.getResource(id).getInputStream();
        } catch (Exception e) {
            DataScriptMod.LOGGER.warn("Could not load lua module \"{}\"", filename, e);
            return null;
        }
    }
}
