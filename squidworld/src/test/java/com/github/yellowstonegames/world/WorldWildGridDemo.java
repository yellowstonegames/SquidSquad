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

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.glyph.GlyphGrid;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordObjectOrderedMap;
import com.github.yellowstonegames.grid.IntPointHash;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.place.WildernessGenerator;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import static com.github.yellowstonegames.core.DescriptiveColor.*;

/**
 * Map generator that uses text to show features at a location as well as color.
 * Port of <a href="https://github.com/zacharycarter/mapgen">Zachary Carter's world generation technique</a>, with
 * substantial changes.
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
 */
public class WorldWildGridDemo extends ApplicationAdapter {
//    private static final int bigWidth = 256, bigHeight = 256;
//    private static final int bigWidth = 1024, bigHeight = 512;
    private static final int bigWidth = 512, bigHeight = 256;
//    private static final int bigWidth = 2048, bigHeight = 1024;
    //private static final int bigWidth = 400, bigHeight = 400;
    private static final float cellWidth = 16, cellHeight = 16;
    private static final int shownWidth = 96, shownHeight = 48;
    private Stage stage;
    private GlyphGrid display, inner;
    private InputProcessor input;
    private Viewport view;
    private Camera camera;
    private DistinctRandom rng;
    private long seed;
    private Vector3 position, previousPosition, nextPosition, temp;
//    private WorldMapGenerator.MimicMap world;
    private HyperellipticalWorldMap world;
    private BlendedWorldMapView wmv;
    private PoliticalMapper pm;
    private Thesaurus thesaurus;
    private ObjectList<PoliticalMapper.Faction> factions;
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
    private final Color INK = new Color(DescriptiveColor.toRGBA8888(Biome.TABLE[60].colorOklab));

    private static final char[] BIOME_CHARS = new char[66];
    static {
        for (int i = 0; i < 66; i++) {
            BIOME_CHARS[i] = Biome.TABLE[i].representation;
        }
    }

