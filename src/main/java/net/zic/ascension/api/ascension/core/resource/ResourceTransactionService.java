package net.zic.ascension.api.ascension.core.resource;

import net.zic.ascension.api.ascension.value.ScaledValue;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.event.resource.ResourceTransactionEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ResourceTransactionService {
    private static final int MAX_TRANSACTION_DEPTH = 16;
    private static final ThreadLocal<Deque<ActiveTransaction>> ACTIVE_TRANSACTIONS = ThreadLocal.withInitial(ArrayDeque::new);

    private ResourceTransactionService() {
    }

    public static Result consume(net.minecraft.world.entity.LivingEntity entity, Identifier resource, ResourceSourceIdentity source, double amount) {
        return transact(entity, resource, ResourceOperation.CONSUME, source, amount);
    }

    public static Result accumulate(net.minecraft.world.entity.LivingEntity entity, Identifier resource, ResourceSourceIdentity source, double amount) {
        return transact(entity, resource, ResourceOperation.ACCUMULATE, source, amount);
    }

    public static Result restore(net.minecraft.world.entity.LivingEntity entity, Identifier resource, ResourceSourceIdentity source, double amount) {
        return transact(entity, resource, ResourceOperation.RESTORE, source, amount);
    }

    public static Result generate(net.minecraft.world.entity.LivingEntity entity, Identifier resource, ResourceSourceIdentity source, double amount) {
        return transact(entity, resource, ResourceOperation.GENERATE, source, amount);
    }

    public static Result drain(net.minecraft.world.entity.LivingEntity entity, Identifier resource, ResourceSourceIdentity source, double amount) {
        return transact(entity, resource, ResourceOperation.DRAIN, source, amount);
    }

    private static Result transact(net.minecraft.world.entity.LivingEntity entity, Identifier resource, ResourceOperation operation, ResourceSourceIdentity source, double amount) {
        return transact(ResourceTransactionRequest.of(entity, resource, operation, amount, source));
    }

    public static Result transact(ResourceTransactionRequest request) {
        Result invalid = validateRequest(request);
        if (invalid != null) {
            return invalid;
        }
        ResourceType resourceType = ResourceRegistries.RESOURCE_TYPE_REGISTRY.getValue(request.resource());
        if (resourceType == null || !resourceType.supports(request.entity())) {
            return simpleResult(request, null, Status.UNSUPPORTED, 0.0D, 0.0D, 0.0D);
        }
        Deque<ActiveTransaction> active = ACTIVE_TRANSACTIONS.get();
        RecursionKey recursionKey = RecursionKey.of(request);
        if (active.size() >= MAX_TRANSACTION_DEPTH) {
            double current = resourceType.getAmount(request.entity());
            return simpleResult(request, resourceType, Status.RECURSION_BLOCKED, current, current, 0.0D);
        }
        if (!request.hasFlag(ResourceTransactionRequest.Flag.ALLOW_REENTRY)
                && active.stream().anyMatch(transaction -> transaction.key.equals(recursionKey))) {
            double current = resourceType.getAmount(request.entity());
            return simpleResult(request, resourceType, Status.RECURSION_BLOCKED, current, current, 0.0D);
        }
        ActiveTransaction parent = active.peek();
        UUID transactionId = UUID.randomUUID();
        double amountBefore = resourceType.getAmount(request.entity());
        Context context = new Context(
                transactionId,
                parent == null ? null : parent.id,
                active.size(),
                request,
                resourceType,
                amountBefore
        );
        active.push(new ActiveTransaction(transactionId, recursionKey));
        try {
            if (!request.hasFlag(ResourceTransactionRequest.Flag.BYPASS_EVENTS)) {
                ResourceTransactionEvent.PreValidation pre = new ResourceTransactionEvent.PreValidation(context);
                NeoForge.EVENT_BUS.post(pre);
                if (pre.isCanceled()) {
                    return finish(new Result(context, Status.CANCELLED, request.amount(), 0.0D, 0.0D, amountBefore, amountBefore));
                }
            }
            ResourceModifiers.Collector collector = new ResourceModifiers.Collector();
            if (!request.hasFlag(ResourceTransactionRequest.Flag.BYPASS_MODIFIERS)
                    && !request.hasFlag(ResourceTransactionRequest.Flag.BYPASS_EVENTS)) {
                NeoForge.EVENT_BUS.post(new ResourceTransactionEvent.Modify(context, collector));
            }
            ResourceModifiers.Resolution modifierResolution = collector.resolve(request.amount());
            if (modifierResolution.immune()) {
                return finish(new Result(context, Status.IMMUNE, request.amount(), 0.0D, 0.0D, amountBefore, amountBefore));
            }
            if (modifierResolution.cancelled()) {
                return finish(new Result(context, Status.CANCELLED, request.amount(), 0.0D, 0.0D, amountBefore, amountBefore));
            }
            double resolvedAmount = modifierResolution.amount();
            ResourceOperation.Application application = request.operation().apply(
                    resourceType,
                    request.entity(),
                    resolvedAmount,
                    request.hasFlag(ResourceTransactionRequest.Flag.SIMULATE)
            );
            return finish(new Result(
                    context,
                    application.status(),
                    request.amount(),
                    resolvedAmount,
                    application.appliedAmount(),
                    application.amountBefore(),
                    application.amountAfter()
            ));
        } finally {
            active.pop();
            if (active.isEmpty()) {
                ACTIVE_TRANSACTIONS.remove();
            }
        }
    }

    private static Result finish(Result result) {
        if (!result.context().request().hasFlag(ResourceTransactionRequest.Flag.BYPASS_EVENTS)) {
            NeoForge.EVENT_BUS.post(new ResourceTransactionEvent.Post(result));
        }
        return result;
    }

    private static Result validateRequest(ResourceTransactionRequest request) {
        if (request == null
                || request.entity() == null
                || request.resource() == null
                || request.operation() == null
                || request.source() == null
                || !Double.isFinite(request.amount())
                || request.amount() < 0.0D) {
            return simpleResult(request, null, Status.INVALID, 0.0D, 0.0D, 0.0D);
        }
        if (request.entity().level().isClientSide() && !request.hasFlag(ResourceTransactionRequest.Flag.SIMULATE)) {
            return simpleResult(request, null, Status.REJECTED, 0.0D, 0.0D, 0.0D);
        }
        return null;
    }

    private static Result simpleResult(
            ResourceTransactionRequest request,
            ResourceType resourceType,
            Status status,
            double amountBefore,
            double amountAfter,
            double appliedAmount
    ) {
        Context context = request == null ? null : new Context(
                UUID.randomUUID(),
                null,
                0,
                request,
                resourceType,
                amountBefore
        );
        return new Result(
                context,
                status,
                request == null ? 0.0D : request.amount(),
                0.0D,
                appliedAmount,
                amountBefore,
                amountAfter
        );
    }

    public enum Status {
        SUCCESS,
        PARTIAL,
        CANCELLED,
        IMMUNE,
        INVALID,
        UNSUPPORTED,
        REJECTED,
        RECURSION_BLOCKED
    }

    public record Context(
            UUID transactionId,
            UUID parentTransactionId,
            int depth,
            ResourceTransactionRequest request,
            ResourceType resourceType,
            double amountBefore
    ) {
        public ScaledValue.Context scaledValueContext(OriginSource source, Identifier skillId) {
            Map<Identifier, Double> variables = new HashMap<>(request.values());
            variables.put(AscensionCraft.prefix("resource/requested_amount"), request.amount());
            variables.put(AscensionCraft.prefix("resource/current_amount"), amountBefore);
            variables.put(AscensionCraft.prefix("resource/maximum_amount"), resourceType.getMaximum(request.entity()));
            variables.put(AscensionCraft.prefix("resource/transaction_depth"), (double) depth);
            double castProgress = variables.getOrDefault(AscensionCraft.prefix("cast/progress"), 0.0D);
            return new ScaledValue.Context(source, skillId, request.entity(), request.target(), castProgress, variables);
        }
    }

    public record Result(
            Context context,
            Status status,
            double requestedAmount,
            double resolvedAmount,
            double appliedAmount,
            double amountBefore,
            double amountAfter
    ) {
        public boolean succeeded() {
            return status == Status.SUCCESS || status == Status.PARTIAL;
        }

        public boolean wasPrevented() {
            return status == Status.CANCELLED || status == Status.IMMUNE;
        }
    }

    private record ActiveTransaction(UUID id, RecursionKey key) {
    }

    private record RecursionKey(UUID entity, Identifier resource, ResourceOperation operation, Object source) {
        private static RecursionKey of(ResourceTransactionRequest request) {
            return new RecursionKey(
                    request.entity().getUUID(),
                    request.resource(),
                    request.operation(),
                    request.source()
            );
        }
    }
}
