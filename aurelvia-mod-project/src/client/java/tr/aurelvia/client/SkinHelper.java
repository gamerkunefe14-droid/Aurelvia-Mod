package tr.aurelvia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Oyuncu derisini (skin) bulur. Once cevrimici bilgisi, sonra varsayilan skin.
 */
public final class SkinHelper {

    /** Hicbir sey bulunamazsa kullanilacak yedek doku. */
    private static final ResourceLocation FALLBACK =
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    public static ResourceLocation skinFor(String uuidText) {
        UUID uuid = parse(uuidText);
        Minecraft minecraft = Minecraft.getInstance();

        // 1) Oyuncu su an sunucudaysa gercek skinini kullan
        if (uuid != null && minecraft != null && minecraft.getConnection() != null) {
            try {
                PlayerInfo info = minecraft.getConnection().getPlayerInfo(uuid);
                if (info != null) {
                    PlayerSkin skin = info.getSkin();
                    if (skin != null && skin.texture() != null) {
                        return skin.texture();
                    }
                }
            } catch (Throwable ignored) {
                // surum farki -> asagidaki yonteme dus
            }
        }

        // 2) UUID'den varsayilan skin
        if (uuid != null) {
            try {
                PlayerSkin skin = DefaultPlayerSkin.get(uuid);
                if (skin != null && skin.texture() != null) {
                    return skin.texture();
                }
            } catch (Throwable ignored) {
                // surum farki -> yedek dokuya dus
            }
        }

        return FALLBACK;
    }

    public static UUID parse(String uuidText) {
        if (uuidText == null || uuidText.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuidText);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SkinHelper() {
    }
}
