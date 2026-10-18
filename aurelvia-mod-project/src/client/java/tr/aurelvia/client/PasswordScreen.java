package tr.aurelvia.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import tr.aurelvia.networking.ModNetworking;

/**
 * Bilgisayara sag tiklaninca acilan sifre ekrani.
 * Her acilista sifre istenir (sunucu tarafinda dogrulanir).
 */
public class PasswordScreen extends Screen {

    public static final int PANEL_W = 260;
    public static final int PANEL_H = 130;

    private final BlockPos machinePos;
    private EditBox passwordField;

    public PasswordScreen(BlockPos machinePos) {
        super(Component.translatable("gui.aurelvia_machines.password_title"));
        this.machinePos = machinePos;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int top = cy - PANEL_H / 2;

        this.passwordField = new EditBox(this.font, cx - 110, top + 52, 220, 20,
                Component.translatable("gui.aurelvia_machines.password_label"));
        this.passwordField.setMaxLength(128);
        this.addRenderableWidget(this.passwordField);

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.aurelvia_machines.login"), b -> this.submit())
                .bounds(cx - 110, top + 86, 105, 20).build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.aurelvia_machines.cancel"), b -> this.onClose())
                .bounds(cx + 5, top + 86, 105, 20).build());

        this.setInitialFocus(this.passwordField);
    }

    private void submit() {
        if (this.passwordField == null) {
            return;
        }
        String password = this.passwordField.getValue();
        ClientPlayNetworking.send(new ModNetworking.SubmitPasswordPayload(this.machinePos, password));
        this.passwordField.setValue("");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int top = cy - PANEL_H / 2;
        int left = cx - PANEL_W / 2;

        // panel
        guiGraphics.fill(left - 1, top - 1, left + PANEL_W + 1, top + PANEL_H + 1, 0xFFFFD400);
        guiGraphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xEE14141F);
        guiGraphics.fill(left, top, left + PANEL_W, top + 26, 0xFF22223A);

        guiGraphics.drawCenteredString(this.font, this.title, cx, top + 8, 0xFFFFD400);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.aurelvia_machines.password_label"), cx, top + 34, 0xFFB9B9CE);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335) && this.passwordField != null && this.passwordField.isFocused()) {
            this.submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
