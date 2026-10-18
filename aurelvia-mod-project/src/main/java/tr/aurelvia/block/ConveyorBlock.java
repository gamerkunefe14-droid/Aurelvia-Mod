package tr.aurelvia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import tr.aurelvia.item.IdCardItem;

import java.util.List;

/**
 * Konveyor.
 *
 *  - TEK BASINA calisan gercek bir konveyordur: ustune cikan her seyi baktigi yone tasir.
 *  - Kimlik makinesinin bir parcasi olarak kullanildiginda uzerindeki kimlik kartini TASIMAZ,
 *    kart orada bekler; sag tikla ele alinir.
 */
public class ConveyorBlock extends Block {

    public static final MapCodec<ConveyorBlock> CODEC = simpleCodec(ConveyorBlock::new);

    /** Kac tick'te bir hareket uygular (2 tick = 100ms). */
    private static final int TICK_DELAY = 2;
    /** Tasima hizi (blok/tick). */
    private static final double SPEED = 0.14D;

    public ConveyorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<ConveyorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    // ------------------------------------------------------------------ tasima
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        moveEntities(level, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        // Konveyor surekli calisir: bir sonraki tick'i planla
        level.scheduleTick(pos, this, TICK_DELAY);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, TICK_DELAY);
        }
    }

    private void moveEntities(Level level, BlockPos pos, Direction facing) {
        AABB box = new AABB(
                pos.getX(), pos.getY() + 0.5D, pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 1.5D, pos.getZ() + 1.0D);
        List<Entity> entities = level.getEntities(null, box, e -> true);
        for (Entity entity : entities) {
            if (entity.isSpectator()) {
                continue;
            }
            // Kimlik karti konveyorde bekler, tasinmaz
            if (entity instanceof ItemEntity && IdCardItem.isCard(((ItemEntity) entity).getItem())) {
                continue;
            }
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(
                    motion.x * 0.6D + facing.getStepX() * SPEED,
                    motion.y,
                    motion.z * 0.6D + facing.getStepZ() * SPEED);
            entity.hurtMarked = true; // hiz degisimini istemcilere gonder
        }
    }

    // ------------------------------------------------------- kartin ele alinmasi
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hitResult) {
        ItemEntity card = IdMachineStructure.findCard(level, pos);
        if (card == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack stack = card.getItem();
            if (player.getInventory().add(stack)) {
                card.discard();
                player.displayClientMessage(
                        Component.literal("Kimlik kartini ald\u0131n: " + IdCardItem.playerName(stack)), true);
            } else {
                player.displayClientMessage(Component.literal("Envanterin dolu!"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
