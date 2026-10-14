package net.zic.ascension.client.gui;

import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

public final class NpcDialogueStyle {
    public static final int PANEL_WIDTH = 256;
    public static final int PANEL_HEIGHT = 96;
    public static final int PANEL_Y = 22;
    public static final int NAMEPLATE_WIDTH = 128;
    public static final int NAMEPLATE_HEIGHT = 24;
    public static final int BUTTON_WIDTH = 160;
    public static final int BUTTON_HEIGHT = 20;
    public static final int ORNAMENT_SIZE = 32;

    public static final int WIDE_SCREEN_MIN_WIDTH = 624;
    public static final int WIDE_CHOICE_X = 268;
    public static final int WIDE_CHOICE_Y = 23;
    public static final int WIDE_CHOICE_STEP = 23;
    public static final int COMPACT_CHOICE_Y = 122;
    public static final int COMPACT_CHOICE_WIDTH = 124;
    public static final int COMPACT_CHOICE_STEP_X = 132;
    public static final int COMPACT_CHOICE_STEP_Y = 23;
    public static final int BOTTOM_MARGIN = 12;

    public static final int NAME_X = 43;
    public static final int NAME_Y = 8;
    public static final int NAME_MAX_WIDTH = 91;
    public static final int PANEL_TEXT_X = 12;
    public static final int PANEL_TEXT_WIDTH = 231;
    public static final int REALM_Y = 11;
    public static final int DETAILS_Y = 23;
    public static final int RELATIONSHIP_Y = 34;
    public static final int MESSAGE_Y = 49;
    public static final int MESSAGE_LINE_HEIGHT = 11;
    public static final int MESSAGE_LINES_PER_PAGE = 3;
    public static final int FOOTER_Y = 80;
    public static final float COMPACT_BUTTON_SCALE = COMPACT_CHOICE_WIDTH / (float) BUTTON_WIDTH;

    public static final int NAME_COLOR = 0xFFE6F7FF;
    public static final int TEXT_COLOR = 0xFFF2F7FA;
    public static final int SECONDARY_COLOR = 0xFF9EBAC8;
    public static final int HIGHLIGHT_COLOR = 0xFF63D7FF;
    public static final int WARNING_COLOR = 0xFFFF8888;

    public static final ITextureData PANEL = sprite("dialogue_panel.png", PANEL_WIDTH, PANEL_HEIGHT, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
    public static final ITextureData NAMEPLATE = sprite("nameplate.png", NAMEPLATE_WIDTH, NAMEPLATE_HEIGHT, 0, 0, NAMEPLATE_WIDTH, NAMEPLATE_HEIGHT);
    public static final ITextureData ORNAMENT = sprite("dialogue_ornament.png", ORNAMENT_SIZE, ORNAMENT_SIZE, 0, 0, ORNAMENT_SIZE, ORNAMENT_SIZE);
    public static final ITextureData BUTTON_NORMAL = sprite("choice_buttons.png", BUTTON_WIDTH, BUTTON_HEIGHT * 3, 0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    public static final ITextureData BUTTON_HOVER = sprite("choice_buttons.png", BUTTON_WIDTH, BUTTON_HEIGHT * 3, 0, BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);
    public static final ITextureData BUTTON_DISABLED = sprite("choice_buttons.png", BUTTON_WIDTH, BUTTON_HEIGHT * 3, 0, BUTTON_HEIGHT * 2, BUTTON_WIDTH, BUTTON_HEIGHT);

    private NpcDialogueStyle() {
    }

    private static ITextureData sprite(String file, int textureWidth, int textureHeight, int x, int y, int width, int height) {
        Identifier id = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/main/npc_dialogue/" + file);
        return new TextureDataSubsection(id, textureWidth, textureHeight, x, y, width, height);
    }
}
