package net.zic.ascension.common.entities.npc;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.common.entities.AscEntities;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.network.npc.NpcDialoguePacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CultivatorNpcEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(CultivatorNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> GENERATED_NAME = SynchedEntityData.defineId(CultivatorNpcEntity.class, EntityDataSerializers.STRING);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("move.run");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, TrialProgress> trials = new HashMap<>();
    private List<String> assignedTeachings = List.of();
    @Nullable private UUID challengedBy;
    private boolean summonedGuard;
    private boolean reinforcementSummoned;
    private int reinforcementTicks;
    private int tauntTicks;
    private int guardLifetime = 1800;
    private boolean profileReady;

    public CultivatorNpcEntity(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 9));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN, "default");
        builder.define(GENERATED_NAME, "");
    }

    public String getNpcType() { return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath(); }
    public String getSkin() { return entityData.get(SKIN); }
    public void setSkin(String skin) {
        if (skin != null && skin.matches("[a-z0-9_/-]+") && !skin.contains("..")) entityData.set(SKIN, skin);
    }
    public void setGeneratedName(String name) { entityData.set(GENERATED_NAME, name); }
    public boolean isSummonedGuard() { return summonedGuard; }
    public void setSummonedGuard(boolean value) { summonedGuard = value; }

    @Override
    public Component getName() {
        if (getCustomName() != null) return getCustomName();
        String name = entityData.get(GENERATED_NAME);
        return name.isBlank() ? super.getName() : Component.literal(name);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("NpcSkin", getSkin());
        output.putString("NpcName", entityData.get(GENERATED_NAME));
        if (challengedBy != null) output.putString("NpcChallenger", challengedBy.toString());
        output.putString("NpcTeachings", String.join(",", assignedTeachings));
        StringBuilder progress = new StringBuilder();
        for (var entry : trials.entrySet()) {
            if (!progress.isEmpty()) progress.append(';');
            TrialProgress trial = entry.getValue();
            progress.append(entry.getKey()).append(',').append(trial.kills).append(',').append(trial.completed).append(',').append(trial.technique);
        }
        output.putString("NpcTrials", progress.toString());
        output.putString("NpcSummonedGuard", Boolean.toString(summonedGuard));
        output.putInt("NpcGuardLifetime", guardLifetime);
        output.putString("NpcReinforcementSent", Boolean.toString(reinforcementSummoned));
        output.putInt("NpcReinforcementTicks", reinforcementTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setSkin(input.getStringOr("NpcSkin", "default"));
        setGeneratedName(input.getStringOr("NpcName", ""));
        profileReady = false;
        try {
            String storedChallenger = input.getStringOr("NpcChallenger", "");
            challengedBy = storedChallenger.isBlank() ? null : UUID.fromString(storedChallenger);
        } catch (IllegalArgumentException exception) {
            challengedBy = null;
        }
        String storedTeachings = input.getStringOr("NpcTeachings", "");
        assignedTeachings = storedTeachings.isBlank() ? List.of() : List.of(storedTeachings.split(","));
        trials.clear();
        String storedTrials = input.getStringOr("NpcTrials", "");
        if (!storedTrials.isBlank()) {
            for (String row : storedTrials.split(";")) {
                try {
                    String[] columns = row.split(",", 4);
                    if (columns.length != 4) continue;
                    UUID uuid = UUID.fromString(columns[0]);
                    int kills = Math.max(0, Math.min(64, Integer.parseInt(columns[1])));
                    if (columns[3].length() > 160) continue;
                    trials.put(uuid, new TrialProgress(columns[3], kills, Boolean.parseBoolean(columns[2])));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        summonedGuard = Boolean.parseBoolean(input.getStringOr("NpcSummonedGuard", "false"));
        guardLifetime = Math.max(0, input.getIntOr("NpcGuardLifetime", 1800));
        reinforcementSummoned = Boolean.parseBoolean(input.getStringOr("NpcReinforcementSent", "false"));
        reinforcementTicks = Math.max(0, input.getIntOr("NpcReinforcementTicks", 0));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        initializeProfile(level.getLevel());
        return result;
    }

    private void initializeProfile(ServerLevel server) {
        if (profileReady) return;
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        if (entityData.get(GENERATED_NAME).isBlank()) {
            setGeneratedName(profile.randomName(random));
            setSkin(profile.randomSkin(random));
        }
        if (canGuide() && !summonedGuard && assignedTeachings.isEmpty() && !profile.teachings().isEmpty()) {
            List<String> available = new ArrayList<>(profile.teachings());
            int count = getNpcType().equals("sect_master") ? 3 : 1;
            List<String> picked = new ArrayList<>();
            while (!available.isEmpty() && picked.size() < count) {
                picked.add(available.remove(random.nextInt(available.size())));
            }
            assignedTeachings = List.copyOf(picked);
        }
        profileReady = true;
    }

    public boolean canGuide() {
        return getNpcType().equals("sect_elder") || getNpcType().equals("sect_master");
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)) return;
        initializeProfile(server);
        if (summonedGuard && --guardLifetime <= 0) {
            discard();
            return;
        }
        ServerPlayer challenger = challengedBy == null ? null : server.getServer().getPlayerList().getPlayer(challengedBy);
        if (challenger == null || challenger.level() != server || !challenger.isAlive() || challenger.isSpectator()
                || challenger.getAbilities().instabuild || distanceToSqr(challenger) > 48 * 48) {
            if (getTarget() != null) setTarget(null);
            if (isAggressive()) setAggressive(false);
        } else {
            if (getTarget() != challenger) setTarget(challenger);
            if (!isAggressive()) setAggressive(true);
            if (getNpcType().equals("young_master") && !reinforcementSummoned) {
                if (reinforcementTicks > 0) reinforcementTicks--;
                if (reinforcementTicks <= 0) summonReinforcement(server, challenger);
            }
        }
        if (getNpcType().equals("young_master") && challengedBy == null && !summonedGuard) {
            if (++tauntTicks >= 440) {
                tauntTicks = 0;
                for (ServerPlayer nearby : server.players()) {
                    if (!nearby.isSpectator() && distanceToSqr(nearby) <= 9 * 9) {
                        taunt(nearby);
                        break;
                    }
                }
            }
        }
    }

    private void taunt(ServerPlayer player) {
        NpcProfile profile = NpcProfile.load((ServerLevel) level(), getNpcType());
        if (profile.taunts().isEmpty()) return;
        String text = profile.taunts().get(random.nextInt(profile.taunts().size()));
        player.sendSystemMessage(Component.literal(getName().getString() + ": " + text));
        triggerAnim("actions", "taunt");
    }

    private void summonReinforcement(ServerLevel server, ServerPlayer player) {
        reinforcementSummoned = true;
        triggerAnim("actions", "summon");
        CultivatorNpcEntity guard = AscEntities.SECT_ELDER.get().create(server, EntitySpawnReason.EVENT);
        if (guard == null) return;
        guard.snapTo(getX() + (random.nextBoolean() ? 2.0 : -2.0), getY(), getZ() + 2.0, getYRot(), 0.0F);
        guard.setSummonedGuard(true);
        guard.setGeneratedName("Summoned Sect Elder");
        server.addFreshEntity(guard);
        guard.challenge(player);
        player.sendSystemMessage(Component.translatable("gui.ascension.npc.reinforcement"));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.getItemInHand(hand).is(Items.NAME_TAG))
            return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer && level() instanceof ServerLevel server) {
            if (isChallenger(serverPlayer) && player.isShiftKeyDown()) {
                withdraw(serverPlayer);
            } else if (challengedBy == null || isChallenger(serverPlayer)) {
                if (challengedBy == null) triggerAnim("actions", "greet");
                showDialogue(serverPlayer, NpcProfile.load(server, getNpcType()).greeting());
            } else serverPlayer.sendSystemMessage(Component.translatable("gui.ascension.npc.aggressive"));
        }
        return InteractionResult.SUCCESS;
    }

    private void showDialogue(ServerPlayer player, String message) {
        PacketDistributor.sendToPlayer(player, new NpcDialoguePacket(getId(), getUUID(),
                getName().getString(), message, isChallenger(player),
                canGuide() && !summonedGuard && !assignedTeachings.isEmpty(),
                trials.containsKey(player.getUUID()) && trials.get(player.getUUID()).completed));
    }

    public void speak(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || (challengedBy != null && !isChallenger(player))) return;
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        showDialogue(player, profile.randomDialogue(random));
        if (getNpcType().equals("young_master")) triggerAnim("actions", "taunt");
    }

    public void guide(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || !canGuide() || summonedGuard ||
                challengedBy != null || assignedTeachings.isEmpty()) return;
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        TrialProgress progress = trials.get(player.getUUID());
        if (progress == null) {
            String technique = assignedTeachings.get(random.nextInt(assignedTeachings.size()));
            progress = new TrialProgress(technique, 0, false);
            trials.put(player.getUUID(), progress);
        }
        if (progress.completed) {
            showDialogue(player, "I have already enlightened your wisdom. Seek your own path now.");
            return;
        }
        if (progress.kills < profile.requiredKills()) {
            showDialogue(player, "Your trial continues. Defeat " + profile.requiredKills() +
                    " hostile creatures near this teacher. Progress: " + progress.kills + "/" + profile.requiredKills() + ".");
            return;
        }
        Item offering = BuiltInRegistries.ITEM.getValue(Identifier.parse(profile.offeringItem()));
        if (offering == null || offering == Items.AIR) {
            showDialogue(player, "This teacher's offering is currently unavailable.");
            return;
        }
        int required = profile.offeringCount();
        int available = countOffering(player, offering);
        if (available < required) {
            showDialogue(player, "Your trial is complete, but you must offer " + required + " " +
                    new ItemStack(offering).getHoverName().getString() + ". You currently carry " + available + ".");
            return;
        }
        Identifier techniqueId;
        try {
            techniqueId = Identifier.parse(progress.technique);
        } catch (IllegalArgumentException exception) {
            showDialogue(player, "The promised teaching is unavailable.");
            return;
        }
        if (CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, techniqueId, server.registryAccess()) == null) {
            showDialogue(player, "The promised teaching is unavailable.");
            return;
        }
        if (getNpcType().equals("sect_master")) {
            AscensionEntityDataProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if (provider == null || provider.getData() == null ||
                    !AscensionOriginSourceHelper.addTechnique(provider.getData().getSource(), techniqueId)) {
                showDialogue(player, "Your cultivation cannot yet receive this teaching. Return when you are ready.");
                return;
            }
        } else {
            ItemStack manual = new ItemStack(ModItems.TECHNIQUE_MANUAL.get());
            manual.set(AscensionComponents.REGISTRY_ID_HOLDER.get(), techniqueId);
            if (!player.getInventory().add(manual)) player.drop(manual, false);
        }
        consumeOffering(player, offering, required);
        progress.completed = true;
        String taughtName = techniqueId.getPath();
        taughtName = taughtName.substring(taughtName.lastIndexOf('/') + 1).replace('_', ' ');
        triggerAnim("actions", getNpcType().equals("sect_master") ? "teach" : "greet");
        showDialogue(player, getNpcType().equals("sect_master")
                ? "I have imparted " + taughtName + " directly into your understanding. Our guidance is complete."
                : "You have passed the trial. Take this manual for " + taughtName + " and cultivate diligently.");
    }

    private int countOffering(ServerPlayer player, Item item) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private void consumeOffering(ServerPlayer player, Item item, int count) {
        for (int i = 0; i < player.getInventory().getContainerSize() && count > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(item)) continue;
            int take = Math.min(count, stack.getCount());
            stack.shrink(take);
            count -= take;
        }
        player.getInventory().setChanged();
    }

    public boolean hasActiveTrial(ServerPlayer player) {
        TrialProgress progress = trials.get(player.getUUID());
        return !summonedGuard && canGuide() && progress != null && !progress.completed;
    }

    public void recordHostileKill(ServerPlayer player) {
        TrialProgress progress = trials.get(player.getUUID());
        if (progress != null && !progress.completed && level() instanceof ServerLevel server) {
            int cap = NpcProfile.load(server, getNpcType()).requiredKills();
            if (progress.kills < cap) progress.kills++;
        }
    }

    public boolean isChallenger(Player player) {
        return challengedBy != null && challengedBy.equals(player.getUUID());
    }

    public void challenge(ServerPlayer player) {
        if (challengedBy != null || !isAlive()) return;
        if (player.getAbilities().instabuild) {
            player.sendSystemMessage(Component.translatable("gui.ascension.npc.survival_required"));
            return;
        }
        challengedBy = player.getUUID();
        setTarget(player);
        setAggressive(true);
        triggerAnim("actions", "greet");
        if (getNpcType().equals("young_master") && !reinforcementSummoned && level() instanceof ServerLevel server) {
            reinforcementTicks = NpcProfile.load(server, getNpcType()).summonDelayTicks();
            taunt(player);
        }
        player.sendSystemMessage(Component.translatable("gui.ascension.npc.challenge_started"));
    }

    public void withdraw(ServerPlayer player) {
        if (!isChallenger(player)) return;
        challengedBy = null;
        setTarget(null);
        setAggressive(false);
        getNavigation().stop();
        player.sendSystemMessage(Component.translatable("gui.ascension.npc.challenge_ended"));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean success = super.doHurtTarget(level, target);
        if (success) triggerAnim("actions", "attack");
        return success;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean success = super.hurtServer(level, source, amount);
        if (success && isAlive()) triggerAnim("actions", "hurt");
        return success;
    }

    @Override
    protected void tickDeath() {
        if (deathTime == 0 && !level().isClientSide()) triggerAnim("actions", "die");
        super.tickDeath();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 5, state ->
                state.setAndContinue(!state.isMoving() ? IDLE : isAggressive() ? RUN : WALK)));
        controllers.add(new AnimationController<>("actions", 0, state -> PlayState.STOP)
                .triggerableAnim("greet", RawAnimation.begin().thenPlay("misc.greet"))
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("attack.palm_strike"))
                .triggerableAnim("hurt", RawAnimation.begin().thenPlay("misc.hurt"))
                .triggerableAnim("die", RawAnimation.begin().thenPlay("misc.die"))
                .triggerableAnim("taunt", RawAnimation.begin().thenPlay("misc.taunt"))
                .triggerableAnim("summon", RawAnimation.begin().thenPlay("misc.summon"))
                .triggerableAnim("teach", RawAnimation.begin().thenPlay("misc.hand_seal")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    private static final class TrialProgress {
        private final String technique;
        private int kills;
        private boolean completed;

        private TrialProgress(String technique, int kills, boolean completed) {
            this.technique = technique;
            this.kills = kills;
            this.completed = completed;
        }
    }
}
