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
import net.zic.ascension.configuration.mob_traits.MobTraitRoll;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.EntityStatHolder;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class MobConfigurationInstance {
    public record PoolSelection(Identifier pool, String entry) {
    }

    private static final Identifier RARITY_POOL = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "rarity");
    private final List<PoolSelection> selections = new ArrayList<>();
    private final List<MobTraitReference> traits = new ArrayList<>();
    private final List<ResolvedMobTrait> rolledTraits = new ArrayList<>();
    private MobTraitRoll cultivation = MobTraitRoll.EMPTY;
    private final Map<Identifier, Double> statBiases = new LinkedHashMap<>();

    public MobConfigurationInstance() {
    }

    public MobConfigurationInstance(List<PoolSelection> selections, List<MobTraitReference> traits) {
        this(selections, traits, List.of(), MobTraitRoll.EMPTY, Map.of());
    }

    public MobConfigurationInstance(List<PoolSelection> selections, List<MobTraitReference> traits,
                                    List<ResolvedMobTrait> rolledTraits) {
        this(selections, traits, rolledTraits, MobTraitRoll.EMPTY, Map.of());
    }

    public MobConfigurationInstance(List<PoolSelection> selections, List<MobTraitReference> traits,
                                    List<ResolvedMobTrait> rolledTraits, MobTraitRoll cultivation,
                                    Map<Identifier, Double> statBiases) {
        this.selections.addAll(selections);
        this.traits.addAll(traits);
        this.rolledTraits.addAll(rolledTraits);
        this.cultivation = cultivation == null ? MobTraitRoll.EMPTY : cultivation;
        this.statBiases.putAll(statBiases);
    }

    public List<PoolSelection> getSelections() {
        return List.copyOf(selections);
    }

    public List<MobTraitReference> getTraits() {
        return List.copyOf(traits);
    }

    public List<ResolvedMobTrait> getRolledTraits() {
        return List.copyOf(rolledTraits);
    }

    public MobTraitRoll getCultivation() {
        return cultivation;
    }

    public Map<Identifier, Double> getStatBiases() {
        return Map.copyOf(statBiases);
    }

    private Identifier statOwner(Identifier stat) {
        return Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                "mob_species_stat/" + stat.getNamespace() + "/" + stat.getPath());
    }

    private Identifier cultivationOwner(UUID mobId) {
        return Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_species_cultivation/" + mobId);
    }

    private Identifier rolledOwner(MobTraitReference trait, UUID mobId) {
        Identifier base = owner(trait, mobId);
        return Identifier.fromNamespaceAndPath(base.getNamespace(), base.getPath() + "/rolled");
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
        if (statBiases.isEmpty()) return;
        EntityStatHolder holder = mob.getData(ZenithAttachments.STAT_HOLDER);
        holder.startProcess("mob_species_stat_gain");
        for (Map.Entry<Identifier, Double> entry : statBiases.entrySet()) {
            double multiplier = entry.getValue();
            if (!Double.isFinite(multiplier) || multiplier <= 0 || multiplier == 1.0D) continue;
            holder.addMultiplierModifier(ZenithStatHelper.stat(entry.getKey()),
                    Modifier.multiplier(statOwner(entry.getKey()), 0, multiplier - 1.0D));
        }
        holder.resolveProcess("mob_species_stat_gain");
    }

    public void applyToSource(OriginSource source, UUID mobId) {
        if (source == null) return;
        for (MobTraitReference trait : traits) {
            MobTraitDefinition definition = trait.getTrait(source.getRegistryAccess());
            if (definition != null) definition.applyToSource(source, owner(trait, mobId));
        }
        for (ResolvedMobTrait rolled : rolledTraits) {
            rolled.applyToSource(source, rolledOwner(rolled.trait(), mobId));
        }
        ResolvedMobTrait.applyRollToSource(source, cultivationOwner(mobId), cultivation);
    }

    public void removeFromMob(Mob mob) {
        for (MobTraitReference trait : traits) {
            MobTraitDefinition definition = trait.getTrait(mob.registryAccess());
            if (definition != null) definition.removeFromMob(mob);
        }
        if (statBiases.isEmpty()) return;
        EntityStatHolder holder = mob.getData(ZenithAttachments.STAT_HOLDER);
        holder.startProcess("mob_species_stat_removal");
        for (Identifier stat : statBiases.keySet()) {
            ValueContainer<Double> container = holder.getStatInstance(ZenithStatHelper.stat(stat));
            if (container != null) container.removeModifier(statOwner(stat));
        }
        holder.resolveProcess("mob_species_stat_removal");
    }

    public void removeFromSource(OriginSource source, UUID mobId) {
        if (source == null) return;
        ResolvedMobTrait.removeRollFromSource(source, cultivationOwner(mobId), cultivation);
        for (ResolvedMobTrait rolled : rolledTraits) {
            rolled.removeFromSource(source, rolledOwner(rolled.trait(), mobId));
        }
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
        ValueOutput.ValueOutputList rolls = output.childrenList("rolled_traits");
        for (ResolvedMobTrait rolled : rolledTraits) {
            rolled.write(rolls.addChild());
        }
        if (!cultivation.isEmpty()) {
            ResolvedMobTrait.writeRoll(output.childrenList("cultivation").addChild(), cultivation);
        }
        ValueOutput.ValueOutputList stats = output.childrenList("stat_biases");
        for (Map.Entry<Identifier, Double> entry : statBiases.entrySet()) {
            ValueOutput stat = stats.addChild();
            stat.putString("id", entry.getKey().toString());
            stat.putDouble("multiplier", entry.getValue());
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
        rolledTraits.clear();
        cultivation = MobTraitRoll.EMPTY;
        statBiases.clear();
        for (ValueInput statInput : input.childrenListOrEmpty("stat_biases")) {
            try {
                Identifier id = Identifier.parse(statInput.getStringOr("id", "none"));
                double multiplier = statInput.getDoubleOr("multiplier", 1.0D);
                if (Double.isFinite(multiplier) && multiplier > 0 && !id.getPath().equals("none")) {
                    statBiases.put(id, multiplier);
                }
            } catch (Exception exception) {
                AscensionCraft.LOGGER.debug("Unable to read mob species stat", exception);
            }
        }
        for (ValueInput rollInput : input.childrenListOrEmpty("cultivation")) {
            try {
                cultivation = ResolvedMobTrait.readRoll(rollInput);
                break;
            } catch (Exception exception) {
                AscensionCraft.LOGGER.debug("Unable to read mob species cultivation", exception);
            }
        }
        for (ValueInput rollInput : input.childrenListOrEmpty("rolled_traits")) {
            try {
                ResolvedMobTrait rolled = ResolvedMobTrait.read(rollInput);
                if (traits.contains(rolled.trait()) && rolledTraits.stream()
                        .noneMatch(existing -> existing.trait().equals(rolled.trait()))) {
                    rolledTraits.add(rolled);
                }
            } catch (Exception exception) {
                AscensionCraft.LOGGER.debug("Unable to read resolved mob trait", exception);
            }
        }
    }
}
