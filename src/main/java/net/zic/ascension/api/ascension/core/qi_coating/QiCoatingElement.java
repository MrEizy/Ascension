package net.zic.ascension.api.ascension.core.qi_coating;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An element the Qi Coating skills can be switched to, loaded from
 * {@code data/<namespace>/ascension/skill_system/qi_coating_elements/}. The player needs {@code path} to pick it (an element
 * without one, like Pure Qi, is open to every cultivator), and it puts on the coating {@code body_coating} through Qi Coating
 * and {@code external_coating} through External Qi Coating (coating ids, from {@code skill_system/qi_coatings/}). An
 * element without a coating for a channel isn't offered there.
 * <p>
 * The hooks are what the element does in combat while it is shown (QiCoatingService runs them): {@code body_on_hit} when the
 * player lands a bare-handed melee hit, {@code body_on_hurt} when an attacker hurts them, and {@code external_on_hit} when they
 * land a melee hit with a weapon External Qi Coating shows on. {@code external_on_mine} is what it does to blocks mined with a
 * tool External Qi Coating shows on (QiCoatingMining runs it), {@code external_on_use} what it does when such a tool is used on a
 * block, and {@code external_on_kill} to the drops of something killed with one. Their costs can read how many items the drops
 * hold, as the context variable {@code ascension:qi_coating/items}.
 *
 * @param name           the name shown in the coating wheel
 * @param path           the path (Dao) the player needs, or empty if anyone can pick it
 * @param bodyCoating     the coating Qi Coating puts on
 * @param externalCoating the coating External Qi Coating puts on
 * @param order          where it sits in the wheel, lowest first
 * @param bodyOnHit      run on the entity hit, for a bare-handed melee hit while Qi Coating shows this element
 * @param bodyOnHurt     run on the attacker, when the player is hurt while Qi Coating shows this element
 * @param externalOnHit  run on the entity hit, for a melee hit with a coated weapon while External Qi Coating shows this element
 * @param externalOnMine what it does to blocks mined with a coated tool while External Qi Coating shows this element
 * @param externalOnUse  what it does when a coated tool is used on a block while External Qi Coating shows this element
 * @param externalOnKill what it does to the drops of something killed with a coated weapon while External Qi Coating shows
 *                       this element
 */
