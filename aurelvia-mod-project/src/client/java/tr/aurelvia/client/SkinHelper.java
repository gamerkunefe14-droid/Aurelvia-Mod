package tr.aurelvia.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * Oyuncu cildini (skin) bulur.
 *  1) sunucudaki gercek oyuncu bilgisi (ClientPacketListener#getPlayerInfo)
 *  2) yoksa UUID'den varsayilan cilt
 *  3) o da yoksa Steve
 */
public final class SkinHelper {

    private SkinHelper() {
    }

    public static PlayerSkin skinFor(String uuidText) {
        Minecraft mc = Minecraft.getInstance();
        UUID uuid = parseUuid(uuidText);

        if (uuid != null && mc != null) {
            ClientPacketListener connection = mc.getConnection();
            if (connection != null) {
                PlayerInfo info = connection.getPlayerInfo(uuid);
                if (info != null && info.getSkin() != null) {
                    return info.getSkin();
                }
                return DefaultPlayerSkin.get(uuid);
            }
        }
        return DefaultPlayerSkin.getDefaultSkin();
    }

    public static Identifier textureOf(PlayerSkin skin) {
        if (skin != null && skin.body() != null && skin.body().texturePath() != null) {
            return skin.body().texturePath();
        }
        PlayerSkin fallback = DefaultPlayerSkin.getDefaultSkin();
        return fallback.body().texturePath();
    }

    private static UUID parseUuid(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
