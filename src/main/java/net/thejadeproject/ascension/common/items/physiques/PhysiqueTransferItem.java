package net.thejadeproject.ascension.common.items.physiques;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.events.ElementalPhysiqueHandler;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.toast.ShowAscensionToast;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.ElementalBodyPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.ElementalPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.List;

import static net.thejadeproject.ascension.clients.toast.AscensionToastInterface.DEFAULT_BACKGROUND;

public class PhysiqueTransferItem extends Item {

    public PhysiqueTransferItem(Properties properties) {
        super(properties);
    }

    @Override
    public @javax.annotation.Nonnull InteractionResultHolder<ItemStack> use(@javax.annotation.Nonnull Level level, @javax.annotation.Nonnull Player player, @javax.annotation.Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.pass(stack);

        String targetPhysiqueId = stack.get(ModDataComponents.PHYSIQUE_ID.get());
        Integer purity = stack.get(ModDataComponents.PURITY.get());

        if (targetPhysiqueId == null || targetPhysiqueId.isEmpty() || purity == null) {
            return InteractionResultHolder.fail(stack);
        }

        if (purity < 100) {
            player.sendSystemMessage(
                    Component.literal("Cannot transfer physique: ")
                            .append(Component.literal(purity + "%").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" purity. Need "))
                            .append(Component.literal("100%").withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(" purity."))
            );
            return InteractionResultHolder.fail(stack);
        }


        Component physiqueName = getPhysiqueDisplayName(targetPhysiqueId);

        ItemStack toastIcon = stack.copy();
        toastIcon.setCount(1);

        ResourceLocation targetId = ResourceLocation.bySeparator(targetPhysiqueId, ':');
        IPhysique targetPhysique = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(targetId);

        if (player.isShiftKeyDown()) {
            // Shift+right-click: replace physique entirely
            player.getData(ModAttachments.ENTITY_DATA).setPhysique(targetId);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

//            player.sendSystemMessage(
//                    Component.literal("Physique replaced with ")
//                            .append(getPhysiqueDisplayName(targetPhysiqueId))
//                            .append(Component.literal("!"))
//            );

            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(
                        serverPlayer,
                        new ShowAscensionToast(
                                physiqueName.getString(),
                                "Physique Transferred",
                                toastIcon,
                                DEFAULT_BACKGROUND
                        )
                );
            }

            return InteractionResultHolder.success(stack);
        }

        // Right-click: attempt elemental fusion
        if (!(targetPhysique instanceof ElementalBodyPhysique targetElemental)) {
            return InteractionResultHolder.pass(stack);
        }

        IPhysiqueData currentData = player.getData(ModAttachments.ENTITY_DATA).getActiveFormData().getPhysiqueData();
        if (!(currentData instanceof ElementalPhysiqueData elementalData)) {
            player.sendSystemMessage(Component.literal("Your current physique cannot be fused.").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stack);
        }

        if (!ElementalPhysiqueHandler.canMerge(elementalData, targetElemental.getElement())) {
            player.sendSystemMessage(Component.literal("This essence is not compatible with your current physique.").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stack);
        }

        // setPhysique fires PhysiqueChangeEvent.Pre — our handler cancels it and upgrades the data
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.getData(ModAttachments.ENTITY_DATA).setPhysique(targetId);
        player.sendSystemMessage(
                Component.literal("Physique fused with ")
                        .append(getPhysiqueDisplayName(targetPhysiqueId))
                        .append(Component.literal("!"))
        );
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @javax.annotation.Nonnull Component getName(@javax.annotation.Nonnull ItemStack stack) {
        String targetPhysiqueId = stack.get(ModDataComponents.PHYSIQUE_ID.get());
        Integer purity = stack.get(ModDataComponents.PURITY.get());

        if (targetPhysiqueId != null && !targetPhysiqueId.isEmpty()) {
            ResourceLocation physiqueResource = ResourceLocation.parse(targetPhysiqueId);
            IPhysique physique = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(physiqueResource);

            if (physique != null) {
                Component baseName = Component.empty().append(physique.getDisplayTitle()).append(" Essence");

                if (purity != null && purity < 100) {
                    return baseName.copy()
                            .append(Component.literal(" [").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(purity + "%").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
                }

                return baseName;
            }
        }

        return Component.literal("Essence");
    }

    @Override
    public void appendHoverText(@javax.annotation.Nonnull ItemStack stack, @javax.annotation.Nonnull TooltipContext context, @javax.annotation.Nonnull List<Component> tooltip, @javax.annotation.Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        String targetPhysiqueId = stack.get(ModDataComponents.PHYSIQUE_ID.get());
        Integer purity = stack.get(ModDataComponents.PURITY.get());

        if (targetPhysiqueId != null && !targetPhysiqueId.isEmpty()) {
            tooltip.add(Component.literal("Target: ").withStyle(ChatFormatting.GRAY)
                    .append(getPhysiqueDisplayName(targetPhysiqueId)));

            if (purity != null) {
                ChatFormatting purityColor = ChatFormatting.GRAY;

                if (purity >= 100) purityColor = ChatFormatting.GREEN;
                else if (purity >= 75) purityColor = ChatFormatting.YELLOW;
                else if (purity >= 50) purityColor = ChatFormatting.GOLD;
                else if (purity >= 25) purityColor = ChatFormatting.RED;

                tooltip.add(Component.literal("Purity: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(purity + "%").withStyle(purityColor)));
            }
        } else {
            tooltip.add(Component.literal("No physique set").withStyle(ChatFormatting.GRAY));
        }

        if (targetPhysiqueId != null && !targetPhysiqueId.isEmpty()) {
            IPhysique targetPhysique = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(ResourceLocation.parse(targetPhysiqueId));
            if (targetPhysique instanceof ElementalBodyPhysique) {
                tooltip.add(Component.translatable("ascension.physique_essence.tooltip.fuse")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        tooltip.add(Component.translatable("ascension.physique_essence.tooltip.replace")
                .withStyle(ChatFormatting.GRAY));

        if (purity != null && purity >= 100) {
            tooltip.add(Component.literal("Ready to use!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        } else if (purity != null) {
            tooltip.add(Component.literal("Requires 100% purity to use")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private Component getPhysiqueDisplayName(String targetPhysiqueId) {
        ResourceLocation physiqueResource = ResourceLocation.parse(targetPhysiqueId);
        IPhysique physique = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(physiqueResource);

        if (physique != null) {
            return Component.empty().append(physique.getDisplayTitle()).withStyle(ChatFormatting.GOLD);
        }

        return Component.literal("Unknown Physique").withStyle(ChatFormatting.GRAY);
    }

    public static ItemStack createWithPhysique(String physiqueId, int purity) {
        ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
        stack.set(ModDataComponents.PHYSIQUE_ID.get(), physiqueId);
        stack.set(ModDataComponents.PURITY.get(), Math.min(purity, 100));
        return stack;
    }

    public static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        String id1 = stack1.get(ModDataComponents.PHYSIQUE_ID.get());
        String id2 = stack2.get(ModDataComponents.PHYSIQUE_ID.get());

        if (id1 == null || id2 == null) return false;
        return id1.equals(id2);
    }

    public static int getCombinedPurity(ItemStack stack1, ItemStack stack2) {
        Integer purity1 = stack1.get(ModDataComponents.PURITY.get());
        Integer purity2 = stack2.get(ModDataComponents.PURITY.get());

        if (purity1 == null) purity1 = 0;
        if (purity2 == null) purity2 = 0;

        return Math.min(purity1 + purity2, 100);
    }
}