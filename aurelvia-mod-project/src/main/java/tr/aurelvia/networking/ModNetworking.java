package tr.aurelvia.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.block.entity.IdMachineBlockEntity;

/**
 * Network paketleri
 */
public class ModNetworking {
    
    // Şifre doğrulama paketi (Client -> Server)
    public record PasswordPayload(BlockPos pos, String password) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PasswordPayload> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AurelviaMachinesMod.MOD_ID, "password"));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, PasswordPayload> STREAM_CODEC = 
            StreamCodec.composite(
                BlockPos.STREAM_CODEC, PasswordPayload::pos,
                ByteBufCodecs.STRING_UTF8, PasswordPayload::password,
                PasswordPayload::new
            );
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    // Oyuncu seçimi paketi (Client -> Server)
    public record PlayerSelectPayload(BlockPos pos, String playerName) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PlayerSelectPayload> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AurelviaMachinesMod.MOD_ID, "player_select"));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSelectPayload> STREAM_CODEC = 
            StreamCodec.composite(
                BlockPos.STREAM_CODEC, PlayerSelectPayload::pos,
                ByteBufCodecs.STRING_UTF8, PlayerSelectPayload::playerName,
                PlayerSelectPayload::new
            );
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    // Makine açma paketi (Server -> Client)
    public record OpenIdMachinePayload(BlockPos pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenIdMachinePayload> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AurelviaMachinesMod.MOD_ID, "open_id_machine"));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenIdMachinePayload> STREAM_CODEC = 
            StreamCodec.composite(
                BlockPos.STREAM_CODEC, OpenIdMachinePayload::pos,
                OpenIdMachinePayload::new
            );
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    
    // Oyuncu listesi paketi (Server -> Client)
    public record PlayerListPayload(BlockPos pos, java.util.List<String> playerNames) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PlayerListPayload> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AurelviaMachinesMod.MOD_ID, "player_list"));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerListPayload> STREAM_CODEC = 
            StreamCodec.composite(
                BlockPos.STREAM_CODEC, PlayerListPayload::pos,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), PlayerListPayload::playerNames,
                PlayerListPayload::new
            );
        
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register() {
        AurelviaMachinesMod.LOGGER.info("Registering Aurelvia networking");
        
        // Server-side packet handlers
        PayloadTypeRegistry.playC2S().register(PasswordPayload.TYPE, PasswordPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PlayerSelectPayload.TYPE, PlayerSelectPayload.STREAM_CODEC);
        
        // Client-side packet types
        PayloadTypeRegistry.playS2C().register(OpenIdMachinePayload.TYPE, OpenIdMachinePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerListPayload.TYPE, PlayerListPayload.STREAM_CODEC);
        
        // Server-side handlers
        ServerPlayNetworking.registerGlobalReceiver(PasswordPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                handlePassword(payload, context.player());
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(PlayerSelectPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                handlePlayerSelect(payload, context.player());
            });
        });
    }
    
    private static void handlePassword(PasswordPayload payload, ServerPlayer player) {
        String correctPassword = "UOhfyu7679nu6obBnoeryb79huı824byuYBYO";
        
        if (correctPassword.equals(payload.password())) {
            // Şifre doğru - oyuncu listesini gönder
            java.util.List<String> playerNames = player.getServer().getPlayerList().getPlayers()
                .stream()
                .map(p -> p.getGameProfile().getName())
                .collect(java.util.stream.Collectors.toList());
            
            sendPlayerList(player, payload.pos(), playerNames);
        } else {
            // Şifre yanlış
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("gui.aurelvia.wrong_password"));
        }
    }
    
    private static void handlePlayerSelect(PlayerSelectPayload payload, ServerPlayer player) {
        BlockEntity blockEntity = player.level().getBlockEntity(payload.pos());
        if (blockEntity instanceof IdMachineBlockEntity idMachine) {
            idMachine.startProducing(payload.playerName());
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("gui.aurelvia.producing_card", payload.playerName()));
        }
    }
    
    public static void sendOpenIdMachineScreen(ServerPlayer player, BlockPos pos) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket(
            new OpenIdMachinePayload(pos)
        ));
    }
    
    public static void sendPlayerList(ServerPlayer player, BlockPos pos, java.util.List<String> playerNames) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket(
            new PlayerListPayload(pos, playerNames)
        ));
    }
}
