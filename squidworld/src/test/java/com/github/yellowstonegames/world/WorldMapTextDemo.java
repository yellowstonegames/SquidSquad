package com.github.yellowstonegames.world;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.glyph.GlyphMap;
import com.github.yellowstonegames.glyph.KnownFonts;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import static com.github.yellowstonegames.core.DescriptiveColor.differentiateLightness;
import static com.github.yellowstonegames.core.DescriptiveColor.toRGBA8888;

/**
 * Map generator that uses text to show features at a location as well as color.
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
 */
public class WorldMapTextDemo extends ApplicationAdapter {
    //private static final int bigWidth = 314 * 3, bigHeight = 300;
//    private static final int bigWidth = 256, bigHeight = 256;
//    private static final int bigWidth = 1024, bigHeight = 512;
    private static final int bigWidth = 512, bigHeight = 256;
//    private static final int bigWidth = 2048, bigHeight = 1024;
    //private static final int bigWidth = 400, bigHeight = 400;
    private static final int cellWidth = 10, cellHeight = 16;
    private static final int shownWidth = 96, shownHeight = 48;
    private SpriteBatch batch;
    private GlyphMap display;//, overlay;
    private InputProcessor input;
    private Viewport view;
    private Camera camera;
    private LaserRandom rng;
    private long seedA, seedB;
    private Vector3 position, previousPosition, nextPosition;
//    private WorldMapGenerator.MimicMap world;
    private HyperellipticalWorldMap world;
    private WorldMapView wmv;
    private Thesaurus thesaurus;
    private PoliticalMapper pm;
    private ObjectList<PoliticalMapper.Faction> factions;
    private IntObjectOrderedMap<Language> atlas;
    private ObjectObjectOrderedMap<Coord, String> cities;
    private long counter;
    //private float nation = 0f;
    private long ttg; // time to generate
    private float moveAmount;
    private static int black = 0x000000FF,
            white = 0xFFFFFFFF;

    private static final char[] BIOME_CHARS = new char[66];
    static {
        for (int i = 0; i < 66; i++) {
            BIOME_CHARS[i] = Biome.TABLE[i].representation;
        }
    }
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        display = new GlyphMap(
                KnownFonts.getCascadiaMono().scaleTo(cellWidth, cellHeight),
                bigWidth * bigHeight);
        view = new StretchViewport(shownWidth * cellWidth, shownHeight * cellHeight);
        camera = view.getCamera();
        seedA = 1234567890L;
        seedB = 0x12345L;
        rng = new LaserRandom(seedA, seedB);
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
        world = new HyperellipticalWorldMap(seedA, bigWidth, bigHeight, WorldMapGenerator.DEFAULT_NOISE, 0.8f);
        wmv = new WorldMapView(world);
        thesaurus = new Thesaurus(rng.nextLong(), rng.nextLong());
        pm = new PoliticalMapper();
        atlas = new IntObjectOrderedMap<>(80);
        factions = new ObjectList<>(80);
        cities = new ObjectObjectOrderedMap<>(96);
        position = new Vector3(bigWidth * cellWidth * 0.5f, bigHeight * cellHeight * 0.5f, 0);
        previousPosition = position.cpy();
        nextPosition = position.cpy();
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode){
                    case Input.Keys.ENTER:
                        seedA = rng.nextLong();
                        seedB = rng.nextLong() | 1L;
                        generate(seedA, seedB);
                        rng.setState(seedA, seedB);
                        break;
                    case Input.Keys.S:
                    case Input.Keys.DOWN:
                        position.add(0, 1, 0);
                        break;
                    case Input.Keys.W:
                    case Input.Keys.UP:
                        position.add(0, -1, 0);
                        break;
                    case Input.Keys.A:
                    case Input.Keys.LEFT:
                        position.add(-1, 0, 0);
                        break;
                    case Input.Keys.D:
                    case Input.Keys.RIGHT:
                        position.add(1, 0, 0);
                        break;
                    case 'Q':
                    case 'q':
                    case Input.Keys.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                previousPosition.set(position);
                nextPosition.set(screenX, screenY, 0);
                camera.unproject(nextPosition);
                nextPosition.set(MathUtils.round(nextPosition.x * 4f), MathUtils.round(nextPosition.y * 4f), nextPosition.z);
                counter = System.currentTimeMillis();
                moveAmount = 0f;
                return true;
            }
        };
        generate(seedA, seedB);
        rng.setState(seedA, seedB);
        Gdx.input.setInputProcessor(input);
    }

