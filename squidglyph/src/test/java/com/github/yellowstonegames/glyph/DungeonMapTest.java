package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.place.DungeonProcessor;
import com.github.yellowstonegames.smooth.CoordGlider;
import com.github.yellowstonegames.smooth.Director;
import com.github.tommyettinger.textra.Font;
import java.text.DateFormat;
import java.util.Date;

import static com.badlogic.gdx.Input.Keys.*;
import static com.github.yellowstonegames.core.DescriptiveColor.*;
import static com.github.yellowstonegames.core.MathTools.swayRandomized;

public class DungeonMapTest extends ApplicationAdapter {

    private SpriteBatch batch;
    private GlyphMap gm;
    private DungeonProcessor dungeonProcessor;
    private char[][] bare, dungeon, prunedDungeon;
    private float[][] res, light;
    private Region seen, inView;
    private final Noise waves = new Noise(123, 0.5f, Noise.FOAM, 1);
    private Director<GlidingGlyph> director;
    private ObjectList<GlidingGlyph> glyphs;

    private static final int GRID_WIDTH = 60;
    private static final int GRID_HEIGHT = 32;

    private static final int DEEP_OKLAB = describeOklab("dark dull cobalt");
    private static final int SHALLOW_OKLAB = describeOklab("dull denim");
    private static final int STONE_OKLAB = describeOklab("darkmost gray dullest bronze");
    private static final long deepText = toRGBA8888(offsetLightness(DEEP_OKLAB));
    private static final long shallowText = toRGBA8888(offsetLightness(SHALLOW_OKLAB));
    private static final long stoneText = toRGBA8888(describeOklab("gray dullmost butter bronze"));

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(GRID_WIDTH * 20, GRID_HEIGHT * 20);
        config.disableAudio(true);
        config.setForegroundFPS(120);
        config.useVsync(true);
        new Lwjgl3Application(new DungeonMapTest(), config);
    }

    @Override
    public void create() {
        LaserRandom random = new LaserRandom(Hasher.decarabia.hash64(DateFormat.getDateInstance().format(new Date())));
        batch = new SpriteBatch();
        Font font = KnownFonts.getInconsolataLGC().scaleTo(20f, 20f);
//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        Font font = KnownFonts.getCozette();
//        Font font = KnownFonts.getAStarry();
        gm = new GlyphMap(font, 60, 32);
        GlidingGlyph playerGlyph = new GlidingGlyph((long) describe("red orange") << 32 | '@', Coord.get(1, 1));
        playerGlyph.getLocation().setCompleteRunner(() -> {
            seen.or(inView.refill(FOV.reuseFOV(res, light, playerGlyph.getLocation().getEnd().x, playerGlyph.getLocation().getEnd().y, 6.5f, Radius.CIRCLE), 0.001f, 2f));
            LineTools.pruneLines(dungeon, seen, prunedDungeon);
        });

        glyphs = ObjectList.with(playerGlyph);

        director = new Director<>(GlidingGlyph::getLocation, glyphs);
        director.setDuration(100L);
        dungeonProcessor = new DungeonProcessor(60, 32, random);
        dungeonProcessor.addWater(DungeonProcessor.ALL, 40);
        waves.setFractalType(Noise.RIDGED_MULTI);
        light = new float[GRID_WIDTH][GRID_HEIGHT];
        seen = new Region(GRID_WIDTH, GRID_HEIGHT);
        prunedDungeon = new char[GRID_WIDTH][GRID_HEIGHT];
        inView = new Region(GRID_WIDTH, GRID_HEIGHT);
        Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
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
                        move(Direction.UP);
                        break;
                    case A:
                    case LEFT:
                        move(Direction.LEFT);
                        break;
                    case S:
                    case DOWN:
                        move(Direction.DOWN);
                        break;
                    case D:
                    case RIGHT:
                        move(Direction.RIGHT);
                        break;
                    default: return false;
                }
                return true;
            }
        });

        regenerate();
    }

    public void move(Direction way){
        final CoordGlider cg = glyphs.first().getLocation();
        // this prevents movements from restarting while a slide is already in progress.
        if(cg.getChange() != 0f && cg.getChange() != 1f) return;

        final Coord next = cg.getStart().translate(way);
        if(next.isWithin(GRID_WIDTH, GRID_HEIGHT) && bare[next.x][next.y] == '.') {
            cg.setEnd(next);
            director.play();
        }
    }

    public void regenerate(){
        dungeonProcessor.setPlaceGrid(dungeon = LineTools.hashesToLines(dungeonProcessor.generate(), true));
        bare = dungeonProcessor.getBarePlaceGrid();
        ArrayTools.insert(dungeon, prunedDungeon, 0, 0);
        res = FOV.generateSimpleResistances(bare);
        Coord player = new Region(bare, '.').singleRandom(dungeonProcessor.rng);
        glyphs.first().getLocation().setStart(player);
        glyphs.first().getLocation().setEnd(player);
        seen.remake(inView.refill(FOV.reuseFOV(res, light, player.x, player.y, 6.5f, Radius.CIRCLE), 0.001f, 2f));
        LineTools.pruneLines(dungeon, seen, prunedDungeon);
        gm.backgrounds = new int[GRID_WIDTH][GRID_HEIGHT];
        gm.map.clear();
    }

    public void recolor(){
        Coord player = glyphs.first().location.getStart();
        float modifiedTime = (System.currentTimeMillis() & 0xFFFFFL) * 0x1p-9f;
        FOV.reuseFOV(res, light, player.x, player.y, swayRandomized(12345, modifiedTime) * 2.5f + 4f, Radius.CIRCLE);
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if(inView.contains(x, y)) {
                    switch (prunedDungeon[x][y]) {
                        case '~':
                            gm.backgrounds[x][y] = toRGBA8888(lighten(DEEP_OKLAB, 0.6f * Math.min(1.2f, Math.max(0, light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))));
                            gm.put(x, y, deepText << 32 | prunedDungeon[x][y]);
                            break;
                        case ',':
                            gm.backgrounds[x][y] = toRGBA8888(lighten(SHALLOW_OKLAB, 0.6f * Math.min(1.2f, Math.max(0, light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))));
                            gm.put(x, y, shallowText << 32 | prunedDungeon[x][y]);
                            break;
                        case ' ':
                            gm.backgrounds[x][y] = 0;
                            break;
                        default:
                            gm.backgrounds[x][y] = toRGBA8888(lighten(STONE_OKLAB, 0.6f * light[x][y]));
                            gm.put(x, y, stoneText << 32 | prunedDungeon[x][y]);
                    }
                }
                else if(seen.contains(x, y)){
                    switch (prunedDungeon[x][y]) {
                        case '~':
                            gm.backgrounds[x][y] = toRGBA8888(edit(DEEP_OKLAB, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f, 1f));
                            gm.put(x, y, deepText << 32 | prunedDungeon[x][y]);
                            break;
                        case ',':
                            gm.backgrounds[x][y] = toRGBA8888(edit(SHALLOW_OKLAB, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f, 1f));
                            gm.put(x, y, shallowText << 32 | prunedDungeon[x][y]);
                            break;
                        case ' ':
                            gm.backgrounds[x][y] = 0;
                            break;
                        default:
                            gm.backgrounds[x][y] = toRGBA8888(edit(STONE_OKLAB, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f, 1f));
                            gm.put(x, y, stoneText << 32 | prunedDungeon[x][y]);
                    }
                }
                else {
                    gm.backgrounds[x][y] = 0;
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
