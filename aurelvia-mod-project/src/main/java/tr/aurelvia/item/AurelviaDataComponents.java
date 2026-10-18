package tr.aurelvia.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import tr.aurelvia.AurelviaMachinesMod;

/**
 * Özel data component'ler
 */
public class AurelviaDataComponents {
    
    public static final DataComponentType<String> PLAYER_NAME = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(AurelviaMachinesMod.MOD_ID, "player_name"),
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(StreamCodec.ofMember(
                            (value, buf) -> buf.writeUtf(value),
                            buf -> buf.readUtf()
                    ))
                    .build()
    );

    public static void register() {
        AurelviaMachinesMod.LOGGER.info("Registering Aurelvia data components");
    }
}
