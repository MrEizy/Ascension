package net.zic.ascension.api.ascension.core.qi;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
//TODO add enforcement to ensure all take positive numbers
public class SimpleQiHandler implements QiHandler{

    final long CAPACITY;
    long qi;
    public SimpleQiHandler(long capacity) {
        this(capacity,0);
    }
    public SimpleQiHandler(long capacity,long qi){
        CAPACITY = capacity;
        this.qi = qi;
    }

    @Override
    public boolean tryConsume(int amount) {
        long newQi = Math.min( qi -amount,CAPACITY);
        if(newQi < 0) return false;
        qi = newQi;
        return true;
    }

    @Override
    public int insertQi(int amount) {
        int change = CAPACITY-qi < amount ? Math.toIntExact(CAPACITY - qi) : amount;
        qi = qi + change;
        return change;
    }

    @Override
    public int extractQi(int amount) {
        int change = qi-amount < 0 ? Math.toIntExact(qi) : amount;
        qi = qi -change;
        return change;
    }

    @Override
    public long getQi() {
        return qi;
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }
}
