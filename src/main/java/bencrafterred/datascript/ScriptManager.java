package bencrafterred.datascript;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class ScriptManager {
    public static final Identifier TAG_ID_MAIN = new Identifier(DataScriptMod.NAMESPACE, "main");
    private boolean justLoaded = true;
    private final MinecraftServer server;
    private List<Script> loadScripts = ImmutableList.of();
    private List<Script> tickScripts = ImmutableList.of();

    public ScriptManager(MinecraftServer server) {
        this.server = server;
    }

    public void tick() {
        if (this.justLoaded) {
            this.justLoaded = false;
            this.initializeAll(loadScripts, "datascrip:initialize_scripts");
        }
        this.executeTickAll(tickScripts, "datascript:tick_scripts");
    }

    public void initializeAll(Collection<Script> scripts, String label) {
        ImmutableList.Builder<Script> builder = ImmutableList.builder();
        this.server.getProfiler().push(label);
        scripts.forEach(script -> {
            this.server.getProfiler().push(script.getId().toString());
            try {
                if (script.initialize()) {
                    builder.add(script);
                }
            } catch (Throwable e) {
                DataScriptMod.LOGGER.warn("An exception occured while executing/initializing script \"{}\"", script.getId(), e);
            }
            this.server.getProfiler().pop();
        });
        this.server.getProfiler().pop();
        this.tickScripts = builder.build();
    }

    public void executeTickAll(Collection<Script> scripts, String label) {
        this.server.getProfiler().push(label);
        scripts.forEach(script -> {
            this.server.getProfiler().push(script.getId().toString());
            try {
                script.executeTick();
            } catch (Throwable e) {
                DataScriptMod.LOGGER.warn("An exception occured while executing/ticking script {}", script.getId(), e);
            }
            this.server.getProfiler().pop();
        });
        this.server.getProfiler().pop();
    }

    public void setScripts(ScriptLoader loader) {
        if (loader != null) {
            this.loadScripts = ImmutableList.copyOf(loader.getTags().getTagOrEmpty(TAG_ID_MAIN).values());
            this.justLoaded = true;
        }
    }
}
