package bencrafterred.datascript.api;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import bencrafterred.datascript.Minecraft;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;

public class ServerLib extends TwoArgFunction {
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable lib = new LuaTable();
        lib.set("_native", new _native());
        env.get("package").get("loaded").set("api::minecraft", lib);
        return lib;
    }

    static class _native extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return CoerceJavaToLua.coerce(Minecraft.getServer());
        }
    }

    // native.execute(command)
    static class execute extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            String command = arg.checkjstring();
            int returnCode = Minecraft.getServer().getCommandManager().execute(Minecraft.getServer().getCommandSource(), command);
            return LuaValue.valueOf(returnCode);
        }
    }

    // native.select(selector)
    static class select extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            StringReader reader = new StringReader(arg.checkjstring());
            try {
                EntitySelector selector = EntityArgumentType.entities().parse(reader);
                List<? extends Entity> entities = selector.getEntities(Minecraft.getServer().getCommandSource());
                return LuaValue.listOf(entities.stream().map(CoerceJavaToLua::coerce).collect(Collectors.toList()).toArray(new LuaValue[0]));
            } catch (CommandSyntaxException e) {
                return NIL;
            }
        }
    }
}
