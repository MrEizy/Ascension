package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.network.auction.AuctionInboxActionPacket;

abstract class AuctionPanel extends RenderableElement {
    protected final AuctionContainer host;
    private final ITextureData body;
    private final ITextureData header;

    AuctionPanel(UIFrame frame, AuctionContainer host, Identifier texture) {
        super(frame);
        this.host = host;
        this.body = AuctionUi.body(texture);
        this.header = AuctionUi.header(texture);
        setWidth(AuctionContainer.WIDTH);
        setHeight(AuctionContainer.HEIGHT);
    }

    protected void addOwnerNavigation(AuctionButtons.OwnerTab selected) {
        AuctionButtons.NavButton create = new AuctionButtons.NavButton(
                getUiFrame(), AuctionUi.CREATE_BUTTON, selected == AuctionButtons.OwnerTab.CREATE,
                Component.literal("Create Auction"), host::openCreate
        );
        create.place(1, 15);
        addChild(create);

        AuctionButtons.NavButton auctions = new AuctionButtons.NavButton(
                getUiFrame(), AuctionUi.MY_AUCTIONS_BUTTON, selected == AuctionButtons.OwnerTab.AUCTIONS,
                Component.literal("My Auctions"), () -> host.openMyAuctions(0)
        );
        auctions.place(31, 15);
        addChild(auctions);

        AuctionButtons.NavButton mail = new AuctionButtons.NavButton(
                getUiFrame(), AuctionUi.MAIL_BUTTON, selected == AuctionButtons.OwnerTab.INBOX,
                Component.literal("Auction Inbox"),
                () -> ClientPacketDistributor.sendToServer(
                        AuctionInboxActionPacket.open(host.packet().houseId(), host.packet().accessPos()))
        );
        mail.place(209, 15);
        addChild(mail);
    }

    protected void addBack(Runnable action) {
        AuctionButtons.BackButton back = new AuctionButtons.BackButton(getUiFrame(), action);
        back.place(5, 45);
        addChild(back);
    }

    protected void addPager(int page, int pages, Runnable previous, Runnable next) {
        if (page > 0) {
            AuctionButtons.HotspotButton prev = new AuctionButtons.HotspotButton(
                    getUiFrame(), Component.literal("Previous page"), previous
            );
            prev.place(51, 163, 22, 14);
            addChild(prev);
        }
        if (page + 1 < pages) {
            AuctionButtons.HotspotButton nextButton = new AuctionButtons.HotspotButton(
                    getUiFrame(), Component.literal("Next page"), next
            );
            nextButton.place(160, 163, 22, 14);
            addChild(nextButton);
        }
    }

    protected void drawHeaderTitle(GuiGraphicsExtractor graphics, Component title) {
        AuctionUi.centeredFitted(graphics, title, 128.0F, 25.0F, 70.0F, 0.74F, AuctionUi.TEXT);
    }

    protected void drawPage(GuiGraphicsExtractor graphics, int page, int pages) {
        AuctionUi.centeredFitted(graphics, (page + 1) + " / " + pages, 117.0F, 168.0F, 78.0F, 0.66F, AuctionUi.TEXT);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        header.renderAt(graphics, 76, 0);
        body.renderAt(graphics, 0, 40);
    }
}
