package net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.stats;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.attributes.AscensionAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.stats.Stat;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainer;

public record SyncStat(String form, ValueContainer container)implements CustomPacketPayload {

    public static final Type<SyncStat> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sync_stat"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStat> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncStat::form,
            StreamCodec.of(ValueContainer::encode,ValueContainer::decode),
            SyncStat::container,
            SyncStat::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(SyncStat payload, IPayloadContext context) {
        context.enqueueWork(()->{
            //System.out.println("stat was synced");
            IEntityData entityData = context.player().getData(ModAttachments.ENTITY_DATA);
            Stat stat = AscensionRegistries.Stats.STATS_REGISTRY.get(payload.container.getIdentifier());
            entityData.getEntityFormData(ResourceLocation.parse(payload.form)).getStatSheet().setContainer(stat,payload.container);
            entityData.getAscensionAttributeHolder().updateAttributes(entityData);
            //System.out.println("==============================================");
            entityData.getEntityFormData(ResourceLocation.parse(payload.form)).getStatSheet().log();
            entityData.getAscensionAttributeHolder().log();
        });
    }
}
