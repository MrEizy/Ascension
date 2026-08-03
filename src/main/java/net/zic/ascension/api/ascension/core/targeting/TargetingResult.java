package net.zic.ascension.api.ascension.core.targeting;

import net.minecraft.network.chat.Component;

import java.util.List;

public record TargetingResult(
        List<SkillTarget> targets,
        Component failureMessage
) {
    public TargetingResult {
        targets = targets == null ? List.of() : List.copyOf(targets);
    }

    public static TargetingResult success(List<SkillTarget> targets) {
        return new TargetingResult(targets, null);
    }

    public static TargetingResult success(SkillTarget target) {
        return new TargetingResult(target == null ? List.of() : List.of(target), null);
    }

    public static TargetingResult failure(Component message) {
        return new TargetingResult(List.of(), message);
    }

    public boolean succeeded() {
        return failureMessage == null;
    }

    public SkillTarget primaryTarget() {
        return targets.isEmpty() ? null : targets.getFirst();
    }
}
