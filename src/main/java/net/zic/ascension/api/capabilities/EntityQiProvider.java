package net.zic.ascension.api.capabilities;

public interface EntityQiProvider {


    double getQi();
    double getMaxQi();
    void regenQi(double amount);
    boolean reduceQi(double amount);
    void setQi(double value);

}
