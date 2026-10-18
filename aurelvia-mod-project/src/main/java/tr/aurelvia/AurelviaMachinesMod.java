package tr.aurelvia;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.aurelvia.block.ComputerBlock;
import tr.aurelvia.block.ConveyorBlock;
import tr.aurelvia.block.IdMachineBlock;
import tr.aurelvia.block.entity.IdMachineBlockEntity;
import tr.aurelvia.item.AurelviaDataComponents;
import tr.aurelvia.item.IdCardItem;
import tr.aurelvia.networking.ModNetworking;

/**
 * Aurelvia Makineleri - ana mod sinifi.
 *
 * Icerik:
 *  - Bilgisayar   : tek basina kullanilamaz, kimlik makinesinin parcasi
 *  - Konveyor     : tek basina da konveyor olarak calisir
 *  - Kimlik Makinesi : 2 blok genisliginde + ustunde bilgisayar
 */
public class AurelviaMachinesMod implements ModInitializer {
    public static final String MOD_ID = "aurelvia_machines";
    public static final Logger LOGGER = LoggerFactory.getLogger("AurelviaMachines");

    /** Bilgisayarin istedigi sabit sifre. (u0131 = noktasiz i) */
    public static final String MACHINE_PASSWORD = "UOhfyu7679nu6obBnoeryb79hu\u0131824byuYBYO";

    // ---------------------------------------------------------------- bloklar
    public static final Block COMPUTER_BLOCK =
            new ComputerBlock(BlockBehaviour.Properties.of().strength(3.0F, 4.0F));
    public static final Block CONVEYOR_BLOCK =
            new ConveyorBlock(BlockBehaviour.Properties.of().strength(2.0F, 4.0F));
    public static final Block ID_MACHINE_BLOCK =
            new IdMachineBlock(BlockBehaviour.Properties.of().strength(4.0F, 6.0F));

    // ------------------------------------------------------------ blok itemlari
    public static final Item COMPUTER_ITEM =
            new BlockItem(COMPUTER_BLOCK, new Item.Properties());
    public static final Item CONVEYOR_ITEM =
            new BlockItem(CONVEYOR_BLOCK, new Item.Properties());
    public static final Item ID_MACHINE_ITEM =
            new BlockItem(ID_MACHINE_BLOCK, new Item.Properties());

    // ------------------------------------------------------------------ itemlar
    public static final Item ID_CARD_ITEM =
            new IdCardItem(new Item.Properties().stacksTo(1));

    // ------------------------------------------------------------ block entity
    public static BlockEntityType<IdMachineBlockEntity> ID_MACHINE_BLOCK_ENTITY;

    // ----------------------------------------------------------- creative sekme
    public static final CreativeModeTab MACHINES_TAB = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.aurelvia_machines.main"))
            .icon(() -> new ItemStack(ID_MACHINE_ITEM))
            .displayItems((parameters, output) -> {
                output.accept(COMPUTER_ITEM);
                output.accept(CONVEYOR_ITEM);
                output.accept(ID_MACHINE_ITEM);
                output.accept(ID_CARD_ITEM);
            })
            .build();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[Aurelvia Makineleri] Baslatiliyor (Minecraft 1.21.11 / Fabric)");

        // Data component'ler once kaydedilmeli
        AurelviaDataComponents.init();

        // Bloklar
        Registry.register(BuiltInRegistries.BLOCK, id("computer"), COMPUTER_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, id("conveyor"), CONVEYOR_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, id("id_machine"), ID_MACHINE_BLOCK);

        // Blok itemlari
        Registry.register(BuiltInRegistries.ITEM, id("computer"), COMPUTER_ITEM);
        Registry.register(BuiltInRegistries.ITEM, id("conveyor"), CONVEYOR_ITEM);
        Registry.register(BuiltInRegistries.ITEM, id("id_machine"), ID_MACHINE_ITEM);

        // Itemlar
        Registry.register(BuiltInRegistries.ITEM, id("id_card"), ID_CARD_ITEM);

        // Block entity tipi
        ID_MACHINE_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id("id_machine"),
                BlockEntityType.Builder.of(IdMachineBlockEntity::new, ID_MACHINE_BLOCK).build(null));

        // Yaratici mod sekmesi: "Aurelvia makineleri"
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("machines"), MACHINES_TAB);

        // Ag paketleri
        ModNetworking.register();

        LOGGER.info("[Aurelvia Makineleri] Hazir.");
    }
}
