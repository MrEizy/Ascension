package net.zic.ascension.mob_cultivation.loot;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.profile.MobCultivationProfileManager;
import net.zic.ascension.mob_cultivation.profile.ResolvedMobCultivationProfile;

import java.util.ArrayList;
import java.util.List;

public final class MobCultivationLoot {
    private MobCultivationLoot() {
    }

    public static List<ItemStack> rollBonusLoot(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        if (!data.isCultivated()) return List.of();

        ResolvedMobCultivationProfile profile = MobCultivationProfileManager.resolve(mob, data.getCategory());
        int realmScore = Math.max(0, MobCultivationManager.getRealmScore(mob));
        double baseChance = 0.18D + realmScore * 0.055D + data.getCategory().lootChanceBonus();
        double chance = Math.min(1.0D, baseChance * profile.lootMultiplier() * data.getEliteTier().lootMultiplier());
        if (mob.getRandom().nextDouble() > chance) return List.of();

        MobCultivationLootProfile lootProfile = MobCultivationLootProfileManager.get(data.getLootProfile());
        if (lootProfile == null) {
            lootProfile = MobCultivationLootProfileManager.get(net.zic.ascension.AscensionCraft.prefix("default"));
        }
        if (lootProfile == null) return List.of();

        int rolls = 1;
        if (realmScore >= 6) rolls++;
        if (data.getEliteTier() == MobCultivationEliteTier.ANCIENT) rolls++;
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < rolls; i++) {
            ItemStack stack = rollEntry(mob, data, lootProfile, realmScore);
            if (!stack.isEmpty()) result.add(stack);
        }
        return List.copyOf(result);
    }

    private static ItemStack rollEntry(Mob mob, MobCultivationData data, MobCultivationLootProfile profile, int realmScore) {
        List<MobCultivationLootProfile.Entry> candidates = new ArrayList<>();
        double total = 0.0D;
        for (MobCultivationLootProfile.Entry entry : profile.entries()) {
            if (entry.matches(data, realmScore)) {
                candidates.add(entry);
                total += entry.weight();
            }
        }
        if (candidates.isEmpty() || total <= 0.0D) return ItemStack.EMPTY;

        double roll = mob.getRandom().nextDouble() * total;
        MobCultivationLootProfile.Entry selected = candidates.getLast();
        for (MobCultivationLootProfile.Entry entry : candidates) {
            roll -= entry.weight();
            if (roll <= 0.0D) {
                selected = entry;
                break;
            }
        }

        Item item = BuiltInRegistries.ITEM.getValue(selected.item());
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        int count = selected.minimumCount() >= selected.maximumCount() ? selected.minimumCount() : mob.getRandom().nextIntBetweenInclusive(selected.minimumCount(), selected.maximumCount());
        return new ItemStack(item, count);
    }
}
