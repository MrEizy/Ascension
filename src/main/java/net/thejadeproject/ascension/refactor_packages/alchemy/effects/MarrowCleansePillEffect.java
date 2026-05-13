package net.thejadeproject.ascension.refactor_packages.alchemy.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.alchemy.BasicPillEffect;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.physiques.ModPhysiques;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.EvolvingPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.helpers.PhysiqueEvolutionHelper;

public class MarrowCleansePillEffect extends BasicPillEffect {

    private final double baseChance;
    private final double maxChance;

    public MarrowCleansePillEffect(Component name, Component description) {
        this(0.01D, 0.20D, name, description);
    }

    public MarrowCleansePillEffect(double baseChance, double maxChance, Component name, Component description) {
        super(name, description);
        this.baseChance = baseChance;
        this.maxChance = maxChance;
    }

    @Override
    public boolean tryConsume(LivingEntity entity, ItemStack itemStack, double purityScale, double realmMultiplier) {
        if (!(entity instanceof ServerPlayer player)) return false;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return false;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        if (!(entityData.getPhysique() instanceof EvolvingPhysique evolvingPhysique)) {
            return false;
        }

        if (!evolvingPhysique.canEvolveInto(ModPhysiques.MORTAL.getId())) {
            return false;
        }

        double chance = clamp(baseChance * purityScale * realmMultiplier, 0.0D, maxChance);

        if (player.getRandom().nextDouble() >= chance) {
            player.sendSystemMessage(
                    Component.literal("marrow cleanse failed")
            );
            return true;
        }

        return PhysiqueEvolutionHelper.tryEvolveInto(
                player,
                entityData,
                ModPhysiques.MORTAL.getId()
        );
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}