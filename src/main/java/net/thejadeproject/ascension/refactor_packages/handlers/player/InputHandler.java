package net.thejadeproject.ascension.refactor_packages.handlers.player;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.overaly.EasyOverlayHandler;
import net.lucent.easygui.screen.EasyScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.network.serverBound.input.ChangePlayerInputState;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.IntrospectionContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skill_casting.SkillHotBarContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skill_view.SkillMenuContainer;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

import static net.thejadeproject.ascension.util.KeyBindHandler.CULTIVATION_CATEGORY;

//lets us dynamically add and remove "actions" associated with inputs
//these actions are then synced with the server
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = AscensionCraft.MOD_ID,value = Dist.CLIENT)
public class InputHandler {
    public static final KeyMapping CAST_SKILL_KEY = new KeyMapping("key.ascension.cast_skill", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, CULTIVATION_CATEGORY);
    public static final KeyMapping SKILL_WHEEL_OVERLAY = new KeyMapping("key.ascension.skill_wheel", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_R, CULTIVATION_CATEGORY);
    public static final KeyMapping INTROSPECTION = new KeyMapping("key.ascension.introspection", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_I, CULTIVATION_CATEGORY);

    public final static HashSet<KeyMapping> state = new HashSet<>();
    //maps a keyMapping->handler
    private final static HashMap<KeyMapping, ActionHandler> actionHandlerMapping = new HashMap<>(){{
        put(CAST_SKILL_KEY,new ActionHandler("skill_cast").setOnDown((mod)-> {
            //System.out.println("pressed skill cast key");

        }));
        put(SKILL_WHEEL_OVERLAY,new ActionHandler("skill_wheel").setOnDown(mod->{
            ((SkillHotBarContainer) EasyOverlayHandler.getFrame(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"skill_wheel")).getRoot()).open();
        }).setOnRelease(mod->{
            ((SkillHotBarContainer) EasyOverlayHandler.getFrame(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"skill_wheel")).getRoot()).close();
        }));
        put(INTROSPECTION,new ActionHandler("open_introspection")
                .setOnDown(mod->{
                    UIFrame frame = new UIFrame();
                    frame.setRoot(new IntrospectionContainer(frame));
                    Minecraft.getInstance().setScreen( new EasyScreen(Component.literal("Introspection"),frame));
                })
        );
    }};
    public static class ActionHandler {
        public Consumer<Integer> actionDown = (val)->{};
        public Consumer<Integer> actionHeld =  (val)->{};
        public Consumer<Integer> actionReleased =  (val)->{};
        public String actionName;

        public ActionHandler(String name){
            this.actionName = name;
        }
        public ActionHandler setOnDown(Consumer<Integer> inputEvent){
            this.actionDown = inputEvent;
            return this;
        }
        public ActionHandler setOnHeld(Consumer<Integer> inputEvent){
            this.actionHeld = inputEvent;
            return this;
        }
        public ActionHandler setOnRelease(Consumer<Integer> inputEvent){
            this.actionReleased = inputEvent;
            return this;
        }

        public static void sendSatePacket(String name,int modifier,boolean isDown){
            PacketDistributor.sendToServer(new ChangePlayerInputState(name,modifier,isDown));
        }
    }


    public static void sendSatePacket(String name,int modifier,boolean isDown){
        PacketDistributor.sendToServer(new ChangePlayerInputState(name,modifier,isDown));
    }
    public void removeKeyMapping(KeyMapping mapping){
        state.remove(mapping);
        sendSatePacket(actionHandlerMapping.remove(mapping).actionName,0,false);
    }
    public void setActionDown(KeyMapping mapping,String actionName, Consumer<Integer> inputEven){
        actionHandlerMapping.computeIfAbsent(mapping,key->new ActionHandler(actionName));
        actionHandlerMapping.get(mapping).setOnDown(inputEven);
    }
    public void setActionHeld(KeyMapping mapping,String actionName, Consumer<Integer> inputEven){
        actionHandlerMapping.computeIfAbsent(mapping,key->new ActionHandler(actionName));
        actionHandlerMapping.get(mapping).setOnHeld(inputEven);
    }
    public void setActionRelease(KeyMapping mapping,String actionName, Consumer<Integer> inputEven){
        actionHandlerMapping.computeIfAbsent(mapping,key->new ActionHandler(actionName));
        actionHandlerMapping.get(mapping).setOnRelease(inputEven);
    }



    @SubscribeEvent
    public static void mouseInputEvent(InputEvent.MouseButton.Pre event){
        if (Minecraft.getInstance().player == null) return;
        handleInput(event.getButton(),event.getAction(),event.getModifiers());
    }
    @SubscribeEvent
    public static void keyInputEvent(InputEvent.Key event) {
        if (Minecraft.getInstance().player == null) return;
        handleInput(event.getKey(),event.getAction(),event.getModifiers());
    }

    public static void handleInput(int button, int action,int modifiers){
        Minecraft minecraft = Minecraft.getInstance();


        for(Map.Entry<KeyMapping,ActionHandler> keyHandler : actionHandlerMapping.entrySet()){
            if(keyHandler.getKey().getKey().getValue() == button && action == GLFW.GLFW_PRESS && keyHandler.getKey().isConflictContextAndModifierActive()){

                //mouse down
                state.add(keyHandler.getKey());
                keyHandler.getValue().actionDown.accept(modifiers);
                sendSatePacket(keyHandler.getValue().actionName,modifiers,true);

            }else if (button == keyHandler.getKey().getKey().getValue() && action == GLFW.GLFW_RELEASE && keyHandler.getKey().isConflictContextAndModifierActive()){
                //key released
                state.remove(keyHandler.getKey());
                keyHandler.getValue().actionReleased.accept(modifiers);
                sendSatePacket(keyHandler.getValue().actionName,modifiers,false);
            }else if (button == keyHandler.getKey().getKey().getValue() && action == GLFW.GLFW_REPEAT &&keyHandler.getKey().isConflictContextAndModifierActive()){
                keyHandler.getValue().actionHeld.accept(modifiers);

            }
        }
        if(Minecraft.getInstance().screen != null){
            Iterator<KeyMapping> mappings = state.iterator();
            while(mappings.hasNext()){
                KeyMapping mapping = mappings.next();
                if(!mapping.isConflictContextAndModifierActive()){
                    mappings.remove();
                    actionHandlerMapping.get(mapping).actionReleased.accept(modifiers);
                    sendSatePacket(actionHandlerMapping.get(mapping).actionName,modifiers,false);
                }
            }

        }
    }
}
