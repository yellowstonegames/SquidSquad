/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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
import static com.github.yellowstonegames.grid.LongPointHash.hash32;

/**
 * Simplex noise functions, in 2D, 3D, 4D, 5D, and 6D. This approach uses a simple sigmoid function to confine the
 * results of 4D, 5D, and 6D noise, meaning those can't produce values outside the -1 to 1 range here. It also means the
 * output has more variety in terms of what values it can produce relative to {@link SimplexNoiseScaled}, which just
 * scales noise down to try to fit it in-range instead of ensuring it has to be. SimplexNoiseScaled produces mostly
 * mid-range results in 4D and up, where this class is able to produce extreme values much more often. Because of how
 * this calculates 4D through 6D noise, those look better with 3 or more octaves when used with {@link NoiseWrapper}.
 * Using 2 octaves tends to allow flat areas to overlap each other awkwardly, but this mostly disappears with 3 octaves.
 * <br>
 * {@link SimplexNoise} takes a different approach to increasing contrast in dimensions 4 and up, and may look better
 * when used with {@link NoiseWrapper#getFractalType() fractal types} other than FBM.
 */
public class SimplexNoiseHard implements INoise {

    public long seed;
    public static final SimplexNoiseHard instance = new SimplexNoiseHard();

    public SimplexNoiseHard() {
        seed = 0x1337BEEF2A22L;
    }

    public SimplexNoiseHard(long seed)
    {
        this.seed = seed;
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

    public static final float F2 = 0.36602540378443864676372317075294f;
    public static final float G2 = 0.21132486540518711774542560974902f;
    public static final float H2 = G2 * 2.0f;
    public static final float LIMIT2 = 0.5f;
    public static final float F3 = (float)(1.0 / 3.0);
    public static final float G3 = (float)(1.0 / 6.0);
    public static final float LIMIT3 = 0.6f;
    public static final float F4 = (float)((Math.sqrt(5.0) - 1.0) * 0.25);
    public static final float G4 = (float)((5.0 - Math.sqrt(5.0)) * 0.05);
    public static final float LIMIT4 = 0.4675f;
    public static final float F5 = (float)((Math.sqrt(6.0) - 1.0) / 5.0);
    public static final float G5 = (float)((6.0 - Math.sqrt(6.0)) / 30.0);
    public static final float LIMIT5 = 0.67f;
    public static final float F6 = (float)((Math.sqrt(7.0) - 1.0) / 6.0);
    public static final float G6 = (float)(F6 / (1.0 + 6.0 * F6));
    public static final float LIMIT6 = 0.69f;

    public static float noise(final float x, final float y, final long seed) {
        final float[] GRADIENTS_2D = GradientVectors.GRADIENTS_2D;

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

        float n = 0;

        t = 0.5f - x0 * x0 - y0 * y0;
        if (t > 0) {
            t *= t;
            final int h = hash256(i, j, seed) << 1;
            n += t * t * (x0 * GRADIENTS_2D[h] + y0 * GRADIENTS_2D[h+1]);
        }

        t = 0.5f - x1 * x1 - y1 * y1;
        if (t > 0) {
            t *= t;
            final int h = hash256(i + i1, j + j1, seed) << 1;
            n += t * t * (x1 * GRADIENTS_2D[h] + y1 * GRADIENTS_2D[h+1]);
        }

        t = 0.5f - x2 * x2 - y2 * y2;
        if (t > 0)  {
            t *= t;
            final int h = hash256(i + 1, j + 1, seed) << 1;
            n += t * t * (x2 * GRADIENTS_2D[h] + y2 * GRADIENTS_2D[h+1]);
        }

        return n * 99.20689070704672f; // this is 99.83685446303647 / 1.00635 ; the first number was found by kdotjpg
    }

    public static float noise(final float x, final float y, final float z, final long seed) {
        final float[] GRADIENTS_3D = GradientVectors.GRADIENTS_3D;

        float t = (x + y + z) * F3;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);

        t = (i + j + k) * G3;
        float x0 = x - (i - t);
        float y0 = y - (j - t);
        float z0 = z - (k - t);

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
            } else // x0 < z0
            {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else // x0 < y0
        {
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
            } else // x0 >= z0
            {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            }
        }

