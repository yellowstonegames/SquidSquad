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
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.sort.ObjectComparators;
import com.github.tommyettinger.random.AceRandom;
import com.github.yellowstonegames.grid.BlueNoise;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.grid.Coord;

import com.github.yellowstonegames.grid.CoordFloatOrderedMap;

import java.text.DateFormat;
import java.util.*;

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
public class BlueNoiseEqualOmniTilingGenerator extends ApplicationAdapter {

    /**
     * True if this should produce triangular-mapped blue noise.
     */
    private static final boolean isTriangular = false;

    private static final double sigma = 1.9, sigma2 = sigma * sigma;

    /**
     * Affects the size of the parent noise; typically 8 or 9 for a 256x256 or 512x512 parent image.
     */
    private static final int shift = 8;
    /**
     * Affects how many sectors are cut out of the full size; this is an exponent (with a base of 2).
     */
    private static final int sectorShift = 1;

    private static final int blockShift = shift - sectorShift;

    private static final int size = 1 << shift;
    private static final int sizeSq = size * size;
    private static final int sectors = 1 << sectorShift;
    private static final int totalSectors = sectors * sectors;
    private static final int sector = size >>> sectorShift;
    private static final int sectorSize = sector * sector;
    private static final int mask = size - 1;
    private static final int sectorMask = sector - 1;
    private static final int sectorExponent = Integer.bitCount(sectorMask);
//    private static final int wrapMask = sector >>> 3;
//    private static final int wrapMask = sector * 5 >>> 5;
//    private static final int wrapMask = sector * 13 >>> 5;
    private static final int wrapMask = sector >>> 1;
    private static final float fraction = 1f / (totalSectors);
//    private static final float fraction = 1f / (totalSectors * 2f);
//    private static final float fraction = 1f / (totalSectors * 4f);
    private static int lightOccurrenceBase = sizeSq >>> 8 + sectorShift + sectorShift;
    private static int lightOccurrence = lightOccurrenceBase;
    private static final int triAdjust = Integer.numberOfTrailingZeros(sizeSq >>> 8 + sectorShift + sectorShift);

    private final CoordFloatOrderedMap energy = new CoordFloatOrderedMap(sizeSq, 0.5f);
//    private final ObjectFloatOrderedMap<Coord> energy = new ObjectFloatOrderedMap<Coord>(sizeSq, 0.5f)
//    { // OK, we're making an anonymous subclass of ObjectFloatOrderedMap so its hashing function is faster.
//      // It may also make it collide less, but the computation is much simpler here than the default.
//      // This makes a roughly 3x difference in runtime. (!)
//        @Override
//        protected int place(@NonNull Object item) {
//            final int x = ((Coord)item).x, y = ((Coord)item).y;
//            // Cantor pairing function
//            return y + ((x + y) * (x + y + 1) >> 1) & mask;
//        }
//
//        @Override
//        protected boolean equate(@NonNull Object left, @Nullable Object right) {
//            return left == right;
//        }
//    };
    private final float[][] lut = new float[sector][sector];
    private final int[][] done = new int[size][size];
    private Pixmap pm, pmSection;
    private FastPNG writer;
    private String path;
    private final int[] lightCounts = new int[sectors * sectors];

    private final AceRandom random = new AceRandom(1, 2, 3, 4, 5);
    @Override
    public void create() {
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
//        lut[0][0] = Float.POSITIVE_INFINITY;

        generate();
        getThresholdAndFFT(pm);
//        getThresholdAndFFT(new Pixmap(Gdx.files.local("2025/BlueNoiseOmniTiling512x512.png")));
        Gdx.app.exit();
    }

