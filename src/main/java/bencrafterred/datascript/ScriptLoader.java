package bencrafterred.datascript;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import org.apache.commons.io.IOUtils;
import org.luaj.vm2.Globals;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.Tag.Builder;
import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class ScriptLoader implements SimpleResourceReloadListener<Pair<Map<Identifier, Builder>, Map<Identifier, CompletableFuture<Script.Builder>>>> {
    private static ScriptLoader loader;
    private volatile Map<Identifier, Script> scripts = ImmutableMap.of();
    private final TagGroupLoader<Script> tagLoader = new TagGroupLoader<>(this::get, "tags/datascripts");
    private volatile TagGroup<Script> tags = TagGroup.createEmpty();
    private Map<String, Globals> globals = Maps.newHashMap();

    public ScriptLoader() {
        loader = this;
    }

    public Optional<Script> get(Identifier id) {
        return Optional.ofNullable(this.scripts.get(id));
    }

    public Map<Identifier, Script> getScripts() {
        return this.scripts;
    }

    public TagGroup<Script> getTags() {
        return this.tags;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(DataScriptMod.NAMESPACE, "datascript_loader");
    }

    @Override
    public CompletableFuture<Pair<Map<Identifier, Builder>, Map<Identifier, CompletableFuture<Script.Builder>>>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        CompletableFuture<Map<Identifier, Builder>> tagsFuture = CompletableFuture.supplyAsync(() -> {
            return this.tagLoader.loadTags(manager);
        }, executor);
        CompletableFuture<Map<Identifier, CompletableFuture<Script.Builder>>> scriptsFuture = CompletableFuture.supplyAsync(() -> {
            return manager.findResources("datascripts", (path) -> {
                return path.endsWith(".lua");
            });
        }, executor).thenCompose((ids) -> {
            Map<Identifier, CompletableFuture<Script.Builder>> map = Maps.newHashMap();
            ids.forEach(id -> {
                String path = id.getPath();
                Identifier scriptId = new Identifier(id.getNamespace(), path.substring("datascripts/".length(), path.length() - ".lua".length()));
                map.put(scriptId, CompletableFuture.supplyAsync(() -> {
                    String sourceCode = readScriptSource(manager, id);
                    return new Script.Builder().withId(scriptId).withSourceCode(sourceCode);
                }, executor));
            });
    
            CompletableFuture<?>[] completableFutures = (CompletableFuture[])map.values().toArray(new CompletableFuture[0]);
            return CompletableFuture.allOf(completableFutures).handle((unused, ex) -> {
                return map;
            });
        });
        return tagsFuture.thenCombine(scriptsFuture, Pair::of);
    }

    @Override
    public CompletableFuture<Void> apply(Pair<Map<Identifier, Builder>, Map<Identifier, CompletableFuture<Script.Builder>>> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            this.globals.clear();
            Map<Identifier, CompletableFuture<Script.Builder>> map = data.getSecond();
            ImmutableMap.Builder<Identifier, Script> builder = ImmutableMap.builder();
            map.forEach((id, scriptFuture) -> {
                scriptFuture.handle((scriptBuilder, ex) -> {
                if (ex != null) {
                    DataScriptMod.LOGGER.error("Failed to load datascript {}", id, ex);
                } else {
                    Globals globals = this.getGlobals(manager, scriptBuilder.getId().getNamespace());
                    Script script = scriptBuilder.withGlobals(globals).build();
                    builder.put(id, script);
                }
                return null;
                }).join();
            });
            this.scripts = builder.build();
            this.tags = this.tagLoader.buildGroup(data.getFirst());
            if (Minecraft.getScriptManager() != null) {
                Minecraft.getScriptManager().setScripts(this);
            }
        }, executor);
    }

    private Globals getGlobals(ResourceManager manager, String namespace) {
        return this.getGlobals(manager, namespace, false);
    }

    private Globals getGlobals(ResourceManager manager, String namespace, boolean debugGlobals) {
        if (!this.globals.containsKey(namespace)) {
            this.globals.put(namespace, LuaGlobalsFactory.createGlobals(manager, debugGlobals));
        }
        return this.globals.get(namespace);
    }

    private static String readScriptSource(ResourceManager manager, Identifier id) {
        try {
            Resource resource = manager.getResource(id);
    
            String sourceCode;
            try {
                sourceCode = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                if (resource != null) {
                    try {
                        resource.close();
                    } catch (Exception e2) {
                        e.addSuppressed(e2);
                    }
                }
    
                throw e;
            }
    
            if (resource != null) {
                resource.close();
            }
    
            return sourceCode;
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    public static ScriptLoader getLoader() {
        return loader;
    }
}
