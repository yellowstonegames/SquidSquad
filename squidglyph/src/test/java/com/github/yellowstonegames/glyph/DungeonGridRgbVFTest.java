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
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.core.FullPaletteRgb;
import com.github.yellowstonegames.glyph.rexpaint.XPIO;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.path.DijkstraMap;
import com.github.yellowstonegames.place.DungeonProcessor;

import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.Input.Keys.*;
import static com.github.yellowstonegames.core.DescriptiveColorRgb.*;

public class DungeonGridRgbVFTest extends ApplicationAdapter {

    private Stage stage;
    private Font font;
    private GlyphGrid gg;
    private DungeonProcessor dungeonProcessor;
    private char[][] bare;
    /**
     * Handles field of view calculations as they change when the player moves around; also, lighting with colors.
     */
    private final VisionFrameworkRgb vision = new VisionFrameworkRgb();
    private final Noise waves = new Noise(123, 0.5f, Noise.FOAM, 1);
    private GlyphActor playerGlyph;
    private DijkstraMap playerToCursor;
    private final ObjectDeque<Coord> toCursor = new ObjectDeque<>(100);
    private final ObjectDeque<Coord> awaitedMoves = new ObjectDeque<>(50);
    private Coord cursor = Coord.get(-1, -1);
    private final Vector2 pos = new Vector2();
    private long startTime, lastMove;

    private static final int GRID_WIDTH = 40;
    private static final int GRID_HEIGHT = 25;
    private static final int CELL_WIDTH = 32;
    private static final int CELL_HEIGHT = 32;

    private static final int DEEP_RGBA = describe("dark dull cobalt");
    private static final int SHALLOW_RGBA = describe("dull denim");
    private static final int GRASS_RGBA = describe("duller dark green");
    private static final int DRY_RGBA = describe("dull light apricot sage");
    private static final int STONE_RGBA = describe("darkmost gray dullest bronze");
    private static final int MEMORY_RGBA = describe("darker gray black");
    private static final int MEMORY_FG_RGBA = describe("silver black");
    private static final int deepText = offsetLightness(DEEP_RGBA);
    private static final int shallowText = offsetLightness(SHALLOW_RGBA);
    private static final int grassText = offsetLightness(GRASS_RGBA);
    private static final int stoneText = describe("gray dullmost butter bronze");

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Dungeon Map ('r' rebuilds!)");
        config.setWindowedMode(GRID_WIDTH * CELL_WIDTH, GRID_HEIGHT * CELL_HEIGHT);
        config.disableAudio(true);
        config.setForegroundFPS(360); // shouldn't need to be any faster
        config.useVsync(true);
        new Lwjgl3Application(new DungeonGridRgbVFTest(), config);
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);
        long seed = 12345; // unchanging seed
//        long seed = EnhancedRandom.seedFromMath();// random seed every time
//        long seed = TimeUtils.millis() >>> 21; // use to keep each seed for about half an hour; useful for testing
        Gdx.app.log("SEED", "Initial seed is " + seed);
        EnhancedRandom random = new WhiskerRandom(seed);
        stage = new Stage();
