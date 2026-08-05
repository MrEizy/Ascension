package net.zic.ascension.api.ascension.event.resource;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.resource.ResourceModifiers;

public abstract class ResourceTransactionEvent extends Event {
    private final ResourceTransactionService.Context context;

    protected ResourceTransactionEvent(ResourceTransactionService.Context context) {
        this.context = context;
    }

    public ResourceTransactionService.Context getContext() {
        return context;
    }

    public static final class PreValidation extends ResourceTransactionEvent implements ICancellableEvent {
        public PreValidation(ResourceTransactionService.Context context) {
            super(context);
        }
    }

    public static final class Modify extends ResourceTransactionEvent {
        private final ResourceModifiers.Collector collector;

        public Modify(ResourceTransactionService.Context context, ResourceModifiers.Collector collector) {
            super(context);
            this.collector = collector;
        }

        public ResourceModifiers.Collector getCollector() {
            return collector;
        }
    }

    public static final class Post extends ResourceTransactionEvent {
        private final ResourceTransactionService.Result result;

        public Post(ResourceTransactionService.Result result) {
            super(result.context());
            this.result = result;
        }

        public ResourceTransactionService.Result getResult() {
            return result;
        }
    }
}
