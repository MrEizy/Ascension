package net.thejadeproject.ascension.refactor_packages.techniques.custom.essence;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.IBreakthroughInstance;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.NineHeavenlyTribulations;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.GenericTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.stat_change_handlers.BasicStatChangeHandler;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data.BloodfeastTechniqueData;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bloodfeast Soul-Refining Scripture
 *
 * An Essence-path technique that cultivates by devouring the life-force of
 * surrounding entities via the Bloodfeast Banquet skill.
 *
 * ── Speed vs stats ───────────────────────────────────────────────────────────
 * BASE_RATE (3.5) is higher than the standard 2.0, but stat entries registered
 * via this technique's addMinor/MajorRealm* methods should use weaker values
 * than a normal Essence technique to compensate.
 *
 * ── Realm gates ──────────────────────────────────────────────────────────────
 * Major realms 2-6 each require a running total kill count (any entity type):
 *   realm 2 →   100 kills
 *   realm 3 →  1000 kills
 *   realm 4 →  1250 kills
 *   realm 5 →  1500 kills
 *   realm 6 →  2000 kills
 *
 * ── Player-only bonus ────────────────────────────────────────────────────────
 * If EVERY kill made between two consecutive gate clears was a player kill,
 * the player earns a permanent stacking bonus applied on top of the normal
 * stat-change gains on every future realm change:
 *
 *   +0.2  per player-only gate  →  per MINOR realm change, per tracked stat/attribute
 *   +0.4  per player-only gate  →  per MAJOR realm change, per tracked stat/attribute
 *
 * Bonus modifiers are keyed "bloodfeast_bonus_minor_<major>_<minor>" /
 * "bloodfeast_bonus_major_<major>_0" so they sit alongside — not on top of —
 * the handler's own modifier entries.
 *
 * ── AOE range ────────────────────────────────────────────────────────────────
 *   realm 0 →  8 blocks
 *   realm 1 → 12 blocks
 *   realm 2 → 16 blocks
 *   realm 3 → 20 blocks
 *   realm 4 → 24 blocks (cap)
 */
public class BloodfeastSoulRefiningTechnique extends GenericTechnique {

    public static final double BASE_RATE = 3.5D;
    private static final int[] REALM_RANGES = {8, 12, 16, 20, 24};

    // ── Shadow maps ───────────────────────────────────────────────────────────
    // BasicStatChangeHandler's internal maps are private; we keep a parallel
    // copy here so applyBloodfeastBonus knows which stats/attributes to touch.
    // Always register entries through this class's add* methods — never call
    // statHandler.add* directly — so both maps stay in sync.
    private final HashMap<ResourceLocation, ValueContainerModifier>  shadowMinorStatMap  = new HashMap<>();
    private final HashMap<ResourceLocation, ValueContainerModifier>  shadowMajorStatMap  = new HashMap<>();
    private final HashMap<Holder<Attribute>, ValueContainerModifier> shadowMinorAttrMap  = new HashMap<>();
    private final HashMap<Holder<Attribute>, ValueContainerModifier> shadowMajorAttrMap  = new HashMap<>();

    private final BasicStatChangeHandler statHandler;

    // ── Construction ─────────────────────────────────────────────────────────

    public BloodfeastSoulRefiningTechnique(BasicStatChangeHandler statChangeHandler) {
        super(
                ModPaths.ESSENCE.getId(),
                Component.translatable("ascension.technique.bloodfeast_soul_refining_scripture"),
                BASE_RATE,
                Set.of()
        );
        this.statHandler = statChangeHandler;
        setStatChangeHandler(statChangeHandler);
    }

    // ── Fluent registration helpers ───────────────────────────────────────────
    // Use these instead of calling statHandler.add* directly.

    public BloodfeastSoulRefiningTechnique addMinorRealmStatModifier(
            ResourceLocation stat, ValueContainerModifier modifier) {
        statHandler.addMinorRealmStatModifier(stat, modifier);
        shadowMinorStatMap.put(stat, modifier);
        return this;
    }

    public BloodfeastSoulRefiningTechnique addMajorRealmStatModifier(
            ResourceLocation stat, ValueContainerModifier modifier) {
        statHandler.addMajorRealmStatModifier(stat, modifier);
        shadowMajorStatMap.put(stat, modifier);
        return this;
    }

