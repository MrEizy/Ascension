package net.zic.ascension.api.ascension.core.alchemy;

public final class AlchemyProcessHooks {
    private AlchemyProcessHooks() {
    }

    public static ProcessSettings resolve(AlchemyContext context) {
        return ProcessSettings.DEFAULT;
    }

    public record ProcessSettings(double heat, double refinementControl, double safeEnergyDelta, double explosionEnergyDelta
    ) {
        public static final ProcessSettings DEFAULT = new ProcessSettings(1.0D, 2.5D, 3.0D, 8.0D);

        public ProcessSettings {
            heat = finiteNonNegative(heat, 1.0D);
            refinementControl = finiteNonNegative(refinementControl, 1.0D);
            safeEnergyDelta = finiteNonNegative(safeEnergyDelta, 3.0D);
            explosionEnergyDelta = Math.max(safeEnergyDelta, finiteNonNegative(explosionEnergyDelta, 8.0D));
        }

        private static double finiteNonNegative(double value, double fallback) {
            return Double.isFinite(value) ? Math.max(0.0D, value) : fallback;
        }
    }
}
