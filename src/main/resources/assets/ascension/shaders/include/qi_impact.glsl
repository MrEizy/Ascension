#version 330

// How long a qi coating's hit reaction runs: the coatings' impact age (QiCoatingLayer.impactData) is a share of this. Must match
// QiCoatingLayer.IMPACT_MILLIS.
const float IMPACT_SECONDS = 2.0;
