package tr.aurelvia.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * Kimlik karti item'i.
 *
 * Kart uzerinde:
 *   - oyuncunun onden gorunumu (skin)
 *   - oyuncunun adi
 *   - "UNION of YELLOW BANANA" yazisi
 * bulunur. Gorsel, kart ele alinip sag tiklandiginda tam ekran gosterilir.
 */
public class IdCardItem extends Item {

    /**
     * Client tarafindan (AurelviaMachinesClient) doldurulur.
     * Dedicated server'da null kalir.
     */
    public static Consumer<ItemStack> CLIENT_VIEWER;

    public IdCardItem(Properties properties) {
        super(properties);
    }

    // --------------------------------------------------------------- veri erisimi

    public static void setProfile(ItemStack stack, String name, String uuid) {
        stack.set(AurelviaDataComponents.CARD_PLAYER_NAME, name == null ? "" : name);
        stack.set(AurelviaDataComponents.CARD_PLAYER_UUID, uuid == null ? "" : uuid);
    }

    public static String getOwnerName(ItemStack stack) {
        return stack.get(AurelviaDataComponents.CARD_PLAYER_NAME);
    }

    public static String getOwnerUuid(ItemStack stack) {
        return stack.get(AurelviaDataComponents.CARD_PLAYER_UUID);
    }

    // ------------------------------------------------------------------ gorunum

    @Override
    public Component getName(ItemStack stack) {
        String owner = getOwnerName(stack);
        if (owner != null && !owner.isEmpty()) {
            return Component.translatable("item.aurelvia_machines.id_card.named", owner);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        String owner = getOwnerName(stack);
        if (owner != null && !owner.isEmpty()) {
            tooltipComponents.add(Component.translatable("gui.aurelvia_machines.card_org")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltipComponents.add(Component.translatable("gui.aurelvia_machines.card_hint")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        String owner = getOwnerName(stack);
        if (owner != null && !owner.isEmpty() && CLIENT_VIEWER != null) {
            CLIENT_VIEWER.accept(stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }
}
