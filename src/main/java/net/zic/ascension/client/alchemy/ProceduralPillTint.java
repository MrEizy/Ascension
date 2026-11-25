package net.zic.ascension.client.alchemy;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalProfile;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalRegistries;
import net.zic.ascension.common.item.components.AscensionComponents;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public record ProceduralPillTint() implements ItemTintSource {
    public static final MapCodec<ProceduralPillTint> CODEC = MapCodec.unit(ProceduralPillTint::new);
    private static final int FALLBACK_COLOR = 0xBCB897;

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(AscensionCraft.prefix("procedural_pill"), CODEC);
    }

    @Override
    public MapCodec<ProceduralPillTint> type() {
        return CODEC;
    }

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        MedicinalProfile profile = stack.get(AscensionComponents.MEDICINAL_PILL.get());
        if (profile == null || profile.effects().isEmpty()) return opaque(FALLBACK_COLOR);

        MedicinalProfile.Effect primary = profile.effects().getFirst();
        for (MedicinalProfile.Effect effect : profile.effects()) {
            if (!effect.adverse()) {
                primary = effect;
                break;
            }
        }
        Identifier id = primary.id();
        ClientLevel context = level != null ? level : Minecraft.getInstance().level;
        if (context != null) {
            var definition = context.registryAccess().lookup(MedicinalRegistries.EFFECTS.key())
                    .flatMap(registry -> registry.get(ResourceKey.create(MedicinalRegistries.EFFECTS.key(), id)))
                    .flatMap(holder -> holder.value().pillColor());
            if (definition.isPresent()) return opaque(definition.get());
        }
        return opaque(defaultColor(id));
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    private static int defaultColor(Identifier effect) {
        if (!AscensionCraft.MOD_ID.equals(effect.getNamespace())) return FALLBACK_COLOR;
        return switch (effect.getPath()) {
            case "qi_restoration" -> 0x375BEF;
            case "qi_circulation" -> 0x557DFA;
            case "health_restoration" -> 0x37EFED;
            case "vitality_renewal" -> 0x67F7F6;
            case "essence_cultivation" -> 0xFD9500;
            case "body_cultivation" -> 0xB52619;
            case "body_reinforcement" -> 0xE64C40;
            case "soul_cultivation" -> 0xBCB897;
            case "medicinal_cleansing" -> 0x2FF776;
            case "deep_purification" -> 0x2FF7B8;
            case "stamina_restoration" -> 0xA9F72F;
            case "stamina_vigor" -> 0xEFAF37;
            case "sustenance" -> 0xEF7F37;
            case "medicinal_toxicity" -> 0x92FF19;
            case "meridian_burn" -> 0xF8B114;
            case "meridian_corruption" -> 0x7019FF;
            default -> FALLBACK_COLOR;
        };
    }
}
