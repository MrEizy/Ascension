package net.zic.ascension.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

// Optional JEI integration: only loaded by JEI, so the mod runs fine without it
@JeiPlugin
public class AscensionJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    // Transfer items share one item each; the registry id component tells them apart
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerFromDataComponentTypes(ModItems.TECHNIQUE_MANUAL.get(), AscensionComponents.REGISTRY_ID_HOLDER.get());
        registration.registerFromDataComponentTypes(ModItems.BLOODLINE_ESSENCE.get(), AscensionComponents.REGISTRY_ID_HOLDER.get());
        registration.registerFromDataComponentTypes(ModItems.PHYSIQUE_ESSENCE.get(), AscensionComponents.REGISTRY_ID_HOLDER.get());
    }
}
