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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.network.npc.NpcDialoguePacket;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CultivatorNpcEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(CultivatorNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> GENERATED_NAME = SynchedEntityData.defineId(CultivatorNpcEntity.class, EntityDataSerializers.STRING);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("move.run");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable private UUID challengedBy;

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
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setSkin(input.getStringOr("NpcSkin", "default"));
        setGeneratedName(input.getStringOr("NpcName", ""));
        try {
            String storedChallenger = input.getStringOr("NpcChallenger", "");
            challengedBy = storedChallenger.isBlank() ? null : UUID.fromString(storedChallenger);
        } catch (IllegalArgumentException exception) {
            challengedBy = null;
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        initializeProfile(level.getLevel());
        return result;
    }

    private void initializeProfile(ServerLevel server) {
        if (!entityData.get(GENERATED_NAME).isBlank()) return;
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        setGeneratedName(profile.randomName(random));
        setSkin(profile.randomSkin(random));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)) return;
        initializeProfile(server);
        if (challengedBy != null) {
            Player challenger = server.getPlayerByUUID(challengedBy);
            if (challenger == null || !challenger.isAlive() || challenger.isSpectator()
                    || challenger.getAbilities().instabuild || distanceToSqr(challenger) > 48 * 48) {
                if (getTarget() != null) setTarget(null);
                if (isAggressive()) setAggressive(false);
            } else {
                if (getTarget() != challenger) setTarget(challenger);
                if (!isAggressive()) setAggressive(true);
            }
        }
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
                PacketDistributor.sendToPlayer(serverPlayer, new NpcDialoguePacket(getId(), getUUID(),
                        getName().getString(), NpcProfile.load(server, getNpcType()).greeting(),
                        isChallenger(serverPlayer)));
            } else serverPlayer.sendSystemMessage(Component.translatable("gui.ascension.npc.aggressive"));
        }
        return InteractionResult.SUCCESS;
    }

    public void speak(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || (challengedBy != null && !isChallenger(player))) return;
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        PacketDistributor.sendToPlayer(player, new NpcDialoguePacket(getId(), getUUID(),
                getName().getString(), profile.randomDialogue(random), isChallenger(player)));
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
                .triggerableAnim("die", RawAnimation.begin().thenPlay("misc.die")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
