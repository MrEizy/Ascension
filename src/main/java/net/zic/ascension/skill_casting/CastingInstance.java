package net.zic.ascension.skill_casting;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.castable.CastData;
import net.zic.ascension.api.core.skill.castable.CastableSkill;
import net.zic.ascension.api.core.skill.castable.PreCastData;
import net.zic.ascension.api.core.skill.castable.data.CastResult;
import net.zic.ascension.api.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.core.skill.castable.data.CastType;
import net.zic.zenithlib.network.ByteBufHelpers;

public class CastingInstance {

    private Identifier skill;
    private final CastStatus status = new CastStatus();
    private CastData castData;
    private int ticksElapsed;

    private boolean dirty;

    public void startCast(Player caster, Identifier skill, PreCastData preCastData){
        if(skill != null && skill.equals(this.skill)) return; //same skill ignore

        endCast(caster); //clean up

        if(skill == null) return;

        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,caster.level().registryAccess()) instanceof CastableSkill castableSkill)) return;

        this.skill = skill;

        CastResult result = castableSkill.tryCast(caster);
        if(!result.isSuccess()){
            caster.sendOverlayMessage(result.message);
            markDirty();
            this.skill = null;
            return;
        }

        CastData data = castableSkill.initialCast(
                caster,
                preCastData
        );
        if(castableSkill.getCastType() == CastType.INSTANT){
            this.skill = null;
            return;
        }

        castData = data;
        markDirty();
    }

    public void endCast(Player caster, CastStatus.Reason reason){
        if(skill == null) return;
        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,caster.level().registryAccess()) instanceof CastableSkill castableSkill)) return;

        status.setReason(reason);
        endCast(caster);
    }
    public void endCast(Player caster){
        if(skill == null) return;
        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,caster.level().registryAccess()) instanceof CastableSkill castableSkill)) return;


        castableSkill.finalCast(caster,status,castData,ticksElapsed);

        skill = null;
        castData = null;
        status.resolve();
        ticksElapsed = 0;

        markDirty();
    }

    public void continueCasting(Player caster){
        if(skill == null) return;
        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,caster.level().registryAccess()) instanceof CastableSkill castableSkill)) return;

        castableSkill.continueCasting(caster,status,castData,ticksElapsed);
        if(!status.isCasting()){
            endCast(caster);
        }

        ticksElapsed++;
    }



    public void markDirty(){
        this.dirty = true;
    }
    public boolean isDirty(){
        return dirty;
    }
    public void resolveDirty(){
        this.dirty = false;
    }

    public void encode(ByteBuf buf){
        buf.writeBoolean(skill != null);
        if(skill == null) return;
        ByteBufHelpers.encodeIdentifier(skill,buf);
        buf.writeBoolean(castData != null);
        if(castData != null){
            castData.encode(buf);
        }

    }
    public void decode(RegistryFriendlyByteBuf buf,Player player){
        if(!buf.readBoolean()){
            endCast(player, CastStatus.Reason.NATURAL); //this will always be the case for the client
            return;
        }
        Identifier skill = buf.readIdentifier();
        if(!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                buf.registryAccess()
        ) instanceof CastableSkill castableSkill)){
            endCast(player, CastStatus.Reason.NATURAL);
            return;
        };

        if(!skill.equals(this.skill)) endCast(player, CastStatus.Reason.NATURAL);
        this.skill = skill;
        if (!buf.readBoolean()){
            this.castData = null;
        }else{
            this.castData = castableSkill.loadCastData(buf);
        }


    }
}
