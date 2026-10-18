#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Derleme zamani (compile-only) API iskeletlerini (stub) uretir.

Bu siniflar SADECE derlemek icindir; JAR'a KONMAZ.
Tum imzalar Minecraft 1.21.11 resmi Mojang eslemelerinden
(BigBroadBean/mappings-extracted, 1.21.11) alinmistir; Fabric API
imzalari FabricMC/fabric-api 1.21.11 dalindan alinmistir.
"""
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "stubs")

# (paket, dosya adi, icerik)
FILES = []


def add(pkg, name, body):
    FILES.append((pkg, name, body))


# ------------------------------------------------------------------ com.mojang
add("com.mojang.serialization", "Codec", """
public interface Codec<A> {
    Codec<String> STRING = null;
}
""")
add("com.mojang.serialization", "MapCodec", """
public abstract class MapCodec<A> {
}
""")
add("com.mojang.authlib", "GameProfile", """
import java.util.UUID;
public class GameProfile {
    public UUID getId() { return null; }
    public String getName() { return null; }
}
""")
add("com.mojang.blaze3d.pipeline", "RenderPipeline", """
public class RenderPipeline {
}
""")
add("org.joml", "Matrix3x2fStack", """
public class Matrix3x2fStack {
    public Matrix3x2fStack pushMatrix() { return this; }
    public Matrix3x2fStack popMatrix() { return this; }
    public Matrix3x2fStack translate(float x, float y) { return this; }
    public Matrix3x2fStack scale(float x, float y) { return this; }
}
""")

# ------------------------------------------------------------------ net.minecraft temel
add("net.minecraft", "ChatFormatting", """
public enum ChatFormatting {
    BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY,
    DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE,
    OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET
}
""")
add("net.minecraft.resources", "Identifier", """
public class Identifier {
    public static Identifier fromNamespaceAndPath(String namespace, String path) { return null; }
    public static Identifier withDefaultNamespace(String path) { return null; }
    public String getPath() { return null; }
    public String getNamespace() { return null; }
    public String toString() { return null; }
}
""")
add("net.minecraft.core", "Vec3i", """
public class Vec3i {
    public int getX() { return 0; }
    public int getY() { return 0; }
    public int getZ() { return 0; }
}
""")
add("net.minecraft.core", "BlockPos", """
import net.minecraft.network.codec.StreamCodec;
public class BlockPos extends Vec3i {
    public static final StreamCodec<io.netty.buffer.ByteBuf, BlockPos> STREAM_CODEC = null;
    public BlockPos(int x, int y, int z) {}
    public BlockPos above() { return null; }
    public BlockPos below() { return null; }
    public BlockPos relative(Direction dir) { return null; }
    public BlockPos relative(Direction dir, int dist) { return null; }
    public String toShortString() { return null; }
    public static BlockPos of(String s) { return null; }
}
""")
add("net.minecraft.core", "Direction", """
public enum Direction {
    DOWN, UP, NORTH, SOUTH, WEST, EAST;
    public Direction getClockWise() { return null; }
    public Direction getOpposite() { return null; }
    public int getStepX() { return 0; }
    public int getStepY() { return 0; }
    public int getStepZ() { return 0; }
    public boolean getClockWiseFlag() { return false; }
}
""")
add("net.minecraft.core", "Registry", """
import net.minecraft.resources.Identifier;
public interface Registry<T> {
    static <V, T extends V> T register(Registry<V> registry, Identifier id, T value) { return null; }
    boolean containsKey(Identifier id);
}
""")
add("net.minecraft.core", "DefaultedRegistry", """
public class DefaultedRegistry<T> implements Registry<T> {
    public boolean containsKey(net.minecraft.resources.Identifier id) { return false; }
}
""")
add("net.minecraft.core", "ClientAsset", """
import net.minecraft.resources.Identifier;
public interface ClientAsset {
    interface Texture extends ClientAsset {
        Identifier texturePath();
    }
}
""")
add("net.minecraft.core.component", "DataComponentType", """
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
public class DataComponentType<T> {
    public static <T> Builder<T> builder() { return null; }
    public static class Builder<T> {
        public Builder<T> persistent(Codec<T> codec) { return this; }
        public Builder<T> networkSynchronized(StreamCodec<? super RegistryFriendlyByteBuf, T> codec) { return this; }
        public DataComponentType<T> build() { return null; }
    }
}
""")
add("net.minecraft.core.registries", "BuiltInRegistries", """
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
public class BuiltInRegistries {
    public static final DefaultedRegistry<Block> BLOCK = null;
    public static final DefaultedRegistry<Item> ITEM = null;
    public static final Registry<CreativeModeTab> CREATIVE_MODE_TAB = null;
    public static final Registry<DataComponentType<?>> DATA_COMPONENT_TYPE = null;
}
""")

# ------------------------------------------------------------------ chat
add("net.minecraft.network.chat", "Component", """
import net.minecraft.ChatFormatting;
public interface Component {
    String getString();
    static MutableComponent literal(String text) { return null; }
    static MutableComponent empty() { return null; }
    static MutableComponent translatable(String key) { return null; }
    static MutableComponent translatable(String key, Object... args) { return null; }
}
""")
add("net.minecraft.network.chat", "MutableComponent", """
import net.minecraft.ChatFormatting;
public interface MutableComponent extends Component {
    MutableComponent withStyle(ChatFormatting... formats);
    MutableComponent withStyle(ChatFormatting format);
}
""")

# ------------------------------------------------------------------ network
add("net.minecraft.network", "RegistryFriendlyByteBuf", """
import io.netty.buffer.ByteBuf;
public class RegistryFriendlyByteBuf extends ByteBuf {
}
""")
add("net.minecraft.network.codec", "StreamCodec", """
import java.util.function.BiFunction;
import java.util.function.Function;
public interface StreamCodec<B, V> {
    static <B, C, T1> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1, Function<T1, C> factory) { return null; }
    static <B, C, T1, T2> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            BiFunction<T1, T2, C> factory) { return null; }
}
""")
add("net.minecraft.network.codec", "ByteBufCodecs", """
import io.netty.buffer.ByteBuf;
public class ByteBufCodecs {
    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = null;
}
""")
add("net.minecraft.network.protocol.common.custom", "CustomPacketPayload", """
import net.minecraft.resources.Identifier;
public interface CustomPacketPayload {
    Type<? extends CustomPacketPayload> type();
    static <T extends CustomPacketPayload> Type<T> createType(String id) { return null; }
    class Type<T extends CustomPacketPayload> {
        public Type(Identifier id) {}
        public Identifier id() { return null; }
    }
}
""")

# ------------------------------------------------------------------ world
add("net.minecraft.world", "InteractionResult", """
public interface InteractionResult {
    Success SUCCESS = null;
    Success SUCCESS_SERVER = null;
    Success CONSUME = null;
    Fail FAIL = null;
    Pass PASS = null;
    boolean consumesAction();
    class Success implements InteractionResult {
        public boolean consumesAction() { return true; }
    }
    class Fail implements InteractionResult {
        public boolean consumesAction() { return false; }
    }
    class Pass implements InteractionResult {
        public boolean consumesAction() { return false; }
    }
}
""")
add("net.minecraft.world", "InteractionHand", """
public enum InteractionHand { MAIN_HAND, OFF_HAND }
""")
add("net.minecraft.world.phys", "Vec3", """
import net.minecraft.core.Vec3i;
public class Vec3 {
    public Vec3(double x, double y, double z) {}
    public final double x = 0;
    public final double y = 0;
    public final double z = 0;
    public static Vec3 atCenterOf(Vec3i pos) { return null; }
    public Vec3 add(double x, double y, double z) { return null; }
}
""")
add("net.minecraft.world.phys", "AABB", """
public class AABB {
    public AABB(double x0, double y0, double z0, double x1, double y1, double z1) {}
    public AABB expandTowards(double x, double y, double z) { return null; }
}
""")
add("net.minecraft.world.phys", "BlockHitResult", """
public class BlockHitResult {
}
""")
add("net.minecraft.util", "RandomSource", """
public interface RandomSource {
}
""")

# ------------------------------------------------------------------ entity
add("net.minecraft.world.entity", "Entity", """
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public abstract class Entity {
    /** 1.21.11: eski adi hasImpulse'ti -> hurtMarked */
    public boolean hurtMarked;
    public Level level() { return null; }
    public boolean isSpectator() { return false; }
    public boolean isAlive() { return false; }
    public void discard() {}
    public void setNoGravity(boolean noGravity) {}
    public Vec3 getDeltaMovement() { return null; }
    public void setDeltaMovement(Vec3 motion) {}
    public void setDeltaMovement(double x, double y, double z) {}
    public void setPos(Vec3 pos) {}
    public UUID getUUID() { return null; }
    public BlockPos blockPosition() { return null; }
    public double getX() { return 0; }
    public double getY() { return 0; }
    public double getZ() { return 0; }
    public void setPos(double x, double y, double z) {}
    public Component getName() { return null; }
    public ItemStack getItemInHand(InteractionHand hand) { return null; }
    public void displayClientMessage(Component message, boolean actionBar) {}
    public net.minecraft.server.MinecraftServer getServer() { return null; }
}
""")
add("net.minecraft.world.entity.item", "ItemEntity", """
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public class ItemEntity extends Entity {
    public ItemEntity(Level level, double x, double y, double z, ItemStack stack) {}
    public ItemStack getItem() { return null; }
    public void setItem(ItemStack stack) {}
    public void setPickUpDelay(int delay) {}
    public void setUnlimitedLifetime() {}
}
""")
add("net.minecraft.world.entity.player", "Inventory", """
import net.minecraft.world.item.ItemStack;
public class Inventory {
    public boolean add(ItemStack stack) { return false; }
}
""")
add("net.minecraft.world.entity.player", "PlayerSkin", """
import net.minecraft.core.ClientAsset;
public record PlayerSkin(ClientAsset.Texture body, ClientAsset.Texture cape,
                         ClientAsset.Texture elytra, Object model, boolean secure) {
}
""")
add("net.minecraft.world.entity.player", "Player", """
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
public abstract class Player extends Entity {
    public Inventory getInventory() { return null; }
    public com.mojang.authlib.GameProfile getGameProfile() { return null; }
    public String getPlainTextName() { return null; }
    public String getScoreboardName() { return null; }
    public net.minecraft.world.entity.item.ItemEntity drop(ItemStack stack, boolean dropAround) { return null; }
}
""")
add("net.minecraft.server.level", "ServerPlayer", """
public class ServerPlayer extends net.minecraft.world.entity.player.Player {
    public net.minecraft.server.network.ServerGamePacketListenerImpl connection;
}
""")
add("net.minecraft.server.network", "ServerGamePacketListenerImpl", """
public class ServerGamePacketListenerImpl {
}
""")
add("net.minecraft.server", "MinecraftServer", """
import net.minecraft.server.players.PlayerList;
public abstract class MinecraftServer {
    public PlayerList getPlayerList() { return null; }
    public void execute(Runnable task) {}
}
""")
add("net.minecraft.server.players", "PlayerList", """
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
public abstract class PlayerList {
    public List<ServerPlayer> getPlayers() { return null; }
}
""")

# ------------------------------------------------------------------ item
add("net.minecraft.world.item", "ItemLike", """
public interface ItemLike {
}
""")
add("net.minecraft.world.item", "ItemStack", """
import net.minecraft.core.component.DataComponentType;
public class ItemStack implements ItemLike {
    public ItemStack(ItemLike item) {}
    public ItemStack(ItemLike item, int count) {}
    public <T> T set(DataComponentType<T> type, T value) { return null; }
    public <T> T get(DataComponentType<? extends T> type) { return null; }
    public Item getItem() { return null; }
    public boolean isEmpty() { return false; }
    public ItemStack copy() { return null; }
}
""")
add("net.minecraft.world.item", "Item", """
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
public class Item implements ItemLike {
    public Item(Properties properties) {}
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) { return null; }
    public Component getName(ItemStack stack) { return null; }
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> adder, TooltipFlag flag) {}
    public interface TooltipContext {
        TooltipContext EMPTY = null;
    }
    public static class Properties {
        public Properties stacksTo(int maxStackSize) { return this; }
    }
}
""")
add("net.minecraft.world.item", "BlockItem", """
import net.minecraft.world.level.block.Block;
public class BlockItem extends Item {
    public BlockItem(Block block, Properties properties) { super(properties); }
}
""")
add("net.minecraft.world.item", "TooltipFlag", """
public interface TooltipFlag {
    Default NORMAL = null;
    Default ADVANCED = null;
    boolean isAdvanced();
    interface Default extends TooltipFlag {
    }
}
""")
add("net.minecraft.world.item.component", "TooltipDisplay", """
public record TooltipDisplay(boolean hideTooltip, java.util.SequencedSet<net.minecraft.core.component.DataComponentType<?>> hiddenComponents) {
}
""")
add("net.minecraft.world.item", "CreativeModeTab", """
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
public class CreativeModeTab {
    public static Builder builder(Row row, int column) { return null; }
    public enum Row { TOP, BOTTOM }
    public interface ItemDisplayParameters {
    }
    public interface Output {
        void accept(ItemLike item);
        void accept(ItemStack stack);
    }
    public interface DisplayItemsGenerator {
        void accept(ItemDisplayParameters parameters, Output output);
    }
    public static class Builder {
        public Builder title(Component title) { return this; }
        public Builder icon(Supplier<ItemStack> icon) { return this; }
        public Builder displayItems(DisplayItemsGenerator generator) { return this; }
        public CreativeModeTab build() { return null; }
    }
}
""")
add("net.minecraft.world.item.context", "BlockPlaceContext", """
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
public class BlockPlaceContext {
    public BlockPos getClickedPos() { return null; }
    public Level getLevel() { return null; }
    public Player getPlayer() { return null; }
    public Direction getHorizontalDirection() { return null; }
    public Direction getNearestLookingDirection() { return null; }
}
""")

# ------------------------------------------------------------------ level / block
add("net.minecraft.world.level", "Level", """
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
public abstract class Level {
    public boolean isClientSide() { return false; }
    public BlockState getBlockState(BlockPos pos) { return null; }
    public BlockEntity getBlockEntity(BlockPos pos) { return null; }
    public boolean setBlock(BlockPos pos, BlockState state, int flags) { return false; }
    public boolean removeBlock(BlockPos pos, boolean isMoving) { return false; }
    public boolean addFreshEntity(Entity entity) { return false; }
    public List<ServerPlayer> players() { return null; }
    public List<Entity> getEntities(Entity except, AABB bounds,
                                    java.util.function.Predicate<? super Entity> predicate) { return null; }
    public void scheduleTick(BlockPos pos, net.minecraft.world.level.block.Block block, int delay) {}
}
""")
add("net.minecraft.server.level", "ServerLevel", """
import java.util.UUID;
import net.minecraft.world.entity.Entity;
public class ServerLevel extends net.minecraft.world.level.Level {
    public Entity getEntity(UUID uuid) { return null; }
}
""")
add("net.minecraft.world.level.block", "Block", """
import java.util.function.Function;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
public class Block extends BlockBehaviour {
    public Block(Properties properties) { super(properties); }
    public MapCodec<? extends Block> codec() { return null; }
    protected static <B extends Block> MapCodec<B> simpleCodec(Function<Properties, B> factory) { return null; }
    public static void dropResources(BlockState state, Level level, BlockPos pos) {}
    public BlockState defaultBlockState() { return null; }
    protected void registerDefaultState(BlockState state) {}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {}
    public BlockState getStateForPlacement(BlockPlaceContext context) { return null; }
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state,
                                        net.minecraft.world.entity.player.Player player) { return null; }
}
""")
add("net.minecraft.world.level.block.state", "BlockBehaviour", """
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
public abstract class BlockBehaviour {
    protected BlockBehaviour(Properties properties) {}
    public abstract MapCodec<? extends Block> codec();
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) { return null; }
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {}
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}
    public static class Properties {
        public static Properties of() { return null; }
        public Properties strength(float strength) { return this; }
        public Properties strength(float destroyTime, float explosionResistance) { return this; }
        public Properties noOcclusion() { return this; }
        public Properties requiresCorrectToolForDrops() { return this; }
    }
}
""")
add("net.minecraft.world.level.block.state.properties", "Property", """
public abstract class Property<T extends Comparable<T>> {
    public String getName() { return null; }
}
""")
add("net.minecraft.world.level.block.state.properties", "EnumProperty", """
public class EnumProperty<T extends Enum<T>> extends Property<T> {
}
""")
add("net.minecraft.world.level.block.state.properties", "BooleanProperty", """
public class BooleanProperty extends Property<Boolean> {
    public static BooleanProperty create(String name) { return null; }
}
""")
add("net.minecraft.world.level.block.state.properties", "BlockStateProperties", """
import net.minecraft.core.Direction;
public class BlockStateProperties {
    public static final EnumProperty<Direction> HORIZONTAL_FACING = null;
    public static final EnumProperty<Direction> FACING = null;
}
""")
add("net.minecraft.world.level.block.state", "StateDefinition", """
import net.minecraft.world.level.block.state.properties.Property;
public class StateDefinition<O, S> {
    public static class Builder<O, S> {
        public Builder<O, S> add(Property<?>... properties) { return this; }
    }
}
""")
add("net.minecraft.world.level.block.state", "BlockState", """
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
public class BlockState {
    public Block getBlock() { return null; }
    public boolean is(Block block) { return false; }
    public <T extends Comparable<T>> T getValue(Property<T> property) { return null; }
    public <T extends Comparable<T>, V extends T> BlockState setValue(Property<T> property, V value) { return null; }
    public boolean hasProperty(Property<?> property) { return false; }
    public boolean canBeReplaced(BlockPlaceContext context) { return false; }
    public boolean canBeReplaced() { return false; }
}
""")
add("net.minecraft.world.level.block.entity", "BlockEntity", """
public abstract class BlockEntity {
}
""")

# ------------------------------------------------------------------ client
add("net.minecraft.client.gui.components.events", "GuiEventListener", """
public interface GuiEventListener {
}
""")
add("net.minecraft.client.gui.components", "Renderable", """
public interface Renderable {
}
""")
add("net.minecraft.client.gui.narration", "NarratableEntry", """
public interface NarratableEntry {
}
""")
add("net.minecraft.client.gui.components", "AbstractWidget", """
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
public abstract class AbstractWidget implements GuiEventListener, Renderable, NarratableEntry {
}
""")
add("net.minecraft.client.gui.components", "Button", """
import net.minecraft.network.chat.Component;
public class Button extends AbstractWidget {
    public static Builder builder(Component message, OnPress onPress) { return null; }
    public interface OnPress {
        void onPress(Button button);
    }
    public static class Builder {
        public Builder bounds(int x, int y, int width, int height) { return this; }
        public Button build() { return null; }
    }
}
""")
add("net.minecraft.client.gui.components", "EditBox", """
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
public class EditBox extends AbstractWidget {
    public EditBox(Font font, int x, int y, int width, int height, Component message) {}
    public void setMaxLength(int length) {}
    public String getValue() { return null; }
    public void setValue(String text) {}
    public void setTextColor(int color) {}
    public void setBordered(boolean bordered) {}
    public boolean isFocused() { return false; }
    public void setFocused(boolean focused) {}
}
""")
add("net.minecraft.client.gui", "Font", """
import net.minecraft.network.chat.Component;
public class Font {
    public int width(String text) { return 0; }
    public int width(Component text) { return 0; }
}
""")
add("net.minecraft.client.gui", "GuiGraphics", """
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
public class GuiGraphics {
    public void fill(int minX, int minY, int maxX, int maxY, int color) {}
    public void drawString(Font font, String text, int x, int y, int color) {}
    public void drawString(Font font, String text, int x, int y, int color, boolean dropShadow) {}
    public void drawString(Font font, Component text, int x, int y, int color) {}
    public void drawString(Font font, Component text, int x, int y, int color, boolean dropShadow) {}
    public void drawCenteredString(Font font, Component text, int x, int y, int color) {}
    public void drawCenteredString(Font font, String text, int x, int y, int color) {}
    public org.joml.Matrix3x2fStack pose() { return null; }
    public void blit(com.mojang.blaze3d.pipeline.RenderPipeline pipeline, Identifier texture,
                     int x, int y, float u, float v, int width, int height,
                     int textureWidth, int textureHeight) {}
}
""")
add("net.minecraft.client.renderer", "RenderPipelines", """
import com.mojang.blaze3d.pipeline.RenderPipeline;
public class RenderPipelines {
    public static final RenderPipeline GUI_TEXTURED = null;
    public static final RenderPipeline GUI = null;
}
""")
add("net.minecraft.client.gui.screens", "Screen", """
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
public abstract class Screen {
    protected final Minecraft minecraft = null;
    protected final Component title = null;
    protected final Font font = null;
    public int width;
    public int height;
    protected Screen(Component title) {}
    protected void init() {}
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
    protected <T extends GuiEventListener> T addRenderableWidget(T widget) { return widget; }
    public void setInitialFocus(GuiEventListener listener) {}
    public void onClose() {}
    public boolean isPauseScreen() { return true; }
}
""")
add("net.minecraft.client.multiplayer", "ClientPacketListener", """
import java.util.UUID;
public class ClientPacketListener {
    public PlayerInfo getPlayerInfo(UUID uuid) { return null; }
    public PlayerInfo getPlayerInfo(String name) { return null; }
}
""")
add("net.minecraft.client.multiplayer", "PlayerInfo", """
import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.PlayerSkin;
public class PlayerInfo {
    public PlayerSkin getSkin() { return null; }
    public GameProfile getProfile() { return null; }
}
""")
add("net.minecraft.client.resources", "DefaultPlayerSkin", """
import java.util.UUID;
import net.minecraft.world.entity.player.PlayerSkin;
public class DefaultPlayerSkin {
    public static PlayerSkin get(UUID uuid) { return null; }
    public static PlayerSkin getDefaultSkin() { return null; }
}
""")
add("net.minecraft.client", "Minecraft", """
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
public class Minecraft {
    public static Minecraft getInstance() { return null; }
    public void setScreen(Screen screen) {}
    public void execute(Runnable task) {}
    public ClientPacketListener getConnection() { return null; }
}
""")

# ------------------------------------------------------------------ fabric
add("net.fabricmc.api", "ModInitializer", """
public interface ModInitializer {
    void onInitialize();
}
""")
add("net.fabricmc.api", "ClientModInitializer", """
public interface ClientModInitializer {
    void onInitializeClient();
}
""")
add("net.fabricmc.fabric.api.networking.v1", "PayloadTypeRegistry", """
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public interface PayloadTypeRegistry<B> {
    static PayloadTypeRegistry<RegistryFriendlyByteBuf> playC2S() { return null; }
    static PayloadTypeRegistry<RegistryFriendlyByteBuf> playS2C() { return null; }
    <T extends CustomPacketPayload> Object register(CustomPacketPayload.Type<T> type,
                                                    StreamCodec<? super B, T> codec);
}
""")
add("net.fabricmc.fabric.api.networking.v1", "ServerPlayNetworking", """
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public final class ServerPlayNetworking {
    public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(
            CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) { return false; }
    public static void send(ServerPlayer player, CustomPacketPayload payload) {}
    public interface PlayPayloadHandler<T extends CustomPacketPayload> {
        void receive(T payload, Context context);
    }
    public interface Context {
        MinecraftServer server();
        ServerPlayer player();
    }
}
""")
add("net.fabricmc.fabric.api.client.networking.v1", "ClientPlayNetworking", """
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public final class ClientPlayNetworking {
    public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(
            CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) { return false; }
    public static void send(CustomPacketPayload payload) {}
    public interface PlayPayloadHandler<T extends CustomPacketPayload> {
        void receive(T payload, Context context);
    }
    public interface Context {
        Minecraft client();
    }
}
""")
add("io.netty.buffer", "ByteBuf", """
public abstract class ByteBuf {
}
""")
add("org.slf4j", "Logger", """
public interface Logger {
    void info(String msg);
    void info(String format, Object arg);
    void warn(String msg);
    void error(String msg);
}
""")
add("org.slf4j", "LoggerFactory", """
public class LoggerFactory {
    public static Logger getLogger(String name) { return null; }
}
""")


def main():
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    count = 0
    for pkg, name, body in FILES:
        d = os.path.join(OUT, pkg.replace(".", os.sep))
        os.makedirs(d, exist_ok=True)
        with open(os.path.join(d, name + ".java"), "w", encoding="utf-8") as fh:
            fh.write("package %s;\n" % pkg)
            fh.write(body.lstrip("\n"))
        count += 1
    print("Stub uretildi: %d sinif -> %s" % (count, OUT))


if __name__ == "__main__":
    main()
