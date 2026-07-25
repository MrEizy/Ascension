package net.zic.ascension.api.event.resource;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.core.resource.ResourceTransactionContext;
import net.zic.ascension.api.core.resource.ResourceTransactionResult;
import net.zic.ascension.api.core.resource.modifier.ResourceModifierCollector;

public abstract class ResourceTransactionEvent extends Event {
    private final ResourceTransactionContext context;

    protected ResourceTransactionEvent(ResourceTransactionContext context) {
        this.context = context;
    }

    public ResourceTransactionContext getContext() {
        return context;
    }

    public static final class PreValidation extends ResourceTransactionEvent implements ICancellableEvent {
        public PreValidation(ResourceTransactionContext context) {
            super(context);
        }
    }

    public static final class Modify extends ResourceTransactionEvent {
        private final ResourceModifierCollector collector;

        public Modify(ResourceTransactionContext context, ResourceModifierCollector collector) {
            super(context);
            this.collector = collector;
        }

        public ResourceModifierCollector getCollector() {
            return collector;
        }
    }

    public static final class Post extends ResourceTransactionEvent {
        private final ResourceTransactionResult result;

        public Post(ResourceTransactionResult result) {
            super(result.context());
            this.result = result;
        }

        public ResourceTransactionResult getResult() {
            return result;
        }
    }
}
