package net.zic.ascension.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    private static final KeyMapping.Category ASCENSION_CATEGORY = KeyMapping.Category.register(
            Identifier.parse("ascension")
    );

    public static final KeyMapping CYCLE_MODE = new KeyMapping(
            "key.ascension.cycle_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            ASCENSION_CATEGORY
    );

    public static final KeyMapping OPEN_INTROSPECTION = new KeyMapping(
            "key.ascension.open_introspection",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            ASCENSION_CATEGORY
    );
}
