package net.thejadeproject.ascension.common.items;

import net.favouriteless.modopedia.client.BookOpenHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AncestorJournalItem extends Item {

    private final ResourceLocation bookId;

    public AncestorJournalItem(ResourceLocation bookId) {
        super(new Properties().stacksTo(1));
        this.bookId = bookId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            BookOpenHandler.tryOpenBook(bookId);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

}
