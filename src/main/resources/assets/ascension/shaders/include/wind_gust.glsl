#version 330

// Wind gusts shared by the wind rings (qi_coating.fsh) and the wind skin look (qi_coating_skin.fsh), so both agree on when and
// where a gust is: every WIND_GUST_PERIOD seconds a gust climbs the arm from the hand to WIND_GUST_TOP (and on past it until its
// tail clears) in WIND_GUST_RISE seconds, a band WIND_GUST_BAND of the limb tall that is strongest at its front.
// WIND_GUST_SKIP of the periods stay calm.
const float WIND_GUST_PERIOD = 2.4;
const float WIND_GUST_RISE = 1.6;
const float WIND_GUST_BAND = 0.6;
const float WIND_GUST_TOP = 0.62;
const float WIND_GUST_SKIP = 0.0;

float windCycleHash(float cycle) {
    return fract(sin(cycle * 91.345) * 43758.5453);
}

// How strong the gust is at this height up the limb (0 at the hand) right now: 0 when calm, in steps of a third up to 1.
float windGust(float height, float seconds) {
    float cycle = floor(seconds / WIND_GUST_PERIOD);
    if (windCycleHash(cycle) < WIND_GUST_SKIP) {
        return 0.0;
    }
    float front = (seconds - cycle * WIND_GUST_PERIOD) / WIND_GUST_RISE * (WIND_GUST_TOP + WIND_GUST_BAND);
    float behind = front - height;
    if (behind < 0.0 || behind > WIND_GUST_BAND) {
        return 0.0;
    }
    return ceil((1.0 - behind / WIND_GUST_BAND) * 3.0) / 3.0;
}
