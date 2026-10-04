package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import org.lwjgl.glfw.GLFW;

final class AuctionUi {
    static final int TEXT = 0xFFEAF8FF;
    static final int MUTED = 0xFF91A8B7;
    static final int ACCENT = 0xFF48D6FF;
    static final int POSITIVE = 0xFF71E6A4;
    static final int NEGATIVE = 0xFFFF8C8C;
    static final int ROW = 0x66121820;
    static final int ROW_HOVER = 0xA01B2B37;
    static final int SELECTED = 0x6636BFE8;

    static final Identifier MAIN = texture("auction_house_main_gui.png");
    static final Identifier CREATE = texture("auction_house_create_gui.png");
    static final Identifier BIDDING = texture("auction_house_bidding_gui.png");
    static final Identifier INBOX = texture("auction_house_inbox_gui.png");
    static final Identifier MY_AUCTIONS = texture("auction_house_my_auctions_gui.png");

    static final Identifier CREATE_BUTTON = texture("create_auction_buttons.png");
    static final Identifier MAIL_BUTTON = texture("mail_buttons.png");
    static final Identifier MY_AUCTIONS_BUTTON = texture("my_auctions_buttons.png");
    static final Identifier MINUS_BUTTON = texture("negative_button.png");
    static final Identifier PLUS_BUTTON = texture("plus_button.png");

    private AuctionUi() {
    }

    static ITextureData body(Identifier texture) {
        return sprite(texture, 256, 260, 0, 40, 234, 140);
    }

    static ITextureData header(Identifier texture) {
        return sprite(texture, 256, 260, 76, 0, 120, 40);
    }

    static ITextureData auctionRow() {
        return sprite(MY_AUCTIONS, 256, 260, 1, 187, 216, 20);
    }

    static ITextureData inboxRow() {
        return sprite(INBOX, 256, 260, 1, 187, 216, 20);
    }

    static ITextureData inboxClaimButton() {
        return sprite(INBOX, 256, 260, 63, 212, 88, 10);
    }

    static ITextureData backHover() {
        return sprite(CREATE, 256, 260, 3, 196, 18, 10);
    }

    static ITextureData sprite(Identifier texture, int textureWidth, int textureHeight,
                               int x, int y, int width, int height) {
        return new TextureDataSubsection(texture, textureWidth, textureHeight, x, y, width, height);
    }

    static void centered(GuiGraphicsExtractor graphics, Component text, int x, int y, int color) {
        graphics.centeredText(Minecraft.getInstance().font, text, x, y, color);
    }

    static void centeredString(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        graphics.centeredText(Minecraft.getInstance().font, Component.literal(text), x, y, color);
    }

    static void centeredScaled(GuiGraphicsExtractor graphics, Component text, float centerX, float topY, float scale, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, topY);
        graphics.pose().scale(scale, scale);
        graphics.centeredText(Minecraft.getInstance().font, text, 0, 0, color);
        graphics.pose().popMatrix();
    }

    static void centeredScaled(GuiGraphicsExtractor graphics, String text, float centerX, float topY, float scale, int color) {
        centeredScaled(graphics, Component.literal(text), centerX, topY, scale, color);
    }

    static void centeredFitted(
            GuiGraphicsExtractor graphics,
            Component text,
            float centerX,
            float topY,
            float maxWidth,
            float maxScale,
            int color
    ) {
        Font font = Minecraft.getInstance().font;
        int width = Math.max(1, font.width(text));
        float scale = Math.min(maxScale, maxWidth / width);
        centeredScaled(graphics, text, centerX, topY, scale, color);
    }

    static void centeredFitted(GuiGraphicsExtractor graphics, String text, float centerX, float topY, float maxWidth, float maxScale, int color) {
        centeredFitted(graphics, Component.literal(text), centerX, topY, maxWidth, maxScale, color);
    }

    static void stringScaled(GuiGraphicsExtractor graphics, String text,
                             float x, float y, float scale, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.text(Minecraft.getInstance().font, text, 0, 0, color, false);
        graphics.pose().popMatrix();
    }

    static void item(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;
        graphics.item(stack, x, y);
        if (stack.getCount() > 1) {
            Font font = Minecraft.getInstance().font;
            String count = Integer.toString(stack.getCount());
            graphics.text(font, count, x + Math.max(1, 17 - font.width(count)), y + 9, TEXT, true);
        }
    }

    static void itemScaled(GuiGraphicsExtractor graphics, ItemStack stack, float centerX, float centerY, float scale) {
        if (stack.isEmpty()) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX - 8.0F * scale, centerY - 8.0F * scale);
        graphics.pose().scale(scale, scale);
        graphics.item(stack, 0, 0);
        graphics.pose().popMatrix();
    }

    static ItemStack inventoryStack(int slot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || slot < 0 || slot >= minecraft.player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return minecraft.player.getInventory().getItem(slot);
    }

    static int modifierInt(int modifiers, int normal, int shift, int control) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) return control;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) return shift;
        return normal;
    }

    static long modifierLong(int modifiers, long normal, long shift, long control) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) return control;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) return shift;
        return normal;
    }

    static long durationStep(int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) return 10L * 60L * 60L * 1000L;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) return 30L * 60L * 1000L;
        return 60L * 60L * 1000L;
    }

    static long safeAdd(long a, long b) {
        if (b > 0L && a > Long.MAX_VALUE - b) return Long.MAX_VALUE;
        return a + b;
    }

    static String formatDuration(long millis) {
        long totalMinutes = Math.max(0L, millis / 60_000L);
        long days = totalMinutes / (24L * 60L);
        long hours = (totalMinutes / 60L) % 24L;
        long minutes = totalMinutes % 60L;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    static String shortString(String text, int maxCharacters) {
        if (text == null) return "";
        if (text.length() <= maxCharacters) return text;
        if (maxCharacters <= 1) return text.substring(0, Math.max(0, maxCharacters));
        return text.substring(0, maxCharacters - 1) + "…";
    }

    static String stackTitle(ItemStack stack, int count, int maxCharacters) {
        String suffix = count > 1 ? " x" + count : "";
        String name = stack.getHoverName().getString();
        int maxName = Math.max(1, maxCharacters - suffix.length());
        if (name.length() > maxName) {
            name = maxName <= 1 ? name.substring(0, maxName) : name.substring(0, maxName - 1) + "…";
        }
        return name + suffix;
    }

    static String stackTitle(ItemStack stack, int maxCharacters) {
        return stackTitle(stack, stack.getCount(), maxCharacters);
    }

    private static Identifier texture(String file) {
        return Identifier.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "textures/gui/main/auction_house/" + file
        );
    }
}
