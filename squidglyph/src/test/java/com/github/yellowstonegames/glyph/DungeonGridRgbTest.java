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

package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.LineWobble;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.glyph.rexpaint.XPIO;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.path.DijkstraMap;
import com.github.yellowstonegames.place.DungeonProcessor;

import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.Input.Keys.*;
import static com.github.yellowstonegames.core.DescriptiveColorRgb.*;

public class DungeonGridRgbTest extends ApplicationAdapter {

    private Stage stage;
    private Font font;
    private GlyphGrid gg;
    private DungeonProcessor dungeonProcessor;
    private char[][] bare, dungeon, prunedDungeon;
    private float[][] res, light;
    private Region seen, inView, blockage;
    private final Noise waves = new Noise(123, 0.5f, Noise.FOAM, 1);
    private GlyphActor playerGlyph;
    private DijkstraMap playerToCursor;
    private final ObjectDeque<Coord> toCursor = new ObjectDeque<>(100);
    private final ObjectDeque<Coord> awaitedMoves = new ObjectDeque<>(50);
    private Coord cursor = Coord.get(-1, -1);
    private final Vector2 pos = new Vector2();
    private Runnable post;

    private static final int GRID_WIDTH = 40;
    private static final int GRID_HEIGHT = 25;
    private static final int CELL_WIDTH = 32;
    private static final int CELL_HEIGHT = 32;

    private static final int DEEP_COLOR = describeRgb("dark dull cobalt");
    private static final int SHALLOW_COLOR = describeRgb("dull denim");
    private static final int GRASS_COLOR = describeRgb("duller dark green");
    private static final int DRY_COLOR = describeRgb("dull light apricot sage");
    private static final int STONE_COLOR = describeRgb("darkmost gray dullest bronze");
    private static final int deepText = (offsetLightness(DEEP_COLOR));
    private static final int shallowText = (offsetLightness(SHALLOW_COLOR));
    private static final int grassText = (offsetLightness(GRASS_COLOR));
    private static final int stoneText = (describeRgb("gray dullmost butter bronze"));

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Dungeon Map ('r' rebuilds!)");
        config.setWindowedMode(GRID_WIDTH * CELL_WIDTH, GRID_HEIGHT * CELL_HEIGHT);
        config.disableAudio(true);
        config.setForegroundFPS(360); // shouldn't need to be any faster
        config.useVsync(true);
        new Lwjgl3Application(new DungeonGridRgbTest(), config);
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);
        long seed = EnhancedRandom.seedFromMath();// random seed every time
//        long seed = TimeUtils.millis() >>> 21; // use to keep each seed for about half an hour; useful for testing
        Gdx.app.log("SEED", "Initial seed is " + seed);
        EnhancedRandom random = new WhiskerRandom(seed);
        stage = new Stage();
//        Font font = KnownFonts.getIosevka().scaleTo(15f, 25f);
        font = KnownFonts.getInconsolata(Font.DistanceFieldType.MSDF).adjustLineHeight(1.25f).setCrispness(0.5f);
//        Font font = KnownFonts.getIosevka().scaleTo(15f, 25f).setDescent(0f).adjustLineHeight(1.25f);

