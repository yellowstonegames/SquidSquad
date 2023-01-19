/*
 * Copyright (c) 2023 See AUTHORS file.
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

import com.github.yellowstonegames.core.DigitTools;

import static com.github.tommyettinger.digital.MathTools.fastFloor;
import static com.github.yellowstonegames.grid.LongPointHash.hash256;
import static com.github.yellowstonegames.grid.Noise.*;

/**
 * Simplex noise functions, in 2D, 3D, 4D, 5D, and 6D. This approach uses a simple sigmoid function to confine the
 * results of 4D, 5D, and 6D noise, meaning those can't produce values outside the -1 to 1 range here. It also means the
 * output has more variety in terms of what values it can produce relative to {@link SimplexNoiseScaled}, which just
 * scales noise down to try to fit it in-range instead of ensuring it has to be. SimplexNoiseScaled produces mostly
 * mid-range results in 4D and up, where this class is able to produce extreme values much more often. Because of how
 * this calculates 4D through 6D noise, those look better with 3 or more octaves when used with {@link NoiseWrapper}.
 * Using 2 octaves tends to allow flat areas to overlap each other awkwardly, but this mostly disappears with 3 octaves.
 */
public class SimplexNoise implements INoise {

    public long seed;
    public static final SimplexNoise instance = new SimplexNoise();

    public SimplexNoise() {
        seed = 0x1337BEEF2A22L;
    }

    public SimplexNoise(long seed)
    {
        this.seed = seed;
    }

    protected static float gradCoord2D(long seed, int x, int y, float xd, float yd) {
        final int h = hash256(x, y, seed) << 1;
        return xd * GRADIENTS_2D[h] + yd * GRADIENTS_2D[h+1];
    }
    /**
     * Computes the hash for a 3D int point and its dot product with a 3D float point as one step.
     * @return a float between -1.2571 and 1.2571, exclusive
     */
    protected static float gradCoord3D(long seed, int x, int y, int z, float xd, float yd, float zd) {
        final int hash = LongPointHash.hash32(x, y, z, seed) << 2;
        return xd * GRADIENTS_3D[hash] + yd * GRADIENTS_3D[hash + 1] + zd * GRADIENTS_3D[hash + 2];
    }
    protected static float gradCoord5D(long seed, int x, int y, int z, int w, int u, float xd, float yd, float zd, float wd, float ud) {
        final int hash = hash256(x, y, z, w, u, seed) << 3;
        return xd * GRADIENTS_5D[hash] + yd * GRADIENTS_5D[hash + 1] + zd * GRADIENTS_5D[hash + 2] + wd * GRADIENTS_5D[hash + 3] + ud * GRADIENTS_5D[hash + 4];
    }

