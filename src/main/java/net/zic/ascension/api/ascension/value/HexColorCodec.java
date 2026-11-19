package net.zic.ascension.api.ascension.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public final class HexColorCodec {
    public static final Codec<Integer> RGB_CODEC = Codec.STRING.comapFlatMap(
            value -> parse(value).map(color -> color & 0xFFFFFF),
            HexColorCodec::toHexString
    );
    public static final Codec<Integer> ARGB_CODEC = Codec.STRING.comapFlatMap(
            HexColorCodec::parseArgb,
            HexColorCodec::toArgbHexString
    );
    public static final Codec<Integer> CODEC = RGB_CODEC;

    private HexColorCodec() {
    }

    public static DataResult<Integer> parse(String value) {
        if (value == null || value.isBlank()) {
            return DataResult.error(() -> "Color cannot be blank");
        }
        String hex = value.startsWith("#") ? value.substring(1) : value;
        if (hex.length() != 6 && hex.length() != 8) {
            return DataResult.error(() -> "Color must be #RRGGBB or #AARRGGBB, got: " + value);
        }
        for (int index = 0; index < hex.length(); index++) {
            if (Character.digit(hex.charAt(index), 16) < 0) {
                return DataResult.error(() -> "Invalid hex color: " + value);
            }
        }
        try {
            return DataResult.success((int) Long.parseLong(hex, 16));
        } catch (NumberFormatException exception) {
            return DataResult.error(() -> "Invalid hex color: " + value);
        }
    }

    public static DataResult<Integer> parseArgb(String value) {
        return parse(value).map(color -> {
            String hex = value.startsWith("#") ? value.substring(1) : value;
            return hex.length() == 6 ? color | 0xFF000000 : color;
        });
    }

    public static String toHexString(int color) {
        return String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }

    public static String toArgbHexString(int color) {
        return String.format(Locale.ROOT, "#%08X", color);
    }

    public static float[] toFloats(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
    }
}
