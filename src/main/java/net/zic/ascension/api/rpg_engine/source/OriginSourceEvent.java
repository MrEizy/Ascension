package net.zic.ascension.api.rpg_engine.source;

import net.neoforged.bus.api.Event;

public abstract class OriginSourceEvent extends Event {
    private final OriginSource source;

    public OriginSourceEvent(OriginSource source){
        this.source = source;
    }

    public OriginSource getSource() {
        return source;
    }

    public static class OriginSourceFinishedLoadingEvent extends OriginSourceEvent{

        public OriginSourceFinishedLoadingEvent(OriginSource source) {
            super(source);
        }
    }
}
