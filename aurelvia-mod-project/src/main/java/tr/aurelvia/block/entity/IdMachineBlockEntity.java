package tr.aurelvia.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tr.aurelvia.AurelviaMachinesMod;
import tr.aurelvia.block.ConveyorBlock;
import tr.aurelvia.block.IdMachineBlock;
import tr.aurelvia.block.IdMachineStructure;
import tr.aurelvia.item.IdCardItem;

import java.util.UUID;

/**
 * Kimlik makinesinin beyni:
 *  - secilen oyuncu icin kart uretir
 *  - karti konveyorun uzerinde sergiler
 *  - sag tiklaninca karti oyuncunun eline verir
 */
public class IdMachineBlockEntity extends BlockEntity {

    /** Kartin hazirlanma suresi (tick). 40 tick = 2 saniye. */
    public static final int PRODUCE_TIME = 40;

    // uretim kuyrugu
    private String pendingName = "";
    private String pendingUuid = "";
    private int produceTimer = 0;

    // hazir kart
    private String cardName = "";
    private String cardUuid = "";

    // konveyorun uzerinde sergilenen gorsel esya
    private UUID displayEntityId;
    private int maintenanceCounter = 0;

    public IdMachineBlockEntity(BlockPos pos, BlockState state) {
        super(AurelviaMachinesMod.ID_MACHINE_BLOCK_ENTITY, pos, state);
    }

    // ------------------------------------------------------------- uretim

    public void startProducing(String name, String uuid) {
        this.pendingName = name == null ? "" : name;
        this.pendingUuid = uuid == null ? "" : uuid;
        this.produceTimer = PRODUCE_TIME;
        setChanged();
    }

    public boolean hasCard() {
        return !cardName.isEmpty();
    }

    public boolean isProducing() {
        return produceTimer > 0;
    }

    /** Sunucu tarafinda her tick cagrilir. */
    public void serverTick() {
        if (produceTimer > 0) {
            produceTimer--;
            if (produceTimer <= 0) {
                produceTimer = 0;
                cardName = pendingName;
                cardUuid = pendingUuid;
                pendingName = "";
                pendingUuid = "";
                setChanged();
                spawnDisplayEntity();
                notifyPlayers();
            } else if (produceTimer % 5 == 0) {
                setChanged();
            }
        }

        // Sergilenen karti bakimli tut (kaybolursa yeniden olustur, alinamaz kalsin)
        if (hasCard()) {
            if (++maintenanceCounter >= 10) {
                maintenanceCounter = 0;
                maintainDisplayEntity();
            }
        }
    }

    private void notifyPlayers() {
        if (level == null || level.isClientSide()) {
            return;
        }
        for (Player player : level.players()) {
            if (player.blockPosition().closerThan(getBlockPos(), 24.0D)) {
                player.sendSystemMessage(net.minecraft.network.chat.Component
                        .translatable("gui.aurelvia_machines.card_ready", cardName));
            }
        }
    }

    // ------------------------------------------------------------- kart

    public ItemStack createCard() {
        ItemStack stack = new ItemStack(AurelviaMachinesMod.ID_CARD_ITEM);
        IdCardItem.setProfile(stack, cardName, cardUuid);
        return stack;
    }

    public boolean takeCard(Player player) {
        if (!hasCard()) {
            return false;
        }
        ItemStack stack = createCard();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        clearCard();
        return true;
    }

    private void clearCard() {
        cardName = "";
        cardUuid = "";
        removeDisplayEntity();
        setChanged();
    }

    /** Makine kirildiginda icindeki karti yere dusurur. */
    public void dropPendingCard(Level dropLevel, BlockPos pos) {
        if (dropLevel.isClientSide()) {
            return;
        }
        if (hasCard()) {
            ItemEntity.dropItemStack(dropLevel, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, createCard());
            clearCard();
        }
        if (!pendingName.isEmpty()) {
            pendingName = "";
            pendingUuid = "";
            produceTimer = 0;
            setChanged();
        }
    }

    // -------------------------------------------------- sergilenen esya

    private BlockPos conveyorPos() {
        BlockState state = getBlockState();
        Direction facing = state.hasProperty(IdMachineBlock.FACING)
                ? state.getValue(IdMachineBlock.FACING)
                : Direction.NORTH;
        return IdMachineStructure.conveyorPos(getBlockPos(), facing);
    }

    private void spawnDisplayEntity() {
        if (level == null || level.isClientSide() || !hasCard()) {
            return;
        }
        removeDisplayEntity();

        BlockPos conveyor = conveyorPos();
        Vec3 center = Vec3.atCenterOf(conveyor).add(0.0D, 0.35D, 0.0D);
        ItemEntity entity = new ItemEntity(level, center.x(), center.y(), center.z(), createCard());
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setPickUpDelay(32767);
        entity.setUnlimitedLifetime();
        level.addFreshEntity(entity);
        displayEntityId = entity.getUUID();
        setChanged();
    }

    private void maintainDisplayEntity() {
        if (level == null || level.isClientSide() || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        if (displayEntityId == null) {
            spawnDisplayEntity();
            return;
        }
        if (serverLevel.getEntity(displayEntityId) instanceof ItemEntity entity && entity.isAlive()) {
            entity.setPickUpDelay(32767);
            entity.setNoGravity(true);
            entity.setDeltaMovement(Vec3.ZERO);
        } else {
            displayEntityId = null;
            spawnDisplayEntity();
        }
    }

    private void removeDisplayEntity() {
        if (level == null || level.isClientSide() || displayEntityId == null) {
            return;
        }
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (serverLevel.getEntity(displayEntityId) instanceof ItemEntity entity) {
                entity.discard();
            }
        }
        displayEntityId = null;
    }

    // ------------------------------------------------------------- NBT

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("PendingName", pendingName);
        tag.putString("PendingUuid", pendingUuid);
        tag.putInt("ProduceTimer", produceTimer);
        tag.putString("CardName", cardName);
        tag.putString("CardUuid", cardUuid);
        if (displayEntityId != null) {
            tag.putUUID("DisplayEntity", displayEntityId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pendingName = tag.getString("PendingName");
        pendingUuid = tag.getString("PendingUuid");
        produceTimer = tag.getInt("ProduceTimer");
        cardName = tag.getString("CardName");
        cardUuid = tag.getString("CardUuid");
        displayEntityId = tag.hasUUID("DisplayEntity") ? tag.getUUID("DisplayEntity") : null;
    }

    /** Konveyorun uzerindeki karti dogrudan almak icin (blok disindan). */
    public static boolean tryTakeFromConveyor(Level level, BlockPos machinePos, Player player) {
        if (level.getBlockEntity(machinePos) instanceof IdMachineBlockEntity machine) {
            return machine.takeCard(player);
        }
        return false;
    }

    /** Konveyor blogunun makineye bagli olup olmadigini kontrol eder. */
    public static boolean isAttachedConveyor(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.hasProperty(ConveyorBlock.ATTACHED) && Boolean.TRUE.equals(state.getValue(ConveyorBlock.ATTACHED));
    }
}
