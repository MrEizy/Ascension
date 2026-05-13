package net.thejadeproject.ascension.common.items.techniques;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.toast.ShowAscensionToast;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;

import static net.thejadeproject.ascension.clients.toast.AscensionToastInterface.DEFAULT_BACKGROUND;


public class TechniqueTransferItem extends Item {

    public TechniqueTransferItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        //System.out.println("trying to learn technique");

        if (!level.isClientSide() && player.isShiftKeyDown()) {
            String techniqueId = stack.get(ModDataComponents.TECHNIQUE_ID.get());

            if (techniqueId == null) {
                //System.out.println("no held technique");
                return InteractionResultHolder.fail(stack);
            }

            Component techniqueName = getName(stack);

            ItemStack toastIcon = stack.copy();
            toastIcon.setCount(1);

            if (player.getData(ModAttachments.ENTITY_DATA).setTechnique(ResourceLocation.parse(techniqueId))) {
                //System.out.println("technique learned");

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

//                player.sendSystemMessage(
//                        Component.literal("Successfully Learned:  ")
//                                .append(techniqueName)
//                                .append(Component.literal("!"))
//                );

                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(
                            serverPlayer,
                            new ShowAscensionToast(
                                    techniqueName.getString(),
                                    "Technique Learned",
                                    toastIcon,
                                    DEFAULT_BACKGROUND
                            )
                    );
                }
            } else {
                //System.out.println("failed to learn");

                player.sendSystemMessage(
                        Component.literal("unable to learn ")
                                .append(techniqueName)
                                .append(Component.literal("!"))
                );
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        String targetTechnique = stack.get(ModDataComponents.TECHNIQUE_ID.get());

        if (targetTechnique != null) {
            ResourceLocation techniqueId = ResourceLocation.parse(targetTechnique);
            ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(techniqueId);

            if (technique != null) return technique.getDisplayTitle();
        }

        return Component.literal("Manual");
    }

    public static ItemStack createWithTechnique(String techniqueId) {
        ItemStack stack = new ItemStack(ModItems.TECHNIQUE_MANUAL.get());
        stack.set(ModDataComponents.TECHNIQUE_ID.get(), techniqueId);
        return stack;
    }
}