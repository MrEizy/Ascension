package net.zic.ascension.biome.configuration;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.resources.Identifier;

/**
 * Holds configuration details for a biome,
 * these are not configurations that effect world gen
 * @param energyCap the influence this biome has on the max energy of a chunk
 * @param energyRegen the influence this biome has on the energy Regen of a chunk
 * @param affinities the affinities this biome gives a chunk
 */
public record BiomeConfiguration(double energyCap, double energyRegen, Object2DoubleMap<Identifier> affinities) {

    /**
     *  because multiple configurations can influence the same Biome we use a builder pattern
     *
     */
    public static class Builder{
        private double energyCap;
        private double energyRegen;
        private final Object2DoubleMap<Identifier> affinities =  new Object2DoubleOpenHashMap<>();

        public void setEnergyCap(double energyCap) {
            this.energyCap = Math.max(energyCap, this.energyCap);
        }
        public void setEnergyRegen(double energyRegen) {
            this.energyRegen = Math.max(energyRegen, this.energyRegen);
        }
        public void addAffinity(Identifier affinity,double value){
            affinities.put(affinity,Math.max(affinities.getOrDefault(affinity,Double.MIN_VALUE),value));
        }

        public BiomeConfiguration build(){
            return new BiomeConfiguration(energyCap, energyRegen, affinities);
        }
    }
}
