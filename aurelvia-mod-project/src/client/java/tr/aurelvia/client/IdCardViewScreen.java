package tr.aurelvia.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * Kimlik kartini buyuk gosteren ekran.
 */
public class IdCardViewScreen extends Screen {

    private final String playerName;
    private final String playerUuid;
    private PlayerSkin skin;

    public IdCardViewScreen(String playerName, String playerUuid) {
        super(Component.literal("Kimlik Kart\u0131"));
        this.playerName = playerName;
        this.playerUuid = playerUuid;
    }

    @Override
    protected void init() {
        this.skin = SkinHelper.skinFor(this.playerUuid);
        this.addRenderableWidget(Button.builder(Component.literal("Kapat"), b -> this.onClose())
                .bounds((this.width - 100) / 2, this.height / 2 + 86, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x = (this.width - IdCardRenderer.cardWidth()) / 2;
        int y = (this.height - IdCardRenderer.cardHeight()) / 2 - 20;
        IdCardRenderer.draw(guiGraphics, this.font, x, y, this.playerName, this.skin);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, y - 16, 0xFFFFC94D);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
