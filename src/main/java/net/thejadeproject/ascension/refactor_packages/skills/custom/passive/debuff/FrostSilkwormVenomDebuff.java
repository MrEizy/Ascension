package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.attributes.AttributeValueContainer;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimpleDebuffSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillHelper;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ModifierOperation;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;


public class FrostSilkwormVenomDebuff extends SimpleDebuffSkill implements ITickingSkill {

    // Stage transitions — victim has 20s warning before damage
    private static final int STAGE_2_TICKS = 100;   //  5 seconds
    private static final int STAGE_3_TICKS = 200;   // 10 seconds
    private static final int STAGE_4_TICKS = 400;   // 20 seconds
    private static final int MAX_DURATION_TICKS = 900; // 45 seconds — self-terminates regardless of how it was applied

    // Subtle freeze overlay per stage
    private static final int FREEZE_STAGE_1 = 10;
    private static final int FREEZE_STAGE_2 = 20;
    private static final int FREEZE_STAGE_3 = 35;
    private static final int FREEZE_STAGE_4 = 55;

    // 2% of Ascension max HP drained per second at stage 4, capped at 50%
    private static final int DAMAGE_INTERVAL = 20;
    private static final double DRAIN_PER_INTERVAL = 0.03;
    private static final double MAX_DRAIN_FRACTION = 0.50;

    private static final ResourceLocation DRAIN_ID =
            ResourceLocation.fromNamespaceAndPath("ascension", "frost_silkworm_temp_hp_drain");


    private int getStage(int ticks) {
        if (ticks >= STAGE_4_TICKS) return 4;
        if (ticks >= STAGE_3_TICKS) return 3;
        if (ticks >= STAGE_2_TICKS) return 2;
        return 1;
    }

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (DebuffSkillHelper.removeIfExpired(player, entityData, ModSkills.FROST_SILKWORM_POISON_TEMP.getId())) {
            return;
        }

        DebuffSkillData data = DebuffSkillHelper.getDebuffData(entityData, ModSkills.FROST_SILKWORM_POISON_TEMP.getId());
        if (data == null) return;

        int ticks = (int) data.getElapsedTicks(player.serverLevel().getGameTime());

        if (ticks >= MAX_DURATION_TICKS) {
            entityData.removeSkill(ModSkills.FROST_SILKWORM_POISON_TEMP.getId(), ModForms.MORTAL_VESSEL.getId());
            return;
        }

        int stage = getStage(ticks);

        int targetFreeze = switch (stage) {
            case 1 -> FREEZE_STAGE_1;
            case 2 -> FREEZE_STAGE_2;
            case 3 -> FREEZE_STAGE_3;
            default -> FREEZE_STAGE_4;
        };

        player.setTicksFrozen(Math.max(player.getTicksFrozen(), targetFreeze));

        if (stage == 4 && ticks % DAMAGE_INTERVAL == 0) {
            applyDrain(player, entityData);
        }
    }

    private static void applyDrain(ServerPlayer player, IEntityData entityData) {
        AttributeValueContainer ascMaxHp = entityData.getAscensionAttributeHolder().getAttribute(Attributes.MAX_HEALTH);
        if (ascMaxHp == null) return;

        ValueContainerModifier existing = ascMaxHp.getAllModifiers().stream()
                .filter(m -> DRAIN_ID.equals(m.getIdentifier()))
                .findFirst().orElse(null);
        double currentDrain = existing != null ? Math.abs(existing.getVal()) : 0.0;

        double originalMaxHp = ascMaxHp.getValue() + currentDrain;
        double cap = originalMaxHp * MAX_DRAIN_FRACTION;

        if (currentDrain >= cap) return;

        double newDrain = Math.min(currentDrain + originalMaxHp * DRAIN_PER_INTERVAL, cap);

        ascMaxHp.removeModifier(DRAIN_ID);
        ascMaxHp.addModifier(new ValueContainerModifier(-newDrain, ModifierOperation.ADD_FINAL, DRAIN_ID));

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        PacketDistributor.sendToPlayer(player, new SyncAttributeHolder(entityData.getAscensionAttributeHolder()));
    }

    @Override
    public void onRemoved(IEntityData attachedEntityData, IPersistentSkillData persistentData) {
        if (!(attachedEntityData.getAttachedEntity() instanceof ServerPlayer player)) return;
        restoreMaxHealth(player);
    }

    public static void restoreMaxHealth(ServerPlayer player) {
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;
        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        AttributeValueContainer ascMaxHp = entityData.getAscensionAttributeHolder().getAttribute(Attributes.MAX_HEALTH);
        if (ascMaxHp == null) return;
        ascMaxHp.removeModifier(DRAIN_ID);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
        PacketDistributor.sendToPlayer(player, new SyncAttributeHolder(entityData.getAscensionAttributeHolder()));
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.frost_silkworm_poison_temp_debuff";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.frost_silkworm_poison_temp_debuff.description";
    }
}
