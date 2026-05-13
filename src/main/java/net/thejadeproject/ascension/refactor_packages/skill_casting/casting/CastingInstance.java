package net.thejadeproject.ascension.refactor_packages.skill_casting.casting;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.overaly.EasyOverlayHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.Container;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.casting.SyncCastingInstance;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skill_casting.SkillHotBar;
import net.thejadeproject.ascension.refactor_packages.skills.ISkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;

public class CastingInstance {

    private ResourceLocation skillKey;
    private ICastData castData;
    private int ticksElapsed; //how many ticks it has been since cast was started

    public CastingInstance(){

    }

    public ICastableSkill getCastableSkill(){return skillKey == null? null : (ICastableSkill) AscensionRegistries.Skills.SKILL_REGISTRY.get(skillKey);}

    // returns null if no cast was cancelled and a CastEndData instance otherwise
    public CastEndData startCast(Entity entity,ResourceLocation skillKey){
        CastEndData previousSkill = endCast(entity,CastEndReason.CANCELLED);
        this.skillKey = skillKey;
        this.castData = getCastableSkill().freshCastData();
        PacketDistributor.sendToPlayer((ServerPlayer) entity,new SyncCastingInstance(this.skillKey,this.castData));
       return previousSkill;
    }
    public CastEndData endCast(Entity entity,CastEndReason reason){
        CastEndData castEndData = skillKey == null ? null :  new CastEndData(skillKey,reason,castData,ticksElapsed);
        if(skillKey != null){
            getCastableSkill().finalCast(castEndData,entity,castData);
        }

        skillKey = null;
        castData = null;
        ticksElapsed = 0;

        if(entity instanceof ServerPlayer player) PacketDistributor.sendToPlayer(player,new SyncCastingInstance(null,null));
        return castEndData;
    }

    //first make sure we have a skill, try cast it and if the cast is ended call endCast
    public CastEndData castTick(Entity entity){
        ticksElapsed++;
        if(getCastableSkill() != null && !getCastableSkill().continueCasting(ticksElapsed,entity,castData))return endCast(entity,CastEndReason.ENDED);
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void clientCastTick(Entity entity){
        ticksElapsed++;
        if(getCastableSkill() != null) getCastableSkill().continueCasting(ticksElapsed,entity,castData);
    }
    @OnlyIn(Dist.CLIENT)
    public void startClientCast(Entity entity,ResourceLocation skillKey){
        //clear previous data
        if(getCastableSkill() != null && this.skillKey.equals(skillKey)) return;
        SkillHotBar hotBar = entity.getData(ModAttachments.ENTITY_DATA).getSkillCastHandler().getHotBar();
        if(hotBar.getSlot(skillKey) ==-1) return;
        if(getCastableSkill() != null) getCastableSkill().finalCast(new CastEndData(skillKey,CastEndReason.CANCELLED,castData,ticksElapsed),entity,castData);
        this.skillKey = null;
        this.castData = null;
        this.ticksElapsed = 0;


        UIFrame frame = EasyOverlayHandler.getFrame(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"skill_casting"));

        frame.removeElementFromIdAndClasses(frame.getRoot());
        if(skillKey == null) {
            frame.setRoot(new Container(frame,0,0));
            return;
        }


        this.skillKey = skillKey;
        //System.out.println("setting up UI element");

        RenderableElement element = getCastableSkill().getCastElement(frame);

        if(element != null){
            //System.out.println("adding ui element");
            frame.setRoot(element);
        }
        this.castData = getCastableSkill().freshCastData();

        getCastableSkill().initialCast(entity,hotBar.getPreCastData(hotBar.getSlot(skillKey)));

    }
    public void setCastData(ICastData castData){
        this.castData = castData;
    }

    public boolean isCasting(){
        return skillKey != null;
    }
}
