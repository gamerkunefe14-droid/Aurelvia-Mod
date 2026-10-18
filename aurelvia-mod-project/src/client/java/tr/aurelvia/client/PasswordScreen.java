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
 * Şifre giriş ekranı
 */
public class PasswordScreen extends Screen {
    private final BlockPos machinePos;
    private EditBox passwordBox;
    private Button submitButton;
    
    public PasswordScreen(BlockPos pos) {
        super(Component.translatable("gui.aurelvia.password_title"));
        this.machinePos = pos;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Şifre giriş kutusu
        passwordBox = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.literal(""));
        passwordBox.setMaxLength(100);
        passwordBox.setBordered(true);
        passwordBox.setVisible(true);
        passwordBox.setTextColor(16777215);
        this.addRenderableWidget(passwordBox);
        
        // Gönder butonu
        submitButton = Button.builder(Component.translatable("gui.aurelvia.submit"), button -> {
            String password = passwordBox.getValue();
            ClientPlayNetworking.send(new ModNetworking.PasswordPayload(machinePos, password));
        }).bounds(centerX - 50, centerY + 10, 100, 20).build();
        this.addRenderableWidget(submitButton);
        
        // Enter tuşu ile de gönderebilsin
        this.setInitialFocus(passwordBox);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Başlık
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.aurelvia.enter_password"), centerX, centerY - 40, 16777215);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && passwordBox.isFocused()) { // Enter tuşu
            submitButton.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
