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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.tommyettinger.anim8.FastPNG;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.random.AceRandom;
import com.github.yellowstonegames.grid.BlueNoise;
import com.github.yellowstonegames.grid.Coord;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.ints.IntArrays;

import java.text.DateFormat;
import java.util.Date;

/**
 * Porting Bart Wronski's blue noise generator from NumPy to Java, see
 * <a href="https://bartwronski.com/2021/04/21/superfast-void-and-cluster-blue-noise-in-python-numpy-jax/">Wronski's
 * blog</a> for more on the original code. This version has been modified quite a bit.
 * <br>
 * This particular blue noise generator produces "omni-tiling" blue noise textures. Wang tiles have been investigated
 * before for blue noise point sets (<a href="http://johanneskopf.de/publications/blue_noise/">Kopf et al, 2006</a>),
 * but this approach effectively makes a Wang tiling where any left edge can tile seamlessly with any right edge, and
 * any top edge with any bottom edge. It also makes blue noise textures, rather than point sets, and these textures
 * satisfy the progressive quality of void-and-cluster blue noise. The generated textures have been incorporated into
 * {@link BlueNoise}, which also includes a sample way of quickly laying out textures
 * given a position and a seed in {@link BlueNoise#getSeeded(int, int, int)}.
 * <br>
 * The technical details of how this differs from void-and-cluster aren't too complex. Normally, void-and-cluster (as
 * Wronski implemented it) spreads out "energy" away from pixels it has selected, and that energy is used to determine
 * the next pixel to select (it picks the lowest-energy pixel remaining). We do that here too, but where regular
 * void-and-cluster used a single, toroidally-wrapping grid, here we split up the blue noise texture into "sectors,"
 * typically 16 or 64 of them in a grid, and we handle wrapping differently when it would cross between sectors. Where
 * energy spreads within a sector, it stays there and affects nothing else. Where energy crosses the edge between
 * sectors, such as the right edge of one sector spilling over into the left edge of a neighbor, the spread portion
 * actually extends across the corresponding edges of all sectors (here, all left edges). To avoid spreading much more
 * energy than normal, the dispersed energy is lessened; it is effectively divided by the number of sectors. There is
 * an extra step for this "Equal" Omni-Tiling generator, needed to ensure each resulting texture has the correct amount
 * of occurrences of each pixel value. It tracks how many times a sector has been given a value with the current
 * slowly-rising counter, and won't choose a sector to put energy into if it's already received its full allotment of
 * cells. The sector tracking is reset when the counter changes. The rest of the code is roughly the same as Wronski's;
 * we don't have access to Numpy or Jax, so we make do with jdkgdxds.
 * <br>
 * This makes use of some of the more unusual and powerful features in jdkgdxds; that's the reason this tool was moved
 * from SquidLib (which already has existing code to analyze blue noise, and so seemed like a nice fit) to SquidSquad.
 * The "energy" level of each pixel in the grid is tracked by an ObjectFloatOrderedMap with Coord keys. This map is
 * dense, with all 64x64 (or more) cells filled by Coord keys, and the reasons it's used are to pair points with their
 * energies, and because it is an Ordered, so it can be sorted. This code sorts the energy grid a lot; it uses the sort
 * to ensure that when a sector is denied another jolt of energy, the sector that does receive the jolt is the next-best
 * candidate.
 */
public class BlueNoiseFastOmniTilingGenerator extends ApplicationAdapter {

    /**
     * True if this should produce triangular-mapped blue noise.
     */
    private static final boolean isTriangular = true;

    private static final double sigma = 1.9, sigma2 = sigma * sigma;

    /**
     * Affects the size of the parent noise; typically 8 or 9 for a 256x256 or 512x512 parent image.
     */
    private static final int shift = 8;
    /**
     * Affects how many sectors are cut out of the full size; this is an exponent (with a base of 2).
     */
    private static final int sectorShift = 2;

    private static final int blockShift = shift - sectorShift;

    private static final int size = 1 << shift;
    private static final int sizeSq = size * size;
    private static final int sectors = 1 << sectorShift;
    private static final int totalSectors = sectors * sectors;
    private static final int sector = size >>> sectorShift;
    private static final int sectorSize = sector * sector;
    private static final int mask = size - 1;
    private static final int sectorMask = sector - 1;
    private static final int wrapMask = sector >>> 1;
    private static final float fraction = 1f / (totalSectors);
    private static final int lightOccurrenceBase = sizeSq >>> 8 + sectorShift + sectorShift;
    private static int lightOccurrence = lightOccurrenceBase;

    private final float[] energy = new float[sizeSq];
    private final float[][] lut = new float[sector][sector];
    private final int[] done = new int[sizeSq];
    private final int[] inv = ArrayTools.range(sizeSq);
    private Pixmap pm, pmSection;
    private FastPNG writer;
    private String path;
    private final int[] lightCounts = new int[sectors * sectors];

