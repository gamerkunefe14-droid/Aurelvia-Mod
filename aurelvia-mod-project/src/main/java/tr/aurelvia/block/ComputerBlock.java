package tr.aurelvia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;

/**
 * Bilgisayar bloğu - tek başına kullanılamaz, sadece kimlik makinesinin parçası
 */
public class ComputerBlock extends Block {
    public ComputerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            // Tek başına kullanılamaz
            player.sendSystemMessage(Component.translatable("block.aurelvia.computer.cannot_use_alone"));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
