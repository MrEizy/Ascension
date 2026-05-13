package net.thejadeproject.ascension.common.items.pills;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.entity.ModEntities;
import net.thejadeproject.ascension.entity.custom.PillProjectile;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.refactor_packages.alchemy.IPillEffect;
import net.thejadeproject.ascension.refactor_packages.util.PillEffectUtil;

import java.util.List;

public class PillItem extends Item {
    public final int cooldown;
    public final boolean throwable;

    public PillItem(Properties properties, int cooldown, boolean throwable) {
        super(properties);
        this.cooldown = cooldown;
        this.throwable = throwable;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (throwable) {
            // Shift + right-click → throw
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    PillProjectile proj = new PillProjectile(
                            ModEntities.PILL_PROJECTILE.get(), level, player, stack);
                    proj.setItem(stack);
                    proj.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 1.5f, 1f);
                    level.addFreshEntity(proj);
                }
                if (!player.getAbilities().instabuild) stack.shrink(1);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            // Right-click → eat
            else {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            }
        } else {
            // Non-throwable: right-click always eats
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity e) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (level.isClientSide() || !(livingEntity instanceof Player player)) return result;

        double purityScale      = PillEffectUtil.getPurityScale(stack);
        double realmMultiplier  = PillEffectUtil.getRealmMultiplier(stack);

        List<IPillEffect> effects = PillEffectUtil.getPillEffects(stack);
        boolean shouldConsume=true;
        boolean shouldGoOnCooldown = false;
        for(IPillEffect effect : effects){
            if(!effect.tryConsume(livingEntity,stack,purityScale,realmMultiplier)) shouldConsume = false;
            if(effect.shouldGoOnCooldown()) shouldGoOnCooldown = true;
        }

        if (!player.getAbilities().instabuild) stack.shrink(1);
        if(shouldConsume && !player.getAbilities().instabuild)  stack.shrink(1);
        if(shouldGoOnCooldown) player.getCooldowns().addCooldown(this, cooldown);
        return result;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx,
                                List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, list, flag);

        Integer majorRealm = stack.get(ModDataComponents.PILL_MAJOR_REALM.get());
        Integer purity     = stack.get(ModDataComponents.PILL_PURITY.get());
        List<IPillEffect> pillEffects = PillEffectUtil.getPillEffects(stack);
        String  bonus      = stack.get(ModDataComponents.PILL_BONUS_EFFECT.get());

        if (majorRealm == null && purity == null) {
            list.add(Component.literal("Unrefined")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        }

        // ── Realm line ────────────────────────────────────────────
        if (majorRealm != null && purity != null) {
            list.add(Component.literal("Pill Realm: ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(
                                    majorRealm + " — " + PillRealmData.getMajorRealmName(majorRealm))
                            .withStyle(ChatFormatting.WHITE)));
            String gradeName = PillRealmData.getPurityGrade(purity);
            ChatFormatting gradeColor = PillRealmData.getPurityGradeColor(purity);
            list.add(Component.literal("Purity: ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(gradeName).withStyle(gradeColor)));
        }


        list.add(Component.literal("✦ Effects: ")
                .withStyle(ChatFormatting.AQUA));
        for(IPillEffect effect : pillEffects){
            list.add(effect.getName());
        }

    }

}
