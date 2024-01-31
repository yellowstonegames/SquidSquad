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

import com.github.tommyettinger.ds.IntIntOrderedMap;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntOrderedMap;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.digital.Hasher;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Arrays;

/**
 * A port of WaveFunctionCollapse by ExUtumno/mxgmn; takes a single sample of a grid to imitate and produces one or more
 * grids of requested sizes that have a similar layout of cells to the sample. Samples are given as {@code int[][]}
 * where an int is usually an index into an array, list, {@link com.github.tommyettinger.ds.NumberedSet}, or similar
 * indexed collection of items (such as char values or colors) that would be used instead of an int directly. The
 * original WaveFunctionCollapse code, <a href="https://github.com/mxgmn/WaveFunctionCollapse">here</a>, used colors in
 * bitmap images, but this uses 2D int arrays that can stand as substitutes for colors or chars.
 * <br>
 * Port of <a href="https://github.com/mxgmn/WaveFunctionCollapse">mxgmn's original C# repo</a>.
 */
public class WaveFunctionCollapse {
    private boolean[][] wave;

    private int[][][] propagator;
    private int[][][] compatible;
    private int[] observed;

    private int[] stack;
    private int stacksize;

    @NonNull public EnhancedRandom random;
    private int targetWidth, targetHeight, totalOptions;
    private boolean periodic;

    private double[] baseWeights;
    private double[] weightLogWeights;

    private int[] sumsOfOnes;
    private double sumOfWeights, sumOfWeightLogWeights, startingEntropy;
    private double[] sumsOfWeights, sumsOfWeightLogWeights, entropies;


    private int order;
    private int[][] patterns;
    private IntIntOrderedMap choices;
    private int ground;

