package com.github.yellowstonegames.world;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.support.DistinctRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.glyph.GlyphMap;
import com.github.yellowstonegames.glyph.KnownFonts;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordObjectOrderedMap;
import com.github.yellowstonegames.grid.IntPointHash;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.place.WildernessGenerator;
import com.github.yellowstonegames.text.Language;

import static com.github.yellowstonegames.core.DescriptiveColor.differentiateLightness;
import static com.github.yellowstonegames.core.DescriptiveColor.toRGBA8888;

/**
 * Map generator that uses text to show features at a location as well as color.
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
 */
public class WorldWildMapDemo extends ApplicationAdapter {
//    private static final int bigWidth = 256, bigHeight = 256;
//    private static final int bigWidth = 1024, bigHeight = 512;
    private static final int bigWidth = 512, bigHeight = 256;
//    private static final int bigWidth = 2048, bigHeight = 1024;
    //private static final int bigWidth = 400, bigHeight = 400;
    private static final float cellWidth = 16, cellHeight = 16;
    private static final int shownWidth = 96, shownHeight = 48;
    private SpriteBatch batch;
    private GlyphMap display, inner;
    private InputProcessor input;
    private Viewport view;
    private Camera camera;
    private DistinctRandom rng;
    private long seed;
    private Vector3 position, previousPosition, nextPosition;
//    private WorldMapGenerator.MimicMap world;
    private HyperellipticalWorldMap world;
    private WorldMapView wmv;
    private PoliticalMapper pm;
    private IntObjectOrderedMap<Language> atlas;
    private CoordObjectOrderedMap<String> cities;
    private WildernessGenerator wildMap;
    private boolean zoomed;
    //private WorldMapGenerator.EllipticalMap world;
    //private final float[][][] cloudData = new float[128][128][128];
    private long counter;
    //private float nation = 0f;
    private long ttg; // time to generate
    private float moveAmount;
    private final Color INK = new Color(DescriptiveColor.describe("darkmost dullest mauve"));

    private static final char[] BIOME_CHARS = new char[66];
    static {
        for (int i = 0; i < 66; i++) {
            BIOME_CHARS[i] = Biome.TABLE[i].representation;
        }
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        display = new GlyphMap(KnownFonts.getIosevka().scaleTo(cellWidth, cellHeight), bigWidth, bigHeight);
        inner = new GlyphMap(KnownFonts.getIosevka().scaleTo(cellWidth, cellHeight), shownWidth, shownHeight);
        view = display.viewport = new StretchViewport(shownWidth, shownHeight);
        camera = view.getCamera();
        seed = 1234567890L;
        rng = new DistinctRandom(seed);
//// you can use whatever map you have instead of fantasy_map.png, where white means land and black means water
//        Pixmap pix = new Pixmap(Gdx.files.internal("special/fantasy_map.png"));
//        final int bigWidth = pix.getWidth() / 4, bigHeight = pix.getHeight() / 4;
//        GreasedRegion basis = new GreasedRegion(bigWidth, bigHeight);
//        for (int x = 0; x < bigWidth; x++) {
//            for (int y = 0; y < bigHeight; y++) {
//                if(pix.getPixel(x * 4, y * 4) < 0) // only counts every fourth row and every fourth column
//                    basis.insert(x, y);
//            }
//        }
//        basis = WorldMapGenerator.MimicMap.reprojectToElliptical(basis);
//// at this point you could get the GreasedRegion as a String, and save the compressed output to a file:
//// Gdx.files.local("map.txt").writeString(LZSPlus.compress(basis.serializeToString()), false, "UTF16");
//// you could reload basis without needing the original map image with
//// basis = GreasedRegion.deserializeFromString(LZSPlus.decompress(Gdx.files.local("map.txt").readString("UTF16")));
//// it's also possible to store the compressed map as a String in code, but you need to be careful about escaped chars.
//        world = new WorldMapGenerator.LocalMimicMap(seed, basis, FastNoise.instance, 0.8);
//        pix.dispose();

//        world = new WorldMapGenerator.MimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 0.8); // uses a map of Australia for land
        world = new HyperellipticalWorldMap(seed, bigWidth, bigHeight, WorldMapGenerator.DEFAULT_NOISE, 0.8f);
        //world = new WorldMapGenerator.TilingMap(seed, bigWidth, bigHeight, WhirlingNoise.instance, 0.9);
        wmv = new WorldMapView(world);
        pm = new PoliticalMapper();
        cities = new CoordObjectOrderedMap<>(96);
        atlas = new IntObjectOrderedMap<>(80);
        position = new Vector3(bigWidth * 0.5f, bigHeight * 0.5f, 0);
        previousPosition = position.cpy();
        nextPosition = position.cpy();
        input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode){
                    case Input.Keys.ENTER:
                        seed = rng.nextLong();
                        generate(seed);
                        rng.setSeed(seed);
                        break;
                    case Input.Keys.DOWN:
                        position.add(0, 1, 0);
                        break;
                    case Input.Keys.UP:
                        position.add(0, -1, 0);
                        break;
                    case Input.Keys.LEFT:
                        position.add(-1, 0, 0);
                        break;
                    case Input.Keys.RIGHT:
                        position.add(1, 0, 0);
                        break;
                    case Input.Keys.Q:
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                    default:
                        return false;
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(zoomed)
                {
                    zoomed = false;
                    position.set(previousPosition);
                    return true;
                }
                if(button == Input.Buttons.RIGHT)
                {
                    previousPosition.set(position);
                    nextPosition.set(screenX, screenY, 0);
                    camera.unproject(nextPosition);
//                    nextPosition.set(nextPosition.x, nextPosition.y, nextPosition.z);
                    position.set(0.5f * shownWidth, bigHeight - (0.5f * shownHeight), position.z);
                    zoomed = true;
                    final int hash = IntPointHash.hashAll(screenX, screenY, 0x13579BDF);
                    System.out.printf("%s at %f,%f (nextPosition is %s)\n", Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(nextPosition.x), (int) (nextPosition.y))], nextPosition.x, nextPosition.y, nextPosition);
                    System.out.printf("%s at %f,%f (previousPosition is %s)\n", Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(previousPosition.x), (int) (previousPosition.y))], previousPosition.x, previousPosition.y, previousPosition);
                    System.out.printf("%s at %f,%f (position is %s)\n", Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(position.x), (int) (position.y))], position.x, position.y, position);
                    System.out.printf("%s at %f,%f (adjusted)\n", Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(previousPosition.x + shownWidth * 0.5f), (int) (bigHeight - shownHeight * 0.5f - previousPosition.y))],
                            (previousPosition.x + shownWidth * 0.5f), (bigHeight - shownHeight * 0.5f - previousPosition.y));
                    System.out.printf("%s at %f,%f (adjusted 2)\n", Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(previousPosition.x + nextPosition.x), (int) (previousPosition.y + nextPosition.y))],
                            (previousPosition.x + nextPosition.x), (previousPosition.y + nextPosition.y));
                    wildMap = new WildernessGenerator.MixedWildernessGenerator(
                            new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(nextPosition.x)+1, (int) (bigHeight - nextPosition.y)-1)], hash, ~hash),
                            new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(nextPosition.x)+1, (int) (bigHeight - nextPosition.y))], hash, ~hash),
                            new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(nextPosition.x), (int) (bigHeight - nextPosition.y))], hash, ~hash),
                            new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(nextPosition.x), (int) (bigHeight - nextPosition.y)-1)], hash, ~hash),
                            rng
                    );
