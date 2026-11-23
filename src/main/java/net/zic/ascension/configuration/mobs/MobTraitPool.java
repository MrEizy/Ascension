package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

public record MobTraitPool(Identifier id, int rolls, boolean exclusive, List<MobTraitPoolEntry> entries) {
    public static final Codec<MobTraitPool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(MobTraitPool::id),
            Codec.intRange(0, 32).optionalFieldOf("rolls", 1).forGetter(MobTraitPool::rolls),
            Codec.BOOL.optionalFieldOf("exclusive", true).forGetter(MobTraitPool::exclusive),
            MobTraitPoolEntry.CODEC.listOf().fieldOf("entries").forGetter(MobTraitPool::entries)
    ).apply(instance, MobTraitPool::new));
}
