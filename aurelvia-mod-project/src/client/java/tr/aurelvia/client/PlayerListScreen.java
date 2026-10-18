package tr.aurelvia.client;

import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import tr.aurelvia.networking.ModNetworking;

/**
 * Dogru sifreden sonra sunucudaki GERCEK oyunculari listeler.
 * Bir satira tiklaninca sunucuya "secildi" paketi gider.
 */
public class PlayerListScreen extends Screen {

    private static final int PANEL_W = 240;
    private static final int ROWS = 8;
    private static final int ROW_H = 22;

    private final BlockPos pos;
    private final List<String[]> players;

    public PlayerListScreen(BlockPos pos, List<String[]> players) {
        super(Component.literal("Kimlik Bas\u0131lacak Oyuncu"));
        this.pos = pos;
        this.players = players;
    }

    @Override
    protected void init() {
        int x0 = (this.width - PANEL_W) / 2;
        int y0 = (this.height - (ROWS * ROW_H + 78)) / 2;

        int shown = 0;
        for (String[] entry : this.players) {
            if (shown >= ROWS) {
                break;
            }
            final String name = entry[0];
            final String uuid = entry[1];
            final int index = shown;
            this.addRenderableWidget(
                    Button.builder(Component.literal(name), b -> this.select(name, uuid))
                            .bounds(x0 + 14, y0 + 34 + index * ROW_H, PANEL_W - 28, 20)
                            .build());
            shown++;
        }

        this.addRenderableWidget(
                Button.builder(Component.literal("Kapat"), b -> this.onClose())
                        .bounds(x0 + 14, y0 + 34 + ROWS * ROW_H + 6, PANEL_W - 28, 20)
                        .build());
    }

    private void select(String name, String uuid) {
        ClientPlayNetworking.send(new ModNetworking.SelectPlayerPayload(this.pos, name + "|" + uuid));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x0 = (this.width - PANEL_W) / 2;
        int y0 = (this.height - (ROWS * ROW_H + 78)) / 2;
        int h = ROWS * ROW_H + 78;

        guiGraphics.fill(x0, y0, x0 + PANEL_W, y0 + h, 0xEE14181F);
        guiGraphics.fill(x0, y0, x0 + PANEL_W, y0 + 2, 0xFFFFC94D);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, y0 + 14, 0xFFFFC94D);

        if (this.players.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "Sunucuda baska oyuncu yok.",
                    this.width / 2, y0 + 60, 0xFFB9C2CF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
