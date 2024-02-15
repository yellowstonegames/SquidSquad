package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.ds.ObjectFloatMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.DescriptiveColor;
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

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class TechniqueDemo extends ApplicationAdapter {
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
    public static final int gridWidth = 40, gridHeight = 40, cellWidth = 6, cellHeight = 17;
    private int numMonsters = 16;

    private static final int bgColor = FullPaletteRgb.DB_INK;
    private CoordObjectOrderedMap<GlyphActor> teamRed, teamBlue;
    private FloatList redHealth, blueHealth;
    private CoordOrderedSet redPlaces, bluePlaces;
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
        display = new GlyphGrid(KnownFonts.getCozette(), gridWidth * 2, gridHeight, false);
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

        redPlaces = new CoordOrderedSet(numMonsters);
        bluePlaces = new CoordOrderedSet(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Coord monPos = placement.singleRandom(rng);
            placement.remove(monPos);

            GlyphActor ga = new GlyphActor('9', 0xFF0000FF, display.font);
            ga.setLocation(monPos);
            ga.setUserObject(9f);
            teamRed.put(monPos, ga);
            redPlaces.add(monPos);
            display.addActor(ga);

            monPos = placement.singleRandom(rng);
            placement.remove(monPos);

            ga = new GlyphActor('9', 0x0000FFFF, display.font);
            ga.setLocation(monPos);
            ga.setUserObject(9f);
            teamBlue.put(monPos, ga);
            bluePlaces.add(monPos);
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
        display.setPosition(0, 0);
        stage.addActor(display);
    }

    /**
     * Move a unit toward a good position to attack, but don't attack in this method.
     * @param idx the index of the unit in the appropriate ordered Map.
     */
    private void startMove(int idx) {
//        if(health <= 0) return;
        int i = 0;
        DijkstraMap whichDijkstra;
        Technique whichTech;
        CoordOrderedSet whichFoes, whichAllies;
        GlyphActor ae = null;
        float health = 0;
        Coord user = null;
        if(blueTurn)
        {
            whichDijkstra = getToRed;
            whichTech = ((idx & 1) == 0) ? blueBeam : blueBlast;
            whichFoes = redPlaces;
            whichAllies = bluePlaces;
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
            whichFoes = bluePlaces;
            whichAllies = redPlaces;
            ae = teamRed.getAt(idx);
            health = (Float) ae.getUserObject();
            if(health <= 0) {
                phase = Phase.MOVE_ANIM;
                return;
            }
            user = ae.getLocation();
        }
        whichAllies.remove(user);
        /*for(Coord p : whichFoes)
        {
            AnimatedEntity foe = display.getAnimatedEntityByCell(p.x, p.y);
            if(los.isReachable(res, user.x, user.y, p.x, p.y) && foe != null && whichEnemyTeam.get(foe) != null && whichEnemyTeam.get(foe) > 0)
            {
                visibleTargets.add(p);
            }
        }*/
        ObjectDeque<Coord> path = whichDijkstra.findTechniquePath(moveLength, whichTech, res, null, whichFoes, whichAllies, user, whichFoes);
        if(path.isEmpty())
            path = whichDijkstra.findPath(moveLength, whichFoes, whichAllies, user, whichFoes.toArray(new Coord[0]));
        /*
        System.out.println("User at (" + user.x + "," + user.y + ") using " +
                whichTech.name);
        */
        /*
        boolean anyFound = false;

        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                System.out.print((whichDijkstra.targetMap[xx][yy] == null) ? "." : "@");
                anyFound = (whichDijkstra.targetMap[xx][yy] != null) ? true : anyFound;
            }
            System.out.println();
        }*/
        awaitedMoves.clear();
        awaitedMoves.addAll(path);
    }

    public void move(GlyphActor ae, int newX, int newY) {
        Coord n = Coord.get(newX, newY);
        if(!bluePlaces.contains(n) && !redPlaces.contains(n)) {
            ae.addAction(MoreActions.slideTo(newX, newY, 0.075f));
        }
        phase = Phase.MOVE_ANIM;

    }
    
    private void postMove(int idx) {

        int i = 0;
        Technique whichTech;
        CoordOrderedSet whichFoes, whichAllies, visibleTargets = new CoordOrderedSet(8);
        GlyphActor ae = null;
        float health = 0;
        Coord user = null;
        int whichTint = DescriptiveColorRgb.WHITE;
        CoordObjectOrderedMap<GlyphActor> whichEnemyTeam;
        FloatList whichEnemyHealth;
        CoordFloatOrderedMap effects;
        if (blueTurn) {
            whichTech = ((idx & 1) == 0) ? blueBeam : blueBlast;
            whichFoes = redPlaces;
            whichAllies = bluePlaces;
            whichTint = DescriptiveColorRgb.CYAN;
            whichEnemyTeam = teamRed;
            whichEnemyHealth = redHealth;
            ae = teamBlue.getAt(idx);
            health = (Float) ae.getUserObject();
            if (health <= 0) {
                phase = Phase.ATTACK_ANIM;
                return;
            }
            user = ae.getLocation();
        } else {
            whichTech = ((idx & 1) == 0) ? redCloud : redCone;
            whichFoes = bluePlaces;
            whichAllies = redPlaces;
            whichTint = DescriptiveColorRgb.RED;
            whichEnemyTeam = teamBlue;
            whichEnemyHealth = blueHealth;
            ae = teamRed.getAt(idx);
            health = (Float)ae.getUserObject();
            if (health <= 0) {
                phase = Phase.ATTACK_ANIM;
                return;
            }
            user = ae.getLocation();
        }
        for(Coord p : whichFoes)
        {
            GlyphActor foe = whichEnemyTeam.get(p);
            if(los.isReachable(user.x, user.y, p.x, p.y, res) && foe != null && (Float)foe.getUserObject() > 0)
            {
                visibleTargets.add(p);
            }
        }

        CoordObjectOrderedMap<ObjectList<Coord>> ideal = whichTech.idealLocations(user, visibleTargets, whichAllies);
        Coord targetCell = null;
        if(!ideal.isEmpty())
            targetCell = ideal.keyAt(0);

        if(targetCell != null)
        {
            effects = whichTech.apply(user, targetCell);
            GridAction.TintAction tint = new GridAction.TintAction(display, 0.35f, null, colorGrid);
            ArrayTools.fill(tint.colorGrid, 0);
            if(!effects.isEmpty())
                display.addAction(tint);
            for(ObjectFloatMap.Entry<Coord> power : effects.entrySet())
            {
                float strength = ((idx & 1) == 0) ? rng.nextFloat() : power.getValue();
                whichTint = DescriptiveColorRgb.setAlpha(whichTint, strength);
                tint.colorGrid[power.getKey().x][power.getKey().y] = whichTint;
                GlyphActor tgt;
                for(int tgtIdx = 0; tgtIdx < whichEnemyTeam.size(); tgtIdx++)
                {
                    tgt = whichEnemyTeam.getAt(tgtIdx);
                    if(tgt.getLocation().equals(power.getKey()))
                    {
                        float currentHealth = Math.max((Float)tgt.getUserObject() - (1.5f * strength), 0);
                        whichEnemyTeam.setAt(tgtIdx, tgt);
                        tgt.setChar((char)('0' + MathTools.roundPositive(currentHealth)));
                    }
                }
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
        
        whichAllies.add(user);
        phase = Phase.ATTACK_ANIM;
    }
    public void putMap()
    {
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                display.put(i, j, lineDungeon[i][j], FullPaletteRgb.AURORA_STYGIAN_BLUE);
            }
        }
    }
    @Override
    public void render () {
        ScreenUtils.clear(0.13333334f, 0.1254902f, 0.20392157f, 1f); // DB_INK
        // not sure if this is always needed...
        Gdx.gl.glEnable(GL20.GL_BLEND);

        stage.act();
        boolean blueWins = false, redWins = false;
        for(int bh = 0; bh < blueHealth.size; bh++)
        {
            if(blueHealth.get(bh) > 0) {
                redWins = false;
                break;
            }redWins = true;
        }
        for(int rh = 0; rh < redHealth.size; rh++)
        {
            if(redHealth.get(rh) > 0) {
                blueWins = false;
                break;
            }blueWins = true;
        }
        if (blueWins) {
            // still need to display the map, then write over it with a message.
            putMap();
            display.putBoxedString(gridWidth / 2 - 11, gridHeight / 2 - 1, "  BLUE TEAM WINS!  ");
            display.putBoxedString(gridWidth / 2 - 11, gridHeight / 2 + 5, "     q to quit.    ");

            // because we return early, we still need to draw.
            stage.draw();
            // q still needs to quit.
            if(input.hasNext())
                input.next();
            return;
        }
        else if(redWins)
        {
            putMap();
            display.putBoxedString(gridWidth / 2 - 11, gridHeight / 2 - 1, "   RED TEAM WINS!  ");
            display.putBoxedString(gridWidth / 2 - 11, gridHeight / 2 + 5, "     q to quit.    ");

            // because we return early, we still need to draw.
            stage.draw();
            // q still needs to quit.
            if(input.hasNext())
                input.next();
            return;
        }
        int i = 0;
        AnimatedEntity ae = null;
        int whichIdx = 0;
        if(blueTurn) {
            whichIdx = blueIdx;
            ae = teamBlue.get(blueIdx);
        }
        else
        {
            whichIdx = redIdx;
            ae = teamRed.get(redIdx);
        }

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }
        // if the user clicked, we have a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            if(ae == null) {
                awaitedMoves.clear();
            }
            // extremely similar to the block below that also checks if animations are done
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            else if(!display.hasActiveAnimations()) {
                ++framesWithoutAnimation;
                if (framesWithoutAnimation >= 2) {
                    framesWithoutAnimation = 0;
                    Coord m = awaitedMoves.remove(0);
                    move(ae, m.x, m.y);
                }
            }
        }
        // if the previous blocks didn't happen, and there are no active animations, then either change the phase
        // (because with no animations running the last phase must have ended), or start a new animation soon.
        else if(!display.hasActiveAnimations()) {
            ++framesWithoutAnimation;
            if (framesWithoutAnimation >= 2) {
                framesWithoutAnimation = 0;
                switch (phase) {
                    case ATTACK_ANIM: {
                        phase = Phase.MOVE_ANIM;
                        blueTurn = !blueTurn;
                        if(!blueTurn)
                        {
                            whichIdx = (whichIdx + 1) % numMonsters;
                            redIdx = (redIdx + 1) % numMonsters;
                            blueIdx = (blueIdx + 1) % numMonsters;
                        }
                        dijkstraAlert();
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

        // stage has its own batch and must be explicitly told to draw(). this also causes it to act().
        stage.draw();

        OrderedSet<AnimatedEntity> entities = display.getAnimatedEntities(0);
        // display does not draw all AnimatedEntities by default.
        batch.begin();
        for (int j = 0; j < entities.size(); j++) {
            display.drawActor(batch, 1.0f, entities.getAt(j), 0);
        }
        entities = display.getAnimatedEntities(2);
        for (int j = 0; j < entities.size(); j++) {
            display.drawActor(batch, 1.0f, entities.getAt(j), 2);
        }
        /*
        for(AnimatedEntity mon : teamBlue.keySet()) {
                display.drawActor(batch, 1.0f, mon);
        }*/
        // batch must end if it began.
        batch.end();
    }
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib GDX AI Demo");
        config.setWindowedMode(gridWidth * cellWidth * 2, gridHeight * cellHeight);
        config.useVsync(true);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new SquidAIDemo(), config);
    }
}

