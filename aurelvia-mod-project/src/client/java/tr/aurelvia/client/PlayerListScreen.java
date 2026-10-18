package tr.aurelvia.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import tr.aurelvia.networking.ModNetworking;

import java.util.List;

/**
 * Oyuncu listesi ekranı - listeden oyuncu seçimi
 */
public class PlayerListScreen extends Screen {
    private final BlockPos machinePos;
    private final List<String> playerNames;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_SPACING = 4;
    private static final int MAX_VISIBLE = 8;
    private int scrollOffset = 0;
    
    public PlayerListScreen(BlockPos pos, List<String> playerNames) {
        super(Component.translatable("gui.aurelvia.select_player"));
        this.machinePos = pos;
        this.playerNames = playerNames;
    }
    
    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }
    
    private void rebuildButtons() {
        this.clearWidgets();
        
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;
        
        int endIndex = Math.min(scrollOffset + MAX_VISIBLE, playerNames.size());
        
        for (int i = scrollOffset; i < endIndex; i++) {
            String playerName = playerNames.get(i);
            int index = i;
            
            Button button = Button.builder(Component.literal(playerName), btn -> {
                ClientPlayNetworking.send(new ModNetworking.PlayerSelectPayload(machinePos, playerName));
                this.onClose();
            }).bounds(centerX - BUTTON_WIDTH / 2, startY + (i - scrollOffset) * (BUTTON_HEIGHT + BUTTON_SPACING), BUTTON_WIDTH, BUTTON_HEIGHT).build();
            
            this.addRenderableWidget(button);
        }
        
        // Scroll up button
        if (scrollOffset > 0) {
            Button upButton = Button.builder(Component.literal("▲"), btn -> {
                scrollOffset--;
                rebuildButtons();
            }).bounds(centerX + BUTTON_WIDTH / 2 + 5, startY, 20, 20).build();
            this.addRenderableWidget(upButton);
        }
        
        // Scroll down button
        if (scrollOffset + MAX_VISIBLE < playerNames.size()) {
            Button downButton = Button.builder(Component.literal("▼"), btn -> {
                scrollOffset++;
                rebuildButtons();
            }).bounds(centerX + BUTTON_WIDTH / 2 + 5, startY + (MAX_VISIBLE - 1) * (BUTTON_HEIGHT + BUTTON_SPACING), 20, 20).build();
            this.addRenderableWidget(downButton);
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Başlık
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.aurelvia.select_player"), centerX, centerY - 80, 16777215);
        
        // Oyuncu sayısı
        guiGraphics.drawCenteredString(this.font, 
            Component.literal(playerNames.size() + " oyuncu"), 
            centerX, centerY - 68, 10526880);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
