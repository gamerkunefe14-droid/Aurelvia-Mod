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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.block.entity.IdMachineBlockEntity;
import tr.aurelvia.networking.ModNetworking;


/**
 * Kimlik Makinesi (asil blok).
 *
 * Yerlestirildiginde otomatik olarak:
 *   - sagina  konveyor
 *   - ustune  bilgisayar
 * koyar. Yani makine 2 blok genisliginde, 1 blok yuksekliginde olur ve
 * ustunde bilgisayar bulunur.
 *
 * Bilgisayara (veya makineye) sag tiklaninca sifre sorulur.
 */
public class IdMachineBlock extends Block implements EntityBlock {

    public static final MapCodec<IdMachineBlock> CODEC = simpleCodec(IdMachineBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public IdMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        // Konveyor + bilgisayar icin yer var mi?
        if (!IdMachineStructure.hasSpaceFor(level, pos, facing, context)) {
            Player player = context.getPlayer();
            if (player != null && !level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("gui.aurelvia_machines.no_space"));
            }
            return null; // yerlestirme iptal
        }

        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide() || IdMachineStructure.destroying) {
            return;
        }

        Direction facing = state.getValue(FACING);
        BlockPos conveyorPos = IdMachineStructure.conveyorPos(pos, facing);
        BlockPos computerPos = IdMachineStructure.computerPos(pos);

        if (level.getBlockState(conveyorPos).canBeReplaced()) {
            level.setBlock(conveyorPos, AurelviaMachinesMod.CONVEYOR_BLOCK.defaultBlockState()
                    .setValue(ConveyorBlock.FACING, facing)
                    .setValue(ConveyorBlock.ATTACHED, Boolean.TRUE), 3);
        }
        if (level.getBlockState(computerPos).canBeReplaced()) {
            level.setBlock(computerPos, AurelviaMachinesMod.COMPUTER_BLOCK.defaultBlockState()
                    .setValue(ComputerBlock.FACING, facing)
                    .setValue(ComputerBlock.ATTACHED, Boolean.TRUE), 3);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof IdMachineBlockEntity machine) {
            // Konveyorde hazir kart varsa direkt ver
            if (machine.hasCard() && machine.takeCard(player)) {
                player.sendSystemMessage(Component.translatable("gui.aurelvia_machines.card_taken"));
                return InteractionResult.CONSUME;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                ModNetworking.openMachine(serverPlayer, pos);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof IdMachineBlockEntity machine) {
                machine.dropPendingCard(level, pos);
            }
            IdMachineStructure.removeParts(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IdMachineBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (blockEntity instanceof IdMachineBlockEntity machine) {
                machine.serverTick();
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
