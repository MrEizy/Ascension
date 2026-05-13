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
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.ModTechniques;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data.ScholarlySoulTechniqueData;

public class ScholarlySoulChapterItem extends Item {

    private final ResourceLocation chapter;

    public ScholarlySoulChapterItem(Properties properties, ResourceLocation chapter) {
        super(properties);
        this.chapter = chapter;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        IEntityData entityData = serverPlayer.getData(ModAttachments.ENTITY_DATA);
        PathData pathData = entityData.getPathData(ModPaths.SOUL.getId());

        if (pathData == null) {
            serverPlayer.displayClientMessage(
                    Component.literal("Your soul has no path to receive this chapter."),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        ResourceLocation techniqueId = ModTechniques.SCHOLARLY_SOUL_TECHNIQUE.getId();
        ITechniqueData rawData = pathData.getTechniqueData(techniqueId);

        if (!(rawData instanceof ScholarlySoulTechniqueData scholarlyData)) {
            serverPlayer.displayClientMessage(
                    Component.literal("You must learn the Scholarly Soul Technique before understanding this chapter."),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        if (!scholarlyData.unlockChapter(chapter)) {
            serverPlayer.displayClientMessage(
                    Component.literal("You have already understood this chapter."),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        int maxRealm = scholarlyData.getMaxUnlockedMajorRealm();

        serverPlayer.displayClientMessage(
                Component.literal("A new chapter settles into your soul. You can now cultivate up to major realm " + maxRealm + "."),
                true
        );

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }

        pathData.sync(serverPlayer);

        return InteractionResultHolder.success(stack);
    }
}