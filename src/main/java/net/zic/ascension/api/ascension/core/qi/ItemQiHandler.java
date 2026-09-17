package net.zic.ascension.api.ascension.core.qi;

import net.minecraft.world.item.ItemStack;

public interface ItemQiHandler extends QiHandler{


    /**
     * will attempt to consume an amount of qi, but only if it can consume the full amount
     * @param amount the amount we want to consume
     * @return EMPTY-> not consumed or not stacked, STACK>0-> was consumed and split
     */
    ItemStack tryConsumeAndSplit(int amount);


    /**
     * will regenerate an amount of qi
     * @param amount the amount of qi we want to regenerate
     * @return EMPTY -> not inserted or not stacked , STACK >0 -> inserted and split
     */
    ItemStack insertQiAndSplit(int amount);

    /**
     * will reduce an amount of qi, but not more than is present
     * @param amount the amount we wish to extract
     * @return EMPTY -> not extracted or not stacked , STACK >0 -> extracted and split
     */
    ItemStack extractQiAndSplit(int amount);

}
