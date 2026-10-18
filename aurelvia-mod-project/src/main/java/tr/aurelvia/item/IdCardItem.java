package tr.aurelvia.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

/**
 * Kimlik kartı item'ı - oyuncu bilgisi taşır
 */
public class IdCardItem extends Item {
    public IdCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        String playerName = getPlayerName(stack);
        if (playerName != null) {
            tooltipComponents.add(Component.literal("Player: " + playerName).withStyle(ChatFormatting.WHITE));
            tooltipComponents.add(Component.literal("UNION of YELLOW BANANA").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        } else {
            tooltipComponents.add(Component.literal("No player assigned").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        String playerName = getPlayerName(stack);
        if (playerName != null) {
            return Component.translatable("item.aurelvia.id_card.named", playerName);
        }
        return super.getName(stack);
    }

    /**
     * Kimlik kartına oyuncu adını kaydet
     */
    public static void setPlayerName(ItemStack stack, String playerName) {
        stack.set(AurelviaDataComponents.PLAYER_NAME, playerName);
    }

    /**
     * Kimlik kartından oyuncu adını al
     */
    public static String getPlayerName(ItemStack stack) {
        return stack.get(AurelviaDataComponents.PLAYER_NAME);
    }
}
