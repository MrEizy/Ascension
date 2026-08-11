package net.zic.ascension.common.gui.screens;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.screen.EasyScreen;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.elements.auction.AuctionContainer;
import net.zic.ascension.network.auction.AuctionScreenPacket;

public final class AuctionScreen extends EasyScreen {
    public AuctionScreen(AuctionScreenPacket packet) {
        super(Component.translatable("gui.ascension.auction.title"));
        UIFrame frame = getUIFrame();
        frame.setPauseGame(false);
        frame.setRoot(new AuctionContainer(frame, packet));
    }
}
