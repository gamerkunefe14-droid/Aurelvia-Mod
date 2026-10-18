package tr.aurelvia.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

/**
 * Kimlik kartinin uzerinde tasinan bilgiler.
 */
public final class AurelviaDataComponents {

    /** Karttaki oyuncunun adi. */
    public static final DataComponentType<String> CARD_PLAYER_NAME =
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build();

    /** Karttaki oyuncunun UUID'si (skin icin). */
    public static final DataComponentType<String> CARD_PLAYER_UUID =
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build();

    public static void init() {
        // sinifi yukler, statik alanlar hazirlanir
    }

    private AurelviaDataComponents() {
    }
}
