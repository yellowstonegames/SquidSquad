/*
 * Copyright (c) 2023-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.yellowstonegames.core.annotations.Beta;

/**
 * Foam noise but using Simplex noise instead of Value noise. An {@link INoise} implementation.
 * This has a somewhat-similar appearance to higher-frequency Foam noise. It can be a little faster in dimensions 4
 * and up, though it can be hard to notice.
 */
@Beta
public class FoamplexNoise implements INoise {

    /**
     * Use the same seed for any noise that should be continuous (smooth) across calls to nearby points.
     */
    protected long seed;

    public static final FoamplexNoise instance = new FoamplexNoise();

    public FoamplexNoise() {
        this(1234567890L);
    }

    public FoamplexNoise(long seed)
    {
        this.seed = seed;
    }

    // 2D SECTION

    /**
     * Gets 2D noise using {@link #getSeed()}.
     * @param x x coordinate
     * @param y y coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    /**
     * Gets 2D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param seed can be any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        final float p0 = 0.7500f * (x);
        final float p1 = 0.7500f * (x * -0.5f + y * 0.8660254037844386f);
        final float p2 = 0.7500f * (x * -0.5f + y * -0.8660254037844387f);

        float xin = p1;
        float yin = p2;
        final float a = SimplexNoise.noise(xin, yin, seed);
        xin = p2;
        yin = p0;
        final float b = SimplexNoise.noise(xin + a * 0.25f, yin, seed + 0x9A827999FCEF3243L);
        xin = p0;
        yin = p1;
        final float c = SimplexNoise.noise(xin + b * 0.25f, yin, seed + 0x3504F333F9DE6486L);
        final float result = (a + b + c) * 0.3333333333333333f;
        return result / (0.35f * Math.abs(result) + (1f - 0.35f));
// return result / (0.35f * Math.abs(result) + (1f - 0.35f));// gain function for [-1, 1] domain and range
    }

    // 3D SECTION

    /**
     * Gets 3D noise using {@link #getSeed()}.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y, final float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    /**
     * Gets 3D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param seed can be any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        final float p0 = 0.7500f * (x);
        final float p1 = 0.7500f * (x * -0.3333333333333333f + y * 0.9428090415820634f);
        final float p2 = 0.7500f * (x * -0.3333333333333333f + y * -0.4714045207910317f + z * 0.816496580927726f);
        final float p3 = 0.7500f * (x * -0.3333333333333333f + y * -0.4714045207910317f + z * -0.816496580927726f);

        float xin = p1;
        float yin = p2;
        float zin = p3;
        final float a = SimplexNoise.noise(xin, yin, zin, seed);
        xin = p0;
        yin = p2;
        zin = p3;
        final float b = SimplexNoise.noise(xin + a * 0.25f, yin, zin, seed + 0x9A827999FCEF3243L);
        xin = p0;
        yin = p1;
        zin = p3;
        final float c = SimplexNoise.noise(xin + b * 0.25f, yin, zin, seed + 0x3504F333F9DE6486L);
        xin = p0;
        yin = p1;
        zin = p2;
        final float d = SimplexNoise.noise(xin + c * 0.25f, yin, zin, seed + 0xCF876CCDF6CD96C9L);
        final float result = (a + b + c + d) * 0.25f;
        return result / (0.45f * Math.abs(result) + (1f - 0.45f));
// return result / (0.45f * Math.abs(result) + (1f - 0.45f));// gain function for [-1, 1] domain and range
    }

    // 4D SECTION

    /**
     * Gets 4D foam noise using {@link #getSeed()}.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y, final float z, final float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    /**
     * Gets 4D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param seed can be any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        final float p0 = 0.7500f * (x);
        final float p1 = 0.7500f * (x * -0.25f + y *  0.9682458365518543f);
        final float p2 = 0.7500f * (x * -0.25f + y * -0.3227486121839514f + z * 0.9128709291752769f);
        final float p3 = 0.7500f * (x * -0.25f + y * -0.3227486121839514f + z * -0.45643546458763834f + w * 0.7905694150420949f);
        final float p4 = 0.7500f * (x * -0.25f + y * -0.3227486121839514f + z * -0.45643546458763834f + w * -0.7905694150420947f);
        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        final float a = SimplexNoise.noise(xin, yin, zin, win, seed);
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        final float b = SimplexNoise.noise(xin + a * 0.25f, yin, zin, win, seed + 0x9A827999FCEF3243L);
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        final float c = SimplexNoise.noise(xin + b * 0.25f, yin, zin, win, seed + 0x3504F333F9DE6486L);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        final float d = SimplexNoise.noise(xin + c * 0.25f, yin, zin, win, seed + 0xCF876CCDF6CD96C9L);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        final float e = SimplexNoise.noise(xin + d * 0.25f, yin, zin, win, seed + 0x6A09E667F3BCC90CL);

        final float result = (a + b + c + d + e) * 0.2f;
        return result / (0.55f * Math.abs(result) + (1f - 0.55f));
// return result / (0.55f * Math.abs(result) + (1f - 0.55f));// gain function for [-1, 1] domain and range
    }

    // 5D SECTION

    /**
     * Gets 5D noise with {@link #getSeed()}.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    /**
     * Gets 5D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param seed any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        final float p0 = 0.7500f * (x *  0.8157559148337911f + y *  0.5797766823136037f);
        final float p1 = 0.7500f * (x * -0.7314923478726791f + y *  0.6832997137249108f);
        final float p2 = 0.7500f * (x * -0.0208603044412437f + y * -0.3155296974329846f + z * 0.9486832980505138f);
        final float p3 = 0.7500f * (x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w *   0.8944271909999159f);
        final float p4 = 0.7500f * (x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w * -0.44721359549995804f + u *  0.7745966692414833f);
        final float p5 = 0.7500f * (x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w * -0.44721359549995804f + u * -0.7745966692414836f);

        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        float uin = p5;
        final float a = SimplexNoise.noise(xin, yin, zin, win, uin, seed);
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        final float b = SimplexNoise.noise(xin + a * 0.25f, yin, zin, win, uin, seed + 0x9A827999FCEF3243L);
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        final float c = SimplexNoise.noise(xin + b * 0.25f, yin, zin, win, uin, seed + 0x3504F333F9DE6486L);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        final float d = SimplexNoise.noise(xin + c * 0.25f, yin, zin, win, uin, seed + 0xCF876CCDF6CD96C9L);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        final float e = SimplexNoise.noise(xin + d * 0.25f, yin, zin, win, uin, seed + 0x6A09E667F3BCC90CL);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        final float f = SimplexNoise.noise(xin + e * 0.25f, yin, zin, win, uin, seed + 0x48C6001F0ABFB4FL);

        final float result = (a + b + c + d + e + f) * 0.16666666666666666f;
        return result / (0.65f * Math.abs(result) + (1f - 0.65f));
// return result / (0.65f * Math.abs(result) + (1f - 0.65f));// gain function for [-1, 1] domain and range
    }

    // 6D SECTION

    /**
     * Gets 6D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    /**
     * Gets 6D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param v    v position; can be any finite float
     * @param seed any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        final float p0 = 0.7500f * (x);
        final float p1 = 0.7500f * (x * -0.16666666666666666f + y *  0.98601329718326940f);
        final float p2 = 0.7500f * (x * -0.16666666666666666f + y * -0.19720265943665383f + z *  0.96609178307929590f);
        final float p3 = 0.7500f * (x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w *  0.93541434669348530f);
        final float p4 = 0.7500f * (x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u *  0.8819171036881969f);
        final float p5 = 0.7500f * (x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u * -0.4409585518440984f + v *  0.7637626158259734f);
        final float p6 = 0.7500f * (x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u * -0.4409585518440984f + v * -0.7637626158259732f);
        float xin = p0;
        float yin = p5;
        float zin = p3;
        float win = p6;
        float uin = p1;
        float vin = p4;
        final float a = SimplexNoise.noise(xin, yin, zin, win, uin, vin, seed);
        xin = p2;
        yin = p6;
        zin = p0;
        win = p4;
        uin = p5;
        vin = p3;
        final float b = SimplexNoise.noise(xin + a * 0.25f, yin, zin, win, uin, vin, seed + 0x9A827999FCEF3243L);
        xin = p1;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p6;
        vin = p5;
        final float c = SimplexNoise.noise(xin + b * 0.25f, yin, zin, win, uin, vin, seed + 0x3504F333F9DE6486L);
        xin = p6;
        yin = p0;
        zin = p2;
        win = p5;
        uin = p4;
        vin = p1;
        final float d = SimplexNoise.noise(xin + c * 0.25f, yin, zin, win, uin, vin, seed + 0xCF876CCDF6CD96C9L);
        xin = p2;
        yin = p1;
        zin = p5;
        win = p0;
        uin = p3;
        vin = p6;
        final float e = SimplexNoise.noise(xin + d * 0.25f, yin, zin, win, uin, vin, seed + 0x6A09E667F3BCC90CL);
        xin = p0;
        yin = p4;
        zin = p6;
        win = p3;
        uin = p1;
        vin = p2;
        final float f = SimplexNoise.noise(xin + e * 0.25f, yin, zin, win, uin, vin, seed + 0x48C6001F0ABFB4FL);
        xin = p5;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p0;
        final float g = SimplexNoise.noise(xin + f * 0.25f, yin, zin, win, uin, vin, seed + 0x9F0ED99BED9B2D92L);
        final float result = (a + b + c + d + e + f + g) * 0.14285714285714285f;
        return result / (0.75f * Math.abs(result) + (1f - 0.75f));
// return result / (0.75f * Math.abs(result) + (1f - 0.75f));// gain function for [-1, 1] domain and range
    }

    // OTHER

    /**
     * Returns the String "FplN", to be used as a unique tag for this generator.
     *
     * @return the String "FplN"
     */
    @Override
    public String getTag() {
        return "FplN";
    }

    /**
     * Gets the minimum dimension supported by this generator, which is 2.
     *
     * @return the minimum supported dimension, which is 2 inclusive
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 6.
     *
     * @return the maximum supported dimension, which is 6 inclusive
     */
    @Override
    public int getMaxDimension() {
        return 6;
    }

    /**
     * Returns true because this generator can be seeded with {@link #setSeed(long)} and retrieved with
     * {@link #getSeed()}.
     *
     * @return true
     */
    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }
    
    public void setSeed(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    @Override
    public String stringSerialize() {
        return "`" + seed + "`";
    }

    @Override
    public FoamplexNoise stringDeserialize(String data) {
        setSeed(Base.BASE10.readLong(data, 1, data.length() - 1));
        return this;
    }

    public static FoamplexNoise recreateFromString(String data) {
        return new FoamplexNoise(Base.BASE10.readLong(data, 1, data.length() - 1));
    }

    @Override
    public FoamplexNoise copy() {
        return new FoamplexNoise(this.seed);
    }

    @Override
    public String toString() {
        return "FoamplexNoise{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FoamplexNoise that = (FoamplexNoise) o;

        return (seed == that.seed);
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ seed >>> 32);
    }
}