    public BloodfeastSoulRefiningTechnique addMinorRealmAttributeModifier(
            Holder<Attribute> attr, ValueContainerModifier modifier) {
        statHandler.addMinorRealmAttributeModifier(attr, modifier);
        shadowMinorAttrMap.put(attr, modifier);
        return this;
    }

    public BloodfeastSoulRefiningTechnique addMajorRealmAttributeModifier(
            Holder<Attribute> attr, ValueContainerModifier modifier) {
        statHandler.addMajorRealmAttributeModifier(attr, modifier);
        shadowMajorAttrMap.put(attr, modifier);
        return this;
    }

    // ── Display ───────────────────────────────────────────────────────────────

    @Override
    public Component getShortDescription() {
        return Component.translatable("ascension.technique.bloodfeast_soul_refining_scripture.short_desc");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ascension.technique.bloodfeast_soul_refining_scripture.desc");
    }

    // ── Skill grants ─────────────────────────────────────────────────────────

    @Override
    public void onTechniqueAdded(IEntityData heldEntity) {
        PathData pathData = heldEntity.getPathData(getPath());
        ResourceLocation techniqueId = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.getKey(this);

        if (pathData != null && techniqueId != null && pathData.getTechniqueData(techniqueId) == null) {
            pathData.addTechniqueData(techniqueId, freshTechniqueData(heldEntity));
        }

        heldEntity.giveSkill(
                ModSkills.BLOODFEAST_BANQUET_SKILL.getId(),
                ModForms.MORTAL_VESSEL.getId()
        );

        refreshUniversalTechniqueSkills(heldEntity);
    }

    @Override
    public void onTechniqueRemoved(IEntityData heldEntity, ITechniqueData techniqueData) {
        PathData pathData = heldEntity.getPathData(getPath());

        if (pathData != null) {
            pathData.handleRealmChange(pathData.getMajorRealm(), 0, heldEntity);
        }

        heldEntity.removeSkill(
                ModSkills.BLOODFEAST_BANQUET_SKILL.getId(),
                ModForms.MORTAL_VESSEL.getId()
        );

        refreshUniversalTechniqueSkills(heldEntity);
    }

    // ── onRealmChange ─────────────────────────────────────────────────────────

