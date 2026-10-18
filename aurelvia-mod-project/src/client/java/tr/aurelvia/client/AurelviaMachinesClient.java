package tr.aurelvia.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import tr.aurelvia.item.IdCardItem;
import tr.aurelvia.networking.ModNetworking;

/**
 * Istemci tarafi kayitlari:
 *  - sifre ekrani
 *  - oyuncu listesi ekrani
 *  - kimlik karti goruntuleyici
 */
public class AurelviaMachinesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Sunucu "sifre ekranini ac" dedi
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OpenMachinePayload.TYPE, (payload, context) -> {
            Minecraft client = context.client();
            if (client != null) {
                client.execute(() -> client.setScreen(new PasswordScreen(payload.pos())));
            }
        });

        // Sifre dogru -> oyuncu listesi geldi
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.PlayerListPayload.TYPE, (payload, context) -> {
            Minecraft client = context.client();
            if (client != null) {
                client.execute(() -> client.setScreen(
                        new PlayerListScreen(payload.pos(), payload.names(), payload.uuids())));
            }
        });

        // Kart ele alinip sag tiklandiginda buyuk kart gorunumu
        IdCardItem.CLIENT_VIEWER = stack -> {
            Minecraft client = Minecraft.getInstance();
            client.setScreen(new IdCardViewScreen(
                    IdCardItem.getOwnerName(stack),
                    IdCardItem.getOwnerUuid(stack)));
        };
    }
}
