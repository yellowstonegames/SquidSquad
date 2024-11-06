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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.ds.ObjectFloatMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.core.FullPaletteRgb;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.path.DijkstraMap;
import com.github.yellowstonegames.path.technique.BurstAOE;
import com.github.yellowstonegames.path.technique.ConeAOE;
import com.github.yellowstonegames.path.technique.LineAOE;
import com.github.yellowstonegames.path.technique.Technique;
import com.github.yellowstonegames.place.DungeonProcessor;
import com.github.yellowstonegames.place.DungeonTools;
import com.github.yellowstonegames.place.tileset.TilesetType;

public class TechniqueDemo extends ApplicationAdapter {
    private static final float MOVE_DURATION = 0.1f;
    private static final float TINT_DURATION = 0.5f;
//    private static final float MOVE_DURATION = 0.05f;
//    private static final float TINT_DURATION = 0.15f;

    private enum Phase {MOVE_ANIM, ATTACK_ANIM}
    private SpriteBatch batch;

    private Phase phase = Phase.ATTACK_ANIM;
    private EnhancedRandom rng;
    private GlyphGrid display;
    private DungeonProcessor dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private float[][] res;
    private int[][] colorGrid;
    private BresenhamLine los;
    public static final int gridWidth = 40, gridHeight = 40, cellWidth = 16, cellHeight = 24;
    private int numMonsters = 16;

    private CoordObjectOrderedMap<GlyphActor> teamRed, teamBlue;
    private CoordOrderedSet foes = new CoordOrderedSet(16);
    private CoordOrderedSet allies = new CoordOrderedSet(16);
    private CoordOrderedSet visibleTargets = new CoordOrderedSet(8);
    private Technique redCone, redCloud, blueBlast, blueBeam;
    private DijkstraMap getToRed, getToBlue;
    private Stage stage;
    private int framesWithoutAnimation, moveLength = 5;
    private ObjectDeque<Coord> awaitedMoves = new ObjectDeque<>();
    private int redIdx, blueIdx;
    private boolean blueTurn;

    @Override
    public void create () {
        batch = new SpriteBatch();
        display = new GlyphGrid(KnownFonts.getAStarry(Font.DistanceFieldType.MSDF).scaleTo(cellWidth, cellHeight), gridWidth, gridHeight, false);
        display.backgrounds = ArrayTools.fill(FullPaletteRgb.AURORA_CHINCHILLA, gridWidth, gridHeight);
        stage = new Stage(new ScreenViewport(), batch);

        rng = new AceRandom(0x1337BEEF);

        dungeonGen = new DungeonProcessor(gridWidth, gridHeight, rng);

        // change the TilesetType to lots of different choices to see what dungeon works best.
        bareDungeon = dungeonGen.generate(TilesetType.ROUND_ROOMS_DIAGONAL_CORRIDORS);
        bareDungeon = DungeonTools.closeDoors(bareDungeon);
        lineDungeon = LineTools.hashesToLines(bareDungeon);
        Region placement = new Region(bareDungeon, '.');

        colorGrid = new int[gridWidth][gridHeight];

        teamRed = new CoordObjectOrderedMap<>(numMonsters);
        teamBlue = new CoordObjectOrderedMap<>(numMonsters);

        for(int i = 0; i < numMonsters; i++)
        {
            Coord monPos = placement.singleRandom(rng);
            placement.remove(monPos);

            GlyphActor ga = new GlyphActor('9', 0xFF0000FF, display.font);
            ga.setLocation(monPos);
            ga.setUserObject(9f);
            teamRed.put(monPos, ga);
            display.addActor(ga);

            monPos = placement.singleRandom(rng);
            placement.remove(monPos);

            ga = new GlyphActor('9', 0x0000FFFF, display.font);
            ga.setLocation(monPos);
            ga.setUserObject(9f);
            teamBlue.put(monPos, ga);
            display.addActor(ga);
        }
        // your choice of FOV matters here.
        los = new BresenhamLine();
        res = FOV.generateResistances(bareDungeon);

        ConeAOE cone = new ConeAOE(Coord.get(0, 0), 9, 0, 60, Radius.CIRCLE);
        cone.setMinRange(1);
        cone.setMaxRange(9);
        cone.setMetric(Radius.SQUARE);

        redCone = new Technique("Burning Breath", cone);
        redCone.setMap(bareDungeon);

        BurstAOE blast = new BurstAOE(Coord.get(0, 0), 3, Radius.CIRCLE);
        blast.setMinRange(4);
        blast.setMaxRange(8);
        blast.setMetric(Radius.CIRCLE);

        blueBlast = new Technique("Winter Orb", blast);
        blueBlast.setMap(bareDungeon);

        BurstAOE cloud = new BurstAOE(Coord.get(0, 0), 4, Radius.DIAMOND);
        cloud.setMinRange(4);
        cloud.setMaxRange(5);
        cloud.setMetric(Radius.CIRCLE);

        redCloud = new Technique("Acid Mist", cloud);
        redCloud.setMap(bareDungeon);

        LineAOE beam = new LineAOE(Coord.get(0, 0), Coord.get(8, 0), 1, Radius.CIRCLE);
        beam.setMinRange(2);
        beam.setMaxRange(8);
        beam.setMetric(Radius.CIRCLE);
        blueBeam = new Technique("Atomic Death Ray", beam);
        blueBeam.setMap(bareDungeon);


        getToRed = new DijkstraMap(bareDungeon, Measurement.EUCLIDEAN);
        getToBlue = new DijkstraMap(bareDungeon, Measurement.EUCLIDEAN);

        awaitedMoves = new ObjectDeque<>(10);

        // just quit if we get a Q.
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                switch (keycode){
                    case Input.Keys.Q:
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        return true;
                }
                return false;
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        // and then add display, our one visual component, to the list of things that act in Stage.
        stage.addActor(display);
    }

