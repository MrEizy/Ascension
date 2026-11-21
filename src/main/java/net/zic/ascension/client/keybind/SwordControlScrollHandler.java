package net.zic.ascension.client.keybind;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.skill.castable.SwordControlService;
import net.zic.ascension.network.SwordControlDistancePacket;
import net.zic.ascension.skill_casting.SkillCaster;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class SwordControlScrollHandler {
    private SwordControlScrollHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.isCanceled() || event.getScrollDeltaY() == 0.0D || mc.player == null
                || mc.screen != null || mc.level == null) {
            return;
        }
        SkillCaster caster = SkillCaster.getExisting(mc.player);
        if (caster == null || !SwordControlService.isControlSkill(caster.getCastingSkill())) {
            return;
        }
        event.setCanceled(true);
        ClientPacketDistributor.sendToServer(new SwordControlDistancePacket(event.getScrollDeltaY() > 0.0D ? 1 : -1));
    }
}
