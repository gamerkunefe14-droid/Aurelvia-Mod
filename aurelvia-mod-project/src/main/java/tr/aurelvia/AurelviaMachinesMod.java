package tr.aurelvia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
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
import tr.aurelvia.item.IdCardItem;

public class AurelviaMachinesMod implements ModInitializer {
    public static final String MOD_ID = "aurelvia_machines";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Blocks
    public static final Block COMPUTER_BLOCK = new ComputerBlock(BlockBehaviour.Properties.of().strength(3.0f));
    public static final Block CONVEYOR_BLOCK = new ConveyorBlock(BlockBehaviour.Properties.of().strength(2.0f));
    public static final Block ID_MACHINE_BLOCK = new IdMachineBlock(BlockBehaviour.Properties.of().strength(4.0f));

    // Block Items
    public static final Item COMPUTER_ITEM = new BlockItem(COMPUTER_BLOCK, new Item.Properties());
    public static final Item CONVEYOR_ITEM = new BlockItem(CONVEYOR_BLOCK, new Item.Properties());
    public static final Item ID_MACHINE_ITEM = new BlockItem(ID_MACHINE_BLOCK, new Item.Properties());

    // Items
    public static final Item ID_CARD_ITEM = new IdCardItem(new Item.Properties().stacksTo(1));

    // Block Entity
    public static BlockEntityType<IdMachineBlockEntity> ID_MACHINE_BLOCK_ENTITY;

    // Creative Tab
    public static final CreativeModeTab AURELVIA_MACHINES_TAB = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.aurelvia.machines"))
            .icon(() -> new ItemStack(COMPUTER_ITEM))
            .displayItems((parameters, output) -> {
                output.accept(COMPUTER_ITEM);
                output.accept(CONVEYOR_ITEM);
                output.accept(ID_MACHINE_ITEM);
                output.accept(ID_CARD_ITEM);
            })
            .build();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Aurelvia Mod");

        // Register blocks
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "computer"), COMPUTER_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "conveyor"), CONVEYOR_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "id_machine"), ID_MACHINE_BLOCK);

        // Register block items
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "computer"), COMPUTER_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "conveyor"), CONVEYOR_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "id_machine"), ID_MACHINE_ITEM);

        // Register items
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "id_card"), ID_CARD_ITEM);

        // Register block entity
        ID_MACHINE_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "id_machine_block_entity"),
                BlockEntityType.Builder.of(IdMachineBlockEntity::new, ID_MACHINE_BLOCK).build(null)
        );

        // Register creative tab
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(MOD_ID, "machines"), AURELVIA_MACHINES_TAB);

        // Register networking
        ModNetworking.register();

        // Register recipes
        ModRecipes.register();

        LOGGER.info("Aurelvia Mod initialized successfully");
    }
}
