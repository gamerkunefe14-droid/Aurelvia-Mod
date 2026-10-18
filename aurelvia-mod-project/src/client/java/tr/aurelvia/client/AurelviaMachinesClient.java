package tr.aurelvia.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import tr.aurelvia.item.IdCardItem;
import tr.aurelvia.networking.ModNetworking;

/**
 * Client tarafi: paket alimi + ekranlarin acilmasi.
 */
public class AurelviaMachinesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Sunucu "sifre sor" dediginde ekran ac
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OpenMachinePayload.TYPE,
                (payload, context) -> {
                    context.client().execute(() ->
                            Minecraft.getInstance().setScreen(new PasswordScreen(payload.pos())));
                });

        // Sunucu oyuncu listesini gonderdiginde listeyi ac
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.PlayerListPayload.TYPE,
                (payload, context) -> {
                    context.client().execute(() -> Minecraft.getInstance().setScreen(
                            new PlayerListScreen(payload.pos(), ModNetworking.parseEntries(payload.data()))));
                });

        // Kimlik karti eldeyken sag tik -> karti buyuk goster
        IdCardItem.VIEWER = (name, uuid) ->
                Minecraft.getInstance().setScreen(new IdCardViewScreen(name, uuid));
    }
}
