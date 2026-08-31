package net.zic.ascension.api.ascension.core.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public record TechniqueSkillDefinition(int unlock, RequirementHolder requirements, Map<Integer, TechniqueSkillCap> caps) {
    private static final Codec<Integer> REALM_KEY_CODEC = Codec.STRING.xmap(Integer::parseInt, Object::toString);

    public static final Codec<TechniqueSkillDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("unlock", 0).forGetter(TechniqueSkillDefinition::unlock),
            RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(TechniqueSkillDefinition::requirements),
            Codec.unboundedMap(REALM_KEY_CODEC, TechniqueSkillCap.CODEC).optionalFieldOf("caps", Map.of()).forGetter(TechniqueSkillDefinition::caps)
    ).apply(instance, TechniqueSkillDefinition::new));

    public TechniqueSkillDefinition {
        unlock = Math.max(0, unlock);
        requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
        TreeMap<Integer, TechniqueSkillCap> sortedCaps = new TreeMap<>();
        if (caps != null) {
            caps.forEach((realm, cap) -> {
                if (realm != null && realm >= 0 && cap != null) {
                    sortedCaps.put(realm, cap);
                }
            });
        }
        caps = Map.copyOf(sortedCaps);
    }

    public Optional<TechniqueSkillCap> capAt(int majorRealm) {
        TechniqueSkillCap result = null;
        int bestRealm = Integer.MIN_VALUE;
        for (Map.Entry<Integer, TechniqueSkillCap> entry : caps.entrySet()) {
            if (entry.getKey() <= majorRealm && entry.getKey() > bestRealm) {
                bestRealm = entry.getKey();
                result = entry.getValue();
            }
        }
        return Optional.ofNullable(result);
    }
}
