package net.zic.ascension.impl.core.technique;

import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;

/**
 * A simple technique whose manual has been torn into pages. Collect every page in a technique binder to rebuild the manual.
 * Takes everything else from the SimpleTechnique it is built on.
 */
public class LostTechnique extends SimpleTechnique implements Paged {
    private final PageConfig pages;

    public LostTechnique(SimpleTechnique base, PageConfig pages) {
        super(base.getName(null), base.getDescription(null), base.getOnAddedActions(), base.getRealmStatGrowth(),
                base.getStatsPerRealm(), base.getCultivationPaths(), base.getMaxRealm(), base.getMaxMinorRealm(),
                base.itemTooltip(), base.requirements());
        this.pages = pages;
    }

    @Override
    public PageConfig pages() {
        return pages;
    }

    @Override
    public TechniqueType getType() {
        return AscensionTechniqueTypes.LOST_TECHNIQUE_TYPE.get();
    }
}