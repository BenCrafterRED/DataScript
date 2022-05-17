package bencrafterred.datascript.mixin;

import java.net.Proxy;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bencrafterred.datascript.Minecraft;
import bencrafterred.datascript.ScriptLoader;
import bencrafterred.datascript.ScriptManager;
import bencrafterred.datascript.event.ServerStartEvents;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.UserCache;
import net.minecraft.util.registry.DynamicRegistryManager.Impl;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage.Session;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    private ScriptManager scriptManager;

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/util/thread/ReentrantThreadExecutor.<init>(Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "Lnet/minecraft/server/MinecraftServer;<init>(Ljava/lang/Thread;Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/SaveProperties;Lnet/minecraft/resource/ResourcePackManager;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/resource/ServerResourceManager;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfileRepository;Lnet/minecraft/util/UserCache;Lnet/minecraft/server/WorldGenerationProgressListenerFactory;)V")
    private void startConstructServer(Thread serverThread, Impl registryManager, Session session, SaveProperties saveProperties, ResourcePackManager dataPackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, @Nullable MinecraftSessionService sessionService, @Nullable GameProfileRepository gameProfileRepo, @Nullable UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        scriptManager = new ScriptManager((MinecraftServer) (Object) this);
        scriptManager.setScripts(ScriptLoader.getLoader());
        Minecraft.setScriptManager(scriptManager);
        Minecraft.setServer((MinecraftServer) (Object) this);
        ServerStartEvents.START_CONSTRUCT_SERVER.invoker().onStartConstructServer((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/server/MinecraftServer;<init>(Ljava/lang/Thread;Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/SaveProperties;Lnet/minecraft/resource/ResourcePackManager;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/resource/ServerResourceManager;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfileRepository;Lnet/minecraft/util/UserCache;Lnet/minecraft/server/WorldGenerationProgressListenerFactory;)V")
    private void endConstructServer(Thread serverThread, Impl registryManager, Session session, SaveProperties saveProperties, ResourcePackManager dataPackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, @Nullable MinecraftSessionService sessionService, @Nullable GameProfileRepository gameProfileRepo, @Nullable UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        ServerStartEvents.END_CONSTRUCT_SERVER.invoker().onEndConstructServer((MinecraftServer) (Object) this);
    }
}
