package net.zic.ascension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.network.ByteBufHelpers;

public record TogglePassiveSkillPacket(Identifier skill, boolean enabled) implements CustomPacketPayload {
    public static final Type<TogglePassiveSkillPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "toggle_passive_skill")
    );
    public static final StreamCodec<FriendlyByteBuf, TogglePassiveSkillPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TogglePassiveSkillPacket decode(FriendlyByteBuf buf) {
            return new TogglePassiveSkillPacket(ByteBufHelpers.decodeIdentifier(buf), buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, TogglePassiveSkillPacket packet) {
            ByteBufHelpers.encodeIdentifier(packet.skill(), buf);
            buf.writeBoolean(packet.enabled());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TogglePassiveSkillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if (holder == null) {
                return;
            }
            OriginSource source = holder.getData().getSource();
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, packet.skill(), player.registryAccess());
            SkillData data = AscensionOriginSourceHelper.getSkillData(source, packet.skill());
            if (!(skill instanceof ToggleableSkill toggleable) || data == null || toggleable.isEnabled(data) == packet.enabled()) {
                return;
            }
            if (packet.enabled() && !toggleable.canEnable(player, source, data)) {
                return;
            }
            if (packet.enabled()) {
                toggleable.setEnabled(data, true);
                toggleable.onEnabled(source, data);
                toggleable.applyEnabledToEntity(player, data);
            } else {
                toggleable.onDisabled(source, data);
                toggleable.removeEnabledFromEntity(player, data);
                toggleable.setEnabled(data, false);
            }
            AscensionOriginSourceHelper.markSkillDirty(source, packet.skill());
        });
    }
}