public record QiCoatingElement(
        Component name,
        Optional<Identifier> path,
        Optional<Identifier> bodyCoating,
        Optional<Identifier> externalCoating,
        int order,
        Optional<Hook> bodyOnHit,
        Optional<Hook> bodyOnHurt,
        Optional<Hook> externalOnHit,
        Optional<Mining> externalOnMine,
        Optional<Use> externalOnUse,
        Optional<Kill> externalOnKill
) {
    public static final Codec<QiCoatingElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(QiCoatingElement::name),
            Identifier.CODEC.optionalFieldOf("path").forGetter(QiCoatingElement::path),
            Identifier.CODEC.optionalFieldOf("body_coating").forGetter(QiCoatingElement::bodyCoating),
            Identifier.CODEC.optionalFieldOf("external_coating").forGetter(QiCoatingElement::externalCoating),
            Codec.INT.optionalFieldOf("order", 0).forGetter(QiCoatingElement::order),
            Hook.CODEC.optionalFieldOf("body_on_hit").forGetter(QiCoatingElement::bodyOnHit),
            Hook.CODEC.optionalFieldOf("body_on_hurt").forGetter(QiCoatingElement::bodyOnHurt),
            Hook.CODEC.optionalFieldOf("external_on_hit").forGetter(QiCoatingElement::externalOnHit),
            Mining.CODEC.optionalFieldOf("external_on_mine").forGetter(QiCoatingElement::externalOnMine),
            Use.CODEC.optionalFieldOf("external_on_use").forGetter(QiCoatingElement::externalOnUse),
            Kill.CODEC.optionalFieldOf("external_on_kill").forGetter(QiCoatingElement::externalOnKill)
    ).apply(instance, QiCoatingElement::new));

    /**
     * The element with this id, or null if no datapack defines it.
     */
    public static QiCoatingElement byId(Identifier id, RegistryAccess access) {
        return CoreRegistries.safeAccess(CoreRegistries.QI_COATING_ELEMENT_REGISTRY, id, access);
    }

    /**
     * The coating the element puts on through a channel, if it has one there.
     */
    public Optional<Identifier> coating(QiCoatingChannel channel) {
        return channel == QiCoatingChannel.BODY ? bodyCoating : externalCoating;
    }

    /**
     * What an element does when its hook fires: like a passive skill's trigger without the event, which the hook itself decides.
     *
     * @param cooldown   ticks before it can fire again; 0 fires on every hit
     * @param costs      paid each time it fires; it doesn't fire if the player can't pay
     * @param conditions all must pass for it to fire
     * @param actions    skill actions run with the entity hit (or the attacker, for body_on_hurt) as the target
     */
    public record Hook(int cooldown, List<ActiveSkillCostDefinition> costs, List<SkillCondition> conditions, List<SkillAction> actions) {
        public static final Codec<Hook> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 72000).optionalFieldOf("cooldown", 0).forGetter(Hook::cooldown),
                ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost", List.of()).forGetter(Hook::costs),
                SkillCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(Hook::conditions),
                SkillAction.CODEC.listOf().fieldOf("actions").forGetter(Hook::actions)
        ).apply(instance, Hook::new));

        public Hook {
            costs = List.copyOf(costs);
            conditions = List.copyOf(conditions);
            actions = List.copyOf(actions);
        }
    }

    /**
     * What an element does to blocks mined with a coated tool.
     *
     * @param costs       paid for each block whose drops it changes (smelts, converts or takes in); they're left alone if the
     *                    player can't pay
     * @param smelt       drops are swapped for what a furnace makes of them (no furnace experience: only what the block drops
     *                    itself, as ores like coal and lapis do)
     * @param convert     the mined block's own drop (the block as an item) is swapped for the item it names, count kept; other
     *                    drops (stone's cobblestone, say) are left alone. A conversion may cost something else (Conversion)
     * @param toInventory drops go straight into the player's inventory; what doesn't fit falls as usual
     * @param speed       faster mining on some blocks (costs nothing beyond the coating's upkeep); one entry or a list
     * @param fortune     drops are rolled as if the tool had this many more levels of Fortune, on top of its own (free)
     * @param silkTouch   these blocks drop as if the tool had Silk Touch, and ice among them doesn't melt (free)
     * @param airborne    mining off the ground is as fast as on it (no fivefold slowdown)
     * @param underwater  mining with the head under water is as fast as out of it, as with Aqua Affinity
     */
    public record Mining(List<ActiveSkillCostDefinition> costs, boolean smelt, Map<Item, Conversion> convert, boolean toInventory,
                         List<Speed> speed, int fortune, Optional<HolderSet<Block>> silkTouch, boolean airborne, boolean underwater) {
        public static final Codec<Mining> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost", List.of()).forGetter(Mining::costs),
                Codec.BOOL.optionalFieldOf("smelt", false).forGetter(Mining::smelt),
                Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Conversion.CODEC)
                        .optionalFieldOf("convert", Map.of()).forGetter(Mining::convert),
                Codec.BOOL.optionalFieldOf("to_inventory", false).forGetter(Mining::toInventory),
                ExtraCodecs.compactListCodec(Speed.CODEC).optionalFieldOf("speed", List.of()).forGetter(Mining::speed),
                Codec.intRange(0, 10).optionalFieldOf("fortune", 0).forGetter(Mining::fortune),
                RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("silk_touch").forGetter(Mining::silkTouch),
                Codec.BOOL.optionalFieldOf("airborne", false).forGetter(Mining::airborne),
                Codec.BOOL.optionalFieldOf("underwater", false).forGetter(Mining::underwater)
        ).apply(instance, Mining::new));

        public Mining {
            costs = List.copyOf(costs);
            convert = Map.copyOf(convert);
            speed = List.copyOf(speed);
        }
    }

    /**
     * What a block converts to: the item, and what that costs in place of the mining's cost if it costs something else. Written
     * as just the item's id, or as {@code {"item": ..., "cost": ...}}.
     */
    public record Conversion(Item item, Optional<List<ActiveSkillCostDefinition>> costs) {
        private static final Codec<Conversion> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Conversion::item),
                ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost").forGetter(Conversion::costs)
        ).apply(instance, Conversion::new));
        public static final Codec<Conversion> CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), FULL_CODEC).xmap(
                either -> either.map(item -> new Conversion(item, Optional.empty()), conversion -> conversion),
                conversion -> conversion.costs().isEmpty() ? Either.left(conversion.item()) : Either.right(conversion)
        );
    }

    /**
     * Mining speed on `blocks` (a block, a list of them, or a #tag) times `multiplier`, with any coated tool or only `tools` (an
     * item, a list of them, or a #tag). Big multipliers break soft blocks instantly (leaves at 50).
     */
    public record Speed(HolderSet<Block> blocks, float multiplier, Optional<HolderSet<Item>> tools) {
        public static final Codec<Speed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(Speed::blocks),
                Codec.floatRange(0.0F, 100.0F).fieldOf("multiplier").forGetter(Speed::multiplier),
                RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("tools").forGetter(Speed::tools)
        ).apply(instance, Speed::new));
    }

    /**
     * What an element does when a coated tool is used on a block (right-click), paid once a use.
     *
     * @param costs     paid each time it does something; nothing happens if the player can't pay. A till's or path's cost can
     *                  read how many blocks it changes, as the context variable {@code ascension:qi_coating/blocks}
     * @param till      a hoe tilling a block tills round it too (Area)
     * @param flatten   a shovel making a path makes it round the block too (Area)
     * @param bonemeal  a hoe used on something bone meal grows (not something it would till) grows it as bone meal does
     * @param transform any coated tool used on one of these blocks turns it into the block it names; a fluid only at its
     *                  source (water to ice, say). A transform may cost something else (Transform)
     */
    public record Use(List<ActiveSkillCostDefinition> costs, Optional<Area> till, Optional<Area> flatten, boolean bonemeal,
                      Map<Block, Transform> transform) {
        public static final Codec<Use> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost", List.of()).forGetter(Use::costs),
                Area.CODEC.optionalFieldOf("till").forGetter(Use::till),
                Area.CODEC.optionalFieldOf("flatten").forGetter(Use::flatten),
                Codec.BOOL.optionalFieldOf("bonemeal", false).forGetter(Use::bonemeal),
                Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Transform.CODEC)
                        .optionalFieldOf("transform", Map.of()).forGetter(Use::transform)
        ).apply(instance, Use::new));

        public Use {
            costs = List.copyOf(costs);
            transform = Map.copyOf(transform);
        }
    }

    /**
     * How far round the block a tool use reaches (Use till and flatten), on the same level, where the tool could change a block.
     * Written as just the radius (at once, the use's cost), or as {@code {"radius": ..., "charge_ticks": ..., "cost": ...}}.
     *
     * @param radius      a round patch this far out (1 is 3x3). A number or a scaled value: it reads {@code ascension:charge}
     *                    (0 to 1, how far the hold got) and can read the player's affinity; capped at 8
     * @param chargeTicks 0 changes the blocks on the click; above 0 the tool is held on the block to charge and changes them on
     *                    release, at full charge after this many ticks
     * @param costs       paid in place of the use's cost, if it costs something else
     */
    public record Area(ScaledValue radius, int chargeTicks, Optional<List<ActiveSkillCostDefinition>> costs) {
        private static final Codec<Area> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("radius").forGetter(Area::radius),
                Codec.intRange(0, 1200).optionalFieldOf("charge_ticks", 0).forGetter(Area::chargeTicks),
                ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost").forGetter(Area::costs)
        ).apply(instance, Area::new));
        public static final Codec<Area> CODEC = Codec.either(Codec.DOUBLE, FULL_CODEC).xmap(
                either -> either.map(radius -> new Area(ScaledValue.constant(radius), 0, Optional.empty()), area -> area),
                area -> area.chargeTicks() == 0 && area.costs().isEmpty() && area.radius().terms().isEmpty()
                        ? Either.left(area.radius().base()) : Either.right(area)
        );

        /**
         * Whether it reaches past the block at all (a radius that isn't a plain 0).
         */
        public boolean reaches() {
            return !radius.terms().isEmpty() || radius.base() > 0.0D;
        }
    }

    /**
     * What a block transforms to: the block, and what that costs in place of the use's cost if it costs something else. Written
     * as just the block's id, or as {@code {"block": ..., "cost": ...}}.
     */
    public record Transform(Block block, Optional<List<ActiveSkillCostDefinition>> costs) {
        private static final Codec<Transform> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Transform::block),
                ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost").forGetter(Transform::costs)
        ).apply(instance, Transform::new));
        public static final Codec<Transform> CODEC = Codec.either(BuiltInRegistries.BLOCK.byNameCodec(), FULL_CODEC).xmap(
                either -> either.map(block -> new Transform(block, Optional.empty()), transform -> transform),
                transform -> transform.costs().isEmpty() ? Either.left(transform.block()) : Either.right(transform)
        );
    }

    /**
     * What an element does to the drops of something killed with a coated weapon.
     *
     * @param costs       paid once a kill whose drops it changes; they're left alone if the player can't pay
     * @param toInventory drops go straight into the player's inventory; what doesn't fit falls as usual
     */
    public record Kill(List<ActiveSkillCostDefinition> costs, boolean toInventory) {
        public static final Codec<Kill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost", List.of()).forGetter(Kill::costs),
                Codec.BOOL.optionalFieldOf("to_inventory", false).forGetter(Kill::toInventory)
        ).apply(instance, Kill::new));

        public Kill {
            costs = List.copyOf(costs);
        }
    }
}
