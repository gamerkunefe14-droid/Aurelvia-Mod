package tr.aurelvia.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Kimlik Karti.
 * Eldeyken sag tik: karti buyuk goruntule (cilt + ad + UNION of YELLOW BANANA).
 *
 * NOT: Bu sinif common source set'tedir, bu yuzden client siniflarina DOGRUDAN
 * dokunamaz. Buyuk ekrani acma isi {@link #VIEWER} kancasi uzerinden client modu
 * tarafindan baglanir.
 */
public class IdCardItem extends Item {

    /** Kartin uzerindeki sabit yazi. (U noktalidir) */
    public static final String UNION_TEXT = "UN\u0130ON of YELLOW BANANA";

    /** Client modu bu kancayi doldurur; sunucuda null kalir. */
    public static CardViewer VIEWER = null;

    /** Karti buyuk goruntuleyen client tarafi arabirimi. */
    public interface CardViewer {
        void open(String playerName, String playerUuid);
    }

    public IdCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide() && VIEWER != null) {
            ItemStack stack = player.getItemInHand(usedHand);
            String name = playerName(stack);
            if (name != null && !name.isEmpty()) {
                VIEWER.open(name, playerUuid(stack));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> adder, TooltipFlag flag) {
        String name = playerName(stack);
        adder.accept(Component.literal("Sahibi: " + (name == null ? "-" : name)));
        adder.accept(Component.literal(UNION_TEXT));
    }

    public static String playerName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.get(AurelviaDataComponents.CARD_PLAYER_NAME);
    }

    public static String playerUuid(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.get(AurelviaDataComponents.CARD_PLAYER_UUID);
    }

    /** Bu yigin dolu (sahibi olan) bir kimlik karti mi? */
    public static boolean isCard(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IdCardItem)) {
            return false;
        }
        String name = playerName(stack);
        return name != null && !name.isEmpty();
    }

    /** Verilen oyuncu icin bir kimlik karti yigini uretir. */
    public static ItemStack create(String playerName, String playerUuid) {
        ItemStack stack = new ItemStack(tr.aurelvia.AurelviaMachinesMod.ID_CARD_ITEM, 1);
        stack.set(AurelviaDataComponents.CARD_PLAYER_NAME, playerName);
        stack.set(AurelviaDataComponents.CARD_PLAYER_UUID, playerUuid);
        return stack;
    }
}
