package net.zic.ascension.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record LingzhiMushroomConfiguration(Block mushroomBlock, HolderSet<Block> validSupports) implements FeatureConfiguration {
    public static final Codec<LingzhiMushroomConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("mushroom_block").forGetter(LingzhiMushroomConfiguration::mushroomBlock),
                    RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("support_blocks").forGetter(LingzhiMushroomConfiguration::validSupports)
            ).apply(instance, LingzhiMushroomConfiguration::new));
}