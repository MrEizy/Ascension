package net.thejadeproject.ascension.refactor_packages.physiques.custom.physique_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.LinkedHashSet;
import java.util.Set;

public class ElementalPhysiqueData implements IPhysiqueData {

    private final ResourceLocation physiqueKey;
    private boolean fire;
    private boolean water;
    private boolean wood;
    private boolean earth;
    private boolean metal;

    public ElementalPhysiqueData(ResourceLocation physiqueKey,
                                  boolean fire, boolean water, boolean wood,
                                  boolean earth, boolean metal) {
        this.physiqueKey = physiqueKey;
        this.fire  = fire;
        this.water = water;
        this.wood  = wood;
        this.earth = earth;
        this.metal = metal;
    }

    public static ElementalPhysiqueData forElement(ResourceLocation physiqueKey, ResourceLocation element) {
        return new ElementalPhysiqueData(
            physiqueKey,
            element.equals(ModPaths.FIRE.getId()),
            element.equals(ModPaths.WATER.getId()),
            element.equals(ModPaths.WOOD.getId()),
            element.equals(ModPaths.EARTH.getId()),
            element.equals(ModPaths.METAL.getId())
        );
    }

    public boolean hasFire()  { return fire; }
    public boolean hasWater() { return water; }
    public boolean hasWood()  { return wood; }
    public boolean hasEarth() { return earth; }
    public boolean hasMetal() { return metal; }

    public void setFlag(ResourceLocation element, boolean value) {
        if      (element.equals(ModPaths.FIRE.getId()))  fire  = value;
        else if (element.equals(ModPaths.WATER.getId())) water = value;
        else if (element.equals(ModPaths.WOOD.getId()))  wood  = value;
        else if (element.equals(ModPaths.EARTH.getId())) earth = value;
        else if (element.equals(ModPaths.METAL.getId())) metal = value;
    }

    public boolean hasElement(ResourceLocation element) {
        if (element.equals(ModPaths.FIRE.getId()))  return fire;
        if (element.equals(ModPaths.WATER.getId())) return water;
        if (element.equals(ModPaths.WOOD.getId()))  return wood;
        if (element.equals(ModPaths.EARTH.getId())) return earth;
        if (element.equals(ModPaths.METAL.getId())) return metal;
        return false;
    }

    /** Returns active elements in generative cycle order: Wood → Fire → Earth → Metal → Water */
    public Set<ResourceLocation> getActiveElements() {
        Set<ResourceLocation> active = new LinkedHashSet<>();
        if (wood)  active.add(ModPaths.WOOD.getId());
        if (fire)  active.add(ModPaths.FIRE.getId());
        if (earth) active.add(ModPaths.EARTH.getId());
        if (metal) active.add(ModPaths.METAL.getId());
        if (water) active.add(ModPaths.WATER.getId());
        return active;
    }

    public int getActiveCount() {
        int n = 0;
        if (fire)  n++;
        if (water) n++;
        if (wood)  n++;
        if (earth) n++;
        if (metal) n++;
        return n;
    }

    @Override
    public ResourceLocation getPhysiqueKey() { return physiqueKey; }

    @Override
    public IPhysique getPhysique() {
        return AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(physiqueKey);
    }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("physiqueKey", physiqueKey.toString());
        tag.putBoolean("fire",  fire);
        tag.putBoolean("water", water);
        tag.putBoolean("wood",  wood);
        tag.putBoolean("earth", earth);
        tag.putBoolean("metal", metal);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(physiqueKey);
        buf.writeBoolean(fire);
        buf.writeBoolean(water);
        buf.writeBoolean(wood);
        buf.writeBoolean(earth);
        buf.writeBoolean(metal);
    }

    public static ElementalPhysiqueData read(CompoundTag tag) {
        return new ElementalPhysiqueData(
            ResourceLocation.parse(tag.getString("physiqueKey")),
            tag.getBoolean("fire"),
            tag.getBoolean("water"),
            tag.getBoolean("wood"),
            tag.getBoolean("earth"),
            tag.getBoolean("metal")
        );
    }

    public static ElementalPhysiqueData decode(RegistryFriendlyByteBuf buf) {
        return new ElementalPhysiqueData(
            buf.readResourceLocation(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean()
        );
    }
}
