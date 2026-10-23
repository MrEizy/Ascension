package net.zic.ascension.api.ascension.core.skill.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;

public class SkillSerializerHandler implements SerializerHandler<SkillHolder> {
    @Override
    public SkillHolder read(ValueInput input, RegistryAccess access) {
        SkillHolder holder = new SkillHolder();

        //skills are cached until their owners add them again
        ValueInput.ValueInputList skillsInput = input.childrenListOrEmpty("skills");
        for(ValueInput skillInput : skillsInput){
            try {
                SkillInstance<?> instance = SkillInstance.read(skillInput,access);
                if(instance == null) continue;
                AscensionCraft.LOGGER.debug("Reading Skill {}",instance.getSkillId());
                holder.addCachedSkill(instance);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error loading skill");
                AscensionCraft.LOGGER.debug("stacktrace: ",e);
            }
        }
        return holder;
    }

    @Override
    public void write(SkillHolder writable, ValueOutput output, RegistryAccess access) {

        ValueOutput.ValueOutputList skillOutputList = output.childrenList("skills");
        for(SkillInstance<?> instance : writable.getSkillInstances()){
            try {
                instance.write(skillOutputList.addChild(),access);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error writing Skill {}",instance.getSkillId());
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
    }
}
