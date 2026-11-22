package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mob_traits.MobTraitRoll;

import java.util.ArrayList;
import java.util.List;

public record ResolvedMobTrait(MobTraitReference trait, MobTraitRoll roll) {
    public void applyToSource(OriginSource source, Identifier owner) {
        applyRollToSource(source, owner, roll);
    }

    public static void applyRollToSource(OriginSource source, Identifier owner, MobTraitRoll roll) {
        if (source == null) return;
        for (MobTraitRoll.PathRoll path : roll.paths()) {
            boolean existing = AscensionOriginSourceHelper.hasPath(source, path.path());
            if (!AscensionOriginSourceHelper.addPath(source, path.path(), owner) || existing) continue;
            PathInstance<?> instance = AscensionOriginSourceHelper.getPathInstance(source, path.path());
            Realm realm = Realm.of(path.majorRealm(), path.minorRealm());
            if (instance != null && instance.getCurrentRealm().compareTo(realm) < 0) {
                instance.handleRealmChange(realm, source);
            }
        }
        for (Identifier skill : roll.skills()) {
            AscensionOriginSourceHelper.addSkill(source, skill, owner);
        }
    }

    public void removeFromSource(OriginSource source, Identifier owner) {
        removeRollFromSource(source, owner, roll);
    }

    public static void removeRollFromSource(OriginSource source, Identifier owner, MobTraitRoll roll) {
        if (source == null) return;
        for (Identifier skill : roll.skills()) {
            AscensionOriginSourceHelper.removeSkill(source, skill, owner);
        }
        for (MobTraitRoll.PathRoll path : roll.paths()) {
            AscensionOriginSourceHelper.removePath(source, path.path(), owner);
        }
    }

    public void write(ValueOutput output) {
        output.putString("trait", trait.id().toString());
        writeRoll(output, roll);
    }

    public static void writeRoll(ValueOutput output, MobTraitRoll roll) {
        ValueOutput.ValueOutputList pathList = output.childrenList("paths");
        for (MobTraitRoll.PathRoll path : roll.paths()) {
            ValueOutput child = pathList.addChild();
            child.putString("id", path.path().toString());
            child.putInt("major", path.majorRealm());
            child.putInt("minor", path.minorRealm());
        }
        ValueOutput.ValueOutputList skillList = output.childrenList("skills");
        for (Identifier skill : roll.skills()) {
            skillList.addChild().putString("id", skill.toString());
        }
    }

    public static ResolvedMobTrait read(ValueInput input) {
        MobTraitReference trait = new MobTraitReference(Identifier.parse(input.getStringOr("trait", "none")));
        return new ResolvedMobTrait(trait, readRoll(input));
    }

    public static MobTraitRoll readRoll(ValueInput input) {
        List<MobTraitRoll.PathRoll> paths = new ArrayList<>();
        for (ValueInput child : input.childrenListOrEmpty("paths")) {
            try {
                Identifier path = Identifier.parse(child.getStringOr("id", "none"));
                int major = child.getIntOr("major", 0);
                int minor = child.getIntOr("minor", 0);
                if (major >= 0 && minor >= 0 && !path.getPath().equals("none")) {
                    MobTraitRoll.PathRoll selection = new MobTraitRoll.PathRoll(path, major, minor);
                    if (paths.stream().noneMatch(existing -> existing.path().equals(path))) paths.add(selection);
                }
            } catch (Exception ignored) {
            }
        }
        List<Identifier> skills = new ArrayList<>();
        for (ValueInput child : input.childrenListOrEmpty("skills")) {
            try {
                Identifier skill = Identifier.parse(child.getStringOr("id", "none"));
                if (!skill.getPath().equals("none") && !skills.contains(skill)) skills.add(skill);
            } catch (Exception ignored) {
            }
        }
        return new MobTraitRoll(paths, skills);
    }
}
