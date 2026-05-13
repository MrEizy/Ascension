package net.thejadeproject.ascension.common.items.techniques;

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
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.toast.ShowAscensionToast;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.helpers.TechniqueManualRegistry;

import java.util.ArrayList;
import java.util.List;

import static net.thejadeproject.ascension.clients.toast.AscensionToastInterface.DEFAULT_BACKGROUND;

public class TechniquePageItem extends Item {
    public TechniquePageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pageStack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(pageStack);
        }

        ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);

        if (!(offhandStack.getItem() instanceof TechniqueBinderItem)) {
            return InteractionResultHolder.pass(pageStack);
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.success(pageStack);
        }

        String pageTechnique = pageStack.get(ModDataComponents.TECHNIQUE_ID.get());
        Integer pageIndex = pageStack.get(ModDataComponents.PAGE_INDEX.get());

        if (pageTechnique == null || pageIndex == null) {
            return InteractionResultHolder.fail(pageStack);
        }

        ResourceLocation techniqueId = ResourceLocation.parse(pageTechnique);
        var manualData = TechniqueManualRegistry.get(techniqueId);

        if (manualData.isEmpty()) {
            sendToast(player, Component.translatable("ascension.toast.invalid_page"), Component.translatable("ascension.toast.no_manual_data"), pageStack);
            return InteractionResultHolder.fail(pageStack);
        }

        String bookTechnique = offhandStack.get(ModDataComponents.TECHNIQUE_ID.get());
        List<Integer> collectedIndices = offhandStack.getOrDefault(ModDataComponents.COLLECTED_PAGE_INDICES.get(), new ArrayList<>());

        if (bookTechnique == null) {
            offhandStack.set(ModDataComponents.TECHNIQUE_ID.get(), pageTechnique);
            List<Integer> newIndices = new ArrayList<>();
            newIndices.add(pageIndex);
            offhandStack.set(ModDataComponents.COLLECTED_PAGE_INDICES.get(), newIndices);
        } else if (!bookTechnique.equals(pageTechnique)) {
            sendToast(player, Component.translatable("ascension.toast.mismatch"), Component.translatable("ascension.toast.different_technique"), pageStack);
            return InteractionResultHolder.fail(pageStack);
        } else if (collectedIndices.contains(pageIndex)) {
            sendToast(player, Component.translatable("ascension.toast.duplicate_page"), Component.translatable("ascension.toast.already_collected"), pageStack);
            return InteractionResultHolder.fail(pageStack);
        } else {
            List<Integer> updatedIndices = new ArrayList<>(collectedIndices);
            updatedIndices.add(pageIndex);
            offhandStack.set(ModDataComponents.COLLECTED_PAGE_INDICES.get(), updatedIndices);
        }

        List<Integer> newIndices = offhandStack.getOrDefault(ModDataComponents.COLLECTED_PAGE_INDICES.get(), new ArrayList<>());
        int required = manualData.get().requiredPages();

        String chapterKey = manualData.get().chapterTranslationKeys().get(pageIndex);
        ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(techniqueId);
        String techniqueName = technique != null ? technique.getDisplayTitle().getString() : techniqueId.toString();

        if (newIndices.size() >= required) {
            ItemStack manual = TechniqueTransferItem.createWithTechnique(pageTechnique);
            player.setItemInHand(InteractionHand.OFF_HAND, manual);
            sendToast(player, Component.literal(techniqueName), Component.translatable("ascension.toast.manual_complete"), manual);
        } else {
            sendToast(player, Component.literal(techniqueName), Component.translatable("ascension.toast.page_added", Component.translatable(chapterKey), newIndices.size(), required), pageStack);
        }

        if (!player.getAbilities().instabuild) {
            pageStack.shrink(1);
        }

        return InteractionResultHolder.success(pageStack);
    }

    private void sendToast(Player player, Component title, Component subtitle, ItemStack icon) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack toastIcon = icon.copy();
            toastIcon.setCount(1);
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new ShowAscensionToast(title.getString(), subtitle.getString(), toastIcon, DEFAULT_BACKGROUND)
            );
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        String techniqueId = stack.get(ModDataComponents.TECHNIQUE_ID.get());

        if (techniqueId != null) {
            ResourceLocation id = ResourceLocation.parse(techniqueId);
            ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(id);
            if (technique != null) {
                return Component.translatable("ascension.item.technique_page", technique.getDisplayTitle());
            }
        }
        return Component.translatable("ascension.item.technique_page.blank");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String techniqueIdStr = stack.get(ModDataComponents.TECHNIQUE_ID.get());
        Integer pageIndex = stack.get(ModDataComponents.PAGE_INDEX.get());

        if (techniqueIdStr != null && pageIndex != null) {
            ResourceLocation id = ResourceLocation.parse(techniqueIdStr);
            var manualData = TechniqueManualRegistry.get(id);

            if (manualData.isPresent()) {
                String chapterKey = manualData.get().chapterTranslationKeys().get(pageIndex);
                tooltipComponents.add(Component.translatable(chapterKey).withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static ItemStack createWithTechnique(String techniqueId, int pageIndex) {
        ItemStack stack = new ItemStack(ModItems.TECHNIQUE_PAGE.get());
        stack.set(ModDataComponents.TECHNIQUE_ID.get(), techniqueId);
        stack.set(ModDataComponents.PAGE_INDEX.get(), pageIndex);
        return stack;
    }
}