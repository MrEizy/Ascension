package net.zic.ascension.common.effects;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;

/**
 * Vanilla mob effects used to show skill effects in the inventory and HUD.
 * They carry no behaviour themselves; the matching skill effect applies the actual effect
 * and keeps these in sync through the ascension:status_icon module.
 */
@SuppressWarnings("unused") //registered for their side effect, referenced by id from the skill effect json
public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, AscensionCraft.MOD_ID);

    public static final Holder<MobEffect> REJECTION = MOB_EFFECTS.register("rejection",
            () -> new DisplayOnlyEffect(MobEffectCategory.HARMFUL, 0xC9A23F));
    public static final Holder<MobEffect> OPPRESSION = MOB_EFFECTS.register("oppression",
            () -> new DisplayOnlyEffect(MobEffectCategory.HARMFUL, 0xC2562B));
    public static final Holder<MobEffect> SUPPRESSION = MOB_EFFECTS.register("suppression",
            () -> new DisplayOnlyEffect(MobEffectCategory.HARMFUL, 0x7A1F3D));

    private ModEffects() {
    }

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }

    public static class DisplayOnlyEffect extends MobEffect {
        public DisplayOnlyEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}