    private void energize(Coord point) {
        final int outerX = point.x & ~sectorMask, outerY = point.y & ~sectorMask;
        for (int x = 0; x < sector; x++) {
            for (int y = 0; y < sector; y++) {
                if((point.x & sectorMask) <= x + wrapMask && (point.x & sectorMask) + wrapMask >= x &&
                        (point.y & sectorMask) <= y + wrapMask && (point.y & sectorMask) + wrapMask >= y)
                {
                    energy.getAndIncrement(Coord.get(outerX + x, outerY + y),
                            0f, lut[x - point.x & sectorMask][y - point.y & sectorMask]);
                }
                else
                {
                    for (int ex = 0; ex < sectors; ex++) {
                        for (int ey = 0; ey < sectors; ey++) {
//                            energy.getAndIncrement(Coord.get((ex << blockShift) + x, (ey << blockShift) + y),
//                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * fraction + (random.next(1) - 0.5f) * 0x1p-32f);

                            energy.getAndIncrement(Coord.get((ex << blockShift) + x, (ey << blockShift) + y),
                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * fraction);

//                            energy.getAndIncrement(Coord.get((ex << blockShift) + x, (ey << blockShift) + sectorMask - y),
//                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * fraction);
//                            energy.getAndIncrement(Coord.get((ex << blockShift) + sectorMask - x, (ey << blockShift) + y),
//                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * fraction);
//                            energy.getAndIncrement(Coord.get((ex << blockShift) + sectorMask - x, (ey << blockShift) + sectorMask - y),
//                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * fraction);
                        }
                    }
                }
            }
        }
    }

