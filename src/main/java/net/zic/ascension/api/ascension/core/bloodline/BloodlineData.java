package net.zic.ascension.api.ascension.core.bloodline;

/**
 * per-origin state of a bloodline. Saving and syncing is handled by the owning {@link Bloodline}
 * through its {@link Bloodline#serializerHandler()} and {@link Bloodline#syncHandler()}.
 * Every bloodline tracks a purity, purity driven progression relies on it.
 */
public interface BloodlineData {
    int getPurity();
    void setPurity(int newPurity);
}
