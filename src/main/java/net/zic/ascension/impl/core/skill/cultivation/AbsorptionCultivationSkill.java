package net.zic.ascension.impl.core.skill.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.CastData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.EquipSkillData;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * ascension:absorption_cultivation. An active (castable) skill like ascension:active: equip it and hold the cast input to absorb.
 * While it is held it
 * <ul>
 *   <li>absorbs blocks around the player (environment_absorption_type): each listed block gives its value as progress on the path</li>
 *   <li>absorbs the item in the main hand (item_absorption_type): consumes one item per absorption, giving its value
 *       as progress on the path (cultivation_type) and/or acting as the absorb_catalyst of an evolution (evolution_type)</li>
 * </ul>
 * The cast ends when the input is released. Both sections are lists of objects whose fields are merged, so the fields can be
 * split over several objects or put in one. "enviroment_absorption_type" (old spelling) is accepted as well.
 */
public final class AbsorptionCultivationSkill implements CastableSkill<AbsorptionCultivationSkill.Data, AbsorptionCultivationSkill.Cast, EquipSkillData.Empty> {
    public static final Identifier ABSORBING = AscensionCraft.prefix("absorbing");

    private static final SerializerHandler<Data> SERIALIZER_HANDLER = new DataSerializerHandler();
    private static final SyncHandler<Data> SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();
    private static final SyncHandler<Cast> CAST_SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();

