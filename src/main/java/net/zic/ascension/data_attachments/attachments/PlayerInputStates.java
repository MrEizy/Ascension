package net.zic.ascension.data_attachments.attachments;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;

//stores player input states, essential isHeld
//does not store key but an input id
public class PlayerInputStates {

    //integer here is modifiers
    private final HashMap<String,Integer> inputs = new HashMap<>();
    public final Player player;

    public PlayerInputStates(Player player){
        this.player = player;
    }

    public boolean isHeld(String input){
        return inputs.containsKey(input);
    }
    public int getModifier(String input){
        return inputs.get(input);
    }

    public void inputDown(String input,int modifier){
        inputs.put(input,modifier);
        NeoForge.EVENT_BUS.post(new InputStateChanged(player,input,modifier,InputState.PRESSED));
    }
    public void inputReleased(String input){
        inputs.remove(input);
        NeoForge.EVENT_BUS.post(new InputStateChanged(player,input,0,InputState.RELEASED));
    }

    public enum InputState{
        PRESSED,
        DOWN,
        RELEASED
    }

    public static class InputStateChanged extends Event {
        public final Player player;
        public final String input;
        public final int modifier;
        public final InputState state;
        public InputStateChanged(Player player,String input, int modifier, InputState state){
            this.player = player;
            this.input = input;
            this.modifier = modifier;
            this.state = state;
        }
    }
}
