package net.zic.ascension.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.zenithlib.input.InputHandler;
import net.zic.zenithlib.input.MappingHandler;
import org.lwjgl.glfw.GLFW;

public final class ClientSkillCastKeybind {

    private static MappingHandler handler;

    private ClientSkillCastKeybind() {
    }

    public static void register() {
        if (handler != null) {return;}

        handler = InputHandler.registerAction(AscensionSkillListener.skillCast,
                new KeyMapping("key.ascension.skill.skill_cast",
                        KeyConflictContext.IN_GAME,
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        ModKeybinds.ASCENSION_CATEGORY
                )
        );
    }
}