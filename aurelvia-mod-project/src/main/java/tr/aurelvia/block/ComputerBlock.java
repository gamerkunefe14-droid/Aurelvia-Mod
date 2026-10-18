package tr.aurelvia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import tr.aurelvia.block.entity.IdMachineBlockEntity;
import tr.aurelvia.networking.ModNetworking;

/**
 * Bilgisayar blogu.
 *
 * Kural: TEK BASINA KULLANILAMAZ. Sadece kimlik makinesinin ustunde ise calisir
 * ve sag tiklaninca sifre ekranini acar.
 */
public class ComputerBlock extends Block {

    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Makineye bagli mi? (bagliysa kirilinca item dusurmez) */
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");

    public ComputerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ATTACHED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ATTACHED, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos machinePos = IdMachineStructure.findMachine(level, pos);
        if (machinePos == null || !(level.getBlockEntity(machinePos) instanceof IdMachineBlockEntity)) {
            // Tek basina kullanilamaz
            player.sendSystemMessage(Component.translatable("gui.aurelvia_machines.computer_alone"));
            return InteractionResult.CONSUME;
        }

        // Her acilista sifre sorulur
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.openMachine(serverPlayer, machinePos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && Boolean.TRUE.equals(state.getValue(ATTACHED))) {
            IdMachineStructure.onPartBroken(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
