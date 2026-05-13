package net.zic.ascension.refactor_packages.skills;

import net.zic.ascension.refactor_packages.util.IDataInstance;

public interface IPersistentSkillData extends IDataInstance {



    IPersistentSkillData copy();

    IPersistentSkillData merge(IPersistentSkillData other);
}
