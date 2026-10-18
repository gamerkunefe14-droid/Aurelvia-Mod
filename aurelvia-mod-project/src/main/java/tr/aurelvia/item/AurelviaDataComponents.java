package tr.aurelvia.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import tr.aurelvia.AurelviaMachinesMod;

/**
 * Kimlik kartinin uzerinde tasinan bilgiler (data component).
 */
public final class AurelviaDataComponents {

    /** Karttaki oyuncunun adi. */
    public static final DataComponentType<String> CARD_PLAYER_NAME = register("card_player_name",
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** Karttaki oyuncunun UUID'si (deri/skin icin). */
    public static final DataComponentType<String> CARD_PLAYER_UUID = register("card_player_uuid",
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private static <T> DataComponentType<T> register(String name, DataComponentType.Builder<T> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                AurelviaMachinesMod.id(name), builder.build());
    }

    public static void init() {
        // Sinifi yukler, boylece yukaridaki statik kayitlar calisir.
    }

    private AurelviaDataComponents() {
    }
}
