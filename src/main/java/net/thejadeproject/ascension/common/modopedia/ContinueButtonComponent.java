package net.thejadeproject.ascension.common.modopedia;

import net.favouriteless.modopedia.api.Lookup;
import net.favouriteless.modopedia.api.book.Book;
import net.favouriteless.modopedia.api.book.page_components.BookRenderContext;
import net.favouriteless.modopedia.api.book.page_components.PageComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.network.serverBound.UnlockChapterPayload;

public class ContinueButtonComponent extends PageComponent {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "continue_button");

    private static final int BTN_W = 60;
    private static final int BTN_H = 11;

    private String entryUnlock;

    @Override
    public void init(Book book, Lookup lookup, Level level) {
        super.init(book, lookup, level);
        entryUnlock = lookup.get("entry_id").asString();
    }

    @Override
    public void render(GuiGraphics graphics, BookRenderContext context, int mouseX, int mouseY, float partialTick) {
        boolean hovered = context.isHovered(mouseX, mouseY, x, y, BTN_W, BTN_H);
        int bg = hovered ? 0xFFAD8B2A : 0xFF6B5010;
        int border = 0xFF8B6914;

        graphics.fill(x, y, x + BTN_W, y + BTN_H, bg);
        graphics.fill(x, y, x + BTN_W, y + 1, border);
        graphics.fill(x, y + BTN_H - 1, x + BTN_W, y + BTN_H, border);
        graphics.fill(x, y, x + 1, y + BTN_H, border);
        graphics.fill(x + BTN_W - 1, y, x + BTN_W, y + BTN_H, border);

        graphics.drawString(Minecraft.getInstance().font, "[ Continue → ]", x + 3, y + 2, hovered ? 0xFFFFEE88 : 0xFFCCA840, false);
    }

    @Override
    public boolean mouseClicked(BookRenderContext context, double mouseX, double mouseY, int button) {
        if (button == 0 && context.isHovered(mouseX, mouseY, x, y, BTN_W, BTN_H)) {
            PacketDistributor.sendToServer(new UnlockChapterPayload(entryUnlock));
            return true;
        }
        return false;
    }
}
