package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.place.DungeonProcessor;
import com.github.yellowstonegames.smooth.CoordGlider;
import com.github.yellowstonegames.smooth.Director;

import java.text.DateFormat;
import java.util.Date;

import static com.badlogic.gdx.Input.Keys.*;
import static com.github.yellowstonegames.core.DescriptiveColor.*;

public class DungeonMapTest extends ApplicationAdapter {

    private SpriteBatch batch;
    private GlyphMap gm;
    private DungeonProcessor dungeonProcessor;
    private char[][] bare;
    private final Noise waves = new Noise(123, 0.5f, Noise.FOAM, 1);
    private Director<GlidingGlyph> director;
    private ObjectList<GlidingGlyph> glyphs;

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
        glyphs = new ObjectList<>(1);
        director = new Director<>(GlidingGlyph::getLocation, glyphs);
        director.setDuration(100L);
        dungeonProcessor = new DungeonProcessor(60, 32, random);
        dungeonProcessor.addWater(DungeonProcessor.ALL, 15);
        waves.setFractalType(Noise.RIDGED_MULTI);

        Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                CoordGlider cg = glyphs.first().getLocation();
                Coord next;
                switch (keycode){
                    case ESCAPE:
                    case Q:
                        Gdx.app.exit();
                        break;
                    case R:
                        regenerate();
                        break;
                    case W:
                    case UP:
                        next = cg.getStart().translate(Direction.UP);
                        if(next.isWithin(GRID_WIDTH, GRID_HEIGHT) && bare[next.x][next.y] == '.') {
                            cg.setEnd(next);
                            director.play();
                        }
                        break;
                    case A:
                    case LEFT:
                        next = cg.getStart().translate(Direction.LEFT);
                        if(next.isWithin(GRID_WIDTH, GRID_HEIGHT) && bare[next.x][next.y] == '.') {
                            cg.setEnd(next);
                            director.play();
                        }
                        break;
                    case S:
                    case DOWN:
                        next = cg.getStart().translate(Direction.DOWN);
                        if(next.isWithin(GRID_WIDTH, GRID_HEIGHT) && bare[next.x][next.y] == '.') {
                            cg.setEnd(next);
                            director.play();
                        }
                        break;
                    case D:
                    case RIGHT:
                        next = cg.getStart().translate(Direction.RIGHT);
                        if(next.isWithin(GRID_WIDTH, GRID_HEIGHT) && bare[next.x][next.y] == '.') {
                            cg.setEnd(next);
                            director.play();
                        }
                        break;
                    default: return false;
                }
                return true;
            }
        });

        regenerate();
    }

    public void regenerate(){
        dungeonProcessor.setPlaceGrid(LineTools.hashesToLines(dungeonProcessor.generate(), true));
        bare = dungeonProcessor.getBarePlaceGrid();
        Coord player = new Region(bare, '.').singleRandom(dungeonProcessor.rng);
        glyphs.clear();
        glyphs.add(new GlidingGlyph((long) describe("red orange") << 32 | '@', player));
        gm.backgrounds = new int[GRID_WIDTH][GRID_HEIGHT];
        gm.map.clear();
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
        recolor();
        director.step();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Camera camera = gm.viewport.getCamera();
        camera.position.set(gm.gridWidth * 0.5f, gm.gridHeight * 0.5f, 0f);
        camera.update();
        gm.viewport.apply(false);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        gm.draw(batch, 0, 0);
        glyphs.first().draw(batch, gm.font);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gm.resize(width, height);
    }
}
