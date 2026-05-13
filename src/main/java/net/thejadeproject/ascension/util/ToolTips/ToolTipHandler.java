package net.thejadeproject.ascension.util.ToolTips;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = "ascension", bus = EventBusSubscriber.Bus.GAME)
public class ToolTipHandler {

    private static final Map<Item, AnimatedTooltip> ANIMATED_TOOLTIPS = new HashMap<>();
    private static final Map<Item, List<Component>> ITEM_TOOLTIPS = new HashMap<>();

    /** Tooltips shown instead of the normal ones when the stack has PERMANENT = true */
    private static final Map<Item, List<Component>> PERMANENT_ITEM_TOOLTIPS = new HashMap<>();

    // ── Animated tooltips ────────────────────────────────────────────────────

    public static void registerAnimatedTooltip(Item item, String text, float speed) {
        ANIMATED_TOOLTIPS.put(item, new AnimatedTooltip(Component.literal(text), speed));
    }

    public static void registerAnimatedTooltip(Item item, Component text, float speed) {
        ANIMATED_TOOLTIPS.put(item, new AnimatedTooltip(text, speed));
    }

    private static class AnimatedTooltip {
        private final Component text;
        private final float speed;
        private final long startTime;

        public AnimatedTooltip(Component text, float speed) {
            this.text = text;
            this.speed = speed;
            this.startTime = System.currentTimeMillis();
        }

        public MutableComponent getComponent() {
            float time = ((System.currentTimeMillis() - startTime) * speed * 0.1f) % 1.0f;
            return ToolTipsGradient.RGBEachLetter(time, text.getString(), 0.01f);
        }
    }

    // ── Normal tooltip registration ──────────────────────────────────────────

    /**
     * Register a tooltip for an item (shown when the stack is NOT permanent).
     */
    public static void registerTooltip(Item item, Component... tooltipLines) {
        ITEM_TOOLTIPS.put(item, List.of(tooltipLines));
    }

    public static void registerTooltip(Item item, String... tooltipLines) {
        List<Component> components = List.of(tooltipLines).stream()
                .map(line -> Component.translationArg(Component.literal(line).withStyle(ChatFormatting.GRAY)))
                .toList();
        ITEM_TOOLTIPS.put(item, components);
    }

    public static void registerAdvancedTooltip(Item item, List<Component> tooltipLines) {
        ITEM_TOOLTIPS.put(item, tooltipLines);
    }

    // ── Permanent tooltip registration ───────────────────────────────────────

    /**
     * Register a tooltip shown ONLY when the stack has the PERMANENT data component set to true.
     * If not registered, permanent stacks fall back to the normal tooltip.
     */
    public static void registerPermanentTooltip(Item item, Component... tooltipLines) {
        PERMANENT_ITEM_TOOLTIPS.put(item, List.of(tooltipLines));
    }

    public static void registerPermanentTooltip(Item item, List<Component> tooltipLines) {
        PERMANENT_ITEM_TOOLTIPS.put(item, tooltipLines);
    }

    // ── Misc helpers ─────────────────────────────────────────────────────────

    public static void removeTooltip(Item item) {
        ITEM_TOOLTIPS.remove(item);
        PERMANENT_ITEM_TOOLTIPS.remove(item);
    }

    public static boolean hasTooltip(Item item) {
        return ITEM_TOOLTIPS.containsKey(item);
    }

    // ── Event handler ────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();

        // Determine whether this stack is a permanent talisman
        Boolean isPermanent = event.getItemStack().get(ModDataComponents.PERMANENT.get());
        boolean permanent = Boolean.TRUE.equals(isPermanent);

        // Static tooltips – prefer permanent map when the flag is set
        if (permanent && PERMANENT_ITEM_TOOLTIPS.containsKey(item)) {
            event.getToolTip().addAll(PERMANENT_ITEM_TOOLTIPS.get(item));
        } else if (ITEM_TOOLTIPS.containsKey(item)) {
            event.getToolTip().addAll(ITEM_TOOLTIPS.get(item));
        }

        // Animated tooltips (unchanged – always shown)
        if (ANIMATED_TOOLTIPS.containsKey(item)) {
            AnimatedTooltip animated = ANIMATED_TOOLTIPS.get(item);
            event.getToolTip().add(animated.getComponent());
        }
    }
}