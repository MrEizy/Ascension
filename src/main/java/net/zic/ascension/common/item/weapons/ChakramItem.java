package net.zic.ascension.common.item.weapons;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;
import net.zic.ascension.common.entities.AscEntities;
import net.zic.ascension.common.entities.weapons.ChakramEntity;
import net.zic.ascension.common.sounds.AscSounds;

public class ChakramItem extends Item {

    public static final int COOLDOWN_TICKS = 10;
    public static final float THROW_POWER = 1.0F;
    public static final float THROW_SPEED = 1.8F;

    private final ChakramTier tier;

    public ChakramItem(ChakramTier tier, Properties props) {
        super(props);
        this.tier = tier;
    }

    public ChakramTier tier() { return tier; }

    public static Item.Properties buildProperties(ChakramTier tier) {
        ItemAttributeModifiers attrs = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                                tier.meleeDamage - 1.0F,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                                -2.4F,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();

        Item.Properties p = new Item.Properties()
                .setId(Chakrams.itemKey(tier))
                .stacksTo(1)
                .durability(tier.durability)
                .enchantable(tier.enchantability)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, attrs)
                .component(DataComponents.WEAPON, new Weapon(1));

        if (tier.fireResistant()) p.fireResistant();
        if (tier == ChakramTier.NETHERITE) p.rarity(Rarity.UNCOMMON);
        return p;
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {
        // Melee damage from ATTRIBUTE_MODIFIERS.
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.nextDamageWillBreak()) return InteractionResult.FAIL;

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        if (level instanceof ServerLevel serverLevel) {
            int slot = hand == InteractionHand.MAIN_HAND
                    ? player.getInventory().getSelectedSlot()
                    : -1;
            boolean creative = player.hasInfiniteMaterials();

            // Durability is spent at throw time (matches reference).
            if (!creative) {
                stack.hurtWithoutBreaking(1, player);
            }

            ItemStack thrown = creative
                    ? stack.copyWithCount(1)
                    : stack.consumeAndReturn(1, player);

            ChakramEntity chakram = new ChakramEntity(
                    AscEntities.CHAKRAM.get(),
                    serverLevel, player, thrown,
                    THROW_POWER, slot, creative);
            chakram.shootFromRotation(player,
                    player.getXRot(), player.getYRot(), 0.0F,
                    THROW_SPEED * tier.throwSpeed, 0.2F);
            serverLevel.addFreshEntity(chakram);

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    AscSounds.CHAKRAM_THROW.get(), SoundSource.PLAYERS, 1.1F, 1.15F);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}