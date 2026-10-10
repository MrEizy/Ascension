package net.zic.ascension.api.ascension.core.skill.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.zenithlib.nbt.NbtHelpers;

public class SkillSerializerHandler implements SerializerHandler<SkillHolder> {
    @Override
    public SkillHolder read(ValueInput input, RegistryAccess access) {
        SkillHolder holder = new SkillHolder();

        holder.clearCache();
        ValueInput.ValueInputList skillsInput = input.childrenListOrEmpty("skills");
        for(ValueInput skillInput : skillsInput){
            try {
                Identifier skillId = NbtHelpers.readIdentifier(skillInput,"skill");
                AscensionCraft.LOGGER.debug("Reading Skill {}",skillId);

                Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skillId,access);
                if(skill == null) continue;
                SkillData data = skillInput.child("data")
                        .map(valueInput -> skill.loadData(valueInput,access))
                        .orElse(skill.newData(access));

                holder.addCachedSkill(skillId,data);
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
        for(Identifier skill : writable.getSkills()){
            //AscensionCraft.LOGGER.debug("Saving Skill {}",skill);
            try {
                ValueOutput skillOutput = skillOutputList.addChild();

                NbtHelpers.writeIdentifier(skillOutput,"skill",skill);

                if(writable.getSkillData(skill) != null) writable.getSkillData(skill).write(skillOutput.child("data"),access);

            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error writing Skill {}",skill);
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
    }
}
