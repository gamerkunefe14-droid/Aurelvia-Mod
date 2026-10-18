package tr.aurelvia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tr.aurelvia.item.IdCardItem;

import java.util.List;

/**
 * Kimlik makinesinin 3 parcasini bir arada tutan yardimci.
 *
 *   [bilgisayar]   = machinePos.above()
 *   [id_machine]   = machinePos            (sag tik -> sifre)
 *   [konveyor]     = machinePos.relative(yon)   (kart burada belirir)
 */
public final class IdMachineStructure {

    /** Bu blok bir kimlik makinesinin parcasi mi? */
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");

    /** Kartin konveyor uzerinde duracagi yukseklik. */
    private static final double CARD_Y = 1.06D;

    private IdMachineStructure() {
    }

    public static Direction facing(Level level, BlockPos machinePos) {
        return level.getBlockState(machinePos).getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public static BlockPos conveyorPos(Level level, BlockPos machinePos) {
        return machinePos.relative(facing(level, machinePos));
    }

    public static BlockPos computerPos(BlockPos machinePos) {
        return machinePos.above();
    }

    /** Kimlik kartini konveyorun ustune, havada duran bir esya olarak birakir. */
    public static void spawnCard(Level level, BlockPos conveyorPos, String playerName, String playerUuid) {
        // Ayni yerde eski bir kart varsa once onu kaldir
        ItemEntity old = findCard(level, conveyorPos);
        if (old != null) {
            old.discard();
        }

        ItemStack stack = IdCardItem.create(playerName, playerUuid);
        ItemEntity card = new ItemEntity(level,
                conveyorPos.getX() + 0.5D,
                conveyorPos.getY() + CARD_Y,
                conveyorPos.getZ() + 0.5D,
                stack);
        card.setNoGravity(true);
        card.setUnlimitedLifetime();
        card.setPickUpDelay(Short.MAX_VALUE); // oyuncu ustunden gecince kendiliginden toplanmasin
        card.setDeltaMovement(0.0D, 0.0D, 0.0D);
        level.addFreshEntity(card);
    }

    /** Konveyorun ustunde bekleyen kimlik kartini bulur (yoksa null). */
    public static ItemEntity findCard(Level level, BlockPos conveyorPos) {
        AABB box = new AABB(
                conveyorPos.getX(), conveyorPos.getY() + 0.5D, conveyorPos.getZ(),
                conveyorPos.getX() + 1.0D, conveyorPos.getY() + 2.5D, conveyorPos.getZ() + 1.0D);
        List<net.minecraft.world.entity.Entity> found = level.getEntities(null, box, e -> true);
        for (net.minecraft.world.entity.Entity e : found) {
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) e;
                if (IdCardItem.isCard(item.getItem())) {
                    return item;
                }
            }
        }
        return null;
    }

    /** Kartin dunya icindeki merkez konumu (goruntulemek icin). */
    public static Vec3 cardWorldPos(BlockPos conveyorPos) {
        return new Vec3(
                conveyorPos.getX() + 0.5D,
                conveyorPos.getY() + CARD_Y,
                conveyorPos.getZ() + 0.5D);
    }
}