//        Font font = KnownFonts.getIosevka().scaleTo(15f, 25f);
        font = KnownFonts.getInconsolata(Font.DistanceFieldType.SDF);
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

        vision.rememberedColor = DescriptiveColorRgb.describe("darker gray black");

        dungeonProcessor = new DungeonProcessor(GRID_WIDTH, GRID_HEIGHT, random);
        dungeonProcessor.addWater(DungeonProcessor.ALL, 30);
        dungeonProcessor.addGrass(DungeonProcessor.ALL, 10);
        waves.setFractalType(Noise.RIDGED_MULTI);
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
        lastMove = TimeUtils.millis();
        final Coord player = playerGlyph.getLocation();
        final Coord next = Coord.get(Math.round(playerGlyph.getX() + way.deltaX), Math.round(playerGlyph.getY() + way.deltaY));
        if(next.isWithin(GRID_WIDTH, GRID_HEIGHT) && bare[next.x][next.y] == '.') {
            playerGlyph.addAction(MoreActions.slideTo(next.x, next.y, 0.3125f));
            vision.moveViewer(player, next);
            // we can move the player's light now.
            vision.lighting.moveLight(player, next);
        }
        else{
//            if(MathUtils.randomBoolean())
                playerGlyph.addAction(MoreActions.bump(way, 0.3f).append(MoreActions.wiggle(0.2f, 0.2f))
                                        .append(Actions.rotateBy(360f, 1f))
                        .append(new GridAction.ExplosionAction(gg, 1.5f, vision.inView, next, 5))
                );
//            else
//                playerGlyph.addAction(MoreActions.bump(way, 0.3f).append(MoreActions.wiggle(0.2f, 0.2f))
//                    .append(new GridAction.CloudAction(gg, 1.5f, inView, next, 5).useToxicColors()).conclude(post));
//            playerGlyph.addAction(MoreActions.bump(way, 0.3f).append(MoreActions.wiggle(0.125f, 0.2f)));

//            playerGlyph.addAction(MoreActions.bump(way, 0.3f));
//            gg.burst((playerGlyph.getX() + next.x + 1) * 0.5f, (playerGlyph.getY() + next.y + 1) * 0.5f, 1.5f, 7, ',', 0x992200FF, 0x99220000, 0f, 120f, 1f);

//            gg.summon(next.x, next.y, next.x, next.y + 0.5f, '?', 0xFF22CCAA, 0xFF22CC00, 0f, 0f, 1f);
//            gg.addAction(gg.dyeFG(next.x, next.y, 0x992200FF, 1f, Float.POSITIVE_INFINITY, null));
        }
        vision.finishChanges();
    }

    public void regenerate(){
        char[][] dungeon;
        dungeonProcessor.setPlaceGrid(dungeon = LineTools.hashesToLines(dungeonProcessor.generate(), true));
        bare = dungeonProcessor.getBarePlaceGrid();
        Coord player = new Region(bare, '.').singleRandom(dungeonProcessor.rng);
        playerGlyph.setPosition(player.x, player.y);
        vision.restart(dungeon, player, 8);

//        vision.lighting.addLight(player, new Radiance(8, FullPalette.SILVER_GREY, 0f, 0f)); // constant light
        vision.lighting.addLight(player, new Radiance(8, FullPaletteRgb.SILVER_GREY, 0.4f, 0f, 0f, 0.4f)); // flickers

        gg.backgrounds = new int[GRID_WIDTH][GRID_HEIGHT];
        gg.map.clear();
        if(playerToCursor == null)
            playerToCursor = new DijkstraMap(bare, Measurement.EUCLIDEAN);
        else
            playerToCursor.initialize(bare);
        playerToCursor.setGoal(player);
        playerToCursor.partialScan(13, vision.blockage);
        startTime = TimeUtils.millis();
        lastMove = startTime;
    }

    public void recolor(){
        float change = (float) Math.min(Math.max(TimeUtils.timeSinceMillis(lastMove) * 3.0, 0.0), 1000.0);
        vision.update(change);
        final float fraction = change * 0.001f;
        float modifiedTime = (TimeUtils.millis() & 0xFFFFFL) * 0x1p-9f;
        // this could be used if you want the cursor highlight to be all one color.
//        int rainbow = toRGBA8888(
//                limitToGamut(100,
//                        (int) (TrigTools.sinTurns(modifiedTime * 0.2f) * 40f) + 128, (int) (TrigTools.cosTurns(modifiedTime * 0.2f) * 40f) + 128, 255));
        char[][] prunedDungeon = vision.prunedPlaceMap;
        ArrayTools.fill(gg.backgrounds, 0);
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (vision.justSeen.contains(x, y)) {
                    gg.backgrounds[x][y] = (vision.backgroundColors[x][y]);
                    switch (prunedDungeon[x][y]) {
                        case '~':
                            gg.put(x, y, prunedDungeon[x][y], setAlpha(deepText, alpha(vision.getForegroundColor(x,y, change))));
                            break;
                        case ',':
                            gg.put(x, y, prunedDungeon[x][y], setAlpha(shallowText, alpha(vision.getForegroundColor(x,y, change))));
                            break;
                        case '"':
                            gg.put(x, y, prunedDungeon[x][y], setAlpha(grassText, alpha(vision.getForegroundColor(x,y, change))));
                            break;
                        case ' ':
                            break;
                        default:
                            gg.put(x, y, prunedDungeon[x][y], setAlpha(stoneText, alpha(vision.getForegroundColor(x,y, change))));
                    }
                } else if (vision.justHidden.contains(x, y)) {
                    gg.backgrounds[x][y] = lerpColors(vision.previousBackgroundColors[x][y], MEMORY_RGBA, fraction);
                    switch (prunedDungeon[x][y]) {
                        case '~':
                            gg.put(x, y, prunedDungeon[x][y], lerpColors(deepText, MEMORY_FG_RGBA, fraction));
                            break;
                        case ',':
                            gg.put(x, y, prunedDungeon[x][y], lerpColors(shallowText, MEMORY_FG_RGBA, fraction));
                            break;
                        case '"':
                            gg.put(x, y, prunedDungeon[x][y], lerpColors(grassText, MEMORY_FG_RGBA, fraction));
                            break;
                        case ' ':
                            break;
                        default:
                            gg.put(x, y, prunedDungeon[x][y], lerpColors(stoneText, MEMORY_FG_RGBA, fraction));
                    }
                } else if (vision.inView.contains(x, y)) {
                    gg.backgrounds[x][y] = (vision.backgroundColors[x][y]);
                    switch (prunedDungeon[x][y]) {
                        case '~':
                            gg.put(x, y, prunedDungeon[x][y], deepText);
                            break;
                        case ',':
                            gg.put(x, y, prunedDungeon[x][y], shallowText);
                            break;
                        case '"':
                            gg.put(x, y, prunedDungeon[x][y], grassText);
                            break;
                        case ' ':
                            break;
                        default:
                            gg.put(x, y, prunedDungeon[x][y], stoneText);
                    }
                } else if (vision.seen.contains(x, y)) {
                    gg.backgrounds[x][y] = MEMORY_RGBA;
                    if (prunedDungeon[x][y] != ' ') {
                        gg.put(x, y, prunedDungeon[x][y], MEMORY_FG_RGBA);
                    }
                }
            }
        }