    /**
     * Runs the normal BasicStatChangeHandler pass first via super, then
     * applies the accumulated player-only bonus on top using the same
     * stat/attribute entries but with the bonus value and a distinct modifier id.
     */
    @Override
    public void onRealmChange(
            IEntityData entityData,
            int oldMajorRealm, int oldMinorRealm,
            int newMajorRealm, int newMinorRealm
    ) {
        // Step 1 — normal handler + UniversalTechniqueSkillHelper + base sync
        super.onRealmChange(entityData, oldMajorRealm, oldMinorRealm, newMajorRealm, newMinorRealm);

        // Step 2 — read bonus from technique data
        ResourceLocation techniqueId = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.getKey(this);
        if (techniqueId == null) return;

        PathData pathData = entityData.getPathData(getPath());
        if (pathData == null) return;

        ITechniqueData rawData = pathData.getTechniqueData(techniqueId);
        if (!(rawData instanceof BloodfeastTechniqueData data)) return;

        // Step 3 — apply or remove bonus modifiers mirroring BasicStatChangeHandler's pass
        boolean advancing = oldMajorRealm < newMajorRealm
                || (oldMajorRealm == newMajorRealm && newMinorRealm > oldMinorRealm);

        if (advancing) {
            applyBonusForward(entityData, oldMajorRealm, oldMinorRealm, newMajorRealm, newMinorRealm, data);
        } else {
            removeBonusBackward(entityData, oldMajorRealm, oldMinorRealm, newMajorRealm, newMinorRealm);
        }

        entityData.getAscensionAttributeHolder().updateAttributes(entityData);

        if (entityData.isLoading()) return;
        if (entityData.getAttachedEntity().level().isClientSide()) return;
        if (!(entityData.getAttachedEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.connection == null) return;

        PacketDistributor.sendToPlayer(serverPlayer,
                new SyncAttributeHolder(entityData.getAscensionAttributeHolder()));
        for (IEntityFormData formData : entityData.getFormData()) {
            formData.getStatSheet().sync(serverPlayer, formData.getEntityFormId());
        }
    }

    // ── Bonus forward pass ────────────────────────────────────────────────────

    /**
     * Exactly mirrors {@link BasicStatChangeHandler#applyChanges} forward-path
     * loop structure, substituting bonus values for the template modifier values.
     */
    private void applyBonusForward(
            IEntityData entityData,
            int oldMajorRealm, int oldMinorRealm,
            int newMajorRealm, int newMinorRealm,
            BloodfeastTechniqueData data
    ) {
        double minorBonus = data.getMinorRealmStatBonus();
        double majorBonus = data.getMajorRealmStatBonus();

        if (minorBonus <= 0 && majorBonus <= 0) return;

        int majorRealmsChanged = newMajorRealm - oldMajorRealm;

        // Major bonus steps
        for (int i = 1; i <= majorRealmsChanged; i++) {
            applyMajorBonus(entityData, oldMajorRealm, majorBonus);
        }

        // Minor bonus steps — mirrors the handler's three-segment loop exactly
        if (newMajorRealm != oldMajorRealm) {
            // Segment 1: finish minors in old major realm
            int remainingMinors = getMaxMinorRealm(oldMajorRealm) - oldMinorRealm;
            for (int i = 1; i <= remainingMinors; i++) {
                applyMinorBonus(entityData, oldMajorRealm, oldMinorRealm + i, minorBonus);
            }
            // Segment 2: full intermediate major realms
            for (int i = 1; i < majorRealmsChanged; i++) {
                int mid = oldMajorRealm + i;
                int midMax = getMaxMinorRealm(mid);
                for (int j = 0; j <= midMax; j++) {
                    applyMinorBonus(entityData, mid, j, minorBonus);
                }
            }
            // Segment 3: minors up to target in new major realm
            for (int i = 0; i <= newMinorRealm; i++) {
                applyMinorBonus(entityData, newMajorRealm, i, minorBonus);
            }
        } else {
            // Same major realm — only minor steps changed
            for (int i = oldMinorRealm + 1; i <= newMinorRealm; i++) {
                applyMinorBonus(entityData, newMajorRealm, i, minorBonus);
            }
        }
    }


    private void removeBonusBackward(
            IEntityData entityData,
            int oldMajorRealm, int oldMinorRealm,
            int newMajorRealm, int newMinorRealm
    ) {
        for (int i = oldMajorRealm; i > newMajorRealm; i--) {
            removeMajorBonus(entityData, i);
        }

        if (newMajorRealm != oldMajorRealm) {
            for (int i = oldMinorRealm; i >= 0; i--) {
                removeMinorBonus(entityData, oldMajorRealm, i);
            }
            for (int i = oldMajorRealm - 1; i > newMajorRealm; i--) {
                int midMax = getMaxMinorRealm(i);
                for (int j = midMax; j >= 0; j--) {
                    removeMinorBonus(entityData, i, j);
                }
            }
            int maxAtNew = getMaxMinorRealm(newMajorRealm);
            for (int i = maxAtNew; i > newMinorRealm; i--) {
                removeMinorBonus(entityData, newMajorRealm, i);
            }
        } else {
            for (int i = oldMinorRealm; i > newMinorRealm; i--) {
                removeMinorBonus(entityData, newMajorRealm, i);
            }
        }
    }

    // ── Per-step apply/remove ─────────────────────────────────────────────────

    private void applyMinorBonus(IEntityData entityData, int majorRealm, int minorRealm, double bonus) {
        if (bonus <= 0) return;
        ResourceLocation id = bonusMinorId(majorRealm, minorRealm);

        for (Map.Entry<ResourceLocation, ValueContainerModifier> e : shadowMinorStatMap.entrySet()) {
            entityData.getActiveFormData().getStatSheet().addStatModifier(
                    statHandler.getStat(e.getKey()),
                    new ValueContainerModifier(bonus, e.getValue().getOperation(), id, e.getValue().getGroupIdentifier())
            );
        }
        for (Map.Entry<Holder<Attribute>, ValueContainerModifier> e : shadowMinorAttrMap.entrySet()) {
            entityData.getAscensionAttributeHolder().getAttribute(e.getKey()).addModifier(
                    new ValueContainerModifier(bonus, e.getValue().getOperation(), id, e.getValue().getGroupIdentifier())
            );
        }
    }

    private void removeMinorBonus(IEntityData entityData, int majorRealm, int minorRealm) {
        ResourceLocation id = bonusMinorId(majorRealm, minorRealm);
        for (ResourceLocation stat : shadowMinorStatMap.keySet()) {
            entityData.getActiveFormData().getStatSheet().removeStatModifier(statHandler.getStat(stat), id);
        }
        for (Holder<Attribute> attr : shadowMinorAttrMap.keySet()) {
            entityData.getAscensionAttributeHolder().getAttribute(attr).removeModifier(id);
        }
    }

    private void applyMajorBonus(IEntityData entityData, int majorRealm, double bonus) {
        if (bonus <= 0) return;
        ResourceLocation id = bonusMajorId(majorRealm);

        for (Map.Entry<ResourceLocation, ValueContainerModifier> e : shadowMajorStatMap.entrySet()) {
            entityData.getActiveFormData().getStatSheet().addStatModifier(
                    statHandler.getStat(e.getKey()),
                    new ValueContainerModifier(bonus, e.getValue().getOperation(), id, e.getValue().getGroupIdentifier())
            );
        }
        for (Map.Entry<Holder<Attribute>, ValueContainerModifier> e : shadowMajorAttrMap.entrySet()) {
            entityData.getAscensionAttributeHolder().getAttribute(e.getKey()).addModifier(
                    new ValueContainerModifier(bonus, e.getValue().getOperation(), id, e.getValue().getGroupIdentifier())
            );
        }
    }

    private void removeMajorBonus(IEntityData entityData, int majorRealm) {
        ResourceLocation id = bonusMajorId(majorRealm);
        for (ResourceLocation stat : shadowMajorStatMap.keySet()) {
            entityData.getActiveFormData().getStatSheet().removeStatModifier(statHandler.getStat(stat), id);
        }
        for (Holder<Attribute> attr : shadowMajorAttrMap.keySet()) {
            entityData.getAscensionAttributeHolder().getAttribute(attr).removeModifier(id);
        }
    }

    // ── Identifier helpers ────────────────────────────────────────────────────

    private ResourceLocation bonusMinorId(int majorRealm, int minorRealm) {
        return ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "bloodfeast_bonus_minor_" + majorRealm + "_" + minorRealm
        );
    }

