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
import tr.aurelvia.networking.ModNetworking;

/**
 * Bilgisayar.
 *
 *  - TEK BASINA KULLANILAMAZ: sag tiklaninca "bu bilgisayar tek basina calismiyor" der.
 *  - Kimlik makinesinin ustune konuldugunda (attached) sag tiklaninca SUNUCUDAN
 *    sifre ekrani istenir -> her seferinde sifre sorulur.
 */
public class ComputerBlock extends Block {

    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);

    public ComputerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(IdMachineStructure.ATTACHED, Boolean.FALSE));
    }

    @Override
    public MapCodec<ComputerBlock> codec() {
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

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hitResult) {
        boolean attached = state.getValue(IdMachineStructure.ATTACHED).booleanValue();

        if (!attached) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.literal("Bu bilgisayar tek ba\u015f\u0131na \u00e7al\u0131\u015fm\u0131yor. "
                                + "Bir kimlik makinesinin \u00fcst\u00fcne koy."), true);
            }
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer) {
            // Sifre ekrani HER ACILISTA sorulur: client'a "sifre sor" paketi gonder
            ServerPlayNetworking.send((ServerPlayer) player,
                    new ModNetworking.OpenMachinePayload(pos.below()));
        }
        return InteractionResult.SUCCESS;
    }
}
