package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimpleDebuffSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillHelper;

import java.util.ArrayList;
import java.util.List;

public class CorrosivePoisonDebuff extends SimpleDebuffSkill implements ITickingSkill {

    private static final int TICK_INTERVAL = 40; // Every 2 seconds
    private static final int DAMAGE_PER_TICK = 1;
    private static final int DURABILITY_DAMAGE_PER_TICK = 2;

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (DebuffSkillHelper.removeIfExpired(player, entityData, ModSkills.CORROSIVE_POISON_DEBUFF.getId())) {
            return;
        }

        if (player.tickCount % TICK_INTERVAL != 0) {
            return;
        }

        player.hurt(player.damageSources().magic(), DAMAGE_PER_TICK);

        List<ItemStack> itemsToDamage = new ArrayList<>();

        ItemStack mainhand = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!mainhand.isEmpty() && mainhand.isDamageableItem()) {
            itemsToDamage.add(mainhand);
        }

        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (!offhand.isEmpty() && offhand.isDamageableItem()) {
            itemsToDamage.add(offhand);
        }

        for (EquipmentSlot armorSlot : new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        }) {
            ItemStack armor = player.getItemBySlot(armorSlot);
            if (!armor.isEmpty() && armor.isDamageableItem()) {
                itemsToDamage.add(armor);
            }
        }

        for (ItemStack item : itemsToDamage) {
            int newDamage = item.getDamageValue() + DURABILITY_DAMAGE_PER_TICK;

            if (newDamage >= item.getMaxDamage()) {
                EquipmentSlot slot = getSlotForItem(player, item);
                if (slot != null) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            } else {
                item.setDamageValue(newDamage);
            }
        }
    }

    private EquipmentSlot getSlotForItem(ServerPlayer player, ItemStack target) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (player.getItemBySlot(slot) == target) {
                return slot;
            }
        }
        return null;
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.corrosive_miasma_debuff";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.corrosive_miasma_debuff.description";
    }
}