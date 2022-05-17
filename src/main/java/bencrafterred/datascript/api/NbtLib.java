package bencrafterred.datascript.api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import bencrafterred.datascript.LuaUtils;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType.NbtPath;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;

public class NbtLib extends TwoArgFunction {
    public static final LuaValue NATIVE = valueOf("_native");
    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue nbt = new LuaTable();
        nbt.set("fromsnbt", new fromsnbt());
        nbt.set("tobool", new tobool());
        //TODO constructors for lists, arrays, compounds, literals, ...
        env.get("package").get("loaded").set("api::nbt", nbt);
        return nbt;
    }

    static class fromsnbt extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue value) {
            NbtElement nbtElement;
            try {
                nbtElement = new StringNbtReader(new StringReader(value.checkjstring())).parseElement();
            } catch (CommandSyntaxException e) {
                return NIL;
            }
            return new NbtElementWrapper(nbtElement);
        }
    }

    static class tobool extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue value) {
            if (value instanceof NbtElementWrapper) {
                NbtElement element = ((NbtElementWrapper) value).nbtElement;
                if (element instanceof AbstractNbtNumber) {
                    double number = ((AbstractNbtNumber) element).doubleValue();
                    return valueOf(number > 0);
                }
            }
            if (value.isnumber()) {
                return valueOf(value.checkdouble() > 0);
            }
            return valueOf(value.toboolean());
        }
    }

    static class NbtElementWrapper extends LuaTable {
        public static final LuaValue TYPE = valueOf("type");
        public static final LuaTable KEYS_TABLE = LuaUtils.createKeysTable(NATIVE, TYPE);
        
        private final NbtElement nbtElement;

        public NbtElementWrapper(NbtElement element) {
            this(element, true);
        }

        public NbtElementWrapper(NbtElement element, boolean copy) {
            super();
            this.nbtElement = copy ? element.copy() : element;
            this.set("isnumber", new isnumber());
            this.set("islist", new islist());
            this.set("tolua", new tolua());
            this.set("get", new get());
            this.set("tosnbt", new tosnbt());
            this.set("inspect", new inspect());
            this.set("remove", new remove());
            this.set("set", new set());
            this.set("merge", new merge());
            //TODO list operations: append, insert, sublist, ...
            this.setmetatable(tableOf(new LuaValue[] {
                INDEX, new TwoArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue key) {
                        if (key.eq_b(NATIVE)) {
                            return CoerceJavaToLua.coerce(nbtElement);
                        } else if (key.eq_b(TYPE)) {
                            return valueOf(nbtElement.getNbtType().getCrashReportName());
                        }
                        return self.rawget(key);
                    }
                },
                NEWINDEX, new ThreeArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue key, LuaValue value) {
                        if (key.eq_b(NATIVE) || key.eq_b(TYPE)) {
                            error("'" + key + "' is a read-only value");
                        }
                        self.rawset(key, value);
                        return NIL;
                    }
                },
                TOSTRING, new OneArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self) {
                        return valueOf(NbtHelper.toPrettyPrintedText(nbtElement).getString());
                    }
                }
            }));
        }

        @Override
        public Varargs next(LuaValue key) {
            return varargsOf(KEYS_TABLE.next(key).arg1(), this.get(key));
        }

        class isnumber extends ZeroArgFunction {
            @Override
            public LuaValue call() {
                switch (nbtElement.getType()) {
                    case NbtElement.BYTE_TYPE:
                    case NbtElement.SHORT_TYPE:
                    case NbtElement.INT_TYPE:
                    case NbtElement.LONG_TYPE:
                    case NbtElement.FLOAT_TYPE:
                    case NbtElement.DOUBLE_TYPE:
                        return TRUE;
                    default:
                        return FALSE;
                }
            }
        }

        class islist extends ZeroArgFunction {
            @Override
            public LuaValue call() {
                switch (nbtElement.getType()) {
                    case NbtElement.BYTE_ARRAY_TYPE:
                    case NbtElement.LIST_TYPE:
                    case NbtElement.INT_ARRAY_TYPE:
                    case NbtElement.LONG_ARRAY_TYPE:
                        return TRUE;
                    default:
                        return FALSE;
                }
            }
        }

        class tolua extends ZeroArgFunction {
            private LuaValue nbtToLua(NbtElement element) {
                switch (element.getType()) {
                    case NbtElement.BYTE_TYPE:
                        return valueOf(((AbstractNbtNumber) element).byteValue());
                    case NbtElement.SHORT_TYPE:
                        return valueOf(((AbstractNbtNumber) element).shortValue());
                    case NbtElement.INT_TYPE:
                        return valueOf(((AbstractNbtNumber) element).intValue());
                    case NbtElement.LONG_TYPE:
                        return valueOf(((AbstractNbtNumber) element).longValue());
                    case NbtElement.FLOAT_TYPE:
                        return valueOf(((AbstractNbtNumber) element).floatValue());
                    case NbtElement.DOUBLE_TYPE:
                        return valueOf(((AbstractNbtNumber) element).doubleValue());
                    case NbtElement.BYTE_ARRAY_TYPE:
                        return listOf(((NbtByteArray) element).stream()
                            .map(b -> b.byteValue())
                            .collect(Collectors.toList()).toArray(new LuaValue[]{}));
                    case NbtElement.STRING_TYPE:
                        return valueOf(((NbtString) element).asString());
                    case NbtElement.LIST_TYPE:
                        return listOf(((NbtList) element).stream()
                            .map(e -> nbtToLua(e))
                            .collect(Collectors.toList()).toArray(new LuaValue[]{}));
                    case NbtElement.COMPOUND_TYPE:
                        NbtCompound compound = ((NbtCompound) element);
                        return tableOf(compound.getKeys().stream()
                            .flatMap(key -> Stream.of(key, nbtToLua(compound.get(key))))
                            .collect(Collectors.toList()).toArray(new LuaValue[]{}));
                    case NbtElement.INT_ARRAY_TYPE:
                        return listOf(((NbtByteArray) element).stream()
                            .map(i -> i.intValue())
                            .collect(Collectors.toList()).toArray(new LuaValue[]{}));
                    case NbtElement.LONG_ARRAY_TYPE:
                        return listOf(((NbtByteArray) element).stream()
                            .map(l -> l.longValue())
                            .collect(Collectors.toList()).toArray(new LuaValue[]{}));
                    default:
                        return NIL;
                }
            }

            @Override
            public LuaValue call() {
                return nbtToLua(nbtElement);
            }
        }

        class tosnbt extends OneArgFunction {
            public static final LuaValue PRETTY = valueOf("pretty");
            public static final LuaValue COMPACT = valueOf("compact");

            @Override
            public LuaValue call(LuaValue format) {
                if (format.isnil() || format.eq_b(PRETTY)) {
                    return valueOf(NbtHelper.toPrettyPrintedText(nbtElement).getString());
                } else if (format.eq_b(COMPACT)) {
                    return valueOf(nbtElement.asString());
                }
                return error("Invalid format option: '" + format + "'");
            }
        }

        class inspect extends ZeroArgFunction {
            @Override
            public LuaValue call() {
                return valueOf(NbtHelper.toFormattedString(nbtElement, true));
            }
        }

        class copy extends ZeroArgFunction {
            @Override
            public LuaValue call() {
                return new NbtElementWrapper(nbtElement.copy(), false);
            }
        }

        class get extends VarArgFunction {
            @Override
            public Varargs invoke(Varargs varargs) {
                NbtPath path;
                try {
                    path = NbtPathArgumentType.nbtPath().parse(new StringReader(varargs.checkjstring(1)));
                } catch (CommandSyntaxException e) {
                    throw new LuaError(e);
                }
                List<NbtElement> elements;
                try {
                    elements = path.get(nbtElement);
                } catch (CommandSyntaxException e) { // No element found
                    return NIL;
                }
                return varargsOf(elements.stream().map(NbtElementWrapper::new).collect(Collectors.toList()).toArray(new LuaValue[]{}));
            }
        }

        class remove extends OneArgFunction {
            @Override
            public LuaValue call(LuaValue pathArg) {
                NbtPath path;
                try {
                    path = NbtPathArgumentType.nbtPath().parse(new StringReader(pathArg.checkjstring()));
                } catch (CommandSyntaxException e) {
                    throw new LuaError(e);
                }
                return valueOf(path.remove(nbtElement));
            }
        }

        class set extends TwoArgFunction {
            @Override
            public LuaValue call(LuaValue pathArg, LuaValue elementArg) {
                NbtPath path;
                try {
                    path = NbtPathArgumentType.nbtPath().parse(new StringReader(pathArg.checkjstring()));
                } catch (CommandSyntaxException e) {
                    throw new LuaError(e);
                }
                if (!(elementArg instanceof NbtElementWrapper)) {
                    error("element arg has to be an nbt element");
                }
                NbtElement element = ((NbtElementWrapper) elementArg).nbtElement;
                try {
                    path.put(nbtElement, element);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
                return NIL;
            }
        }

        /**Lua function: element.merge(path: string, source: NbtElement)
         * <hr>
         * Merges the source element into this element at the given path. Only for nbt compound elements.
         */
        class merge extends TwoArgFunction {
            @Override
            public LuaValue call(LuaValue pathArg, LuaValue elementArg) {
                if (!(nbtElement instanceof NbtCompound)) {
                    error("this nbt element does not support 'merge'");
                }
                NbtPath path;
                try {
                    path = NbtPathArgumentType.nbtPath().parse(new StringReader(pathArg.checkjstring()));
                } catch (CommandSyntaxException e) {
                    throw new LuaError(e);
                }
                if (!(elementArg instanceof NbtElementWrapper)) {
                    error("arg #2 has to be an nbt element");
                }
                NbtElement element = ((NbtElementWrapper) elementArg).nbtElement;
                if (!(element instanceof NbtCompound)) {
                    error("arg #2 has to be an nbt compound");
                }
                NbtCompound compound = (NbtCompound) element;
                ((NbtCompound) nbtElement).copyFrom(compound);
                try {
                    path.put(nbtElement, element);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
                return NIL;
            }
        }

        class append extends VarArgFunction {
            @Override
            public Varargs invoke(Varargs varargs) {
                return NIL;
            }
        }
    }
}
