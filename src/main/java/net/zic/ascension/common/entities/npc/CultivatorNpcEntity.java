package net.zic.ascension.common.entities.npc;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
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
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.common.entities.AscEntities;
import net.zic.ascension.common.entities.npc.ai.NpcCultivationGoal;
import net.zic.ascension.common.entities.npc.ai.NpcSocialGoal;
import net.zic.ascension.common.entities.npc.ai.NpcHomeWanderGoal;
import net.zic.ascension.common.entities.npc.ai.NpcReturnHomeGoal;
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
    private static final RawAnimation CULTIVATION_ENTER = RawAnimation.begin().thenPlay("cultivation.enter");
    private static final RawAnimation MEDITATION = RawAnimation.begin().thenLoop("cultivation.meditate");
    private static final RawAnimation QI_CIRCULATION = RawAnimation.begin().thenLoop("cultivation.qi_circulation");
    private static final RawAnimation CULTIVATION_EXIT = RawAnimation.begin().thenPlay("cultivation.exit");
    private static final EntityDataAccessor<Integer> DAILY_ACTIVITY = SynchedEntityData.defineId(CultivatorNpcEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, TrialProgress> trials = new HashMap<>();
    private final NpcSocialState social = new NpcSocialState();
    private String personalityId = "";
    private String affiliationId = "";
    private NpcHabits habits;
    @Nullable private CultivatorNpcEntity socialPartner;
    private List<String> assignedTeachings = List.of();
    @Nullable private UUID challengedBy;
    private NpcEncounterType encounterType = NpcEncounterType.NONE;
    private int encounterTicks;
    private boolean summonedGuard;
    private boolean reinforcementSummoned;
    private int reinforcementTicks;
    private int tauntTicks;
    private int guardLifetime = 1800;
    private boolean profileReady;
    @Nullable private BlockPos homePos;
    @Nullable private BlockPos villageAnchor;
    private int homeRadius = 24;
    private String homeDimension = "";

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
        goalSelector.addGoal(3, new NpcReturnHomeGoal(this));
        goalSelector.addGoal(4, new NpcSocialGoal(this));
        goalSelector.addGoal(5, new NpcCultivationGoal(this));
        goalSelector.addGoal(6, new NpcHomeWanderGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 9));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN, "default");
        builder.define(GENERATED_NAME, "");
        builder.define(DAILY_ACTIVITY, NpcDailyActivity.NONE.ordinal());
    }

    public String getNpcType() { return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath(); }
    public String getSkin() { return entityData.get(SKIN); }
    public void setSkin(String skin) {
        if (skin != null && skin.matches("[a-z0-9_/-]+") && !skin.contains("..")) entityData.set(SKIN, skin);
    }
    public void setGeneratedName(String name) { entityData.set(GENERATED_NAME, name); }
    public boolean isSummonedGuard() { return summonedGuard; }
    public NpcAffiliation getAffiliation() {
        NpcAffiliation faction = NpcAffiliation.parse(affiliationId);
        return faction == null ? NpcAffiliation.ORTHODOX : faction;
    }
    public void setAffiliation(NpcAffiliation faction) {
        if (faction != null) affiliationId = faction.id();
    }

    @Nullable
    public BlockPos getHomePos() { return homePos; }

    @Nullable
    public BlockPos getVillageAnchor() { return villageAnchor; }

    public int getHomeRadius() { return homeRadius; }

    public boolean canReturnHome() {
        return !summonedGuard && !isNoAi() && challengedBy == null && getTarget() == null
                && !isCultivating() && !isSocializing() && !isAggressive() && isAlive();
    }

    public void setVillageHome(BlockPos home, BlockPos village, int radius) {
        if (!(level() instanceof ServerLevel server) || summonedGuard) return;
        homePos = home.immutable();
        villageAnchor = village.immutable();
        homeRadius = Math.clamp(radius, 8, 64);
        homeDimension = server.dimension().identifier().toString();
    }

    public String getPersonalityLabel() {
        if (!(level() instanceof ServerLevel server)) return personalityId;
        return NpcProfile.load(server, getNpcType()).personality(personalityId).name();
    }

    public int getRespect(ServerPlayer player) { return social.get(player.getUUID(), profilePersonality().initialRespect()).respect(); }
    public int setRespect(ServerPlayer player, int amount) { return social.get(player.getUUID(), profilePersonality().initialRespect()).setRespect(amount); }
    public int addRespect(ServerPlayer player, int amount) { return social.get(player.getUUID(), profilePersonality().initialRespect()).addRespect(amount); }
    public String getStanding(ServerPlayer player) { return social.get(player.getUUID(), profilePersonality().initialRespect()).standing(); }

    private NpcProfile.Personality profilePersonality() {
        return NpcProfile.load((ServerLevel) level(), getNpcType()).personality(personalityId);
    }

    public NpcDailyActivity getDailyActivity() {
        return NpcDailyActivity.fromId(entityData.get(DAILY_ACTIVITY));
    }

    public void setDailyActivity(NpcDailyActivity activity) {
        if (!level().isClientSide()) entityData.set(DAILY_ACTIVITY, activity.ordinal());
    }

    public boolean isCultivating() { return getDailyActivity() != NpcDailyActivity.NONE; }

    public NpcHabits getHabits() {
        if (habits == null && level() instanceof ServerLevel server) initializeProfile(server);
        return habits == null ? new NpcHabits(NpcHabits.Period.DAY, 100, 100) : habits;
    }

    public NpcProfile.DailyLife getDailyLife() {
        return NpcProfile.load((ServerLevel) level(), getNpcType()).dailyLife();
    }

    @Nullable
    public CultivatorNpcEntity getSocialPartner() { return socialPartner; }

    public boolean canBeginSocialEncounter() {
        return !isCultivating() && !isSocializing() && canActSocially() && getNavigation().isDone();
    }

    public boolean canContinueSocialEncounter() {
        return isSocializing() && canActSocially() && socialPartner.canActSocially()
                && distanceToSqr(socialPartner) <= 144.0;
    }

    public boolean isSocializing() { return socialPartner != null; }

    private boolean canActSocially() {
        return isAlive() && !isNoAi() && !summonedGuard && challengedBy == null
                && getTarget() == null && !isAggressive() && hurtTime == 0
                && !isOnFire() && !isInWaterOrRain() && !isInLava()
                && onGround() && !isPassenger();
    }

    public boolean startSocialEncounter(CultivatorNpcEntity partner) {
        if (partner == this || !canBeginSocialEncounter() || !partner.canBeginSocialEncounter()) return false;
        if (partner.level() != level()) return false;
        socialPartner = partner;
        partner.socialPartner = this;
        getNavigation().stop();
        partner.getNavigation().stop();
        return true;
    }

    public void endSocialEncounter() {
        CultivatorNpcEntity partner = socialPartner;
        socialPartner = null;
        if (partner != null && partner.socialPartner == this) partner.socialPartner = null;
    }

    public void announceSocialEncounter(CultivatorNpcEntity partner) {
        if (!(level() instanceof ServerLevel server)) return;
        NpcProfile.SocialTraits disposition = profilePersonality().social();
        List<String> lines;
        if (getAffiliation() != partner.getAffiliation()) lines = disposition.wary();
        else if (relativeRealm(partner) < 0) lines = disposition.senior();
        else lines = disposition.greetings();
        String message = lines.get(random.nextInt(lines.size()));
        String broadcast = getName().getString() + " → " + partner.getName().getString() + ": " + message;
        for (ServerPlayer player : server.players()) {
            if (!player.isSpectator() &&
                    (distanceToSqr(player) <= 12 * 12 || partner.distanceToSqr(player) <= 12 * 12))
                player.sendSystemMessage(Component.literal(broadcast));
        }
        triggerAnim("actions", "greet");
        partner.triggerAnim("actions", "greet");
    }

    public NpcProfile.CultivationRoutine getDailyCultivationRoutine() {
        return NpcProfile.load((ServerLevel) level(), getNpcType()).cultivationRoutine();
    }

    public boolean canContinueDailyCultivation() {
        return isAlive() && !isNoAi() && !summonedGuard && !isSocializing() && challengedBy == null &&
                getTarget() == null && !isAggressive() && hurtTime == 0 && !isOnFire() &&
                !isInWaterOrRain() && !isInLava() && onGround() && !isPassenger();
    }

    public boolean canBeginDailyCultivation() {
        return canContinueDailyCultivation() && getNavigation().isDone();
    }

    private void interruptDailyCultivation() {
        if (isCultivating()) setDailyActivity(NpcDailyActivity.NONE);
    }

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
        output.putString("NpcTeachings", String.join(",", assignedTeachings));
        output.putString("NpcPersonality", personalityId);
        output.putString("NpcAffiliation", affiliationId);
        if (homePos != null && !summonedGuard) {
            output.putInt("NpcHomeX", homePos.getX());
            output.putInt("NpcHomeY", homePos.getY());
            output.putInt("NpcHomeZ", homePos.getZ());
            output.putInt("NpcHomeRadius", homeRadius);
            output.putString("NpcHomeDimension", homeDimension);
            if (villageAnchor != null) {
                output.putInt("NpcVillageX", villageAnchor.getX());
                output.putInt("NpcVillageY", villageAnchor.getY());
                output.putInt("NpcVillageZ", villageAnchor.getZ());
            }
        }
        if (habits != null) {
            output.putString("NpcHabitPeriod", habits.preferredPeriod().name());
            output.putInt("NpcHabitSocial", habits.sociability());
            output.putInt("NpcHabitDiscipline", habits.discipline());
        }
        social.save(output);
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
        homePos = input.getInt("NpcHomeX").isPresent() && input.getInt("NpcHomeY").isPresent()
                && input.getInt("NpcHomeZ").isPresent()
                ? new BlockPos(input.getIntOr("NpcHomeX", 0), input.getIntOr("NpcHomeY", 0), input.getIntOr("NpcHomeZ", 0)) : null;
        villageAnchor = input.getInt("NpcVillageX").isPresent() && input.getInt("NpcVillageY").isPresent()
                && input.getInt("NpcVillageZ").isPresent()
                ? new BlockPos(input.getIntOr("NpcVillageX", 0), input.getIntOr("NpcVillageY", 0), input.getIntOr("NpcVillageZ", 0)) : null;
        homeRadius = Math.clamp(input.getIntOr("NpcHomeRadius", 24), 8, 64);
        homeDimension = input.getStringOr("NpcHomeDimension", "");
        personalityId = NpcProfile.canonicalPersonalityId(input.getStringOr("NpcPersonality", ""));
        affiliationId = input.getStringOr("NpcAffiliation", "");
        NpcHabits.Period period = NpcHabits.Period.parse(input.getStringOr("NpcHabitPeriod", ""));
        habits = period == null ? null : new NpcHabits(period,
                Math.clamp(input.getIntOr("NpcHabitSocial", 100), 25, 175),
                Math.clamp(input.getIntOr("NpcHabitDiscipline", 100), 25, 175));
        socialPartner = null;
        social.load(input);
        challengedBy = null;
        encounterType = NpcEncounterType.NONE;
        encounterTicks = 0;
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
        if (summonedGuard) {
            homePos = null;
            villageAnchor = null;
        }
        reinforcementSummoned = Boolean.parseBoolean(input.getStringOr("NpcReinforcementSent", "false"));
        reinforcementTicks = Math.max(0, input.getIntOr("NpcReinforcementTicks", 0));
    }

    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        initializeProfile(level.getLevel());
        return result;
    }

    private void initializeProfile(ServerLevel server) {
        if (profileReady) return;
        if (homePos != null && !homeDimension.isBlank() && !homeDimension.equals(server.dimension().identifier().toString())) {
            homePos = null;
            villageAnchor = null;
        }
        if (homePos == null && !summonedGuard) {
            homePos = blockPosition().immutable();
            homeRadius = 24;
            villageAnchor = null;
        }
        homeDimension = server.dimension().identifier().toString();
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        if (personalityId.isBlank()) personalityId = profile.randomPersonality(random).id();
        if (habits == null) habits = NpcHabits.roll(random, profile, profile.personality(personalityId));
        if (NpcAffiliation.parse(affiliationId) == null) affiliationId = profile.randomAffiliation(random).id();
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
        if (challengedBy != null) {
            ServerPlayer challenger = server.getServer().getPlayerList().getPlayer(challengedBy);
            NpcProfile.DuelRules rules = NpcProfile.load(server, getNpcType()).duelRules();
            if (challenger == null || challenger.level() != server || !challenger.isAlive() || challenger.isSpectator()
                    || challenger.getAbilities().instabuild || distanceToSqr(challenger) > rules.maxDistance() * rules.maxDistance()
                    || ++encounterTicks >= rules.maxDurationTicks()) {
                endEncounter();
                if (challenger != null && challenger.isAlive())
                    challenger.sendSystemMessage(Component.literal(getName().getString() + ": The encounter has ended."));
            } else {
                if (getTarget() != challenger) setTarget(challenger);
                if (!isAggressive()) setAggressive(true);
                if (encounterType == NpcEncounterType.MORTAL && getNpcType().equals("young_master") && !reinforcementSummoned) {
                    if (reinforcementTicks > 0) reinforcementTicks--;
                    if (reinforcementTicks <= 0) summonReinforcement(server, challenger);
                }
            }
        }
        if (getNpcType().equals("young_master") && challengedBy == null && !summonedGuard && !isCultivating() && !isSocializing()) {
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
        NpcProfile.Personality personality = profile.personality(personalityId);
        List<String> lines = relativeRealm(player) < 0 && !personality.stronger().isEmpty()
                ? personality.stronger() : !personality.taunts().isEmpty() ? personality.taunts() : profile.taunts();
        if (lines.isEmpty()) return;
        String text = lines.get(random.nextInt(lines.size()));
        player.sendSystemMessage(Component.literal(getName().getString() + ": " + text));
        triggerAnim("actions", relativeRealm(player) < 0 ? "greet" : "taunt");
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
            interruptDailyCultivation();
            endSocialEncounter();
            if (isChallenger(serverPlayer) && player.isShiftKeyDown()) {
                withdraw(serverPlayer);
            } else if (challengedBy == null || isChallenger(serverPlayer)) {
                if (challengedBy == null) triggerAnim("actions", "greet");
                showDialogue(serverPlayer, greeting(serverPlayer));
            } else serverPlayer.sendSystemMessage(Component.translatable("gui.ascension.npc.aggressive"));
        }
        return InteractionResult.SUCCESS;
    }

    private NpcSocialState.Relationship relationship(ServerPlayer player, NpcProfile.Personality personality) {
        return social.get(player.getUUID(), personality.initialRespect());
    }

    private int relativeRealm(LivingEntity player) {
        AscensionEntityDataProvider npcProvider = getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        AscensionEntityDataProvider playerProvider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (npcProvider == null || playerProvider == null || !npcProvider.hasData() || !playerProvider.hasData()
                || npcProvider.getData() == null || playerProvider.getData() == null) return 0;
        var npcSource = npcProvider.getData().getSource();
        var playerSource = playerProvider.getData().getSource();
        if (npcSource == null || playerSource == null) return 0;
        PathHolder npcPaths = npcSource.getDataSource(CoreDataSources.PATH_DATA_SOURCE.get());
        PathHolder playerPaths = playerSource.getDataSource(CoreDataSources.PATH_DATA_SOURCE.get());
        if (npcPaths == null || playerPaths == null) return 0;
        for (Identifier id : npcPaths.getPaths()) {
            PathInstance npcPath = npcPaths.getPath(id);
            PathInstance playerPath = playerPaths.getPath(id);
            if (npcPath == null || playerPath == null) continue;
            int result = Integer.compare(npcPath.getCurrentMajorRealm(), playerPath.getCurrentMajorRealm());
            if (result == 0) result = Integer.compare(npcPath.getCurrentMinorRealm(), playerPath.getCurrentMinorRealm());
            return result;
        }
        return 0;
    }

    private String greeting(ServerPlayer player) {
        NpcProfile profile = NpcProfile.load((ServerLevel) level(), getNpcType());
        NpcProfile.Personality personality = profile.personality(personalityId);
        NpcSocialState.Relationship relation = relationship(player, personality);
        String message = selectDialogue(player, profile, personality, relation, true);
        return message;
    }

    private String selectDialogue(ServerPlayer player, NpcProfile profile, NpcProfile.Personality personality,
                                  NpcSocialState.Relationship relation, boolean greeting) {
        if (greeting && random.nextInt(3) == 0) {
            int reputation = NpcReputation.get(player.level().getServer()).value(player.getUUID(), getAffiliation());
            if (reputation <= -40)
                return "Your deeds have angered the " + getAffiliation().label() + ". Be mindful of your standing.";
            if (reputation >= 40)
                return "Your deeds have earned recognition among the " + getAffiliation().label() + ".";
        }
        List<String> lines = List.of();
        if (relation.respect() <= -40 && !personality.hostile().isEmpty()) lines = personality.hostile();
        else if (relation.respect() >= 40 && !personality.friendly().isEmpty()) lines = personality.friendly();
        else if (relation.guidance() > 0 && !personality.guided().isEmpty()) lines = personality.guided();
        else if (relation.respect() <= -20 && !personality.hostile().isEmpty()) lines = personality.hostile();
        else if (relation.respect() >= 25 && !personality.friendly().isEmpty()) lines = personality.friendly();
        else {
            int relative = relativeRealm(player);
            if (relative < 0 && !personality.stronger().isEmpty()) lines = personality.stronger();
            else if (relative > 0 && !personality.weaker().isEmpty()) lines = personality.weaker();
            else if (relation.challenges() > 0 && !personality.challenged().isEmpty() && random.nextBoolean()) lines = personality.challenged();
            else if (relation.talks() > 0 && !personality.familiar().isEmpty() && random.nextBoolean()) lines = personality.familiar();
        }
        if (!lines.isEmpty()) return lines.get(random.nextInt(lines.size()));
        if (greeting) return personality.greeting().isBlank() ? profile.greeting() : personality.greeting();
        lines = personality.dialogue().isEmpty() || random.nextBoolean() ? profile.dialogue() : personality.dialogue();
        return lines.get(random.nextInt(lines.size()));
    }

    private void showDialogue(ServerPlayer player, String message) {
        ServerLevel server = (ServerLevel) level();
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        NpcProfile.Personality personality = profile.personality(personalityId);
        NpcSocialState.Relationship relation = relationship(player, personality);
        PacketDistributor.sendToPlayer(player, new NpcDialoguePacket(getId(), getUUID(),
                getName().getString(), message, isChallenger(player),
                canGuide() && !summonedGuard && !assignedTeachings.isEmpty(),
                trials.containsKey(player.getUUID()) && trials.get(player.getUUID()).completed,
                personality.name(), relation.standing(), relation.respect(), getAffiliation().label(),
                NpcReputation.get(server.getServer()).value(player.getUUID(), getAffiliation()),
                personality.gifts().hint(), relation.talks(),
                relation.talks() >= profile.dialogueUnlocks().giftTalks() && !summonedGuard,
                relation.talks() >= profile.dialogueUnlocks().insultTalks() && !summonedGuard,
                relation.talks() >= profile.dialogueUnlocks().challengeTalks() && !summonedGuard,
                relation.talks() >= profile.dialogueUnlocks().guidanceTalks() && !summonedGuard,
                relation.respect() <= -40, encounterType == NpcEncounterType.MORTAL,
                relation.wins(), relation.losses(), getNpcRealmLabel()));
    }

    private String getNpcRealmLabel() {
        if (!(level() instanceof ServerLevel server)) return "Unawakened";
        AscensionEntityDataProvider provider = getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (provider == null || !provider.hasData() || provider.getData() == null) return "Unawakened";
        var source = provider.getData().getSource();
        if (source == null) return "Unawakened";
        PathHolder paths = source.getDataSource(CoreDataSources.PATH_DATA_SOURCE.get());
        if (paths == null) return "Unawakened";
        for (Identifier pathId : paths.getPaths()) {
            PathInstance instance = paths.getPath(pathId);
            if (instance == null) continue;
            var path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, server.registryAccess());
            if (path != null) return path.getRealmName(instance.getCurrentMajorRealm(), instance.getCurrentMinorRealm()).getString();
        }
        return "Unawakened";
    }

    public void speak(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || challengedBy != null) return;
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        NpcProfile.Personality personality = profile.personality(personalityId);
        NpcSocialState.Relationship relation = relationship(player, personality);
        String message = selectDialogue(player, profile, personality, relation, false);
        relation.talked();
        showDialogue(player, message);
        if (getNpcType().equals("young_master") && relativeRealm(player) >= 0) triggerAnim("actions", "taunt");
    }

    public void gift(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || !actionUnlocked(player, NpcAction.GIFT)
                || summonedGuard || challengedBy != null) return;
        NpcProfile.Personality personality = profilePersonality();
        NpcSocialState.Relationship relation = relationship(player, personality);
        if (!relation.canGift(server.getGameTime())) {
            showDialogue(player, "I have already accepted an offering from you recently.");
            return;
        }
        ItemStack offered = !player.getMainHandItem().isEmpty() ? player.getMainHandItem()
                : !player.getOffhandItem().isEmpty() ? player.getOffhandItem() : ItemStack.EMPTY;
        Item item = offered.isEmpty() ? ModItems.SPIRITUAL_STONE.get() : offered.getItem();
        if (offered.isEmpty() && countOffering(player, item) == 0) {
            showDialogue(player, "Hold something you wish to offer, or carry a Spiritual Stone with empty hands.");
            return;
        }
        NpcGiftPreferences gifts = personality.gifts();
        NpcGiftPreferences.Response response = gifts.response(item);
        if (response == NpcGiftPreferences.Response.DISLIKED) {
            showDialogue(player, gifts.dislikedReply());
            return;
        }
        if (response == NpcGiftPreferences.Response.UNSUITABLE) {
            showDialogue(player, gifts.refusedReply());
            return;
        }
        boolean preferred = response == NpcGiftPreferences.Response.PREFERRED;
        int respect = relation.respect();
        if (respect <= -40 && !preferred) {
            showDialogue(player, gifts.hostileReply());
            return;
        }
        if (respect >= 40 && preferred && random.nextInt(100) < gifts.trustedRefusalChance()) {
            showDialogue(player, gifts.trustedReply());
            return;
        }
        int change = Math.max(1, personality.giftRespect() + (preferred ? gifts.preferredBonus() : 0));
        if (respect <= -40) change = Math.max(1, change * gifts.hostilePercent() / 100);
        else if (respect <= -15) change = Math.max(1, change * gifts.waryPercent() / 100);
        if (!relation.gifted(server.getGameTime(), change)) return;
        if (offered.isEmpty()) consumeOffering(player, item, 1);
        else {
            offered.shrink(1);
            player.getInventory().setChanged();
        }
        triggerAnim("actions", "greet");
        showDialogue(player, preferred ? gifts.preferredReply() : gifts.acceptedReply());
    }

    public void insult(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || !actionUnlocked(player, NpcAction.INSULT)
                || summonedGuard || challengedBy != null) return;
        NpcProfile.Personality personality = profilePersonality();
        NpcSocialState.Relationship relation = relationship(player, personality);
        if (!relation.insulted(server.getGameTime(), personality.insultRespect())) {
            showDialogue(player, "I have heard quite enough from you for now.");
            return;
        }
        if (getNpcType().equals("young_master")) triggerAnim("actions", "taunt");
        showDialogue(player, "Watch your tongue, cultivator. I will remember that insult.");
    }

    public void helpedBy(ServerPlayer player) {
        if (summonedGuard || challengedBy != null || !isAlive() || distanceToSqr(player) > 32 * 32) return;
        NpcProfile.Personality personality = profilePersonality();
        relationship(player, personality).helped(personality.aidRespect());
    }

    public void guide(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || !actionUnlocked(player, NpcAction.GUIDANCE)
                || !canGuide() || summonedGuard || challengedBy != null || assignedTeachings.isEmpty()) return;
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
        NpcProfile.Personality personality = profile.personality(personalityId);
        relationship(player, personality).guided(personality.guidanceRespect());
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

    public enum NpcAction { GIFT, INSULT, GUIDANCE, CHALLENGE }

    public boolean actionUnlocked(ServerPlayer player, NpcAction action) {
        if (!(level() instanceof ServerLevel server) || summonedGuard) return false;
        NpcProfile profile = NpcProfile.load(server, getNpcType());
        int talks = relationship(player, profile.personality(personalityId)).talks();
        return switch (action) {
            case GIFT -> talks >= profile.dialogueUnlocks().giftTalks();
            case INSULT -> talks >= profile.dialogueUnlocks().insultTalks();
            case GUIDANCE -> talks >= profile.dialogueUnlocks().guidanceTalks();
            case CHALLENGE -> talks >= profile.dialogueUnlocks().challengeTalks();
        };
    }

    public NpcEncounterType getEncounterType() { return encounterType; }

    public boolean isFriendlyDuelWith(ServerPlayer player) {
        return isChallenger(player) && encounterType == NpcEncounterType.FRIENDLY;
    }

    public float duelFinishHealth(LivingEntity participant) {
        if (!(level() instanceof ServerLevel server)) return 1.0F;
        int percent = NpcProfile.load(server, getNpcType()).duelRules().finishHealthPercent();
        return Math.max(1.0F, participant.getMaxHealth() * percent / 100.0F);
    }

    public void resolveFriendlyDuel(ServerPlayer player, boolean playerWon) {
        if (!isFriendlyDuelWith(player)) return;
        NpcProfile.Personality personality = profilePersonality();
        NpcSocialState.Relationship relation = relationship(player, personality);
        if (playerWon) relation.won(personality.duelWinRespect());
        else relation.lost(personality.duelLossRespect());
        endEncounter();
        String outcome = playerWon ? "You won the sparring match." : "You lost the sparring match.";
        player.sendSystemMessage(Component.literal(getName().getString() + ": " + outcome +
                " Record: " + relation.wins() + " wins, " + relation.losses() + " losses."));
    }

    public void challenge(ServerPlayer player) { startEncounter(player, false); }

    public void mortalConfrontation(ServerPlayer player) { startEncounter(player, true); }

    private void startEncounter(ServerPlayer player, boolean requestedMortal) {
        if (challengedBy != null || !isAlive() || !(level() instanceof ServerLevel server)) return;
        if (!summonedGuard && !actionUnlocked(player, NpcAction.CHALLENGE)) return;
        if (player.getAbilities().instabuild) {
            player.sendSystemMessage(Component.translatable("gui.ascension.npc.survival_required"));
            return;
        }
        NpcProfile.Personality personality = profilePersonality();
        boolean mortal = summonedGuard || relationship(player, personality).respect() <= -40;
        if (!summonedGuard && mortal != requestedMortal) {
            showDialogue(player, mortal ? "There will be no friendly duel between us. This would be a fight to the death."
                    : "Our differences do not warrant a mortal confrontation.");
            return;
        }
        interruptDailyCultivation();
        endSocialEncounter();
        challengedBy = player.getUUID();
        encounterType = mortal ? NpcEncounterType.MORTAL : NpcEncounterType.FRIENDLY;
        encounterTicks = 0;
        if (!summonedGuard) relationship(player, personality).challenged(personality.challengeRespect());
        setTarget(player);
        setAggressive(true);
        triggerAnim("actions", "greet");
        if (mortal && getNpcType().equals("young_master") && !reinforcementSummoned) {
            reinforcementTicks = NpcProfile.load(server, getNpcType()).summonDelayTicks();
            taunt(player);
        }
        player.sendSystemMessage(Component.literal(mortal
                ? getName().getString() + ": This confrontation is to the death!"
                : getName().getString() + ": An honourable duel begins. Surrender by sneaking and interacting."));
    }

    private void endEncounter() {
        challengedBy = null;
        encounterType = NpcEncounterType.NONE;
        encounterTicks = 0;
        setTarget(null);
        setAggressive(false);
        getNavigation().stop();
    }

    public void withdraw(ServerPlayer player) {
        if (!isFriendlyDuelWith(player)) return;
        NpcProfile.Personality personality = profilePersonality();
        relationship(player, personality).withdrew(personality.withdrawRespect());
        endEncounter();
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
        boolean challengerAttack = source.getEntity() instanceof ServerPlayer attacker && isChallenger(attacker);
        boolean success = super.hurtServer(level, source, amount);
        if (success) {
            interruptDailyCultivation();
            endSocialEncounter();
        }
        if (success && isAlive()) {
            triggerAnim("actions", "hurt");
            if (source.getEntity() instanceof ServerPlayer attacker && !challengerAttack && !summonedGuard) {
                NpcProfile.Personality personality = profilePersonality();
                relationship(attacker, personality).attacked(level.getGameTime(), personality.attackRespect());
            }
        }
        return success;
    }

    @Override
    protected void tickDeath() {
        if (deathTime == 0 && !level().isClientSide()) triggerAnim("actions", "die");
        super.tickDeath();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 5, state -> {
            RawAnimation animation = switch (getDailyActivity()) {
                case CULTIVATION_ENTER -> CULTIVATION_ENTER;
                case MEDITATION -> MEDITATION;
                case QI_CIRCULATION -> QI_CIRCULATION;
                case CULTIVATION_EXIT -> CULTIVATION_EXIT;
                case NONE -> !state.isMoving() ? IDLE : isAggressive() ? RUN : WALK;
            };
            return state.setAndContinue(animation);
        }));
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

    public enum NpcEncounterType {
        NONE,
        FRIENDLY,
        MORTAL
    }

}