//        Font font = KnownFonts.getIosevkaSlab().scaleTo(15f, 25f);
//        Font font = KnownFonts.getCascadiaMonoMSDF().scaleTo(15f, 25f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        Font font = KnownFonts.getIosevkaSlabSDF().scaleTo(12f, 26f);
//        Font font = KnownFonts.getGoNotoUniversalSDF().scaleTo(30f, 25f).setCrispness(0.4f);
//        Font font = KnownFonts.getInconsolata();
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        Font font = KnownFonts.getCozette();
//        Font font = KnownFonts.getAStarry();
//        Font font = KnownFonts.getIosevkaMSDF().scaleTo(24, 24);
//        Font font = KnownFonts.getAStarry().scaleTo(16, 16);
//        Font font = KnownFonts.getAStarry().fitCell(24, 24, true);
//        Font font = KnownFonts.getInconsolataMSDF().fitCell(24, 24, true);
        gg = new GlyphGrid(font, GRID_WIDTH, GRID_HEIGHT, true);
        //use Ă to test glyph height
        playerGlyph = new GlyphActor('@', "[red orange]", gg.font);
        gg.addActor(playerGlyph);
        post = () -> {
            seen.or(inView.refill(FOV.reuseFOV(res, light,
                    Math.round(playerGlyph.getX()), Math.round(playerGlyph.getY()), 6.5f, Radius.CIRCLE), 0.001f, 999f));
            blockage.remake(seen).not().fringe8way();
            LineTools.pruneLines(dungeon, seen, prunedDungeon);
            if(!awaitedMoves.isEmpty())
                awaitedMoves.removeFirst();
            playerToCursor.clearGoals();
            playerToCursor.resetMap();
            playerToCursor.setGoal(playerGlyph.getLocation());
            playerToCursor.partialScan(13, blockage);
        };

        dungeonProcessor = new DungeonProcessor(GRID_WIDTH, GRID_HEIGHT, random);
        dungeonProcessor.addWater(DungeonProcessor.ALL, 30);
        dungeonProcessor.addGrass(DungeonProcessor.ALL, 10);
        waves.setFractalType(Noise.RIDGED_MULTI);
        light = new float[GRID_WIDTH][GRID_HEIGHT];
        seen = new Region(GRID_WIDTH, GRID_HEIGHT);
        blockage = new Region(GRID_WIDTH, GRID_HEIGHT);
        prunedDungeon = new char[GRID_WIDTH][GRID_HEIGHT];
        inView = new Region(GRID_WIDTH, GRID_HEIGHT);
        input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode){
                    case ESCAPE:
                    case Q:
                        Gdx.app.exit();
                        break;
                    case R:
                        regenerate();
                        break;
                    case ENTER:
                    case NUMPAD_ENTER:
                        XPIO.saveXP(Gdx.files.local("Dungeon" + System.currentTimeMillis() + ".xp"), gg);
                        break;
                    default: return false;
                }
                return true;
            }
            // if the user clicks and mouseMoved hasn't already assigned a path to toCursor, then we call mouseMoved
            // ourselves and copy toCursor over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                pos.set(screenX, screenY);
                gg.viewport.unproject(pos);
                if (onGrid(MathUtils.floor(pos.x), MathUtils.floor(pos.y))) {
                    mouseMoved(screenX, screenY);
                    awaitedMoves.addAll(toCursor);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return mouseMoved(screenX, screenY);
            }

            // causes the path to the mouse position to become highlighted (toCursor contains a list of Coords that
            // receive highlighting). Uses DijkstraMap.findPathPreScanned() to find the path, which is rather fast.
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if(!awaitedMoves.isEmpty())
                    return false;
                pos.set(screenX, screenY);
                gg.viewport.unproject(pos);
                if (onGrid(screenX = MathUtils.floor(pos.x), screenY = MathUtils.floor(pos.y))) {
                    // we also need to check if screenX or screenY is the same cell.
                    if (cursor.x == screenX && cursor.y == screenY || gg.areChildrenActing()) {
                        return false;
                    }
                    cursor = Coord.get(screenX, screenY);
                    // This uses DijkstraMap.findPathPreScannned() to get a path as a List of Coord from the current
                    // player position to the position the user clicked on. The "PreScanned" part is an optimization
                    // that's special to DijkstraMap; because the part of the map that is viable to move into has
                    // already been fully analyzed by the DijkstraMap.partialScan() method at the start of the
                    // program, and re-calculated whenever the player moves, we only need to do a fraction of the
                    // work to find the best path with that info.
                    toCursor.clear();
                    playerToCursor.findPathPreScanned(toCursor, cursor);
                    // findPathPreScanned includes the current cell (goal) by default, which is helpful when
                    // you're finding a path to a monster or loot, and want to bump into it, but here can be
                    // confusing because you would "move into yourself" as your first move without this.
                    if (!toCursor.isEmpty()) {
                        toCursor.removeFirst();
                    }
                }
                return false;
            }
        });

        regenerate();
        stage.addActor(gg);
    }

    public void move(Direction way){

        // this prevents movements from restarting while a slide is already in progress.
        if(playerGlyph.hasActions()) return;

        final Coord next = Coord.get(Math.round(playerGlyph.getX() + way.deltaX), Math.round(playerGlyph.getY() + way.deltaY));
        if(next.isWithin(GRID_WIDTH, GRID_HEIGHT) && bare[next.x][next.y] == '.') {
            playerGlyph.addAction(MoreActions.slideTo(next.x, next.y, 0.2f, post));
        }
        else{
//            if(MathUtils.randomBoolean())
                playerGlyph.addAction(MoreActions.bump(way, 0.3f).append(MoreActions.wiggle(0.2f, 0.2f))
                                        .append(Actions.rotateBy(360f, 1f))
                        .append(new GridAction.ExplosionAction(gg, 1.5f, inView, next, 5))
                        .conclude(post));
//            else
//                playerGlyph.addAction(MoreActions.bump(way, 0.3f).append(MoreActions.wiggle(0.2f, 0.2f))
//                    .append(new GridAction.CloudAction(gg, 1.5f, inView, next, 5).useToxicColors()).conclude(post));
//            playerGlyph.addAction(MoreActions.bump(way, 0.3f).append(MoreActions.wiggle(0.125f, 0.2f)));

//            playerGlyph.addAction(MoreActions.bump(way, 0.3f));
//            gg.burst((playerGlyph.getX() + next.x + 1) * 0.5f, (playerGlyph.getY() + next.y + 1) * 0.5f, 1.5f, 7, ',', 0x992200FF, 0x99220000, 0f, 120f, 1f);

//            gg.summon(next.x, next.y, next.x, next.y + 0.5f, '?', 0xFF22CCAA, 0xFF22CC00, 0f, 0f, 1f);
//            gg.addAction(gg.dyeFG(next.x, next.y, 0x992200FF, 1f, Float.POSITIVE_INFINITY, null));
        }
    }

    public void regenerate(){
        dungeonProcessor.setPlaceGrid(dungeon = LineTools.hashesToLines(dungeonProcessor.generate(), true));
        bare = dungeonProcessor.getBarePlaceGrid();
        ArrayTools.insert(dungeon, prunedDungeon, 0, 0);
        res = FOV.generateSimpleResistances(bare);
        Coord player = new Region(bare, '.').singleRandom(dungeonProcessor.rng);
        playerGlyph.setPosition(player.x, player.y);
        seen.remake(inView.refill(FOV.reuseFOV(res, light, player.x, player.y, 6.5f, Radius.CIRCLE), 0.001f, 2f));
        blockage.remake(seen).not().fringe8way();
        LineTools.pruneLines(dungeon, seen, prunedDungeon);
        gg.backgrounds = new int[GRID_WIDTH][GRID_HEIGHT];
        gg.map.clear();
        if(playerToCursor == null)
            playerToCursor = new DijkstraMap(bare, Measurement.EUCLIDEAN);
        else
            playerToCursor.initialize(bare);
        playerToCursor.setGoal(player);
        playerToCursor.partialScan(13, blockage);
    }

    public void recolor(){
        int playerX = Math.round(playerGlyph.getX());
        int playerY = Math.round(playerGlyph.getY());
        float modifiedTime = (TimeUtils.millis() & 0xFFFFFL) * 0x1p-9f;
        // this could be used if you want the cursor highlight to be all one color.
//        int rainbow = toRGBA8888(
//                limitToGamut(100,
//                        (int) (TrigTools.sinTurns(modifiedTime * 0.2f) * 40f) + 128, (int) (TrigTools.cosTurns(modifiedTime * 0.2f) * 40f) + 128, 255));
        FOV.reuseFOV(res, light, playerX, playerY, LineWobble.bicubicWobble(12345, modifiedTime) * 2.5f + 4f, Radius.CIRCLE);
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (inView.contains(x, y)) {
                    int idx = toCursor.indexOf(Coord.get(x, y));
                    if(idx != -1){
                        gg.backgrounds[x][y] = DescriptiveColorRgb.hsb2rgb(modifiedTime * 0.25f - idx * 0.0625f, 0.9f, 1f, 1f);
                        gg.put(x, y, prunedDungeon[x][y], stoneText);
                    }
                    else {
                        switch (prunedDungeon[x][y]) {
                            case '~':
                                gg.backgrounds[x][y] = (lighten(DEEP_COLOR, 0.6f * Math.min(1.2f, Math.max(0, light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))));
                                gg.put(x, y, prunedDungeon[x][y], deepText);
                                break;
                            case ',':
                                gg.backgrounds[x][y] = (lighten(SHALLOW_COLOR, 0.6f * Math.min(1.2f, Math.max(0, light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))));
                                gg.put(x, y, prunedDungeon[x][y], shallowText);
                                break;
                            case '"':
                                gg.backgrounds[x][y] = (darken(lerpColors(GRASS_COLOR, DRY_COLOR, waves.getConfiguredNoise(x, y) * 0.5f + 0.5f), 0.4f * Math.min(1.1f, Math.max(0, 1f - light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime * 0.7f)))));
                                gg.put(x, y, prunedDungeon[x][y], grassText);
                                break;
                            case ' ':
                                gg.backgrounds[x][y] = 0;
                                break;
                            default:
                                gg.backgrounds[x][y] = (lighten(STONE_COLOR, 0.6f * light[x][y]));
                                gg.put(x, y, prunedDungeon[x][y], stoneText);
                        }
                    }
                } else if (seen.contains(x, y)) {
                    switch (prunedDungeon[x][y]) {
                        case '~':
                            gg.backgrounds[x][y] = (evaluateHsl(DEEP_COLOR, null, null, DungeonGridRgbTest::slightlyDarken));
                            gg.put(x, y, prunedDungeon[x][y], deepText);
                            break;
                        case ',':
                            gg.backgrounds[x][y] = (evaluateHsl(SHALLOW_COLOR, null, null, DungeonGridRgbTest::slightlyDarken));
                            gg.put(x, y, prunedDungeon[x][y], shallowText);
                            break;
                        case ' ':
                            gg.backgrounds[x][y] = 0;
                            break;
                        default:
                            gg.backgrounds[x][y] = (evaluateHsl(STONE_COLOR, null, null, DungeonGridRgbTest::slightlyDarken));
                            gg.put(x, y, prunedDungeon[x][y], stoneText);
                    }
                } else {
                    gg.backgrounds[x][y] = 0;
                }
            }
        }
        gg.map.remove(playerGlyph.getLocation());
    }

    private static float slightlyDarken(float lightness) {
        return 0.7f * lightness;
    }

    /**
     * Supports WASD, vi-keys (hjklyubn), arrow keys, and numpad for movement, plus '.' or numpad 5 to stay still.
     */
    public void handleHeldKeys() {
        if(input.isKeyPressed(A) || input.isKeyPressed(H) || input.isKeyPressed(LEFT) || input.isKeyPressed(NUMPAD_4))
            move(Direction.LEFT);
        else if(input.isKeyPressed(S) || input.isKeyPressed(J) || input.isKeyPressed(DOWN) || input.isKeyPressed(NUMPAD_2))
            move(Direction.DOWN);
        else if(input.isKeyPressed(W) || input.isKeyPressed(K) || input.isKeyPressed(UP) || input.isKeyPressed(NUMPAD_8))
            move(Direction.UP);
        else if(input.isKeyPressed(D) || input.isKeyPressed(L) || input.isKeyPressed(RIGHT) || input.isKeyPressed(NUMPAD_6))
            move(Direction.RIGHT);
        else if(input.isKeyPressed(Y) || input.isKeyPressed(NUMPAD_7))
            move(Direction.UP_LEFT);
        else if(input.isKeyPressed(U) || input.isKeyPressed(NUMPAD_9))
            move(Direction.UP_RIGHT);
        else if(input.isKeyPressed(B) || input.isKeyPressed(NUMPAD_1))
            move(Direction.DOWN_LEFT);
        else if(input.isKeyPressed(N) || input.isKeyPressed(NUMPAD_3))
            move(Direction.DOWN_RIGHT);
        else if(input.isKeyPressed(PERIOD) || input.isKeyPressed(NUMPAD_5) || input.isKeyPressed(NUMPAD_DOT))
            move(Direction.NONE);
    }

    @Override
    public void render() {
        recolor();
        handleHeldKeys();

        if(!gg.areChildrenActing() && !awaitedMoves.isEmpty())
        {
            Coord m = awaitedMoves.peekFirst();
            if (!toCursor.isEmpty())
                toCursor.removeFirst();
            move(playerGlyph.getLocation().toGoTo(m));
        }
        else {
            if (!gg.areChildrenActing()) {
//                postMove();
                // this only happens if we just removed the last Coord from awaitedMoves, and it's only then that we need to
                // re-calculate the distances from all cells to the player. We don't need to calculate this information on
                // each part of a many-cell move (just the end), nor do we need to calculate it whenever the mouse moves.
                if (awaitedMoves.isEmpty()) {
                    // the next two lines remove any lingering data needed for earlier paths
                    playerToCursor.clearGoals();
                    playerToCursor.resetMap();
                    // the next line marks the player as a "goal" cell, which seems counter-intuitive, but it works because all
                    // cells will try to find the distance between themselves and the nearest goal, and once this is found, the
                    // distances don't change as long as the goals don't change. Since the mouse will move and new paths will be
                    // found, but the player doesn't move until a cell is clicked, the "goal" is the non-changing cell, so the
                    // player's position, and the "target" of a pathfinding method like DijkstraMap.findPathPreScanned() is the
                    // currently-moused-over cell, which we only need to set where the mouse is being handled.
                    playerToCursor.setGoal(playerGlyph.getLocation());
                    // DijkstraMap.partialScan only finds the distance to get to a cell if that distance is less than some limit,
                    // which is 13 here. It also won't try to find distances through an impassable cell, which here is the blockage
                    // GreasedRegion that contains the cells just past the edge of the player's FOV area.
                    playerToCursor.partialScan(13, blockage);
                }
            }
        }

        ScreenUtils.clear(Color.BLACK);
        Camera camera = gg.viewport.getCamera();
        camera.position.set(gg.gridWidth * 0.5f, gg.gridHeight * 0.5f, 0f);
        camera.update();
        stage.act();
        stage.draw();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gg.resize(width, height);
    }

    private boolean onGrid(int screenX, int screenY)
    {
        return screenX >= 0 && screenX < GRID_WIDTH && screenY >= 0 && screenY < GRID_HEIGHT;
    }

}
