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
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.MizuchiRandom;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.glyph.GlyphMap;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Noise;
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
    private static final int cellWidth = 16, cellHeight = 16;
    private static final int shownWidth = 96, shownHeight = 48;
    private SpriteBatch batch;
    private GlyphMap display;//, overlay;
    private InputProcessor input;
    private Viewport view;
    private Camera camera;
    private EnhancedRandom rng;
    private long seedA, seedB;
    private Vector3 position, previousPosition, nextPosition, temp;
//    private MimicWorldMap world;
    private HyperellipticalWorldMap world;
    private WorldMapView wmv;
    private Thesaurus thesaurus;
    private PoliticalMapper pm;
    private ObjectList<PoliticalMapper.Faction> factions;
    private IntObjectOrderedMap<Language> atlas;
    private ObjectObjectOrderedMap<Coord, String> cities;
    private long counter;
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
                KnownFonts.getIosevkaSlab().scaleTo(cellWidth, cellHeight),
                bigWidth, bigHeight);
        view = display.viewport = new StretchViewport(shownWidth, shownHeight);
        camera = view.getCamera();
        seedA = 1234567890L;
        seedB = 0x12345L;
        rng = new MizuchiRandom(seedA, seedB);
        Noise noise = new Noise(rng.nextInt(), 1.0f, Noise.FOAM, 1);
//        world = new MimicWorldMap(seedA, noise, 0.8f); // uses a map of Australia for land
        world = new HyperellipticalWorldMap(seedA, bigWidth, bigHeight, noise, 0.8f);
        wmv = new WorldMapView(world);
        thesaurus = new Thesaurus(rng.nextLong(), rng.nextLong());
        pm = new PoliticalMapper();
        atlas = new IntObjectOrderedMap<>(80);
        factions = new ObjectList<>(80);
        cities = new ObjectObjectOrderedMap<>(96);
        position = new Vector3(bigWidth * 0.25f, bigHeight * 0.25f, 0);
        previousPosition = position.cpy();
        nextPosition = position.cpy();
        temp = new Vector3();
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode){
                    case Input.Keys.ENTER:
                        seedA = rng.nextLong();
                        seedB = rng.nextLong() | 1L;
                        generate(seedA, seedB);
                        rng.setSelectedState(0, seedA);
                        rng.setSelectedState(1, seedB);
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
                view.unproject(nextPosition);
                nextPosition.set(MathUtils.clamp(nextPosition.x, 0, bigWidth - 1), MathUtils.clamp(nextPosition.y, 0, bigHeight - 1), nextPosition.z);
                counter = System.currentTimeMillis();
                moveAmount = 0f;
                int bc = wmv.biomeMapper.getBiomeCode((int)(nextPosition.x), (int)(nextPosition.y));
                int code = wmv.biomeMapper.biomeCodeData[(int)(nextPosition.x)][(int)(nextPosition.y)];
                System.out.printf("%c at %s with priority %d and biome A %s, B %s\n", BIOME_CHARS[bc], nextPosition, code >>> 20, wmv.biomeMapper.getBiomeTable()[wmv.biomeMapper.extractPartA(code)], wmv.biomeMapper.getBiomeTable()[wmv.biomeMapper.extractPartB(code)]);
                return true;
            }
        };
        generate(seedA, seedB);
        rng.setSelectedState(0, seedA);
        rng.setSelectedState(1, seedB);
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
        // You can remove the 0.9f and 1.3f parameters if you use a different kind of noise than FOAM.
        // For some reason, Foam's distribution and the equalization that gets applied to land levels don't work well.
        wmv.generate(seedA, seedB, 0.9f, 1.3f);
        wmv.show();
        atlas.clear();
        for (int i = 0; i < 64; i++) {
            Language lang = rng.randomElement(Language.romanizedHumanLanguages)
                    .mix(Language.romanizedLanguages[rng.nextInt(Language.romanizedLanguages.length - 7)], // - 7 removes fancy, imp, and alien langs
                            rng.nextFloat(0.125f)).removeAccents();
            factions.add(new PoliticalMapper.Faction(lang, thesaurus.makeNationName(lang)));
            atlas.put(ArrayTools.letterAt(i), lang);
        }
        final char[][] political = pm.generate(rng.nextLong(), world, wmv.getBiomeMapper(), factions, 64, 1f);
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
                cities.put(points[i], lang.word(rng, true)
                );
//                        .toUpperCase());
            }
        }
        //counter = 0L;
        putMap();
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        // uncomment next line to generate maps as quickly as possible
//        generate(rng.nextLong(), rng.nextLong());
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
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                        display.put(x, y, '≈', toRGBA8888(differentiateLightness(wmv.BIOME_COLOR_TABLE[43], oklab[x][y])));
                        break;
                    case 3:
                        display.put(x, y, '~', toRGBA8888(differentiateLightness(wmv.BIOME_COLOR_TABLE[43], oklab[x][y])));
                        break;
                    default:
                        int bc = dbm.getBiomeCode(x,y);
                        display.put(x, y, BIOME_CHARS[bc], toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[bc], oklab[x][y])));
                }
            }
        }
        int dark = toRGBA8888(0xFE808040);
        for (int i = 0; i < cities.size(); i++) {
            Coord ct = cities.keyAt(i);
            String cname = cities.getAt(i);
            if(cname == null) continue;
            int nationColor = toRGBA8888(differentiateLightness(DescriptiveColor.edit(
                    DescriptiveColor.COLORS_BY_HUE.get((int) (((pm.politicalMap[ct.x][ct.y] + rng.getSelectedState(0)) * 0x9E3779B97F4A7C15L >>> 32) * 48 >>> 32) + 2),
                    0.15f, 0f, 0f, 0f, 1.2f, 0.8f, 0.8f, 1f), 0xFE7F7F40)); // dark gray
            display.put(ct.x, ct.y, '□', toRGBA8888(differentiateLightness(DescriptiveColor.GRAY, oklab[ct.x][ct.y])));
            int pos = ct.x - (cname.length() >> 1);
            if(ct.y >= display.backgrounds[0].length - 1 || pos < 0 || pos + cname.length() >= display.backgrounds.length) continue;
            for (int j = 0; j < cname.length(); pos++, j++) {
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
            }
            else {
                previousPosition.set(nextPosition);
                position.set(nextPosition);
                moveAmount = 0f;
                counter = System.currentTimeMillis();
            }
        }
        camera.position.set(position);

//        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
        batch.setProjectionMatrix(camera.combined);

        temp.set(0, Gdx.graphics.getBackBufferHeight(), 0);
        view.unproject(temp);
        int lowX = MathUtils.floor(temp.x);
        int lowY = MathUtils.floor(temp.y);
        batch.begin();
        display.draw(batch, 0f, 0f, lowX, lowY, lowX + shownWidth + 1, lowY + shownHeight + 1);
        batch.end();

//        batch.begin();
//        display.draw(batch, 0f, 0f, camera.frustum);
//        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        display.resize(width, height);
        view.update(width, height, false);
        view.apply(false);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Demo: Text World Map");
        config.useVsync(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.setWindowedMode(shownWidth * cellWidth, shownHeight * cellHeight);
        new Lwjgl3Application(new WorldMapTextDemo(), config);
    }
}
