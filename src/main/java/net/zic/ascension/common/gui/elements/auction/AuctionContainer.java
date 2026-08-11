package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.RenderableElement;
import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.common.auction.AuctionManager;
import net.zic.ascension.common.auction.AuctionViewData;
import net.zic.ascension.common.gui.elements.general.BetterButton;
import net.zic.ascension.network.auction.AuctionScreenPacket;
import net.zic.ascension.network.auction.BidAuctionPacket;
import net.zic.ascension.network.auction.AuctionInboxActionPacket;
import net.zic.ascension.network.auction.CreateAuctionPacket;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

public final class AuctionContainer extends RenderableElement {
    private static final int WIDTH = 360;
    private static final int HEIGHT = 238;
    private static final int BG = 0xEF171A20;
    private static final int PANEL = 0xEE252A33;
    private static final int PANEL_ALT = 0xEE303642;
    private static final int BORDER = 0xFF77808F;
    private static final int BORDER_HOVER = 0xFFE5C36A;
    private static final int TEXT = 0xFFF2F2F2;
    private static final int MUTED = 0xFFB7BDC8;
    private static final int GOLD = 0xFFE5C36A;

    private final AuctionScreenPacket packet;

    public AuctionContainer(UIFrame frame, AuctionScreenPacket packet) {
        super(frame);
        this.packet = packet;
        setWidth(WIDTH);
        setHeight(HEIGHT);
        getPositioning().setPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-WIDTH / 2);
        getPositioning().setY(-HEIGHT / 2);

