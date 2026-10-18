package tr.aurelvia.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import tr.aurelvia.networking.ModNetworking;

/**
 * Bilgisayar sifre ekrani. HER ACILISTA gosterilir.
 */
public class PasswordScreen extends Screen {

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 118;

    private final BlockPos pos;
    private EditBox passwordBox;

    public PasswordScreen(BlockPos pos) {
        super(Component.literal("Aurelvia Bilgisayar\u0131"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        int x0 = (this.width - PANEL_W) / 2;
        int y0 = (this.height - PANEL_H) / 2;

        this.passwordBox = new EditBox(this.font, x0 + 20, y0 + 46, PANEL_W - 40, 20,
                Component.literal("Sifre"));
        this.passwordBox.setMaxLength(128);
        this.addRenderableWidget(this.passwordBox);

        this.addRenderableWidget(Button.builder(Component.literal("Giri\u015f"), b -> this.submit())
                .bounds(x0 + 20, y0 + 74, (PANEL_W - 50) / 2, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u0130ptal"), b -> this.onClose())
                .bounds(x0 + 25 + (PANEL_W - 50) / 2, y0 + 74, (PANEL_W - 50) / 2, 20).build());

        this.setInitialFocus(this.passwordBox);
    }

    private void submit() {
        String value = this.passwordBox.getValue();
        if (value == null) {
            value = "";
        }
        ClientPlayNetworking.send(new ModNetworking.PasswordPayload(this.pos, value.trim()));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x0 = (this.width - PANEL_W) / 2;
        int y0 = (this.height - PANEL_H) / 2;

        guiGraphics.fill(x0, y0, x0 + PANEL_W, y0 + PANEL_H, 0xEE14181F);
        guiGraphics.fill(x0, y0, x0 + PANEL_W, y0 + 2, 0xFFFFC94D);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, y0 + 14, 0xFFFFC94D);
        guiGraphics.drawString(this.font, "Yetkili \u015fifresini gir:", x0 + 20, y0 + 34, 0xFFB9C2CF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
