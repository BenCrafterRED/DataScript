package bencrafterred.datascript;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class LuaUtils {
    public static LuaTable createKeysTable(LuaValue...keys) {
        return LuaTable.tableOf(
            Stream.of(keys)
                .flatMap(key -> Stream.of(key, key))
                .collect(Collectors.toList())
                .toArray(new LuaValue[]{})
        );
    }
}