        switch (packet.mode()) {
            case OWNER -> openPanel(new OwnerPanel(frame));
            case OWNER_INBOX -> openPanel(new InboxPanel(frame, 0));
            case BIDDER -> openBidderInitial(frame);
            case INBOX -> openPanel(new InboxPanel(frame, 0));
            case BIDS -> openPanel(new CurrentBidsPanel(frame, 0));
        }
    }

    private void openBidderInitial(UIFrame frame) {
        UUID focus = packet.focusAuctionId();
        if (focus != null) {
            for (AuctionViewData.HouseView house : packet.houses()) {
                for (AuctionViewData.AuctionView auction : house.auctions()) {
                    if (auction.id().equals(focus)) {
                        openPanel(new AuctionDetailPanel(frame, house, auction));
                        return;
                    }
                }
            }
        }
        openPanel(new BidderBrowserPanel(frame, 0));
    }

    private void openPanel(RenderableElement panel) {
        removeChildren();
        panel.setActive(true);
        addChild(panel);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, WIDTH, HEIGHT, BG);
        border(graphics, 0, 0, WIDTH, HEIGHT, BORDER);
    }

    private final class OwnerPanel extends RenderableElement {
        OwnerPanel(UIFrame frame) {
            super(frame);
            setWidth(WIDTH);
            setHeight(HEIGHT);

            TextButton create = new TextButton(frame, Component.translatable("gui.ascension.auction.create"),
                    () -> openPanel(new CreatePanel(frame)));
            create.place(12, 34, 116, 22);
            addChild(create);

            TextButton inbox = new TextButton(frame,
                    Component.translatable("gui.ascension.auction.open_inbox", packet.inboxCurrency()),
                    () -> ClientPacketDistributor.sendToServer(AuctionInboxActionPacket.open(packet.houseId(), packet.accessPos())));
            inbox.place(232, 34, 116, 22);
            addChild(inbox);
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font,
                    Component.translatable("gui.ascension.auction.owner", packet.ownerName()), WIDTH / 2, 10, TEXT);
            graphics.text(font, Component.literal("Spiritual Stones waiting: " + packet.inboxCurrency()), 12, 64, GOLD, false);
            graphics.text(font, Component.literal("Active auctions: " + packet.auctions().size()), 12, 78, MUTED, false);

            int y = 94;
            if (packet.auctions().isEmpty()) {
                graphics.centeredText(font, Component.translatable("gui.ascension.auction.no_auctions"), WIDTH / 2, 132, MUTED);
                return;
            }
            for (int i = 0; i < Math.min(4, packet.auctions().size()); i++) {
                AuctionViewData.AuctionView auction = packet.auctions().get(i);
                drawListing(graphics, auction, 12, y, 336, 30);
                y += 33;
            }
            if (packet.auctions().size() > 4) {
                graphics.text(font, Component.literal("+ " + (packet.auctions().size() - 4) + " more"), 14, 224, MUTED, false);
            }
        }
    }

    private final class CreatePanel extends RenderableElement {
        private int selectedSlot = -1;
        private int quantity = 1;
        private long startingBid = 1L;
        private long duration = 60L * 60L * 1000L;

        CreatePanel(UIFrame frame) {
            super(frame);
            setWidth(WIDTH);
            setHeight(HEIGHT);

            TextButton back = new TextButton(frame, Component.translatable("gui.ascension.auction.back"),
                    () -> openPanel(new OwnerPanel(frame)));
            back.place(10, 8, 52, 18);
            addChild(back);

            for (int slot = 0; slot < 36; slot++) {
                int finalSlot = slot;
                int row = slot < 9 ? 3 : (slot - 9) / 9;
                int col = slot < 9 ? slot : (slot - 9) % 9;
                int y = slot < 9 ? 190 : 132 + row * 18;
                ItemButton item = new ItemButton(frame, () -> inventoryStack(finalSlot),
                        () -> selectedSlot == finalSlot,
                        () -> {
                            ItemStack stack = inventoryStack(finalSlot);
                            if (!stack.isEmpty()) {
                                selectedSlot = finalSlot;
                                quantity = Math.min(quantity, stack.getCount());
                                if (quantity <= 0) quantity = 1;
                            }
                        });
                item.place(12 + col * 18, y, 17, 17);
                addChild(item);
            }

            ModifierTextButton qtyDown = new ModifierTextButton(frame, Component.literal("-"), modifiers ->
                    adjustQuantity(-modifierInt(modifiers, 1, 8, 64)));
            qtyDown.place(205, 49, 24, 19);
            addChild(qtyDown);
            ModifierTextButton qtyUp = new ModifierTextButton(frame, Component.literal("+"), modifiers ->
                    adjustQuantity(modifierInt(modifiers, 1, 8, 64)));
            qtyUp.place(318, 49, 24, 19);
            addChild(qtyUp);

            ModifierTextButton bidDown = new ModifierTextButton(frame, Component.literal("-"), modifiers ->
                    startingBid = Math.max(1L, startingBid - modifierLong(modifiers, 1L, 10L, 100L)));
            bidDown.place(205, 76, 24, 19);
            addChild(bidDown);
            ModifierTextButton bidUp = new ModifierTextButton(frame, Component.literal("+"), modifiers ->
                    startingBid = safeAdd(startingBid, modifierLong(modifiers, 1L, 10L, 100L)));
            bidUp.place(318, 76, 24, 19);
            addChild(bidUp);

            ModifierTextButton durationDown = new ModifierTextButton(frame, Component.literal("-"), modifiers -> duration = Math.max(
                    AuctionManager.MIN_DURATION_MILLIS,
                    duration - durationStep(modifiers)
            ));
            durationDown.place(205, 103, 24, 19);
            addChild(durationDown);
            ModifierTextButton durationUp = new ModifierTextButton(frame, Component.literal("+"), modifiers -> duration = Math.min(
                    AuctionManager.MAX_DURATION_MILLIS,
                    safeAdd(duration, durationStep(modifiers))
            ));
            durationUp.place(318, 103, 24, 19);
            addChild(durationUp);

            TextButton confirm = new TextButton(frame, Component.translatable("gui.ascension.auction.confirm"), () -> {
                if (selectedSlot < 0 || packet.houseId() == null) {
                    return;
                }
                ClientPacketDistributor.sendToServer(new CreateAuctionPacket(
                        packet.houseId(),
                        packet.accessPos(),
                        selectedSlot,
                        quantity,
                        startingBid,
                        duration
                ));
            });
            confirm.place(205, 132, 137, 24);
            addChild(confirm);
        }

        private void adjustQuantity(int amount) {
            if (selectedSlot < 0) return;
            ItemStack stack = inventoryStack(selectedSlot);
            int max = Math.max(1, stack.getCount());
            quantity = Math.max(1, Math.min(max, quantity + amount));
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, Component.translatable("gui.ascension.auction.create"), WIDTH / 2, 11, TEXT);

            ItemStack selected = selectedSlot < 0 ? ItemStack.EMPTY : inventoryStack(selectedSlot);
            graphics.text(font,
                    selected.isEmpty() ? Component.translatable("gui.ascension.auction.select_item") : selected.getHoverName(),
                    12, 38, selected.isEmpty() ? MUTED : TEXT, false);

            graphics.text(font, Component.translatable("gui.ascension.auction.quantity", quantity), 236, 55, TEXT, false);
            graphics.text(font, Component.translatable("gui.ascension.auction.bid", startingBid), 236, 82, TEXT, false);
            graphics.text(font, Component.translatable("gui.ascension.auction.duration", formatDuration(duration)), 236, 109, TEXT, false);
            graphics.text(font, Component.literal("Click: normal | Shift: medium | Ctrl: large"), 12, 218, MUTED, false);
        }
    }

    private final class BidderBrowserPanel extends RenderableElement {
        private final int page;

        BidderBrowserPanel(UIFrame frame, int page) {
            super(frame);
            int pages = Math.max(1, (packet.houses().size() + 5) / 6);
            this.page = Math.max(0, Math.min(page, pages - 1));
            setWidth(WIDTH);
            setHeight(HEIGHT);

            int start = this.page * 6;
            int y = 42;
            for (int i = start; i < Math.min(start + 6, packet.houses().size()); i++) {
                AuctionViewData.HouseView house = packet.houses().get(i);
                HouseButton button = new HouseButton(frame, house, () -> openPanel(new HousePanel(frame, house, 0)));
                button.place(18, y, 324, 27);
                addChild(button);
                y += 31;
            }

            if (this.page > 0) {
                TextButton previous = new TextButton(frame, Component.literal("<"), () -> openPanel(new BidderBrowserPanel(frame, this.page - 1)));
                previous.place(10, 210, 26, 18);
                addChild(previous);
            }
            if (this.page + 1 < pages) {
                TextButton next = new TextButton(frame, Component.literal(">"), () -> openPanel(new BidderBrowserPanel(frame, this.page + 1)));
                next.place(324, 210, 26, 18);
                addChild(next);
            }
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, Component.translatable("gui.ascension.auction.bidder"), WIDTH / 2, 12, TEXT);
            if (packet.houses().isEmpty()) {
                graphics.centeredText(font, Component.translatable("gui.ascension.auction.no_houses"), WIDTH / 2, 100, MUTED);
            } else {
                int pages = Math.max(1, (packet.houses().size() + 5) / 6);
                graphics.centeredText(font, Component.literal("Page " + (page + 1) + " / " + pages), WIDTH / 2, 217, MUTED);
            }
        }
    }

    private final class HousePanel extends RenderableElement {
        private final AuctionViewData.HouseView house;
        private final int page;

        HousePanel(UIFrame frame, AuctionViewData.HouseView house, int page) {
            super(frame);
            this.house = house;
            int pages = Math.max(1, (house.auctions().size() + 4) / 5);
            this.page = Math.max(0, Math.min(page, pages - 1));
            setWidth(WIDTH);
            setHeight(HEIGHT);

            TextButton back = new TextButton(frame, Component.translatable("gui.ascension.auction.back"),
                    () -> openPanel(new BidderBrowserPanel(frame, 0)));
            back.place(10, 8, 52, 18);
            addChild(back);

            int start = this.page * 5;
            int y = 43;
            for (int i = start; i < Math.min(start + 5, house.auctions().size()); i++) {
                AuctionViewData.AuctionView auction = house.auctions().get(i);
                ListingButton button = new ListingButton(frame, auction,
                        () -> openPanel(new AuctionDetailPanel(frame, house, auction)));
                button.place(12, y, 336, 32);
                addChild(button);
                y += 35;
            }

            if (this.page > 0) {
                TextButton previous = new TextButton(frame, Component.literal("<"), () -> openPanel(new HousePanel(frame, house, this.page - 1)));
                previous.place(10, 218, 26, 16);
                addChild(previous);
            }
            if (this.page + 1 < pages) {
                TextButton next = new TextButton(frame, Component.literal(">"), () -> openPanel(new HousePanel(frame, house, this.page + 1)));
                next.place(324, 218, 26, 16);
                addChild(next);
            }
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, Component.literal(house.ownerName() + "'s Auctions"), WIDTH / 2, 12, TEXT);
            if (house.auctions().isEmpty()) {
                graphics.centeredText(font, Component.translatable("gui.ascension.auction.no_auctions"), WIDTH / 2, 100, MUTED);
            }
        }
    }

    private final class AuctionDetailPanel extends RenderableElement {
        private final AuctionViewData.HouseView house;
        private final AuctionViewData.AuctionView auction;
        private long desiredBid;

        AuctionDetailPanel(UIFrame frame, AuctionViewData.HouseView house, AuctionViewData.AuctionView auction) {
            super(frame);
            this.house = house;
            this.auction = auction;
            this.desiredBid = auction.minimumNextBid();
            setWidth(WIDTH);
            setHeight(HEIGHT);

            TextButton back = new TextButton(frame, Component.translatable("gui.ascension.auction.back"),
                    () -> openPanel(new HousePanel(frame, house, 0)));
            back.place(10, 8, 52, 18);
            addChild(back);

            ModifierTextButton bidDown = new ModifierTextButton(frame, Component.literal("-"),
                    modifiers -> desiredBid = Math.max(auction.minimumNextBid(), desiredBid - modifierLong(modifiers, 1L, 10L, 100L)));
            bidDown.place(95, 175, 30, 22);
            addChild(bidDown);
            ModifierTextButton bidUp = new ModifierTextButton(frame, Component.literal("+"),
                    modifiers -> desiredBid = safeAdd(desiredBid, modifierLong(modifiers, 1L, 10L, 100L)));
            bidUp.place(235, 175, 30, 22);
            addChild(bidUp);
            TextButton place = new TextButton(frame, Component.translatable("gui.ascension.auction.bid_now"), () ->
                    ClientPacketDistributor.sendToServer(new BidAuctionPacket(packet.accessPos(), auction.id(), desiredBid)));
            place.place(125, 204, 110, 23);
            addChild(place);
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, auction.item().getHoverName(), WIDTH / 2, 12, TEXT);
            graphics.fill(154, 38, 206, 90, PANEL);
            border(graphics, 154, 38, 52, 52, BORDER);
            graphics.item(auction.item(), 172, 56);

            int y = 101;
            graphics.centeredText(font,
                    Component.translatable(auction.currentBid() > 0 ? "gui.ascension.auction.current_bid" : "gui.ascension.auction.starting_bid",
                            auction.currentBid() > 0 ? auction.currentBid() : auction.startingBid()), WIDTH / 2, y, GOLD);
            y += 14;
            if (!auction.highestBidderName().isBlank()) {
                graphics.centeredText(font, Component.translatable("gui.ascension.auction.highest_bidder", auction.highestBidderName()), WIDTH / 2, y, MUTED);
                y += 14;
            }
            graphics.centeredText(font, Component.translatable("gui.ascension.auction.time_left",
                    formatDuration(Math.max(0L, auction.endsAtMillis() - System.currentTimeMillis()))), WIDTH / 2, y, MUTED);
            graphics.centeredText(font, Component.translatable("gui.ascension.auction.minimum_bid", auction.minimumNextBid()), WIDTH / 2, 153, MUTED);
            graphics.centeredText(font, Component.literal(Long.toString(desiredBid)), WIDTH / 2, 181, TEXT);
        }
    }

    private final class InboxPanel extends RenderableElement {
        private final int page;

        InboxPanel(UIFrame frame, int page) {
            super(frame);
            int pages = Math.max(1, (packet.inboxItems().size() + 44) / 45);
            this.page = Math.max(0, Math.min(page, pages - 1));
            setWidth(WIDTH);
            setHeight(HEIGHT);

            TextButton back = new TextButton(frame, Component.translatable("gui.ascension.auction.back"), () -> {
                if (packet.mode() == AuctionScreenPacket.Mode.OWNER || packet.mode() == AuctionScreenPacket.Mode.OWNER_INBOX) {
                    openPanel(new OwnerPanel(frame));
                } else {
                    Minecraft.getInstance().setScreen(null);
                }
            });
            back.place(10, 8, 52, 18);
            addChild(back);

            UUID returnHouse = (packet.mode() == AuctionScreenPacket.Mode.OWNER || packet.mode() == AuctionScreenPacket.Mode.OWNER_INBOX)
                    ? packet.houseId() : null;
            var returnPos = returnHouse == null ? net.minecraft.core.BlockPos.ZERO : packet.accessPos();

            if (packet.inboxCurrency() > 0L) {
                TextButton claim = new TextButton(frame,
                        Component.translatable("gui.ascension.auction.claim_stones", packet.inboxCurrency()),
                        () -> ClientPacketDistributor.sendToServer(AuctionInboxActionPacket.currency(returnHouse, returnPos)));
                claim.place(94, 35, 172, 22);
                addChild(claim);
            }

            int start = this.page * 45;
            for (int local = 0; local < 45 && start + local < packet.inboxItems().size(); local++) {
                AuctionViewData.InboxEntryView entry = packet.inboxItems().get(start + local);
                int col = local % 9;
                int row = local / 9;
                ItemButton item = new ItemButton(frame, entry::item, () -> false,
                        () -> ClientPacketDistributor.sendToServer(AuctionInboxActionPacket.item(entry.id(), returnHouse, returnPos)));
                item.place(98 + col * 18, 77 + row * 18, 17, 17);
                addChild(item);
            }

            if (this.page > 0) {
                TextButton previous = new TextButton(frame, Component.literal("<"), () -> openPanel(new InboxPanel(frame, this.page - 1)));
                previous.place(68, 120, 22, 22);
                addChild(previous);
            }
            if (this.page + 1 < pages) {
                TextButton next = new TextButton(frame, Component.literal(">"), () -> openPanel(new InboxPanel(frame, this.page + 1)));
                next.place(270, 120, 22, 22);
                addChild(next);
            }
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, Component.translatable("gui.ascension.auction.inbox"), WIDTH / 2, 11, TEXT);
            graphics.centeredText(font, Component.translatable("gui.ascension.auction.inbox_hint"), WIDTH / 2, 62, MUTED);
            int pages = Math.max(1, (packet.inboxItems().size() + 44) / 45);
            graphics.centeredText(font, Component.literal("Page " + (page + 1) + " / " + pages), WIDTH / 2, 177, MUTED);
            if (packet.inboxItems().isEmpty() && packet.inboxCurrency() <= 0L) {
                graphics.centeredText(font, Component.translatable("gui.ascension.auction.inbox_empty"), WIDTH / 2, 105, MUTED);
            }
        }
    }

    private final class CurrentBidsPanel extends RenderableElement {
        private final int page;

        CurrentBidsPanel(UIFrame frame, int page) {
            super(frame);
            int pages = Math.max(1, (packet.auctions().size() + 4) / 5);
            this.page = Math.max(0, Math.min(page, pages - 1));
            setWidth(WIDTH);
            setHeight(HEIGHT);

            TextButton back = new TextButton(frame, Component.translatable("gui.ascension.auction.back"),
                    () -> Minecraft.getInstance().setScreen(null));
            back.place(10, 8, 52, 18);
            addChild(back);

            if (this.page > 0) {
                TextButton previous = new TextButton(frame, Component.literal("<"), () -> openPanel(new CurrentBidsPanel(frame, this.page - 1)));
                previous.place(10, 218, 26, 16);
                addChild(previous);
            }
            if (this.page + 1 < pages) {
                TextButton next = new TextButton(frame, Component.literal(">"), () -> openPanel(new CurrentBidsPanel(frame, this.page + 1)));
                next.place(324, 218, 26, 16);
                addChild(next);
            }
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, Component.translatable("gui.ascension.auction.current_bids"), WIDTH / 2, 12, TEXT);

            if (packet.auctions().isEmpty()) {
                graphics.centeredText(font, Component.translatable("gui.ascension.auction.no_current_bids"), WIDTH / 2, 106, MUTED);
                return;
            }

            int start = page * 5;
            int y = 43;
            for (int i = start; i < Math.min(start + 5, packet.auctions().size()); i++) {
                drawCurrentBid(graphics, packet.auctions().get(i), 12, y, 336, 36);
                y += 39;
            }

            int pages = Math.max(1, (packet.auctions().size() + 4) / 5);
            graphics.centeredText(font, Component.literal("Page " + (page + 1) + " / " + pages), WIDTH / 2, 221, MUTED);
        }
    }

    private static class TextButton extends BetterButton {
        private final Component text;
        private final Runnable action;

        TextButton(UIFrame frame, Component text, Runnable action) {
            super(frame, 0, 0);
            this.text = text;
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
            int fill = isHovered() || isPressed() ? PANEL_ALT : PANEL;
            graphics.fill(0, 0, getWidth(), getHeight(), fill);
            border(graphics, 0, 0, getWidth(), getHeight(), isHovered() ? BORDER_HOVER : BORDER);
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, text, getWidth() / 2, Math.max(2, (getHeight() - 8) / 2), TEXT);
        }
    }

    private static final class ModifierTextButton extends BetterButton {
        private final Component text;
        private final IntConsumer action;

        ModifierTextButton(UIFrame frame, Component text, IntConsumer action) {
            super(frame, 0, 0);
            this.text = text;
            this.action = action;
        }

        void place(int x, int y, int width, int height) {
            getPositioning().setX(x);
            getPositioning().setY(y);
            setWidth(width);
            setHeight(height);
        }

        @Override
        public void onMouseUp(EasyEvent event) {
            if (!(event instanceof EasyMouseEvent mouseEvent)) {
                return;
            }

            MouseButtonEvent buttonEvent = mouseEvent.getMouseEvent();
            if (buttonEvent == null || buttonEvent.button() != InputConstants.MOUSE_BUTTON_LEFT) {
                return;
            }

            boolean shouldClick = isPressed()
                    && isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY());
            setPressed(false);
            if (shouldClick) {
                action.accept(buttonEvent.modifiers());
            }
        }

        @Override
        public void onClick() {
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int fill = isHovered() || isPressed() ? PANEL_ALT : PANEL;
            graphics.fill(0, 0, getWidth(), getHeight(), fill);
            border(graphics, 0, 0, getWidth(), getHeight(), isHovered() ? BORDER_HOVER : BORDER);
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, text, getWidth() / 2, Math.max(2, (getHeight() - 8) / 2), TEXT);
        }
    }

    private static final class ItemButton extends BetterButton {
        private final StackSupplier stackSupplier;
        private final BooleanSupplier selected;
        private final Runnable action;

        ItemButton(UIFrame frame, StackSupplier stackSupplier, BooleanSupplier selected, Runnable action) {
            super(frame, 0, 0);
            this.stackSupplier = stackSupplier;
            this.selected = selected;
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
            graphics.fill(0, 0, getWidth(), getHeight(), selected.getAsBoolean() ? 0xFF61522C : PANEL);
            border(graphics, 0, 0, getWidth(), getHeight(), selected.getAsBoolean() || isHovered() ? BORDER_HOVER : BORDER);
            ItemStack stack = stackSupplier.get();
            if (!stack.isEmpty()) {
                graphics.item(stack, 1, 1);
                if (stack.getCount() > 1) {
                    String count = Integer.toString(stack.getCount());
                    Font font = Minecraft.getInstance().font;
                    graphics.text(font, count, Math.max(1, 16 - font.width(count)), 9, TEXT, true);
                }
            }
        }
    }

    private static final class HouseButton extends BetterButton {
        private final AuctionViewData.HouseView house;
        private final Runnable action;

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
        public void onClick() { action.run(); }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, getWidth(), getHeight(), isHovered() ? PANEL_ALT : PANEL);
            border(graphics, 0, 0, getWidth(), getHeight(), isHovered() ? BORDER_HOVER : BORDER);
            graphics.item(new ItemStack(Items.PLAYER_HEAD), 6, 5);
            Font font = Minecraft.getInstance().font;
            graphics.text(font, Component.literal(house.ownerName()), 29, 5, TEXT, false);
            graphics.text(font, Component.translatable("gui.ascension.auction.auctions_count", house.auctions().size()), 29, 15, MUTED, false);
        }
    }

    private static final class ListingButton extends BetterButton {
        private final AuctionViewData.AuctionView auction;
        private final Runnable action;

        ListingButton(UIFrame frame, AuctionViewData.AuctionView auction, Runnable action) {
            super(frame, 0, 0);
            this.auction = auction;
            this.action = action;
        }

        void place(int x, int y, int width, int height) {
            getPositioning().setX(x);
            getPositioning().setY(y);
            setWidth(width);
            setHeight(height);
        }

        @Override
        public void onClick() { action.run(); }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, getWidth(), getHeight(), isHovered() ? PANEL_ALT : PANEL);
            border(graphics, 0, 0, getWidth(), getHeight(), isHovered() ? BORDER_HOVER : BORDER);
            graphics.item(auction.item(), 6, 8);
            Font font = Minecraft.getInstance().font;
            graphics.text(font, auction.item().getHoverName(), 29, 5, TEXT, false);
            long bid = auction.currentBid() > 0L ? auction.currentBid() : auction.startingBid();
            graphics.text(font, Component.literal(bid + " stones | " + formatDuration(Math.max(0L, auction.endsAtMillis() - System.currentTimeMillis()))),
                    29, 17, MUTED, false);
        }
    }

    private void drawCurrentBid(GuiGraphicsExtractor graphics, AuctionViewData.AuctionView auction, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL);
        border(graphics, x, y, width, height, auction.viewerIsWinning() ? BORDER_HOVER : BORDER);
        graphics.item(auction.item(), x + 6, y + 8);
        Font font = Minecraft.getInstance().font;
        graphics.text(font, auction.item().getHoverName(), x + 29, y + 4, TEXT, false);
        String state = Component.translatable(auction.viewerIsWinning()
                ? "gui.ascension.auction.bid_winning"
                : "gui.ascension.auction.bid_outbid").getString();
        graphics.text(font, Component.literal("Your bid: " + auction.viewerEscrow()
                        + " | Current: " + (auction.currentBid() > 0L ? auction.currentBid() : auction.startingBid())
                        + " | " + state),
                x + 29, y + 15, auction.viewerIsWinning() ? GOLD : MUTED, false);
        graphics.text(font, Component.literal(auction.sellerName() + " | "
                        + formatDuration(Math.max(0L, auction.endsAtMillis() - System.currentTimeMillis()))),
                x + 29, y + 27, MUTED, false);
    }

    private void drawListing(GuiGraphicsExtractor graphics, AuctionViewData.AuctionView auction, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL);
        border(graphics, x, y, width, height, BORDER);
        graphics.item(auction.item(), x + 6, y + 7);
        Font font = Minecraft.getInstance().font;
        graphics.text(font, auction.item().getHoverName(), x + 28, y + 5, TEXT, false);
        long shown = auction.currentBid() > 0 ? auction.currentBid() : auction.startingBid();
        String bidder = auction.highestBidderName().isBlank() ? "No bids" : auction.highestBidderName();
        graphics.text(font, Component.literal(shown + " stones | " + bidder + " | "
                        + formatDuration(Math.max(0L, auction.endsAtMillis() - System.currentTimeMillis()))),
                x + 28, y + 17, MUTED, false);
    }

    private static ItemStack inventoryStack(int slot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || slot < 0 || slot >= minecraft.player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return minecraft.player.getInventory().getItem(slot);
    }

    private static int modifierInt(int modifiers, int normal, int shift, int control) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) return control;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) return shift;
        return normal;
    }

    private static long modifierLong(int modifiers, long normal, long shift, long control) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) return control;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) return shift;
        return normal;
    }

    private static long durationStep(int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) return 10L * 60L * 60L * 1000L;
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) return 30L * 60L * 1000L;
        return 60L * 60L * 1000L;
    }

    private static long safeAdd(long a, long b) {
        if (b > 0L && a > Long.MAX_VALUE - b) return Long.MAX_VALUE;
        return a + b;
    }

    private static String formatDuration(long millis) {
        long totalMinutes = Math.max(0L, millis / 60_000L);
        long days = totalMinutes / (24L * 60L);
        long hours = (totalMinutes / 60L) % 24L;
        long minutes = totalMinutes % 60L;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    private static void border(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    @FunctionalInterface
    private interface StackSupplier {
        ItemStack get();
    }
}