    private final AceRandom random = new AceRandom(1, 2, 3, 4, 5);

    private int get(final int x, final int y) {
        return x << shift | y;
    }

    private void update(final int x, final int y, final float value) {
        final int idx = x << shift | y;
        energy[idx] = value;
    }

    private void add(final int x, final int y, final float value) {
        final int idx = x << shift | y;
        energy[idx] += value;
    }

    @Override
    public void create() {
        // with triangular=true, shift=8, sectorShift=2:
        // using parallelRadixSortIndirect:
        // Took 75969ms to process.
        // using radixSortIndirect:
        // Took 82311ms to process.
        // using parallelQuickSortIndirect:
        // Took 42030ms to process.
        // using parallelQuickSortIndirect but also parallelQuickSort for post...
        // Took 59216ms to process.
        final long startTime = System.currentTimeMillis();
        Coord.expandPoolTo(size, size);
        String date = DateFormat.getDateInstance().format(new Date());
        path = "out/blueNoise/" + date + "_" + System.currentTimeMillis() + "/";
        random.setSeed(Hasher.bune.hashBulk64(date));
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pmSection = new Pixmap(sector, sector, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        writer = new FastPNG((int)(pm.getWidth() * pm.getHeight() * 2f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);

        final int hs = sector >>> 1;
        float[] column = new float[sector];
        for (int i = 1; i < hs; i++) {
            column[sector - i] = column[i] = (float) Math.exp(-0.5 * i * i / sigma2);
        }
        column[0] = 1f;
        for (int x = 0; x < sector; x++) {
            for (int y = 0; y < sector; y++) {
                lut[x][y] = column[x] * column[y];
            }
        }
        lut[0][0] = Float.MAX_VALUE;

        generate();
        getThresholdAndFFT(pm);
        System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to process.");
        Gdx.app.exit();
    }

    private void energize(int point) {
        final int pointX = point >>> shift, pointY = point & mask;
        final int outerX = pointX & ~sectorMask, outerY = pointY & ~sectorMask;
        for (int x = 0; x < sector; x++) {
            for (int y = 0; y < sector; y++) {
                if((pointX & sectorMask) <= x + wrapMask && (pointX & sectorMask) + wrapMask >= x &&
                        (pointY & sectorMask) <= y + wrapMask && (pointY & sectorMask) + wrapMask >= y)
                {
                    add(outerX + x, outerY + y, lut[x - pointX & sectorMask][y - pointY & sectorMask]);
                }
                else
                {
                    for (int ex = 0; ex < sectors; ex++) {
                        for (int ey = 0; ey < sectors; ey++) {
                            add((ex << blockShift) + x, (ey << blockShift) + y,
                                    lut[x - pointX & sectorMask][y - pointY & sectorMask] * fraction);
                        }
                    }
                }
            }
        }
    }

    public static int vdc(final int base, int index)
    {
        return (int)(MathTools.GOLDEN_LONGS[base & 1023] * index >>> 64 - shift);
//        if(base <= 2) {
//            return (Integer.reverse(index) >>> 32 - shift);
//        }
//        double denominator = base, res = 0.0;
//        while (index > 0)
//        {
//            res += (index % base) / denominator;
//            index /= base;
//            denominator *= base;
//        }
//        return (int) (res * sector);
    }

    public void generate()
    {
        long startTime = System.currentTimeMillis();

        final int limit = totalSectors << 3;
        int[] initial = new int[limit];
        int idx = 1;
        for (int i = 0; i < limit; i++) {
            int runningLimit = (i >>> sectorShift + sectorShift);
            int pt;
            do{
                pt = get(vdc(1, idx), vdc(2, idx));
                idx++;
                int loc = ((pt >>> shift) >>> blockShift) << sectorShift | (pt & mask) >>> blockShift;
                if(lightCounts[loc] <= runningLimit) {
                    lightCounts[loc]++;
                    break;
                }
            } while (true);
            initial[i] = pt;
        }
        int ctr = 0;

        for(int c : initial) {
            energize(c);
            done[c] = ctr++;
        }

        for (int n = sizeSq; ctr < n; ctr++) {
            if((ctr & (lightOccurrenceBase << sectorShift + sectorShift) - 1) == 0) {
                lightOccurrence += lightOccurrenceBase;
                System.out.println("Completed " + ctr + " out of " + n + " in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
            //Took 5714ms to generate. (7,3)
//            order.sortJDK((a, b) -> Float.floatToIntBits(energy.get(a) - energy.get(b)));
            // parallel is only faster with large shifts, like 10
//            FloatArrays.parallelRadixSortIndirect(inv, energy, true);
//            FloatArrays.radixSortIndirect(inv, energy, true);
            FloatArrays.parallelQuickSortIndirect(inv, energy);
            int low = inv[0];
            int k = 1;
            while(lightCounts[((low>>>shift) >>> blockShift) << sectorShift | ((low&mask) >>> blockShift)] >= lightOccurrence){
                low = inv[k++];
            }
            lightCounts[((low>>>shift) >>> blockShift) << sectorShift | ((low&mask) >>> blockShift)]++;
            energize(low);
            done[low] = ctr;

        }

        int[] histogram = new int[256];

        if(isTriangular) {
            int[] colorMap = new int[sectorSize];
            int span = 1, lastIndex = colorMap.length - 1;
            for(int i = 0, inner = 0; i < 128; i++) {
                for (int j = 0; j < span; j++) {
                    colorMap[inner] = i * 0x01010100 | 0xFF;
                    colorMap[lastIndex - inner] = (255 - i) * 0x01010100 | 0xFF;
                    inner++;
                }
                int boost = -1;
                for (int j = blockShift, s = 0; j < 7; j++, s += 2) {
                    boost &= i >>> s & i >>> s + 1;
                }
                span += (-63 + i | 63 - i) >>> 31 & boost;
            }

            IntArrays.unstableSort(inv, (a, b) ->
                    (((a>>>shift) >>> blockShift) << sectorShift | ((a&mask) >>> blockShift))
                    - (((b>>>shift) >>> blockShift) << sectorShift | ((b&mask) >>> blockShift))
            );
            for (int from = 0; from < sizeSq; from += sectorSize) {
                random.shuffle(inv, from, sectorSize);
                IntArrays.stableSort(inv, from, from + sectorSize, (a, b) -> done[a] - done[b]);
                for (int i = 0; i < sectorSize; i++) {
                    final int pt = inv[from + i];
                    final int color = colorMap[i];
                    pm.drawPixel(pt>>>shift, pt&mask, color);
                    histogram[color>>>24]++;
                }
            }
        }
        else {
            IntArrays.unstableSort(inv, (a, b) ->
                    (((a>>>shift) >>> blockShift) << sectorShift | ((a&mask) >>> blockShift))
                            - (((b>>>shift) >>> blockShift) << sectorShift | ((b&mask) >>> blockShift))
            );
            for (int from = 0; from < sizeSq; from += sectorSize) {
                random.shuffle(inv, from, sectorSize);
                IntArrays.stableSort(inv, from, from + sectorSize, (a, b) -> done[a] - done[b]);
                for (int i = 0; i < sectorSize; i++) {
                    final int pt = inv[from+i];
                    final float r = (i * (256f / sectorSize));
                    final int level = ((int) (r) & 0xFF);
                    histogram[level]++;
                    pm.drawPixel(pt>>>shift, pt&mask, level * 0x01010100 | 0xFF);
                }
            }
        }

        System.out.println("HISTOGRAM:");
        for (int i = 0; i < 256; i++) {
            System.out.printf("%3d: %d\n", i, histogram[i]);
        }

        String name = path + "BlueNoise" + (isTriangular ? "TriFast" : "Fast") + "Tiling";
        writer.write(Gdx.files.local(name + size + "x" + size + ".png"), pm);
        for (int y = 0; y < sectors; y++) {
            for (int x = 0; x < sectors; x++) {
                pmSection.drawPixmap(pm, x * sector, y * sector, sector, sector, 0, 0, sector, sector);
                writer.write(Gdx.files.local(name + "_" + x + "x" + y + ".png"), pmSection);
            }
        }

        System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to generate.");
    }

    public void getThresholdAndFFT(Pixmap pm) {
        final double[][] real = new double[size][size], imag = new double[size][size];
        final float[][] colors = new float[size][size];
        for (int threshold = 0; threshold < 255; threshold++) {
            ArrayTools.fill(imag, 0.0);
            Pixmap thr = new Pixmap(size, size, Pixmap.Format.RGBA8888);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    int ic = pm.getPixel(x & pm.getWidth() - 1, y & pm.getHeight() - 1);
                    if((ic >>> 24) <= threshold) {
                        real[x][y] = 1f;
                        thr.drawPixel(x, y, -1); // white
                    }
                    else {
                        real[x][y] = 0f;
                        thr.drawPixel(x, y, 255); // black
                    }
                }
            }
            String name = path + "Threshold" + threshold + (isTriangular ? "TriFast" : "Fast") + "_";

            writer.write(Gdx.files.local(name + "BW" + size + "x" + size + ".png"), thr);

            Fft.transform2D(real, imag);
            Fft.getColors(real, imag, colors);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    thr.drawPixel(x, y, BitConversion.floatToReversedIntBits(colors[x][y]));
                }
            }
            writer.write(Gdx.files.local(name + "FFT" + size + "x" + size + ".png"), thr);
            thr.dispose();

        }
    }

    @Override
    public void dispose() {
        super.dispose();
        pm.dispose();
        pmSection.dispose();
        writer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Tool: LARGE Blue Noise Tiling Generator");
        config.setWindowedMode(size, size);
        config.disableAudio(true);
        new Lwjgl3Application(new BlueNoiseFastOmniTilingGenerator(), config);
    }
}
