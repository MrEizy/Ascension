package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingElement;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.passive.PassiveTriggerService;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Runs what the element a player's External Qi Coating shows does with a tool it shows on (QiCoatingService.coatsWeapon):
 * external_on_mine (QiCoatingElement.Mining), the drops it changes and its faster mining, and external_on_use
 * (QiCoatingElement.Use), a hoe tilling and a shovel making paths round where they do (at once, or charged by holding them on
 * the block and released) and growing plants like bone meal, and any tool transforming the block it's used on. Bare hands are
 * left alone. The faster mining is worked out on the client too (QiCoatingMiningClient), as the client decides when a block
 * breaks.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class QiCoatingMining {
    // The context variable costs read how many items the drops hold from, so they can charge per item.
    public static final Identifier ITEMS = AscensionCraft.prefix("qi_coating/items");
    // The context variable a till's or path's cost reads how many blocks it changes from, so it can charge per block.
    public static final Identifier BLOCKS = AscensionCraft.prefix("qi_coating/blocks");
    // Set while a hoe or shovel changes the blocks round one, so the changes it makes don't reach round themselves.
    private static boolean reachingRound;
    // The furthest a till or path reaches whatever the data says (a patch 17 across), so a big charge can't stall the server.
    private static final double MAX_AREA_RADIUS = 8.0D;
    // Ticks without a click before a held hoe counts as let go, if the client's release (ServerboundToolChargeReleasePacket) never
    // came: it looked off the block, say. The client repeats a held click every 4, so this leaves room for network jitter.
    private static final int RELEASE_TICKS = 20;
    // Hoes and shovels held on a block charging, by player. Live on the server only.
    private static final Map<UUID, AreaCharge> AREA_CHARGES = new HashMap<>();
    // Reused by onServerTick for the charges released this tick.
    private static final List<UUID> RELEASED = new ArrayList<>();
    // Reused for the blocks a till or path reaches and what they change into, filled by collectArea.
    private static final List<BlockPos> AREA_BLOCKS = new ArrayList<>();
    private static final List<BlockState> AREA_STATES = new ArrayList<>();

    private QiCoatingMining() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player) || !QiCoatingService.coatsWeapon(event.getTool())) {
            return;
        }
        QiCoatingSelectionService.Shown shown = shown(player);
        QiCoatingElement element = element(player, shown);
        QiCoatingElement.Mining mining = element == null ? null : element.externalOnMine().orElse(null);
        if (mining == null) {
            return;
        }
        ServerLevel level = event.getLevel();
        ItemStack enchanted = enchanted(level, mining, event.getState(), event.getTool());
        if (enchanted != null) {
            reroll(event, level, enchanted);
        }
        List<ItemEntity> drops = event.getDrops();
        if (drops.isEmpty()) {
            return;
        }
        // Only the mined block's own drop converts, so stone's cobblestone isn't taken for mossy cobblestone, say.
        Item blockItem = event.getState().getBlock().asItem();
        QiCoatingElement.Conversion converted = mining.convert().get(blockItem);
        boolean smelt = mining.smelt() && drops.stream().anyMatch(drop -> smelted(level, drop.getItem()) != null);
        boolean convert = converted != null && blockItem != Items.AIR && drops.stream().anyMatch(drop -> drop.getItem().is(blockItem));
        if (!smelt && !convert && !mining.toInventory()) {
            return;
        }
        // A conversion with its own cost (an anvil's, say) is paid in place of the mining's.
        List<ActiveSkillCostDefinition> costs = convert ? converted.costs().orElse(mining.costs()) : mining.costs();
        if (!PassiveTriggerService.run(player, player, shown.skill(), List.of(), costs, List.of(), itemCount(drops))) {
            return;
        }
        for (ItemEntity drop : drops) {
            ItemStack stack = drop.getItem();
            if (smelt) {
                // No furnace experience: the block drops its own, as ores like coal and lapis do.
                AbstractCookingRecipe recipe = smelted(level, stack);
                if (recipe != null) {
                    ItemStack made = recipe.assemble(new SingleRecipeInput(stack));
                    drop.setItem(made.copyWithCount(made.getCount() * stack.getCount()));
                    continue;
                }
            }
            if (convert && stack.is(blockItem)) {
                drop.setItem(new ItemStack(converted.item(), stack.getCount()));
            }
        }
        if (mining.toInventory()) {
            takeIn(player, drops);
        }
    }

    // The tool as the element's fortune and silk_touch make it for this block (a copy, with Fortune raised on top of its own and
    // Silk Touch added), or null if they don't touch it.
    private static ItemStack enchanted(ServerLevel level, QiCoatingElement.Mining mining, BlockState state, ItemStack tool) {
        boolean silkTouch = mining.silkTouch().map(state::is).orElse(false);
        if (mining.fortune() <= 0 && !silkTouch) {
            return null;
        }
        HolderLookup.RegistryLookup<Enchantment> enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack copy = tool.copy();
        EnchantmentHelper.updateEnchantments(copy, mutable -> {
            if (mining.fortune() > 0) {
                Holder<Enchantment> fortune = enchantments.getOrThrow(Enchantments.FORTUNE);
                mutable.set(fortune, mutable.getLevel(fortune) + mining.fortune());
            }
            if (silkTouch) {
                mutable.set(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
            }
        });
        return copy;
    }

    // Rolls the block's drops again with `tool` in place of the one it was mined with, scattered as Block.popResource does.
    private static void reroll(BlockDropsEvent event, ServerLevel level, ItemStack tool) {
        if (!level.getGameRules().get(GameRules.BLOCK_DROPS)) {
            return;
        }
        BlockPos pos = event.getPos();
        RandomSource random = level.getRandom();
        List<ItemEntity> drops = event.getDrops();
        drops.clear();
        for (ItemStack stack : Block.getDrops(event.getState(), level, pos, event.getBlockEntity(), event.getBreaker(), tool)) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5 + Mth.nextDouble(random, -0.25, 0.25),
                    pos.getY() + 0.5 + Mth.nextDouble(random, -0.25, 0.25) - EntityType.ITEM.getHeight() / 2.0,
                    pos.getZ() + 0.5 + Mth.nextDouble(random, -0.25, 0.25), stack);
            drop.setDefaultPickUpDelay();
            drops.add(drop);
        }
    }

    /**
     * Whether an ice block mined with this tool stays whole (it's among the element's silk_touch blocks) rather than melting
     * where it stood. Asked by IceBlockQiCoatingMixin.
     */
    public static boolean keepsWhole(Player player, BlockState state, ItemStack tool) {
        if (!(player instanceof ServerPlayer) || !QiCoatingService.coatsWeapon(tool)) {
            return false;
        }
        QiCoatingElement element = element(player, shown(player));
        QiCoatingElement.Mining mining = element == null ? null : element.externalOnMine().orElse(null);
        return mining != null && mining.silkTouch().map(state::is).orElse(false);
    }

    /**
     * The drops of something killed with a coated weapon (external_on_kill): into the killer's inventory.
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getSource().getDirectEntity() != player
                || event.getDrops().isEmpty() || !QiCoatingService.coatsWeapon(player.getMainHandItem())) {
            return;
        }
        QiCoatingSelectionService.Shown shown = shown(player);
        QiCoatingElement element = element(player, shown);
        QiCoatingElement.Kill kill = element == null ? null : element.externalOnKill().orElse(null);
        if (kill == null || !kill.toInventory()) {
            return;
        }
        if (!PassiveTriggerService.run(player, player, shown.skill(), List.of(), kill.costs(), List.of(), itemCount(event.getDrops()))) {
            return;
        }
        takeIn(player, event.getDrops());
    }

    // Puts the drops in the player's inventory, leaving what doesn't fit to fall.
    private static void takeIn(ServerPlayer player, Collection<ItemEntity> drops) {
        Iterator<ItemEntity> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next().getItem();
            player.getInventory().add(stack);
            if (stack.isEmpty()) {
                iterator.remove();
            }
        }
    }

    // The cost variables for some drops: how many items they hold (ITEMS).
    private static Map<Identifier, Double> itemCount(Collection<ItemEntity> drops) {
        return Map.of(ITEMS, (double) drops.stream().mapToInt(drop -> drop.getItem().getCount()).sum());
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        applySpeed(event, element(player, shown(player)));
    }

    /**
     * Speeds up mining as the element's external_on_mine says, if the player holds a tool External Qi Coating shows on. Shared
     * with the client (QiCoatingMiningClient).
     */
    public static void applySpeed(PlayerEvent.BreakSpeed event, QiCoatingElement element) {
        ItemStack tool = event.getEntity().getMainHandItem();
        QiCoatingElement.Mining mining = element == null ? null : element.externalOnMine().orElse(null);
        if (mining == null || !QiCoatingService.coatsWeapon(tool)) {
            return;
        }
        // Undoes what Player.getDestroySpeed takes off for mining off the ground (a fifth) or with the head under water.
        Player player = event.getEntity();
        if (mining.airborne() && !player.onGround()) {
            event.setNewSpeed(event.getNewSpeed() * 5.0F);
        }
        if (mining.underwater() && player.getFluidInteraction().isEyeInFluidMatching(player, (_, type, _) -> type.getIsWaterLike())) {
            float submerged = (float) player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
            if (submerged > 0.0F && submerged < 1.0F) {
                event.setNewSpeed(event.getNewSpeed() / submerged);
            }
        }
        BlockState state = event.getState();
        for (QiCoatingElement.Speed speed : mining.speed()) {
            if (state.is(speed.blocks()) && speed.tools().map(tool::is).orElse(true)) {
                event.setNewSpeed(event.getNewSpeed() * speed.multiplier());
            }
        }
    }

    /**
     * The tool uses that reach round the block they're used on (external_on_use till and flatten): the tools that do it, the
     * ability they use, their sound and where the element's data keeps its Area.
     */
    public enum ToolArea {
        TILL(ItemTags.HOES, ItemAbilities.HOE_TILL, SoundEvents.HOE_TILL, QiCoatingElement.Use::till),
        FLATTEN(ItemTags.SHOVELS, ItemAbilities.SHOVEL_FLATTEN, SoundEvents.SHOVEL_FLATTEN, QiCoatingElement.Use::flatten);

        private static final ToolArea[] VALUES = values();
        private final TagKey<Item> tools;
        private final ItemAbility ability;
        private final SoundEvent sound;
        private final Function<QiCoatingElement.Use, Optional<QiCoatingElement.Area>> area;

        ToolArea(TagKey<Item> tools, ItemAbility ability, SoundEvent sound,
                 Function<QiCoatingElement.Use, Optional<QiCoatingElement.Area>> area) {
            this.tools = tools;
            this.ability = ability;
            this.sound = sound;
            this.area = area;
        }

        // The tool use `tool` does, or null if it's none of them.
        private static @Nullable ToolArea of(ItemStack tool) {
            for (ToolArea kind : VALUES) {
                if (tool.is(kind.tools)) {
                    return kind;
                }
            }
            return null;
        }

        // The tool use that uses `ability`, or null if none does.
        private static @Nullable ToolArea of(ItemAbility ability) {
            for (ToolArea kind : VALUES) {
                if (kind.ability == ability) {
                    return kind;
                }
            }
            return null;
        }

        // The element's Area for this use, if it has one that reaches past the block.
        private QiCoatingElement.@Nullable Area area(QiCoatingElement.@Nullable Use use) {
            QiCoatingElement.Area found = use == null ? null : area.apply(use).orElse(null);
            return found != null && found.reaches() ? found : null;
        }
    }

    /**
     * A coated hoe tilling a block or shovel making a path (external_on_use till or flatten, with no charge_ticks) does the
     * same to the blocks round it: each one it could change, with air above it, on the same level.
     */
    @SubscribeEvent
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (reachingRound || event.isSimulated() || !(event.getPlayer() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ToolArea kind = ToolArea.of(event.getItemAbility());
        ItemStack tool = event.getHeldItemStack();
        if (kind == null || !tool.is(kind.tools)) {
            return;
        }
        QiCoatingSelectionService.Shown shown = shown(player);
        QiCoatingElement.Use use = usable(player, shown, tool);
        QiCoatingElement.Area area = kind.area(use);
        if (use == null || area == null || area.chargeTicks() > 0 || event.getContext().getClickedFace() == Direction.DOWN
                || !level.getBlockState(event.getPos().above()).isAir()) {
            return;
        }
        UseOnContext context = event.getContext();
        collectArea(level, player, tool, context.getHand(), kind, event.getPos(), context.getClickedFace(),
                areaRadius(area, player, shown.skill(), 0.0D), false);
        if (AREA_BLOCKS.isEmpty()) {
            return;
        }
        // The tool changes the clicked block itself, so it counts towards the cost too.
        if (!PassiveTriggerService.run(player, player, shown.skill(), List.of(), area.costs().orElse(use.costs()), List.of(),
                Map.of(BLOCKS, AREA_BLOCKS.size() + 1.0D))) {
            AREA_BLOCKS.clear();
            AREA_STATES.clear();
            return;
        }
        changeCollected(level, player);
    }

    /**
     * Charges a till or path (an Area with charge_ticks): a coated hoe or shovel held on a block it would change doesn't
     * change it but charges, and changes round it on release (the client's ServerboundToolChargeReleasePacket, or
     * onServerTick if that never comes), further the longer it was held. The client repeats a held click every 4 ticks, and
     * each one lands here; the client cancels its own half too (QiCoatingMiningClient).
     */
    @SubscribeEvent
    public static void onChargeArea(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ItemStack tool = event.getItemStack();
        ToolArea kind = ToolArea.of(tool);
        if (kind == null) {
            return;
        }
        QiCoatingSelectionService.Shown shown = shown(player);
        QiCoatingElement element = element(player, shown);
        if (!chargesArea(element, player, tool, event.getHitVec())) {
            return;
        }
        QiCoatingElement.Area area = kind.area(element.externalOnUse().orElse(null));
        // chargesArea already checked it charges; this only tells the null checker so.
        if (area == null) {
            return;
        }
        long now = level.getGameTime();
        AreaCharge charge = AREA_CHARGES.get(player.getUUID());
        if (charge == null || charge.level != level || charge.kind != kind) {
            charge = new AreaCharge(kind, level, event.getPos().immutable(), event.getHitVec().getDirection(), now);
            AREA_CHARGES.put(player.getUUID(), charge);
        }
        charge.last = now;
        // A rising note each time the patch grows a block further, so the player hears it charge.
        double progress = charge.progress(area);
        int reach = Mth.floor(areaRadius(area, player, shown.skill(), progress));
        if (reach > charge.reach) {
            charge.reach = reach;
            level.playSound(null, charge.center, kind.sound, SoundSource.PLAYERS, 0.6F, 0.6F + (float) progress);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }

    /**
     * Whether this click with `tool` charges a till or path rather than changing the block at once: a hoe or shovel External Qi
     * Coating shows on, in the main hand, whose element charges that use, clicked on top or side of a block it would change
     * with air above. Shared with the client (QiCoatingMiningClient), which must not change the block on the click either.
     */
    public static boolean chargesArea(@Nullable QiCoatingElement element, Player player, ItemStack tool, BlockHitResult hit) {
        if (element == null || tool != player.getMainHandItem() || hit.getDirection() == Direction.DOWN) {
            return false;
        }
        ToolArea kind = ToolArea.of(tool);
        QiCoatingElement.Area area = kind == null ? null : kind.area(element.externalOnUse().orElse(null));
        if (area == null || area.chargeTicks() <= 0) {
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        if (!player.level().getBlockState(pos.above()).isAir()) {
            return false;
        }
        UseOnContext context = new UseOnContext(player.level(), player, InteractionHand.MAIN_HAND, tool, hit);
        return player.level().getBlockState(pos).getToolModifiedState(context, kind.ability, true) != null;
    }

    /**
     * Releases the player's charged till or path, if they have one: they let go of the use key
     * (ServerboundToolChargeReleasePacket).
     */
    public static void releaseArea(ServerPlayer player) {
        AreaCharge charge = AREA_CHARGES.remove(player.getUUID());
        if (charge != null && player.level() == charge.level) {
            // Held right up to now, not just to the last repeated click (up to 4 ticks back).
            charge.last = charge.level.getGameTime();
            releaseArea(player, charge);
        }
    }

    /**
     * Releases the charges whose clicks stopped without a release from the client (it looked off the block, or the packet was
     * lost), or whose player left.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (AREA_CHARGES.isEmpty()) {
            return;
        }
        // Released after the loop: changing blocks runs events that mustn't see the map change under them.
        for (Map.Entry<UUID, AreaCharge> entry : AREA_CHARGES.entrySet()) {
            if (entry.getValue().level.getGameTime() - entry.getValue().last > RELEASE_TICKS) {
                RELEASED.add(entry.getKey());
            }
        }
        for (int i = 0; i < RELEASED.size(); i++) {
            UUID id = RELEASED.get(i);
            AreaCharge charge = AREA_CHARGES.remove(id);
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(id);
            if (player != null && player.level() == charge.level) {
                releaseArea(player, charge);
            }
        }
        RELEASED.clear();
    }

    // Changes the blocks round the charged one as far as the charge reached, if the player still holds a tool that charges
    // this use and can pay.
    private static void releaseArea(ServerPlayer player, AreaCharge charge) {
        ItemStack tool = player.getMainHandItem();
        if (!tool.is(charge.kind.tools)) {
            return;
        }
        QiCoatingSelectionService.Shown shown = shown(player);
        QiCoatingElement.Use use = usable(player, shown, tool);
        QiCoatingElement.Area area = charge.kind.area(use);
        if (use == null || area == null || area.chargeTicks() <= 0) {
            return;
        }
        ServerLevel level = charge.level;
        collectArea(level, player, tool, InteractionHand.MAIN_HAND, charge.kind, charge.center, charge.face,
                areaRadius(area, player, shown.skill(), charge.progress(area)), true);
        if (AREA_BLOCKS.isEmpty()) {
            return;
        }
        if (!PassiveTriggerService.run(player, player, shown.skill(), List.of(), area.costs().orElse(use.costs()), List.of(),
                Map.of(BLOCKS, (double) AREA_BLOCKS.size()))) {
            AREA_BLOCKS.clear();
            AREA_STATES.clear();
            return;
        }
        changeCollected(level, player);
        level.playSound(null, charge.center, charge.kind.sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    // The area's radius for the player at this charge (0 to 1), capped at MAX_AREA_RADIUS.
    private static double areaRadius(QiCoatingElement.Area area, ServerPlayer player, Identifier skill, double charge) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        ScaledValue.Context context = new ScaledValue.Context(source, skill, player, player, charge, Map.of());
        return Mth.clamp(area.radius().resolve(context), 0.0D, MAX_AREA_RADIUS);
    }

    // Fills AREA_BLOCKS (and AREA_STATES) with the blocks round `center` the tool would change, in a round patch `radius` out:
    // on the same level, with air above, where the player may build. Rounded at radius + 0.5, so 1 is 3x3 and part radii still
    // grow it.
    private static void collectArea(ServerLevel level, ServerPlayer player, ItemStack tool, InteractionHand hand, ToolArea kind,
                                    BlockPos center, Direction face, double radius, boolean withCenter) {
        AREA_BLOCKS.clear();
        AREA_STATES.clear();
        int reach = Mth.floor(radius);
        double reachSqr = (radius + 0.5D) * (radius + 0.5D);
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx * dx + dz * dz > reachSqr || !withCenter && dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos at = center.offset(dx, 0, dz);
                if (!level.getBlockState(above.setWithOffset(at, Direction.UP)).isAir() || !level.mayInteract(player, at)) {
                    continue;
                }
                UseOnContext here = new UseOnContext(level, player, hand, tool, new BlockHitResult(Vec3.atCenterOf(at), face, at, false));
                BlockState changed = level.getBlockState(at).getToolModifiedState(here, kind.ability, true);
                if (changed != null) {
                    AREA_BLOCKS.add(at);
                    AREA_STATES.add(changed);
                }
            }
        }
    }

    // Sets the blocks collectArea found to their changed states, then empties the lists.
    private static void changeCollected(ServerLevel level, ServerPlayer player) {
        reachingRound = true;
        try {
            for (int i = 0; i < AREA_BLOCKS.size(); i++) {
                BlockPos at = AREA_BLOCKS.get(i);
                BlockState changed = AREA_STATES.get(i);
                level.setBlock(at, changed, 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, at, GameEvent.Context.of(player, changed));
            }
        } finally {
            reachingRound = false;
            AREA_BLOCKS.clear();
            AREA_STATES.clear();
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        AREA_CHARGES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        AREA_CHARGES.clear();
    }

    // A hoe or shovel held on a block to charge: which use, where, since when, the last click, and how far the patch had grown.
    private static final class AreaCharge {
        private final ToolArea kind;
        private final ServerLevel level;
        private final BlockPos center;
        private final Direction face;
        private final long start;
        private long last;
        private int reach = -1;

        private AreaCharge(ToolArea kind, ServerLevel level, BlockPos center, Direction face, long start) {
            this.kind = kind;
            this.level = level;
            this.center = center;
            this.face = face;
            this.start = start;
            this.last = start;
        }

        // How far the charge got, 0 to 1, up to the last click (or the release, when the client sent one).
        private double progress(QiCoatingElement.Area area) {
            return Mth.clamp((last - start) / (double) area.chargeTicks(), 0.0D, 1.0D);
        }
    }

    /**
     * A coated hoe used on something bone meal grows (external_on_use bonemeal) grows it as bone meal does. Blocks the hoe would
     * till are left to the hoe.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ItemStack hoe = event.getItemStack();
        QiCoatingSelectionService.Shown shown = shown(player);
        QiCoatingElement.Use use = usableHoe(player, shown, hoe);
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (use == null || !use.bonemeal() || !(state.getBlock() instanceof BonemealableBlock growable)
                || !growable.isValidBonemealTarget(level, pos, state)) {
            return;
        }
        UseOnContext context = new UseOnContext(level, player, event.getHand(), hoe, event.getHitVec());
        if (state.getToolModifiedState(context, ItemAbilities.HOE_TILL, true) != null) {
            return;
        }
        if (!PassiveTriggerService.run(player, player, shown.skill(), List.of(), use.costs(), List.of(), Map.of())) {
            return;
        }
        if (BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, player)) {
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    /**
     * A coated tool used on a block its element transforms (external_on_use transform) turns it into the block named: water at
     * its source into ice, say. The block is picked again with fluid sources in, as the click itself passes through water.
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ItemStack tool = event.getItemStack();
        QiCoatingSelectionService.Shown shown = shown(player);
        QiCoatingElement.Use use = usable(player, shown, tool);
        if (use == null || use.transform().isEmpty()) {
            return;
        }
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange())),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        QiCoatingElement.Transform transform = use.transform().get(state.getBlock());
        if (transform == null || state.getBlock() instanceof LiquidBlock && !state.getFluidState().isSource() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, hit.getDirection(), tool)) {
            return;
        }
        if (!PassiveTriggerService.run(player, player, shown.skill(), List.of(), transform.costs().orElse(use.costs()), List.of(), Map.of())) {
            return;
        }
        BlockState made = transform.block().defaultBlockState();
        level.setBlockAndUpdate(pos, made);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, made));
        level.playSound(null, pos, made.getSoundType(level, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.swing(InteractionHand.MAIN_HAND, true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    // The external_on_use of the element the player's External Qi Coating shows, if `hoe` is a hoe it shows on.
    private static QiCoatingElement.Use usableHoe(ServerPlayer player, QiCoatingSelectionService.Shown shown, ItemStack hoe) {
        return hoe.is(ItemTags.HOES) ? usable(player, shown, hoe) : null;
    }

    // The external_on_use of the element the player's External Qi Coating shows, if `tool` is in the main hand and it shows on it.
    private static QiCoatingElement.Use usable(ServerPlayer player, QiCoatingSelectionService.Shown shown, ItemStack tool) {
        if (tool != player.getMainHandItem() || !QiCoatingService.coatsWeapon(tool)) {
            return null;
        }
        QiCoatingElement element = element(player, shown);
        return element == null ? null : element.externalOnUse().orElse(null);
    }

    private static QiCoatingSelectionService.Shown shown(Player player) {
        return QiCoatingSelectionService.shown(player.getUUID(), QiCoatingChannel.EXTERNAL).orElse(null);
    }

    private static QiCoatingElement element(Player player, QiCoatingSelectionService.Shown shown) {
        return shown == null ? null : shown.definition(player.registryAccess());
    }

    // What a furnace makes of this stack, or null if it can't smelt it.
    private static AbstractCookingRecipe smelted(ServerLevel level, ItemStack stack) {
        return level.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .map(holder -> (AbstractCookingRecipe) holder.value())
                .orElse(null);
    }
}