    public static final MapCodec<AbsorptionCultivationSkill> CODEC = RecordCodecBuilder.<AbsorptionCultivationSkill>mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(AbsorptionCultivationSkill::getName),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(AbsorptionCultivationSkill::getDescription),
            Identifier.CODEC.fieldOf("path").forGetter(AbsorptionCultivationSkill::path),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("cooldown", ScaledValue.constant(0.0D)).forGetter(AbsorptionCultivationSkill::cooldown),
            EnvironmentPart.LIST.optionalFieldOf("environment_absorption_type", List.of())
                    .forGetter(skill -> skill.environment.map(environment -> List.of(environment.toPart())).orElse(List.of())),
            EnvironmentPart.LIST.optionalFieldOf("enviroment_absorption_type", List.of())
                    .forGetter(skill -> List.<EnvironmentPart>of()),
            ItemPart.LIST.optionalFieldOf("item_absorption_type", List.of())
                    .forGetter(skill -> skill.items.map(items -> List.of(items.toPart())).orElse(List.of()))
    ).apply(instance, (name, description, path, cooldown, environmentParts, legacyEnvironmentParts, itemParts) -> {
        List<EnvironmentPart> environment = new ArrayList<>(environmentParts);
        environment.addAll(legacyEnvironmentParts);
        return new AbsorptionCultivationSkill(name, description, path, cooldown, Environment.merge(environment), ItemAbsorption.merge(itemParts));
    })).flatXmap(AbsorptionCultivationSkill::validate, DataResult::success);

    private final Component name;
    private final Component description;
    private final Identifier path;
    private final ScaledValue cooldown;
    private final Optional<Environment> environment;
    private final Optional<ItemAbsorption> items;

    public AbsorptionCultivationSkill(Component name, Component description, Identifier path, ScaledValue cooldown,
                                      Optional<Environment> environment, Optional<ItemAbsorption> items) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.cooldown = cooldown == null ? ScaledValue.constant(0.0D) : cooldown;
        this.environment = environment == null ? Optional.empty() : environment;
        this.items = items == null ? Optional.empty() : items;
    }

    private static DataResult<AbsorptionCultivationSkill> validate(AbsorptionCultivationSkill skill) {
        if (skill.environment.isEmpty() && skill.items.isEmpty()) {
            return DataResult.error(() -> "An absorption_cultivation skill needs environment_absorption_type or item_absorption_type");
        }
        if (skill.items.isPresent() && skill.items.get().cultivationType().isEmpty()) {
            return DataResult.error(() -> "item_absorption_type needs cultivation_type (true or false)");
        }
        return DataResult.success(skill);
    }

    /** the path the absorbed progress goes to */
    public Identifier path() {
        return path;
    }

    /** ticks before the skill can be cast again, applied when a cast ends */
    public ScaledValue cooldown() {
        return cooldown;
    }

    public Optional<Environment> environment() {
        return environment;
    }

    public Optional<ItemAbsorption> items() {
        return items;
    }

    // ── config ─────────────────────────────────────────────────────────────────────────

    // ── config ─────────────────────────────────────────────────────────────────────────

    /** "minecraft:stone, 5": an id and how much progress it gives. The value can be left out and then is 1 */
    public record ValueEntry(Identifier id, double value) {
        public static final Codec<ValueEntry> CODEC = Codec.STRING.comapFlatMap(ValueEntry::parse, ValueEntry::format);

        static DataResult<ValueEntry> parse(String text) {
            String[] parts = text.split(",", 2);
            Identifier id = Identifier.tryParse(parts[0].trim());
            if (id == null) {
                return DataResult.error(() -> "Invalid id in absorption entry: " + text);
            }
            double value = 1.0D;
            if (parts.length > 1) {
                try {
                    value = Double.parseDouble(parts[1].trim());
                } catch (NumberFormatException exception) {
                    return DataResult.error(() -> "Invalid value in absorption entry: " + text);
                }
            }
            if (!Double.isFinite(value) || value < 0.0D) {
                return DataResult.error(() -> "The value of an absorption entry has to be 0 or more: " + text);
            }
            return DataResult.success(new ValueEntry(id, value));
        }

        String format() {
            return id + ", " + value;
        }
    }

    static Map<Identifier, Double> toMap(List<ValueEntry> entries, Map<Identifier, Double> into) {
        for (ValueEntry entry : entries) {
            into.put(entry.id(), entry.value());
        }
        return into;
    }

    static List<ValueEntry> toEntries(Map<Identifier, Double> map) {
        List<ValueEntry> entries = new ArrayList<>();
        map.forEach((id, value) -> entries.add(new ValueEntry(id, value)));
        return entries;
    }

    /**
     * The shape of the area blocks are absorbed from, with the player in the middle of it.
     * The block the player is standing on is never absorbed, whatever the shape.
     */
    public enum Shape implements StringRepresentable {
        /** every block whose distance from the player is at most the radius */
        SPHERE("sphere") {
            @Override
            public boolean contains(int dx, int dy, int dz, int radius) {
                return dx * dx + dy * dy + dz * dz <= radius * radius;
            }
        },
        /** every block no further than the radius on each axis, a cube with sides of radius * 2 + 1 blocks */
        CUBE("cube") {
            @Override
            public boolean contains(int dx, int dy, int dz, int radius) {
                return Math.abs(dx) <= radius && Math.abs(dy) <= radius && Math.abs(dz) <= radius;
            }
        };

        public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);
        private final String serializedName;

        Shape(String serializedName) {
            this.serializedName = serializedName;
        }

        /** whether a block this far from the player (in blocks, per axis) is inside the shape */
        public abstract boolean contains(int dx, int dy, int dz, int radius);

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    /** One object of environment_absorption_type, every field optional */
    record EnvironmentPart(Optional<Integer> radius, Optional<Shape> shape, Optional<Boolean> breakingAnimation, Optional<Integer> timeNeeded,
                           Optional<Boolean> evolutionType, List<ValueEntry> blocks, List<ValueEntry> blockTags) {
        static final Codec<EnvironmentPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 8).optionalFieldOf("radius").forGetter(EnvironmentPart::radius),
                Shape.CODEC.optionalFieldOf("shape").forGetter(EnvironmentPart::shape),
                Codec.BOOL.optionalFieldOf("breaking_animation").forGetter(EnvironmentPart::breakingAnimation),
                Codec.intRange(1, 12000).optionalFieldOf("time_needed").forGetter(EnvironmentPart::timeNeeded),
                Codec.BOOL.optionalFieldOf("evolution_type").forGetter(EnvironmentPart::evolutionType),
                ValueEntry.CODEC.listOf().optionalFieldOf("blocks", List.of()).forGetter(EnvironmentPart::blocks),
                ValueEntry.CODEC.listOf().optionalFieldOf("block_tags", List.of()).forGetter(EnvironmentPart::blockTags)
        ).apply(instance, EnvironmentPart::new));
        // one object or a list of objects
        static final Codec<List<EnvironmentPart>> LIST = Codec.withAlternative(CODEC.listOf(), CODEC, List::of);
    }

    /** One object of item_absorption_type, every field optional */
    record ItemPart(Optional<Boolean> evolutionType, Optional<Boolean> cultivationType, Optional<Integer> timeNeeded,
                    List<ValueEntry> items, List<ValueEntry> itemTags) {
        static final Codec<ItemPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("evolution_type").forGetter(ItemPart::evolutionType),
                Codec.BOOL.optionalFieldOf("cultivation_type").forGetter(ItemPart::cultivationType),
                Codec.intRange(1, 12000).optionalFieldOf("time_needed").forGetter(ItemPart::timeNeeded),
                ValueEntry.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(ItemPart::items),
                ValueEntry.CODEC.listOf().optionalFieldOf("item_tags", List.of()).forGetter(ItemPart::itemTags)
        ).apply(instance, ItemPart::new));
        static final Codec<List<ItemPart>> LIST = Codec.withAlternative(CODEC.listOf(), CODEC, List::of);
    }

    /**
     * Blocks around the player that get absorbed.
     *
     * @param radius            how far from the player a block can be, 1-8, default 2
     * @param shape             sphere or cube, the player is in the middle of it, default sphere
     * @param breakingAnimation whether the vanilla block breaking animation shows while it is absorbed, default true
     * @param timeNeeded        ticks one block takes to absorb, default 20
     * @param evolutionType     whether an absorbed block can also act as an absorb_catalyst of an evolution, default false
     * @param blocks            block id to the progress it gives
     * @param blockTags         block tag id to the progress it gives (a direct block entry wins over a tag)
     */
    public record Environment(int radius, Shape shape, boolean breakingAnimation, int timeNeeded, boolean evolutionType,
                              Map<Identifier, Double> blocks, Map<Identifier, Double> blockTags) {
        static Optional<Environment> merge(List<EnvironmentPart> parts) {
            if (parts.isEmpty()) {
                return Optional.empty();
            }
            int radius = 2;
            Shape shape = Shape.SPHERE;
            boolean breakingAnimation = true;
            int timeNeeded = 20;
            boolean evolutionType = false;
            Map<Identifier, Double> blocks = new LinkedHashMap<>();
            Map<Identifier, Double> blockTags = new LinkedHashMap<>();
            for (EnvironmentPart part : parts) {
                radius = part.radius().orElse(radius);
                shape = part.shape().orElse(shape);
                breakingAnimation = part.breakingAnimation().orElse(breakingAnimation);
                timeNeeded = part.timeNeeded().orElse(timeNeeded);
                evolutionType = part.evolutionType().orElse(evolutionType);
                toMap(part.blocks(), blocks);
                toMap(part.blockTags(), blockTags);
            }
            return Optional.of(new Environment(radius, shape, breakingAnimation, timeNeeded, evolutionType, blocks, blockTags));
        }

        EnvironmentPart toPart() {
            return new EnvironmentPart(Optional.of(radius), Optional.of(shape), Optional.of(breakingAnimation), Optional.of(timeNeeded),
                    Optional.of(evolutionType), toEntries(blocks), toEntries(blockTags));
        }

        /** whether a block this far from the player is inside the area, extra widens the radius by that much */
        public boolean covers(int dx, int dy, int dz, int extra) {
            return shape.contains(dx, dy, dz, radius + extra);
        }

        public Identifier id(BlockState state) {
            return BuiltInRegistries.BLOCK.getKey(state.getBlock());
        }

        /** the progress this block gives, empty if it is not absorbed */
        public OptionalDouble value(BlockState state) {
            Double direct = blocks.get(id(state));
            if (direct != null) {
                return OptionalDouble.of(direct);
            }
            double best = -1.0D;
            for (Map.Entry<Identifier, Double> tag : blockTags.entrySet()) {
                if (state.is(TagKey.create(Registries.BLOCK, tag.getKey()))) {
                    best = Math.max(best, tag.getValue());
                }
            }
            return best < 0.0D ? OptionalDouble.empty() : OptionalDouble.of(best);
        }
    }

    /**
     * The item in the main hand that gets absorbed while sneaking.
     *
     * @param evolutionType   whether an absorbed item can act as an absorb_catalyst of an evolution, default false
     * @param cultivationType whether an absorbed item gives progress on the path. Required
     * @param timeNeeded      ticks one item takes to absorb, default 20
     * @param items           item id to the progress it gives
     * @param itemTags        item tag id to the progress it gives (a direct item entry wins over a tag)
     */
    public record ItemAbsorption(boolean evolutionType, Optional<Boolean> cultivationType, int timeNeeded,
                                 Map<Identifier, Double> items, Map<Identifier, Double> itemTags) {
        static Optional<ItemAbsorption> merge(List<ItemPart> parts) {
            if (parts.isEmpty()) {
                return Optional.empty();
            }
            boolean evolutionType = false;
            Optional<Boolean> cultivationType = Optional.empty();
            int timeNeeded = 20;
            Map<Identifier, Double> items = new LinkedHashMap<>();
            Map<Identifier, Double> itemTags = new LinkedHashMap<>();
            for (ItemPart part : parts) {
                evolutionType = part.evolutionType().orElse(evolutionType);
                if (part.cultivationType().isPresent()) {
                    cultivationType = part.cultivationType();
                }
                timeNeeded = part.timeNeeded().orElse(timeNeeded);
                toMap(part.items(), items);
                toMap(part.itemTags(), itemTags);
            }
            return Optional.of(new ItemAbsorption(evolutionType, cultivationType, timeNeeded, items, itemTags));
        }

        ItemPart toPart() {
            return new ItemPart(Optional.of(evolutionType), cultivationType, Optional.of(timeNeeded), toEntries(items), toEntries(itemTags));
        }

        public boolean cultivates() {
            return cultivationType.orElse(false);
        }

        public Identifier id(ItemStack stack) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem());
        }

        /** the progress this item gives, empty if it is not absorbed */
        public OptionalDouble value(ItemStack stack) {
            if (stack.isEmpty()) {
                return OptionalDouble.empty();
            }
            Double direct = items.get(id(stack));
            if (direct != null) {
                return OptionalDouble.of(direct);
            }
            double best = -1.0D;
            for (Map.Entry<Identifier, Double> tag : itemTags.entrySet()) {
                if (stack.is(TagKey.create(Registries.ITEM, tag.getKey()))) {
                    best = Math.max(best, tag.getValue());
                }
            }
            return best < 0.0D ? OptionalDouble.empty() : OptionalDouble.of(best);
        }
    }

    // ── skill ──────────────────────────────────────────────────────────────────────────

    //──Cast────────────────────────────────────────────────────────

    @Override
    public CastResult tryCast(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
        EntityCooldownHandler cooldownHandler = instance.getCaster().getData(ZenithAttachments.COOLDOWN_HANDLER);
        if (cooldownHandler.isOnCooldown(instance.getSkillId())) {
            return CastResult.fail(Component.literal(
                    "On Cooldown (" + cooldownHandler.getCooldown(instance.getSkillId()) / 20.0D + "s)"
            ));
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(instance.getCaster());
        if (source == null || AscensionOriginSourceHelper.getPathInstance(source, path) == null) {
            return CastResult.fail(Component.translatable("ascension.absorption.fail.no_path"));
        }
        return CastResult.success();
    }

    @Override
    public void startCast(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
        instance.setPhase(ABSORBING);
    }

    //absorbs every tick the input is held, and lets go when it is released
    @Override
    public void tickCast(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
        if (!instance.isPhase(ABSORBING)) {
            instance.end(CastStatus.Reason.INVALIDATED);
            return;
        }
        if (!instance.isInputHeld()) {
            instance.end(CastStatus.Reason.COMPLETED);
            return;
        }
        if (instance.getCaster() instanceof ServerPlayer player) {
            AbsorptionCultivationService.tick(player, instance.getSkillId(), this);
        }
    }

    @Override
    public @Nullable ActiveCastVisualState visualState(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
        return null;
    }

    @Override
    public void endCast(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
        if (instance.getCaster() instanceof ServerPlayer player) {
            AbsorptionCultivationService.stop(player);
            if (instance.getStatus().isCompleted()) {
                applyCooldown(player, instance.getSkillId());
            }
        }
    }

    private void applyCooldown(ServerPlayer caster, Identifier skillId) {
        double value = cooldown.resolve(new ScaledValue.Context(
                AscensionOriginSourceHelper.getEntitySource(caster), skillId, caster, null, 0.0D, Map.of()));
        if (!Double.isFinite(value) || value <= 0.0D) {
            return;
        }
        int ticks = (int) Math.clamp(Math.round(value), 0L, (long) Integer.MAX_VALUE);
        if (ticks > 0) {
            caster.getData(ZenithAttachments.COOLDOWN_HANDLER).addCooldown(skillId, ticks);
        }
    }

    @Override
    public Cast newCastData(RegistryAccess access) {
        return new Cast();
    }

    @Override
    public SyncHandler<Cast> castSyncHandler() {
        return CAST_SYNC_HANDLER;
    }

    //──Equip────────────────────────────────────────────────────────

    @Override
    public EquipSkillData.Empty newEquipData(RegistryAccess access) {
        return EquipSkillData.Empty.INSTANCE;
    }

    @Override
    public SerializerHandler<EquipSkillData.Empty> equipSerializerHandler() {
        return EquipSkillData.Empty.SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<EquipSkillData.Empty> equipSyncHandler() {
        return EquipSkillData.Empty.SYNC_HANDLER;
    }

    //──Skill────────────────────────────────────────────────────────

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.ABSORPTION_CULTIVATION_SKILL_TYPE.get();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public void onAdded(OriginSource source, SkillInstance<Data> instance) {
    }

    @Override
    public void onRemoved(OriginSource source, SkillInstance<Data> instance) {
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillInstance<Data> instance) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillInstance<Data> instance) {
    }

    @Override
    public Data newData(RegistryAccess access) {
        return new Data();
    }

    @Override
    public SerializerHandler<Data> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<Data> syncHandler() {
        return SYNC_HANDLER;
    }

    //the skill holds no state
    public static final class Data implements SkillData {
    }

    //progress of a cast is kept by AbsorptionCultivationService
    public static final class Cast implements CastData {
    }

    private static final class DataSerializerHandler implements SerializerHandler<Data> {
        @Override
        public Data read(ValueInput input, RegistryAccess access) {
            return new Data();
        }

        @Override
        public void write(Data writable, ValueOutput output, RegistryAccess access) {
        }
    }
}