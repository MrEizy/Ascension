package net.zic.ascension.api.ascension.core.resource;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.ascension.core.resource.modifier.ResourceModifierCollector;
import net.zic.ascension.api.ascension.core.resource.modifier.ResourceModifierResolution;
import net.zic.ascension.api.ascension.event.resource.ResourceTransactionEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public final class ResourceTransactionService {
    private static final int MAX_TRANSACTION_DEPTH = 16;
    private static final ThreadLocal<Deque<ActiveTransaction>> ACTIVE_TRANSACTIONS = ThreadLocal.withInitial(ArrayDeque::new);

    private ResourceTransactionService() {
    }

    public static ResourceTransactionResult transact(ResourceTransactionRequest request) {
        ResourceTransactionResult invalid = validateRequest(request);
        if (invalid != null) {
            return invalid;
        }

        ResourceType resourceType = ResourceRegistries.RESOURCE_TYPE_REGISTRY.getValue(request.resource());
        if (resourceType == null || !resourceType.supports(request.entity())) {
            return simpleResult(request, null, ResourceTransactionStatus.UNSUPPORTED, 0.0D, 0.0D, 0.0D);
        }
        if (!resourceType.supports(request.operation())) {
            double current = resourceType.getAmount(request.entity());
            return simpleResult(request, resourceType, ResourceTransactionStatus.UNSUPPORTED, current, current, 0.0D);
        }

        Deque<ActiveTransaction> active = ACTIVE_TRANSACTIONS.get();
        RecursionKey recursionKey = RecursionKey.of(request);
        if (active.size() >= MAX_TRANSACTION_DEPTH) {
            double current = resourceType.getAmount(request.entity());
            return simpleResult(request, resourceType, ResourceTransactionStatus.RECURSION_BLOCKED, current, current, 0.0D);
        }
        if (!request.hasFlag(ResourceTransactionFlag.ALLOW_REENTRY)
                && active.stream().anyMatch(transaction -> transaction.key.equals(recursionKey))) {
            double current = resourceType.getAmount(request.entity());
            return simpleResult(request, resourceType, ResourceTransactionStatus.RECURSION_BLOCKED, current, current, 0.0D);
        }

        ActiveTransaction parent = active.peek();
        UUID transactionId = UUID.randomUUID();
        double amountBefore = resourceType.getAmount(request.entity());
        ResourceTransactionContext context = new ResourceTransactionContext(
                transactionId,
                parent == null ? null : parent.id,
                active.size(),
                request,
                resourceType,
                amountBefore
        );
        active.push(new ActiveTransaction(transactionId, recursionKey));

        try {
            if (!request.hasFlag(ResourceTransactionFlag.BYPASS_EVENTS)) {
                ResourceTransactionEvent.PreValidation pre = new ResourceTransactionEvent.PreValidation(context);
                NeoForge.EVENT_BUS.post(pre);
                if (pre.isCanceled()) {
                    return finish(new ResourceTransactionResult(
                            context,
                            ResourceTransactionStatus.CANCELLED,
                            request.amount(),
                            0.0D,
                            0.0D,
                            amountBefore,
                            amountBefore
                    ));
                }
            }

            ResourceModifierCollector collector = new ResourceModifierCollector();
            if (!request.hasFlag(ResourceTransactionFlag.BYPASS_MODIFIERS)
                    && !request.hasFlag(ResourceTransactionFlag.BYPASS_EVENTS)) {
                NeoForge.EVENT_BUS.post(new ResourceTransactionEvent.Modify(context, collector));
            }

            ResourceModifierResolution modifierResolution = collector.resolve(request.amount());
            if (modifierResolution.immune()) {
                return finish(new ResourceTransactionResult(
                        context,
                        ResourceTransactionStatus.IMMUNE,
                        request.amount(),
                        0.0D,
                        0.0D,
                        amountBefore,
                        amountBefore
                ));
            }
            if (modifierResolution.cancelled()) {
                return finish(new ResourceTransactionResult(
                        context,
                        ResourceTransactionStatus.CANCELLED,
                        request.amount(),
                        0.0D,
                        0.0D,
                        amountBefore,
                        amountBefore
                ));
            }

            double resolvedAmount = modifierResolution.amount();
            ResourceApplicationResult application = resourceType.apply(
                    request.entity(),
                    request.operation(),
                    resolvedAmount,
                    request.hasFlag(ResourceTransactionFlag.SIMULATE)
            );
            ResourceTransactionResult result = new ResourceTransactionResult(
                    context,
                    application.status(),
                    request.amount(),
                    resolvedAmount,
                    application.appliedAmount(),
                    application.amountBefore(),
                    application.amountAfter()
            );
            return finish(result);
        } finally {
            active.pop();
            if (active.isEmpty()) {
                ACTIVE_TRANSACTIONS.remove();
            }
        }
    }

    private static ResourceTransactionResult finish(ResourceTransactionResult result) {
        if (!result.context().request().hasFlag(ResourceTransactionFlag.BYPASS_EVENTS)) {
            NeoForge.EVENT_BUS.post(new ResourceTransactionEvent.Post(result));
        }
        return result;
    }

    private static ResourceTransactionResult validateRequest(ResourceTransactionRequest request) {
        if (request == null
                || request.entity() == null
                || request.resource() == null
                || request.operation() == null
                || request.source() == null
                || !Double.isFinite(request.amount())
                || request.amount() < 0.0D) {
            return simpleResult(request, null, ResourceTransactionStatus.INVALID, 0.0D, 0.0D, 0.0D);
        }
        if (request.entity().level().isClientSide() && !request.hasFlag(ResourceTransactionFlag.SIMULATE)) {
            return simpleResult(request, null, ResourceTransactionStatus.REJECTED, 0.0D, 0.0D, 0.0D);
        }
        return null;
    }

    private static ResourceTransactionResult simpleResult(
            ResourceTransactionRequest request,
            ResourceType resourceType,
            ResourceTransactionStatus status,
            double amountBefore,
            double amountAfter,
            double appliedAmount
    ) {
        ResourceTransactionContext context = request == null
                ? null
                : new ResourceTransactionContext(
                        UUID.randomUUID(),
                        null,
                        0,
                        request,
                        resourceType,
                        amountBefore
                );
        return new ResourceTransactionResult(
                context,
                status,
                request == null ? 0.0D : request.amount(),
                0.0D,
                appliedAmount,
                amountBefore,
                amountAfter
        );
    }

    private record ActiveTransaction(UUID id, RecursionKey key) {
    }

    private record RecursionKey(
            UUID entity,
            Identifier resource,
            ResourceOperation operation,
            Identifier source
    ) {
        private static RecursionKey of(ResourceTransactionRequest request) {
            return new RecursionKey(
                    request.entity().getUUID(),
                    request.resource(),
                    request.operation(),
                    request.source().id()
            );
        }
    }
}
