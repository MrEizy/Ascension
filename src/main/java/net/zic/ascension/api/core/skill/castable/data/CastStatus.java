package net.zic.ascension.api.core.skill.castable.data;

public class CastStatus {
    //TODO think about adding more reason, then expanding the method to a generic isReason()
    public enum Reason{
        NATURAL,
        CANCELLED
    }

    private Reason reason;

    public void cancel(){
        reason = Reason.CANCELLED;
    }
    public void finish(){
        reason = Reason.NATURAL;
    }

    public void setReason(Reason reason){
        this.reason = reason;
    }

    public boolean isCasting(){
        return reason == null;
    }
    public boolean isCancelled(){
        return reason == Reason.CANCELLED;
    }
    public boolean isFinished(){
        return reason == Reason.NATURAL;
    }

    public void resolve(){
        reason = null;
    }

    public Reason getReason(){
        return reason;
    }
}