    /**
     * Move a unit toward a good position to attack, but don't attack in this method.
     * @param idx the index of the unit in the appropriate ordered Map.
     */
    private void startMove(int idx) {
//        if(health <= 0) return;
        DijkstraMap whichDijkstra;
        Technique whichTech;
        foes.clear();
        allies.clear();
        GlyphActor ae;
        float health;
        Coord user;
        if(blueTurn)
        {
            whichDijkstra = getToRed;
            whichTech = ((idx & 1) == 0) ? blueBeam : blueBlast;
            foes.addAll(teamRed.order());
            allies.addAll(teamBlue.order());
            ae = teamBlue.getAt(idx);
            health = (Float) ae.getUserObject();
            if(health <= 0) {
                phase = Phase.MOVE_ANIM;
                return;
            }
            user = ae.getLocation();
        }
        else
        {
            whichDijkstra = getToBlue;
            whichTech = ((idx & 1) == 0) ? redCloud : redCone;
            foes.addAll(teamBlue.order());
            allies.addAll(teamRed.order());
            ae = teamRed.getAt(idx);
            health = (Float) ae.getUserObject();
            if(health <= 0) {
                phase = Phase.MOVE_ANIM;
                return;
            }
            user = ae.getLocation();
        }
        allies.remove(user);
        ObjectDeque<Coord> path = whichDijkstra.findTechniquePath(moveLength, whichTech, res, null, foes, allies, user, foes);
        if(path.isEmpty())
            path = whichDijkstra.findPath(moveLength, foes, allies, user, foes.toArray(new Coord[0]));
        awaitedMoves.clear();
        awaitedMoves.addAll(path);
    }

    public void move(GlyphActor ae, Coord n) {
        if(!teamBlue.containsKey(n) && !teamRed.containsKey(n)) {
            Coord old = ae.getLocation();
            ae.addAction(MoreActions.slideTo(n.x, n.y, MOVE_DURATION));
            if(teamBlue.containsKey(old))
            {
                teamBlue.alter(old, n);
            }
            else if(teamRed.containsKey(old))
            {
                teamRed.alter(old, n);
            }
        }
        phase = Phase.MOVE_ANIM;

    }
    
