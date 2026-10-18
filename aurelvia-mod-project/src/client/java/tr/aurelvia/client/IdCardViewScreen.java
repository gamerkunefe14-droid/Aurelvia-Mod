package tr.aurelvia.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Kart ele alinip sag tiklandiginda acilan buyuk kimlik karti gorunumu.
 */
public class IdCardViewScreen extends Screen {

    private final String playerName;
    private final String uuid;

    public IdCardViewScreen(String playerName, String uuid) {
        super(Component.translatable("gui.aurelvia_machines.card_title"));
        this.playerName = playerName;
        this.uuid = uuid;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int cardX = this.width / 2 - IdCardRenderer.CARD_W / 2;
        int cardY = this.height / 2 - IdCardRenderer.CARD_H / 2;

        IdCardRenderer.drawCard(guiGraphics, this.font, cardX, cardY, this.playerName, this.uuid);

        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.aurelvia_machines.card_close_hint"),
                this.width / 2, cardY + IdCardRenderer.CARD_H + 10, 0xFF888899);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
