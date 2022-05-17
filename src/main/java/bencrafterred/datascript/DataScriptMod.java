package bencrafterred.datascript;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bencrafterred.datascript.event.ServerStartEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class DataScriptMod implements ModInitializer {
	public static final String NAMESPACE = "datascript";
	public static final Logger LOGGER = LogManager.getLogger(NAMESPACE);

	@Override
	public void onInitialize() {
		LOGGER.info("Loading DataScript mod!");

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ScriptLoader());
		
		ServerTickEvents.START_SERVER_TICK.register(world -> {
			world.getProfiler().push(NAMESPACE + ":datascripts_tick");
			Minecraft.getScriptManager().tick();
			world.getProfiler().pop();
		});

		ServerStartEvents.START_CONSTRUCT_SERVER.register(server -> {
			ScriptManager scriptManager = new ScriptManager(server);
			scriptManager.setScripts(ScriptLoader.getLoader());
			Minecraft.setScriptManager(scriptManager);
			Minecraft.setServer(server);
		});
	}
}
