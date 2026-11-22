package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MobConfigurationInstance {

    private MobCultivationEliteTier tier = MobCultivationEliteTier.NORMAL;
    private final List<MobTraitReference> traits = new ArrayList<>();

    public MobConfigurationInstance() {
    }

    public MobConfigurationInstance(MobCultivationEliteTier tier, List<MobTraitReference> traits) {
        this.tier = tier;
        this.traits.addAll(traits);
    }

    public MobCultivationEliteTier getTier() {
        return tier;
    }

    public List<MobTraitReference> getTraits() {
        return List.copyOf(traits);
    }

    private Identifier owner(MobTraitReference trait, UUID mobId) {
        return Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                "mob_trait/" + mobId + "/" + trait.id().getNamespace() + "/" + trait.id().getPath());
    }

    public void applyToMob(Mob mob) {
        for (MobTraitReference trait : traits) {
            MobTraitDefinition definition = trait.getTrait(mob.registryAccess());
            if (definition != null) definition.applyToMob(mob);
        }
    }

    public void applyToSource(OriginSource source, UUID mobId) {
        if (source == null) return;
        for (MobTraitReference trait : traits) {
            MobTraitDefinition definition = trait.getTrait(source.getRegistryAccess());
            if (definition != null) definition.applyToSource(source, owner(trait, mobId));
        }
    }

    public void removeFromMob(Mob mob) {
        for (MobTraitReference trait : traits) {
            MobTraitDefinition definition = trait.getTrait(mob.registryAccess());
            if (definition != null) definition.removeFromMob(mob);
        }
    }

    public void removeFromSource(OriginSource source, UUID mobId) {
        if (source == null) return;
        for (MobTraitReference trait : traits) {
            MobTraitDefinition definition = trait.getTrait(source.getRegistryAccess());
            if (definition != null) definition.removeFromSource(source, owner(trait, mobId));
        }
    }

    public void write(ValueOutput output, RegistryAccess access) {
        output.putString("tier", tier.name());
        ValueOutput.ValueOutputList list = output.childrenList("traits");
        for (MobTraitReference trait : traits) {
            trait.write(list.addChild());
        }
    }

    public void read(ValueInput input, RegistryAccess access) {
        try {
            tier = MobCultivationEliteTier.valueOf(input.getStringOr("tier", "NORMAL"));
        } catch (IllegalArgumentException exception) {
            tier = MobCultivationEliteTier.NORMAL;
        }
        traits.clear();
        for (ValueInput traitInput : input.childrenListOrEmpty("traits")) {
            try {
                MobTraitReference reference = MobTraitReference.of(traitInput);
                if (!traits.contains(reference)) traits.add(reference);
            } catch (Exception exception) {
                AscensionCraft.LOGGER.debug("Unable to read mob trait reference", exception);
            }
        }
    }
}
