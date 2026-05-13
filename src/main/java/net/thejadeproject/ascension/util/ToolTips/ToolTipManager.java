package net.thejadeproject.ascension.util.ToolTips;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.thejadeproject.ascension.common.items.ModItems;


public class ToolTipManager {
      public static void registerAllTooltips() {
        // Example tooltip registrations
        /*ToolTipHandler.registerTooltip(
                ModItems.EXAMPLE_ITEM.get(),
                "This is a simple tooltip",
                "Second line of tooltip"
        );*/

        // Tooltip with custom formatting


        //Medicinal Pills
        ToolTipHandler.registerTooltip(ModItems.PILL_RESIDUE.get(), Component.translatable("ascension.tooltip.waste").withStyle(ChatFormatting.DARK_RED), Component.literal("☆☆☆☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.NEUTRALITY_PILL.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.FASTING_PILL_T1.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★☆☆☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.FASTING_PILL_T2.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★★☆☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.FASTING_PILL_T3.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));

        ToolTipHandler.registerTooltip(ModItems.ANTIDOTE_PILL_QDP.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));

        ToolTipHandler.registerTooltip(ModItems.MARROW_CLEANSE_PILL.get(), Component.translatable("ascension.tooltip.evolutionary").withStyle(ChatFormatting.RED), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.CRIMSON_LOTUS_BONE_PILL.get(), Component.translatable("ascension.tooltip.evolutionary").withStyle(ChatFormatting.RED), Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));


        ToolTipHandler.registerTooltip(ModItems.CLEANSING_PILL_T1.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.CLEANSING_PILL_T2.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★★★★☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.CLEANSING_PILL_T3.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.CLEANSING_PILL_T4.get(), Component.translatable("ascension.tooltip.medicinal").withStyle(ChatFormatting.RED), Component.literal("★☆☆☆☆").withStyle(ChatFormatting.DARK_RED));

        //Herbs
        ToolTipHandler.registerTooltip(ModItems.GOLDEN_SUN_LEAF.get(), Component.literal("★★☆☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.JADE_BAMBOO_OF_SERENITY.get(), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.WHITE_JADE_ORCHID.get(), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.IRONWOOD_SPROUT.get(), Component.literal("★☆☆☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.HUNDRED_YEAR_GINSENG.get(), Component.literal("★★☆☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get(), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));

        //Fires
        ToolTipHandler.registerTooltip(ModItems.CRIMSON_LOTUS_FLAME.get(), Component.literal("★☆☆☆☆").withStyle(ChatFormatting.RED));
        ToolTipHandler.registerTooltip(ModItems.SOUL_FLAME.get(), Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.FLAME.get(), Component.literal("★★★★☆").withStyle(ChatFormatting.YELLOW));


        //Artifacts


       //Talisman consumable
        ToolTipHandler.registerTooltip(ModItems.SPATIAL_RUPTURE_TALISMAN_T1.get(), Component.translatable("ascension.tooltip.srtt1").withStyle(ChatFormatting.GOLD), Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.SPATIAL_RUPTURE_TALISMAN_T2.get(), Component.translatable("ascension.tooltip.srtt2").withStyle(ChatFormatting.GOLD), Component.literal("★☆☆☆☆").withStyle(ChatFormatting.DARK_RED));
        ToolTipHandler.registerTooltip(ModItems.SPATIAL_RUPTURE_TALISMAN_T3.get(), Component.translatable("ascension.tooltip.srtt3").withStyle(ChatFormatting.GOLD), Component.literal("★★☆☆☆").withStyle(ChatFormatting.DARK_RED));
        ToolTipHandler.registerTooltip(ModItems.SOULSTEAD_RETURN_TALISMAN.get(), Component.translatable("ascension.tooltip.srt").withStyle(ChatFormatting.GOLD), Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.WORLD_AXIS_TALISMAN.get(), Component.translatable("ascension.tooltip.wat").withStyle(ChatFormatting.GOLD), Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.VOID_MARKING_TALISMAN.get(), Component.translatable("ascension.tooltip.vmt").withStyle(ChatFormatting.GOLD), Component.literal("★☆☆☆☆").withStyle(ChatFormatting.RED));

        //Talisman Permanent
       ToolTipHandler.registerPermanentTooltip(ModItems.SPATIAL_RUPTURE_TALISMAN_T1.get(),
               Component.translatable("ascension.tooltip.srtt1.permanent").withStyle(ChatFormatting.LIGHT_PURPLE),
               Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));

       ToolTipHandler.registerPermanentTooltip(ModItems.SPATIAL_RUPTURE_TALISMAN_T2.get(),
               Component.translatable("ascension.tooltip.srtt2.permanent").withStyle(ChatFormatting.LIGHT_PURPLE),
               Component.literal("★☆☆☆☆").withStyle(ChatFormatting.DARK_RED));

       ToolTipHandler.registerPermanentTooltip(ModItems.SPATIAL_RUPTURE_TALISMAN_T3.get(),
               Component.translatable("ascension.tooltip.srtt3.permanent").withStyle(ChatFormatting.LIGHT_PURPLE),
               Component.literal("★★☆☆☆").withStyle(ChatFormatting.DARK_RED));

       ToolTipHandler.registerPermanentTooltip(ModItems.SOULSTEAD_RETURN_TALISMAN.get(),
               Component.translatable("ascension.tooltip.srt.permanent").withStyle(ChatFormatting.LIGHT_PURPLE),
               Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));

       ToolTipHandler.registerPermanentTooltip(ModItems.WORLD_AXIS_TALISMAN.get(),
               Component.translatable("ascension.tooltip.wat.permanent").withStyle(ChatFormatting.LIGHT_PURPLE),
               Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));

       ToolTipHandler.registerPermanentTooltip(ModItems.VOID_MARKING_TALISMAN.get(),
               Component.translatable("ascension.tooltip.vmt.permanent").withStyle(ChatFormatting.LIGHT_PURPLE),
               Component.literal("★☆☆☆☆").withStyle(ChatFormatting.RED));





        ToolTipHandler.registerTooltip(ModItems.TABLET_OF_DESTRUCTION_HUMAN.get(), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.TABLET_OF_DESTRUCTION_EARTH.get(), Component.literal("★★★★☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get(), Component.literal("★★★★★").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.REPAIR_SLIP.get(), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.ENDER_POUCH.get(), Component.literal("★★☆☆☆").withStyle(ChatFormatting.YELLOW));
        ToolTipHandler.registerTooltip(ModItems.FIRE_GOURD.get(), Component.literal("★★★☆☆").withStyle(ChatFormatting.RED));


        //Crafting Ingredients
        ToolTipHandler.registerTooltip(ModItems.TALISMAN_PAPER.get(), Component.literal("★★★☆☆").withStyle(ChatFormatting.YELLOW));


        // Advanced example with conditional logic
        registerAdvancedTooltips();
    }

    private static void registerAdvancedTooltips() {
        // You can add more complex tooltip logic here
        //ToolTipHandler.registerTooltip(
                //ModItems.SPECIAL_ITEM.get(),
                //"Special Item Tooltip",
                //"Hold Shift for more info"
        //);
    }
}
