package net.zic.ascension.refactor_packages.techniques.stability;

import java.util.function.Function;

public class GenericCalculatedStabilityHandler implements IStabilityHandler{

    private final Function<Double,Double> stabilityFunction;
    private final double startX;
    private final double endX;
    private final double startY;
    private final double endY;
    private final double xPerTick;
    private final double maxCultivationTicks;
    public GenericCalculatedStabilityHandler(Function<Double,Double> stabilityFunction, double startX, double endX, double totalCultivationTicksNeeded){
        this.stabilityFunction = stabilityFunction;
        this.startX = startX;
        this.endX = endX;
        this.xPerTick = (endX-startX)/totalCultivationTicksNeeded;
        this.maxCultivationTicks = totalCultivationTicksNeeded;
        this.startY = stabilityFunction.apply(startX);
        this.endY = stabilityFunction.apply(endX);
    }


    @Override
    public double getStability(double cultivationTicks) {
        return (stabilityFunction.apply(startX+xPerTick*cultivationTicks)-startY)/(endY-startY);
    }

    @Override
    public double getMaxCultivationTicks() {
        return maxCultivationTicks;
    }
}
