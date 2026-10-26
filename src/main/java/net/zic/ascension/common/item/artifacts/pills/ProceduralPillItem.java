package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalConsumptionService;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalProfile;
import net.zic.ascension.common.item.components.AscensionComponents;


/**
 * A single consumable whose persistent profile defines every individual pill's actual medicine.
 * Unconfigured stacks cannot be consumed; only the server can apply the resolved medicinal effects.
 */
public final class ProceduralPillItem extends Item {
    public ProceduralPillItem(Properties properties) {
        super(properties.component(DataComponents.CONSUMABLE,
                Consumables.defaultFood().consumeSeconds(1.0F).build()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        MedicinalProfile profile = player.getItemInHand(hand).get(AscensionComponents.MEDICINAL_PILL.get());
        if (profile == null || profile.composition().isEmpty() || profile.effects().isEmpty()) return InteractionResult.FAIL;
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        MedicinalProfile profile = stack.get(AscensionComponents.MEDICINAL_PILL.get());
        if (profile == null || profile.composition().isEmpty() || profile.effects().isEmpty()) return stack;
        if (!level.isClientSide()) MedicinalConsumptionService.consume(profile, entity, true);
        return super.finishUsingItem(stack, level, entity);
    }

}
