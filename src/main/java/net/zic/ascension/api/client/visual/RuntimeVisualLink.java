package net.zic.ascension.api.client.visual;

public record RuntimeVisualLink(int from, int to) {
    public RuntimeVisualLink {
        from = Math.max(0, from);
        to = Math.max(0, to);
    }
}
