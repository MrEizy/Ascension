package net.zic.ascension.api.core.skill.castable.data;

import net.minecraft.network.chat.Component;

public class CastResult {
    public static CastResult success(){
        return new CastResult(Type.SUCCESS);
    }
    public static CastResult success(Component message){
        return new CastResult(Type.SUCCESS,message);
    }
    public static CastResult fail(){
        return new CastResult(Type.FAILURE);
    }
    public static CastResult fail(Component message){
        return new CastResult(Type.FAILURE,message);
    }

    public enum Type {
        SUCCESS,
        FAILURE
    }

    public final Type type;
    public final Component message;

    private CastResult(Type type) {
        this(type, null);
    }

    private CastResult(Type type, Component message) {
        this.type = type;
        this.message = message;
    }

    public boolean isSuccess() {
        return this.type == Type.SUCCESS;
    }
}
