package net.zic.ascension.common.gui.elements.starter;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.common.gui.elements.general.BetterButton;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.starter.StarterSelectionStage;
import net.zic.ascension.network.ChooseStarterOptionPacket;

import java.util.Collection;
import java.util.List;

public class StarterOptionButton extends BetterButton {
    public static final int PANEL_WIDTH = 176;
    public static final int PANEL_HEIGHT = 182;

    private static final int HOLDER_WIDTH = 150;
    private static final int HOLDER_HEIGHT = 26;

    private static final int HOLDER_X = 13;
    private static final int HOLDER_Y = 23;

    private static final int ICON_X = 21;
    private static final int ICON_Y = 28;

    private static final int NAME_CENTER_X = 98;
    private static final int NAME_Y = 33;
    private static final int NAME_MAX_WIDTH = 104;

    private static final int DESCRIPTION_CENTER_X = PANEL_WIDTH / 2;
    private static final int DESCRIPTION_TOP = 70;
    private static final int DESCRIPTION_HEIGHT = 86;
    private static final int DESCRIPTION_WRAP_WIDTH = PANEL_WIDTH - 32;
    private static final int DESCRIPTION_LINE_HEIGHT = 10;
    private static final int DESCRIPTION_MAX_LINES = 8;

    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int DESCRIPTION_COLOR = 0xFFE6E0D4;

    private static final int PATHS_CENTER_X = PANEL_WIDTH / 2;
    private static final int PATHS_Y = 58;
    private static final int PATHS_MAX_WIDTH = PANEL_WIDTH - 32;

    private static final int PHYSIQUE_DESCRIPTION_TOP = 76;
    private static final int PHYSIQUE_DESCRIPTION_HEIGHT = 80;

    private static final int PATHS_COLOR = 0xFFD8C28F;

