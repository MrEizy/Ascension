package net.thejadeproject.ascension.clients.jei;

import javax.annotation.Nonnull;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;

@JeiPlugin
public class AscensionJeiPlugin implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
                ModItems.TECHNIQUE_MANUAL.get(),
                new ISubtypeInterpreter<>() {
                    @Override
                    public Object getSubtypeData(@Nonnull ItemStack stack, @Nonnull mezz.jei.api.ingredients.subtypes.UidContext ctx) {
                        String id = stack.get(ModDataComponents.TECHNIQUE_ID.get());
                        return id != null ? id : "none";
                    }
                    @Override
                    public @Nonnull String getLegacyStringSubtypeInfo(@Nonnull ItemStack stack, @Nonnull mezz.jei.api.ingredients.subtypes.UidContext ctx) {
                        String id = stack.get(ModDataComponents.TECHNIQUE_ID.get());
                        return id != null ? id : "none";
                    }
                }
        );
        registration.registerSubtypeInterpreter(
                ModItems.PHYSIQUE_ESSENCE.get(),
                new ISubtypeInterpreter<>() {
                    @Override
                    public Object getSubtypeData(@Nonnull ItemStack stack, @Nonnull mezz.jei.api.ingredients.subtypes.UidContext ctx) {
                        String id = stack.get(ModDataComponents.PHYSIQUE_ID.get());
                        return id != null ? id : "none";
                    }
                    @Override
                    public @Nonnull String getLegacyStringSubtypeInfo(@Nonnull ItemStack stack, @Nonnull mezz.jei.api.ingredients.subtypes.UidContext ctx) {
                        String id = stack.get(ModDataComponents.PHYSIQUE_ID.get());
                        return id != null ? id : "none";
                    }
                }
        );
    }
}
