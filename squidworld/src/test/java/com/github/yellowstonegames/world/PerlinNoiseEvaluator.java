/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

package com.github.yellowstonegames.world;

/**
 * TL;DR: The range of D-dimensional Perlin noise is ±√(D / 4.0) .
 * Scale this by its inverse, so ±1.0 / √(D / 4.0) , to get our noise in the right range.
 * That is, according to <a href="https://digitalfreepen.com/2017/06/20/range-perlin-noise.html">this article</a>.
 */
public class PerlinNoiseEvaluator {
    public static void mainAn(String[] args) {
        final PerlinNoiseAnalysis analysis = new PerlinNoiseAnalysis(-5L);
//        analysis.analyzeGradCoord3D();
        analysis.analyzeExtremes3D();
    }
    public static void main(String[] args) {
        final PerlinNoiseAnalysis analysis = new PerlinNoiseAnalysis(-1234567890L);
//        {
//            @Override
//            public float gradCoord2D(long seed, int x, int y, float xd, float yd) {
//                final int hash = (x + y & 255) << 1;
//                return xd * GRADIENTS_2D[hash] + yd * GRADIENTS_2D[hash + 1];
//            }
//
//            @Override
//            public float gradCoord3D(long seed, int x, int y, int z, float xd, float yd, float zd) {
//                final int hash = (x + y + z & 31) << 2;
//                return (xd * GRADIENTS_3D[hash] + yd * GRADIENTS_3D[hash + 1] + zd * GRADIENTS_3D[hash + 2]);
//            }
//
//            @Override
//            public float gradCoord4D(long seed, int x, int y, int z, int w, float xd, float yd, float zd, float wd) {
//                final int hash = (x + y + z + w & 63) << 2;
//                return xd * GRADIENTS_4D[hash] + yd * GRADIENTS_4D[hash + 1] + zd * GRADIENTS_4D[hash + 2] + wd * GRADIENTS_4D[hash + 3];
//            }
//
//            @Override
//            public float gradCoord5D(long seed, int x, int y, int z, int w, int u, float xd, float yd, float zd, float wd, float ud) {
//                final int hash = (x + y + z + w + u & 255) << 3;
//                return xd * GRADIENTS_5D[hash] + yd * GRADIENTS_5D[hash + 1] + zd * GRADIENTS_5D[hash + 2]
//                        + wd * GRADIENTS_5D[hash + 3] + ud * GRADIENTS_5D[hash + 4];
//            }
//
//            @Override
//            public float gradCoord6D(long seed, int x, int y, int z, int w, int u, int v, float xd, float yd, float zd, float wd, float ud, float vd) {
//                final int hash = (x + y + z + w + u + v & 255) << 3;
//                return xd * GRADIENTS_6D[hash] + yd * GRADIENTS_6D[hash + 1] + zd * GRADIENTS_6D[hash + 2]
//                        + wd * GRADIENTS_6D[hash + 3] + ud * GRADIENTS_6D[hash + 4] + vd * GRADIENTS_6D[hash + 5];
//            }
//        };

        long startTime = System.currentTimeMillis();
        /*
To 1024f by steps of 0x1p-4f...

In 2D, Perlin:
Min: -0.8827633261681
Max: 0.8834772109985
         */
        for (float fx = 0f; fx < 1024f; fx += 0x1p-4f) {
            for (float fy = 0f; fy < 1024f; fy += 0x1p-4f) {
                analysis.getNoise(fx, fy);
            }
        }
        System.out.printf("In 2D, Perlin: \nMin: %.13f\nMax: %.13f\n", analysis.min2, analysis.max2);
/*
Iteration 508 in 2533.923 seconds.
In 3D, Perlin:
Min: -0.7905685305595
Max: 0.8010496497154
*/
        final float LIMIT = 256f, INC = 0x0.FFFCp-2f;
        for (float fx = -LIMIT; fx < LIMIT; fx += INC) {
            for (float fy = -LIMIT; fy < LIMIT; fy += INC) {
                for (float fz = -LIMIT; fz < LIMIT; fz += INC) {
                    analysis.getNoise(fx, fy, fz);
                }
            }
            if((int)(fx+0.5f) == ((int)(fx+0.5f) | 15))
                System.out.printf("In 3D, Perlin: \nMin: %.13f\nMax: %.13f\nIteration %f in %.3f seconds.\n", analysis.min3, analysis.max3, fx, (System.currentTimeMillis() - startTime) * 1E-3);
        }
        System.out.printf("FINAL: In 3D, Perlin: \nMin: %.13f\nMax: %.13f\n", analysis.min3, analysis.max3);
    }
}
