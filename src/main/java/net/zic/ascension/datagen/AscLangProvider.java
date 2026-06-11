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

        add("key.ascension.open_introspection", "Open Introspection");

        add("gui.ascension.introspection.title", "Introspection");
        add("gui.ascension.introspection.main", "Main");
        add("gui.ascension.introspection.stats", "Stats");
        add("gui.ascension.introspection.skills", "Skills");
        add("gui.ascension.introspection.cultivation", "Cultivation");
        add("gui.ascension.introspection.physique", "Physique");
        add("gui.ascension.introspection.bloodlines", "Bloodlines");
        add("gui.ascension.introspection.bloodline_count", "Bloodlines (%s)");
        add("gui.ascension.introspection.bloodline_multiple", "%s +%s");
        add("gui.ascension.introspection.bloodline_purity", "Purity: %s%%");
        add("gui.ascension.introspection.technique", "Technique");
        add("gui.ascension.introspection.none", "None");
        add("gui.ascension.introspection.no_physique", "No physique");
        add("gui.ascension.introspection.no_bloodlines", "No bloodlines");
        add("gui.ascension.introspection.no_paths", "No cultivation paths");
        add("gui.ascension.introspection.no_paths_description", "This character has not acquired a cultivation path.");
        add("gui.ascension.introspection.no_skills", "No skills");
        add("gui.ascension.introspection.no_skills_description", "This character has not acquired any skills.");
        add("gui.ascension.introspection.no_stats", "No Ascension stats are currently available.");
        add("gui.ascension.introspection.select_identity", "Select the physique or bloodline panel above.");
        add("gui.ascension.introspection.data_unavailable", "Synchronized Ascension data is temporarily unavailable.");
        add("gui.ascension.introspection.missing_registry_entry", "The synchronized identifier is not present in the client registry.");
        add("gui.ascension.introspection.empty_slot", "Empty");
        add("gui.ascension.introspection.castable_skill", "Castable skill. Select it, then click a hotbar slot below to assign or remove it.");
        add("stat.ascension.vitality", "Vitality");
        add("stat.ascension.agility", "Agility");
        add("stat.ascension.strength", "Strength");
        add("stat.ascension.intelligence", "Intelligence");
    }
}
