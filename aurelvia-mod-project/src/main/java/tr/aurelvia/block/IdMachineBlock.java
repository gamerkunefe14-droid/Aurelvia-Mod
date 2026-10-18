package tr.aurelvia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.networking.ModNetworking;

/**
 * Kimlik Makinesi - ana govde (soldaki parca).
 *
 *  Yerlestirildiginde otomatik olarak:
 *    - yanina (baktigi yone) KONVEYOR
 *    - ustune BILGISAYAR
 *  koyar. Uc parca birlikte calisir.
 *
 *  Govdeye veya bilgisayara sag tiklamak -> sifre sorar.
 */
public class IdMachineBlock extends Block {

    public static final MapCodec<IdMachineBlock> CODEC = simpleCodec(IdMachineBlock::new);

    public IdMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(IdMachineStructure.ATTACHED, Boolean.FALSE));
    }

    @Override
    public MapCodec<IdMachineBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, IdMachineStructure.ATTACHED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection())
                .setValue(IdMachineStructure.ATTACHED, Boolean.FALSE);
    }

    /** Yerlestirilir yerlestirmez konveyoru ve bilgisayari kurar. */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide() || movedByPiston) {
            return;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos conveyorPos = pos.relative(facing);
        BlockPos computerPos = pos.above();

        boolean okConveyor = level.getBlockState(conveyorPos).canBeReplaced();
        boolean okComputer = level.getBlockState(computerPos).canBeReplaced();

        if (!okConveyor || !okComputer) {
            return; // yer yok, tek parca olarak kalsin
        }

        // 2 blok genislik: konveyor
        level.setBlock(conveyorPos, AurelviaMachinesMod.CONVEYOR_BLOCK.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        // ustte bilgisayar
        level.setBlock(computerPos, AurelviaMachinesMod.COMPUTER_BLOCK.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(IdMachineStructure.ATTACHED, Boolean.TRUE), 3);
        // kendisini de "parca" isaretle
        level.setBlock(pos, state.setValue(IdMachineStructure.ATTACHED, Boolean.TRUE), 3);
    }

    /** Govdeye sag tiklamak bilgisayari acmakla ayni isi yapar (sifre sorar). */
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!state.getValue(IdMachineStructure.ATTACHED).booleanValue()) {
            player.displayClientMessage(
                    Component.literal("Makine eksik: yaninda konveyor ve ustunde bilgisayar olmali."), true);
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer) {
            ServerPlayNetworking.send((ServerPlayer) player, new ModNetworking.OpenMachinePayload(pos));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Kirilinca parcalari da temizler.
     * (1.21.11'de onRemove kaldirildigi icin playerWillDestroy kullaniyoruz.)
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && state.getValue(IdMachineStructure.ATTACHED).booleanValue()) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos conveyorPos = pos.relative(facing);
            BlockPos computerPos = pos.above();

            Block.dropResources(level.getBlockState(conveyorPos), level, conveyorPos);
            level.removeBlock(conveyorPos, false);

            Block.dropResources(level.getBlockState(computerPos), level, computerPos);
            level.removeBlock(computerPos, false);

            // Konveyorun ustunde unutulmus kart varsa onu da kaldir
            net.minecraft.world.entity.item.ItemEntity card =
                    IdMachineStructure.findCard(level, conveyorPos);
            if (card != null) {
                card.discard();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
