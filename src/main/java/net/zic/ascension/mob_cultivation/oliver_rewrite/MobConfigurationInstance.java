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
import java.util.Locale;
import java.util.UUID;

public class MobConfigurationInstance {
    public record PoolSelection(Identifier pool, String entry) {
    }

    private static final Identifier RARITY_POOL = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "rarity");
    private final List<PoolSelection> selections = new ArrayList<>();
    private final List<MobTraitReference> traits = new ArrayList<>();

    public MobConfigurationInstance() {
    }

    public MobConfigurationInstance(List<PoolSelection> selections, List<MobTraitReference> traits) {
        this.selections.addAll(selections);
        this.traits.addAll(traits);
    }

    public List<PoolSelection> getSelections() {
        return List.copyOf(selections);
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
        ValueOutput.ValueOutputList selectedPools = output.childrenList("selections");
        for (PoolSelection selection : selections) {
            ValueOutput child = selectedPools.addChild();
            child.putString("pool", selection.pool().toString());
            child.putString("entry", selection.entry());
        }
        ValueOutput.ValueOutputList list = output.childrenList("traits");
        for (MobTraitReference trait : traits) {
            trait.write(list.addChild());
        }
    }

    public void read(ValueInput input, RegistryAccess access) {
        selections.clear();
        for (ValueInput selectionInput : input.childrenListOrEmpty("selections")) {
            try {
                Identifier pool = Identifier.parse(selectionInput.getStringOr("pool", ""));
                String entry = selectionInput.getStringOr("entry", "");
                PoolSelection selection = new PoolSelection(pool, entry);
                if (!entry.isBlank() && !selections.contains(selection)) selections.add(selection);
            } catch (Exception exception) {
                AscensionCraft.LOGGER.debug("Unable to read mob trait pool selection", exception);
            }
        }
        if (selections.isEmpty()) {
            String legacyTier = input.getStringOr("tier", "").toLowerCase(Locale.ROOT);
            if (legacyTier.equals("normal") || legacyTier.equals("elite") || legacyTier.equals("ancient")) {
                selections.add(new PoolSelection(RARITY_POOL, legacyTier));
            }
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
