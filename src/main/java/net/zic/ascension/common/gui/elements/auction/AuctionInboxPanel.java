package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.common.auction.AuctionViewData;
import net.zic.ascension.network.auction.AuctionInboxActionPacket;

import java.util.UUID;

final class AuctionInboxPanel extends AuctionPanel {
    private static final int PAGE_SIZE = 4;

    private final int page;
    private final int pages;
    private final UUID returnHouse;
    private final net.minecraft.core.BlockPos returnPos;

    AuctionInboxPanel(UIFrame frame, AuctionContainer host, int requestedPage) {
        super(frame, host, AuctionUi.INBOX);

        int totalEntries = totalEntries();
        this.pages = Math.max(1, (totalEntries + PAGE_SIZE - 1) / PAGE_SIZE);
        this.page = Math.max(0, Math.min(requestedPage, pages - 1));
        this.returnHouse = host.hasOwnerContext() ? host.packet().houseId() : null;
        this.returnPos = returnHouse == null ? net.minecraft.core.BlockPos.ZERO : host.packet().accessPos();

        if (host.hasOwnerContext()) {
            addOwnerNavigation(AuctionButtons.OwnerTab.INBOX);
            addBack(host::openOwner);
        } else {
            addBack(host::close);
        }

        addEntries();
        addPager(page, pages,
                () -> host.openInbox(page - 1),
                () -> host.openInbox(page + 1));
    }

    private int totalEntries() {
        return host.packet().inboxItems().size() + (host.packet().inboxCurrency() > 0L ? 1 : 0);
    }

    private void addEntries() {
        int total = totalEntries();
        int start = page * PAGE_SIZE;
        int count = Math.min(PAGE_SIZE, total - start);
        int y = 56 + Math.max(0, (92 - count * 22) / 2);

        for (int virtualIndex = start; virtualIndex < Math.min(start + PAGE_SIZE, total); virtualIndex++) {
            if (virtualIndex < host.packet().inboxItems().size()) {
                AuctionViewData.InboxEntryView entry = host.packet().inboxItems().get(virtualIndex);

                AuctionButtons.InboxEntryButton row = new AuctionButtons.InboxEntryButton(
                        getUiFrame(), entry
                );
                row.place(9, y, 216, 20);
                addChild(row);

                AuctionButtons.InboxClaimButton claim = new AuctionButtons.InboxClaimButton(
                        getUiFrame(),
                        () -> ClientPacketDistributor.sendToServer(
                                AuctionInboxActionPacket.item(entry.id(), returnHouse, returnPos))
                );
                claim.place(178, y + 4);
                addChild(claim);
            } else {
                AuctionButtons.CurrencyInboxButton row = new AuctionButtons.CurrencyInboxButton(
                        getUiFrame(), host.packet().inboxCurrency()
                );
                row.place(9, y, 216, 20);
                addChild(row);

                AuctionButtons.InboxClaimButton claim = new AuctionButtons.InboxClaimButton(
                        getUiFrame(),
                        () -> ClientPacketDistributor.sendToServer(
                                AuctionInboxActionPacket.currency(returnHouse, returnPos))
                );
                claim.place(178, y + 4);
                addChild(claim);
            }
            y += 22;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawHeaderTitle(graphics, Component.translatable("gui.ascension.auction.inbox"));

        AuctionUi.centeredScaled(graphics,
                "Items: " + totalEntries(),
                117.0F, 47.0F, 0.66F, AuctionUi.MUTED);

        if (totalEntries() == 0) {
            AuctionUi.centeredScaled(graphics,
                    Component.translatable("gui.ascension.auction.inbox_empty"),
                    117.0F, 103.0F, 0.82F, AuctionUi.MUTED);
        }

        drawPage(graphics, page, pages);
    }
}