    /**
     * Creates a WaveFunctionCollapse that will imitate the given {@code itemGrid}. This will be able to produce 2D int
     * arrays of size {@code width} by {@code height}, using the same content as present in itemGrid. The {@code order}
     * should usually be 2 (low-quality), 3 (normal quality), or sometimes 4 (possibly-too-high quality); it determines
     * how far away from a given cell in itemGrid to consider when placing that cell's value in a result. This acts as
     * if {@code periodicInput} is true, making the top row to be considered adjacent to the bottom, and likewise for
     * the left column and right column. This will not produce tiling output; to do that, use the larger constructor and
     * pass true to {@code periodicOutput}. The {@code symmetry} value will be treated as 1, and {@code ground} as 0.
     * @param itemGrid the initial data to imitate; this must not be empty
     * @param order how far away from a cell to look when determining what value it should have; usually 2, 3, or 4
     * @param width the width of the output 2D array to produce
     * @param height the height of the output 2D array to produce
     */
    public WaveFunctionCollapse(int[][] itemGrid, int order, int width, int height, EnhancedRandom random){
        this(itemGrid, order, width, height, random, true, false, 1, 0);
    }
    /**
     * Creates a WaveFunctionCollapse that will imitate the given {@code itemGrid}. This will be able to produce 2D int
     * arrays of size {@code width} by {@code height}, using the same content as present in itemGrid. The {@code order}
     * should usually be 2 (low-quality), 3 (normal quality), or sometimes 4 (possibly-too-high quality); it determines
     * how far away from a given cell in itemGrid to consider when placing that cell's value in a result. You will
     * probably want to try different values for {@code periodicInput} and {@code periodicOutput}. If periodicInput is
     * true, then the top row is considered adjacent to the bottom, and likewise for the left column and right column.
     * If periodicOutput is true, this will spend much more time trying to make all the edges match up so the result can
     * tile seamlessly. The {@code symmetry} value will be clamped between 1 and 8, inclusive; if 1, only the tiles in
     * their current state will be considered, but if it is greater, more and more reflections and rotations of areas in
     * itemGrid will be considered. I don't really know what {@code ground} does, and it should probably be 0.
     * @param itemGrid the initial data to imitate; this must not be empty
     * @param order how far away from a cell to look when determining what value it should have; usually 2, 3, or 4
     * @param width the width of the output 2D array to produce
     * @param height the height of the output 2D array to produce
     * @param random the EnhancedRandom number generator to use; will be referenced directly, not copied.
     * @param periodicInput if true, {@code itemGrid} is treated as wrapping from edge to opposite edge
     * @param periodicOutput if true, the output will tile if repeated; may make generation much more difficult
     * @param symmetry the level of symmetry to consider when imitating areas; between 1 and 8, inclusive, but usually 1
     * @param ground not sure what this does, to be honest; should usually be 0
     */
    public WaveFunctionCollapse(int[][] itemGrid, int order, int width, int height, @NonNull EnhancedRandom random,
                                boolean periodicInput, boolean periodicOutput, int symmetry, int ground)
    {
        targetWidth = width;
        targetHeight = height;

        this.order = order;
        periodic = periodicOutput;

        this.random = random;

        symmetry = Math.min(Math.max(symmetry, 1), 8);

        int sampleWidth = itemGrid.length, sampleHeight = itemGrid[0].length;
        choices = new IntIntOrderedMap(sampleWidth * sampleHeight);
        int[][] sample = new int[sampleWidth][sampleHeight];
        for (int y = 0; y < sampleHeight; y++) {
            for (int x = 0; x < sampleWidth; x++)
            {
                int color = itemGrid[x][y];
                int i = choices.getOrDefault(color, choices.size());
                if(i == choices.size())
                    choices.put(color, i);
                sample[x][y] = i;
            }
        }

        ObjectIntOrderedMap<int[]> weights = new ObjectIntOrderedMap<int[]>(){
            @Override
            protected int place(@NonNull Object item) {
                return item instanceof int[] ? Hasher.focalor.hash((int[])item) & mask : super.place(item);
            }

            @Override
            protected int locateKey(@NonNull Object key) {
                if(key instanceof int[])
                {
                    Object[] keyTable = this.keyTable;
                    for (int i = place(key); ; i = i + 1 & mask) {
                        int[] other = (int[]) keyTable[i];
                        if (other == null)
                            return ~i; // Always negative; means empty space is available at i.
                        if (Arrays.equals((int[]) key, other)) // If you want to change how equality is determined, do it here.
                            return i; // Same key was found.
                    }
                }
                else return super.locateKey(key);
            }
        };

        for (int y = 0; y < (periodicInput ? sampleHeight : sampleHeight - order + 1); y++) {
            for (int x = 0; x < (periodicInput ? sampleWidth : sampleWidth - order + 1); x++) {
                int[][] ps = new int[8][];

                ps[0] = patternFromSample(x, y, sample, sampleWidth, sampleHeight);
                ps[1] = reflect(ps[0]);
                ps[2] = rotate(ps[0]);
                ps[3] = reflect(ps[2]);
                ps[4] = rotate(ps[2]);
                ps[5] = reflect(ps[4]);
                ps[6] = rotate(ps[4]);
                ps[7] = reflect(ps[6]);

                for (int k = 0; k < symmetry; k++) {
                    int[] ind = ps[k];
                    weights.put(ind, weights.getOrDefault(ind, 0) + 1);
                }
            }
        }
        totalOptions = weights.size();
        this.ground = (ground + totalOptions) % totalOptions;
        patterns = new int[totalOptions][];
        baseWeights = new double[totalOptions];

        for (int w = 0; w < totalOptions; w++) {
            patterns[w] = weights.keyAt(w);
            baseWeights[w] = weights.getAt(w);
        }
        

        propagator = new int[4][][];
        IntList list = new IntList(totalOptions);
        for (int d = 0; d < 4; d++)
        {
            propagator[d] = new int[totalOptions][];
            for (int t = 0; t < totalOptions; t++)
            {
                list.clear();
                for (int t2 = 0; t2 < totalOptions; t2++) if (agrees(patterns[t], patterns[t2], DX[d], DY[d])) list.add(t2);
                propagator[d][t] = list.toArray();
            }
        }
    }

    private int[] patternFromSample(int x, int y, int[][] sample, int sampleWidth, int sampleHeight) {
        int[] result = new int[order * order];
        for (int dy = 0; dy < order; dy++) {
            for (int dx = 0; dx < order; dx++) {
                result[dx + dy * order] = sample[(x + dx) % sampleWidth][(y + dy) % sampleHeight];
            }
        }
        return result;
    }
    private int[] rotate(int[] p)
    {
        int[] result = new int[order * order];
        for (int y = 0; y < order; y++) {
            for (int x = 0; x < order; x++){
                result[x + y * order] = p[order - 1 - y + x * order];
            }
        }
        return result;
    }
    private int[] reflect(int[] p)
    {
        int[] result = new int[order * order];
        for (int y = 0; y < order; y++) {
            for (int x = 0; x < order; x++){
                result[x + y * order] = p[order - 1 - x + y * order];
            }
        }
        return result;
    }
    private boolean agrees(int[] p1, int[] p2, int dx, int dy)
    {
        int xmin = Math.max(dx, 0), xmax = dx < 0 ? dx + order : order,
                ymin = Math.max(dy, 0), ymax = dy < 0 ? dy + order : order;
        for (int y = ymin; y < ymax; y++) {
            for (int x = xmin; x < xmax; x++) {
                if (p1[x + order * y] != p2[x - dx + order * (y - dy)])
                    return false;
            }
        }
        return true;
    }

