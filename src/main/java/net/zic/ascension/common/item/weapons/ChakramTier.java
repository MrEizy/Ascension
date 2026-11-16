package net.zic.ascension.common.item.weapons;

public enum ChakramTier {

    COPPER("copper", 180,  3.0F, 12, 1.6F, 1.10F, 3.0F, false),
    IRON("iron", 250,  4.0F, 14, 1.8F, 1.20F, 4.0F, false),
    GOLDEN("golden", 100,  3.0F, 22, 2.0F, 1.30F, 5.0F, false),
    DIAMOND("diamond", 800,  5.0F, 10, 2.2F, 1.40F, 6.0F, false),
    NETHERITE("netherite", 1500,  6.0F, 15, 2.5F, 1.50F, 8.0F, true),
    BLACK_IRON("black_iron", 638,  5.5F, 18, 2.1F, 1.35F, 6.5F, false),
    FROST_SILVER("frost_silver", 518,  6.0F, 22, 2.4F, 1.55F, 7.0F, false),
    SPIRITUAL_STONE("spiritual_stone", 2758, 7.5F, 28, 2.8F, 1.70F, 9.5F, false);

    private final String name;
    public final int durability;
    public final float meleeDamage;
    public final int enchantability;
    public final float throwSpeed;
    public final float ricochetSpeed;
    public final float damage;
    public final boolean fireResistant;

    ChakramTier(String name, int durability, float meleeDamage, int enchantability,
                float throwSpeed, float ricochetSpeed, float damage, boolean fireResistant) {
        this.name = name;
        this.durability = durability;
        this.meleeDamage = meleeDamage;
        this.enchantability = enchantability;
        this.throwSpeed = throwSpeed;
        this.ricochetSpeed = ricochetSpeed;
        this.damage = damage;
        this.fireResistant = fireResistant;
    }

    public String itemName() { return name + "_chakram"; }
    public boolean fireResistant() { return fireResistant; }
}