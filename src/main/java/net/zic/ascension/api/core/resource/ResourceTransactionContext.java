package net.zic.ascension.api.core.resource;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.value.ScaledValueContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record ResourceTransactionContext(
        UUID transactionId,
        UUID parentTransactionId,
        int depth,
        ResourceTransactionRequest request,
        ResourceType resourceType,
        double amountBefore
) {
    public ScaledValueContext scaledValueContext(OriginSource source, Identifier skillId) {
        Map<Identifier, Double> variables = new HashMap<>(request.values());
        variables.put(AscensionCraft.prefix("resource/requested_amount"), request.amount());
        variables.put(AscensionCraft.prefix("resource/current_amount"), amountBefore);
        variables.put(
                AscensionCraft.prefix("resource/maximum_amount"),
                resourceType.getMaximum(request.entity())
        );
        variables.put(AscensionCraft.prefix("resource/transaction_depth"), (double) depth);
        double charge = variables.getOrDefault(AscensionCraft.prefix("cast/charge"), 0.0D);
        return new ScaledValueContext(
                source,
                skillId,
                request.entity(),
                request.target(),
                charge,
                variables
        );
    }
}
