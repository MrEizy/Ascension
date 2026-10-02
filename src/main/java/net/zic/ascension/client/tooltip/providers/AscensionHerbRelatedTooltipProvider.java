package net.zic.ascension.client.tooltip.providers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
import net.zic.ascension.common.blocks.crops.herbs.PodHerbBlock;
import net.zic.ascension.common.blocks.crops.mushrooms.LingzhiMushroomBlock;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.item.herbs.HerbItem;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviders;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipSubject;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;

import java.util.Optional;

public final class AscensionHerbRelatedTooltipProvider implements ZenithTooltipProviders.ContextualProvider {
    public static final Identifier ID = AscensionCraft.prefix("herb_related_tooltips");

    private static final Identifier DEFAULT_TEMPLATE = AscensionCraft.prefix("default_herb_related");
    private static final Identifier DEFAULT_THEME = AscensionCraft.prefix("herb");

    private AscensionHerbRelatedTooltipProvider() {}

    public static void register() {
        ZenithTooltipProviders.registerContextual(ID, new AscensionHerbRelatedTooltipProvider());
    }

    @Override
    public Optional<ZenithTooltipProviders.Result> create(ZenithTooltipContext context) {
        return resolve(context.stack()).flatMap(info -> {
            Component targetName = new ItemStack(info.targetItem()).getHoverName();
            Component description = Component.translatable(info.kind().descriptionKey(), targetName);

            Optional<ZenithTooltipDocument> document = ZenithTooltipRepository.fromTemplate(
                    DEFAULT_TEMPLATE,
                    DEFAULT_THEME
            );

            return document.map(value -> ZenithTooltipProviders.Result.withSubject(
                    value,
                    context,
                    info.definition().id(),
                    info.definition(),
                    ZenithTooltipSubject.of(context.stack().getHoverName(), description)
            ));
        });
    }

    public static Optional<RelatedHerbInfo> resolve(ItemStack stack) {
        if (stack.getItem() instanceof HerbItem.Seed seed) {
            Block crop = seed.cropBlock();
            if (crop instanceof HerbCropBlock herbCrop) {
                return Optional.of(new RelatedHerbInfo(
                        herbCrop.definition(),
                        herbCrop.harvestItem(),
                        RelatedHerbKind.SEED
                ));
            }
        }

        if (stack.getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof LingzhiMushroomBlock mushroom) {
                return Optional.of(new RelatedHerbInfo(
                        mushroom.definition(),
                        mushroom.harvestItem(),
                        RelatedHerbKind.PLANT
                ));
            }

            if (blockItem.getBlock() instanceof PodHerbBlock podHerb) {
                return Optional.of(new RelatedHerbInfo(
                        podHerb.definition(),
                        podHerb.harvestItem(),
                        RelatedHerbKind.PLANT
                ));
            }
        }

        return Optional.empty();
    }

    public enum RelatedHerbKind {
        SEED(
                "ascension.herb.related.type.seed",
                "ascension.herb.related.seed.description",
                "ascension.herb.related.seed.target"
        ),
        PLANT(
                "ascension.herb.related.type.plant",
                "ascension.herb.related.plant.description",
                "ascension.herb.related.plant.target"
        );

        private final String typeKey;
        private final String descriptionKey;
        private final String targetLabelKey;

        RelatedHerbKind(String typeKey, String descriptionKey, String targetLabelKey) {
            this.typeKey = typeKey;
            this.descriptionKey = descriptionKey;
            this.targetLabelKey = targetLabelKey;
        }

        public String typeKey() {
            return typeKey;
        }

        public String descriptionKey() {
            return descriptionKey;
        }

        public String targetLabelKey() {
            return targetLabelKey;
        }
    }

    public record RelatedHerbInfo(
            HerbDefinition definition,
            Item targetItem,
            RelatedHerbKind kind
    ) {}
}
