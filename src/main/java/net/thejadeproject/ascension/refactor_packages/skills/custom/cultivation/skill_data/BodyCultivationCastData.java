package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.skill_data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;

public class BodyCultivationCastData implements ICastData {
    private int consecutiveEligibleTicks = 0;

    public void incrementEligible() { consecutiveEligibleTicks++; }
    public void resetEligible() { consecutiveEligibleTicks = 0; }
    public int getConsecutiveEligibleTicks() { return consecutiveEligibleTicks; }

    @Override
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("eligible_ticks", consecutiveEligibleTicks);
        return tag;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        // consecutiveEligibleTicks is server-only; nothing to send to the client
    }
}
