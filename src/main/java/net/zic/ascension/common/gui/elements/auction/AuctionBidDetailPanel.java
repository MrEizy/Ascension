package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.common.auction.AuctionViewData;
import net.zic.ascension.network.auction.AuctionActionPacket;
import net.zic.ascension.network.auction.BidAuctionPacket;

final class AuctionBidDetailPanel extends AuctionPanel {
    private final AuctionViewData.HouseView house;
    private final AuctionViewData.AuctionView auction;
    private long desiredBid;

    AuctionBidDetailPanel(UIFrame frame, AuctionContainer host,
                          AuctionViewData.HouseView house, AuctionViewData.AuctionView auction) {
        super(frame, host, AuctionUi.BIDDING);
        this.house = house;
        this.auction = auction;
        this.desiredBid = auction.minimumNextBid();

        addBack(() -> host.openHouse(house, 0));

        AuctionButtons.ModifierButton down = new AuctionButtons.ModifierButton(
                frame, AuctionUi.MINUS_BUTTON, Component.literal("Bid amount"),
                modifiers -> desiredBid = Math.max(auction.minimumNextBid(),
                        desiredBid - AuctionUi.modifierLong(modifiers, 1L, 10L, 100L))
        );
        down.place(89, 136);
        addChild(down);

        AuctionButtons.ModifierButton up = new AuctionButtons.ModifierButton(
                frame, AuctionUi.PLUS_BUTTON, Component.literal("Bid amount"),
                modifiers -> desiredBid = AuctionUi.safeAdd(desiredBid,
                        AuctionUi.modifierLong(modifiers, 1L, 10L, 100L))
        );
        up.place(133, 136);
        addChild(up);

        AuctionButtons.HotspotButton notifications = new AuctionButtons.HotspotButton(
                frame,
                Component.literal("Notifications"),
                () -> ClientPacketDistributor.sendToServer(
                        AuctionActionPacket.toggleNotifications(host.packet().accessPos(), auction.id()))
        );
        notifications.place(90, 152, 54, 10);
        addChild(notifications);

        AuctionButtons.HotspotButton bid = new AuctionButtons.HotspotButton(
                frame,
                Component.literal("Place bid"),
                () -> ClientPacketDistributor.sendToServer(
                        new BidAuctionPacket(host.packet().accessPos(), auction.id(), desiredBid))
        );
        bid.place(74, 167, 86, 8);
        addChild(bid);

        AuctionButtons.TooltipHotspot itemTip = new AuctionButtons.TooltipHotspot(
                frame, () -> auction.item().getHoverName()
        );
        itemTip.place(90, 60, 54, 53);
        addChild(itemTip);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawHeaderTitle(graphics, Component.literal("Bidding"));

        AuctionUi.centeredScaled(graphics,
                AuctionUi.stackTitle(auction.item(), 24),
                117.0F, 47.0F, 0.67F, AuctionUi.TEXT);
        AuctionUi.itemScaled(graphics, auction.item(), 117.0F, 86.5F, 2.5F);
        AuctionUi.centeredScaled(graphics,
                AuctionUi.formatDuration(Math.max(0L, auction.endsAtMillis() - System.currentTimeMillis())) + " left",
                117.0F, 120.0F, 0.72F, AuctionUi.MUTED);

        AuctionUi.centeredScaled(graphics, Long.toString(desiredBid), 117.0F, 138.0F, 0.74F, AuctionUi.TEXT);
        AuctionUi.centeredScaled(graphics,
                auction.viewerNotifications() ? "Notify: ON" : "Notify: OFF",
                117.0F, 153.0F, 0.68F,
                auction.viewerNotifications() ? AuctionUi.POSITIVE : AuctionUi.MUTED);
        AuctionUi.centeredScaled(graphics, "Place Bid", 117.0F, 168.0F, 0.66F, AuctionUi.TEXT);
    }
}
