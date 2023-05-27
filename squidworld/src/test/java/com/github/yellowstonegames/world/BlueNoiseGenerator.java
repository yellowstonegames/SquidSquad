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
 * https://bartwronski.com/2021/04/21/superfast-void-and-cluster-blue-noise-in-python-numpy-jax/ for more.
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
public class BlueNoiseGenerator extends ApplicationAdapter {
    private static final int shift = 7, size = 1 << shift, mask = size - 1;
    private static final double sigma = 1.9, sigma2 = sigma * sigma;
    private final ObjectFloatOrderedMap<Coord> energy = new ObjectFloatOrderedMap<Coord>(size * size, 0.5f)
    {
        @Override
        protected int place(@NonNull Object item) {
            final int x = ((Coord)item).x, y = ((Coord)item).y;
            // Cantor pairing function
            return y + ((x + y) * (x + y + 1) >> 1) & mask;
        }

        @Override
        protected boolean equate(@NonNull Object left, @Nullable Object right) {
            return left == right;
        }
    };
    private final float[][] lut = new float[size][size];
    private final int[][] done = new int[size][size];
    private Pixmap pm;
    private WhiskerRandom rng;
    private PixmapIO.PNG writer;
    private String path;

    @Override
    public void create() {
        Coord.expandPoolTo(size, size);
        String date = DateFormat.getDateInstance().format(new Date());
        path = "out/blueNoise/" + date + "/";
        
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        writer = new PixmapIO.PNG((int)(pm.getWidth() * pm.getHeight() * 1.5f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);
        rng = new WhiskerRandom(Hasher.hash64(1L, date));

        final int hs = size >>> 1;
        float[] column = new float[size];
        for (int i = 1; i < hs; i++) {
            column[size - i] = column[i] = (float) Math.exp(-0.5 * i * i / sigma2);
        }
        column[0] = 1f;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                lut[x][y] = column[x] * column[y];
            }
        }
        lut[0][0] = Float.POSITIVE_INFINITY;


        generate();
        Gdx.app.exit();
    }

    private void energize(Coord point) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                energy.getAndIncrement(Coord.get(x, y), 0f, lut[x - point.x & mask][y - point.y & mask]);
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
        for (int i = 1; i <= limit; i++) {
            initial.add(Coord.get(vdc(5, i), vdc(3, i)));
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
        }
        final int toByteShift = Math.max(0, shift + shift - 8);

        ByteBuffer buffer = pm.getPixels();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                buffer.putInt((done[x][y] >>> toByteShift) * 0x01010100 | 0xFF);
//                pm.drawPixel(x, y, done[x][y] << 8 | 0xFF);
//                pm.drawPixel(x, y, (done[x][y] >>> toByteShift) * 0x01010100 | 0xFF);
            }
        }
        buffer.flip();

        try {
            writer.write(Gdx.files.local(path + "BlueNoiseRevised.png"), pm); // , false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: " + path + "BlueNoiseRevised.png", ex);
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
        config.setTitle("SquidSquad Tool: Blue Noise Generator");
        config.setWindowedMode(size, size);
        new Lwjgl3Application(new BlueNoiseGenerator(), config);
    }
}
