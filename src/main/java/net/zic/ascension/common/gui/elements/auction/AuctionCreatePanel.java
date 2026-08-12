package net.zic.ascension.common.gui.elements.auction;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.common.auction.AuctionManager;
import net.zic.ascension.network.auction.CreateAuctionPacket;

final class AuctionCreatePanel extends AuctionPanel {
    private int selectedSlot = -1;
    private int quantity = 1;
    private long startingBid = 1L;
    private long duration = 60L * 60L * 1000L;

    AuctionCreatePanel(UIFrame frame, AuctionContainer host) {
        super(frame, host, AuctionUi.CREATE);
        addOwnerNavigation(AuctionButtons.OwnerTab.CREATE);
        addBack(host::openOwner);
        addInventoryButtons();
        addControlButtons();

        AuctionButtons.HotspotButton confirm = new AuctionButtons.HotspotButton(
                frame, Component.literal("Create auction"), this::createAuction
        );
        confirm.place(174, 100, 54, 10);
        addChild(confirm);

        AuctionButtons.TooltipHotspot previewTooltip = new AuctionButtons.TooltipHotspot(
                frame, () -> {
                    ItemStack stack = selectedStack();
                    return stack.isEmpty() ? Component.literal("Selected item") : stack.getHoverName();
                }
        );
        previewTooltip.place(174, 116, 54, 53);
        addChild(previewTooltip);
    }

    private void addInventoryButtons() {
        for (int slot = 0; slot < 36; slot++) {
            int finalSlot = slot;
            int col;
            int y;
            if (slot < 9) {
                col = slot;
                y = 139;
            } else {
                int inventoryIndex = slot - 9;
                col = inventoryIndex % 9;
                y = 81 + (inventoryIndex / 9) * 18;
            }

            AuctionButtons.ItemSlotButton button = new AuctionButtons.ItemSlotButton(
                    getUiFrame(),
                    () -> AuctionUi.inventoryStack(finalSlot),
                    () -> selectedSlot == finalSlot,
                    () -> selectSlot(finalSlot)
            );
            button.place(7 + col * 18, y);
            addChild(button);
        }
    }

    private void addControlButtons() {
        AuctionButtons.ModifierButton quantityDown = new AuctionButtons.ModifierButton(
                getUiFrame(), AuctionUi.MINUS_BUTTON, Component.literal("Quantity"),
                modifiers -> adjustQuantity(-AuctionUi.modifierInt(modifiers, 1, 8, 64))
        );
        quantityDown.place(173, 48);
        addChild(quantityDown);

        AuctionButtons.ModifierButton quantityUp = new AuctionButtons.ModifierButton(
                getUiFrame(), AuctionUi.PLUS_BUTTON, Component.literal("Quantity"),
                modifiers -> adjustQuantity(AuctionUi.modifierInt(modifiers, 1, 8, 64))
        );
        quantityUp.place(217, 48);
        addChild(quantityUp);

        AuctionButtons.ModifierButton bidDown = new AuctionButtons.ModifierButton(
                getUiFrame(), AuctionUi.MINUS_BUTTON, Component.literal("Starting bid"),
                modifiers -> startingBid = Math.max(1L,
                        startingBid - AuctionUi.modifierLong(modifiers, 1L, 10L, 100L))
        );
        bidDown.place(173, 65);
        addChild(bidDown);

        AuctionButtons.ModifierButton bidUp = new AuctionButtons.ModifierButton(
                getUiFrame(), AuctionUi.PLUS_BUTTON, Component.literal("Starting bid"),
                modifiers -> startingBid = AuctionUi.safeAdd(startingBid,
                        AuctionUi.modifierLong(modifiers, 1L, 10L, 100L))
        );
        bidUp.place(217, 65);
        addChild(bidUp);

        AuctionButtons.ModifierButton durationDown = new AuctionButtons.ModifierButton(
                getUiFrame(), AuctionUi.MINUS_BUTTON, Component.literal("Duration"),
                modifiers -> duration = Math.max(AuctionManager.MIN_DURATION_MILLIS,
                        duration - AuctionUi.durationStep(modifiers))
        );
        durationDown.place(173, 82);
        addChild(durationDown);

        AuctionButtons.ModifierButton durationUp = new AuctionButtons.ModifierButton(
                getUiFrame(), AuctionUi.PLUS_BUTTON, Component.literal("Duration"),
                modifiers -> duration = Math.min(AuctionManager.MAX_DURATION_MILLIS,
                        AuctionUi.safeAdd(duration, AuctionUi.durationStep(modifiers)))
        );
        durationUp.place(217, 82);
        addChild(durationUp);
    }

    private ItemStack selectedStack() {
        return selectedSlot < 0 ? ItemStack.EMPTY : AuctionUi.inventoryStack(selectedSlot);
    }

    private void selectSlot(int slot) {
        ItemStack stack = AuctionUi.inventoryStack(slot);
        if (stack.isEmpty()) return;
        selectedSlot = slot;
        quantity = Math.max(1, Math.min(quantity, stack.getCount()));
    }

    private void adjustQuantity(int delta) {
        if (selectedSlot < 0) return;
        ItemStack stack = AuctionUi.inventoryStack(selectedSlot);
        quantity = Math.max(1, Math.min(Math.max(1, stack.getCount()), quantity + delta));
    }

    private void createAuction() {
        if (selectedSlot < 0 || host.packet().houseId() == null) return;
        ClientPacketDistributor.sendToServer(new CreateAuctionPacket(
                host.packet().houseId(),
                host.packet().accessPos(),
                selectedSlot,
                quantity,
                startingBid,
                duration
        ));
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawHeaderTitle(graphics, Component.translatable("gui.ascension.auction.create"));

        ItemStack selected = selectedStack();
        String selectedName = selected.isEmpty()
                ? "Select an item"
                : AuctionUi.stackTitle(selected, quantity, 22);
        AuctionUi.centeredScaled(graphics, selectedName, 87.0F, 61.0F, 0.72F,
                selected.isEmpty() ? AuctionUi.MUTED : AuctionUi.TEXT);

        AuctionUi.centeredScaled(graphics, Integer.toString(quantity), 201.0F, 51.0F, 0.74F, AuctionUi.TEXT);
        AuctionUi.centeredScaled(graphics, Long.toString(startingBid), 201.0F, 68.0F, 0.74F, AuctionUi.TEXT);
        AuctionUi.centeredScaled(graphics, AuctionUi.formatDuration(duration), 201.0F, 85.0F, 0.70F, AuctionUi.TEXT);
        AuctionUi.centeredScaled(graphics, "Create", 201.0F, 102.0F, 0.68F, AuctionUi.TEXT);

        if (!selected.isEmpty()) {
            AuctionUi.itemScaled(graphics, selected, 201.0F, 142.0F, 2.5F);
        }

        AuctionUi.centeredScaled(graphics, "Shift: medium  •  Ctrl: large", 88.0F, 164.0F, 0.60F, AuctionUi.MUTED);
    }
}
