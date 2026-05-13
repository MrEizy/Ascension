package net.thejadeproject.ascension.refactor_packages.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.techniques.MergeResponsePayload;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.List;
import java.util.Set;

public class TechniqueMergeScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/merge/merge_background.png");
    private static final ResourceLocation BTN_ATTEMPT =
        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/merge/button_attempt.png");
    private static final ResourceLocation BTN_ATTEMPT_HOVERED =
        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/merge/button_attempt_hovered.png");
    private static final ResourceLocation BTN_DECLINE =
        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/merge/button_decline.png");
    private static final ResourceLocation BTN_DECLINE_HOVERED =
        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/merge/button_decline_hovered.png");

    private static final int BG_WIDTH = 200;
    private static final int BG_HEIGHT = 120;
    private static final int BTN_WIDTH = 80;
    private static final int BTN_HEIGHT = 20;

    private final List<Set<ResourceLocation>> eligibleMerges;
    private int selectedIndex = 0;

    public TechniqueMergeScreen(List<Set<ResourceLocation>> eligibleMerges) {
        super(Component.literal("Technique Resonance"));
        this.eligibleMerges = eligibleMerges;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private int bgX() { return (width - BG_WIDTH) / 2; }
    private int bgY() { return (height - BG_HEIGHT) / 2; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = bgX();
        int y = bgY();

        graphics.blit(BACKGROUND, x, y, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);

        graphics.drawCenteredString(font, "Technique Resonance", x + BG_WIDTH / 2, y + 8, 0xFFD700);

        Set<ResourceLocation> selected = eligibleMerges.get(selectedIndex);
        int nameY = y + 30;
        for (ResourceLocation techId : selected) {
            Component name = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(techId).getDisplayTitle();
            graphics.drawCenteredString(font, name, x + BG_WIDTH / 2, nameY, 0xFFFFFF);
            nameY += 12;
        }

        int btnY = y + BG_HEIGHT - 30;

        int attemptX = x + 15;
        boolean attemptHovered = mouseX >= attemptX && mouseX <= attemptX + BTN_WIDTH
            && mouseY >= btnY && mouseY <= btnY + BTN_HEIGHT;
        graphics.blit(attemptHovered ? BTN_ATTEMPT_HOVERED : BTN_ATTEMPT,
            attemptX, btnY, 0, 0, BTN_WIDTH, BTN_HEIGHT, BTN_WIDTH, BTN_HEIGHT);
        graphics.drawCenteredString(font, "Attempt Merge", attemptX + BTN_WIDTH / 2, btnY + 6, 0xFFFFFF);

        int declineX = x + BG_WIDTH - BTN_WIDTH - 15;
        boolean declineHovered = mouseX >= declineX && mouseX <= declineX + BTN_WIDTH
            && mouseY >= btnY && mouseY <= btnY + BTN_HEIGHT;
        graphics.blit(declineHovered ? BTN_DECLINE_HOVERED : BTN_DECLINE,
            declineX, btnY, 0, 0, BTN_WIDTH, BTN_HEIGHT, BTN_WIDTH, BTN_HEIGHT);
        graphics.drawCenteredString(font, "Not Now", declineX + BTN_WIDTH / 2, btnY + 6, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = bgX();
        int y = bgY();
        int btnY = y + BG_HEIGHT - 30;

        int attemptX = x + 15;
        if (mouseX >= attemptX && mouseX <= attemptX + BTN_WIDTH && mouseY >= btnY && mouseY <= btnY + BTN_HEIGHT) {
            PacketDistributor.sendToServer(new MergeResponsePayload(eligibleMerges.get(selectedIndex)));
            onClose();
            return true;
        }

        int declineX = x + BG_WIDTH - BTN_WIDTH - 15;
        if (mouseX >= declineX && mouseX <= declineX + BTN_WIDTH && mouseY >= btnY && mouseY <= btnY + BTN_HEIGHT) {
            onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
