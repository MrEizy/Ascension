package net.zic.ascension.mob_cultivation.trait;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

public record MobCultivationTraitDefinition(
        Identifier id,
        String displayName,
        int regenerationInterval,
        double regenerationFraction,
        double packAlertRadius,
        double retreatHealthThreshold,
        boolean seekHighQi,
        double mundanePhysicalReduction,
        double spiritReductionScale,
        int mundaneNullificationRealmScore,
        double pathInfusedReduction,
        double fireDamageMultiplier,
        double magicDamageMultiplier
) {
    public static MobCultivationTraitDefinition parse(Identifier id, JsonObject root) {
        return new MobCultivationTraitDefinition(
                id, string(root, "display_name", id.toString()),
                Math.max(20, integer(root, "regeneration_interval", 100)),
                nonNegativeDouble(root, "regeneration_fraction", 0.0D),
                nonNegativeDouble(root, "pack_alert_radius", 0.0D),
                Math.clamp(nonNegativeDouble(root, "retreat_health_threshold", 0.0D), 0.0D, 1.0D),
                bool(root, "seek_high_qi", false),
                clamp01(root, "mundane_physical_reduction", 0.0D),
                nonNegativeDouble(root, "spirit_reduction_scale", 0.0D),
                Math.max(-1, integer(root, "mundane_nullification_realm_score", -1)),
                clamp01(root, "path_infused_reduction", 0.0D),
                nonNegativeDouble(root, "fire_damage_multiplier", 1.0D),
                nonNegativeDouble(root, "magic_damage_multiplier", 1.0D)
        );
    }
    private static String string(JsonObject r,String k,String f){try{return r.has(k)?r.get(k).getAsString():f;}catch(Exception e){return f;}}
    private static int integer(JsonObject r,String k,int f){try{return r.has(k)?r.get(k).getAsInt():f;}catch(Exception e){return f;}}
    private static double nonNegativeDouble(JsonObject r,String k,double f){try{double v=r.has(k)?r.get(k).getAsDouble():f;return Double.isFinite(v)&&v>=0?v:f;}catch(Exception e){return f;}}
    private static double clamp01(JsonObject r,String k,double f){return Math.clamp(nonNegativeDouble(r,k,f),0.0D,1.0D);}
    private static boolean bool(JsonObject r,String k,boolean f){try{return r.has(k)?r.get(k).getAsBoolean():f;}catch(Exception e){return f;}}
}
