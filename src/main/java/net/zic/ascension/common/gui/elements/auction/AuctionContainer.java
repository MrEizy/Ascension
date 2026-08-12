package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.Minecraft;
import net.zic.ascension.common.auction.AuctionViewData;
import net.zic.ascension.network.auction.AuctionScreenPacket;

import java.util.UUID;

public final class AuctionContainer extends RenderableElement {
    public static final int WIDTH = 234;
    public static final int HEIGHT = 222;

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
            case OWNER -> openOwner();
            case OWNER_AUCTIONS -> openMyAuctions(0);
            case OWNER_INBOX, INBOX -> openInbox(0);
            case BIDDER -> openBidderInitial();
            case BIDS -> openCurrentBids(0);
        }
    }

    AuctionScreenPacket packet() {
        return packet;
    }

    boolean hasOwnerContext() {
        return packet.houseId() != null && (packet.mode() == AuctionScreenPacket.Mode.OWNER
                || packet.mode() == AuctionScreenPacket.Mode.OWNER_AUCTIONS
                || packet.mode() == AuctionScreenPacket.Mode.OWNER_INBOX);
    }

    void openOwner() {
        openPanel(new AuctionOwnerPanel(getUiFrame(), this));
    }

    void openCreate() {
        openPanel(new AuctionCreatePanel(getUiFrame(), this));
    }

    void openMyAuctions(int page) {
        openPanel(new AuctionMyAuctionsPanel(getUiFrame(), this, page));
    }

    void openOwnerDetail(AuctionViewData.AuctionView auction, Runnable returnAction) {
        openPanel(new AuctionOwnerDetailPanel(getUiFrame(), this, auction, returnAction));
    }

    void openBidderBrowser(int page) {
        openPanel(new AuctionBidderBrowserPanel(getUiFrame(), this, page));
    }

    void openHouse(AuctionViewData.HouseView house, int page) {
        openPanel(new AuctionHouseAuctionsPanel(getUiFrame(), this, house, page));
    }

    void openBidDetail(AuctionViewData.HouseView house, AuctionViewData.AuctionView auction) {
        openPanel(new AuctionBidDetailPanel(getUiFrame(), this, house, auction));
    }

    void openInbox(int page) {
        openPanel(new AuctionInboxPanel(getUiFrame(), this, page));
    }

    void openCurrentBids(int page) {
        openPanel(new AuctionCurrentBidsPanel(getUiFrame(), this, page));
    }

    void close() {
        Minecraft.getInstance().setScreen(null);
    }

    private void openBidderInitial() {
        UUID focus = packet.focusAuctionId();
        if (focus != null) {
            for (AuctionViewData.HouseView house : packet.houses()) {
                for (AuctionViewData.AuctionView auction : house.auctions()) {
                    if (auction.id().equals(focus)) {
                        openBidDetail(house, auction);
                        return;
                    }
                }
            }
        }
        openBidderBrowser(0);
    }

    private void openPanel(RenderableElement panel) {
        removeChildren();
        panel.setActive(true);
        addChild(panel);
    }
}