    @Override
    public float getNoise(final float x, final float y) {
        return noise(x, y, seed);
    }
    @Override
    public float getNoise(final float x, final float y, final float z) {
        return noise(x, y, z, seed);
    }
    @Override
    public float getNoise(final float x, final float y, final float z, final float w) {
        return noise(x, y, z, w, seed);
    }
    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u) {
        return noise(x, y, z, w, u, seed);
    }
    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u, final float v) {
        return noise(x, y, z, w, u, v, seed);
    }

    @Override
    public float getNoiseWithSeed(final float x, final float y, final long seed) {
        return noise(x, y, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final float z, final long seed) {
        return noise(x, y, z, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final float z, final float w, final long seed) {
        return noise(x, y, z, w, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final float z, final float w, final float u, final long seed) {
        return noise(x, y, z, w, u, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final float z, final float w, final float u, final float v, final long seed) {
        return noise(x, y, z, w, u, v, seed);
    }

    protected static final float F2 = 0.36602540378443864676372317075294f,
            G2 = 0.21132486540518711774542560974902f,
            H2 = G2 * 2.0f,
            F3 = (float)(1.0 / 3.0),
            G3 = (float)(1.0 / 6.0),
            F4 = (float)((Math.sqrt(5.0) - 1.0) * 0.25),
            G4 = (float)((5.0 - Math.sqrt(5.0)) * 0.05),
            LIMIT4 = 0.4675f,
            F5 = (float)((Math.sqrt(6.0) - 1.0) / 5.0),
            G5 = (float)((6.0 - Math.sqrt(6.0)) / 30.0),
            LIMIT5 = 0.67f,
            F6 = (float)((Math.sqrt(7.0) - 1.0) / 6.0),
            G6 = (float)(F6 / (1.0 + 6.0 * F6)),
            LIMIT6 = 0.69f;

    public static float noise(final float x, final float y, final long seed) {
        float t = (x + y) * F2;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);

        t = (i + j) * G2;
        float X0 = i - t;
        float Y0 = j - t;

        float x0 = x - X0;
        float y0 = y - Y0;

        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        float x1 = x0 - i1 + G2;
        float y1 = y0 - j1 + G2;
        float x2 = x0 - 1 + H2;
        float y2 = y0 - 1 + H2;

        float n0, n1, n2;

        n0 = 0.5f - x0 * x0 - y0 * y0;
        if (n0 > 0) {
            n0 *= n0;
            n0 *= n0 * gradCoord2D(seed, i, j, x0, y0);
        }
        else n0 = 0.0f;

        n1 = 0.5f - x1 * x1 - y1 * y1;
        if (n1 > 0) {
            n1 *= n1;
            n1 *= n1 * gradCoord2D(seed, i + i1, j + j1, x1, y1);
        }
        else n1 = 0.0f;

        n2 = 0.5f - x2 * x2 - y2 * y2;
        if (n2 > 0)  {
            n2 *= n2;
            n2 *= n2 * gradCoord2D(seed, i + 1, j + 1, x2, y2);
        }
        else n2 = 0.0f;

        return (n0 + n1 + n2) * 99.20689070704672f; // this is 99.83685446303647 / 1.00635 ; the first number was found by kdotjpg
    }

    public static float noise(final float x, final float y, final float z, final long seed) {
        final float s = (x + y + z) * F3;
        final int i = fastFloor(x + s),
                j = fastFloor(y + s),
                k = fastFloor(z + s);

        final float t = (i + j + k) * G3;
        final float X0 = i - t, Y0 = j - t, Z0 = k - t,
                x0 = x - X0, y0 = y - Y0, z0 = z - Z0;

        int i1, j1, k1;
        int i2, j2, k2;

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            }
        }

        float x1 = x0 - i1 + G3; // Offsets for second corner in (x,y,z) coords
        float y1 = y0 - j1 + G3;
        float z1 = z0 - k1 + G3;
        float x2 = x0 - i2 + F3; // Offsets for third corner in (x,y,z) coords
        float y2 = y0 - j2 + F3;
        float z2 = z0 - k2 + F3;
        float x3 = x0 - 0.5f; // Offsets for last corner in (x,y,z) coords
        float y3 = y0 - 0.5f;
        float z3 = z0 - 0.5f;

        // Calculate the contribution from the four corners
        float n0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (n0 > 0) {
            n0 *= n0;
            n0 *= n0 * gradCoord3D(seed, i, j, k, x0, y0, z0);
        }
        else n0 = 0.0f;

        float n1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (n1 > 0) {
            n1 *= n1;
            n1 *= n1 * gradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1);
        }
        else n1 = 0.0f;

        float n2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (n2 > 0) {
            n2 *= n2;
            n2 *= n2 * gradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2);
        }
        else n2 = 0.0f;
        float n3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (n3 > 0) {
            n3 *= n3;
            n3 *= n3 * gradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3);
        }
        else n3 = 0.0f;

        // Add contributions from each corner to get the final noise value.
        // The result is clamped to stay just inside [-1,1]
        return 31.5f * (n0 + n1 + n2 + n3);
        //return (32.0 * n) * 1.25086885 + 0.0003194984;
    }

    public static float noise(final float x, final float y, final float z, final float w, final long seed) {
        float n0, n1, n2, n3, n4;
        float[] gradient4DLUT = GRADIENTS_4D;
        float t = (x + y + z + w) * F4;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        t = (i + j + k + l) * G4;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;

        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;

        if (z0 > w0) rankz++; else rankw++;

        int i1 = 2 - rankx >>> 31;
        int j1 = 2 - ranky >>> 31;
        int k1 = 2 - rankz >>> 31;
        int l1 = 2 - rankw >>> 31;

        int i2 = 1 - rankx >>> 31;
        int j2 = 1 - ranky >>> 31;
        int k2 = 1 - rankz >>> 31;
        int l2 = 1 - rankw >>> 31;

        int i3 = -rankx >>> 31;
        int j3 = -ranky >>> 31;
        int k3 = -rankz >>> 31;
        int l3 = -rankw >>> 31;

        float x1 = x0 - i1 + G4;
        float y1 = y0 - j1 + G4;
        float z1 = z0 - k1 + G4;
        float w1 = w0 - l1 + G4;

        float x2 = x0 - i2 + 2 * G4;
        float y2 = y0 - j2 + 2 * G4;
        float z2 = z0 - k2 + 2 * G4;
        float w2 = w0 - l2 + 2 * G4;

        float x3 = x0 - i3 + 3 * G4;
        float y3 = y0 - j3 + 3 * G4;
        float z3 = z0 - k3 + 3 * G4;
        float w3 = w0 - l3 + 3 * G4;

        float x4 = x0 - 1 + 4 * G4;
        float y4 = y0 - 1 + 4 * G4;
        float z4 = z0 - 1 + 4 * G4;
        float w4 = w0 - 1 + 4 * G4;

        float t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            final int h0 = (hash256(i, j, k, l, seed) & 0xFC);
            t0 *= t0;
            n0 = t0 * t0 * (x0 * gradient4DLUT[h0] + y0 * gradient4DLUT[h0 | 1] + z0 * gradient4DLUT[h0 | 2] + w0 * gradient4DLUT[h0 | 3]);
        }
        else n0 = 0;
        float t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            final int h1 = (hash256(i + i1, j + j1, k + k1, l + l1, seed) & 0xFC);
            t1 *= t1;
            n1 = t1 * t1 * (x1 * gradient4DLUT[h1] + y1 * gradient4DLUT[h1 | 1] + z1 * gradient4DLUT[h1 | 2] + w1 * gradient4DLUT[h1 | 3]);
        }
        else n1 = 0;
        float t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            final int h2 = (hash256(i + i2, j + j2, k + k2, l + l2, seed) & 0xFC);
            t2 *= t2;
            n2 = t2 * t2 * (x2 * gradient4DLUT[h2] + y2 * gradient4DLUT[h2 | 1] + z2 * gradient4DLUT[h2 | 2] + w2 * gradient4DLUT[h2 | 3]);
        }
        else n2 = 0;
        float t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            final int h3 = (hash256(i + i3, j + j3, k + k3, l + l3, seed) & 0xFC);
            t3 *= t3;
            n3 = t3 * t3 * (x3 * gradient4DLUT[h3] + y3 * gradient4DLUT[h3 | 1] + z3 * gradient4DLUT[h3 | 2] + w3 * gradient4DLUT[h3 | 3]);
        }
        else n3 = 0;
        float t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            final int h4 = (hash256(i + 1, j + 1, k + 1, l + 1, seed) & 0xFC);
            t4 *= t4;
            n4 = t4 * t4 * (x4 * gradient4DLUT[h4] + y4 * gradient4DLUT[h4 | 1] + z4 * gradient4DLUT[h4 | 2] + w4 * gradient4DLUT[h4 | 3]);
        }
        else n4 = 0;

        t = (n0 + n1 + n2 + n3 + n4) * 141;
        return t / (0.750f + Math.abs(t));