    private void init()
    {
        wave = new boolean[targetWidth * targetHeight][];
        compatible = new int[wave.length][][];
        for (int i = 0; i < wave.length; i++)
        {
            wave[i] = new boolean[totalOptions];
            compatible[i] = new int[totalOptions][];
            for (int t = 0; t < totalOptions; t++) compatible[i][t] = new int[4];
        }

        weightLogWeights = new double[totalOptions];
        sumOfWeights = 0;
        sumOfWeightLogWeights = 0;

        for (int t = 0; t < totalOptions; t++)
        {
            weightLogWeights[t] = baseWeights[t] * Math.log(baseWeights[t]);
            sumOfWeights += baseWeights[t];
            sumOfWeightLogWeights += weightLogWeights[t];
        }

        startingEntropy = Math.log(sumOfWeights) - sumOfWeightLogWeights / sumOfWeights;

        sumsOfOnes = new int[targetWidth * targetHeight];
        sumsOfWeights = new double[targetWidth * targetHeight];
        sumsOfWeightLogWeights = new double[targetWidth * targetHeight];
        entropies = new double[targetWidth * targetHeight];

        stack = new int[wave.length * totalOptions << 1];
        stacksize = 0;
    }

    private int observe()
    {
        double min = 1E+3;
        int argmin = -1;

        for (int i = 0; i < wave.length; i++)
        {
            if (onBoundary(i % targetWidth, i / targetWidth)) continue;

            int amount = sumsOfOnes[i];
            if (amount == 0) return 0;

            double entropy = entropies[i];
            if (amount > 1 && entropy <= min)
            {
                double noise = 1E-6 * random.nextDouble();
                if (entropy + noise < min)
                {
                    min = entropy + noise;
                    argmin = i;
                }
            }
        }

        if (argmin == -1)
        {
            observed = new int[targetWidth * targetHeight];
            for (int i = 0; i < wave.length; i++) {
                for (int t = 0; t < totalOptions; t++) {
                    if (wave[i][t]) { 
                        observed[i] = t;
                        break;
                    }
                }
            }
            return 1;
        }

        double[] distribution = new double[totalOptions];
        double sum = 0.0, x = 0.0;
        for (int t = 0; t < totalOptions; t++)
        {
            sum += (distribution[t] = wave[argmin][t] ? baseWeights[t] : 0);
        }
        int r = 0;
        sum = random.nextDouble(sum);
        for (; r < totalOptions; r++) {
            if((x += distribution[r]) > sum)
                break;
        }

        boolean[] w = wave[argmin];
        for (int t = 0; t < totalOptions; t++){
            if (w[t] != (t == r))
                ban(argmin, t);
        }

        return -1;
    }

    private void propagate()
    {
        while (stacksize > 0)
        {
            int i1 = stack[stacksize - 2], e2 = stack[stacksize - 1];
            stacksize -= 2;
            int x1 = i1 % targetWidth, y1 = i1 / targetWidth;

            for (int d = 0; d < 4; d++)
            {
                int dx = DX[d], dy = DY[d];
                int x2 = x1 + dx, y2 = y1 + dy;
                if (onBoundary(x2, y2)) continue;

                if (x2 < 0) x2 += targetWidth;
                else if (x2 >= targetWidth) x2 -= targetWidth;
                if (y2 < 0) y2 += targetHeight;
                else if (y2 >= targetHeight) y2 -= targetHeight;

                int i2 = x2 + y2 * targetWidth;
                int[] p = propagator[d][e2];
                int[][] compat = compatible[i2];

                for (int l = 0; l < p.length; l++)
                {
                    int t2 = p[l];
                    int[] comp = compat[t2];

                    comp[d]--;
                    if (comp[d] == 0) ban(i2, t2);
                }
            }
        }
    }
    /**
     * Try to actually generate a result, taking {@code limit} tries at most (0 or less for unlimited). Returns true if
     * a result was found successfully. The limit should usually be either 0 or a fairly high number (at least 100) if
     * you want a result from this, though you can call this many times before it gets a successful result. This takes
     * a {@code seed} as a long, but you are likely to get comparable results with {@link #run(int)} if the state of
     * {@link #random} is deterministic before this is called.
     * @param seed used with {@link #random}, passed to {@link EnhancedRandom#setSeed(long)} before this starts attempts
     * @param limit how many attempts to allow; may be 0 or less for unlimited attempts. A common idiom is:
     * <code>
     *         long seed = 1L; // can be any initial seed
     *         while (!wfc.run(seed++, 0)); // This tries until there is a success, which may never happen.
     *         int[][] resultGrid = wfc.result();
     * </code>
     * @return true if this found a result successfully, or false if it did not
     */
    public boolean run(long seed, int limit)
    {
        if (wave == null) init();

        clear();
        random.setSeed(seed);

        for (int l = 0; l < limit || limit == 0; l++)
        {
            int result = observe();
            if (result >= 0) return result == 1;
            propagate();
        }

        return false;
    }

