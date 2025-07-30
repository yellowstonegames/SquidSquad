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
import com.github.tommyettinger.random.AceRandom;
import com.github.yellowstonegames.grid.BlueNoise;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;

import static com.github.yellowstonegames.world.BlueNoiseDoubleFastOmniTilingGenerator.*;

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
 * Higher precision doesn't seem to help with grid artifacts when using triangular mapping...
 */
public class BlueNoiseTentDatProcessor extends ApplicationAdapter {

    /**
     * True if this should produce triangular-mapped blue noise (always).
     */
    private static final boolean isTriangular = true;
    private Pixmap pm, pmSection;
    private FastPNG writer;
    private String path;
    private static final String datPath = "squidworld/src/test/resources/2025/BlueNoiseOmniTiling256x256_Jul_29.dat";

    public final AceRandom random = new AceRandom(1, 2, 3, 4, 5);

    public static int get(final int x, final int y) {
        return x << shift | y;
    }

    @Override
    public void create() {
        final long startTime = System.currentTimeMillis();
        String date = DateFormat.getDateInstance().format(new Date());
        path = "out/blueNoise/" + date + "_" + System.currentTimeMillis() + "/";
        random.setSeed(Hasher.bune.hashBulk64(date));
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pmSection = new Pixmap(sector, sector, Pixmap.Format.RGBA8888);
        pmSection.setBlending(Pixmap.Blending.None);

        writer = new FastPNG((int)(pm.getWidth() * pm.getHeight() * 2f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);
        generate();
        getThresholdAndFFT(pm);
        System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to process.");
        Gdx.app.exit();
    }

    public void generate()
    {
        long startTime = System.currentTimeMillis();

        ByteBuffer bytes = ByteBuffer.wrap(Gdx.files.local(datPath).readBytes());
        int[] inv = new int[sizeSq];
        bytes.asIntBuffer().get(inv);

        System.out.println(inv[0]);
        System.out.println(inv[1]);
        System.out.println(inv[2]);
        System.out.println(inv[3]);

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

            for (int from = 0; from < sizeSq; from += sectorSize) {
                for (int i = 0; i < sectorSize; i++) {
                    final int pt = inv[from + i];
                    final int color = colorMap[i];
                    pm.drawPixel(pt>>>shift, pt&mask, color);
                    histogram[color>>>24]++;
                }
            }
        }
        else {
            for (int from = 0; from < sizeSq; from += sectorSize) {
                for (int i = 0; i < sectorSize; i++) {
                    final int pt = inv[from+i];
                    final double r = (i * (256.0 / sectorSize));
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
//        Gdx.files.local(name + size + "x" + size + ".dat").writeBytes(bytes.array(), false);
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
        config.setTitle("SquidSquad Tool: Tent Blue Noise DAT Processor");
        config.setWindowedMode(size, size);
        config.disableAudio(true);
        new Lwjgl3Application(new BlueNoiseTentDatProcessor(), config);
    }
}