//        return t / (1f + Math.abs(t));
    }
    // debug code, for finding what constant should be used for 14.75
//        final float ret =  (n0 + n1 + n2 + n3 + n4) * (14.7279);
//        if(ret < -1 || ret > 1) {
//            System.out.println(ret + " is out of bounds! seed=" + seed + ", x=" + x + ", y=" + y + ", z=" + z + ", w=" + w);
//            return ret * -0.5f;
//        }
//        return ret;
    // normal return code
//        return (n0 + n1 + n2 + n3 + n4) * 14.7279f;

    /**
     * Thanks to Mark A. Ropper for
     * <a href="https://computergraphics.stackexchange.com/questions/6408/what-might-be-causing-these-artifacts-in-5d-6d-simplex-noise">this implementation</a>.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate (4th dimension)
     * @param u u coordinate (5th dimension)
     * @param seed long value that should completely change the noise if it changes even slightly
     * @return a continuous noise value between -1.0 and 1.0, both inclusive
     */
    public static float noise(final float x, final float y, final float z, final float w, final float u, final long seed) {
        float n0, n1, n2, n3, n4, n5;
        float t = (x + y + z + w + u) * F5;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        int h = fastFloor(u + t);
        t = (i + j + k + l + h) * G5;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;
        float U0 = h - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;
        float u0 = u - U0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        int ranku = 0;

        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;
        if (x0 > u0) rankx++; else ranku++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;
        if (y0 > u0) ranky++; else ranku++;

        if (z0 > w0) rankz++; else rankw++;
        if (z0 > u0) rankz++; else ranku++;

        if (w0 > u0) rankw++; else ranku++;

        int i1 = 3 - rankx >>> 31;
        int j1 = 3 - ranky >>> 31;
        int k1 = 3 - rankz >>> 31;
        int l1 = 3 - rankw >>> 31;
        int h1 = 3 - ranku >>> 31;

        int i2 = 2 - rankx >>> 31;
        int j2 = 2 - ranky >>> 31;
        int k2 = 2 - rankz >>> 31;
        int l2 = 2 - rankw >>> 31;
        int h2 = 2 - ranku >>> 31;

        int i3 = 1 - rankx >>> 31;
        int j3 = 1 - ranky >>> 31;
        int k3 = 1 - rankz >>> 31;
        int l3 = 1 - rankw >>> 31;
        int h3 = 1 - ranku >>> 31;

        int i4 = -rankx >>> 31;
        int j4 = -ranky >>> 31;
        int k4 = -rankz >>> 31;
        int l4 = -rankw >>> 31;
        int h4 = -ranku >>> 31;

        float x1 = x0 - i1 + G5;
        float y1 = y0 - j1 + G5;
        float z1 = z0 - k1 + G5;
        float w1 = w0 - l1 + G5;
        float u1 = u0 - h1 + G5;

        float x2 = x0 - i2 + 2 * G5;
        float y2 = y0 - j2 + 2 * G5;
        float z2 = z0 - k2 + 2 * G5;
        float w2 = w0 - l2 + 2 * G5;
        float u2 = u0 - h2 + 2 * G5;

        float x3 = x0 - i3 + 3 * G5;
        float y3 = y0 - j3 + 3 * G5;
        float z3 = z0 - k3 + 3 * G5;
        float w3 = w0 - l3 + 3 * G5;
        float u3 = u0 - h3 + 3 * G5;

        float x4 = x0 - i4 + 4 * G5;
        float y4 = y0 - j4 + 4 * G5;
        float z4 = z0 - k4 + 4 * G5;
        float w4 = w0 - l4 + 4 * G5;
        float u4 = u0 - h4 + 4 * G5;

        float x5 = x0 - 1 + 5 * G5;
        float y5 = y0 - 1 + 5 * G5;
        float z5 = z0 - 1 + 5 * G5;
        float w5 = w0 - 1 + 5 * G5;
        float u5 = u0 - 1 + 5 * G5;

        t = LIMIT5 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0 - u0 * u0;
        if (t < 0) n0 = 0;
        else
        {
            t *= t;
            n0 = t * t * gradCoord5D(seed, i, j, k, l, h, x0, y0, z0, w0, u0);
        }

        t = LIMIT5 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1;
        if (t < 0) n1 = 0;
        else
        {
            t *= t;
            n1 = t * t * gradCoord5D(seed, i + i1, j + j1, k + k1, l + l1, h + h1, x1, y1, z1, w1, u1);
        }

        t = LIMIT5 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2;
        if (t < 0) n2 = 0;
        else
        {
            t *= t;
            n2 = t * t * gradCoord5D(seed, i + i2, j + j2, k + k2, l + l2, h + h2, x2, y2, z2, w2, u2);
        }

        t = LIMIT5 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3;
        if (t < 0) n3 = 0;
        else
        {
            t *= t;
            n3 = t * t * gradCoord5D(seed, i + i3, j + j3, k + k3, l + l3, h + h3, x3, y3, z3, w3, u3);
        }

        t = LIMIT5 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4;
        if (t < 0) n4 = 0;
        else
        {
            t *= t;
            n4 = t * t * gradCoord5D(seed, i + i4, j + j4, k + k4, l + l4, h + h4, x4, y4, z4, w4, u4);
        }

        t = LIMIT5 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5;
        if (t < 0) n5 = 0;
        else
        {
            t *= t;
            n5 = t * t * gradCoord5D(seed, i + 1, j + 1, k + 1, l + 1, h + 1, x5, y5, z5, w5, u5);
        }

        t = (n0 + n1 + n2 + n3 + n4 + n5) * 32f;
//        return t / (1f + Math.abs(t));
        return t / (0.700f + Math.abs(t));
//        return (float) Math.tanh(t);
//        return t / (float) Math.sqrt(1f + t * t);
    }
