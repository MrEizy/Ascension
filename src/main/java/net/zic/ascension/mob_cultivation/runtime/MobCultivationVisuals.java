package net.zic.ascension.mob_cultivation.runtime;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;
import net.zic.ascension.mob_cultivation.profile.MobCultivationProfileManager;

public final class MobCultivationVisuals {
    private static final int AURA_INTERVAL = 40;
    private static boolean debugNamesEnabled;

    private MobCultivationVisuals() {
    }

    public static void tick(Mob mob, long gameTime) {
        if ((mob.getId() + gameTime) % AURA_INTERVAL != 0L) return;
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        spawnAura(mob, 2);
        applyDebugName(mob);
    }

    public static boolean areDebugNamesEnabled() { return debugNamesEnabled; }
    public static void setDebugNamesEnabled(boolean enabled) { debugNamesEnabled = enabled; }

    public static void applyDebugName(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        if (!debugNamesEnabled || !data.isCultivated()) {
            clearDebugName(mob);
            return;
        }
        PathData pathData = MobCultivationManager.getPathData(mob);
        if (pathData == null) return;
        MutableComponent name = Component.empty();
        if (data.getEliteTier().isElite()) {
            name.append(Component.literal(data.getEliteTier().name() + " "));
        }
        name.append(mob.getType().getDescription())
                .append(" • ")
                .append(pathData.getRealmName(pathData.getMajorRealm(), pathData.getMinorRealm(), mob.registryAccess()));
        String generatedName = name.getString();

        if (data.isDebugNameApplied()) {
            Component currentName = mob.getCustomName();
            if (currentName != null && !currentName.getString().equals(data.getLastDebugName())) {
                data.clearDebugNameState();
                return;
            }
        } else if (mob.getCustomName() != null) {
            return;
        }
        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
        data.setDebugName(generatedName);
    }

    public static void clearDebugName(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        if (!data.isDebugNameApplied()) return;
        Component currentName = mob.getCustomName();
        if (currentName != null && currentName.getString().equals(data.getLastDebugName())) {
            mob.setCustomName(null);
            mob.setCustomNameVisible(false);
        }
        data.clearDebugNameState();
    }

    public static void spawnAura(Mob mob, int baseCount) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        if (!MobCultivationProfileManager.resolve(mob, data.getCategory()).showParticles()) return;
        int count = Math.max(1, baseCount * data.getEliteTier().particleMultiplier());
        level.sendParticles(
                getAuraParticle(data),
                mob.getX(),
                mob.getY() + mob.getBbHeight() * 0.55D,
                mob.getZ(),
                count,
                mob.getBbWidth() * 0.35D,
                mob.getBbHeight() * 0.35D,
                mob.getBbWidth() * 0.35D,
                0.01D
        );
    }

    public static void onBreakthrough(Mob mob, boolean major) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        spawnAura(mob, major ? 32 : 12);
        level.playSound(
                null,
                mob.blockPosition(),
                major ? SoundEvents.TOTEM_USE : SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.HOSTILE,
                major ? 1.15F : 0.7F,
                major ? 0.85F : 1.2F
        );
        if (major) {
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(),
                    36, mob.getBbWidth(), mob.getBbHeight() * 0.5D, mob.getBbWidth(), 0.06D
            );
        }
    }

    public static void announceEliteSpawn(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        if (!data.getEliteTier().isElite() || !(mob.level() instanceof ServerLevel level)) return;
        spawnAura(mob, data.getEliteTier() == MobCultivationEliteTier.ANCIENT ? 12 : 9);
        Component message = Component.literal("A powerful presence has appeared nearby.");
        double radius = data.getEliteTier() == MobCultivationEliteTier.ANCIENT ? 96.0D : 56.0D;
        for (ServerPlayer player : level.getPlayers(candidate -> candidate.distanceToSqr(mob) <= radius * radius)) {
            player.sendSystemMessage(message);
        }
    }

    private static ParticleOptions getAuraParticle(MobCultivationData data) {
        for (Identifier path : data.getSubPaths()) {
            String value = path.toString();
            if (value.endsWith("elemental/fire")) return ParticleTypes.SOUL_FIRE_FLAME;
            if (value.endsWith("elemental/water")) return ParticleTypes.DRIPPING_WATER;
            if (value.endsWith("elemental/ice")) return ParticleTypes.SNOWFLAKE;
            if (value.endsWith("elemental/lightning")) return ParticleTypes.ELECTRIC_SPARK;
            if (value.endsWith("elemental/poison")) return ParticleTypes.WITCH;
            if (value.endsWith("elemental/space")) return ParticleTypes.PORTAL;
        }
        if (MobCultivationManager.BODY_PATH.equals(data.getFoundationPath())) return ParticleTypes.CRIT;
        if (MobCultivationManager.SOUL_PATH.equals(data.getFoundationPath())) return ParticleTypes.SOUL;
        return ParticleTypes.ENCHANT;
    }
}
