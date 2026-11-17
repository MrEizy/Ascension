package net.zic.ascension.common.entities.weapons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.common.enchantments.AscEnchantments;
import net.zic.ascension.common.item.weapons.ChakramItem;
import net.zic.ascension.common.item.weapons.ChakramTier;
import net.zic.ascension.common.sounds.AscSounds;
import net.zic.ascension.common.util.ModTags;

import java.util.HashSet;
import java.util.Set;

public class ChakramEntity extends Projectile {

    public static final int OFFHAND_SLOT = -1;
    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(ChakramEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_RETURNING =
            SynchedEntityData.defineId(ChakramEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int BASE_RICOCHETS = 2;
    private static final int BASE_FLIGHT_TICKS = 8;
    private static final int FLIGHT_TICKS_PER_RICOCHET = 6;
    private static final int MAX_RETURN_TICKS = 300;
    private static final double GRAVITY = 0.012;      // match reference
    private static final double AIR_DRAG = 0.99;
    private static final double WATER_DRAG = 0.80;
    private static final float RAZOR_EDGE_DAMAGE = 0.8F;
    private static final double CATCH_DISTANCE = 0.8;
    private static final int HIT_LEG_THRESHOLD = 20;

    private float power = 1.0F;
    private int slot = OFFHAND_SLOT;
    private boolean creative;
    private int ricochetsLeft = BASE_RICOCHETS;
    private int flightTicks;
    private int maxFlightTicks = BASE_FLIGHT_TICKS;
    private int returnTicks;
    private int swiftReturn = -1;
    private boolean phasingRecall;
    private final Set<Integer> hitThisLeg = new HashSet<>();

    public ChakramEntity(EntityType<? extends ChakramEntity> type, Level level) {
        super(type, level);
    }

    public ChakramEntity(EntityType<ChakramEntity> type, ServerLevel level,
                         LivingEntity owner, ItemStack stack, float power, int slot, boolean creative) {
        super(type, level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.15, owner.getZ());
        setItem(stack);
        this.power = power;
        this.slot = slot;
        this.creative = creative;
        this.ricochetsLeft = BASE_RICOCHETS + enchantLevel(AscEnchantments.RICOCHET);
        this.maxFlightTicks = BASE_FLIGHT_TICKS + Math.round(4.0F * power);
        this.swiftReturn = enchantLevel(AscEnchantments.SWIFT_RETURN);
        this.phasingRecall = enchantLevel(AscEnchantments.PHASING_RECALL) > 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(DATA_ITEM, ItemStack.EMPTY);
        b.define(DATA_RETURNING, false);
    }

    public ItemStack getItem() { return entityData.get(DATA_ITEM); }
    public void setItem(ItemStack s) { entityData.set(DATA_ITEM, s.copyWithCount(1)); }
    public boolean isReturning() { return entityData.get(DATA_RETURNING); }
    private void setReturning(boolean b) { entityData.set(DATA_RETURNING, b); }

    private ChakramTier tier() {
        return getItem().getItem() instanceof ChakramItem item ? item.tier() : ChakramTier.IRON;
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) return;
        if (level().isClientSide()) {
            updateFacing();
            return;
        }
        Vec3 from = position();
        if (isReturning()) tickReturning(from);
        else tickFlying(from);
        updateFacing();
    }

    private boolean tryBreakDestructible(BlockHitResult hit) {
        BlockState state = level().getBlockState(hit.getBlockPos());
        if (!state.is(ModTags.Blocks.CHAKRAM_DESTRUCTION)) return false;

        if (level() instanceof ServerLevel server) {
            server.destroyBlock(hit.getBlockPos(), false, this);
            server.sendParticles(ParticleTypes.CRIT,
                    hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    6, 0.2, 0.2, 0.2, 0.1);
        }
        return true;
    }

    private void tickFlying(Vec3 from) {
        Vec3 vel = getDeltaMovement();
        Vec3 to = from.add(vel);

        HitResult hit = level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        boolean hitBlock = hit.getType() == HitResult.Type.BLOCK;
        Vec3 end = hitBlock ? hit.getLocation() : to;

        if (level() instanceof ServerLevel server) {
            hitEntities(server, from, end);
            if (isRemoved()) return;
        }

        if (hitBlock) {
            BlockHitResult bhr = (BlockHitResult) hit;
            if (!tryBreakDestructible(bhr)) {
                onHitBlock(bhr, vel);
                return;
            }
        }
        setPos(to.x, to.y, to.z);

        double drag = isInWater() ? WATER_DRAG : AIR_DRAG;
        Vec3 v = getDeltaMovement();
        setDeltaMovement(v.x * drag, v.y * drag - GRAVITY, v.z * drag);

        flightTicks++;
        if (level() instanceof ServerLevel && flightTicks >= maxFlightTicks) {
            startReturning();
        }
    }

    private void onHitBlock(BlockHitResult hit, Vec3 vel) {
        Direction face = hit.getDirection();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 at = hit.getLocation().add(normal.scale(0.05));
        setPos(at.x, at.y, at.z);

        BlockState state = level().getBlockState(hit.getBlockPos());
        boolean server = level() instanceof ServerLevel;
        if (server) state.onProjectileHit(level(), state, hit, this);

        double into = vel.dot(normal);
        double speed = vel.length();
        Vec3 reflected = vel.subtract(normal.scale(2.0 * into));

        if (state.is(ModTags.Blocks.NO_RICOCHET)) {
            setDeltaMovement(vel.scale(0.05));
            if (server) {
                sound(AscSounds.CHAKRAM_THUD.get(), 0.8F, 1.0F);
                startReturning();
            }
        } else if (state.is(ModTags.Blocks.BOUNCY)) {
            setDeltaMovement(reflected.scale(1.05));
            if (server) sound(AscSounds.CHAKRAM_RICOCHET.get(), 0.6F, 1.6F);
        } else if (-into < 0.22 * speed) {
            // Grazing hit — slide along the surface instead of bouncing.
            Vec3 along = vel.subtract(normal.scale(into));
            setDeltaMovement(along.scale(0.93).add(normal.scale(-into * 0.45)));
        } else if (server && ricochetsLeft <= 0) {
            setDeltaMovement(reflected.scale(0.35));
            sound(AscSounds.CHAKRAM_THUD.get(), 0.7F, 1.2F);
            startReturning();
        } else {
            ricochetsLeft--;
            maxFlightTicks += FLIGHT_TICKS_PER_RICOCHET;
            hitThisLeg.clear();
            setDeltaMovement(reflected.scale(0.85));
            if (level() instanceof ServerLevel server1) {
                sound(AscSounds.CHAKRAM_RICOCHET.get(), 0.9F, 0.9F + random.nextFloat() * 0.3F);
                server1.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 8, 0.1, 0.1, 0.1, 0.3);
            }
        }
    }

