package net.zic.ascension.api.ascension.core.path.realm;

import net.minecraft.resources.Identifier;

/*
    TODO:
        consider the situation where someone had a realms limit broken and cultivated say 200 minor realms into it.
        now say they lost the source of their limit break. what should happen now? should they lose all their progress and continue progressing as normal.
        or should they be able to progress, while still maintaining the 200+ minor realms?
        from a lore perspective both make sense,
        from a balance perspective letting them keep it is only kinda problematic? cus yeah against people at the same realm
        they are broken. but the people that started cultivating at the same time as them are probably at a much higher realm already
 */
public interface CompositeRealm {
    CompositeRealmDefinition definition();
    int getCurrentRealm();

}