//                    wildMap = new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(previousPosition.x + nextPosition.x), (int) (previousPosition.y + nextPosition.y))], rng);
                    wildMap = new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode((int)(nextPosition.x), (int) (nextPosition.y))], rng);
                    wildMap.generate();
                    nextPosition.set(previousPosition);
                }
                else {
                    previousPosition.set(position);
                    nextPosition.set(screenX, screenY, 0);
                    camera.unproject(nextPosition);
                    nextPosition.set((nextPosition.x * 4f), (nextPosition.y * 4f), nextPosition.z);
                    counter = System.currentTimeMillis();
                    moveAmount = 0f;
                }
                return true;
            }
        };
        generate(seed);
        rng.setSeed(seed);
        Gdx.input.setInputProcessor(input);
    }

    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        System.out.println("Seed used: 0x" + DigitTools.hex(seed) + "L");
        world.seedA = (int)(seed & 0xFFFFFFFFL);
        world.seedB = (int) (seed >>> 32);
        wmv.generate();
        wmv.show();
        atlas.clear();
        for (int i = 0; i < 64; i++) {
            atlas.put(ArrayTools.letterAt(i),
                    rng.randomElement(Language.romanizedHumanLanguages).mix(rng.randomElement(Language.romanizedHumanLanguages), rng.nextFloat()).removeAccents());
        }
        final char[][] political = pm.generate(rng.nextLong(), world, wmv.biomeMapper, 1);
        cities.clear();
        Coord[] points = world.landData
                .copy() // don't want to edit the actual land map
                .removeEdges() // don't want cities on the edge of the map
                .separatedRegionBlue(0.1f, 500) // get 500 points in a regularly-tiling but unpredictable, sparse pattern
                .randomPortion(rng, 112); // randomly select less than 1/4 of those points, breaking the pattern
        for (int i = 0; i < points.length; i++) {
            char p = political[points[i].x][points[i].y];
            if(p == '~' || p == '%')
                continue;
            Language lang = atlas.get(p);
            if(lang != null)
            {
                cities.put(points[i], lang.word(rng, false).toUpperCase());
            }
        }
        //counter = 0L;
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        if(zoomed)
        {
            inner.backgrounds = wildMap.colors;
            for (int y = 0; y < wildMap.height; y++) {
                for (int x = 0; x < wildMap.width; x++) {
                    inner.put(x, y, wildMap.glyphs[x][y]);
                }
            }
            return;
        }
        display.backgrounds = wmv.getColorMap();
        int[][] oklab = wmv.getColorMapOklab();
        BiomeMapper.DetailedBiomeMapper dbm = wmv.getBiomeMapper();
        int hc, tc, codeA, codeB;
        float mix;
        int[][] heightCodeData = world.heightCodeData;
        for (int y = 0; y < bigHeight; y++) {
            PER_CELL:
            for (int x = 0; x < bigWidth; x++) {
                hc = heightCodeData[x][y];
                if (hc == 1000)
                    continue;
                tc = dbm.heatCodeData[x][y];
                if (tc == 0) {
                    switch (hc) {
                        case 0:
                        case 1:
                        case 2:
                            display.put(x, y, '≈', toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[30], oklab[x][y])));
                            continue PER_CELL;
                        case 3:
                            display.put(x, y, '~', toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[24], oklab[x][y])));
                            continue PER_CELL;
                        case 4:
                            display.put(x, y, '¤', toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[42], oklab[x][y])));
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                        display.put(x, y, '≈', toRGBA8888(differentiateLightness(wmv.BIOME_COLOR_TABLE[44], oklab[x][y])));
                        break;
                    case 3:
                        display.put(x, y, '~', toRGBA8888(differentiateLightness(wmv.BIOME_COLOR_TABLE[43], oklab[x][y])));
                        break;
                    default:
                        int bc = dbm.biomeCodeData[x][y];
                        codeB = dbm.extractPartB(bc);
                        codeA = dbm.extractPartA(bc);
                        mix = dbm.extractMixAmount(bc);
                        if (mix <= 0.5) {
                            display.put(x, y, BIOME_CHARS[codeA], toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[codeB], oklab[x][y])));
                        } else {
                            display.put(x, y, BIOME_CHARS[codeB], toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[codeA], oklab[x][y])));
                        }
                }
            }
        }
        int dark = toRGBA8888(0xFE7F7F40);
        for (int i = 0; i < cities.size(); i++) {
            Coord ct = cities.keyAt(i);
            String cname = cities.getAt(i);
            if(cname == null) continue;
            int nationColor = toRGBA8888(differentiateLightness(
                    DescriptiveColor.COLORS_BY_HUE.get((int) ((pm.politicalMap[ct.x][ct.y] * 0x9E3779B97F4A7C15L >>> 32) * 48 >>> 32) + 2),
                    0xFE7F7F40)); // dark gray
            display.put(ct.x, ct.y, '□', toRGBA8888(differentiateLightness(DescriptiveColor.GRAY, oklab[ct.x][ct.y])));
            int pos = ct.x - (cname.length() >> 1);
            if(ct.y >= display.backgrounds[0].length - 1 || pos + cname.length() >= display.backgrounds.length) continue;
            for (int j = 0; j < cname.length(); pos++, j++) {
                display.backgrounds[pos][ct.y + 1] = dark;
                display.put(pos, ct.y + 1, cname.charAt(j), nationColor);
            }
        }
        for (int i = 0; i < cities.size(); i++) {
            Coord ct = cities.keyAt(i);
            String cname = cities.getAt(i);
            int bg = DescriptiveColor.describe("dullest chocolate");
            int fg = DescriptiveColor.describe("dull yellow");
            display.put(ct.x, ct.y, '□', bg);
//            display.put(ct.x, ct.y, '#', SColor.SOOTY_WILLOW_BAMBOO);
            for (int j = ct.x - (cname.length() >> 1), k = 0; j < cname.length() && j < display.backgrounds.length; j++, k++) {
                display.put(j, ct.y + 1, cname.charAt(k), fg);
                display.backgrounds[j][ct.y + 1] = bg;
            }
        }
    }
    @Override
    public void render() {
        ScreenUtils.clear(INK);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if(!nextPosition.epsilonEquals(previousPosition)) {
            moveAmount = (System.currentTimeMillis() - counter) * 0.001f;
            if (moveAmount <= 1f) {
                position.set(previousPosition).lerp(nextPosition, moveAmount);
                position.set((position.x), (position.y), position.z);
            }
            else {
                previousPosition.set(position);
                nextPosition.set(position);
                nextPosition.set((nextPosition.x), (nextPosition.y), nextPosition.z);
                moveAmount = 0f;
                counter = System.currentTimeMillis();
            }
        }
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");
        if(zoomed){
            view.apply(false);
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            inner.draw(batch, 0, 0);
            batch.end();
            return;
        }
        view.apply(false);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        display.draw(batch, 0.125f * shownWidth - 1.25f * position.x, 0.125f * shownHeight - 1.25f * position.y);
        batch.end();
    }
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        display.resize(width, height);
        inner.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Demo: Detailed World Map");
        config.useVsync(true);
        config.setWindowedMode((int)(shownWidth * cellWidth), (int)(shownHeight * cellHeight));
        new Lwjgl3Application(new WorldWildMapDemo(), config);
    }
}
