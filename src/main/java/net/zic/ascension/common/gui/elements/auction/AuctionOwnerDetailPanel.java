package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.common.auction.AuctionViewData;
import net.zic.ascension.network.auction.AuctionActionPacket;

final class AuctionOwnerDetailPanel extends AuctionPanel {
    private final AuctionViewData.AuctionView auction;

    AuctionOwnerDetailPanel(UIFrame frame, AuctionContainer host, AuctionViewData.AuctionView auction, Runnable returnAction) {
        super(frame, host, AuctionUi.BIDDING);
        this.auction = auction;

        addBack(returnAction);

        if (auction.currentBid() <= 0L) {
            AuctionButtons.HotspotButton cancel = new AuctionButtons.HotspotButton(
                    frame,
                    Component.literal("Cancel auction"),
                    () -> ClientPacketDistributor.sendToServer(
                            AuctionActionPacket.cancel(host.packet().accessPos(), auction.id()))
            );
            cancel.place(74, 167, 86, 8);
            addChild(cancel);
        }

        AuctionButtons.TooltipHotspot itemTip = new AuctionButtons.TooltipHotspot(
                frame, () -> auction.item().getHoverName()
        );
        itemTip.place(90, 60, 54, 53);
        addChild(itemTip);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawHeaderTitle(graphics, Component.literal("My Auction"));

        AuctionUi.centeredScaled(graphics,
                AuctionUi.stackTitle(auction.item(), 24),
                117.0F, 47.0F, 0.67F, AuctionUi.TEXT);
        AuctionUi.itemScaled(graphics, auction.item(), 117.0F, 86.5F, 2.5F);
        AuctionUi.centeredScaled(graphics,
                AuctionUi.formatDuration(Math.max(0L, auction.endsAtMillis() - System.currentTimeMillis())) + " left",
                117.0F, 120.0F, 0.72F, AuctionUi.MUTED);

        long shownBid = auction.currentBid() > 0L ? auction.currentBid() : auction.startingBid();
        AuctionUi.centeredScaled(graphics, Long.toString(shownBid), 117.0F, 138.0F, 0.74F, AuctionUi.ACCENT);
        AuctionUi.centeredScaled(graphics,
                auction.highestBidderName().isBlank() ? "No bids yet" : AuctionUi.shortString(auction.highestBidderName(), 18),
                117.0F, 153.0F, 0.68F, AuctionUi.MUTED);

        AuctionUi.centeredScaled(graphics,
                auction.currentBid() <= 0L ? "Cancel Auction" : "Auction locked",
                117.0F, 168.0F, 0.66F,
                auction.currentBid() <= 0L ? AuctionUi.NEGATIVE : AuctionUi.MUTED);
    }
}
