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

public class BlueNoiseStats {
    public static void main(String[] args){
        int[][] distances = new int[64][256];
        double[] sums = new double[64];
        int[] counts = new int[64];
        byte[][] tileNoise = BlueNoise.TILE_NOISE;
        for (int j = 0, tileNoiseLength = tileNoise.length; j < tileNoiseLength; j++) {
            byte[] bn = tileNoise[j];
            for (int x = 0, i = 0; x < 64; x++) {
                for (int y = 0; y < 64; y++, i++) {
                    int v = bn[i] + 128;
                    int dist = Math.min(Math.abs(x + x - 63), Math.abs(y + y - 63));
                    counts[dist]++;
                    distances[dist][v]++;
                }

            }
        }
        for (int i = 1; i < 64; i+=2) {
            for (int v = 0; v < 256; v++) {
                sums[i] += distances[i][v] / (double)counts[i];
            }
            System.out.println("Distance " + i + ": total is " + sums[i]);
        }
    }
}
