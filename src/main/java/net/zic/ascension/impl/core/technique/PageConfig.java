package net.zic.ascension.impl.core.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * The "pages" object of a lost_technique.
 *
 * @param amount    how many pages the technique has, 1-64. One technique page item exists for each
 * @param translate the base translation key of the page names. Page 0 is named by translate + ".0", page 1 by translate + ".1"
 *                  and so on. Without it pages are just called "Page 1", "Page 2"...
 */
public record PageConfig(int amount, Optional<String> translate) {
    public static final Codec<PageConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 64).fieldOf("amount").forGetter(PageConfig::amount),
            Codec.STRING.optionalFieldOf("translate").forGetter(PageConfig::translate)
    ).apply(instance, PageConfig::new));

    public PageConfig {
        translate = translate == null ? Optional.empty() : translate;
    }

    /** the name of a page, index starts at 0 */
    public Component pageName(int index) {
        return translate
                .map(key -> Component.translatable(key + "." + index))
                .orElseGet(() -> Component.translatable("ascension.technique.page_number", index + 1));
    }
}