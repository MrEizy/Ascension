package net.zic.ascension.common.enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.zic.ascension.AscensionCraft;

public final class AscEnchantments {
    public static final ResourceKey<Enchantment> RICOCHET =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("ricochet"));
    public static final ResourceKey<Enchantment> RAZOR_EDGE =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("razor_edge"));
    public static final ResourceKey<Enchantment> SWIFT_RETURN =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("swift_return"));
    public static final ResourceKey<Enchantment> PHASING_RECALL =
            ResourceKey.create(Registries.ENCHANTMENT, AscensionCraft.prefix("phasing_recall"));

    private AscEnchantments() {}
}