        float x1 = x0 - i1 + G3;
        float y1 = y0 - j1 + G3;
        float z1 = z0 - k1 + G3;
        float x2 = x0 - i2 + F3;
        float y2 = y0 - j2 + F3;
        float z2 = z0 - k2 + F3;
        float x3 = x0 - 0.5f;
        float y3 = y0 - 0.5f;
        float z3 = z0 - 0.5f;

        float n = 0;

        t = LIMIT3 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t > 0) {
            t *= t;
            final int h = hash32(i, j, k, seed) << 2;
            n += t * t * (x0 * GRADIENTS_3D[h] + y0 * GRADIENTS_3D[h + 1] + z0 * GRADIENTS_3D[h + 2]);
        }

        t = LIMIT3 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t > 0) {
            t *= t;
            final int h = hash32(i + i1, j + j1, k + k1, seed) << 2;
            n += t * t * (x1 * GRADIENTS_3D[h] + y1 * GRADIENTS_3D[h + 1] + z1 * GRADIENTS_3D[h + 2]);
        }

        t = LIMIT3 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t > 0) {
            t *= t;
            final int h = hash32(i + i2, j + j2, k + k2, seed) << 2;
            n += t * t * (x2 * GRADIENTS_3D[h] + y2 * GRADIENTS_3D[h + 1] + z2 * GRADIENTS_3D[h + 2]);
        }

        t = LIMIT3 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t > 0)  {
            t *= t;
            final int h = hash32(i + 1, j + 1, k + 1, seed) << 2;
            n += t * t * (x3 * GRADIENTS_3D[h] + y3 * GRADIENTS_3D[h + 1] + z3 * GRADIENTS_3D[h + 2]);
        }

        return 39.59758f * n;
    }

    public static float noise(final float x, final float y, final float z, final float w, final long seed) {
        final float[] GRADIENTS_4D = GradientVectors.GRADIENTS_4D;
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

        // @formatter:off
        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;

        if (z0 > w0) rankz++; else rankw++;
        // @formatter:on

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

        float n = 0f;
        float t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            final int h0 = (hash256(i, j, k, l, seed) & 0xFC);
            t0 *= t0;
            n += t0 * t0 * (x0 * GRADIENTS_4D[h0] + y0 * GRADIENTS_4D[h0 + 1] + z0 * GRADIENTS_4D[h0 + 2] + w0 * GRADIENTS_4D[h0 + 3]);
        }
        float t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            final int h1 = (hash256(i + i1, j + j1, k + k1, l + l1, seed) & 0xFC);
            t1 *= t1;
            n += t1 * t1 * (x1 * GRADIENTS_4D[h1] + y1 * GRADIENTS_4D[h1 + 1] + z1 * GRADIENTS_4D[h1 + 2] + w1 * GRADIENTS_4D[h1 + 3]);
        }
        float t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            final int h2 = (hash256(i + i2, j + j2, k + k2, l + l2, seed) & 0xFC);
            t2 *= t2;
            n += t2 * t2 * (x2 * GRADIENTS_4D[h2] + y2 * GRADIENTS_4D[h2 + 1] + z2 * GRADIENTS_4D[h2 + 2] + w2 * GRADIENTS_4D[h2 + 3]);
        }
        float t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            final int h3 = (hash256(i + i3, j + j3, k + k3, l + l3, seed) & 0xFC);
            t3 *= t3;
            n += t3 * t3 * (x3 * GRADIENTS_4D[h3] + y3 * GRADIENTS_4D[h3 + 1] + z3 * GRADIENTS_4D[h3 + 2] + w3 * GRADIENTS_4D[h3 + 3]);
        }
        float t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            final int h4 = (hash256(i + 1, j + 1, k + 1, l + 1, seed) & 0xFC);
            t4 *= t4;
            n += t4 * t4 * (x4 * GRADIENTS_4D[h4] + y4 * GRADIENTS_4D[h4 + 1] + z4 * GRADIENTS_4D[h4 + 2] + w4 * GRADIENTS_4D[h4 + 3]);
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
        n *= 356.165f;
        return n / (0.750f + Math.abs(t));
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
        final float[] GRADIENTS_5D = GradientVectors.GRADIENTS_5D;
        float n = 0f;
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

        // @formatter:off
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
        // @formatter:on

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
        if (t > 0) {
            t *= t;
            final int hash = hash256(i, j, k, l, h, seed) << 3;
            n += t * t * (x0 * GRADIENTS_5D[hash] + y0 * GRADIENTS_5D[hash + 1] + z0 * GRADIENTS_5D[hash + 2] + w0 * GRADIENTS_5D[hash + 3] + u0 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + i1, j + j1, k + k1, l + l1, h + h1, seed) << 3;
            n += t * t * (x1 * GRADIENTS_5D[hash] + y1 * GRADIENTS_5D[hash + 1] + z1 * GRADIENTS_5D[hash + 2] + w1 * GRADIENTS_5D[hash + 3] + u1 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + i2, j + j2, k + k2, l + l2, h + h2, seed) << 3;
            n += t * t * (x2 * GRADIENTS_5D[hash] + y2 * GRADIENTS_5D[hash + 1] + z2 * GRADIENTS_5D[hash + 2] + w2 * GRADIENTS_5D[hash + 3] + u2 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + i3, j + j3, k + k3, l + l3, h + h3, seed) << 3;
            n += t * t * (x3 * GRADIENTS_5D[hash] + y3 * GRADIENTS_5D[hash + 1] + z3 * GRADIENTS_5D[hash + 2] + w3 * GRADIENTS_5D[hash + 3] + u3 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + i4, j + j4, k + k4, l + l4, h + h4, seed) << 3;
            n += t * t * (x4 * GRADIENTS_5D[hash] + y4 * GRADIENTS_5D[hash + 1] + z4 * GRADIENTS_5D[hash + 2] + w4 * GRADIENTS_5D[hash + 3] + u4 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + 1, j + 1, k + 1, l + 1, h + 1, seed) << 3;
            n += t * t * (x5 * GRADIENTS_5D[hash] + y5 * GRADIENTS_5D[hash + 1] + z5 * GRADIENTS_5D[hash + 2] + w5 * GRADIENTS_5D[hash + 3] + u5 * GRADIENTS_5D[hash + 4]);
        }

        n *= 64f;
        return n / (0.700f + Math.abs(t));
