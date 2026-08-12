package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.auction.AuctionViewData;

final class AuctionOwnerPanel extends AuctionPanel {
    private final AuctionScrollBox auctions;

    AuctionOwnerPanel(UIFrame frame, AuctionContainer host) {
        super(frame, host, AuctionUi.MAIN);
        addOwnerNavigation(AuctionButtons.OwnerTab.NONE);

        auctions = new AuctionScrollBox(frame, 156, 58, 19);
        auctions.getPositioning().setX(39);
        auctions.getPositioning().setY(96);
        addChild(auctions);

        int totalHeight = host.packet().auctions().size() * 19;
        int y = totalHeight < 58 ? (58 - totalHeight) / 2 : 0;
        for (AuctionViewData.AuctionView auction : host.packet().auctions()) {
            AuctionButtons.ListingButton row = new AuctionButtons.ListingButton(frame, auction,
                    AuctionButtons.ListingStyle.COMPACT,
                    () -> host.openOwnerDetail(auction, host::openOwner));
            row.size(156, 19);
            auctions.addRow(row, y);
            y += 19;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawHeaderTitle(graphics, Component.translatable("gui.ascension.auction.title"));

        String owner = host.packet().ownerName().isBlank() ? "Owner" : AuctionUi.shortString(host.packet().ownerName(), 16);
        AuctionUi.centeredFitted(graphics, owner, 52.0F, 50.0F, 80.0F, 0.74F, AuctionUi.TEXT);
        AuctionUi.centeredFitted(graphics, "Stones: " + host.packet().inboxCurrency(), 52.0F, 66.0F, 80.0F, 0.68F, AuctionUi.ACCENT);

        if (host.packet().auctions().isEmpty()) {
            AuctionUi.centeredFitted(graphics,
                    Component.translatable("gui.ascension.auction.no_auctions"),
                    117.0F, 116.0F, 150.0F, 0.78F, AuctionUi.MUTED);
        }
    }
}