    private void postMove(int idx) {

        Technique whichTech;
        visibleTargets.clear();
        foes.clear();
        allies.clear();
        GlyphActor ae;
        float health;
        Coord user;
        int whichTint;
        CoordObjectOrderedMap<GlyphActor> whichEnemyTeam;
        CoordFloatOrderedMap effects;
        if (blueTurn) {
            whichTech = ((idx & 1) == 0) ? blueBeam : blueBlast;
            foes.addAll(teamRed.order());
            allies.addAll(teamBlue.order());
            whichTint = DescriptiveColorRgb.CYAN;
            whichEnemyTeam = teamRed;
            ae = teamBlue.getAt(idx);
            health = (Float) ae.getUserObject();
            if (health <= 0) {
                phase = Phase.ATTACK_ANIM;
                return;
            }
            user = ae.getLocation();
        } else {
            whichTech = ((idx & 1) == 0) ? redCloud : redCone;
            foes.addAll(teamBlue.order());
            allies.addAll(teamRed.order());
            whichTint = DescriptiveColorRgb.RED;
            whichEnemyTeam = teamBlue;
            ae = teamRed.getAt(idx);
            health = (Float)ae.getUserObject();
            if (health <= 0) {
                phase = Phase.ATTACK_ANIM;
                return;
            }
            user = ae.getLocation();
        }
        for(Coord p : foes)
        {
            GlyphActor foe = whichEnemyTeam.get(p);
            if(los.isReachable(user.x, user.y, p.x, p.y, res) && foe != null && (Float)foe.getUserObject() > 0)
            {
                visibleTargets.add(p);
            }
        }

        CoordObjectOrderedMap<ObjectList<Coord>> ideal = whichTech.idealLocations(user, visibleTargets, allies);
        Coord targetCell = null;
        if(!ideal.isEmpty())
            targetCell = ideal.keyAt(0);

        if(targetCell != null)
        {
            effects = whichTech.apply(user, targetCell);
            ArrayTools.fill(colorGrid, 0);
            for(ObjectFloatMap.Entry<Coord> power : effects.entrySet())
            {
                float strength = ((idx & 1) == 0) ? rng.nextFloat() : power.getValue();
                whichTint = DescriptiveColorRgb.setAlpha(whichTint, strength);
                colorGrid[power.getKey().x][power.getKey().y] = whichTint;
                GlyphActor tgt;
                for(int tgtIdx = 0; tgtIdx < whichEnemyTeam.size(); tgtIdx++)
                {
                    tgt = whichEnemyTeam.getAt(tgtIdx);
                    if(tgt.getLocation().equals(power.getKey()))
                    {
                        float currentHealth = Math.max((Float)tgt.getUserObject() - (3.5f * strength) - 1, 0);
                        tgt.setUserObject(currentHealth);
                        tgt.setChar((char)('0' + MathTools.ceilPositive(currentHealth)));
                    }
                }
            }
            for (int i = teamBlue.size()-1; i >= 0; i--) {
                if((Float)teamBlue.getAt(i).getUserObject() <= 0f)
                    display.removeActor(teamBlue.removeAt(i));
            }
            for (int i = teamRed.size()-1; i >= 0; i--) {
                if((Float)teamRed.getAt(i).getUserObject() <= 0f)
                    display.removeActor(teamRed.removeAt(i));
            }
            if(!effects.isEmpty())
            {
                display.addAction(new GridAction.TintAction(display, TINT_DURATION, null, colorGrid));
            }
        }
        else
        {
////
////            System.out.println("NO ATTACK POSITION: User at (" + user.x + "," + user.y + ") using " +
////                    whichTech.name);
//            display.tint(user.x * 2    , user.y, SColor.GOLDEN_YELLOW, 0, display.getAnimationDuration() * 3);
//            display.tint(user.x * 2 + 1, user.y, SColor.GOLDEN_YELLOW, 0, display.getAnimationDuration() * 3);
        }
        
        allies.add(user);
        phase = Phase.ATTACK_ANIM;
    }
    public void putMap()
    {
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                display.put(i, j, lineDungeon[i][j], FullPaletteRgb.AURORA_STYGIAN_BLUE);
                display.backgrounds[i][j] = FullPaletteRgb.AURORA_CHINCHILLA;
            }
        }
    }
    @Override
    public void render () {
        ScreenUtils.clear(0.13333334f, 0.1254902f, 0.20392157f, 1f); // DB_INK
        // not sure if this is always needed...
        Gdx.gl.glEnable(GL20.GL_BLEND);

        putMap();

        Camera camera = display.viewport.getCamera();
        camera.position.set(display.gridWidth * 0.5f, display.gridHeight * 0.5f, 0f);
        camera.update();
        stage.act();
        boolean blueWins = teamRed.isEmpty(), redWins = teamBlue.isEmpty();
        if (blueWins) {
            // still need to display the map, then write over it with a message.
            System.out.println("  BLUE TEAM WINS!  ");

            // because we return early, we still need to draw.
            stage.draw();
            return;
        }
        else if(redWins)
        {
            System.out.println("   RED TEAM WINS!  ");

            // because we return early, we still need to draw.
            stage.draw();
            return;
        }
        int i = 0;
        GlyphActor ae;
        int whichIdx;
        if(blueTurn) {
            whichIdx = blueIdx;
            ae = teamBlue.getAt(blueIdx);
        }
        else
        {
            whichIdx = redIdx;
            ae = teamRed.getAt(redIdx);
        }

        // there is a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            if(ae == null) {
                awaitedMoves.clear();
            }
            // extremely similar to the block below that also checks if animations are done
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            else if(!display.isAnythingActing()) {
                ++framesWithoutAnimation;
                if (framesWithoutAnimation >= 2) {
                    framesWithoutAnimation = 0;
                    Coord m = awaitedMoves.removeFirst();
                    move(ae, m);
                }
            }
        }
        // if the previous blocks didn't happen, and there are no active animations, then either change the phase
        // (because with no animations running the last phase must have ended), or start a new animation soon.
        else if(!display.isAnythingActing()) {
            ++framesWithoutAnimation;
            if (framesWithoutAnimation >= 2) {
                framesWithoutAnimation = 0;
                switch (phase) {
                    case ATTACK_ANIM: {
                        phase = Phase.MOVE_ANIM;
                        blueTurn = !blueTurn;
                        if(blueTurn) {
                            blueIdx = (blueIdx + 1) % teamBlue.size();
                            whichIdx = blueIdx;
                        } else {
                            redIdx = (redIdx + 1) % teamRed.size();
                            whichIdx = redIdx;
                        }
                        startMove(whichIdx);
                    }
                    break;
                    case MOVE_ANIM: {
                        postMove(whichIdx);
                    }
                }
            }
        }
        // if we do have an animation running, then how many frames have passed with no animation needs resetting
        else
        {
            framesWithoutAnimation = 0;
        }
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        display.resize(width, height);
    }

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Technique Demo");
        config.setWindowedMode(gridWidth * cellWidth, gridHeight * cellHeight);
        config.useVsync(true);
        new Lwjgl3Application(new TechniqueDemo(), config);
    }
}

