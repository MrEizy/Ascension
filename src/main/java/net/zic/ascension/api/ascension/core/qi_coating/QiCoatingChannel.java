package net.zic.ascension.api.ascension.core.qi_coating;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.AscensionCraft;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The two places a Qi Coating can go, each with its own skill (Qi Coating, External Qi Coating) and state group, so one of each
 * can be on at the same time.
 */
@NullMarked
public enum QiCoatingChannel implements StringRepresentable {
    // On the player: hands, or the whole body for the layer looks.
    BODY("body", AscensionCraft.prefix("qi_coating")),
    // On the held weapon (Sword Qi).
    EXTERNAL("external", AscensionCraft.prefix("external_qi_coating"));

    public static final Codec<QiCoatingChannel> CODEC = StringRepresentable.fromEnum(QiCoatingChannel::values);
    public static final StreamCodec<ByteBuf, QiCoatingChannel> STREAM_CODEC = ByteBufCodecs.idMapper(
            index -> values()[index], QiCoatingChannel::ordinal
    );

    private final String name;
    private final Identifier id;

    QiCoatingChannel(String name, Identifier id) {
        this.name = name;
        this.id = id;
    }

    /**
     * The channel's id, as its state group is named.
     */
    public Identifier id() {
        return id;
    }

    /**
     * The channel a coating goes on: weapon coatings are external, everything else is on the body.
     */
    public static QiCoatingChannel of(QiCoating coating) {
        return coating.swordQi() ? EXTERNAL : BODY;
    }

    public boolean matches(@Nullable QiCoating coating) {
        return coating != null && coating.swordQi() == (this == EXTERNAL);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
