package net.zic.ascension.client.tooltip.providers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction.LinkedContainerData;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionAscendant;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviders;
import net.zic.zenithlib.tooltip.api.element.DividerElement;
import net.zic.zenithlib.tooltip.api.element.HeaderElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adds stack-dependent state to the Tablet of Destruction tooltips.
 */
public final class AscensionTabletTooltipProvider
        implements ZenithTooltipProviders.Provider {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "tablet_tooltips"
    );

    private static final int PRIMARY_PAGE_INDEX = 0;
    private static final int HEAVENLY_FUNCTIONS_PAGE_INDEX = 1;

    private AscensionTabletTooltipProvider() {}

    public static void register() {
        ZenithTooltipProviders.register(
                ID,
                new AscensionTabletTooltipProvider()
        );
    }

    @Override
    public Optional<ZenithTooltipDocument> create(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess
    ) {
        if (!(stack.getItem() instanceof BaseTabletOfDestruction tablet)) {
            return Optional.empty();
        }

        ZenithTooltipDocument baseDocument =
                ZenithTooltipRepository.get(stack, itemId);

        if (baseDocument == null || baseDocument.pages().isEmpty()) {
            return Optional.empty();
        }

        if (!tablet.supportsDropBlocks()
                && !tablet.supportsContainerLinking()) {

            return Optional.empty();
        }

        List<ZenithTooltipPage> pages =
                new ArrayList<>(baseDocument.pages());

        addStateSummary(pages, tablet, stack);

        if (tablet.supportsContainerLinking()) {
            addLinkedStorageDetails(pages, tablet, stack);
        }

        return Optional.of(new ZenithTooltipDocument(
                baseDocument.theme(),
                pages,
                baseDocument.animationPresets()
        ));
    }


    private static void addStateSummary(
            List<ZenithTooltipPage> pages,
            BaseTabletOfDestruction tablet,
            ItemStack stack
    ) {
        ZenithTooltipPage firstPage = pages.get(PRIMARY_PAGE_INDEX);

        List<ZenithTooltipElement> elements =
                new ArrayList<>(firstPage.elements());

        elements.add(new DividerElement());
        elements.add(HeaderElement.literal(
                "Current State",
                ZenithTooltipColor.ACCENT
        ));

        if (tablet.supportsDropBlocks()) {
            addDropModeSummary(elements, tablet, stack);
        }

        if (tablet.supportsContainerLinking()) {
            addLinkedStorageSummary(elements, tablet, stack);
        }

        if (tablet instanceof TabletOfDestructionAscendant ascendantTablet) {
            addAscendantShapeSummary(elements, ascendantTablet, stack);
        }

        pages.set(
                PRIMARY_PAGE_INDEX,
                new ZenithTooltipPage(
                        firstPage.title(),
                        elements
                )
        );
    }

    private static void addDropModeSummary(
            List<ZenithTooltipElement> elements,
            BaseTabletOfDestruction tablet,
            ItemStack stack
    ) {
        boolean collecting =
                tablet.getDropMode(stack)
                        == BaseTabletOfDestruction.DROP_ON;

        elements.add(RowElement.literal(
                "Drop Mode",
                collecting ? "Collect Blocks" : "Destroy Only",
                ZenithTooltipColor.TEXT,
                collecting
                        ? ZenithTooltipColor.POSITIVE
                        : ZenithTooltipColor.WARNING
        ));
    }

    private static void addLinkedStorageSummary(
            List<ZenithTooltipElement> elements,
            BaseTabletOfDestruction tablet,
            ItemStack stack
    ) {
        LinkedContainerData link = tablet.getLinkedContainer(stack);
        boolean linked = link.pos() != null;

        elements.add(RowElement.literal(
                "Linked Storage",
                linked ? "Linked" : "Not Linked",
                ZenithTooltipColor.TEXT,
                linked
                        ? ZenithTooltipColor.POSITIVE
                        : ZenithTooltipColor.MUTED
        ));
    }

    private static void addLinkedStorageDetails(
            List<ZenithTooltipPage> pages,
            BaseTabletOfDestruction tablet,
            ItemStack stack
    ) {
        if (pages.size() <= HEAVENLY_FUNCTIONS_PAGE_INDEX) {
            return;
        }

        ZenithTooltipPage storagePage =
                pages.get(HEAVENLY_FUNCTIONS_PAGE_INDEX);

        List<ZenithTooltipElement> elements =
                new ArrayList<>(storagePage.elements());

        int insertionIndex = findFirstDivider(elements);

        List<ZenithTooltipElement> dynamicDetails =
                createLinkedStorageDetails(tablet, stack);

        elements.addAll(insertionIndex, dynamicDetails);

        pages.set(
                HEAVENLY_FUNCTIONS_PAGE_INDEX,
                new ZenithTooltipPage(
                        storagePage.title(),
                        elements
                )
        );
    }

    private static int findFirstDivider(
            List<ZenithTooltipElement> elements
    ) {
        for (int index = 0; index < elements.size(); index++) {
            if (elements.get(index) instanceof DividerElement) {
                return index;
            }
        }

        return elements.size();
    }

    private static List<ZenithTooltipElement> createLinkedStorageDetails(
            BaseTabletOfDestruction tablet,
            ItemStack stack
    ) {
        List<ZenithTooltipElement> details = new ArrayList<>();

        LinkedContainerData link = tablet.getLinkedContainer(stack);

        if (link.pos() == null) {
            details.add(RowElement.literal(
                    "Status",
                    "Not Linked",
                    ZenithTooltipColor.TEXT,
                    ZenithTooltipColor.MUTED
            ));

            return details;
        }

        LinkDisplay display = resolveLinkDisplay(link);

        details.add(RowElement.literal(
                "Container",
                display.name(),
                ZenithTooltipColor.TEXT,
                display.color()
        ));

        details.add(RowElement.literal(
                "Position",
                formatPosition(link.pos()),
                ZenithTooltipColor.TEXT,
                ZenithTooltipColor.ACCENT
        ));

        if (link.dimension() != null && !link.dimension().isBlank()) {
            details.add(RowElement.literal(
                    "Dimension",
                    link.dimension(),
                    ZenithTooltipColor.TEXT,
                    ZenithTooltipColor.MUTED
            ));
        }

        return details;
    }

    private static LinkDisplay resolveLinkDisplay(
            LinkedContainerData link
    ) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null
                || link.pos() == null
                || link.dimension() == null) {
            return new LinkDisplay(
                    "Linked",
                    ZenithTooltipColor.ACCENT
            );
        }

        String currentDimension =
                level.dimension().identifier().toString();

        if (!currentDimension.equals(link.dimension())) {
            return new LinkDisplay(
                    "Linked in Another Dimension",
                    ZenithTooltipColor.ACCENT
            );
        }

        if (!level.hasChunkAt(link.pos())) {
            return new LinkDisplay(
                    "Linked, Area Unloaded",
                    ZenithTooltipColor.WARNING
            );
        }

        BlockState state = level.getBlockState(link.pos());

        if (state.isAir()) {
            return new LinkDisplay(
                    "Missing",
                    ZenithTooltipColor.NEGATIVE
            );
        }

        return new LinkDisplay(
                state.getBlock().getName().getString(),
                ZenithTooltipColor.POSITIVE
        );
    }

    private static void addAscendantShapeSummary(
            List<ZenithTooltipElement> elements,
            TabletOfDestructionAscendant tablet,
            ItemStack stack
    ) {
        TabletOfDestructionAscendant.MineShape shape =
                tablet.getShapeForRenderer(stack);

        elements.add(RowElement.literal(
                "Shape",
                shapeDisplayName(shape),
                ZenithTooltipColor.TEXT,
                ZenithTooltipColor.ACCENT
        ));
    }

    private static String shapeDisplayName(
            TabletOfDestructionAscendant.MineShape shape
    ) {
        return switch (shape) {
            case SHAPELESS -> "Shapeless";
            case TUNNEL -> "Mining Tunnel";
            case ESCAPE -> "Escape Tunnel";
            case DOME -> "Dome";
        };
    }

    private static String formatPosition(BlockPos pos) {
        return pos.getX()
                + ", "
                + pos.getY()
                + ", "
                + pos.getZ();
    }

    private record LinkDisplay(
            String name,
            ZenithTooltipColor color
    ) {}
}