package net.zic.ascension.api.ascension.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class HexColorCodec {

    public static final Codec<Integer> CODEC = Codec.STRING.comapFlatMap(
            HexColorCodec::parse,
            HexColorCodec::toHexString
    );

    private HexColorCodec() {
    }

    public static DataResult<Integer> parse(String value) {
        if (value == null || value.isBlank()) {
            return DataResult.error(() -> "Color cannot be blank");
        }
        String hex = value.startsWith("#") ? value.substring(1) : value;
        if (hex.length() != 6 && hex.length() != 8) {
            return DataResult.error(() -> "Color must be a 6 or 8 digit hex code, got: " + value);
        }
        try {
            long parsed = Long.parseLong(hex, 16);
            return DataResult.success((int) (parsed & 0xFFFFFFFFL));
        } catch (NumberFormatException exception) {
            return DataResult.error(() -> "Invalid hex color: " + value);
        }
    }

    public static String toHexString(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    public static float[] toFloats(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
    }
}