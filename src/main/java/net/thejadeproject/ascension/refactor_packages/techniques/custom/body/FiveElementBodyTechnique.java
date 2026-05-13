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

import java.util.Set;

public class FiveElementBodyTechnique extends GenericTechnique {

    private final ResourceLocation skillId;

    public FiveElementBodyTechnique(BasicStatChangeHandler handler, ResourceLocation skillId) {
        super(ModPaths.BODY.getId(), Component.translatable("ascension.technique.five_element_body_technique"), 15.0, Set.of());
        this.skillId = skillId;
        setStatChangeHandler(handler);
    }

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        heldEntity.giveSkill(skillId, ModForms.MORTAL_VESSEL.getId());
        heldEntity.getPathBonusHandler().addPathBonus(ModPaths.BODY.getId(), 3.0D);
    }

    @Override
    public void onTechniqueRemoved(IEntityData heldEntity, ITechniqueData techniqueData) {
        PathData pathData = heldEntity.getPathData(getPath());
        if (pathData != null) {
            pathData.handleRealmChange(pathData.getMajorRealm(), 0, heldEntity);
        }
        heldEntity.getPathBonusHandler().removePathBonus(ModPaths.BODY.getId(), 3.0D);
        heldEntity.removeSkill(skillId, ModForms.MORTAL_VESSEL.getId());
    }

    @Override
    public boolean isCompatibleWith(ResourceLocation technique) {
        var tech = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique);
        return !(tech instanceof BodyElementTechnique) && !(tech instanceof CombinedBodyElementTechnique);
    }

    @Override
    public ITechniqueData freshTechniqueData(IEntityData heldEntity) { return null; }

    @Override
    public ITechniqueData fromCompound(CompoundTag tag) { return null; }

    @Override
    public ITechniqueData fromNetwork(RegistryFriendlyByteBuf buf) { return null; }
}
