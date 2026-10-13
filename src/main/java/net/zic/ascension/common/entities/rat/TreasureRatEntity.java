package net.zic.ascension.common.entities.rat;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.common.entities.AscEntities;
import net.zic.ascension.common.item.ModItems;
import org.jetbrains.annotations.Nullable;

public class TreasureRatEntity extends Animal implements GeoEntity {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(TreasureRatEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.treasure_rat.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.treasure_rat.walk");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public TreasureRatEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 2.5));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        goalSelector.addGoal(3, new TemptGoal(this, 1.25, stack -> stack.is(ModItems.JADE.get()), true));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.FOLLOW_RANGE, 6.0);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.JADE.get());
    }

    @Nullable
    @Override
    public TreasureRatEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
        TreasureRatEntity baby = AscEntities.TREASURE_RAT.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null) {
            baby.setVariant(randomVariant(level.getRandom()));
        }
        return baby;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, RatVariant.JADE.getId());
    }

    public RatVariant getVariant() {
        return RatVariant.byId(entityData.get(VARIANT));
    }

    public void setVariant(RatVariant variant) {
        entityData.set(VARIANT, variant.getId());
    }

    private static RatVariant randomVariant(RandomSource random) {
        return RatVariant.byId(random.nextInt(RatVariant.values().length));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", getVariant().getId());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(RatVariant.byId(input.getIntOr("Variant", 0)));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        setVariant(randomVariant(level.getRandom()));
        return data;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("movement", 5, test ->
                test.setAndContinue(test.isMoving() ? WALK : IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
