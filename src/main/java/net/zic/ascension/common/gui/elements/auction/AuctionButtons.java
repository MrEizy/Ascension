package net.zic.ascension.common.gui.elements.auction;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zic.ascension.common.auction.AuctionViewData;
import net.zic.ascension.common.gui.elements.general.AscensionTooltip;
import net.zic.ascension.common.gui.elements.general.BetterButton;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

final class AuctionButtons {
    enum OwnerTab {
        CREATE,
        AUCTIONS,
        INBOX,
        NONE
    }

    enum ListingStyle {
        COMPACT,
        STANDARD,
        CURRENT_BID,
        INBOX
    }

    private AuctionButtons() {
    }

    static final class NavButton extends BetterButton {
        private final ITextureData normal;
        private final ITextureData active;
        private final boolean selected;
        private final Runnable action;
        private final AscensionTooltip tooltip;
        private final Component tooltipText;

        NavButton(UIFrame frame, Identifier texture, boolean selected, Component tooltipText, Runnable action) {
            super(frame, 0, 0);
            this.normal = AuctionUi.sprite(texture, 24, 48, 0, 0, 24, 24);
            this.active = AuctionUi.sprite(texture, 24, 48, 0, 24, 24, 24);
            this.selected = selected;
            this.action = action;
            this.tooltipText = tooltipText;
            this.tooltip = new AscensionTooltip(frame);
            this.tooltip.setActive(true);
            setWidth(24);
            setHeight(24);
        }

        void place(int x, int y) {
            getPositioning().setX(x);
            getPositioning().setY(y);
        }

        @Override
        public void onClick() {
            action.run();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (selected) {
                active.renderAt(graphics, 0, -3);
            } else if (isHovered() || isPressed()) {
                active.render(graphics);
            } else {
                normal.render(graphics);
            }
            if (isHovered()) {
                tooltip.setText(tooltipText);
                getUiFrame().setTooltip(tooltip);
            }
        }
    }

    static final class BackButton extends BetterButton {
        private final ITextureData hover = AuctionUi.backHover();
        private final Runnable action;
        private final AscensionTooltip tooltip;

        BackButton(UIFrame frame, Runnable action) {
            super(frame, 0, 0);
            this.action = action;
            this.tooltip = new AscensionTooltip(frame);
            this.tooltip.setActive(true);
            setWidth(18);
            setHeight(10);
        }

        void place(int x, int y) {
            getPositioning().setX(x);
            getPositioning().setY(y);
        }

        @Override
        public void onClick() {
            action.run();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (isHovered() || isPressed()) {
                hover.render(graphics);
            }
            if (isHovered()) {
                tooltip.setText(Component.literal("Back"));
                getUiFrame().setTooltip(tooltip);
            }
        }
    }

    static final class HotspotButton extends BetterButton {
        private final Runnable action;
        private final AscensionTooltip tooltip;
        private final Component tooltipText;

        HotspotButton(UIFrame frame, Component tooltipText, Runnable action) {
            super(frame, 0, 0);
            this.action = action;
            this.tooltipText = tooltipText;
            this.tooltip = tooltipText == null ? null : new AscensionTooltip(frame);
            if (this.tooltip != null) this.tooltip.setActive(true);
        }

        void place(int x, int y, int width, int height) {
            getPositioning().setX(x);
            getPositioning().setY(y);
            setWidth(width);
            setHeight(height);
        }

        @Override
        public void onClick() {
            action.run();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (isHovered() && tooltip != null) {
                tooltip.setText(tooltipText);
                getUiFrame().setTooltip(tooltip);
            }
        }
    }

    static final class ModifierButton extends BetterButton {
        private final ITextureData normal;
        private final ITextureData hover;
        private final IntConsumer action;
        private final AscensionTooltip tooltip;
        private final Component tooltipText;

        ModifierButton(UIFrame frame, Identifier texture, Component tooltipText, IntConsumer action) {
            super(frame, 0, 0);
            this.normal = AuctionUi.sprite(texture, 12, 24, 0, 0, 12, 12);
            this.hover = AuctionUi.sprite(texture, 12, 24, 0, 12, 12, 12);
            this.action = action;
            this.tooltipText = tooltipText;
            this.tooltip = new AscensionTooltip(frame);
            this.tooltip.setActive(true);
            setWidth(12);
            setHeight(12);
        }

        void place(int x, int y) {
            getPositioning().setX(x);
            getPositioning().setY(y);
        }

        @Override
        public void onMouseUp(EasyEvent event) {
            if (!(event instanceof EasyMouseEvent mouseEvent)) return;
            MouseButtonEvent buttonEvent = mouseEvent.getMouseEvent();
            if (buttonEvent == null || buttonEvent.button() != InputConstants.MOUSE_BUTTON_LEFT) return;

            boolean shouldClick = isPressed() && isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY());
            setPressed(false);
            if (shouldClick) action.accept(buttonEvent.modifiers());
        }