    private void tickReturning(Vec3 from) {
        // Show a subtle phase trail while the chakram is travelling through solid blocks.
        if (phasingRecall && level() instanceof ServerLevel serverLevel
                && level().getBlockState(BlockPos.containing(from)).isSolid()) {
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    from.x, from.y, from.z, 3, 0.1, 0.1, 0.1, 0.05);
        }

        Entity owner = getOwner();
        boolean server = level() instanceof ServerLevel;
        if (owner == null || !owner.isAlive() || owner.isSpectator() || owner.level() != level()) {
            if (server) dropAsItem();
            return;
        }

        returnTicks++;
        if (server && returnTicks > MAX_RETURN_TICKS) {
            dropAsItem();
            return;
        }

        Vec3 target = owner.getEyePosition().subtract(0, 0.45, 0);
        Vec3 toTarget = target.subtract(from);
        double dist = toTarget.length();
        double catchDist = 1.2 + owner.getBbWidth() * 0.5;

        if (server && dist < catchDist) {
            catchBy(owner);
            return;
        }

        if (swiftReturn < 0) swiftReturn = enchantLevel(AscEnchantments.SWIFT_RETURN);
        double speed = (1.1 + 0.35 * swiftReturn) * tier().ricochetSpeed;
        double ramp = Math.min(1.0, 0.35 + returnTicks / 10.0);
        Vec3 desired = dist > 1.0E-6 ? toTarget.scale(Math.min(speed * ramp, dist) / dist) : Vec3.ZERO;
        Vec3 vel = getDeltaMovement().scale(0.55).add(desired.scale(0.45));
        if (vel.length() < desired.length() * 0.6) vel = desired.scale(0.6);

