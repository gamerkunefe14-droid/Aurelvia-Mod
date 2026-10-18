package tr.aurelvia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import tr.aurelvia.block.entity.IdMachineBlockEntity;

import java.util.List;

/**
 * Konveyor blogu.
 *
 * - Tek basina yerlestirilirse GERCEK konveyor gibi calisir:
 *   uzerindeki esyalari/varliklari baktigi yone dogru iter.
 * - Kimlik makinesinin parcasi olarak yerlestirildiginde (ATTACHED = true)
 *   uretilen kimlik kartini tasir; sag tiklaninca karti ele verir.
 */
public class ConveyorBlock extends Block {

    public static final MapCodec<ConveyorBlock> CODEC = simpleCodec(ConveyorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");

    /** Kac tickte bir itme yapilacagi. */
    private static final int TICK_DELAY = 4;
    /** Her tick'te uygulanacak hiz. */
    private static final double PUSH_SPEED = 0.075D;

    public ConveyorBlock(Properties properties) {
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

    // ------------------------------------------------------------- konveyor isi

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, TICK_DELAY);
        }
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos,
                        RandomSource random) {
        level.scheduleTick(pos, this, TICK_DELAY);

        if (Boolean.TRUE.equals(state.getValue(ATTACHED))) {
            return; // makinenin parcasi: esya tasimaz, karti bekler
        }

        Direction direction = state.getValue(FACING);
        // Blogun kendi hacmi + tam ustu
        AABB box = new AABB(pos).expandTowards(0.0D, 1.0D, 0.0D);
        List<Entity> entities = level.getEntities(null, box);
        for (Entity entity : entities) {
            if (entity instanceof ItemEntity item && item.getPickUpDelay() > 100) {
                continue; // makinenin sergiledigi kart gibi "alinamaz" esyalari itme
            }
            if (!entity.isAlive() || entity.isSpectator()) {
                continue;
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    direction.getStepX() * PUSH_SPEED,
                    0.0D,
                    direction.getStepZ() * PUSH_SPEED));
            entity.hasImpulse = true;
        }
    }

    // --------------------------------------------------------------- etkilesim

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!Boolean.TRUE.equals(state.getValue(ATTACHED))) {
            return InteractionResult.PASS;
        }

        BlockPos machinePos = IdMachineStructure.findMachine(level, pos);
        if (machinePos != null && level.getBlockEntity(machinePos) instanceof IdMachineBlockEntity machine) {
            if (machine.takeCard(player)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(
                            net.minecraft.network.chat.Component.translatable("gui.aurelvia_machines.card_taken"));
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && Boolean.TRUE.equals(state.getValue(ATTACHED))) {
            IdMachineStructure.onPartBroken(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