        @Override
        public void onClick() {
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (isHovered() || isPressed()) hover.render(graphics); else normal.render(graphics);
            if (isHovered()) {
                tooltip.setText(tooltipText);
                getUiFrame().setTooltip(tooltip);
            }
        }
    }

    static final class ItemSlotButton extends BetterButton {
        private final Supplier<ItemStack> stack;
        private final BooleanSupplier selected;
        private final Runnable action;
        private final AscensionTooltip tooltip;

        ItemSlotButton(UIFrame frame, Supplier<ItemStack> stack, BooleanSupplier selected, Runnable action) {
            super(frame, 0, 0);
            this.stack = stack;
            this.selected = selected;
            this.action = action;
            this.tooltip = new AscensionTooltip(frame);
            this.tooltip.setActive(true);
            setWidth(17);
            setHeight(17);
        }

        void place(int x, int y) {
            getPositioning().setX(x);
            getPositioning().setY(y);
        }

        @Override
        public void onClick() {
            action.run();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (selected.getAsBoolean()) {
                graphics.fill(0, 0, 17, 17, AuctionUi.SELECTED);
            } else if (isHovered()) {
                graphics.fill(0, 0, 17, 17, 0x2227C9F4);
            }

            ItemStack current = stack.get();
            AuctionUi.item(graphics, current, 0, 0);
            if (isHovered() && !current.isEmpty()) {
                tooltip.setText(current.getHoverName());
                getUiFrame().setTooltip(tooltip);
            }
        }
    }

    static final class ListingButton extends BetterButton {
        private final AuctionViewData.AuctionView auction;
        private final ListingStyle style;
        private final Runnable action;
        private final AscensionTooltip tooltip;
        private final ITextureData rowTexture = AuctionUi.auctionRow();

        ListingButton(UIFrame frame, AuctionViewData.AuctionView auction, ListingStyle style, Runnable action) {
            super(frame, 0, 0);
            this.auction = auction;
            this.style = style;
            this.action = action;
            this.tooltip = new AscensionTooltip(frame);
            this.tooltip.setActive(true);
        }

        void place(int x, int y, int width, int height) {
            getPositioning().setX(x);
            getPositioning().setY(y);
            setWidth(width);
            setHeight(height);
        }

        void size(int width, int height) {
            setWidth(width);
            setHeight(height);
        }

        @Override
        public void onClick() {
            if (action != null) action.run();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (style == ListingStyle.COMPACT) {
                graphics.fill(0, 0, getWidth(), getHeight(), action != null && (isHovered() || isPressed())
                        ? AuctionUi.ROW_HOVER : AuctionUi.ROW);
                AuctionUi.item(graphics, auction.item(), 4, Math.max(1, (getHeight() - 16) / 2));
                AuctionUi.stringScaled(graphics, AuctionUi.stackTitle(auction.item(), 24), 24, 2, 0.76F, AuctionUi.TEXT);
                long shown = auction.currentBid() > 0L ? auction.currentBid() : auction.startingBid();
                AuctionUi.stringScaled(graphics,
                        shown + " stones • " + AuctionUi.formatDuration(Math.max(0L,
                                auction.endsAtMillis() - System.currentTimeMillis())),
                        24, 11, 0.64F, AuctionUi.MUTED);
            } else {
                rowTexture.render(graphics);
                if (isHovered() || isPressed()) {
                    graphics.fill(3, 3, getWidth() - 3, getHeight() - 3, 0x1427C9F4);
                }
                AuctionUi.item(graphics, auction.item(), 7, 2);

                if (style == ListingStyle.CURRENT_BID) {
                    AuctionUi.stringScaled(graphics, AuctionUi.stackTitle(auction.item(), 24), 27, 2, 0.70F, AuctionUi.TEXT);
                    String state = auction.viewerIsWinning() ? "Winning" : "Outbid";
                    int stateColor = auction.viewerIsWinning() ? AuctionUi.POSITIVE : AuctionUi.NEGATIVE;
                    AuctionUi.stringScaled(graphics,
                            auction.viewerEscrow() + " / " + Math.max(auction.startingBid(), auction.currentBid()) + " • " + state,
                            27, 11, 0.58F, stateColor);
                } else {
                    AuctionUi.stringScaled(graphics, AuctionUi.stackTitle(auction.item(), 25), 27, 2, 0.70F, AuctionUi.TEXT);
                    long bid = auction.currentBid() > 0L ? auction.currentBid() : auction.startingBid();
                    String bidder = auction.highestBidderName().isBlank() ? "No bids" : AuctionUi.shortString(auction.highestBidderName(), 10);
                    AuctionUi.stringScaled(graphics,
                            bid + " stones • " + bidder + " • " + AuctionUi.formatDuration(Math.max(0L,
                                    auction.endsAtMillis() - System.currentTimeMillis())),
                            27, 11, 0.56F, AuctionUi.MUTED);
                }
            }

            if (isHovered()) {
                tooltip.setText(auction.item().getHoverName());
                getUiFrame().setTooltip(tooltip);
            }
        }
    }

    static final class HouseButton extends BetterButton {
        private final AuctionViewData.HouseView house;
        private final Runnable action;
        private final ITextureData rowTexture = AuctionUi.auctionRow();

        HouseButton(UIFrame frame, AuctionViewData.HouseView house, Runnable action) {
            super(frame, 0, 0);
            this.house = house;
            this.action = action;
        }

        void place(int x, int y, int width, int height) {
            getPositioning().setX(x);
            getPositioning().setY(y);
            setWidth(width);
            setHeight(height);
        }

        @Override
        public void onClick() {
            action.run();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            rowTexture.render(graphics);
            if (isHovered() || isPressed()) {
                graphics.fill(3, 3, getWidth() - 3, getHeight() - 3, 0x1427C9F4);
            }
            AuctionUi.item(graphics, new ItemStack(Items.PLAYER_HEAD), 7, 2);
            AuctionUi.stringScaled(graphics, AuctionUi.shortString(house.ownerName(), 24), 27, 2, 0.70F, AuctionUi.TEXT);
            AuctionUi.stringScaled(graphics, house.auctions().size() + " active auctions", 27, 11, 0.58F, AuctionUi.MUTED);
        }
    }

    static final class InboxEntryButton extends BetterButton {
        private final AuctionViewData.InboxEntryView entry;
        private final AscensionTooltip tooltip;
        private final ITextureData rowTexture = AuctionUi.inboxRow();

        InboxEntryButton(UIFrame frame, AuctionViewData.InboxEntryView entry) {
            super(frame, 0, 0);
            this.entry = entry;
            this.tooltip = new AscensionTooltip(frame);
            this.tooltip.setActive(true);
        }

        void place(int x, int y, int width, int height) {
            getPositioning().setX(x);
            getPositioning().setY(y);
            setWidth(width);
            setHeight(height);
        }

        @Override
        public void onClick() {
            // The row is informational. Each row has its own dedicated ClaimButton.
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            rowTexture.render(graphics);
            AuctionUi.item(graphics, entry.item(), 7, 2);
            AuctionUi.stringScaled(graphics, AuctionUi.stackTitle(entry.item(), 22), 27, 6, 0.68F, AuctionUi.TEXT);

            if (isHovered()) {
                tooltip.setText(entry.item().getHoverName());
                getUiFrame().setTooltip(tooltip);
            }
        }
    }

    static final class CurrencyInboxButton extends BetterButton {
        private final long amount;
        private final ITextureData rowTexture = AuctionUi.inboxRow();

        CurrencyInboxButton(UIFrame frame, long amount) {
            super(frame, 0, 0);
            this.amount = amount;
        }

        void place(int x, int y, int width, int height) {
            getPositioning().setX(x);
            getPositioning().setY(y);
            setWidth(width);
            setHeight(height);
        }

        @Override
        public void onClick() {
            // The row is informational. Each row has its own dedicated ClaimButton.
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            rowTexture.render(graphics);
            AuctionUi.stringScaled(graphics, "Spiritual Stones x" + amount, 13, 6, 0.68F, AuctionUi.ACCENT);
        }
    }

    static final class InboxClaimButton extends BetterButton {
        private final Runnable action;
        private final AscensionTooltip tooltip;

        InboxClaimButton(UIFrame frame, Runnable action) {
            super(frame, 0, 0);
            this.action = action;
            this.tooltip = new AscensionTooltip(frame);
            this.tooltip.setActive(true);
            setWidth(42);
            setHeight(12);
        }

        void place(int x, int y) {
            getPositioning().setX(x);
            getPositioning().setY(y);
        }

        @Override
        public void onClick() {
            action.run();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (isHovered() || isPressed()) {
                graphics.fill(0, 0, getWidth(), getHeight(), 0x1827C9F4);
            }
            AuctionUi.centeredScaled(graphics, "Claim", getWidth() / 2.0F, 3.0F, 0.62F, AuctionUi.TEXT);

            if (isHovered()) {
                tooltip.setText(Component.literal("Claim"));
                getUiFrame().setTooltip(tooltip);
            }
        }
    }

    static final class TooltipHotspot extends BetterButton {
        private final AscensionTooltip tooltip;
        private final Supplier<Component> text;

        TooltipHotspot(UIFrame frame, Supplier<Component> text) {
            super(frame, 0, 0);
            this.text = text;
            this.tooltip = new AscensionTooltip(frame);
            this.tooltip.setActive(true);
        }

        void place(int x, int y, int width, int height) {
            getPositioning().setX(x);
            getPositioning().setY(y);
            setWidth(width);
            setHeight(height);
        }

        @Override
        public void onClick() {
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (isHovered()) {
                tooltip.setText(text.get());
                getUiFrame().setTooltip(tooltip);
            }
        }
    }
}
