package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.grid.LineTools;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.place.DungeonProcessor;

import java.text.DateFormat;
import java.util.Date;

import static com.github.yellowstonegames.core.DescriptiveColor.*;

public class DungeonMapTest extends ApplicationAdapter {

    private SpriteBatch batch;
    private GlyphMap gm;
    private DungeonProcessor dungeonProcessor;
    private final Noise waves = new Noise(123, 0.5f, Noise.FOAM, 1);

    private static final int GRID_WIDTH = 60;
    private static final int GRID_HEIGHT = 32;

    private static final int DEEP_OKLAB = describeOklab("dark dull cobalt");
    private static final int SHALLOW_OKLAB = describeOklab("dull denim");
    private static final int STONE_OKLAB = describeOklab("darkmost gray gray duller butter");

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(GRID_WIDTH * 20, GRID_HEIGHT * 20);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new DungeonMapTest(), config);
    }

    @Override
    public void create() {
        LaserRandom random = new LaserRandom(Hasher.decarabia.hash64(DateFormat.getDateInstance().format(new Date())));
        batch = new SpriteBatch();
        Font font = KnownFonts.getIosevkaSlab().scaleTo(20f, 20f);
//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = KnownFonts.getCozette();
//        font = KnownFonts.getAStarry();
        gm = new GlyphMap(font, 60, 32);

        dungeonProcessor = new DungeonProcessor(60, 32, random);
        dungeonProcessor.addWater(DungeonProcessor.ALL, 15);
        waves.setFractalType(Noise.RIDGED_MULTI);
        regenerate();
    }

    public void regenerate(){
        dungeonProcessor.setPlaceGrid(LineTools.hashesToLines(dungeonProcessor.generate(), true));
        gm.backgrounds = new int[GRID_WIDTH][GRID_HEIGHT];
        gm.map.clear();

        recolor();
    }

    public void recolor(){
        int stone = toRGBA8888(STONE_OKLAB);
        long deepText = toRGBA8888(offsetLightness(DEEP_OKLAB));
        long shallowText = toRGBA8888(offsetLightness(SHALLOW_OKLAB));
        long stoneText = toRGBA8888(offsetLightness(STONE_OKLAB));
        char[][] dungeon = dungeonProcessor.getPlaceGrid();
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                switch (dungeon[x][y]){
                    case '~':
                        gm.backgrounds[x][y] = toRGBA8888(lighten(DEEP_OKLAB, Math.max(0, waves.getConfiguredNoise(x, y, (System.currentTimeMillis() & 0xFFFFFL) * 0x1p-9f))));
                        gm.put(x, y, deepText << 32 | dungeon[x][y]);
                        break;
                    case ',':
                        gm.backgrounds[x][y] = toRGBA8888(lighten(SHALLOW_OKLAB, Math.max(0, waves.getConfiguredNoise(x, y, (System.currentTimeMillis() & 0xFFFFFL) * 0x1p-9f))));
                        gm.put(x, y, shallowText << 32 | dungeon[x][y]);
                        break;
                    case ' ':
                        gm.backgrounds[x][y] = 0;
                        break;
                    default:
                        gm.backgrounds[x][y] = stone;
                        gm.put(x, y, stoneText << 32 | dungeon[x][y]);
                }
            }
        }

    }
    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        if(Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY))
            regenerate();
        else
            recolor();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Camera camera = gm.viewport.getCamera();
        camera.position.set(gm.gridWidth * 0.5f, gm.gridHeight * 0.5f, 0f);
        camera.update();
        gm.viewport.apply(false);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        gm.draw(batch, 0, 0);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gm.resize(width, height);
    }
}