//    public void zoomIn() {
//        zoomIn(bigWidth >> 1, bigHeight >> 1);
//    }
//    public void zoomIn(int zoomX, int zoomY)
//    {
//        long startTime = System.currentTimeMillis();
//        world.zoomIn(1, zoomX, zoomY);
//        dbm.makeBiomes(world);
//        //counter = 0L;
//        ttg = System.currentTimeMillis() - startTime;
//    }
//    public void zoomOut()
//    {
//        zoomOut(bigWidth >>1, bigHeight >>1);
//    }
//    public void zoomOut(int zoomX, int zoomY)
//    {
//        long startTime = System.currentTimeMillis();
//        world.zoomOut(1, zoomX, zoomY);
//        dbm.makeBiomes(world);
//        //counter = 0L;
//        ttg = System.currentTimeMillis() - startTime;
//    }
    public void generate(final long seedA, final long seedB)
    {
        long startTime = System.currentTimeMillis();
        System.out.println("Seed used: 0x" + DigitTools.hex(seedA) + "L, 0x" + DigitTools.hex(seedB));
        world.seedA = seedA;
        world.seedB = seedB;
        wmv.generate();
        wmv.show();
        atlas.clear();
        for (int i = 0; i < 64; i++) {
            Language lang = rng.randomElement(Language.romanizedHumanLanguages)
                    .mix(rng.randomElement(Language.romanizedHumanLanguages), rng.nextFloat()).removeAccents();
            factions.add(new PoliticalMapper.Faction(lang, thesaurus.makeNationName(lang)));
            atlas.put(ArrayTools.letterAt(i), lang);
        }
        final char[][] political = pm.generate(rng.nextLong(), world, wmv.getBiomeMapper(), factions, 64, 1f);
        cities.clear();
        Coord[] points = world.landData
                .copy() // don't want to edit the actual land map
                .removeEdges() // don't want cities on the edge of the map
                .separatedRegionBlue(0.1f, 500) // get 500 points in a regularly-tiling but unpredictable, sparse pattern
                .randomPortion(rng,112); // randomly select less than 1/4 of those points, breaking the pattern
        for (int i = 0; i < points.length; i++) {
            char p = political[points[i].x][points[i].y];
            if(p == '~' || p == '%')
                continue;
            Language lang = atlas.get(p);
            if(lang != null)
            {
                cities.put(points[i], lang.word(rng, false)
                );
//                        .toUpperCase());
            }
        }
        //counter = 0L;
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        // uncomment next line to generate maps as quickly as possible
        //generate(rng.nextLong());
//        ArrayTools.fill(display.backgrounds, -0x1.0p125F);
        display.backgrounds = wmv.getColorMap();
        BiomeMapper.DetailedBiomeMapper dbm = wmv.getBiomeMapper();
        int hc, tc, codeA, codeB;
        float mix;
        int[][] heightCodeData = world.heightCodeData;
        //double xp, yp, zp;
        for (int y = 0; y < bigHeight; y++) {
            PER_CELL:
            for (int x = 0; x < bigWidth; x++) {
                hc = heightCodeData[x][y];
                if(hc == 1000)
                    continue;
                tc = dbm.heatCodeData[x][y];
                if(tc == 0)
                {
                    switch (hc)
                    {
                        case 0:
                        case 1:
                        case 2:
                            display.put(x, y, '≈', toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[30], display.backgrounds[x][y])));
                            continue PER_CELL;
                        case 3:
                            display.put(x, y, '~', toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[24], display.backgrounds[x][y])));
                            continue PER_CELL;
                        case 4:
                            display.put(x, y, '¤', toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[42], display.backgrounds[x][y])));
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                        display.put(x, y, '≈', toRGBA8888(differentiateLightness(wmv.BIOME_COLOR_TABLE[44], display.backgrounds[x][y])));
                        break;
                    case 3:
                        display.put(x, y, '~', toRGBA8888(differentiateLightness(wmv.BIOME_COLOR_TABLE[43], display.backgrounds[x][y])));
                        break;
                    default: 
                        int bc = dbm.biomeCodeData[x][y];
                        codeB = dbm.extractPartB(bc);
                        codeA = dbm.extractPartA(bc);
                        mix = dbm.extractMixAmount(bc);
                        if(mix <= 0.5) 
                            display.put(x, y, BIOME_CHARS[codeA], toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[codeB], display.backgrounds[x][y])));
                        else
                            display.put(x, y, BIOME_CHARS[codeB], toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[codeA], display.backgrounds[x][y])));
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
            display.put(ct.x, ct.y, '□', toRGBA8888(differentiateLightness(DescriptiveColor.GRAY, display.backgrounds[ct.x][ct.y])));
            if(ct.y >= display.backgrounds[0].length - 1) continue;
            for (int pos = ct.x - (cname.length() >> 1), j = 0; j < cname.length(); pos++, j++) {
                display.backgrounds[pos][ct.y + 1] = dark;
                display.put(pos, ct.y + 1, cname.charAt(j), nationColor);
            }
        }
    }
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0.13333334f, 0.1254902f, 0.20392157f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if(!nextPosition.epsilonEquals(previousPosition)) {
            moveAmount = (System.currentTimeMillis() - counter) * 0.001f;
            if (moveAmount <= 1f) {
                position.set(previousPosition).lerp(nextPosition, moveAmount);
                position.set(MathUtils.round(position.x), MathUtils.round(position.y), position.z);
            }
            else {
                previousPosition.set(position);
                nextPosition.set(position);
                nextPosition.set(MathUtils.round(nextPosition.x), MathUtils.round(nextPosition.y), nextPosition.z);
                moveAmount = 0f;
                counter = System.currentTimeMillis();
            }
        }
//        camera.position.set(position);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");
        batch.begin();
        display.draw(batch, 0.125f * shownWidth * cellWidth - 1.25f * position.x, 0.125f * shownWidth * cellWidth - 1.25f * position.y);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Demo: Text World Map");
        config.useVsync(true);
        config.setWindowedMode(shownWidth * cellWidth, shownHeight * cellHeight);
        new Lwjgl3Application(new WorldMapTextDemo(), config);
    }
}