    public static int vdc(final int base, int index)
    {
//        return QuasiRandomTools.vanDerCorput(base, index ^ index >>> 1);
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
//        int[] positions = ArrayTools.range(limit);
//        for (int i = 0; i <= limit - totalSectors; i += totalSectors) {
//            rng.shuffle(positions, i, totalSectors);
//        }
        Coord[] initial = new Coord[limit];
//        final int xOff = rng.next(blockShift), yOff = rng.next(blockShift);
        final int xOff = 0, yOff = 0;
        int idx = 1;
        for (int i = 0; i < limit; i++) {
            int runningLimit = (i >>> sectorShift + sectorShift);
//            int sz = positions[i - 1];
//            final Coord pt = Coord.get((vdc(1, i+1) + xOff & sectorMask) + ((i & sectors - 1) << blockShift),
//                    (vdc(2, i+1) + yOff & sectorMask) + (((i >>> sectorShift) & sectors - 1) << blockShift) );
            Coord pt;
            do{
                pt = Coord.get(vdc(1, idx), vdc(2, idx));
                idx++;
                int loc = (pt.x >>> blockShift) << sectorShift | pt.y >>> blockShift;
                if(lightCounts[loc] <= runningLimit) {
                    lightCounts[loc]++;
                    break;
                }
            } while (true);
            initial[i] = pt;
        }
//        CoordOrderedSet initial = new CoordOrderedSet(limit);
//        final int xOff = rng.next(sector), yOff = rng.next(sector);
//        for (int i = 1; initial.size() < limit; i++) {
//            int gray = initial.size();
//            final Coord pt = Coord.get((vdc(7, i) + xOff & sectorMask) + ((gray & sectors - 1) << blockShift),
//                    (vdc(3, i) + yOff & sectorMask) + (((gray >>> sectorShift) & sectors - 1) << blockShift) );
//            initial.add(pt);
//        }
        //// removed because it messes up the initial ordering; could be added back if it shuffled in groups of (sectors * sectors).
//        rng.shuffle(initial);
        energy.clear();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                energy.put(Coord.get(x, y), 0f);
            }
        }
        ArrayTools.fill(done, 0);
        int ctr = 0;

        for(Coord c : initial) {
            energize(c);
            done[c.x][c.y] = ctr++;
        }

        for (int n = sizeSq; ctr < n; ctr++) {
            if((ctr & (lightOccurrenceBase << sectorShift + sectorShift) - 1) == 0) {
                lightOccurrence += lightOccurrenceBase;
                System.out.println("Completed " + ctr + " out of " + n + " in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
//            energy.shuffle(rng);
            //Took 10655950ms to generate. (9,3)
            //Took 9109ms to generate. (7,3)
//            energy.sortByValue((o1, o2) -> Float.floatToIntBits(o1 - o2));
            //Took 5714ms to generate. (7,3)
            energy.order().sortJDK((a, b) -> Float.floatToIntBits(energy.get(a) - energy.get(b)));
            int k = 1;
            Coord low = energy.keyAt(0);
//            Coord low = energy.selectRanked((o1, o2) -> Float.compare(energy.getOrDefault(o1, 0f), energy.getOrDefault(o2, 0f)), 1);
            while(lightCounts[(low.x >>> blockShift) << sectorShift | (low.y >>> blockShift)] >= lightOccurrence){
                low = energy.keyAt(k++);
//                low = energy.selectRanked((o1, o2) -> Float.compare(energy.getOrDefault(o1, 0f), energy.getOrDefault(o2, 0f)), ++k);
            }
            lightCounts[(low.x >>> blockShift) << sectorShift | (low.y >>> blockShift)]++;
            energize(low);
            done[low.x][low.y] = ctr;
        }

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

            ObjectList<Coord> order = energy.order();
            order.sortJDK(Comparator.comparingInt(a -> ((a.x >>> blockShift) << sectorShift | (a.y >>> blockShift))));
            for (int from = 0; from < sizeSq; from += sectorSize) {
                random.shuffle(order, from, sectorSize);
                ObjectComparators.sort(order, from, from + sectorSize, (a, b) -> done[a.x][a.y] - done[b.x][b.y]);
                for (int i = 0; i < sectorSize; i++) {
                    Coord pt = order.get(from + i);
                    pm.drawPixel(pt.x, pt.y, colorMap[i]);
                }
//                for (int i = 0, sSize = sub.size() - 1; i <= sSize; i++) {
//                    Coord pt = sub.get(i);
//                    double r = (i * (1.0 / sSize));
//                    r = ((r > 0.5) ? 1.0 - Math.sqrt(2.0 - 2.0 * r) : Math.sqrt(2.0 * r) - 1.0) * 127.5 + 128.0;
//                    pm.drawPixel(pt.x, pt.y, ((int) (r) & 0xFF) * 0x01010100 | 0xFF);
//                }
            }
//            final int toKindaByteShift = Math.max(0, shift + shift - 8 - triAdjust);
//
//            for (int x = 0; x < size; x++) {
//                for (int y = 0; y < size; y++) {
//                    float r = (done[x][y]) * (1f / (sizeSq - 1f));
////                    float r = (done[x][y] >>> toKindaByteShift) * (1f / ((1 << 8 + triAdjust) - 1));
//
//                    r = ((r > 0.5f) ? 1f - (float)Math.sqrt(2f - 2f * r) : (float)Math.sqrt(2f * r) - 1f) * 127.5f + 127.5f;
//                    buffer.putInt(((int)(r) & 0xFF) * 0x01010100 | 0xFF);
//                }
//            }
        }
        else {
            ObjectList<Coord> order = energy.order();
            order.sortJDK(Comparator.comparingInt(a -> ((a.x >>> blockShift) << sectorShift | (a.y >>> blockShift))));
            for (int from = 0; from < sizeSq; from += sectorSize) {
                random.shuffle(order, from, sectorSize);
                ObjectComparators.sort(order, from, from + sectorSize, (a, b) -> done[a.x][a.y] - done[b.x][b.y]);
                for (int i = 0; i < sectorSize; i++) {
                    Coord pt = order.get(from+i);
                    float r = (i * (256f / sectorSize));
                    pm.drawPixel(pt.x, pt.y, ((int) (r) & 0xFF) * 0x01010100 | 0xFF);
                }
            }

//            ByteBuffer buffer = pm.getPixels();
//            final int toByteShift = Math.max(0, shift + shift - 8);
//            for (int x = 0; x < size; x++) {
//                for (int y = 0; y < size; y++) {
//                    buffer.putInt((done[x][y] >>> toByteShift) * 0x01010100 | 0xFF);
//                }
//            }
//            buffer.flip();
        }

        String name = path + "BlueNoise" + (isTriangular ? "TriOmni" : "Omni") + "Tiling";
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
        long startTime = System.currentTimeMillis();

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
            String name = path + "Threshold" + threshold + (isTriangular ? "TriOmni" : "Omni") + "_";

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

        System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to get FFT.");

    }
    @Override
    public void render() {
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
        config.setTitle("SquidSquad Tool: Blue Noise Tiling Generator");
        config.setWindowedMode(size, size);
        config.disableAudio(true);
        new Lwjgl3Application(new BlueNoiseEqualOmniTilingGenerator(), config);
    }
}
