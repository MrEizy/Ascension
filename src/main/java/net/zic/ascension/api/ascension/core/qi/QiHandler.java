package net.zic.ascension.api.ascension.core.qi;

/**
 * marks something that can hold qi (will use long)
 *
 * (For now im not using Neoforge Energy handler on purpose, because i dont want it to be compatible by default)
 * TODO consider reading more about how Neoforge uses Transactions, and potentially use them
 * TODO update all qi handlers to use long (might make custom class for "bigger" longs, but those would only need to be used
 *  internally in qi storage scenarios(like if i mike a 256bit type, nothing is going to need to regenerate that much every tick
 */
public interface QiHandler {



    /**
     * will attempt to consume an amount of qi, but only if it can consume the full amount
     * @param amount the amount we want to consume
     * @return true-> was consumed, false-> was not consumed
     */
    boolean tryConsume(int amount);


    /**
     * will regenerate an amount of qi
     * @param amount the amount of qi we want to regenerate
     * @return the amount of qi regenerated
     */
    int insertQi(int amount);

    /**
     * will reduce an amount of qi, but not more than is present
     * @param amount the amount we wish to extract
     * @return the amount of qi extracted
     */
    int extractQi(int amount);



    long getQi();
    long getCapacity();
}
