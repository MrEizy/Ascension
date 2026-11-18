package net.zic.ascension.common.enchantments;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.util.ModTags;

public final class AscEnchantments {
    public static final ResourceKey<Enchantment> RICOCHET =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("ricochet"));
    public static final ResourceKey<Enchantment> RAZOR_EDGE =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("razor_edge"));
    public static final ResourceKey<Enchantment> SWIFT_RETURN =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("swift_return"));
    public static final ResourceKey<Enchantment> PHASING_RECALL =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("phasing_recall"));
    public static final ResourceKey<Enchantment> PENETRATING_STRIKE =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("penetrating_strike"));
    public static final ResourceKey<Enchantment> IMPACT =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("impact"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderSet<Item> chakrams = context.lookup(Registries.ITEM).getOrThrow(ModTags.Items.CHAKRAMS);

        register(context, RICOCHET, Enchantment.enchantment(Enchantment.definition(chakrams, 2, 3,
                Enchantment.dynamicCost(10, 9), Enchantment.dynamicCost(45, 9), 4,
                EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND)));
        register(context, RAZOR_EDGE, Enchantment.enchantment(Enchantment.definition(chakrams, 10, 5,
                Enchantment.dynamicCost(1, 11), Enchantment.dynamicCost(21, 11), 1,
                EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND)));
        register(context, SWIFT_RETURN, Enchantment.enchantment(Enchantment.definition(chakrams, 5, 3,
                Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(50, 8), 2,
                EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND)));
        register(context, PHASING_RECALL, Enchantment.enchantment(Enchantment.definition(chakrams, 2, 1,
                Enchantment.constantCost(25), Enchantment.constantCost(50), 4,
                EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND)));

        HolderSet<Item> fists = context.lookup(Registries.ITEM).getOrThrow(ModTags.Items.WEAPON_FISTS);
        register(context, PENETRATING_STRIKE, Enchantment.enchantment(Enchantment.definition(fists, 2, 3,
                        Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4,
                        EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND))
                .withEffect(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, new AddValue(LevelBasedValue.perLevel(-0.15F))));
        register(context, IMPACT, Enchantment.enchantment(Enchantment.definition(fists, 10, 5,
                        Enchantment.dynamicCost(1, 11), Enchantment.dynamicCost(21, 11), 1,
                        EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND))
                .withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(1.0F, 0.5F))));
    }

    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key,
                                 Enchantment.Builder builder) {
        context.register(key, builder.build(key.identifier()));
    }

    private AscEnchantments() {}
}
