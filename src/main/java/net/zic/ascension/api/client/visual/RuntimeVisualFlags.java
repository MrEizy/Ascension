package net.zic.ascension.api.client.visual;

public final class RuntimeVisualFlags {
    public static final int OWNER_RELATIVE = 1;
    public static final int ROTATE_WITH_OWNER = 1 << 1;

    private RuntimeVisualFlags() {
    }

    public static boolean has(int flags, int flag) {
        return (flags & flag) != 0;
    }
}
