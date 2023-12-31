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

package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.yellowstonegames.grid.BlueNoise;
import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.grid.Coord;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Comparator;
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
 * energy than normal, the dispersed energy is lessened; it is effectively divided by the number of sectors. The rest of
 * the code is roughly the same as Wronski's; we don't have access to Numpy or Jax, so we make do with jdkgdxds.
 * <br>
 * This makes use of some of the more unusual and powerful features in jdkgdxds; that's the reason this tool was moved
 * from SquidLib (which already has existing code to analyze blue noise, and so seemed like a nice fit) to SquidSquad.
 * The "energy" level of each pixel in the grid is tracked by an ObjectFloatOrderedMap with Coord keys. This map is
 * dense, with all 64x64 (or more) cells filled by Coord keys, and the reasons it's used are to pair points with their
 * energies, and because it is an Ordered, so it provides
 * {@link com.github.tommyettinger.ds.Ordered#selectRanked(Comparator, int)}. In libGDX, the Select code only applies to
 * arrays and Arrays (ugh, these names...), but in jdkgdxds, it applies to all Ordered implementations. One could use
 * OrderedMap's orderedKeys in libGDX and selectRanked() with that, but there's no OrderedMap with primitive float keys.
 */
public class BlueNoiseOmniTilingGenerator extends ApplicationAdapter {

    /**
     * True if this should produce triangular-mapped blue noise.
     */
    private static final boolean isTriangular = false;

    /**
     * Affects the size of the parent noise; typically 8 or 9 for a 256x256 or 5125x512 parent image.
     */
    private static final int shift = 8;
    /**
     * Affects how many sectors are cut out of the full size; this is an exponent (with a base of 2).
     */
    private static final int sectorShift = 3;

    private static final int size = 1 << shift;
    private static final int sectors = 1 << sectorShift;
    private static final int sector = size >>> sectorShift;
    private static final int mask = size - 1;
    private static final int sectorMask = sector - 1;
    private static final int wrapMask = sectorMask >>> 1;
    private static final float fraction = 1f / (sectors * sectors);

    private static final double sigma = 1.9, sigma2 = sigma * sigma;
    private final ObjectFloatOrderedMap<Coord> energy = new ObjectFloatOrderedMap<Coord>(size * size, 0.5f)
    { // OK, we're making an anonymous subclass of ObjectFloatOrderedMap so its hashing function is faster.
      // It may also make it collide less, but the computation is much simpler here than the default.
      // This makes a roughly 3x difference in runtime. (!)
        @Override
        protected int place(@NonNull Object item) {
            final int x = ((Coord) item).x, y = ((Coord) item).y;
            // Cantor pairing function
            return y + ((x + y) * (x + y + 1) >> 1) & mask;
        }

        @Override
        protected boolean equate(@NonNull Object left, @Nullable Object right) {
            return left == right;
        }
    };
    private final float[][] lut = new float[sector][sector];
    private final int[][] done = new int[size][size];
    private Pixmap pm;
    private WhiskerRandom rng;
    private PixmapIO.PNG writer;
    private String path;

    @Override
    public void create() {
        Coord.expandPoolTo(size, size);
        String date = DateFormat.getDateInstance().format(new Date());
        path = "out/blueNoise/" + date + "/tiling/";
        
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        writer = new PixmapIO.PNG((int)(pm.getWidth() * pm.getHeight() * 1.5f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);
        rng = new WhiskerRandom(Hasher.hash64(1L, date));

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
        lut[0][0] = Float.POSITIVE_INFINITY;


        generate();
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
                else {
                    for (int ex = 0; ex < sectors; ex++) {
                        for (int ey = 0; ey < sectors; ey++) {
                            energy.getAndIncrement(Coord.get((ex << shift - sectorShift) + x, (ey << shift - sectorShift) + y),
                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * fraction);
                        }
                    }
                }
            }
        }
    }

    public static int vdc(final int base, int index)
    {
        if(base <= 2) {
            return (Integer.reverse(index) >>> 32 - shift);
        }
        double denominator = base, res = 0.0;
        while (index > 0)
        {
            res += (index % base) / denominator;
            index /= base;
            denominator *= base;
        }
        return (int) (res * size);
    }

    public void generate()
    {
        long startTime = System.currentTimeMillis();

        final int limit = (size >>> 3) * (size >>> 3);
        ObjectList<Coord> initial = new ObjectList<>(limit);
        final int xOff = rng.next(shift), yOff = rng.next(shift);
        for (int i = 1; i <= limit; i++) {
            initial.add(Coord.get(vdc(5, i) + xOff & mask, vdc(3, i) + yOff & mask));
        }
        initial.shuffle(rng);
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

        for (int n = size * size; ctr < n; ctr++) {
            Coord low = energy.selectRanked(
                    (o1, o2) -> Float.compare(energy.getOrDefault(o1, 0f), energy.getOrDefault(o2, 0f)),
                    1);
            energize(low);
            done[low.x][low.y] = ctr;
            if((ctr & 1023) == 0) System.out.println("Completed " + ctr + " out of " + n + " in " + (System.currentTimeMillis() - startTime) + "ms.");
        }
        ByteBuffer buffer = pm.getPixels();
        if(isTriangular) {
            final float shrink = 1f / (1 << shift + shift);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    float rnd = done[x][y] * shrink + 0.5f;
                    rnd -= (int) rnd;
                    float orig = rnd * 2f - 1f;
                    rnd = (orig == 0f) ? 0f : (float) (orig / Math.sqrt(Math.abs(orig)));
                    rnd = (rnd - Math.signum(orig)) * 127.5f + 127.5f;
//                buffer.putInt((done[x][y] >>> toByteShift) * 0x01010100 | 0xFF);
                    buffer.putInt((Math.round(rnd) & 0xFF) * 0x01010100 | 0xFF);
//                pm.drawPixel(x, y, done[x][y] << 8 | 0xFF);
//                pm.drawPixel(x, y, (done[x][y] >>> toByteShift) * 0x01010100 | 0xFF);
                }
            }
        }
        else {
            final int toByteShift = Math.max(0, shift + shift - 8);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    buffer.putInt((done[x][y] >>> toByteShift) * 0x01010100 | 0xFF);
                }
            }
        }
        buffer.flip();

        String name = path + "BlueNoise" + (isTriangular ? "TriOmni" : "Omni") + "Tiling" + sectors + "x" + sectors + ".png";
        try {
            writer.write(Gdx.files.local(name), pm); // , false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: " + name, ex);
        }

        System.out.println("Took " + (System.currentTimeMillis() - startTime) + "ms to generate.");
    }
    @Override
    public void render() {
    }

    @Override
    public void dispose() {
        super.dispose();
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
        new Lwjgl3Application(new BlueNoiseOmniTilingGenerator(), config);
    }
}
