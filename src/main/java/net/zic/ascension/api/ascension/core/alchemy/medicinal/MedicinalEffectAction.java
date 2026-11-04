package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Optional;

/**
 * Maps a resolved medicinal effect identifier to a server-side gameplay operation.
 * Datapacks control the operation and scaling without introducing a class per medicine.
 */
public record MedicinalEffectAction(String action, double scale, Optional<Identifier> status, Optional<Identifier> effect, Optional<Identifier> path) {
    public static final Codec<MedicinalEffectAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(MedicinalEffectAction::action),
            Codec.DOUBLE.optionalFieldOf("scale", 1.0D).forGetter(MedicinalEffectAction::scale),
            Identifier.CODEC.optionalFieldOf("status").forGetter(MedicinalEffectAction::status),
            Identifier.CODEC.optionalFieldOf("effect").forGetter(MedicinalEffectAction::effect),
            Identifier.CODEC.optionalFieldOf("path").forGetter(MedicinalEffectAction::path)
    ).apply(instance, MedicinalEffectAction::new));

    public MedicinalEffectAction {
        if (!java.util.Set.of("health", "qi", "stamina", "damage", "status", "cleanse", "skill_effect", "food", "cultivation", "reinforcement", "purify_impurities").contains(action)) {
            throw new IllegalArgumentException("Unknown medicinal action: " + action);
        }
        scale = Double.isFinite(scale) ? Mth.clamp(scale, 0.0D, 10.0D) : 1.0D;
        status = status == null ? Optional.empty() : status;
        effect = effect == null ? Optional.empty() : effect;
        path = path == null ? Optional.empty() : path;
        if (action.equals("status") && status.isEmpty()) {
            throw new IllegalArgumentException("Status medicinal actions require a status identifier");
        }
        if (action.equals("cultivation") && path.isEmpty()) {
            throw new IllegalArgumentException("Cultivation medicinal actions require a path identifier");
        }
        if (action.equals("skill_effect") && effect.isEmpty()) {
            throw new IllegalArgumentException("Skill effect medicinal actions require an effect identifier");
        }
    }
}
