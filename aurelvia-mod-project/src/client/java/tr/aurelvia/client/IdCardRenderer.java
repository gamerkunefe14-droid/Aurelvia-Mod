package tr.aurelvia.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * Kimlik kartini GUI uzerine cizer:
 *   [ ON GORUNUS CILDI ]
 *   OYUNCU ADI
 *   UNION of YELLOW BANANA
 */
public final class IdCardRenderer {

    /** Kartin uzerindeki sabit yazi (U noktalidir). */
    public static final String UNION_TEXT = "UN\u0130ON of YELLOW BANANA";

    private static final int CARD_W = 96;
    private static final int CARD_H = 132;
    /** Cildin kac kat buyutulecegi. */
    private static final float BODY_SCALE = 3.0F;

    private IdCardRenderer() {
    }

    public static int cardWidth() {
        return CARD_W;
    }

    public static int cardHeight() {
        return CARD_H;
    }

    public static void draw(GuiGraphics g, Font font, int x, int y, String playerName, PlayerSkin skin) {
        // --- kart govdesi
        g.fill(x - 2, y - 2, x + CARD_W + 2, y + CARD_H + 2, 0xFF111318);
        g.fill(x, y, x + CARD_W, y + CARD_H, 0xFFF7E9A0);
        g.fill(x, y, x + CARD_W, y + 15, 0xFF2E7D32);
        g.fill(x, y + CARD_H - 26, x + CARD_W, y + CARD_H, 0xFF2E7D32);

        g.drawCenteredString(font, Component.literal("AURELVIA"), x + CARD_W / 2, y + 4, 0xFFFFFFFF);

        // --- oyuncunun on gorunusu (cildden kirpilmis)
        Identifier texture = SkinHelper.textureOf(skin);
        int px = x + 18;
        int py = y + 21;
        org.joml.Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.translate(px, py);
        pose.scale(BODY_SCALE, BODY_SCALE);
        drawPlayerBody(g, texture, 0, 0);
        pose.popMatrix();

        // --- ad + birlik yazisi
        g.drawCenteredString(font, Component.literal(safe(playerName)), x + CARD_W / 2, y + 100, 0xFF222222);
        g.drawCenteredString(font, Component.literal(UNION_TEXT), x + CARD_W / 2, y + CARD_H - 20, 0xFFFFFF00);
    }

    /** 64x64 oyuncu cildinden ON GORUNUSU cizer (temel katman + ust katman). */
    private static void drawPlayerBody(GuiGraphics g, Identifier texture, int ox, int oy) {
        // kafa
        part(g, texture, ox + 4, oy + 0, 8, 8, 8, 8);
        part(g, texture, ox + 4, oy + 0, 8, 8, 40, 8);
        // govde
        part(g, texture, ox + 4, oy + 8, 8, 12, 20, 20);
        part(g, texture, ox + 4, oy + 8, 8, 12, 20, 36);
        // sol kol (ekranda sol)
        part(g, texture, ox + 0, oy + 8, 4, 12, 44, 20);
        part(g, texture, ox + 0, oy + 8, 4, 12, 44, 36);
        // sag kol
        part(g, texture, ox + 12, oy + 8, 4, 12, 36, 52);
        part(g, texture, ox + 12, oy + 8, 4, 12, 52, 52);
        // sol bacak
        part(g, texture, ox + 4, oy + 20, 4, 12, 4, 20);
        part(g, texture, ox + 4, oy + 20, 4, 12, 4, 36);
        // sag bacak
        part(g, texture, ox + 8, oy + 20, 4, 12, 20, 52);
        part(g, texture, ox + 8, oy + 20, 4, 12, 4, 52);
    }

    private static void part(GuiGraphics g, Identifier texture, int x, int y, int w, int h, int u, int v) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, w, h, 64, 64);
    }

    private static String safe(String s) {
        return s == null ? "?" : s;
    }
}
