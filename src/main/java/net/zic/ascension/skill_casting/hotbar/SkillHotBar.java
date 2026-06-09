package net.zic.ascension.skill_casting.hotbar;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.castable.CastableSkill;
import net.zic.ascension.api.core.skill.castable.PreCastData;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.List;

//TODO update to include a config option for skill slots
public class SkillHotBar {

    private final SkillHotBarSlot[] slots;

    private int selectedSlot;
    private boolean dirty;

    public SkillHotBar(){
        slots = new SkillHotBarSlot[getMaxSlots()];
        for (int i = 0; i < slots.length; i++) slots[i] = new SkillHotBarSlot();
    }

    public Identifier getSkill(int slot){
        return slots[slot].getSkill();
    }

    public PreCastData getPreCastData(int slot){
        return slots[slot].getPreCastData();
    }

    public int getSelectedSlot(){
        return selectedSlot;
    }

    public void slotSkill(LivingEntity entity,Identifier skill,int slot){
        slots[slot].setSkill(entity,skill);
        markDirty();
    }


    public void select(LivingEntity entity,int slot){
        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,getSkill(selectedSlot),entity.level().registryAccess()) instanceof CastableSkill castableSkill)) return;

        castableSkill.unselected(entity,getPreCastData(selectedSlot));

        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,getSkill(selectedSlot),entity.level().registryAccess()) instanceof CastableSkill newCastable)) return;


        selectedSlot = slot;
        newCastable.selected(
                entity,
                getPreCastData(slot)
        );
        markDirty();
    }

    public void validateSlots(LivingEntity entity){
        //TODO

        markDirty();
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
    public int getMaxSlots(){
        return 6;
    }

    public void write(ValueOutput output){
        output.putInt("selected",selectedSlot);
        NbtHelpers.writeArray(output,"slots",slots,(holder,id,val)->{
            if(val.getSkill() != null){
                NbtHelpers.writeIdentifier(holder,"skill",val.getSkill());
                if(val.getPreCastData() != null) val.getPreCastData().write(holder.child("pre_cast_data"));
            }
        });
    }
    public void load(ValueInput input,LivingEntity entity){
        List<SkillHotBarSlot> loadedSlots = NbtHelpers.readList(input,"slots",(holder,id)->{
            SkillHotBarSlot slot = new SkillHotBarSlot();
            if(holder.getString("skill").isEmpty()) return slot;
            Identifier skill = NbtHelpers.readIdentifier(holder,"skill");
            if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,entity.level().registryAccess()) instanceof CastableSkill castableSkill)) return slot;

            PreCastData data = castableSkill.loadPreCastData(holder.childOrEmpty("pre_cast_data"));

            slot.setSkill(entity,skill,data);
            return slot;
        });
        for(int i = 0;i<slots.length;i++){
            slots[i] = loadedSlots.get(i);
        }
        select(entity,input.getIntOr("selected",0));
    }
    public void encode(ByteBuf buf){
        ByteBufHelpers.encodeArray(slots,buf,((val, buf1) -> {
            buf1.writeBoolean(val.getSkill() != null);
            if(val.getSkill() != null){
                ByteBufHelpers.encodeIdentifier(val.getSkill(),buf1);
                buf1.writeBoolean(val.getPreCastData() != null);
                if(val.getPreCastData() != null) val.getPreCastData().encode(buf1);
            }
        }));
        buf.writeInt(selectedSlot);
    }
    public void decode(RegistryFriendlyByteBuf buf, LivingEntity entity){
        List<SkillHotBarSlot> newSlots = ByteBufHelpers.decodeArray(buf,(buf1 -> {
            SkillHotBarSlot slot = new SkillHotBarSlot();
            if(!buf1.readBoolean()) return slot;
            Identifier skill = ByteBufHelpers.decodeIdentifier(buf1);
            if(!(CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skill,
                    buf.registryAccess()

            )instanceof CastableSkill castableSkill)) return slot;
            slot.setSkill(
                entity,
                skill,
                castableSkill.loadPreCastData(buf1)
            );
            return slot;
        }));

        for(int i = 0;i<slots.length;i++){
            slots[i] = newSlots.get(i);
        }
        select(entity,buf.readInt());
    }
}