    private static final Identifier PANEL_TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/selection_screens/selection_screen.png"
    );

    private static final Identifier HOLDER_TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/selection_screens/text_holder.png"
    );

    private static final ITextureData PANEL = new TextureDataSubsection(
            PANEL_TEXTURE, PANEL_WIDTH, PANEL_HEIGHT,
            0, 0, PANEL_WIDTH, PANEL_HEIGHT
    );

    private static final ITextureData HOLDER = new TextureDataSubsection(
            HOLDER_TEXTURE, HOLDER_WIDTH, HOLDER_HEIGHT,
            0, 0, HOLDER_WIDTH, HOLDER_HEIGHT
    );

    private final StarterSelectionStage stage;
    private final Identifier optionId;
    private final ItemStack iconStack;
    private final Component name;
    private final Component description;
    private final float scale;
    private final Component pathsText;

    public StarterOptionButton(
            UIFrame frame,
            StarterSelectionStage stage,
            Identifier optionId,
            Identifier selectedBloodline,
            float scale
    ) {
        super(frame, 0, 0);
        this.stage = stage;
        this.optionId = optionId;
        this.scale = scale;
        this.iconStack = createIconStack(stage, optionId);
        this.name = resolveName(stage, optionId);
        this.description = resolveDescription(stage, optionId);
        this.pathsText = resolvePaths(stage, optionId);

        setWidth(widthFor(scale));
        setHeight(heightFor(scale));
    }

    public static int widthFor(float scale) {
        return Math.round(PANEL_WIDTH * scale);
    }

    public static int heightFor(float scale) {
        return Math.round(PANEL_HEIGHT * scale);
    }

    private static ItemStack createIconStack(StarterSelectionStage stage, Identifier optionId) {
        ItemStack stack = new ItemStack(stage == StarterSelectionStage.PHYSIQUE
                ? ModItems.PHYSIQUE_ESSENCE.get()
                : ModItems.BLOODLINE_ESSENCE.get());

        stack.set(AscensionComponents.REGISTRY_ID_HOLDER.get(), optionId);

        if (stage == StarterSelectionStage.BLOODLINE) {
            stack.set(AscensionComponents.PURITY.get(), 100);
        }

        return stack;
    }

    private static Component resolveName(StarterSelectionStage stage, Identifier id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Component.literal(id.toString());
        }

        if (stage == StarterSelectionStage.PHYSIQUE) {
            Physique physique = CoreRegistries.safeAccess(
                    CoreRegistries.PHYSIQUE_REGISTRY,
                    id,
                    minecraft.player.registryAccess()
            );
            return physique == null ? Component.literal(id.toString()) : physique.name();
        }

        Bloodline bloodline = CoreRegistries.safeAccess(
                CoreRegistries.BLOODLINE_REGISTRY,
                id,
                minecraft.player.registryAccess()
        );
        return bloodline == null ? Component.literal(id.toString()) : bloodline.getName();
    }

    private static Component resolveDescription(StarterSelectionStage stage, Identifier id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Component.empty();
        }

        if (stage == StarterSelectionStage.PHYSIQUE) {
            Physique physique = CoreRegistries.safeAccess(
                    CoreRegistries.PHYSIQUE_REGISTRY,
                    id,
                    minecraft.player.registryAccess()
            );
            return physique == null || physique.description() == null
                    ? Component.empty()
                    : physique.description();
        }

        Bloodline bloodline = CoreRegistries.safeAccess(
                CoreRegistries.BLOODLINE_REGISTRY,
                id,
                minecraft.player.registryAccess()
        );
        return bloodline == null || bloodline.getDescription() == null
                ? Component.empty()
                : bloodline.getDescription();
    }

    @Override
    public void onClick() {
        ClientPacketDistributor.sendToServer(new ChooseStarterOptionPacket(stage, optionId));
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        PANEL.render(graphics);
        HOLDER.renderAt(graphics, HOLDER_X, HOLDER_Y);
        graphics.item(iconStack, ICON_X, ICON_Y);

        renderName(graphics);
        renderPaths(graphics);
        renderDescription(graphics);

        graphics.pose().popMatrix();
    }

    private void renderName(GuiGraphicsExtractor graphics) {
        Font font = Minecraft.getInstance().font;

        float nameScale = 1.0F;
        int nameWidth = font.width(name);
        if (nameWidth > NAME_MAX_WIDTH) {
            nameScale = NAME_MAX_WIDTH / (float) nameWidth;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(NAME_CENTER_X, NAME_Y);
        graphics.pose().scale(nameScale, nameScale);
        graphics.centeredText(font, name, 0, 0, NAME_COLOR);
        graphics.pose().popMatrix();
    }

    private void renderDescription(GuiGraphicsExtractor graphics) {
        Font font = Minecraft.getInstance().font;
        int descriptionTop = stage == StarterSelectionStage.PHYSIQUE ? PHYSIQUE_DESCRIPTION_TOP : DESCRIPTION_TOP;
        int descriptionHeight = stage == StarterSelectionStage.PHYSIQUE ? PHYSIQUE_DESCRIPTION_HEIGHT : DESCRIPTION_HEIGHT;

        List<FormattedCharSequence> lines = font.split(description, DESCRIPTION_WRAP_WIDTH);

        int lineCount = Math.min(lines.size(), DESCRIPTION_MAX_LINES);
        int blockHeight = lineCount * DESCRIPTION_LINE_HEIGHT;
        int y = descriptionTop + (descriptionHeight - blockHeight) / 2;

        for (int i = 0; i < lineCount; i++) {
            FormattedCharSequence line = lines.get(i);
            int x = DESCRIPTION_CENTER_X - font.width(line) / 2;

            graphics.text(font, line, x, y + i * DESCRIPTION_LINE_HEIGHT, DESCRIPTION_COLOR, false);
        }
    }

    private static Component resolvePaths(StarterSelectionStage stage, Identifier id) {
        if (stage != StarterSelectionStage.PHYSIQUE) {
            return Component.empty();
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Component.empty();
        }

        Physique physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, id, minecraft.player.registryAccess());

        if (physique == null) {
            return Component.empty();
        }

        Collection<Identifier> unlockedPaths = physique.unlockedPaths();
        if (unlockedPaths.isEmpty()) {
            return Component.empty();
        }

        MutableComponent pathNames = Component.empty();
        int index = 0;

        for (Identifier pathId : unlockedPaths) {
            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, minecraft.player.registryAccess());
            Component pathName = path == null ? fallbackPathName(pathId) : resolvePathName(path);

            if (index > 0) {
                pathNames.append(Component.literal(" • "));
            }

            pathNames.append(pathName);
            index++;
        }

        return Component.translatable(
                "gui.ascension.starter.physique_paths",
                pathNames
        );
    }

    private static Component resolvePathName(Path path) {
        return path.name();
    }

    private static Component fallbackPathName(Identifier pathId) {
        String path = pathId.getPath();
        String readable = path.replace('_', ' ').replace('/', ' ');

        if (readable.isEmpty()) {
            return Component.literal(pathId.toString());
        }

        return Component.literal(Character.toUpperCase(readable.charAt(0)) + readable.substring(1)
        );
    }

    private void renderPaths(GuiGraphicsExtractor graphics) {
        if (stage != StarterSelectionStage.PHYSIQUE || pathsText.getString().isBlank()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        float pathScale = 1.0F;
        int pathWidth = font.width(pathsText);

        if (pathWidth > PATHS_MAX_WIDTH) {
            pathScale = PATHS_MAX_WIDTH / (float) pathWidth;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(PATHS_CENTER_X, PATHS_Y);
        graphics.pose().scale(pathScale, pathScale);
        graphics.centeredText(font, pathsText, 0, 0, PATHS_COLOR);
        graphics.pose().popMatrix();
    }
}