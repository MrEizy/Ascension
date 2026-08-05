package net.zic.ascension.api.ascension.core.skill.castable.data;

public class CastStatus {
    public enum Reason {
        RELEASED,
        COMPLETED,
        CANCELLED,
        INTERRUPTED,
        OUT_OF_RESOURCE,
        INVALIDATED
    }

    private Reason reason;

    public void release() {
        reason = Reason.RELEASED;
    }

    public void complete() {
        reason = Reason.COMPLETED;
    }

    public void cancel() {
        reason = Reason.CANCELLED;
    }

    public void interrupt() {
        reason = Reason.INTERRUPTED;
    }

    public void outOfResource() {
        reason = Reason.OUT_OF_RESOURCE;
    }

    public void invalidate() {
        reason = Reason.INVALIDATED;
    }

    public void finish() {
        complete();
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public boolean isCasting() {
        return reason == null;
    }

    public boolean isReleased() {
        return reason == Reason.RELEASED;
    }

    public boolean isCompleted() {
        return reason == Reason.COMPLETED;
    }

    public boolean isCancelled() {
        return reason == Reason.CANCELLED;
    }

    public boolean isInterrupted() {
        return reason == Reason.INTERRUPTED;
    }

    public boolean isOutOfResource() {
        return reason == Reason.OUT_OF_RESOURCE;
    }

    public boolean isInvalidated() {
        return reason == Reason.INVALIDATED;
    }

    public boolean isFinished() {
        return reason != null;
    }

    public void resolve() {
        reason = null;
    }

    public Reason getReason() {
        return reason;
    }
}
