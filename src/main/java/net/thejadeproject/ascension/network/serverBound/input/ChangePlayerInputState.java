package net.thejadeproject.ascension.network.serverBound.input;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.data_attachments.attachments.PlayerInputStates;


public record ChangePlayerInputState(String input,int modifier,boolean isDown)implements CustomPacketPayload {
    public static final Type<ChangePlayerInputState> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "change_player_input_sate"));
    public static final StreamCodec<ByteBuf, ChangePlayerInputState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ChangePlayerInputState::input,
            ByteBufCodecs.VAR_INT,
            ChangePlayerInputState::modifier,
            ByteBufCodecs.BOOL,
            ChangePlayerInputState::isDown,
            ChangePlayerInputState::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(ChangePlayerInputState payload, IPayloadContext context) {

        PlayerInputStates states = context.player().getData(ModAttachments.INPUT_STATES);
        if(payload.isDown()) states.inputDown(payload.input(),payload.modifier());
        else states.inputReleased(payload.input());
    }
}
