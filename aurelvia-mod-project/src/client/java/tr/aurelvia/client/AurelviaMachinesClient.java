package tr.aurelvia.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.networking.ModNetworking;

/**
 * Client-side mod initializer
 */
public class AurelviaClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        AurelviaMachinesMod.LOGGER.info("Initializing Aurelvia Client");
        
        // Register client-side packet handlers
        ClientPlayNetworking.registerGlobalReceiver(
            ModNetworking.OpenIdMachinePayload.TYPE,
            (payload, context) -> {
                context.client().execute(() -> {
                    context.client().setScreen(new PasswordScreen(payload.pos()));
                });
            }
        );
        
        ClientPlayNetworking.registerGlobalReceiver(
            ModNetworking.PlayerListPayload.TYPE,
            (payload, context) -> {
                context.client().execute(() -> {
                    context.client().setScreen(new PlayerListScreen(payload.pos(), payload.playerNames()));
                });
            }
        );
    }
}
