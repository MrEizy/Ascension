package net.zic.ascension.common;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.util.ModTags;
import net.zic.zenithlib.creative.api.CreativeTabSection;
import net.zic.zenithlib.creative.api.CreativeTabSections;

import java.util.function.Supplier;

public final class AscensionCreativeSections {
    private static boolean registered;

    private AscensionCreativeSections() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }

        // --- Item Tab ---

        CreativeTabSections.register(
                ModCreativeModeTabs.ASCENSION_ITEMS_TAB.getKey(),

                section("artifacts",
                        "creative_section.ascension.artifacts",
                        ModItems.TABLET_OF_DESTRUCTION_EARTH)
                        .matchingTag(ModTags.Items.ARTIFACTS)
                        .order(10)
                        .build(),

                section("materials",
                        "creative_section.ascension.materials",
                        ModItems.JADE)
                        .matchingTag(ModTags.Items.MATERIALS)
                        .order(20)
                        .build()

                /*
                 * If you want to make another one, just do this:
                 * <p>
                 * section("path", // This turns into /items
                 *      "creative_section.ascension.path_from_above_here",
                 *      ModItems.ICON_ITEM)
                 *      .matchingTag(ModTags.Items.TAG)
                 *              .order(X0) // next increment down
                 *              .build(), // You need to add a comma to the one above as well.
                 * </p>
                 * I made a few tags in the thing already, so just use those,
                 * or feel free to add more if needed
                 */

        );

        registered = true;
    }

    private static CreativeTabSection.Builder section(String path, String translationKey, Supplier<? extends Item> icon) {
        return CreativeTabSection.builder(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "items/" + path))
                .title(Component.translatable(translationKey))
                .icon(() -> icon.get().getDefaultInstance());
    }
}