package net.thejadeproject.ascension.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.recipe.LowHumanPillCauldronRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI recipe category for the Mortal-tier Pill Cauldron.
 *
 * Layout:
 *   Three input slots at the pedestal positions (left / back / right).
 *   Two output slots below (success / fail) with success/failure % tooltips.
 *   Temperature range and craft time drawn as text beneath the outputs.
 *
 * Texture: textures/jei/cauldron.png  (100 × 90 px recommended)
 */
public class PillCauldronRecipeCategory implements IRecipeCategory<LowHumanPillCauldronRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pill_cauldron_low_human");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/jei/cauldron.png");

    public static final RecipeType<LowHumanPillCauldronRecipe> CAULDRON_RECIPE_TYPE =
            new RecipeType<>(UID, LowHumanPillCauldronRecipe.class);

    // ── Background dimensions ─────────────────────────────────────
    private static final int BG_WIDTH  = 120;
    private static final int BG_HEIGHT = 95;

    // ── Slot positions (match pedestal layout: left / back / right) ─
    // "back" pedestal = top slot in JEI (center)
    private static final int SLOT_LEFT_X  = 10,  SLOT_LEFT_Y  = 12;
    private static final int SLOT_BACK_X  = 37, SLOT_BACK_Y  = 1;
    private static final int SLOT_RIGHT_X = 64, SLOT_RIGHT_Y = 12;

    // ── Output slot positions ─────────────────────────────────────
    private static final int SUCCESS_X = 19, SUCCESS_Y = 51;
    private static final int FAIL_X    = 55, FAIL_Y    = 51;

    private final IDrawable background;
    private final IDrawable icon;

    public PillCauldronRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, BG_WIDTH, BG_HEIGHT);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.PILL_CAULDRON_HUMAN_LOW.get()));
    }

    @Override
    public RecipeType<LowHumanPillCauldronRecipe> getRecipeType() {
        return CAULDRON_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ascension.pill_cauldron_low_human");
    }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          LowHumanPillCauldronRecipe recipe,
                          IFocusGroup focuses) {

        List<SizedIngredient> ings = recipe.getSizedIngredients();

        // ── Input slots (positional: left / back / right) ─────────
        addInputSlot(builder, ings, 0, SLOT_LEFT_X,  SLOT_LEFT_Y);
        addInputSlot(builder, ings, 1, SLOT_BACK_X,  SLOT_BACK_Y);
        addInputSlot(builder, ings, 2, SLOT_RIGHT_X, SLOT_RIGHT_Y);

        // ── Success output ────────────────────────────────────────
        builder.addSlot(RecipeIngredientRole.OUTPUT, SUCCESS_X, SUCCESS_Y)
                .addItemStack(recipe.getSuccess());

        // ── Fail output ───────────────────────────────────────────
        builder.addSlot(RecipeIngredientRole.OUTPUT, FAIL_X, FAIL_Y)
                .addItemStack(recipe.getFail())
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.literal(
                                "Failure: " + (int)((1 - recipe.getChance()) * 100) + "%")));
    }

    private void addInputSlot(IRecipeLayoutBuilder builder,
                              List<SizedIngredient> ings, int index, int x, int y) {
        if (index >= ings.size()) return;
        SizedIngredient ing = ings.get(index);
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack s : ing.ingredient().getItems()) {
            ItemStack copy = s.copy();
            copy.setCount(ing.count());
            stacks.add(copy);
        }
        builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(stacks);
    }

    @Override
    public void draw(LowHumanPillCauldronRecipe recipe, IRecipeSlotsView slots,
                     GuiGraphics g, double mouseX, double mouseY) {
        background.draw(g);

        Minecraft mc    = Minecraft.getInstance();
        int lineY       = BG_HEIGHT - 25;
        int lineSpacing = 10;

        // Temperature range line
        String tempLine = "Temp: " + recipe.getMinTemp() + "° – " + recipe.getMaxTemp() + "°";
        int tempX = (BG_WIDTH - mc.font.width(tempLine)) / 2;
        g.drawString(mc.font, tempLine, tempX, lineY, 0x444444, false);

        // Craft time line
        String timeLine = "Time: " + recipe.getRecipeTime() + "s in range";
        int timeX = (BG_WIDTH - mc.font.width(timeLine)) / 2;
        g.drawString(mc.font, timeLine, timeX, lineY + lineSpacing, 0x444444, false);
    }
}