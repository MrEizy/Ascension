package net.zic.ascension.refactor_packages.skills.castable;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.zic.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.zic.ascension.refactor_packages.skills.IPersistentSkillData;
import net.zic.ascension.refactor_packages.skills.ISkill;

public interface ICastableSkill extends ISkill {

    void onEquip(IEntityData entityData);
    void onUnEquip(IEntityData entityData,IPreCastData preCastData);

    //called after continue and initial
    void finalCast(CastEndData reason, Entity caster,ICastData castData);
    //called before continue casting
    void initialCast(Entity caster,IPreCastData preCastData);

    /*
        done on the server, this is where you do things like make sure there are valid targets,
        and that the amount of qi is valid(should reduce qi in this method
     */
    CastResult canCast(Entity caster, IPreCastData preCastData);

    boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData);

    int getCooldown(CastEndData castEndData);//measured in ticks


    /*
        these are called on both the client and server so make sure to check before handling
        should be used to handle stuff like extra input handlers or UI elements
     */
    void selected(IEntityData entityData);
    void unselected(IEntityData entityData);

    IPreCastData freshPreCastData();
    IPreCastData preCastDataFromCompound(CompoundTag tag);
    IPreCastData preCastDataFromNetwork(RegistryFriendlyByteBuf buf);

    ICastData freshCastData();
    ICastData castDataFromCompound(CompoundTag tag);
    ICastData castDataFromNetwork(RegistryFriendlyByteBuf buf);

    IPersistentSkillData freshPersistentInstance();
    IPersistentSkillData persistentInstanceFromCompound(CompoundTag tag);
    IPersistentSkillData persistentInstanceFromNetwork(RegistryFriendlyByteBuf buf);



    CastType getCastType();

    /**
       when initial cast is called on the client this element is constructed and added to the cast overlay
        the generic behaviour is to behave as a cast bar
     */
    @OnlyIn(Dist.CLIENT)
    RenderableElement getCastElement(UIFrame frame);
}
