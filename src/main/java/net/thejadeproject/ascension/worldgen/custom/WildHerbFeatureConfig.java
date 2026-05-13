package net.thejadeproject.ascension.worldgen.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public record WildHerbFeatureConfig(Block cropBlock, List<Block> validGround) implements FeatureConfiguration {

    public static final Codec<WildHerbFeatureConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.BLOCK.byNameCodec()
                    .fieldOf("crop_block")
                    .forGetter(WildHerbFeatureConfig::cropBlock),
            BuiltInRegistries.BLOCK.byNameCodec()
                    .listOf()
                    .fieldOf("valid_ground")
                    .forGetter(WildHerbFeatureConfig::validGround)
    ).apply(inst, WildHerbFeatureConfig::new));
}