    private ResourceLocation bonusMajorId(int majorRealm) {
        return ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "bloodfeast_bonus_major_" + majorRealm + "_0"
        );
    }

    // ── Gate access check ─────────────────────────────────────────────────────

    public boolean canCultivateMajorRealm(ITechniqueData techniqueData, int majorRealm) {
        if (majorRealm <= 1) return true;
        if (!(techniqueData instanceof BloodfeastTechniqueData data)) return false;
        return data.hasMetKillRequirement(majorRealm);
    }

    // ── AOE range ─────────────────────────────────────────────────────────────

    public static int getRangeForRealm(int majorRealm) {
        if (majorRealm < 0) return REALM_RANGES[0];
        if (majorRealm >= REALM_RANGES.length) return REALM_RANGES[REALM_RANGES.length - 1];
        return REALM_RANGES[majorRealm];
    }

    // ── Compatibility ─────────────────────────────────────────────────────────

    @Override
    public boolean isCompatibleWith(ResourceLocation technique) {
        return AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique)
                instanceof BloodfeastSoulRefiningTechnique;
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    @Override
    public ITechniqueData freshTechniqueData(IEntityData heldEntity) {
        return new BloodfeastTechniqueData();
    }

    @Override
    public ITechniqueData fromCompound(CompoundTag tag) {
        return new BloodfeastTechniqueData(tag);
    }

    @Override
    public ITechniqueData fromNetwork(RegistryFriendlyByteBuf buf) {
        return new BloodfeastTechniqueData(buf);
    }

    // ── Breakthrough ─────────────────────────────────────────────────────────

    @Override
    public IBreakthroughInstance freshBreakthroughData(IEntityData heldEntity) {
        return new NineHeavenlyTribulations(1);
    }

    @Override
    public IBreakthroughInstance breakthroughInstanceFromCompound(
            CompoundTag tag, int majorRealm, int minorRealm, ITechniqueData data) {
        return new NineHeavenlyTribulations(1);
    }

    @Override
    public IBreakthroughInstance breakthroughInstanceFromNetwork(
            RegistryFriendlyByteBuf buf, int majorRealm, int minorRealm, ITechniqueData data) {
        return new NineHeavenlyTribulations(1);
    }
}
