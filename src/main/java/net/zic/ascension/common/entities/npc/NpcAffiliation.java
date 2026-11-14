package net.zic.ascension.common.entities.npc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.common.util.ModTags;

import java.util.Locale;

public enum NpcAffiliation {
    ORTHODOX, UNORTHODOX, DEMONIC;

    public String id() { return name().toLowerCase(Locale.ROOT); }

    public String label() {
        return switch (this) {
            case ORTHODOX -> "Orthodox";
            case UNORTHODOX -> "Unorthodox";
            case DEMONIC -> "Demonic";
        };
    }

    public static NpcAffiliation parse(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "orthodox" -> ORTHODOX;
            case "unorthodox" -> UNORTHODOX;
            case "demonic" -> DEMONIC;
            default -> null;
        };
    }

    public static NpcAffiliation of(LivingEntity entity) {
        if (entity instanceof CultivatorNpcEntity npc) return npc.getAffiliation();

        var type = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType());

        if (type.is(ModTags.EntityTypes.ORTHODOX)) return ORTHODOX;
        if (type.is(ModTags.EntityTypes.UNORTHODOX)) return UNORTHODOX;
        if (type.is(ModTags.EntityTypes.DEMONIC)) return DEMONIC;

        return null;
    }
}