        Vec3 to = from.add(vel);

        // Phasing Recall bypasses block collision only during the return leg.
        if (!phasingRecall) {
            HitResult hit = level().clip(new ClipContext(
                    from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bhr = (BlockHitResult) hit;
                if (!tryBreakDestructible(bhr)) {
                    Vec3 at = bhr.getLocation().add(
                            bhr.getDirection().getStepX() * 0.05,
                            bhr.getDirection().getStepY() * 0.05,
                            bhr.getDirection().getStepZ() * 0.05);
                    setPos(at.x, at.y, at.z);
                    setDeltaMovement(Vec3.ZERO);
                    if (server) {
                        sound(AscSounds.CHAKRAM_THUD.get(), 0.6F, 1.0F);
                        dropAsItem();
                    }
                    return;
                }
            }
        }

        if (server) {
            hitEntities((ServerLevel) level(), from, to);
            if (isRemoved()) return;
        }

        setPos(to.x, to.y, to.z);
        setDeltaMovement(vel);
    }

    private void updateFacing() {
        Vec3 v = getDeltaMovement();
        double horizontal = v.horizontalDistance();
        if (horizontal > 1.0E-4) {
            setYRot((float) Math.toDegrees(Math.atan2(v.x, v.z)));
        }
        setXRot((float) Math.toDegrees(Math.atan2(v.y, Math.max(horizontal, 1.0E-4))));
    }

    private void hitEntities(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        AABB sweep = getBoundingBox().expandTowards(delta).inflate(0.4);
        Entity owner = getOwner();

        for (Entity target : level.getEntities(this, sweep)) {
            if (!canDiscHit(target, owner)) continue;
            AABB box = target.getBoundingBox().inflate(0.35);
            if (box.contains(from) || box.clip(from, to).isPresent()) {
                hurt(level, target, owner);
                if (isRemoved()) return;
            }
        }
    }

    private boolean canDiscHit(Entity target, Entity owner) {
        if (target == owner || !target.isAlive()) return false;
        if (hitThisLeg.contains(target.getId())) return false;
        if (target instanceof ChakramEntity) return false;
        if (owner != null && owner.getRootVehicle() == target.getRootVehicle()) return false;
        if (owner instanceof LivingEntity livingOwner
                && target instanceof TamableAnimal pet
                && pet.isOwnedBy(livingOwner)) return false;
        return true;
    }

