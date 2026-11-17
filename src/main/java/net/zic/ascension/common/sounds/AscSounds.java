package net.zic.ascension.common.sounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;

public class AscSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, AscensionCraft.MOD_ID);

    // ── Chakram sounds ────────────────────────────────────────────────────
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAKRAM_THROW =
            sound("chakram.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAKRAM_RICOCHET =
            sound("chakram.ricochet");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAKRAM_HIT =
            sound("chakram.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAKRAM_RETURN =
            sound("chakram.return");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAKRAM_CATCH =
            sound("chakram.catch");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAKRAM_THUD =
            sound("chakram.thud");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(AscensionCraft.prefix(name)));
    }

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}