package net.thejadeproject.ascension.refactor_packages.techniques.custom.body;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.GenericTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers.BasicStatChangeHandler;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data.BodyTechniqueData;

import java.util.Set;

public class BodyElementTechnique extends GenericTechnique {
    private final ResourceLocation element;
    private final ResourceLocation skillId;

    public BodyElementTechnique(ResourceLocation element, Component title, double baseRate, BasicStatChangeHandler handler, ResourceLocation skillId) {
        super(ModPaths.BODY.getId(), title, baseRate, Set.of());
        this.element = element;
        this.skillId = skillId;
        setStatChangeHandler(handler);
    }

    public ResourceLocation getElement() { return element; }

    @Override
    public boolean isCompatibleWith(ResourceLocation technique) {
        var tech = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique);
        return !(tech instanceof FiveElementBodyTechnique);
    }

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        heldEntity.giveSkill(skillId, ModForms.MORTAL_VESSEL.getId());
        heldEntity.getPathBonusHandler().addPathBonus(ModPaths.BODY.getId(), 1.0D);
        refreshUniversalTechniqueSkills(heldEntity);
    }

    @Override
    public void onTechniqueRemoved(IEntityData heldEntity, ITechniqueData techniqueData) {
        PathData pathData = heldEntity.getPathData(getPath());
        if (pathData != null) {
            pathData.handleRealmChange(pathData.getMajorRealm(), 0, heldEntity);
        }
        heldEntity.getPathBonusHandler().removePathBonus(ModPaths.BODY.getId(), 1.0D);
        heldEntity.removeSkill(skillId, ModForms.MORTAL_VESSEL.getId());
        refreshUniversalTechniqueSkills(heldEntity);
    }

    @Override
    public ITechniqueData freshTechniqueData(IEntityData heldEntity) {
        return new BodyTechniqueData();
    }

    @Override
    public ITechniqueData fromCompound(CompoundTag tag) {
        return BodyTechniqueData.read(tag);
    }

    @Override
    public ITechniqueData fromNetwork(RegistryFriendlyByteBuf buf) {
        return BodyTechniqueData.decode(buf);
    }
}
