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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.block.entity.IdMachineBlockEntity;
import tr.aurelvia.networking.ModNetworking;

/**
 * Kimlik makinesi bloğu - yerleştirildiğinde 2 blok genişliğinde yapı oluşturur
 * Sağ tıklanınca şifre ekranı açılır
 */
public class IdMachineBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<IdMachineBlock> CODEC = simpleCodec(IdMachineBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public IdMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends IdMachineBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Level level = context.getLevel();
        BlockPos mainPos = context.getClickedPos();
        
        // Konveyör için yer kontrol et
        BlockPos conveyorPos = mainPos.relative(facing.getClockWise());
        if (!level.getBlockState(conveyorPos).canBeReplaced(context)) {
            return null; // Yer yok
        }
        
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        
        if (!level.isClientSide() && !movedByPiston) {
            Direction facing = state.getValue(FACING);
            BlockPos conveyorPos = pos.relative(facing.getClockWise());
            
            // Konveyör bloğunu yerleştir
            if (level.getBlockState(conveyorPos).canBeReplaced()) {
                level.setBlock(conveyorPos, 
                    AurelviaMachinesMod.CONVEYOR_BLOCK.defaultBlockState().setValue(ConveyorBlock.FACING, facing), 
                    3);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IdMachineBlockEntity idMachine) {
                // Client'a şifre ekranı açmasını söyle
                ModNetworking.sendOpenIdMachineScreen(serverPlayer, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IdMachineBlockEntity idMachine) {
                // Konveyör bloğunu da kaldır
                Direction facing = state.getValue(FACING);
                BlockPos conveyorPos = pos.relative(facing.getClockWise());
                if (level.getBlockState(conveyorPos).is(AurelviaMachinesMod.CONVEYOR_BLOCK)) {
                    level.destroyBlock(conveyorPos, true);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IdMachineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof IdMachineBlockEntity idMachine) {
                idMachine.tick();
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
