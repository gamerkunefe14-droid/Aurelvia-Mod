package tr.aurelvia;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.aurelvia.block.ComputerBlock;
import tr.aurelvia.block.ConveyorBlock;
import tr.aurelvia.block.IdMachineBlock;
import tr.aurelvia.item.AurelviaDataComponents;
import tr.aurelvia.item.IdCardItem;
import tr.aurelvia.networking.ModNetworking;

/**
 * Aurelvia Makineleri (Minecraft 1.21.11 / Fabric)
 *
 *  Bilgisayar      : tek basina kullanilamaz, makinenin ustunde sifre sorar
 *  Konveyor        : tek basina da gercek konveyor gibi calisir
 *  Kimlik Makinesi : 2 blok genislik + ustte bilgisayar, kimlik karti basar
 */
public class AurelviaMachinesMod implements ModInitializer {

    public static final String MOD_ID = "aurelvia_machines";
    public static final Logger LOGGER = LoggerFactory.getLogger("AurelviaMachines");

    /** Bilgisayarin her acilista istedigi sabit sifre. (\u0131 = noktasiz i) */
    public static final String MACHINE_PASSWORD = "UOhfyu7679nu6obBnoeryb79hu\u0131824byuYBYO";

    // ------------------------------------------------------------------ bloklar
    public static final Block COMPUTER_BLOCK =
            new ComputerBlock(BlockBehaviour.Properties.of().strength(3.0F, 4.0F));
    public static final Block CONVEYOR_BLOCK =
            new ConveyorBlock(BlockBehaviour.Properties.of().strength(2.0F, 4.0F));
    public static final Block ID_MACHINE_BLOCK =
            new IdMachineBlock(BlockBehaviour.Properties.of().strength(4.0F, 6.0F));

    // -------------------------------------------------------------- blok itemlari
    public static final Item COMPUTER_ITEM = new BlockItem(COMPUTER_BLOCK, new Item.Properties());
    public static final Item CONVEYOR_ITEM = new BlockItem(CONVEYOR_BLOCK, new Item.Properties());
    public static final Item ID_MACHINE_ITEM = new BlockItem(ID_MACHINE_BLOCK, new Item.Properties());

    // -------------------------------------------------------------------- itemlar
    public static final Item ID_CARD_ITEM = new IdCardItem(new Item.Properties().stacksTo(1));

    // ----------------------------------------------------------- yaratici sekmesi
    public static final CreativeModeTab MACHINES_TAB = CreativeModeTab
            .builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.aurelvia_machines.main"))
            .icon(() -> new ItemStack(ID_MACHINE_ITEM))
            .displayItems((parameters, output) -> {
                output.accept(COMPUTER_ITEM);
                output.accept(CONVEYOR_ITEM);
                output.accept(ID_MACHINE_ITEM);
                output.accept(ID_CARD_ITEM);
            })
            .build();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[Aurelvia Makineleri] Baslatiliyor (Fabric 1.21.11)");

        AurelviaDataComponents.init();

        Registry.register(BuiltInRegistries.BLOCK, id("computer"), COMPUTER_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, id("conveyor"), CONVEYOR_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, id("id_machine"), ID_MACHINE_BLOCK);

        Registry.register(BuiltInRegistries.ITEM, id("computer"), COMPUTER_ITEM);
        Registry.register(BuiltInRegistries.ITEM, id("conveyor"), CONVEYOR_ITEM);
        Registry.register(BuiltInRegistries.ITEM, id("id_machine"), ID_MACHINE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, id("id_card"), ID_CARD_ITEM);

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("card_player_name"),
                AurelviaDataComponents.CARD_PLAYER_NAME);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("card_player_uuid"),
                AurelviaDataComponents.CARD_PLAYER_UUID);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("machines"), MACHINES_TAB);

        ModNetworking.register();

        LOGGER.info("[Aurelvia Makineleri] Hazir. Sifre: {}", MACHINE_PASSWORD);
    }
}
