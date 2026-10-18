package tr.aurelvia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tr.aurelvia.AurelviaMachinesMod;

/**
 * Kimlik makinesinin 3 parcali yapisini yoneten yardimci sinif.
 *
 *  [bilgisayar]
 *  [makine][konveyor]
 *
 * makine  = asil blok (block entity burada)
 * konveyor= makinenin saginda (facing'e gore)
 * bilgisayar = makinenin tam ustunde
 */
public final class IdMachineStructure {

    /** Ic ice yok etme (recursion) korumasi. */
    public static boolean destroying = false;

    public static BlockPos conveyorPos(BlockPos machinePos, Direction facing) {
        return machinePos.relative(facing.getClockWise());
    }

    public static BlockPos computerPos(BlockPos machinePos) {
        return machinePos.above();
    }

    public static boolean isMachine(BlockState state) {
        return state.is(AurelviaMachinesMod.ID_MACHINE_BLOCK);
    }

    /**
     * Verilen parca bloktan (bilgisayar / konveyor / makine) makinenin
     * ana konumunu bulur. Makine yoksa null doner.
     */
    public static BlockPos findMachine(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (isMachine(state)) {
            return pos;
        }

        // Bilgisayar: altindaki blok makine mi?
        if (state.is(AurelviaMachinesMod.COMPUTER_BLOCK)) {
            BlockPos below = pos.below();
            if (isMachine(level.getBlockState(below))) {
                return below;
            }
        }

        // Konveyor: yanindaki makinenin saginda mi?
        if (state.is(AurelviaMachinesMod.CONVEYOR_BLOCK)) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = pos.relative(dir);
                BlockState candidateState = level.getBlockState(candidate);
                if (isMachine(candidateState)
                        && conveyorPos(candidate, candidateState.getValue(IdMachineBlock.FACING)).equals(pos)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    /**
     * Makinenin konveyor + bilgisayar parcalarini drop birakmadan kaldirir.
     */
    public static void removeParts(Level level, BlockPos machinePos, BlockState machineState) {
        if (destroying || level.isClientSide()) {
            return;
        }
        destroying = true;
        try {
            Direction facing = machineState.getValue(IdMachineBlock.FACING);
            BlockPos conveyor = conveyorPos(machinePos, facing);
            BlockPos computer = computerPos(machinePos);

            if (level.getBlockState(conveyor).is(AurelviaMachinesMod.CONVEYOR_BLOCK)) {
                level.removeBlock(conveyor, false);
            }
            if (level.getBlockState(computer).is(AurelviaMachinesMod.COMPUTER_BLOCK)) {
                level.removeBlock(computer, false);
            }
        } finally {
            destroying = false;
        }
    }

    /**
     * Bir parca (bilgisayar/konveyor) kirildiginda tum makineyi bozar.
     */
    public static void onPartBroken(Level level, BlockPos partPos) {
        if (destroying || level.isClientSide()) {
            return;
        }
        BlockPos machinePos = findMachine(level, partPos);
        if (machinePos == null) {
            return;
        }
        destroying = true;
        try {
            BlockState machineState = level.getBlockState(machinePos);
            if (isMachine(machineState)) {
                // Tum yapiyi kaldir; makine bloku kendi item'ini dusurur.
                Direction facing = machineState.getValue(IdMachineBlock.FACING);
                BlockPos conveyor = conveyorPos(machinePos, facing);
                BlockPos computer = computerPos(machinePos);
                if (!partPos.equals(conveyor) && level.getBlockState(conveyor).is(AurelviaMachinesMod.CONVEYOR_BLOCK)) {
                    level.removeBlock(conveyor, false);
                }
                if (!partPos.equals(computer) && level.getBlockState(computer).is(AurelviaMachinesMod.COMPUTER_BLOCK)) {
                    level.removeBlock(computer, false);
                }
                Block.dropResources(machineState, level, machinePos);
                level.removeBlock(machinePos, false);
            }
        } finally {
            destroying = false;
        }
    }

    /** Makinenin kurulumu icin gereken 3 yerin bos olup olmadigi. */
    public static boolean hasSpaceFor(Level level, BlockPos machinePos, Direction facing,
                                      net.minecraft.world.item.context.BlockPlaceContext context) {
        return canReplace(level, conveyorPos(machinePos, facing), context)
                && canReplace(level, computerPos(machinePos), context);
    }

    private static boolean canReplace(Level level, BlockPos pos,
                                     net.minecraft.world.item.context.BlockPlaceContext context) {
        return level.getBlockState(pos).canBeReplaced(context);
    }

    public static boolean is(Block block, BlockState state) {
        return state.is(block);
    }

    private IdMachineStructure() {
    }
}
