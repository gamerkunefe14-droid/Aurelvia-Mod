package tr.aurelvia.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.block.entity.IdMachineBlockEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Makine <-> istemci arasindaki tum paketler.
 *
 *  S2C: OPEN_MACHINE  -> sifre ekranini ac
 *  S2C: PLAYER_LIST   -> oyuncu listesini gonder
 *  C2S: SUBMIT_PASSWORD
 *  C2S: SELECT_PLAYER
 */
public final class ModNetworking {

    // ------------------------------------------------------------------ S2C

    /** Sunucu, istemciden sifre ekranini acmasini ister. */
    public record OpenMachinePayload(BlockPos pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenMachinePayload> TYPE =
                new CustomPacketPayload.Type<>(AurelviaMachinesMod.id("open_machine"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenMachinePayload> STREAM_CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, OpenMachinePayload::pos, OpenMachinePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Sifre dogruysa gonderilen oyuncu listesi. */
    public record PlayerListPayload(BlockPos pos, List<String> names, List<String> uuids)
            implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PlayerListPayload> TYPE =
                new CustomPacketPayload.Type<>(AurelviaMachinesMod.id("player_list"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerListPayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, PlayerListPayload::pos,
                        ByteBufCodecs.list(ByteBufCodecs.STRING_UTF8), PlayerListPayload::names,
                        ByteBufCodecs.list(ByteBufCodecs.STRING_UTF8), PlayerListPayload::uuids,
                        PlayerListPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // ------------------------------------------------------------------ C2S

    public record SubmitPasswordPayload(BlockPos pos, String password) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SubmitPasswordPayload> TYPE =
                new CustomPacketPayload.Type<>(AurelviaMachinesMod.id("submit_password"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SubmitPasswordPayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, SubmitPasswordPayload::pos,
                        ByteBufCodecs.STRING_UTF8, SubmitPasswordPayload::password,
                        SubmitPasswordPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SelectPlayerPayload(BlockPos pos, String name, String uuid) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SelectPlayerPayload> TYPE =
                new CustomPacketPayload.Type<>(AurelviaMachinesMod.id("select_player"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectPlayerPayload> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, SelectPlayerPayload::pos,
                        ByteBufCodecs.STRING_UTF8, SelectPlayerPayload::name,
                        ByteBufCodecs.STRING_UTF8, SelectPlayerPayload::uuid,
                        SelectPlayerPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // -------------------------------------------------------------- kayit

    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenMachinePayload.TYPE, OpenMachinePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerListPayload.TYPE, PlayerListPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SubmitPasswordPayload.TYPE, SubmitPasswordPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SelectPlayerPayload.TYPE, SelectPlayerPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SubmitPasswordPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = player.getServer();
            if (server == null) {
                return;
            }
            server.execute(() -> handlePassword(player, payload.pos(), payload.password()));
        });

        ServerPlayNetworking.registerGlobalReceiver(SelectPlayerPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = player.getServer();
            if (server == null) {
                return;
            }
            server.execute(() -> handleSelect(player, payload.pos(), payload.name(), payload.uuid()));
        });
    }

    // ------------------------------------------------------------- isleyiciler

    private static void handlePassword(ServerPlayer player, BlockPos pos, String password) {
        if (!AurelviaMachinesMod.MACHINE_PASSWORD.equals(password)) {
            player.sendSystemMessage(Component.translatable("gui.aurelvia_machines.wrong_password")
                    .withStyle(net.minecraft.ChatFormatting.RED));
            // Ekran acik kalsin, tekrar deneyebilsin
            ServerPlayNetworking.send(player, new OpenMachinePayload(pos));
            return;
        }

        // Sifre dogru -> oyuncu listesini gonder (her acilista tekrar sorulur)
        Map<String, String> players = collectPlayers(player.getServer());
        List<String> names = new ArrayList<>(players.keySet());
        List<String> uuids = new ArrayList<>(players.values());
        ServerPlayNetworking.send(player, new PlayerListPayload(pos, names, uuids));
    }

    private static void handleSelect(ServerPlayer player, BlockPos pos, String name, String uuid) {
        if (name == null || name.isEmpty()) {
            return;
        }
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (!(blockEntity instanceof IdMachineBlockEntity machine)) {
            return;
        }
        if (machine.hasCard()) {
            player.sendSystemMessage(Component.translatable("gui.aurelvia_machines.busy")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW));
            return;
        }
        machine.startProducing(name, uuid);
        player.sendSystemMessage(Component.translatable("gui.aurelvia_machines.producing", name)
                .withStyle(net.minecraft.ChatFormatting.GREEN));
    }

    /**
     * Sunucudaki oyuncular: once cevrimici olanlar, sonra (varsa) kullanici onbellegi.
     * Onbellek erisimi reflection ile yapilir; surum degisirse sessizce atlanir.
     */
    public static Map<String, String> collectPlayers(MinecraftServer server) {
        Map<String, String> result = new LinkedHashMap<>();
        if (server == null) {
            return result;
        }
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            String name = p.getName().getString();
            if (name != null && !name.isEmpty()) {
                result.putIfAbsent(name, p.getUUID().toString());
            }
        }
        addCachedProfiles(server, result);
        return result;
    }

    private static void addCachedProfiles(MinecraftServer server, Map<String, String> result) {
        try {
            Object cache = server.getClass().getMethod("getProfileCache").invoke(server);
            if (cache == null) {
                return;
            }
            Object stream = cache.getClass().getMethod("getTopMRUProfiles", int.class).invoke(cache, 64);
            if (!(stream instanceof java.util.stream.Stream<?> profiles)) {
                return;
            }
            for (Object o : profiles.toList()) {
                if (!(o instanceof com.mojang.authlib.GameProfile profile)) {
                    continue;
                }
                String name = profile.getName();
                if (name == null || name.isEmpty()) {
                    continue;
                }
                result.putIfAbsent(name, profile.getId() == null ? "" : profile.getId().toString());
            }
        } catch (Throwable ignored) {
            // Bu surumde kullanici onbellegi erisilebilir degil - sadece cevrimici oyuncular kalir.
        }
    }

    public static void openMachine(ServerPlayer player, BlockPos pos) {
        ServerPlayNetworking.send(player, new OpenMachinePayload(pos));
    }

    private ModNetworking() {
    }
}
