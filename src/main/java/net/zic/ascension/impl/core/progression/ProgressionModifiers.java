package net.zic.ascension.impl.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.progression.modifier.ModifierConflictAction;
import net.zic.ascension.impl.core.progression.modifier.ModifierSlot;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Shared logic for the stat and path bonus progress actions.
 */
public final class ProgressionModifiers {
    private ProgressionModifiers() {
    }

    public static void applyStats(OriginSource source, Map<Identifier, ModifierHolder<Double>> stats, ModifierConflictAction action,
                                  UnaryOperator<Modifier<Double>> transform, int direction, String salt) {
        for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : stats.entrySet()) {
            Stat stat = ZenithStatHelper.stat(modifiers.getKey());
            Supplier<ValueContainer<Double>> container = () -> source.getStatInstance(stat);
            action.applyAll(
                    modifiers.getValue(),
                    flatSlot(container, modifier -> source.addFlatStatModifier(stat, modifier), id -> source.removeStatModifier(stat, id)),
                    multiplierSlot(container, modifier -> source.addMultiplierStatModifier(stat, modifier), id -> source.removeStatModifier(stat, id)),
                    transform, transform, direction, salt
            );
        }
        source.updateEntityStatHolder();
    }

    public static void applyPathBonuses(OriginSource source, Map<PathBonus, ModifierHolder<Double>> bonuses, ModifierConflictAction action,
                                        UnaryOperator<Modifier<Double>> transform, int direction, String salt) {
        for (Map.Entry<PathBonus, ModifierHolder<Double>> modifiers : bonuses.entrySet()) {
            PathBonus bonus = modifiers.getKey();
            Supplier<ValueContainer<Double>> container = () -> AscensionOriginSourceHelper.getPathBonusContainer(source, bonus.category(), bonus.path());
            Consumer<Identifier> remove = id -> AscensionOriginSourceHelper.removeBonusModifier(source, bonus.category(), bonus.path(), id);
            action.applyAll(
                    modifiers.getValue(),
                    flatSlot(container, modifier -> AscensionOriginSourceHelper.addBonusFlatModifier(source, bonus.category(), bonus.path(), modifier), remove),
                    multiplierSlot(container, modifier -> AscensionOriginSourceHelper.addBonusMultiplierModifier(source, bonus.category(), bonus.path(), modifier), remove),
                    transform, transform, direction, salt
            );
        }
    }

    /**
     * Runs a purity scaled application. REPLACE is applied from the purity after the change,
     * so a purity drop re-applies the modifier at the lower purity instead of only removing it.
     *
     * @param apply receives the value transform and the direction to apply with
     */
    public static void applyWithPurity(ModifierConflictAction action, BloodlineData data, int purity, ProgressDirection direction,
                                       PurityApplication apply) {
        if (action == ModifierConflictAction.REPLACE) {
            int newPurity = data.getPurity();
            if (newPurity > 0) apply.apply(scale(newPurity), ProgressDirection.UP.value);
            else apply.apply(UnaryOperator.identity(), ProgressDirection.DOWN.value);
            return;
        }
        apply.apply(scale(purity), direction.value);
    }

    @FunctionalInterface
    public interface PurityApplication {
        void apply(UnaryOperator<Modifier<Double>> transform, int direction);
    }

    private static UnaryOperator<Modifier<Double>> scale(int purity) {
        return modifier -> ModifierConflictAction.withValue(modifier, modifier.value() * purity);
    }

    private static ModifierSlot<Double> flatSlot(Supplier<ValueContainer<Double>> container, Consumer<Modifier<Double>> add, Consumer<Identifier> remove) {
        return ModifierSlot.ofDoubles(
                id -> container.get() == null ? null : container.get().getFlatModifier(id),
                id -> container.get() != null && container.get().hasModifier(id),
                add, remove
        );
    }

    private static ModifierSlot<Double> multiplierSlot(Supplier<ValueContainer<Double>> container, Consumer<Modifier<Double>> add, Consumer<Identifier> remove) {
        return ModifierSlot.ofDoubles(
                id -> container.get() == null ? null : container.get().getMultiplierModifier(id),
                id -> container.get() != null && container.get().hasModifier(id),
                add, remove
        );
    }
}
