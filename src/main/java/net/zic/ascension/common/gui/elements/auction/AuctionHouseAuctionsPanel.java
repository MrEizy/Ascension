package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.auction.AuctionViewData;

final class AuctionHouseAuctionsPanel extends AuctionPanel {
    private static final int PAGE_SIZE = 4;
    private final AuctionViewData.HouseView house;
    private final int page;
    private final int pages;

    AuctionHouseAuctionsPanel(UIFrame frame, AuctionContainer host, AuctionViewData.HouseView house, int requestedPage) {
        super(frame, host, AuctionUi.MY_AUCTIONS);
        this.house = house;
        this.pages = Math.max(1, (house.auctions().size() + PAGE_SIZE - 1) / PAGE_SIZE);
        this.page = Math.max(0, Math.min(requestedPage, pages - 1));

        addBack(() -> host.openBidderBrowser(0));
        addListings();
        addPager(page, pages,
                () -> host.openHouse(house, page - 1),
                () -> host.openHouse(house, page + 1));
    }

    private void addListings() {
        int start = page * PAGE_SIZE;
        int count = Math.min(PAGE_SIZE, house.auctions().size() - start);
        int y = 56 + Math.max(0, (92 - count * 22) / 2);

        for (int i = start; i < Math.min(start + PAGE_SIZE, house.auctions().size()); i++) {
            AuctionViewData.AuctionView auction = house.auctions().get(i);
            AuctionButtons.ListingButton row = new AuctionButtons.ListingButton(
                    getUiFrame(), auction, AuctionButtons.ListingStyle.STANDARD,
                    () -> host.openBidDetail(house, auction)
            );
            row.place(9, y, 216, 20);
            addChild(row);
            y += 22;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawHeaderTitle(graphics, Component.literal(AuctionUi.shortString(house.ownerName(), 16)));

        if (house.auctions().isEmpty()) {
            AuctionUi.centeredScaled(graphics,
                    Component.translatable("gui.ascension.auction.no_auctions"),
                    117.0F, 103.0F, 0.82F, AuctionUi.MUTED);
        }

        drawPage(graphics, page, pages);
    }
}
