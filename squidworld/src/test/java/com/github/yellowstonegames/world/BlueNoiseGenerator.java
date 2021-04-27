package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.grid.Coord;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/**
 * Porting Bart Wronski's blue noise generator from NumPy to Java, see
 * https://bartwronski.com/2021/04/21/superfast-void-and-cluster-blue-noise-in-python-numpy-jax/ for more.
 */
public class BlueNoiseGenerator extends ApplicationAdapter {
    private static final int shift = 6, size = 1 << shift, mask = size - 1;
    private static final double sigma = 1.9, sigma2 = sigma * sigma;
    private final ObjectFloatOrderedMap<Coord> energy = new ObjectFloatOrderedMap<>(size * size);
    private final float[][] lut = new float[size][size];
    private final int[][] done = new int[size][size];
    private Pixmap pm;
    private LaserRandom rng;
    private PixmapIO.PNG writer;
    private String date, path;

    @Override
    public void create() {
        date = DateFormat.getDateInstance().format(new Date());
        path = "out/blueNoise/" + date + "/";
        
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        writer = new PixmapIO.PNG((int)(pm.getWidth() * pm.getHeight() * 1.5f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);
        rng = new LaserRandom(Hasher.hash64(1L, date));

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

    public void generate()
    {
        long startTime = System.currentTimeMillis();

        final int limit = (size >>> 3) * (size >>> 3);
        ObjectList<Coord> initial = new ObjectList<>(limit);
        for (int i = 1; i <= limit; i++) {
            initial.add(Coord.get((int) (0xC13FA9A902A6328FL * i >>> 64 - shift), (int)(0x91E10DA5C79E7B1DL * i >>> 64 - shift)));
        }
        rng.shuffle(initial);
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

        pm.setColor(Color.BLACK);
        pm.fill();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
//                pm.drawPixel(x, y, done[x][y] << 8 | 0xFF);
                pm.drawPixel(x, y, (done[x][y] >>> toByteShift) * 0x01010100 | 0xFF);
            }
        }

        try {
            writer.write(Gdx.files.local(path + "BlueNoiseTiling.png"), pm); // , false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: " + path + "BlueNoiseTiling.png", ex);
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
        config.setTitle("SquidLib Tool: Blue Noise Generator");
        config.setWindowedMode(size, size);
        new Lwjgl3Application(new BlueNoiseGenerator(), config);
    }
}