//        for (int y = 0; y < GRID_HEIGHT; y++) {
//            for (int x = 0; x < GRID_WIDTH; x++) {
//                if (light[x][y] > 0) {
//                    switch (prunedDungeon[x][y]) {
//                        case '~':
//                            gg.backgrounds[x][y] = (DescriptiveColorRgb.lerpColors(vision.backgroundColors[x][y], lighten(DEEP_RGBA, 0.6f * Math.min(1.2f, Math.max(0, light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))), 0.25f));
//                            gg.put(x, y, prunedDungeon[x][y], deepText);
//                            break;
//                        case ',':
//                            gg.backgrounds[x][y] = (DescriptiveColorRgb.lerpColors(vision.backgroundColors[x][y], lighten(SHALLOW_RGBA, 0.6f * Math.min(1.2f, Math.max(0, light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime)))), 0.25f));
//                            gg.put(x, y, prunedDungeon[x][y], shallowText);
//                            break;
//                        case '"':
//                            gg.backgrounds[x][y] = (DescriptiveColorRgb.lerpColors(vision.backgroundColors[x][y], darken(lerpColors(GRASS_RGBA, DRY_RGBA, waves.getConfiguredNoise(x, y) * 0.5f + 0.5f), 0.4f * Math.min(1.1f, Math.max(0, 1f - light[x][y] + waves.getConfiguredNoise(x, y, modifiedTime * 0.7f)))), 0.25f));
//                            gg.put(x, y, prunedDungeon[x][y], grassText);
//                            break;
//                        case ' ':
//                            break;
//                        default:
//                            gg.backgrounds[x][y] = (DescriptiveColorRgb.lerpColors(vision.backgroundColors[x][y], lighten(STONE_RGBA, 0.6f * light[x][y]), 0.25f));
//                            gg.put(x, y, prunedDungeon[x][y], stoneText);
//                    }
//                } else if (vision.seen.contains(x, y)) {
//                    switch (prunedDungeon[x][y]) {
//                        case '~':
//                            gg.backgrounds[x][y] = (vision.backgroundColors[x][y]);
//                            gg.put(x, y, prunedDungeon[x][y], deepText);
//                            break;
//                        case ',':
//                            gg.backgrounds[x][y] = (vision.backgroundColors[x][y]);
//                            gg.put(x, y, prunedDungeon[x][y], shallowText);
//                            break;
//                        case '"':
//                            gg.backgrounds[x][y] = (vision.backgroundColors[x][y]);
//                            gg.put(x, y, prunedDungeon[x][y], grassText);
//                            break;
//                        case ' ':
//                            break;
//                        default:
//                            gg.backgrounds[x][y] = (vision.backgroundColors[x][y]);
//                            gg.put(x, y, prunedDungeon[x][y], stoneText);
//                    }
//                }
//            }
//        }
        Coord loc = playerGlyph.getLocation();
        gg.put(loc.x, loc.y, 0L);
        for (int i = 0; i < toCursor.size(); i++) {
            Coord curr = toCursor.get(i);
            if(vision.inView.contains(curr))
                gg.backgrounds[curr.x][curr.y] = DescriptiveColorRgb.hsb2rgb(modifiedTime * 0.25f - i * 0.0625f, 0.9f, 1f, 1f);
        }
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
        if(!gg.areChildrenActing() && !awaitedMoves.isEmpty())
        {
            Coord m = awaitedMoves.removeFirst();
            if (!toCursor.isEmpty())
                toCursor.removeFirst();
            move(playerGlyph.getLocation().toGoTo(m));
        }
        else if (!gg.areChildrenActing()) {
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
                    playerToCursor.partialScan(13, vision.blockage);
                }
        } else {
            handleHeldKeys();
        }
        recolor();

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
