package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.auction.AuctionViewData;

final class AuctionCurrentBidsPanel extends AuctionPanel {
    private static final int PAGE_SIZE = 4;
    private final int page;
    private final int pages;

    AuctionCurrentBidsPanel(UIFrame frame, AuctionContainer host, int requestedPage) {
        super(frame, host, AuctionUi.MY_AUCTIONS);
        this.pages = Math.max(1, (host.packet().auctions().size() + PAGE_SIZE - 1) / PAGE_SIZE);
        this.page = Math.max(0, Math.min(requestedPage, pages - 1));

        addBack(host::close);
        addBids();
        addPager(page, pages,
                () -> host.openCurrentBids(page - 1),
                () -> host.openCurrentBids(page + 1));
    }

    private void addBids() {
        int start = page * PAGE_SIZE;
        int count = Math.min(PAGE_SIZE, host.packet().auctions().size() - start);
        int y = 56 + Math.max(0, (92 - count * 22) / 2);

        for (int i = start; i < Math.min(start + PAGE_SIZE, host.packet().auctions().size()); i++) {
            AuctionViewData.AuctionView auction = host.packet().auctions().get(i);
            AuctionButtons.ListingButton row = new AuctionButtons.ListingButton(
                    getUiFrame(), auction, AuctionButtons.ListingStyle.CURRENT_BID, null
            );
            row.place(9, y, 216, 20);
            addChild(row);
            y += 22;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawHeaderTitle(graphics, Component.translatable("gui.ascension.auction.current_bids"));

        if (host.packet().auctions().isEmpty()) {
            AuctionUi.centeredScaled(graphics,
                    Component.translatable("gui.ascension.auction.no_current_bids"),
                    117.0F, 103.0F, 0.82F, AuctionUi.MUTED);
        }

        drawPage(graphics, page, pages);
    }
}
