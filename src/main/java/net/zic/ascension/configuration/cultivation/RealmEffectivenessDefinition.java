package net.zic.ascension.configuration.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.cultivation.AuthorityCurve;

import java.util.List;

public final class RealmEffectivenessDefinition {
    private RealmEffectivenessDefinition() {}

    private record Raw(List<Double> multipliers, double mortalAuthority, double terminalGrowth, double existentialProtection) {
        private static final Codec<Raw> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.listOf().fieldOf("multipliers").forGetter(Raw::multipliers),
                Codec.DOUBLE.optionalFieldOf("mortal_authority", 0.1D).forGetter(Raw::mortalAuthority),
                Codec.DOUBLE.optionalFieldOf("terminal_growth", 2.0D).forGetter(Raw::terminalGrowth),
                Codec.DOUBLE.optionalFieldOf("existential_protection", AuthorityCurve.DEFAULT_EXISTENTIAL_PROTECTION)
                        .forGetter(Raw::existentialProtection)
        ).apply(instance, Raw::new));
    }

    public static final Codec<AuthorityCurve> CODEC = Raw.CODEC.comapFlatMap(raw -> {
        try {
            return DataResult.success(new AuthorityCurve(raw.multipliers(), raw.mortalAuthority(), raw.terminalGrowth(), raw.existentialProtection()));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }, curve -> new Raw(curve.multipliers(), curve.mortalAuthority(), curve.terminalGrowth(), curve.existentialProtection()));
}
