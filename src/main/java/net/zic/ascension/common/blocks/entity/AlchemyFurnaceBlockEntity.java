package net.zic.ascension.common.blocks.entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.alchemy.*;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.*;
import net.zic.ascension.common.gui.menus.alchemy.AlchemyFurnaceMenu;
import net.zic.ascension.impl.core.technique.AlchemyTechnique;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.blocks.ModBlocks;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AlchemyFurnaceBlockEntity extends BlockEntity implements MenuProvider, GeoBlockEntity {
    public static final int MAX_INGREDIENTS = 3;
    public static final int OUTPUT_SLOT = 3;
    public static final int PHASE_TICKS = AlchemyStabilization.PHASE_TICKS;
    private static final double REQUIRED_REACTION_QI_FRACTION = 0.8D;
    private static final ResourceSourceIdentity REACTION_QI_SOURCE = ResourceSourceIdentity.of(
            AscensionCraft.prefix("alchemy_reaction"), ResourceSourceIdentity.Tags.SKILL);

    public enum Phase { IDLE, EXTRACTION, REACTION, STABILIZATION, CONDENSATION, SUCCESS, FAILURE }

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation HEATING = RawAnimation.begin().thenLoop("misc.heating");
    private static final RawAnimation REFINING = RawAnimation.begin().thenLoop("misc.refining");
    private static final RawAnimation SUCCESS_ANIM = RawAnimation.begin().thenPlay("misc.success").thenLoop("misc.idle");
    private static final RawAnimation FAILURE_ANIM = RawAnimation.begin().thenPlay("misc.failure").thenLoop("misc.idle");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final SimpleContainer items = new SimpleContainer(4) {
        @Override public void setChanged() {
            super.setChanged();
            AlchemyFurnaceBlockEntity.this.setChanged();
        }
    };
    private final ContainerData menuData = new ContainerData() {
        @Override public int get(int index) {
            if (index == 0) return phase.ordinal();
            if (index == 1) return phaseTicks;
            if (level == null) return 0;
            if (index >= 2 && index <= 5) return plan.index(level.registryAccess(), AlchemyTechnique.Stage.values()[index - 2]);
            if (index == 6) return plan.focusIndex(level.registryAccess());
            if (index == 7) return reactionQiProgress();
            if (index == 8) return stabilizationProgress();
            if (index == 9) return stabilizationSettings.durationTicks();
            return 0;
        }
        @Override public void set(int index, int value) {
            if (index == 0 && value >= 0 && value < Phase.values().length) phase = Phase.values()[value];
            if (index == 1) phaseTicks = value;
            if (level != null && index >= 2 && index <= 5)
                plan = plan.indexed(level.registryAccess(), AlchemyTechnique.Stage.values()[index - 2], value);
            if (level != null && index == 6) plan = plan.indexedFocus(level.registryAccess(), value);
        }
        @Override public int getCount() { return 10; }
    };
    private Phase phase = Phase.IDLE;
    private int phaseTicks;
    private List<AlchemySubstance> extracts = List.of();
    private MedicinalComposition composition = MedicinalComposition.EMPTY;
    private int ingredientCount;
    private double batchIngredientQuality = 0.35D;
    private boolean ruined;
    private AlchemyRefinementPlan plan = AlchemyRefinementPlan.DEFAULT;
    private @Nullable UUID alchemistId;
    private double reactionQiPerTick;
    private double reactionQiDelivered;
    private int reactionGradePenalty;
    private AlchemyStabilization.Settings stabilizationSettings = AlchemyStabilization.Settings.DEFAULT;
    private long stabilizationStartTick;
    private long stabilizationSeed;
    private int stabilizationHitMask;
    private int stabilizationScore;
    private int stabilizationGradePenalty;

    public AlchemyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(AscBlockEntities.ALCHEMY_FURNACE_BE.get(), pos, state);
    }

    public boolean hasPillPrediction() { return getBlockState().is(ModBlocks.INSIGHT_ALCHEMY_FURNACE.get()); }
    public Container inventory() { return items; }
    public ContainerData menuData() { return menuData; }
    public Phase phase() { return phase; }
    public AlchemyRefinementPlan plan() { return plan; }
    public @Nullable UUID alchemistId() { return alchemistId; }
    public AlchemyStabilization.Settings stabilizationSettings() { return stabilizationSettings; }
    public long stabilizationStartTick() { return stabilizationStartTick; }
    public long stabilizationSeed() { return stabilizationSeed; }
    public int stabilizationHitMask() { return stabilizationHitMask; }
    public int stabilizationScore() { return stabilizationScore; }

    public boolean cyclePreparation(int slot, Player player, int direction) {
        if (direction != 1 && direction != -1) return false;
        if (level == null || level.isClientSide() || phase != Phase.IDLE || slot < 0 || slot > 4) return false;
        if (slot == 4) {
            int count = AlchemyRefinementPlan.focusChoices(level.registryAccess()).size() + 1;
            if (count <= 1) return false;
            plan = plan.indexedFocus(level.registryAccess(), Math.floorMod(plan.focusIndex(level.registryAccess()) + direction, count));
        } else {
            AlchemyTechnique.Stage stage = AlchemyTechnique.Stage.values()[slot];
            List<net.minecraft.resources.Identifier> choices = AlchemyRefinementPlan.choices(level.registryAccess(), stage);
            if (choices.isEmpty()) return false;
            int current = plan.index(level.registryAccess(), stage);
            boolean found = false;
            for (int offset = 1; offset <= choices.size(); offset++) {
                int next = Math.floorMod(current + direction * offset, choices.size());
                if (next != current && AlchemyRefinementPlan.isLearnedOrBasic(level.registryAccess(), player, choices.get(next))) {
                    plan = plan.with(stage, choices.get(next));
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }
    public boolean isRunning() { return phase != Phase.IDLE && phase != Phase.SUCCESS && phase != Phase.FAILURE; }
    public static int duration(Phase phase) { return phase == Phase.SUCCESS || phase == Phase.FAILURE ? 60 : PHASE_TICKS; }
    public int phaseDuration() { return phase == Phase.STABILIZATION ? stabilizationSettings.durationTicks() : duration(phase); }
    public boolean canStart() {
        if (phase != Phase.IDLE || !items.getItem(OUTPUT_SLOT).isEmpty()) return false;
        for (int i = 0; i < MAX_INGREDIENTS; i++) if (!items.getItem(i).isEmpty()) return true;
        return false;
    }

    public boolean start(Player player) {
        if (player == null || !canStart() || level == null || level.isClientSide()) return false;
        AlchemyContext context = AlchemyContext.of(level, worldPosition, null);
        List<AlchemySubstance> prepared = new ArrayList<>();
        List<ItemStack> ingredientStacks = new ArrayList<>();
        for (int i = 0; i < MAX_INGREDIENTS; i++) {
            ItemStack stack = items.getItem(i);
            if (stack.isEmpty()) continue;
            AlchemyMaterial material = AlchemyMaterials.resolve(stack, context).orElse(null);
            if (material == null) return false;
            AlchemySubstance result = AlchemyPlanEvaluator.extract(material, context, plan, level.registryAccess());
            if (result.isEmpty()) return false;
            prepared.add(result);
            ingredientStacks.add(stack.copy());
        }
        if (prepared.isEmpty()) return false;
        for (int i = 0; i < MAX_INGREDIENTS; i++) if (!items.getItem(i).isEmpty()) items.removeItem(i, 1);
        batchIngredientQuality = MedicinalIngredientQuality.average(ingredientStacks);
        extracts = List.copyOf(prepared);
        composition = MedicinalComposition.EMPTY;
        ingredientCount = 0;
        ruined = false;
        alchemistId = player.getUUID();
        reactionQiPerTick = 0.0D;
        reactionQiDelivered = 0.0D;
        reactionGradePenalty = 0;
        stabilizationGradePenalty = 0;
        stabilizationHitMask = 0;
        stabilizationScore = 0;
        stabilizationStartTick = 0L;
        stabilizationSeed = 0L;
        transition(Phase.EXTRACTION);
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemyFurnaceBlockEntity furnace) {
        if (!(level instanceof ServerLevel serverLevel) || furnace.phase == Phase.IDLE) return;
        if (furnace.phase == Phase.REACTION) furnace.supplyReactionQi(serverLevel);
        if (++furnace.phaseTicks < furnace.phaseDuration()) {
            if (furnace.phaseTicks % 20 == 0) furnace.setChanged();
            return;
        }
        switch (furnace.phase) {
            case EXTRACTION -> furnace.transition(Phase.REACTION);
            case REACTION -> {
                furnace.reactionGradePenalty = furnace.reactionQiPerTick > 0.0D
                        && furnace.reactionQiDelivered + 1.0E-6D < furnace.reactionQiPerTick
                        * PHASE_TICKS * REQUIRED_REACTION_QI_FRACTION ? 1 : 0;
                furnace.performReactions();
                furnace.transition(furnace.ruined ? Phase.FAILURE : Phase.STABILIZATION);
                if (furnace.ruined) furnace.finish(false);
            }
            case STABILIZATION -> {
                furnace.stabilizationGradePenalty = furnace.stabilizationProgress() <
                        Math.round(furnace.stabilizationSettings.requiredScore() * 100.0D) ? 1 : 0;
                furnace.stabilize();
                furnace.transition(Phase.CONDENSATION);
            }
            case CONDENSATION -> {
                AlchemyTechnique.Refinement condensation = AlchemyPlanEvaluator.technique(furnace.plan,
                        AlchemyTechnique.Stage.CONDENSATION, level.registryAccess());
                ItemStack pill = MedicinalPillFactory.condense(
                        MedicinalEffectResolver.resolve(furnace.composition, level.registryAccess()),
                        furnace.ingredientCount, level.registryAccess(), MedicinalDecay.now(level),
                        AlchemyPlanEvaluator.finalQuality(furnace.batchIngredientQuality, furnace.plan, level.registryAccess()),
                        furnace.plan.focus(), condensation.focusPrecision(),
                        furnace.reactionGradePenalty + furnace.stabilizationGradePenalty);
                boolean success = !pill.isEmpty();
                furnace.items.setItem(OUTPUT_SLOT, success ? pill : new ItemStack(ModItems.PILL_RESIDUE.get()));
                furnace.finish(success);
                furnace.transition(success ? Phase.SUCCESS : Phase.FAILURE);
            }
            case SUCCESS, FAILURE -> furnace.transition(Phase.IDLE);
            default -> {}
        }
    }

    private void supplyReactionQi(ServerLevel serverLevel) {
        if (reactionQiPerTick <= 0.0D || alchemistId == null) return;
        Player player = serverLevel.getServer().getPlayerList().getPlayer(alchemistId);
        if (player == null || player.level() != serverLevel || !player.isAlive()) return;
        double dx = player.getX() - (worldPosition.getX() + 0.5D);
        double dz = player.getZ() - (worldPosition.getZ() + 0.5D);
        if (dx * dx + dz * dz > 2.25D
                || Math.abs(player.getY() - (worldPosition.getY() + 0.5D)) > 1.5D) return;
        ResourceTransactionService.Result result = ResourceTransactionService.drain(
                player, AscensionResourceTypes.QI.getId(), REACTION_QI_SOURCE, reactionQiPerTick);
        reactionQiDelivered = Math.min(reactionQiPerTick * PHASE_TICKS,
                reactionQiDelivered + Math.max(0.0D, result.appliedAmount()));
    }

    private int reactionQiProgress() {
        if (reactionQiPerTick <= 0.0D) return 100;
        return Mth.clamp((int) Math.round(100.0D * reactionQiDelivered
                / (reactionQiPerTick * PHASE_TICKS)), 0, 100);
    }

    private int stabilizationProgress() {
        if (stabilizationSettings.targetCount() <= 0) return 0;
        return Mth.clamp((int) Math.round((double) stabilizationScore / stabilizationSettings.targetCount()), 0, 100);
    }

    public boolean hitStabilization(Player player, int index, int clientTick) {
        if (!(level instanceof ServerLevel serverLevel) || phase != Phase.STABILIZATION
                || alchemistId == null || !alchemistId.equals(player.getUUID())
                || !player.isAlive() || player.level() != serverLevel
                || index < 0 || index >= stabilizationSettings.targetCount()
                || (stabilizationHitMask & (1 << index)) != 0) return false;
        int serverTick = (int) (serverLevel.getGameTime() - stabilizationStartTick);
        if (clientTick < serverTick - 12 || clientTick > serverTick + 3) return false;
        int points = AlchemyStabilization.points(stabilizationSettings, index, clientTick);
        if (points <= 0) return false;
        Vec3 position = AlchemyStabilization.position(worldPosition, getBlockState(),
                AlchemyStabilization.targetPosition(stabilizationSettings, stabilizationSeed, index));
        Vec3 eye = player.getEyePosition();
        Vec3 toTarget = position.subtract(eye);
        double distance = toTarget.length();
        if (distance > AlchemyStabilization.INTERACTION_REACH || distance < 0.15D
                || toTarget.normalize().dot(player.getLookAngle()) <
                Math.cos(Math.atan2(AlchemyStabilization.SERVER_AIM_TOLERANCE, distance))) return false;
        BlockHitResult obstruction = serverLevel.clip(new ClipContext(eye, position, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (obstruction.getType() == HitResult.Type.BLOCK && !obstruction.getBlockPos().equals(worldPosition)) return false;
        stabilizationHitMask |= 1 << index;
        stabilizationScore += points;
        serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                0.7F, points == 100 ? 1.5F : 1.0F);
        setChanged();
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    private void performReactions() {
        MedicinalComposition current = MedicinalComposition.EMPTY;
        int count = 0;
        AlchemyProcessHooks.ProcessSettings settings = AlchemyPlanEvaluator.reactions(plan, level.registryAccess());
        for (AlchemySubstance substance : extracts) {
            MedicinalReactionResolver.Step step = MedicinalReactionResolver.add(current, count, substance,
                    settings.safeEnergyDelta(), settings.explosionEnergyDelta(), level.registryAccess());
            if (step.outcome() == AlchemyMergeResolver.Outcome.CATASTROPHIC) {
                ruined = true;
                break;
            }
            current = step.composition();
            count = step.ingredientCount();
        }
        composition = current;
        ingredientCount = count;
    }

    private void stabilize() {
        if (composition.isEmpty()) return;
        composition = AlchemyPlanEvaluator.stabilize(composition, ingredientCount, plan, level.registryAccess());
    }

    private void finish(boolean success) {
        if (!success) items.setItem(OUTPUT_SLOT, new ItemStack(ModItems.PILL_RESIDUE.get()));
        extracts = List.of();
        composition = MedicinalComposition.EMPTY;
        ingredientCount = 0;
        ruined = false;
        alchemistId = null;
        reactionGradePenalty = 0;
        reactionQiDelivered = 0.0D;
        reactionQiPerTick = 0.0D;
        stabilizationStartTick = 0L;
        stabilizationSeed = 0L;
        stabilizationHitMask = 0;
        stabilizationScore = 0;
        stabilizationGradePenalty = 0;
    }

    private void transition(Phase next) {
        if (next == Phase.REACTION && level != null) {
            reactionQiPerTick = AlchemyPlanEvaluator.technique(plan, AlchemyTechnique.Stage.HEATING,
                    level.registryAccess()).qiPerTick();
            reactionQiDelivered = 0.0D;
        }
        if (next == Phase.STABILIZATION && level instanceof ServerLevel serverLevel) {
            stabilizationSettings = AlchemyPlanEvaluator.technique(plan, AlchemyTechnique.Stage.STABILIZATION,
                    level.registryAccess()).stabilization();
            stabilizationStartTick = serverLevel.getGameTime();
            stabilizationSeed = serverLevel.getRandom().nextLong();
            stabilizationHitMask = 0;
            stabilizationScore = 0;
        }
        phase = next;
        phaseTicks = 0;
        setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public Component getDisplayName() {
        return Component.translatable(hasPillPrediction() ? "block.ascension.insight_alchemy_furnace" : "block.ascension.alchemy_furnace");
    }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AlchemyFurnaceMenu(id, inventory, this);
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AlchemyFurnaceBlockEntity>("furnace", 0, state -> state.setAndContinue(switch (phase) {
            case EXTRACTION -> HEATING;
            case REACTION, STABILIZATION, CONDENSATION -> REFINING;
            case SUCCESS -> SUCCESS_ANIM;
            case FAILURE -> FAILURE_ANIM;
            default -> IDLE;
        })));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < 4; i++) if (!items.getItem(i).isEmpty()) output.store("Slot" + i, ItemStack.CODEC, items.getItem(i));
        output.store("Extracts", AlchemySubstance.CODEC.listOf(), extracts);
        output.store("MedicinalComposition", MedicinalComposition.CODEC, composition);
        output.putInt("IngredientCount", ingredientCount);
        output.putDouble("BatchIngredientQuality", batchIngredientQuality);
        output.putInt("Phase", phase.ordinal());
        output.putInt("PhaseTicks", phaseTicks);
        output.putBoolean("Ruined", ruined);
        output.store("RefinementPlan", AlchemyRefinementPlan.CODEC, plan);
        if (alchemistId != null) output.putString("Alchemist", alchemistId.toString());
        output.putDouble("ReactionQiPerTick", reactionQiPerTick);
        output.putDouble("ReactionQiDelivered", reactionQiDelivered);
        output.putInt("ReactionGradePenalty", reactionGradePenalty);
        output.store("StabilizationSettings", AlchemyStabilization.Settings.CODEC, stabilizationSettings);
        output.putLong("StabilizationStartTick", stabilizationStartTick);
        output.putLong("StabilizationSeed", stabilizationSeed);
        output.putInt("StabilizationHitMask", stabilizationHitMask);
        output.putInt("StabilizationScore", stabilizationScore);
        output.putInt("StabilizationGradePenalty", stabilizationGradePenalty);
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < 4; i++) items.setItem(i, input.read("Slot" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        extracts = input.read("Extracts", AlchemySubstance.CODEC.listOf()).orElse(List.of());
        composition = input.read("MedicinalComposition", MedicinalComposition.CODEC).orElse(MedicinalComposition.EMPTY);
        ingredientCount = Math.max(0, Math.min(MAX_INGREDIENTS, input.getIntOr("IngredientCount", 0)));
        batchIngredientQuality = Mth.clamp(input.getDoubleOr("BatchIngredientQuality", 0.35D), 0.0D, 1.0D);
        phase = Phase.values()[Mth.clamp(input.getIntOr("Phase", 0), 0, Phase.values().length - 1)];
        phaseTicks = Math.max(0, input.getIntOr("PhaseTicks", 0));
        ruined = input.getBooleanOr("Ruined", false);
        plan = input.read("RefinementPlan", AlchemyRefinementPlan.CODEC).orElse(AlchemyRefinementPlan.DEFAULT);
        String storedAlchemist = input.getStringOr("Alchemist", "");
        try {
            alchemistId = storedAlchemist.isEmpty() ? null : UUID.fromString(storedAlchemist);
        } catch (IllegalArgumentException ignored) {
            alchemistId = null;
        }
        double savedRate = input.getDoubleOr("ReactionQiPerTick", 0.0D);
        reactionQiPerTick = Double.isFinite(savedRate) ? Mth.clamp(savedRate, 0.0D, 1000.0D) : 0.0D;
        double savedDelivered = input.getDoubleOr("ReactionQiDelivered", 0.0D);
        reactionQiDelivered = Double.isFinite(savedDelivered)
                ? Mth.clamp(savedDelivered, 0.0D, reactionQiPerTick * PHASE_TICKS) : 0.0D;
        reactionGradePenalty = Mth.clamp(input.getIntOr("ReactionGradePenalty", 0), 0, 1);
        stabilizationSettings = input.read("StabilizationSettings", AlchemyStabilization.Settings.CODEC)
                .orElse(AlchemyStabilization.Settings.DEFAULT);
        phaseTicks = Math.min(phaseTicks, phaseDuration());
        stabilizationStartTick = input.getLongOr("StabilizationStartTick", 0L);
        stabilizationSeed = input.getLongOr("StabilizationSeed", 0L);
        stabilizationHitMask = input.getIntOr("StabilizationHitMask", 0)
                & ((1 << stabilizationSettings.targetCount()) - 1);
        stabilizationScore = Mth.clamp(input.getIntOr("StabilizationScore", 0), 0,
                stabilizationSettings.targetCount() * 100);
        stabilizationGradePenalty = Mth.clamp(input.getIntOr("StabilizationGradePenalty", 0), 0, 1);
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel) {
            for (int i = 0; i < 4; i++) {
                ItemStack stack = items.getItem(i);
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos, stack.copy());
                    items.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider access) { return saveWithoutMetadata(access); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