//        t = (n0 + n1 + n2 + n3 + n4 + n5) * 10.0;
//        if(t < -1.0) {
//            System.out.println(t);
//            return -1.0;
//        }
//        if(t > 1.0) {
//            System.out.println(t);
//            return 1.0;
//        }
//        return t;

    public static float noise(final float x, final float y, final float z,
                               final float w, final float u, final float v, final long seed) {
        float[] gradient6DLUT = GRADIENTS_6D;
        float n0, n1, n2, n3, n4, n5, n6;
        float t = (x + y + z + w + u + v) * F6;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        int h = fastFloor(u + t);
        int g = fastFloor(v + t);
        t = (i + j + k + l + h + g) * G6;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;
        float U0 = h - t;
        float V0 = g - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;
        float u0 = u - U0;
        float v0 = v - V0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        int ranku = 0;
        int rankv = 0;

        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;
        if (x0 > u0) rankx++; else ranku++;
        if (x0 > v0) rankx++; else rankv++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;
        if (y0 > u0) ranky++; else ranku++;
        if (y0 > v0) ranky++; else rankv++;

        if (z0 > w0) rankz++; else rankw++;
        if (z0 > u0) rankz++; else ranku++;
        if (z0 > v0) rankz++; else rankv++;

        if (w0 > u0) rankw++; else ranku++;
        if (w0 > v0) rankw++; else rankv++;

        if (u0 > v0) ranku++; else rankv++;

        int i1 = 4 - rankx >>> 31;
        int j1 = 4 - ranky >>> 31;
        int k1 = 4 - rankz >>> 31;
        int l1 = 4 - rankw >>> 31;
        int h1 = 4 - ranku >>> 31;
        int g1 = 4 - rankv >>> 31;

        int i2 = 3 - rankx >>> 31;
        int j2 = 3 - ranky >>> 31;
        int k2 = 3 - rankz >>> 31;
        int l2 = 3 - rankw >>> 31;
        int h2 = 3 - ranku >>> 31;
        int g2 = 3 - rankv >>> 31;

        int i3 = 2 - rankx >>> 31;
        int j3 = 2 - ranky >>> 31;
        int k3 = 2 - rankz >>> 31;
        int l3 = 2 - rankw >>> 31;
        int h3 = 2 - ranku >>> 31;
        int g3 = 2 - rankv >>> 31;

        int i4 = 1 - rankx >>> 31;
        int j4 = 1 - ranky >>> 31;
        int k4 = 1 - rankz >>> 31;
        int l4 = 1 - rankw >>> 31;
        int h4 = 1 - ranku >>> 31;
        int g4 = 1 - rankv >>> 31;

        int i5 = -rankx >>> 31;
        int j5 = -ranky >>> 31;
        int k5 = -rankz >>> 31;
        int l5 = -rankw >>> 31;
        int h5 = -ranku >>> 31;
        int g5 = -rankv >>> 31;

        float x1 = x0 - i1 + G6;
        float y1 = y0 - j1 + G6;
        float z1 = z0 - k1 + G6;
        float w1 = w0 - l1 + G6;
        float u1 = u0 - h1 + G6;
        float v1 = v0 - g1 + G6;

        float x2 = x0 - i2 + 2 * G6;
        float y2 = y0 - j2 + 2 * G6;
        float z2 = z0 - k2 + 2 * G6;
        float w2 = w0 - l2 + 2 * G6;
        float u2 = u0 - h2 + 2 * G6;
        float v2 = v0 - g2 + 2 * G6;

        float x3 = x0 - i3 + 3 * G6;
        float y3 = y0 - j3 + 3 * G6;
        float z3 = z0 - k3 + 3 * G6;
        float w3 = w0 - l3 + 3 * G6;
        float u3 = u0 - h3 + 3 * G6;
        float v3 = v0 - g3 + 3 * G6;

        float x4 = x0 - i4 + 4 * G6;
        float y4 = y0 - j4 + 4 * G6;
        float z4 = z0 - k4 + 4 * G6;
        float w4 = w0 - l4 + 4 * G6;
        float u4 = u0 - h4 + 4 * G6;
        float v4 = v0 - g4 + 4 * G6;

        float x5 = x0 - i5 + 5 * G6;
        float y5 = y0 - j5 + 5 * G6;
        float z5 = z0 - k5 + 5 * G6;
        float w5 = w0 - l5 + 5 * G6;
        float u5 = u0 - h5 + 5 * G6;
        float v5 = v0 - g5 + 5 * G6;

        float x6 = x0 - 1 + 6 * G6;
        float y6 = y0 - 1 + 6 * G6;
        float z6 = z0 - 1 + 6 * G6;
        float w6 = w0 - 1 + 6 * G6;
        float u6 = u0 - 1 + 6 * G6;
        float v6 = v0 - 1 + 6 * G6;

        n0 = LIMIT6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0 - u0 * u0 - v0 * v0;
        if (n0 <= 0.0f) n0 = 0.0f;
        else
        {
            final int hash = hash256(i, j, k, l, h, g, seed) * 6;
            n0 *= n0;
            n0 *= n0 * (gradient6DLUT[hash] * x0 + gradient6DLUT[hash + 1] * y0 + gradient6DLUT[hash + 2] * z0 +
                    gradient6DLUT[hash + 3] * w0 + gradient6DLUT[hash + 4] * u0 + gradient6DLUT[hash + 5] * v0);
        }

        n1 = LIMIT6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1 - v1 * v1;
        if (n1 <= 0.0f) n1 = 0.0f;
        else
        {
            final int hash = hash256(i + i1, j + j1, k + k1, l + l1, h + h1, g + g1, seed) * 6;
            n1 *= n1;
            n1 *= n1 * (gradient6DLUT[hash] * x1 + gradient6DLUT[hash + 1] * y1 + gradient6DLUT[hash + 2] * z1 +
                    gradient6DLUT[hash + 3] * w1 + gradient6DLUT[hash + 4] * u1 + gradient6DLUT[hash + 5] * v1);
        }
        
        n2 = LIMIT6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2 - v2 * v2;
        if (n2 <= 0.0f) n2 = 0.0f;
        else
        {
            final int hash = hash256(i + i2, j + j2, k + k2, l + l2, h + h2, g + g2, seed) * 6;
            n2 *= n2;
            n2 *= n2 * (gradient6DLUT[hash] * x2 + gradient6DLUT[hash + 1] * y2 + gradient6DLUT[hash + 2] * z2 +
                    gradient6DLUT[hash + 3] * w2 + gradient6DLUT[hash + 4] * u2 + gradient6DLUT[hash + 5] * v2);
        }

        n3 = LIMIT6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3 - v3 * v3;
        if (n3 <= 0.0f) n3 = 0.0f;
        else
        {
            final int hash = hash256(i + i3, j + j3, k + k3, l + l3, h + h3, g + g3, seed) * 6;
            n3 *= n3;
            n3 *= n3 * (gradient6DLUT[hash] * x3 + gradient6DLUT[hash + 1] * y3 + gradient6DLUT[hash + 2] * z3 +
                    gradient6DLUT[hash + 3] * w3 + gradient6DLUT[hash + 4] * u3 + gradient6DLUT[hash + 5] * v3);
        }

        n4 = LIMIT6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4 - v4 * v4;
        if (n4 <= 0.0f) n4 = 0.0f;
        else
        {
            final int hash = hash256(i + i4, j + j4, k + k4, l + l4, h + h4, g + g4, seed) * 6;
            n4 *= n4;
            n4 *= n4 * (gradient6DLUT[hash] * x4 + gradient6DLUT[hash + 1] * y4 + gradient6DLUT[hash + 2] * z4 +
                    gradient6DLUT[hash + 3] * w4 + gradient6DLUT[hash + 4] * u4 + gradient6DLUT[hash + 5] * v4);
        }

        n5 = LIMIT6 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5 - v5 * v5;
        if (n5 <= 0.0f) n5 = 0.0f;
        else
        {
            final int hash = hash256(i + i5, j + j5, k + k5, l + l5, h + h5, g + g5, seed) * 6;
            n5 *= n5;
            n5 *= n5 * (gradient6DLUT[hash] * x5 + gradient6DLUT[hash + 1] * y5 + gradient6DLUT[hash + 2] * z5 +
                    gradient6DLUT[hash + 3] * w5 + gradient6DLUT[hash + 4] * u5 + gradient6DLUT[hash + 5] * v5);
        }

        n6 = LIMIT6 - x6 * x6 - y6 * y6 - z6 * z6 - w6 * w6 - u6 * u6 - v6 * v6;
        if (n6 <= 0.0f) n6 = 0.0f;
        else
        {
            final int hash = hash256(i + 1, j + 1, k + 1, l + 1, h + 1, g + 1, seed) * 6;
            n6 *= n6;
            n6 *= n6 * (gradient6DLUT[hash] * x6 + gradient6DLUT[hash + 1] * y6 + gradient6DLUT[hash + 2] * z6 +
                    gradient6DLUT[hash + 3] * w6 + gradient6DLUT[hash + 4] * u6 + gradient6DLUT[hash + 5] * v6);
        }

        t = (n0 + n1 + n2 + n3 + n4 + n5 + n6) * 64f;
        return t / (0.500f + Math.abs(t));
//        return t / (1f + Math.abs(t));
    }
//        return  (n0 + n1 + n2 + n3 + n4 + n5 + n6) * 7.499f;

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 6;
    }

    @Override
    public boolean canUseSeed() {
        return true;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    public String serializeString() {
        return "`" + seed + "`";
    }

    public static SimplexNoise deserializeString(String data) {
        return new SimplexNoise(DigitTools.longFromDec(data, 1, data.length() - 1));
    }

    @Override
    public String toString() {
        return "SimplexNoise{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplexNoise that = (SimplexNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }
}
