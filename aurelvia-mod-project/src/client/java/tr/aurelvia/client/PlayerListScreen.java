package tr.aurelvia.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import tr.aurelvia.networking.ModNetworking;

import java.util.ArrayList;
import java.util.List;

/**
 * Oyuncu listesi ekrani.
 * Listeden bir oyuncuya tiklaninca secim sunucuya gonderilir ve
 * makine kimlik kartini uretmeye baslar.
 */
public class PlayerListScreen extends Screen {

    private static final int ROW_H = 24;
    private static final int VISIBLE = 8;
    private static final int LIST_W = 230;

    private final BlockPos machinePos;
    private final List<String> names;
    private final List<String> uuids;

    private int scroll = 0;
    private int listX;
    private int listY;

    public PlayerListScreen(BlockPos machinePos, List<String> names, List<String> uuids) {
        super(Component.translatable("gui.aurelvia_machines.select_player_title"));
        this.machinePos = machinePos;
        this.names = names == null ? new ArrayList<>() : names;
        this.uuids = uuids == null ? new ArrayList<>() : uuids;
    }

    @Override
    protected void init() {
        this.listX = this.width / 2 - LIST_W / 2;
        this.listY = this.height / 2 - (VISIBLE * ROW_H) / 2 + 12;

        int buttonX = this.listX + LIST_W + 6;

        this.addRenderableWidget(Button.builder(Component.literal("\u25B2"), b -> this.scrollBy(-1))
                .bounds(buttonX, this.listY, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u25BC"), b -> this.scrollBy(1))
                .bounds(buttonX, this.listY + VISIBLE * ROW_H - 20, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.aurelvia_machines.cancel"),
                        b -> this.onClose())
                .bounds(this.listX, this.listY + VISIBLE * ROW_H + 10, LIST_W, 20).build());
    }

    private void scrollBy(int amount) {
        int max = Math.max(0, this.names.size() - VISIBLE);
        this.scroll = Math.max(0, Math.min(max, this.scroll + amount));
    }

    private void select(int index) {
        if (index < 0 || index >= this.names.size()) {
            return;
        }
        String name = this.names.get(index);
        String uuid = index < this.uuids.size() ? this.uuids.get(index) : "";
        ClientPlayNetworking.send(new ModNetworking.SelectPlayerPayload(this.machinePos, name, uuid));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int cx = this.width / 2;

        // baslik
        guiGraphics.drawCenteredString(this.font, this.title, cx, this.listY - 26, 0xFFFFD400);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.aurelvia_machines.player_count", this.names.size()),
                cx, this.listY - 14, 0xFFB4B4CC);

        // liste cercevesi
        guiGraphics.fill(this.listX - 1, this.listY - 1,
                this.listX + LIST_W + 1, this.listY + VISIBLE * ROW_H + 1, 0xFFFFD400);
        guiGraphics.fill(this.listX, this.listY,
                this.listX + LIST_W, this.listY + VISIBLE * ROW_H, 0xEE14141F);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // satirlar
        int end = Math.min(this.scroll + VISIBLE, this.names.size());
        for (int i = this.scroll; i < end; i++) {
            int slot = i - this.scroll;
            int rowY = this.listY + slot * ROW_H;

            boolean hovered = mouseX >= this.listX && mouseX < this.listX + LIST_W
                    && mouseY >= rowY && mouseY < rowY + ROW_H;

            guiGraphics.fill(this.listX, rowY, this.listX + LIST_W, rowY + ROW_H - 1,
                    hovered ? 0xFF2E2E55 : (i % 2 == 0 ? 0xFF1B1B30 : 0xFF202038));

            // oyuncu kafasi
            String uuid = i < this.uuids.size() ? this.uuids.get(i) : "";
            IdCardRenderer.drawPlayerFront(guiGraphics, this.listX + 6, rowY + 3, 0.55F, uuid);

            guiGraphics.drawString(this.font, this.names.get(i),
                    this.listX + 26, rowY + 8, hovered ? 0xFFFFFF00 : 0xFFFFFFFF, false);
        }

        if (this.names.isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.aurelvia_machines.no_players"),
                    cx, this.listY + 30, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.names.size() > 0) {
            boolean insideX = mouseX >= this.listX && mouseX < this.listX + LIST_W;
            boolean insideY = mouseY >= this.listY && mouseY < this.listY + VISIBLE * ROW_H;
            if (insideX && insideY) {
                int slot = (int) ((mouseY - this.listY) / ROW_H);
                int index = this.scroll + slot;
                if (index >= 0 && index < this.names.size()) {
                    this.select(index);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
