package net.zic.ascension.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.zic.ascension.AscensionCraft;

public class AscLangProvider extends LanguageProvider {
    public AscLangProvider(PackOutput output) {
        super(output, AscensionCraft.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        add("creativetab.ascension.artifact_items", "Ascension Artifacts");



        add("item.ascension.tablet_of_destruction_human", "Tablet of Destruction - Human");
        add("item.ascension.tablet_of_destruction_earth", "Tablet of Destruction - Earth");
        add("item.ascension.tablet_of_destruction_heaven", "Tablet of Destruction - Heaven");

        add("item.ascension.tablet_of_destruction_human.cooldown", "§cTablet is on cooldown.");
        add("item.ascension.tablet_of_destruction_earth.cooldown", "§cTablet is on cooldown.");
        add("item.ascension.tablet_of_destruction_heaven.cooldown", "§cTablet is on cooldown.");

        add("item.ascension.tablet_of_destruction_earth.hint", "§Press [V] to cycle block drop mode.");
        add("item.ascension.tablet_of_destruction_heaven.link_info", "Shift + Right-click a container to link it.");
        add("item.ascension.tablet_of_destruction_heaven.linked_to", "Linked: %s @ %d, %d, %d");

        add("item.ascension.tablet.link_invalid", "§cThat block cannot be linked.");
        add("item.ascension.tablet.link_success", "§aLinked to %s @ %d, %d, %d");
        add("item.ascension.tablet.unlink_success", "§eContainer unlinked.");

        add("ascension.tablet.cooldown", "§cTablet is on cooldown.");
        add("ascension.tablet.drop_mode", "Drop Mode: ");
        add("ascension.tablet.drop_mode.off", "Off");
        add("ascension.tablet.drop_mode.on", "On");
        add("ascension.tablet.cycle_mode_info", "Press [V] to cycle drop mode.");

        add("key.categories.ascension", "Ascension");
        add("key.ascension.cycle_mode", "Cycle Modes");
    }
}