    private void hurt(ServerLevel level, Entity target, Entity owner) {
        hitThisLeg.add(target.getId());
        ItemStack weapon = getItem();
        float damage = tier().damage * (0.45F + 0.55F * power);
        int razor = enchantLevel(AscEnchantments.RAZOR_EDGE);
        if (razor > 0) damage += RAZOR_EDGE_DAMAGE * razor;
        if (isReturning()) damage *= 0.8F;

        DamageSource source = AscensionDamage.chakram(this, owner == null ? this : owner);
        damage = EnchantmentHelper.modifyDamage(level, weapon, target, source, damage);

        if (target.hurtServer(level, source, damage)) {
            if (target instanceof LivingEntity living) {
                Vec3 push = getDeltaMovement().multiply(1, 0, 1);
                if (push.lengthSqr() > 1.0E-4) {
                    push = push.normalize().scale(0.3 + 0.15 * power);
                    living.push(push.x, 0.08, push.z);
                }
                EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, weapon);
            }
            sound(AscSounds.CHAKRAM_HIT.get(), 0.9F, 1.0F + random.nextFloat() * 0.2F);
        }
        setDeltaMovement(getDeltaMovement().scale(0.9));
    }

    private void startReturning() {
        if (isReturning()) return;
        setReturning(true);
        hitThisLeg.clear();
        returnTicks = 0;
        sound(AscSounds.CHAKRAM_RETURN.get(), 0.7F, 1.0F);
    }

    private void catchBy(Entity owner) {
        if (!(owner instanceof Player player)) {
            dropAsItem();
            return;
        }
        if (!creative) giveBack(player, getItem().copy());
        level().playSound(null, player.getX(), player.getY(), player.getZ(),
                AscSounds.CHAKRAM_CATCH.get(), SoundSource.PLAYERS, 0.7F, 0.95F + random.nextFloat() * 0.15F);
        discard();
    }

    private void giveBack(Player player, ItemStack stack) {
        Inventory inv = player.getInventory();
        if (slot == OFFHAND_SLOT && player.getOffhandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, stack);
        } else if (slot >= 0 && slot < 9 && inv.getItem(slot).isEmpty()) {
            inv.setItem(slot, stack);
        } else if (!inv.add(stack) && !stack.isEmpty()) {
            ItemEntity drop = new ItemEntity(level(), player.getX(), player.getY(), player.getZ(), stack);
            drop.setNoPickUpDelay();
            level().addFreshEntity(drop);
        }
    }

    private void dropAsItem() {
        if (level() instanceof ServerLevel level) {
            if (!creative && !getItem().isEmpty()) {
                ItemEntity drop = new ItemEntity(level, getX(), getY(), getZ(), getItem().copy());
                drop.setDefaultPickUpDelay();
                level.addFreshEntity(drop);
            }
        }
        discard();
    }

    private void sound(net.minecraft.sounds.SoundEvent event, float volume, float pitch) {
        level().playSound(null, getX(), getY(), getZ(), event, SoundSource.PLAYERS, volume, pitch);
    }

    private int enchantLevel(ResourceKey<Enchantment> key) {
        if (!(level() instanceof ServerLevel)) return 0;
        try {
            return level().registryAccess().lookup(Registries.ENCHANTMENT)
                    .flatMap(r -> r.get(key))
                    .map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, getItem()))
                    .orElse(0);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        if (!getItem().isEmpty()) out.store("Item", ItemStack.CODEC, getItem());
        out.putBoolean("Returning", isReturning());
        out.putFloat("Power", power);
        out.putInt("Slot", slot);
        out.putBoolean("Creative", creative);
        out.putInt("RicochetsLeft", ricochetsLeft);
        out.putInt("FlightTicks", flightTicks);
        out.putInt("MaxFlightTicks", maxFlightTicks);
        out.putBoolean("PhasingRecall", phasingRecall);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        in.read("Item", ItemStack.CODEC).ifPresent(this::setItem);
        setReturning(in.getBooleanOr("Returning", false));
        power = in.getFloatOr("Power", 1.0F);
        slot = in.getIntOr("Slot", OFFHAND_SLOT);
        creative = in.getBooleanOr("Creative", false);
        ricochetsLeft = in.getIntOr("RicochetsLeft", BASE_RICOCHETS);
        flightTicks = in.getIntOr("FlightTicks", 0);
        maxFlightTicks = in.getIntOr("MaxFlightTicks", BASE_FLIGHT_TICKS);
        phasingRecall = in.getBooleanOr("PhasingRecall", enchantLevel(AscEnchantments.PHASING_RECALL) > 0);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 4096.0;
    }
}