package net.thejadeproject.ascension.common.items.data_components.herb_pouch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.refactor_packages.util.ByteBufUtil;
import net.thejadeproject.ascension.util.ModTags;

import java.util.ArrayList;
import java.util.List;

public record HerbPouchComponent(int capacity, List<ItemStack> herbs) {
    public HerbPouchComponent(int capacity) {
        this(capacity, List.of());
    }

    public HerbPouchComponent {
        List<ItemStack> copied = new ArrayList<>();
        for (ItemStack stack : herbs) {
            if (!stack.isEmpty()) copied.add(stack.copy());
        }
        herbs = List.copyOf(copied);
    }

    public int getTotalCount() {
        int total = 0;
        for (ItemStack stack : herbs) total += stack.getCount();
        return total;
    }

    public InsertResult insert(ItemStack input) {
        if (input.isEmpty() || !input.is(ModTags.Items.HERBS)) {
            return new InsertResult(this, input);
        }

        int space = capacity - getTotalCount();
        if (space <= 0) return new InsertResult(this, input);

        int toInsert = Math.min(space, input.getCount());

        List<ItemStack> newHerbs = new ArrayList<>();
        for (ItemStack stack : herbs) newHerbs.add(stack.copy());

        ItemStack inserted = input.copy();
        inserted.setCount(toInsert);

        for (ItemStack stored : newHerbs) {
            if (ItemStack.isSameItemSameComponents(stored, inserted)) {
                int room = stored.getMaxStackSize() - stored.getCount();
                int move = Math.min(room, inserted.getCount());

                if (move > 0) {
                    stored.grow(move);
                    inserted.shrink(move);
                }

                if (inserted.isEmpty()) break;
            }
        }

        if (!inserted.isEmpty()) newHerbs.add(inserted);

        ItemStack remainder = input.copy();
        remainder.shrink(toInsert);

        return new InsertResult(new HerbPouchComponent(capacity, newHerbs), remainder);
    }

    public ExtractResult extractOneByItem(ItemStack clickedSummaryStack) {
        if (clickedSummaryStack.isEmpty()) {
            return new ExtractResult(this, ItemStack.EMPTY);
        }

        List<ItemStack> newHerbs = new ArrayList<>();
        ItemStack extracted = ItemStack.EMPTY;
        boolean taken = false;

        for (ItemStack stored : herbs) {
            ItemStack copy = stored.copy();

            if (!taken && ItemStack.isSameItem(copy, clickedSummaryStack)) {
                extracted = copy.copy();
                extracted.setCount(1);
                copy.shrink(1);
                taken = true;
            }

            if (!copy.isEmpty()) newHerbs.add(copy);
        }

        return new ExtractResult(new HerbPouchComponent(capacity, newHerbs), extracted);
    }

    public ExtractManyResult extractAllByItem(ItemStack clickedSummaryStack) {
        if (clickedSummaryStack.isEmpty()) {
            return new ExtractManyResult(this, List.of());
        }

        List<ItemStack> newHerbs = new ArrayList<>();
        List<ItemStack> extracted = new ArrayList<>();

        for (ItemStack stored : herbs) {
            ItemStack copy = stored.copy();

            if (ItemStack.isSameItem(copy, clickedSummaryStack)) {
                extracted.add(copy);
            } else {
                newHerbs.add(copy);
            }
        }

        return new ExtractManyResult(new HerbPouchComponent(capacity, newHerbs), extracted);
    }

    public List<ItemStack> getSummaryStacks() {
        List<ItemStack> summaries = new ArrayList<>();

        for (ItemStack herb : herbs) {
            boolean found = false;

            for (ItemStack summary : summaries) {
                if (ItemStack.isSameItem(summary, herb)) {
                    summary.grow(herb.getCount());
                    found = true;
                    break;
                }
            }

            if (!found) {
                ItemStack summary = herb.copy();
                summary.remove(ModDataComponents.HERB_AGE_TICKS.get());
                summary.remove(ModDataComponents.HERB_QUALITY.get());
                summaries.add(summary);
            }
        }

        return summaries;
    }

    public static final Codec<HerbPouchComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("capacity").forGetter(HerbPouchComponent::capacity),
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("herbs").forGetter(HerbPouchComponent::herbs)
            ).apply(instance, HerbPouchComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, HerbPouchComponent> STREAM_CODEC =
            StreamCodec.of(HerbPouchComponent::encode, HerbPouchComponent::decode);

    public static void encode(RegistryFriendlyByteBuf buf, HerbPouchComponent component) {
        buf.writeInt(component.capacity());
        ByteBufUtil.ITEM_STACK_LIST.encode(buf, component.herbs());
    }

    public static HerbPouchComponent decode(RegistryFriendlyByteBuf buf) {
        int capacity = buf.readInt();
        List<ItemStack> herbs = ByteBufUtil.ITEM_STACK_LIST.decode(buf);
        return new HerbPouchComponent(capacity, herbs);
    }

    public record InsertResult(HerbPouchComponent component, ItemStack remainder) {}
    public record ExtractResult(HerbPouchComponent component, ItemStack extracted) {}
    public record ExtractManyResult(HerbPouchComponent component, List<ItemStack> extracted) {}
}