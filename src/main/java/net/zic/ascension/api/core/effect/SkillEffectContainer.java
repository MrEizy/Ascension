package net.zic.ascension.api.core.effect;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;

public final class SkillEffectContainer {
    public static final Codec<SkillEffectContainer> CODEC = SkillEffectInstance.CODEC.listOf().xmap(
            SkillEffectContainer::new,
            SkillEffectContainer::instances
    );

    private final List<SkillEffectInstance> instances;

    public SkillEffectContainer() {
        this.instances = new ArrayList<>();
    }

    private SkillEffectContainer(List<SkillEffectInstance> instances) {
        this.instances = new ArrayList<>(instances);
    }

    public List<SkillEffectInstance> instances() {
        return instances;
    }
}
