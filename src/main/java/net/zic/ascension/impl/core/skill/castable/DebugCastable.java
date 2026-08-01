package net.zic.ascension.impl.core.skill.castable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.castable.CastData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.PreCastData;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastType;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.EmptySkillData;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;

import java.util.UUID;

public record DebugCastable(String message,int cooldown,UUID uuid) implements CastableSkill {

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public void onEquip(LivingEntity entity, PreCastData preCastData) {

    }

    @Override
    public void onUnEquip(LivingEntity entity, PreCastData preCastData) {

    }

    @Override
    public void selected(LivingEntity entity, PreCastData preCastData) {

    }

    @Override
    public void unselected(LivingEntity entity, PreCastData preCastData) {

    }

    @Override
    public CastResult tryCast(LivingEntity caster) {
        EntityCooldownHandler handler = caster.getData(ZenithAttachments.COOLDOWN_HANDLER);
        Identifier identifier =Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,uuid.toString());
        return handler.isOnCooldown(identifier) ?
                CastResult.fail(Component.literal("On Cooldown("+(handler.getCooldown(identifier)/20.0)+"s)")) :
                CastResult.success();
    }

    @Override
    public CastData initialCast(LivingEntity caster, PreCastData preCastData) {

        if(caster instanceof ServerPlayer player){
            player.sendSystemMessage(
                    Component.literal(message)
            );
        }

         caster.getData(ZenithAttachments.COOLDOWN_HANDLER).addCooldown(
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,uuid.toString()),
                cooldown
        );
        return null;
    }

    @Override
    public void continueCasting(LivingEntity caster, CastStatus castStatus, CastData castData, int ticksElapsed) {

    }

    @Override
    public void finalCast(LivingEntity caster, CastStatus status, CastData castData,int ticksElapsed) {

    }

    @Override
    public CastData loadCastData(ByteBuf buf) {
        return null;
    }

    @Override
    public PreCastData newPreCastData() {
        return null;
    }

    @Override
    public PreCastData loadPreCastData(ValueInput input) {
        return null;
    }

    @Override
    public PreCastData loadPreCastData(ByteBuf buf) {
        return null;
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.DEBUG_CASTABLE_TYPE.get();
    }

    @Override
    public Component getName() {
        return Component.literal("DEBUG");
    }

    @Override
    public Component getDescription() {
        return Component.literal("A debug skill");
    }

    @Override
    public void onAdded(OriginSource source, SkillData data) {

    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillData data) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillData data) {

    }

    @Override
    public SkillData newData(RegistryAccess access) {
        return new EmptySkillData();
    }

    @Override
    public SkillData loadData(ValueInput input,RegistryAccess access) {
        return new EmptySkillData();
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new EmptySkillData();
    }
}
