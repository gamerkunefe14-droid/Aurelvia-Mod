package tr.aurelvia.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.block.IdMachineBlock;
import tr.aurelvia.item.IdCardItem;

/**
 * Kimlik makinesi block entity - kimlik kartı üretimi yönetir
 */
public class IdMachineBlockEntity extends BlockEntity {
    private String pendingPlayerName = null;
    private int produceTimer = 0;
    private static final int PRODUCE_TIME = 40; // 2 saniye (20 tick/saniye)

    public IdMachineBlockEntity(BlockPos pos, BlockState state) {
        super(AurelviaMachinesMod.ID_MACHINE_BLOCK_ENTITY, pos, state);
    }

    /**
     * Kimlik kartı üretimi başlat
     */
    public void startProducing(String playerName) {
        this.pendingPlayerName = playerName;
        this.produceTimer = PRODUCE_TIME;
        setChanged();
    }

    /**
     * Her tick'te çağrılır
     */
    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (pendingPlayerName != null && produceTimer > 0) {
            produceTimer--;
            
            if (produceTimer <= 0) {
                // Kimlik kartını üret
                produceIdCard();
                pendingPlayerName = null;
            }
        }
    }

    /**
     * Kimlik kartını konveyörden çıkar
     */
    private void produceIdCard() {
        if (level == null) return;

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof IdMachineBlock)) return;

        Direction facing = state.getValue(IdMachineBlock.FACING);
        BlockPos conveyorPos = getBlockPos().relative(facing.getClockWise());

        // Kimlik kartı item'ı oluştur
        ItemStack cardStack = new ItemStack(AurelviaMachinesMod.ID_CARD_ITEM);
        IdCardItem.setPlayerName(cardStack, pendingPlayerName);

        // Konveyörün üstüne item entity olarak bırak
        Vec3 spawnPos = Vec3.atCenterOf(conveyorPos).add(0, 0.5, 0);
        ItemEntity itemEntity = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, cardStack);
        itemEntity.setDeltaMovement(Vec3.ZERO);
        itemEntity.setPickUpDelay(10); // 0.5 saniye alma gecikmesi
        
        level.addFreshEntity(itemEntity);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (pendingPlayerName != null) {
            tag.putString("PendingPlayer", pendingPlayerName);
        }
        tag.putInt("ProduceTimer", produceTimer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("PendingPlayer")) {
            pendingPlayerName = tag.getString("PendingPlayer");
        }
        produceTimer = tag.getInt("ProduceTimer");
    }
}
