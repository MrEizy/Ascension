package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.impl.core.technique.LostTechnique;
import net.zic.ascension.impl.core.technique.PageConfig;

/**
 * ascension:lost_technique. Every field of simple_technique plus a required "pages" object.
 */
public class LostTechniqueType extends TechniqueType {
    @Override
    public MapCodec<? extends Technique<?>> codec() {
        return RecordCodecBuilder.<LostTechnique>mapCodec(instance -> instance.group(
                SimpleTechniqueCodecs.simple().forGetter((LostTechnique technique) -> technique),
                PageConfig.CODEC.fieldOf("pages").forGetter(LostTechnique::pages)
        ).apply(instance, LostTechnique::new));
    }
}