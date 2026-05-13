package net.thejadeproject.ascension.clients.toast;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;

import static net.thejadeproject.ascension.clients.toast.AscensionToastInterface.DEFAULT_BACKGROUND;

public class AscensionToast implements Toast {



    private static final long DISPLAY_TIME = 5000L;

    private static final int WIDTH = 190;
    private static final int HEIGHT = 32;

    private static final int TEXT_X = 30;

    private static final float TITLE_SCALE = 0.90F;
    private static final float MESSAGE_SCALE = 0.80F;

    private final Component title;
    private final Component message;
    private final ItemStack icon;
    private final ResourceLocation background;

    public AscensionToast(Component title, Component message, ItemStack icon) {
        this(title, message, icon, DEFAULT_BACKGROUND);
    }

    public AscensionToast(Component title, Component message, ItemStack icon, ResourceLocation background) {
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.background = background;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceVisible) {
        Minecraft minecraft = toastComponent.getMinecraft();
        Font font = minecraft.font;

        guiGraphics.blitSprite(background, 0, 0, width(), height());

        guiGraphics.renderFakeItem(icon, 8, 8);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(TITLE_SCALE, TITLE_SCALE, 1.0F);

        guiGraphics.drawString(
                font,
                title.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                (int) (TEXT_X / TITLE_SCALE),
                (int) (7 / TITLE_SCALE),
                0xFFFFFF,
                false
        );

        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(MESSAGE_SCALE, MESSAGE_SCALE, 1.0F);

        guiGraphics.drawString(
                font,
                message.copy().withStyle(ChatFormatting.YELLOW),
                (int) (TEXT_X / MESSAGE_SCALE),
                (int) (20 / MESSAGE_SCALE),
                0xFFFFFF,
                false
        );

        guiGraphics.pose().popPose();

        double displayTime = DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier();
        return timeSinceVisible >= displayTime ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public int height() {
        return HEIGHT;
    }
}