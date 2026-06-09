package net.zic.ascension.skill_casting;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.input.InputHandler;
import net.zic.zenithlib.input.MappingHandler;
import net.zic.zenithlib.input.action.ActionEvent;
import net.zic.zenithlib.input.action.ActionHandler;
import net.zic.zenithlib.input.action.PlayerActionManager;
import org.lwjgl.glfw.GLFW;

/**
 * handles the cast input for skill cast handler
 *
 * will call castSelectedSkill on both the client and the server
 *
 * does not handle action end, since the skill and cast instance handle that
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
class Listener {
    private static final Identifier skillCast = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"skill_cast");
    private static final MappingHandler handler = InputHandler.registerAction(
            skillCast,
            new KeyMapping(
                    "key.ascension.skill.skill_cast",
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    KeyMapping.Category.MISC
            )
    );
    @SubscribeEvent
    public static void onActionStart(ActionEvent.Start event){
        if(!event.getAction().equals(skillCast)) return;
        SkillCastHandler skillCastHandler = event.getPlayer().getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        skillCastHandler.castSelectedSkill();
        AscensionCraft.LOGGER.debug("Skill cast on side {}",(event.getPlayer().level().isClientSide()?"Client":"Server"));
    }
}