    /**
     * Try to actually generate a result, taking {@code limit} tries at most (0 or less for unlimited). Returns true if
     * a result was found successfully. The limit should usually be either 0 or a fairly high number (at least 100) if
     * you want a result from this, though you can call this many times before it gets a successful result.
     * @param limit how many attempts to allow; may be 0 or less for unlimited attempts. A common idiom is:
     * <code>
     *         while (!wfc.run(0)); // This tries until there is a success, which may never happen.
     *         int[][] resultGrid = wfc.result();
     * </code>
     * @return true if this found a result successfully, or false if it did not
     */
    public boolean run(int limit)
    {
        if (wave == null) init();

        clear();

        for (int l = 0; l < limit || limit <= 0; l++)
        {
            int result = observe();
            if (result >= 0) return result == 1;
            propagate();
        }

        return false;
    }

    private void ban(int i, int t)
    {
        wave[i][t] = false;

        int[] comp = compatible[i][t];
        for (int d = 0; d < 4; d++) comp[d] = 0;
        stack[stacksize++] = i;
        stack[stacksize++] = t;

        double sum = sumsOfWeights[i];
        entropies[i] += sumsOfWeightLogWeights[i] / sum - Math.log(sum);

        sumsOfOnes[i] -= 1;
        sumsOfWeights[i] -= baseWeights[t];
        sumsOfWeightLogWeights[i] -= weightLogWeights[t];

        sum = sumsOfWeights[i];
        entropies[i] -= sumsOfWeightLogWeights[i] / sum - Math.log(sum);
    }


    private boolean onBoundary(int x, int y) {
        return !periodic && (x + order > targetWidth || y + order > targetHeight || x < 0 || y < 0);
    }

    public int[][] result()
    {
        int[][] result = new int[targetWidth][targetHeight];

        if (observed != null)
        {
            for (int y = 0; y < targetHeight; y++)
            {
                int dy = y < targetHeight - order + 1 ? 0 : order - 1;
                for (int x = 0; x < targetWidth; x++)
                {
                    int dx = x < targetWidth - order + 1 ? 0 : order - 1;
                    result[x][y] = choices.keyAt(patterns[observed[x - dx + (y - dy) * targetWidth]][dx + dy * order]);
                }
            }
        }
        return result;
    }

    private void clear()
    {
        for (int i = 0; i < wave.length; i++)
        {
            for (int t = 0; t < totalOptions; t++)
            {
                wave[i][t] = true;
                for (int d = 0; d < 4; d++) compatible[i][t][d] = propagator[OPPOSITE[d]][t].length;
            }

            sumsOfOnes[i] = baseWeights.length;
            sumsOfWeights[i] = sumOfWeights;
            sumsOfWeightLogWeights[i] = sumOfWeightLogWeights;
            entropies[i] = startingEntropy;
        }


        if (ground != 0)
        {
            for (int x = 0; x < targetWidth; x++)
            {
                for (int t = 0; t < totalOptions; t++) if (t != ground) ban(x + (targetHeight - 1) * targetWidth, t);
                for (int y = 0; y < targetHeight - 1; y++) ban(x + y * targetWidth, ground);
            }

            propagate();
        }
    }
    private static final int[] DX = { -1, 0, 1, 0 };
    private static final int[] DY = { 0, 1, 0, -1 };
    private static final int[] OPPOSITE = { 2, 3, 0, 1 };

}
