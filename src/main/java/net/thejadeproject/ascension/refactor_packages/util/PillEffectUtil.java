package net.thejadeproject.ascension.refactor_packages.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.refactor_packages.alchemy.IPillEffect;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.ArrayList;
import java.util.List;

public class PillEffectUtil {
    // ── Realm multiplier ──────────────────────────────────────────
    /** Each major realm multiplies effects by this factor over the previous. */
    private static final double REALM_MULTIPLIER = 3.5;

    public static double getRealmMultiplier(int majorRealm) {
        // Realm 1 = 1.0×, Realm 2 = 3.5×, Realm 3 = 12.25×, etc.
        return Math.pow(REALM_MULTIPLIER, Math.max(0, majorRealm - 1));
    }

    public static List<IPillEffect> getPillEffects(ItemStack stack){
        ArrayList<IPillEffect> pillEffects = new ArrayList<>();
        if(!stack.has(ModDataComponents.PILL_EFFECTS.get())) return List.of();
        List<String> raw = stack.get(ModDataComponents.PILL_EFFECTS);
        for(String rawString : raw){
            pillEffects.add(AscensionRegistries.PillEffects.PILL_EFFECT_REGISTRY.get(ResourceLocation.parse(rawString)));
        }
        return pillEffects;
    }
    public static double getPurityScale(ItemStack stack){
        Integer purityComp = stack.get(ModDataComponents.PILL_PURITY.get());
        int purity      = (purityComp != null) ? purityComp : 50;
        return purity / 100.0;
    }
    public static double getRealmMultiplier(ItemStack stack){
        Integer majorComp  = stack.get(ModDataComponents.PILL_MAJOR_REALM.get());
        int majorRealm  = (majorComp  != null) ? majorComp  : 1;
        return getRealmMultiplier(majorRealm);
    }

    public static ItemStack applyPillData(ItemStack stack, int majorRealm,
                                          int purity, String bonusEffect) {
        stack.set(ModDataComponents.PILL_MAJOR_REALM.get(), majorRealm);
        stack.set(ModDataComponents.PILL_PURITY.get(), purity);
        if (bonusEffect != null && !bonusEffect.isEmpty()) {
            stack.set(ModDataComponents.PILL_BONUS_EFFECT.get(), bonusEffect);
        }
        return stack;
    }
}
