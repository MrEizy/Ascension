package net.zic.ascension.common.entities.weapons;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.common.util.ModTags;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class ControlledSwordEntity extends Projectile {
    private static final EntityDataAccessor<ItemStack> DATA_SWORD = SynchedEntityData.defineId(ControlledSwordEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_HAND = SynchedEntityData.defineId(ControlledSwordEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SLOT = SynchedEntityData.defineId(ControlledSwordEntity.class, EntityDataSerializers.INT);
    private static final double MAX_SPEED = 1.4D;
    private static final double MIN_CONTROL_DISTANCE = 2.0D;
    private static final double DEFAULT_MAX_CONTROL_DISTANCE = 18.0D;
    private static final double DEFAULT_CONTROL_DISTANCE = 5.0D;
    private static final double DISTANCE_STEP = 1.5D;
    private static final double CONTROL_ACCELERATION = 0.26D;
    private static final double RECALL_SPEED = 1.8D;
    private static final int HIT_COOLDOWN = 12;
    private final Map<UUID, Long> hitTimes = new HashMap<>();
    private Vec3 aim = Vec3.ZERO;
    private int lastCommandTick;
    private int recallTicks;
    private boolean recalling;
    private double controlDistance = DEFAULT_CONTROL_DISTANCE;
    private double maximumControlDistance = DEFAULT_MAX_CONTROL_DISTANCE;
    private Vec3 visualPosition;
    private float visualYaw;
    private float visualPitch;
    private long lastVisualFrame;

    public ControlledSwordEntity(EntityType<? extends ControlledSwordEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public ControlledSwordEntity(EntityType<? extends ControlledSwordEntity> type, ServerLevel level, Player owner, InteractionHand hand) {
        this(type, level);
        setOwner(owner);
        entityData.set(DATA_SWORD, owner.getItemInHand(hand).copyWithCount(1));
        entityData.set(DATA_HAND, hand == InteractionHand.MAIN_HAND ? 0 : 1);
        entityData.set(DATA_SLOT, owner.getInventory().getSelectedSlot());
        Vec3 look = owner.getLookAngle();
        Vec3 spawn = owner.getEyePosition().add(look.scale(1.5D));
        setPos(spawn.x, spawn.y, spawn.z);
        aim = position();
        lastCommandTick = tickCount;
    }
    

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SWORD, ItemStack.EMPTY);
        builder.define(DATA_HAND, 0);
        builder.define(DATA_SLOT, -1);
    }

    public ItemStack sword() {
        return entityData.get(DATA_SWORD);
    }

    public InteractionHand swordHand() {
        return entityData.get(DATA_HAND) == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public boolean hidesHeldSword(Player player) {
        return player == getOwner()
                && (swordHand() == InteractionHand.OFF_HAND || player.getInventory().getSelectedSlot() == entityData.get(DATA_SLOT))
                && ItemStack.isSameItemSameComponents(player.getItemInHand(swordHand()), sword());
    }

    public void adjustDistance(int steps) {
        if (!recalling && steps != 0) {
            controlDistance = Mth.clamp(controlDistance + Mth.clamp(steps, -4, 4) * DISTANCE_STEP,
                    MIN_CONTROL_DISTANCE, maximumControlDistance);
        }
    }

    public VisualPose visualPose(Vec3 rawPosition, float yaw, float pitch) {
        long now = System.nanoTime();
        long elapsed = now - lastVisualFrame;
        if (visualPosition == null || elapsed < 0L || elapsed > 300_000_000L
                || visualPosition.distanceToSqr(rawPosition) > 100.0D) {
            visualPosition = rawPosition;
            visualYaw = yaw;
            visualPitch = pitch;
        } else {
            float amount = (float) (1.0D - Math.exp(-Math.min(elapsed / 1_000_000_000.0D, 0.1D) / 0.065D));
            visualPosition = visualPosition.lerp(rawPosition, amount);
            visualYaw = Mth.wrapDegrees(visualYaw + Mth.wrapDegrees(yaw - visualYaw) * amount);
            visualPitch = Mth.lerp(amount, visualPitch, pitch);
        }
        lastVisualFrame = now;
        return new VisualPose(visualPosition, visualYaw, visualPitch);
    }

    public record VisualPose(Vec3 position, float yaw, float pitch) {
    }

    public void command(Player player, double maximumDistance) {
        if (recalling || player != getOwner()) {
            return;
        }
        if (Double.isFinite(maximumDistance)) {
            maximumControlDistance = Math.max(MIN_CONTROL_DISTANCE, maximumDistance);
        }
        controlDistance = Math.min(controlDistance, maximumControlDistance);
        lastCommandTick = tickCount;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 target = eye.add(look.scale(controlDistance));
        HitResult hit = level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        aim = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().subtract(look.scale(0.55D)) : target;
    }

    public void recall() {
        recalling = true;
    }

    public boolean isRecalling() {
        return recalling;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || isRemoved()) {
            return;
        }
        if (!(level() instanceof ServerLevel server) || !(getOwner() instanceof Player player)
                || !player.isAlive() || player.isRemoved() || player.level() != level()) {
            discard();
            return;
        }
        if (!recalling && (tickCount - lastCommandTick > 3 || !validWeapon(player))) {
            recall();
        }
        if (recalling && ++recallTicks > 120) {
            discard();
            return;
        }
        Vec3 from = position();
        if (!recalling && from.distanceToSqr(player.position()) > (maximumControlDistance + 5.0D) * (maximumControlDistance + 5.0D)) {
            recall();
        }
        Vec3 destination = recalling ? player.getEyePosition().add(0.0D, -0.25D, 0.0D) : aim;
        if (recalling && from.distanceToSqr(destination) < 1.0D) {
            discard();
            return;
        }
        Vec3 delta = destination.subtract(from);
        double maxSpeed = recalling ? RECALL_SPEED : MAX_SPEED;
        Vec3 desired = delta.scale(recalling ? 0.42D : 0.38D);
        if (desired.lengthSqr() > maxSpeed * maxSpeed) {
            desired = desired.normalize().scale(maxSpeed);
        }
        Vec3 acceleration = desired.subtract(getDeltaMovement());
        double maxAcceleration = recalling ? 0.4D : CONTROL_ACCELERATION;
        if (acceleration.lengthSqr() > maxAcceleration * maxAcceleration) {
            acceleration = acceleration.normalize().scale(maxAcceleration);
        }
        Vec3 velocity = getDeltaMovement().add(acceleration);
        if (velocity.lengthSqr() > maxSpeed * maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }
        Vec3 to = from.add(velocity);
        double movementSpeed = velocity.length();
        if (!recalling) {
            HitResult blockHit = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                to = blockHit.getLocation().subtract(velocity.lengthSqr() > 1.0E-6D ? velocity.normalize().scale(0.08D) : Vec3.ZERO);
                velocity = Vec3.ZERO;
            }
            hitEntities(server, player, from, to, movementSpeed);
        }
        setPos(to.x, to.y, to.z);
        setDeltaMovement(velocity);
        updateRotation(velocity);
    }

    private boolean validWeapon(Player player) {
        if (swordHand() == InteractionHand.MAIN_HAND && player.getInventory().getSelectedSlot() != entityData.get(DATA_SLOT)) {
            return false;
        }
        ItemStack held = player.getItemInHand(swordHand());
        return held.is(ModTags.Items.CONTROLLABLE_SWORDS) && ItemStack.isSameItemSameComponents(held, sword());
    }

    private void updateRotation(Vec3 velocity) {
        if (velocity.lengthSqr() < 0.0001D) {
            return;
        }
        double horizontal = velocity.horizontalDistance();
        setYRot((float) Math.toDegrees(Math.atan2(velocity.x, velocity.z)));
        setXRot((float) -Math.toDegrees(Math.atan2(velocity.y, horizontal)));
    }

    private void hitEntities(ServerLevel server, Player owner, Vec3 from, Vec3 to, double speed) {
        if (speed < 0.22D || from.distanceToSqr(to) < 1.0E-5D) {
            return;
        }
        AABB sweep = getBoundingBox().expandTowards(to.subtract(from)).inflate(0.35D);
        for (Entity entity : server.getEntities(this, sweep)) {
            if (!(entity instanceof LivingEntity target) || target == owner || !target.isAlive()
                    || owner.isAlliedTo(target) || target.getRootVehicle() == owner.getRootVehicle()
                    || target instanceof TamableAnimal pet && pet.isOwnedBy(owner)) {
                continue;
            }
            if (!target.getBoundingBox().inflate(0.3D).contains(from)
                    && target.getBoundingBox().inflate(0.3D).clip(from, to).isEmpty()) {
                continue;
            }
            long now = server.getGameTime();
            if (now - hitTimes.getOrDefault(target.getUUID(), -1000L) < HIT_COOLDOWN) {
                continue;
            }
            hitTimes.put(target.getUUID(), now);
            ItemStack weapon = sword();
            DamageSource source = server.damageSources().playerAttack(owner);
            float damage = weaponDamage(weapon) * (float) Mth.clamp(0.5D + speed / MAX_SPEED * 0.65D, 0.5D, 1.15D);
            damage = EnchantmentHelper.modifyDamage(server, weapon, target, source, damage);
            if (target.hurtServer(server, source, damage)) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(server, target, source, weapon);
                server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.5D,
                        target.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.08D);
            }
        }
        if (hitTimes.size() > 128) {
            Iterator<Map.Entry<UUID, Long>> iterator = hitTimes.entrySet().iterator();
            long cutoff = server.getGameTime() - HIT_COOLDOWN;
            while (iterator.hasNext()) {
                if (iterator.next().getValue() < cutoff) {
                    iterator.remove();
                }
            }
        }
    }

    private static float weaponDamage(ItemStack stack) {
        float damage = 1.0F;
        for (ItemAttributeModifiers.Entry entry : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)
                    && entry.slot().test(EquipmentSlot.MAINHAND)
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                damage += (float) entry.modifier().amount();
            }
        }
        return Math.max(1.0F, damage);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        recall();
    }
}
