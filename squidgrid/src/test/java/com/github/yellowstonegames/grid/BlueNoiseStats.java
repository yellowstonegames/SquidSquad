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

package com.github.yellowstonegames.grid;

public class BlueNoiseStats {
    public static void main(String[] args){
        int[][] distances = new int[128][256];
        double[] sums = new double[128];
        int[] counts = new int[128];
        int[] histogram = new int[256];
        byte[][] tileNoise = BlueNoise.TILE_NOISE;
        for (int j = 0, tileNoiseLength = tileNoise.length; j < tileNoiseLength; j++) {
            byte[] bn = tileNoise[j];
            for (int x = 0, i = 0; x < 128; x++) {
                for (int y = 0; y < 128; y++, i++) {
                    int v = bn[i] + 128;
                    histogram[v]++;
                    int dist = Math.min(Math.abs(x + x - 127), Math.abs(y + y - 127));
                    counts[dist]++;
                    distances[dist][v]++;
                }

            }
        }
        for (int i = 1; i < 128; i+=2) {
            for (int v = 0; v < 256; v++) {
                sums[i] += distances[i][v] / (double)counts[i];
            }
            System.out.println("Distance " + i + ": total is " + sums[i]);
        }
        for (int v = 0; v < 256; v++) {
//            double total = 0.0;
//            for (int d = 1; d < 128; d += 2) {
//                total += distances[d][v] / (double) counts[d];
//            }
//            System.out.println("Value " + v + ": total is " + total);
            System.out.println("Value " + v + " appeared " + histogram[v] + " times.");
        }
    }
}
