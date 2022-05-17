package bencrafterred.datascript.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

public interface ServerStartEvents {
    Event<StartConstructServer> START_CONSTRUCT_SERVER = EventFactory.createArrayBacked(StartConstructServer.class,
        (listeners) -> (server) -> {
            for (StartConstructServer listener : listeners) {
                listener.onStartConstructServer(server);
            }
        }
    );

    Event<EndConstructServer> END_CONSTRUCT_SERVER = EventFactory.createArrayBacked(EndConstructServer.class,
        (listeners) -> (server) -> {
            for (EndConstructServer listener : listeners) {
                listener.onEndConstructServer(server);
            }
        }
    );

    @FunctionalInterface
    public interface StartConstructServer {
        void onStartConstructServer(MinecraftServer server);
    }

    @FunctionalInterface
    public interface EndConstructServer {
        void onEndConstructServer(MinecraftServer server);
    }
}
