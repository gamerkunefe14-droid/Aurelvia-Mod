package tr.aurelvia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Kimlik kartinin cizimi.
 *
 * Kart icerigi:
 *   - oyuncunun ONDEN gorunumu (skin)
 *   - oyuncunun adi
 *   - altta "UNION of YELLOW BANANA"
 */
public final class IdCardRenderer {

    public static final int CARD_W = 200;
    public static final int CARD_H = 128;

    private static final int GOLD = 0xFFFFD400;
    private static final int DARK = 0xF2121220;
    private static final int PANEL = 0xFF232341;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFB4B4CC;

    public static void drawCard(GuiGraphics g, Font font, int x, int y, String playerName, String uuid) {
        // cerceve + govde
        g.fill(x, y, x + CARD_W, y + CARD_H, GOLD);
        g.fill(x + 3, y + 3, x + CARD_W - 3, y + CARD_H - 3, DARK);
        g.fill(x + 3, y + 3, x + CARD_W - 3, y + 27, PANEL);

        // baslik
        g.drawString(font, Component.translatable("gui.aurelvia_machines.card_title"),
                x + 10, y + 9, GRAY, false);

        // oyuncunun onden gorunumu
        drawPlayerFront(g, x + 16, y + 34, 2.0F, uuid);

        // oyuncu adi
        String name = playerName == null ? "" : playerName;
        g.drawString(font, name, x + 62, y + 44, WHITE, true);
        g.drawString(font, Component.translatable("gui.aurelvia_machines.card_holder"),
                x + 62, y + 60, GRAY, false);

        // alt serit: UNION of YELLOW BANANA
        g.fill(x + 3, y + CARD_H - 21, x + CARD_W - 3, y + CARD_H - 3, GOLD);
        Component org = Component.translatable("gui.aurelvia_machines.card_org");
        int textWidth = font.width(org);
        g.drawString(font, org, x + (CARD_W - textWidth) / 2, y + CARD_H - 16, 0xFF14141F, false);
    }

    /**
     * Oyuncunun skin dokusundan 16x32'lik on gorunumu cizer.
     * (px, py) = sol ust kose, scale = olcek.
     */
    public static void drawPlayerFront(GuiGraphics g, int px, int py, float scale, String uuid) {
        ResourceLocation skin = SkinHelper.skinFor(uuid);
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(px, py, 0.0F);
        pose.scale(scale, scale, 1.0F);

        // govde parcalari (birinci katman)
        blit(g, skin, 4, 0, 8, 8, 8, 8);      // kafa
        blit(g, skin, 4, 8, 20, 20, 8, 12);   // govde
        blit(g, skin, 12, 8, 44, 20, 4, 12);  // sag kol
        blit(g, skin, 0, 8, 36, 52, 4, 12);   // sol kol
        blit(g, skin, 8, 20, 4, 20, 4, 12);   // sag bacak
        blit(g, skin, 4, 20, 20, 52, 4, 12);  // sol bacak

        // ikinci katman (sapka/ceket)
        blit(g, skin, 4, 0, 40, 8, 8, 8);     // kafa ust katman
        blit(g, skin, 4, 8, 20, 36, 8, 12);   // govde ust katman

        pose.popPose();
    }

    private static void blit(GuiGraphics g, ResourceLocation texture, int x, int y, int u, int v, int w, int h) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, w, h, 64, 64);
    }

    private IdCardRenderer() {
    }
}
