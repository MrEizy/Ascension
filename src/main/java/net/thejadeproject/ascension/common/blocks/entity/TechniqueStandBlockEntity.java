package net.thejadeproject.ascension.common.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.techniques.TechniqueBinderItem;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.toast.ShowAscensionToast;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.helpers.TechniqueManualRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static net.thejadeproject.ascension.clients.toast.AscensionToastInterface.DEFAULT_BACKGROUND;

public class TechniqueStandBlockEntity extends BlockEntity {

    private ItemStack storedItem = ItemStack.EMPTY;

    public TechniqueStandBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TECHNIQUE_STAND_BE.get(), pos, blockState);
    }

    public ItemStack getStoredItem() {
        return storedItem;
    }

    public void setStoredItem(ItemStack stack) {
        this.storedItem = stack;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public boolean tryInsertPage(ItemStack pageStack, Player player) {
        if (!(storedItem.getItem() instanceof TechniqueBinderItem)) {
            return false;
        }

        String pageTechnique = pageStack.get(ModDataComponents.TECHNIQUE_ID.get());
        Integer pageIndex = pageStack.get(ModDataComponents.PAGE_INDEX.get());

        if (pageTechnique == null || pageIndex == null) {
            return false;
        }

        ResourceLocation techniqueId = ResourceLocation.parse(pageTechnique);
        var manualData = TechniqueManualRegistry.get(techniqueId);

        if (manualData.isEmpty()) {
            sendToast(player, Component.translatable("ascension.toast.invalid_page"), Component.translatable("ascension.toast.no_manual_data"), pageStack);
            return false;
        }

        List<String> chapters = manualData.get().chapterTranslationKeys();
        if (pageIndex < 0 || pageIndex >= chapters.size()) {
            return false;
        }

        String bookTechnique = storedItem.get(ModDataComponents.TECHNIQUE_ID.get());
        List<Integer> collectedIndices = storedItem.getOrDefault(ModDataComponents.COLLECTED_PAGE_INDICES.get(), new ArrayList<>());

        if (bookTechnique == null) {
            storedItem.set(ModDataComponents.TECHNIQUE_ID.get(), pageTechnique);
            List<Integer> newList = new ArrayList<>();
            newList.add(pageIndex);
            storedItem.set(ModDataComponents.COLLECTED_PAGE_INDICES.get(), newList);
        } else if (!bookTechnique.equals(pageTechnique)) {
            sendToast(player, Component.translatable("ascension.toast.mismatch"), Component.translatable("ascension.toast.different_technique"), pageStack);
            return false;
        } else if (collectedIndices.contains(pageIndex)) {
            sendToast(player, Component.translatable("ascension.toast.duplicate_page"), Component.translatable("ascension.toast.already_collected"), pageStack);
            return false;
        } else {
            List<Integer> updated = new ArrayList<>(collectedIndices);
            updated.add(pageIndex);
            storedItem.set(ModDataComponents.COLLECTED_PAGE_INDICES.get(), updated);
        }

        List<Integer> newIndices = storedItem.getOrDefault(ModDataComponents.COLLECTED_PAGE_INDICES.get(), new ArrayList<>());
        int required = manualData.get().requiredPages();
        String chapterKey = chapters.get(pageIndex);
        ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(techniqueId);
        String techniqueName = technique != null ? technique.getDisplayTitle().getString() : techniqueId.toString();

        if (newIndices.size() >= required) {
            sendToast(player, Component.literal(techniqueName), Component.translatable("ascension.toast.manual_complete"), storedItem);
        } else {
            sendToast(player, Component.literal(techniqueName), Component.translatable("ascension.toast.page_added", Component.translatable(chapterKey), newIndices.size(), required), pageStack);
        }

        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public ItemStack retrieveItem() {
        if (storedItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = storedItem.copy();

        if (storedItem.getItem() instanceof TechniqueBinderItem) {
            String id = storedItem.get(ModDataComponents.TECHNIQUE_ID.get());
            if (id != null) {
                ResourceLocation rId = ResourceLocation.parse(id);
                var manualData = TechniqueManualRegistry.get(rId);
                if (manualData.isPresent()) {
                    List<Integer> collected = storedItem.getOrDefault(ModDataComponents.COLLECTED_PAGE_INDICES.get(), new ArrayList<>());
                    int required = manualData.get().requiredPages();
                    if (collected.size() >= required) {
                        result = net.thejadeproject.ascension.common.items.techniques.TechniqueTransferItem.createWithTechnique(id);
                    }
                }
            }
        }

        storedItem = ItemStack.EMPTY;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
        return result;
    }


    private void sendToast(Player player, Component title, Component subtitle, ItemStack icon) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack toastIcon = icon.copy();
            toastIcon.setCount(1);
            PacketDistributor.sendToPlayer(serverPlayer,
                    new ShowAscensionToast(title.getString(), subtitle.getString(), toastIcon, DEFAULT_BACKGROUND));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!storedItem.isEmpty()) {
            tag.put("StoredItem", storedItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("StoredItem")) {
            storedItem = ItemStack.parse(registries, tag.getCompound("StoredItem")).orElse(ItemStack.EMPTY);
        } else {
            storedItem = ItemStack.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 8);
        }
    }
}