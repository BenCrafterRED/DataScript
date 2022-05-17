package bencrafterred.datascript;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

import net.minecraft.util.Identifier;

public class Script {
    private final Identifier id;
    private final LuaValue chunk;
    private LuaFunction tickCallback;

    public Script(Identifier id, Globals globals, String sourceCode) {
        this.id = id;
        this.chunk = globals.load(sourceCode, id.toString());
    }

    public Identifier getId() {
        return this.id;
    }

    public boolean initialize() {
        LuaValue returnValue = chunk.call();
        try {
            this.tickCallback = returnValue.checkfunction();
        } catch (LuaError e) {
            return false;
        }
        return true;
    }

    public void executeTick() {
        this.tickCallback.call();
    }

    public static class Builder {
        private String sourceCode;
        private Identifier id;
        private Globals globals;

        public Identifier getId() {
            return id;
        }

        public String getSourceCode() {
            return sourceCode;
        }

        public Globals getGlobals() {
            return globals;
        }

        public Builder withId(Identifier id) {
            this.id = id;
            return this;
        }

        public Builder withSourceCode(String source) {
            this.sourceCode = source;
            return this;
        }

        public Builder withGlobals(Globals globals) {
            this.globals = globals;
            return this;
        }

        public Script build() {
            return new Script(this.id, this.globals, this.sourceCode);
        }
    }
}
