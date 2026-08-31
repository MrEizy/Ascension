package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class SkillActionScheduler {
    private static final List<Scheduled> TASKS = new ArrayList<>();

    private SkillActionScheduler() {
    }

    public static void schedule(SkillActionContext context, long delay, List<SkillAction> actions) {
        if (context == null || actions == null || actions.isEmpty()) {
            return;
        }
        TASKS.add(new Scheduled(
                context.level(),
                context.level().getGameTime() + Math.max(1L, delay),
                context.caster().getUUID(),
                context.target() == null ? null : context.target().getUUID(),
                context.skill(),
                context.position(),
                context.charge(),
                context.variables(),
                context.attribution(),
                List.copyOf(actions)
        ));
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Pre event) {
        List<Scheduled> due = new ArrayList<>();
        Iterator<Scheduled> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            Scheduled scheduled = iterator.next();
            if (scheduled.level().getGameTime() >= scheduled.executeAt()) {
                due.add(scheduled);
                iterator.remove();
            }
        }
        for (Scheduled scheduled : due) {
            Entity casterEntity = scheduled.level().getEntity(scheduled.caster());
            if (!(casterEntity instanceof LivingEntity caster) || caster.isRemoved() || !caster.isAlive()) {
                continue;
            }
            Entity targetEntity = scheduled.target() == null ? null : scheduled.level().getEntity(scheduled.target());
            LivingEntity target = targetEntity instanceof LivingEntity living && !living.isRemoved() ? living : null;
            SkillActionRuntime.execute(new SkillActionContext(
                    scheduled.level(), caster, scheduled.skill(), target, scheduled.position(), scheduled.charge(),
                    scheduled.variables(), scheduled.attribution()
            ), scheduled.actions());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TASKS.clear();
    }

    private record Scheduled(
            ServerLevel level, long executeAt, UUID caster, UUID target, net.minecraft.resources.Identifier skill,
            net.minecraft.world.phys.Vec3 position, double charge, java.util.Map<net.minecraft.resources.Identifier, Double> variables,
            net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionAttribution attribution, List<SkillAction> actions
    ) {
    }
}
