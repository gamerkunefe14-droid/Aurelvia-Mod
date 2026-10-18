package tr.aurelvia.networking;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.block.IdMachineStructure;

/**
 * Sunucu <-> istemci paketleri.
 *
 *  S2C  open_machine  : "bu makine icin sifre sor"
 *  C2S  password      : oyuncunun girdigi sifre
 *  S2C  player_list   : dogru sifreden sonra gercek oyuncu listesi
 *  C2S  select_player : secilen oyuncu -> konveyora kimlik karti bas
 */
public final class ModNetworking {

    private ModNetworking() {
    }

    // ------------------------------------------------------------------ paketler
    /** S2C: sifre ekranini ac. */
    public record OpenMachinePayload(BlockPos pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenMachinePayload> TYPE =
                CustomPacketPayload.createType("aurelvia_machines:open_machine");
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenMachinePayload> STREAM_CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, OpenMachinePayload::pos, OpenMachinePayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** C2S: girilen sifre. */
    public record PasswordPayload(BlockPos pos, String password) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PasswordPayload> TYPE =
                CustomPacketPayload.createType("aurelvia_machines:password");
        public static final StreamCodec<RegistryFriendlyByteBuf, PasswordPayload> STREAM_CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, PasswordPayload::pos,
                        ByteBufCodecs.STRING_UTF8, PasswordPayload::password, PasswordPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** S2C: sunucudaki gercek oyuncular. data = "ad|uuid;ad|uuid;..." */
    public record PlayerListPayload(BlockPos pos, String data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PlayerListPayload> TYPE =
                CustomPacketPayload.createType("aurelvia_machines:player_list");
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerListPayload> STREAM_CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, PlayerListPayload::pos,
                        ByteBufCodecs.STRING_UTF8, PlayerListPayload::data, PlayerListPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** C2S: secilen oyuncu. data = "ad|uuid" */
    public record SelectPlayerPayload(BlockPos pos, String data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SelectPlayerPayload> TYPE =
                CustomPacketPayload.createType("aurelvia_machines:select_player");
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectPlayerPayload> STREAM_CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, SelectPlayerPayload::pos,
                        ByteBufCodecs.STRING_UTF8, SelectPlayerPayload::data, SelectPlayerPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // ------------------------------------------------------------------ kayit
    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenMachinePayload.TYPE, OpenMachinePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerListPayload.TYPE, PlayerListPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PasswordPayload.TYPE, PasswordPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SelectPlayerPayload.TYPE, SelectPlayerPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PasswordPayload.TYPE, ModNetworking::onPassword);
        ServerPlayNetworking.registerGlobalReceiver(SelectPlayerPayload.TYPE, ModNetworking::onSelectPlayer);
    }

    // --------------------------------------------------------------- sunucu tarafi
    private static void onPassword(PasswordPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        context.server().execute(() -> {
            if (AurelviaMachinesMod.MACHINE_PASSWORD.equals(payload.password())) {
                ServerPlayNetworking.send(player, new PlayerListPayload(payload.pos(), collectPlayers(context)));
            } else {
                player.displayClientMessage(
                        Component.literal("Yanl\u0131\u015f \u015fifre!").withStyle(ChatFormatting.RED), true);
            }
        });
    }

    private static void onSelectPlayer(SelectPlayerPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        context.server().execute(() -> {
            String[] parts = splitEntry(payload.data());
            if (parts == null) {
                return;
            }
            String name = parts[0];
            String uuid = parts[1];

            Level level = player.level();
            BlockPos conveyorPos = IdMachineStructure.conveyorPos(level, payload.pos());
            IdMachineStructure.spawnCard(level, conveyorPos, name, uuid);

            player.displayClientMessage(
                    Component.literal("Kimlik kart\u0131 haz\u0131r: " + name).withStyle(ChatFormatting.GREEN), true);
        });
    }

    /** Sunucudaki gercek oyunculari "ad|uuid;..." biciminde toplar. */
    private static String collectPlayers(ServerPlayNetworking.Context context) {
        StringBuilder sb = new StringBuilder();
        List<ServerPlayer> players = context.server().getPlayerList().getPlayers();
        for (ServerPlayer p : players) {
            String name = null;
            if (p.getGameProfile() != null) {
                name = p.getGameProfile().getName();
            }
            if (name == null || name.isEmpty()) {
                name = p.getPlainTextName();
            }
            if (name == null || name.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(name).append('|').append(p.getUUID().toString());
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ yardimcilar
    public static List<String[]> parseEntries(String data) {
        List<String[]> out = new ArrayList<String[]>();
        if (data == null || data.isEmpty()) {
            return out;
        }
        for (String chunk : data.split(";")) {
            String[] entry = splitEntry(chunk);
            if (entry != null) {
                out.add(entry);
            }
        }
        return out;
    }

    private static String[] splitEntry(String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return null;
        }
        int idx = chunk.indexOf('|');
        if (idx <= 0 || idx >= chunk.length() - 1) {
            return null;
        }
        return new String[] { chunk.substring(0, idx), chunk.substring(idx + 1) };
    }
}