//        return t / (1f + Math.abs(t));
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
        final float[] GRADIENTS_6D = GradientVectors.GRADIENTS_6D;
        float n0, n1, n2, n3, n4, n5, n6, n = 0f;
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

        // @formatter:off
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
        // @formatter:on

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
        if (n0 > 0.0f) {
            final int hash = hash256(i, j, k, l, h, g, seed) << 3;
            n0 *= n0;
            n += n0 * n0 * (GRADIENTS_6D[hash] * x0 + GRADIENTS_6D[hash + 1] * y0 + GRADIENTS_6D[hash + 2] * z0 +
                    GRADIENTS_6D[hash + 3] * w0 + GRADIENTS_6D[hash + 4] * u0 + GRADIENTS_6D[hash + 5] * v0);
        }

        n1 = LIMIT6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1 - v1 * v1;
        if (n1 > 0.0f) {
            final int hash = hash256(i + i1, j + j1, k + k1, l + l1, h + h1, g + g1, seed) << 3;
            n1 *= n1;
            n += n1 * n1 * (GRADIENTS_6D[hash] * x1 + GRADIENTS_6D[hash + 1] * y1 + GRADIENTS_6D[hash + 2] * z1 +
                    GRADIENTS_6D[hash + 3] * w1 + GRADIENTS_6D[hash + 4] * u1 + GRADIENTS_6D[hash + 5] * v1);
        }

        n2 = LIMIT6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2 - v2 * v2;
        if (n2 > 0.0f) {
            final int hash = hash256(i + i2, j + j2, k + k2, l + l2, h + h2, g + g2, seed) << 3;
            n2 *= n2;
            n += n2 * n2 * (GRADIENTS_6D[hash] * x2 + GRADIENTS_6D[hash + 1] * y2 + GRADIENTS_6D[hash + 2] * z2 +
                    GRADIENTS_6D[hash + 3] * w2 + GRADIENTS_6D[hash + 4] * u2 + GRADIENTS_6D[hash + 5] * v2);
        }

        n3 = LIMIT6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3 - v3 * v3;
        if (n3 > 0.0f) {
            final int hash = hash256(i + i3, j + j3, k + k3, l + l3, h + h3, g + g3, seed) << 3;
            n3 *= n3;
            n += n3 * n3 * (GRADIENTS_6D[hash] * x3 + GRADIENTS_6D[hash + 1] * y3 + GRADIENTS_6D[hash + 2] * z3 +
                    GRADIENTS_6D[hash + 3] * w3 + GRADIENTS_6D[hash + 4] * u3 + GRADIENTS_6D[hash + 5] * v3);
        }

        n4 = LIMIT6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4 - v4 * v4;
        if (n4 > 0.0f) {
            final int hash = hash256(i + i4, j + j4, k + k4, l + l4, h + h4, g + g4, seed) << 3;
            n4 *= n4;
            n += n4 * n4 * (GRADIENTS_6D[hash] * x4 + GRADIENTS_6D[hash + 1] * y4 + GRADIENTS_6D[hash + 2] * z4 +
                    GRADIENTS_6D[hash + 3] * w4 + GRADIENTS_6D[hash + 4] * u4 + GRADIENTS_6D[hash + 5] * v4);
        }

        n5 = LIMIT6 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5 - v5 * v5;
        if (n5 > 0.0f) {
            final int hash = hash256(i + i5, j + j5, k + k5, l + l5, h + h5, g + g5, seed) << 3;
            n5 *= n5;
            n += n5 * n5 * (GRADIENTS_6D[hash] * x5 + GRADIENTS_6D[hash + 1] * y5 + GRADIENTS_6D[hash + 2] * z5 +
                    GRADIENTS_6D[hash + 3] * w5 + GRADIENTS_6D[hash + 4] * u5 + GRADIENTS_6D[hash + 5] * v5);
        }

        n6 = LIMIT6 - x6 * x6 - y6 * y6 - z6 * z6 - w6 * w6 - u6 * u6 - v6 * v6;
        if (n6 > 0.0f) {
            final int hash = hash256(i + 1, j + 1, k + 1, l + 1, h + 1, g + 1, seed) << 3;
            n6 *= n6;
            n += n6 * n6 * (GRADIENTS_6D[hash] * x6 + GRADIENTS_6D[hash + 1] * y6 + GRADIENTS_6D[hash + 2] * z6 +
                    GRADIENTS_6D[hash + 3] * w6 + GRADIENTS_6D[hash + 4] * u6 + GRADIENTS_6D[hash + 5] * v6);
        }

        n *= 64.000f;
        return n / (0.500f + Math.abs(t));
    }

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

    @Override
    public String getTag() {
        return "SiHN";
    }

    public String stringSerialize() {
        return "`" + seed + "`";
    }

    public SimplexNoiseHard stringDeserialize(String data) {
        seed = (DigitTools.longFromDec(data, 1, data.length() - 1));
        return this;
    }

    public static SimplexNoiseHard recreateFromString(String data) {
        return new SimplexNoiseHard(DigitTools.longFromDec(data, 1, data.length() - 1));
    }

    @Override
    public SimplexNoiseHard copy() {
        return new SimplexNoiseHard(seed);
    }

    @Override
    public String toString() {
        return "SimplexNoiseHard{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplexNoiseHard that = (SimplexNoiseHard) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }
}
