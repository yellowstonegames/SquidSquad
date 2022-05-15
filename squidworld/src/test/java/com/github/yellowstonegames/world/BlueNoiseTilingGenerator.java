package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.TricycleRandom;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.grid.Coord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class BlueNoiseTilingGenerator extends ApplicationAdapter {
    private static final int shift = 8, size = 1 << shift, sector = size >>> 2,
            mask = size - 1, sectorMask = sector - 1, wrapMask = sectorMask >>> 1;
    private static final double sigma = 1.9, sigma2 = sigma * sigma;
    private final ObjectFloatOrderedMap<Coord> energy = new ObjectFloatOrderedMap<Coord>(size * size, 0.5f)
    {
        @Override
        protected int place(@Nonnull Object item) {
            final int x = ((Coord)item).x, y = ((Coord)item).y;
            // Cantor pairing function
            return y + ((x + y) * (x + y + 1) >> 1) & mask;
        }

        @Override
        protected boolean equate(@Nonnull Object left, @Nullable Object right) {
            return left == right;
        }
    };
    private final float[][] lut = new float[sector][sector];
    private final int[][] done = new int[size][size];
    private Pixmap pm;
    private TricycleRandom rng;
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
        rng = new TricycleRandom(Hasher.hash64(1L, date));

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
        final int secX = point.x >>> shift - 2, secY = point.y >>> shift - 2,
                outerX = point.x & ~sectorMask, outerY = point.y & ~sectorMask;
        for (int x = 0; x < sector; x++) {
            for (int y = 0; y < sector; y++) {
                if((point.x & sectorMask) <= x + wrapMask && (point.x & sectorMask) + wrapMask >= x &&
                        (point.y & sectorMask) <= y + wrapMask && (point.y & sectorMask) + wrapMask >= y)
                {
                    energy.getAndIncrement(Coord.get(outerX + x, outerY + y),
                            0f, lut[x - point.x & sectorMask][y - point.y & sectorMask]);
                    continue;
                }
                float adj = 0x1p-3f;// (x|y) == 0 ? 0x1p-3f - lut[sectorMask][sectorMask] * 0x1p-5f : 0x1p-3f - lut[x][y] * 0x1p-5f;

                if((point.x & sectorMask) + wrapMask < x) {
                    for (int ex = -1 + (secX & 2); ex < 1 + (secX & 2); ex++) {
                        for (int ey = 0; ey < 4; ey++) {
                            energy.getAndIncrement(Coord.get(((ex & 3) << shift - 2) + x, ((ey & 3) << shift - 2) + y),
                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * adj);
                        }
                    }
                }
                else if((point.x & sectorMask) > x + wrapMask) {
                    for (int ex = (secX & 2); ex < 2 + (secX & 2); ex++) {
                        for (int ey = 0; ey < 4; ey++) {
                            energy.getAndIncrement(Coord.get(((ex & 3) << shift - 2) + x, ((ey & 3) << shift - 2) + y),
                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * adj);
                        }
                    }
                }
                else if((point.y & sectorMask) + wrapMask < y) {
                    for (int ex = 0; ex < 4; ex++) {
                        for (int ey = -1 + (secY & 2); ey < 1 + (secY & 2); ey++) {
                            energy.getAndIncrement(Coord.get(((ex & 3) << shift - 2) + x, ((ey & 3) << shift - 2) + y),
                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * adj);
                        }
                    }
                }
                else if((point.y & sectorMask) > y + wrapMask) {
                    for (int ex = 0; ex < 4; ex++) {
                        for (int ey = (secY & 2); ey < 2 + (secY & 2); ey++) {
                            energy.getAndIncrement(Coord.get(((ex & 3) << shift - 2) + x, ((ey & 3) << shift - 2) + y),
                                    0f, lut[x - point.x & sectorMask][y - point.y & sectorMask] * adj);
                        }
                    }
                }
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
        config.setTitle("SquidLib Tool: Blue Noise Tiling Generator");
        config.setWindowedMode(size, size);
        new Lwjgl3Application(new BlueNoiseTilingGenerator(), config);
    }
}
