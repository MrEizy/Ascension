package net.thejadeproject.ascension.refactor_packages.physiques.custom;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ElementalBodyPhysique implements IPhysique {

    private final ResourceLocation element;
    private final Component title;

    public ElementalBodyPhysique(ResourceLocation element, Component title) {
        this.element = element;
        this.title   = title;
    }

    public ResourceLocation getElement() { return element; }

    public static double bonusForTier(int tier) {
        return switch (tier) {
            case 1 -> 1.5;
            case 2 -> 2.25;
            case 3 -> 3.0;
            case 4 -> 4.0;
            default -> 5.0;
        };
    }

    @Override
    public void onPhysiqueAdded(IEntityData heldEntity, ResourceLocation oldPhysique, IPhysiqueData oldPhysiqueData) {
        double bonus = bonusForTier(1);
        heldEntity.getPathBonusHandler().addPathBonus(ModPaths.BODY.getId(), bonus);
        heldEntity.getPathBonusHandler().addPathBonus(element, bonus);
    }

    @Override
    public void onPhysiqueRemoved(IEntityData heldEntity, IPhysiqueData physiqueData, ResourceLocation newPhysique) {
        if (!(physiqueData instanceof ElementalPhysiqueData data)) return;
        double bonus = bonusForTier(data.getActiveCount());
        heldEntity.getPathBonusHandler().removePathBonus(ModPaths.BODY.getId(), bonus);
        for (ResourceLocation el : data.getActiveElements()) {
            heldEntity.getPathBonusHandler().removePathBonus(el, bonus);
        }
    }

    @Override
    public void onFormAdded(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}

    @Override
    public void onFormRemoved(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}

    @Override
    public Component getDisplayTitle() { return title; }

    public Component getDisplayTitle(ElementalPhysiqueData data) {
        Set<ResourceLocation> active = data.getActiveElements();
        return switch (active.size()) {
            case 2 -> {
                if (active.containsAll(Set.of(ModPaths.WOOD.getId(), ModPaths.FIRE.getId())))
                    yield Component.translatable("ascension.physiques.liver_heart_awakening");
                if (active.containsAll(Set.of(ModPaths.FIRE.getId(), ModPaths.EARTH.getId())))
                    yield Component.translatable("ascension.physiques.heart_spleen_awakening");
                if (active.containsAll(Set.of(ModPaths.EARTH.getId(), ModPaths.METAL.getId())))
                    yield Component.translatable("ascension.physiques.spleen_lung_awakening");
                if (active.containsAll(Set.of(ModPaths.METAL.getId(), ModPaths.WATER.getId())))
                    yield Component.translatable("ascension.physiques.lung_kidney_awakening");
                if (active.containsAll(Set.of(ModPaths.WATER.getId(), ModPaths.WOOD.getId())))
                    yield Component.translatable("ascension.physiques.kidney_liver_awakening");
                yield title;
            }
            case 3 -> {
                if (active.containsAll(Set.of(ModPaths.WOOD.getId(), ModPaths.FIRE.getId(), ModPaths.EARTH.getId())))
                    yield Component.translatable("ascension.physiques.vital_blood");
                if (active.containsAll(Set.of(ModPaths.FIRE.getId(), ModPaths.EARTH.getId(), ModPaths.METAL.getId())))
                    yield Component.translatable("ascension.physiques.refined_qi");
                if (active.containsAll(Set.of(ModPaths.EARTH.getId(), ModPaths.METAL.getId(), ModPaths.WATER.getId())))
                    yield Component.translatable("ascension.physiques.flowing_tide");
                if (active.containsAll(Set.of(ModPaths.METAL.getId(), ModPaths.WATER.getId(), ModPaths.WOOD.getId())))
                    yield Component.translatable("ascension.physiques.deep_root");
                if (active.containsAll(Set.of(ModPaths.WATER.getId(), ModPaths.WOOD.getId(), ModPaths.FIRE.getId())))
                    yield Component.translatable("ascension.physiques.rising_yang");
                yield title;
            }
            case 4 -> {
                if (!active.contains(ModPaths.WATER.getId())) yield Component.translatable("ascension.physiques.unquenched_palace");
                if (!active.contains(ModPaths.WOOD.getId()))  yield Component.translatable("ascension.physiques.unrooted_palace");
                if (!active.contains(ModPaths.FIRE.getId()))  yield Component.translatable("ascension.physiques.unlit_palace");
                if (!active.contains(ModPaths.EARTH.getId())) yield Component.translatable("ascension.physiques.ungrounded_palace");
                if (!active.contains(ModPaths.METAL.getId())) yield Component.translatable("ascension.physiques.unrefined_palace");
                yield title;
            }
            case 5  -> Component.translatable("ascension.physiques.five_palace_immortal");
            default -> title;
        };
    }

    @Override
    public Component getShortDescription() { return null; }

    @Override
    public Component getDescription() { return null; }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
         return new DescriptionDisplayContainer(frame,getDisplayTitle(),getDescription());
    }

    @Override
    public Collection<ResourceLocation> paths() { return Set.of(ModPaths.BODY.getId()); }

    @Override
    public Map<ResourceLocation, Double> pathBonuses() { return Map.of(); }

    @Override
    public IPhysiqueData freshPhysiqueData(IEntityData heldEntity) {
        ResourceLocation key = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.getKey(this);
        return ElementalPhysiqueData.forElement(key, element);
    }

    @Override
    public IPhysiqueData fromCompound(CompoundTag tag, IEntityData heldEntity) {
        return ElementalPhysiqueData.read(tag);
    }

    @Override
    public IPhysiqueData fromNetwork(RegistryFriendlyByteBuf buf) {
        return ElementalPhysiqueData.decode(buf);
    }
}