    @Override
    public void create() {
        stage = new Stage();
        display = new GlyphGrid(KnownFonts.getIosevka().scaleTo(cellWidth, cellHeight), bigWidth, bigHeight);
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
//// Gdx.files.local("map.txt").writeString(LZSPlus.compress(basis.stringSerialize()), false, "UTF16");
//// you could reload basis without needing the original map image with
//// basis = GreasedRegion.stringDeserialize(LZSPlus.decompress(Gdx.files.local("map.txt").readString("UTF16")));
//// it's also possible to store the compressed map as a String in code, but you need to be careful about escaped chars.
//        world = new WorldMapGenerator.LocalMimicMap(seed, basis, FastNoise.instance, 0.8);
//        pix.dispose();

        WorldMapGenerator.DEFAULT_NOISE.setNoiseType(Noise.FOAM_FRACTAL);
//        world = new WorldMapGenerator.MimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 0.8); // uses a map of Australia for land
        world = new HyperellipticalWorldMap(seed, bigWidth, bigHeight, WorldMapGenerator.DEFAULT_NOISE, 0.8f);
        //world = new WorldMapGenerator.TilingMap(seed, bigWidth, bigHeight, WhirlingNoise.instance, 0.9);
        wmv = new BlendedWorldMapView(world);
        pm = new PoliticalMapper();
        cities = new CoordObjectOrderedMap<>(96);
        factions = new ObjectList<>(80);
        atlas = new IntObjectOrderedMap<>(80);
        thesaurus = new Thesaurus(rng.nextLong(), rng.nextLong());

        position = new Vector3(bigWidth * 0.5f, bigHeight * 0.5f, 0);
        previousPosition = position.cpy();
        nextPosition = position.cpy();
        temp = new Vector3();

/*
        position = new Vector3(bigWidth * 0.25f, bigHeight * 0.25f, 0);
        previousPosition = position.cpy();
        nextPosition = position.cpy();
*/
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
                    putMap();
                    return true;
                }
                if(button == Input.Buttons.RIGHT)
                {
                    previousPosition.set(position);
                    nextPosition.set(screenX, screenY, 0);
                    camera.unproject(nextPosition);
//                    nextPosition.set(MathUtils.round(nextPosition.x), MathUtils.round(nextPosition.y), nextPosition.z);
                    position.set(0.5f * shownWidth, (0.5f * shownHeight), position.z);
                    zoomed = true;
                    final int hash = IntPointHash.hashAll((int)(nextPosition.x), (int)(nextPosition.y), 0x13579BDF);
//                    final int code = wmv.getBiomeMapper().getBiomeCode(MathUtils.clamp((int)(nextPosition.x), 0, bigWidth - 1),
//                            MathUtils.clamp((int) (nextPosition.y), 0, bigHeight - 1));
//                    wildMap = new WildernessGenerator(shownWidth, shownHeight,
//                            Biome.TABLE[code], hash, ~hash);
                    wildMap = new WildernessGenerator.MixedWildernessGenerator(
                            new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode(MathUtils.clamp((int)(nextPosition.x+1), 0, bigWidth - 1), MathUtils.clamp((int) (nextPosition.y+1), 0, bigHeight - 1))], hash, ~hash),
                            new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode(MathUtils.clamp((int)(nextPosition.x+1), 0, bigWidth - 1), MathUtils.clamp((int) (nextPosition.y  ), 0, bigHeight - 1))], hash, ~hash),
                            new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode(MathUtils.clamp((int)(nextPosition.x  ), 0, bigWidth - 1), MathUtils.clamp((int) (nextPosition.y  ), 0, bigHeight - 1))], hash, ~hash),
                            new WildernessGenerator(shownWidth, shownHeight, Biome.TABLE[wmv.getBiomeMapper().getBiomeCode(MathUtils.clamp((int)(nextPosition.x  ), 0, bigWidth - 1), MathUtils.clamp((int) (nextPosition.y+1), 0, bigHeight - 1))], hash, ~hash),
                            rng
                    );
                    wildMap.generate();
                    nextPosition.set(previousPosition);
                    putMap();
                }
                else {
                    previousPosition.set(position);
                    nextPosition.set(screenX, screenY, 0);
                    camera.unproject(nextPosition);
                    nextPosition.set(MathUtils.clamp((nextPosition.x), 0, bigWidth - 1),
                            MathUtils.clamp((nextPosition.y), 0, bigHeight -1), nextPosition.z);
                    counter = System.currentTimeMillis();
                    moveAmount = 0f;
                }
                return true;
            }
        };
        generate(seed);
        rng.setSeed(seed);
        Gdx.input.setInputProcessor(input);
        stage.addActor(display);
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
                cities.put(points[i], lang.word(rng, false).toUpperCase());
            }
        }
        putMap();
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        if(zoomed)
        {
            display.map.clear();
            display.backgrounds = wildMap.colors;
            for (int y = 0; y < wildMap.height; y++) {
                for (int x = 0; x < wildMap.width; x++) {
                    display.put(x, y, wildMap.glyphs[x][y]);
                }
            }
            return;
        }
        display.map.clear();
        display.backgrounds = wmv.getColorMap();
        int[][] oklab = wmv.getColorMapOklab();
        BiomeMapper.BlendedBiomeMapper bbm = wmv.getBiomeMapper();
        int hc, tc, codeA, codeB;
        float mix;
        final int[] colorTable = wmv.biomeMapper.colorTable;
        int[][] heightCodeData = world.heightCodeData;
        for (int y = 0; y < bigHeight; y++) {
            PER_CELL:
            for (int x = 0; x < bigWidth; x++) {
                hc = heightCodeData[x][y];
                if (hc == 1000)
                    continue;
                tc = bbm.heatCodeData[x][y];
                if (tc == 0) {
                    switch (hc) {
                        case 0:
                        case 1:
                        case 2:
                            display.put(x, y, '≈', toRGBA8888(differentiateLightness(darken(colorTable[30], 0.2f), oklab[x][y])));
                            continue PER_CELL;
                        case 3:
                            display.put(x, y, '~', toRGBA8888(differentiateLightness(darken(colorTable[24], 0.2f), oklab[x][y])));
                            continue PER_CELL;
//                        case 4:
//                            display.put(x, y, '¤', toRGBA8888(differentiateLightness(wmv.BIOME_DARK_COLOR_TABLE[42], oklab[x][y])));
//                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                        display.put(x, y, '≈', toRGBA8888(differentiateLightness(darken(colorTable[43], 0.2f), oklab[x][y])));
                        break;
                    case 3:
                        display.put(x, y, '~', toRGBA8888(differentiateLightness(darken(colorTable[43], 0.2f), oklab[x][y])));
                        break;
                    default:
                        int bc = bbm.getBiomeCode(x, y);
                        display.put(x, y, BIOME_CHARS[bc], toRGBA8888(differentiateLightness(darken(colorTable[bc], 0.2f), oklab[x][y])));
                }
            }
        }
        int dark = toRGBA8888(0xFE7F7F40);
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
        ScreenUtils.clear(INK);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if (!nextPosition.epsilonEquals(previousPosition)) {
            moveAmount = (System.currentTimeMillis() - counter) * 0.001f;
            if (moveAmount <= 1f) {
                position.set(previousPosition).lerp(nextPosition, moveAmount);
            } else {
                previousPosition.set(position);
                nextPosition.set(position);
                moveAmount = 0f;
                counter = System.currentTimeMillis();
            }
        }
        camera.position.set(position);
        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");
        camera.update();
        temp.set(0, Gdx.graphics.getBackBufferHeight(), 0);
        view.unproject(temp);
        display.startX = MathUtils.floor(temp.x);
        display.startY = MathUtils.floor(temp.y);
        display.endX = display.startX + shownWidth + 1;
        display.endY = display.startY + shownHeight + 1;
        stage.draw();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
        stage.act();
    }
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        display.resize(width, height);
        view.update(width, height, false);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Demo: World and Wild Map");
        config.useVsync(true);
        config.setWindowedMode((int)(shownWidth * cellWidth), (int)(shownHeight * cellHeight));
        new Lwjgl3Application(new WorldWildGridDemo(), config);
    }
}
