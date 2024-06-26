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

package com.github.yellowstonegames.path.technique;

import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.path.DijkstraMap;

import java.util.Arrays;
import java.util.Collection;

/**
 * Line Area of Effect that affects an slightly expanded (DDA) line from a given origin Coord to a given end Coord,
 * plus an optional radius of cells around the path of the line, while respecting obstacles in its path and possibly
 * stopping if obstructed. You can specify the RadiusType to Radius.DIAMOND for Manhattan distance, RADIUS.SQUARE for
 * Chebyshev, or RADIUS.CIRCLE for Euclidean.
 * <br>
 * You may want the BeamAOE class instead of this. LineAOE travels point-to-point and does not restrict length, while
 * BeamAOE travels a specific length (and may have a radius, like LineAOE) but then stops only after the travel down the
 * length and radius has reached its end. This difference is relevant if a game has effects that have a definite
 * area measured in a rectangle or elongated pillbox shape, such as a "20-foot-wide bolt of lightning, 100 feet long."
 * BeamAOE is more suitable for that effect, while LineAOE may be more suitable for things like focused lasers that
 * pass through small (likely fleshy) obstacles but stop after hitting the aimed-at target.
 * <br>
 * LineAOE will strike a small area behind the user and in the opposite direction of the target if the radius is
 * greater than 0. This behavior may be altered in a future version.
 * <br>
 * This will produce floats for its {@link #findArea()} method which are equal to 1.0.
 * <br>
 * This class uses {@link BresenhamLine} and {@link DijkstraMap} to create its area of effect.
 */
public class LineAOE implements AOE {
    private Coord origin, end;
    private int radius;
    private float[][] dungeon;
    private DijkstraMap dijkstra;
    private Radius rt;
    private BresenhamLine los;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, AimLimit.FREE);
    public LineAOE(Coord origin, Coord end)
    {
        dijkstra = new DijkstraMap();
        dijkstra.measurement = Measurement.CHEBYSHEV;
        rt = Radius.SQUARE;
        this.origin = origin;
        this.end = end;
        radius = 0;
        los = new BresenhamLine();
    }
    public LineAOE(Coord origin, Coord end, int radius)
    {
        dijkstra = new DijkstraMap();
        dijkstra.measurement = Measurement.CHEBYSHEV;
        rt = Radius.SQUARE;
        this.origin = origin;
        this.end = end;
        this.radius = radius;
        los = new BresenhamLine();
    }
    public LineAOE(Coord origin, Coord end, int radius, Radius radiusType)
    {
        dijkstra = new DijkstraMap();
        rt = radiusType;
        switch (radiusType)
        {
            case DIAMOND:
                dijkstra.measurement = Measurement.MANHATTAN;
                break;
            case SQUARE:
                dijkstra.measurement = Measurement.CHEBYSHEV;
                break;
            default:
                dijkstra.measurement = Measurement.EUCLIDEAN;
                break;
        }
        this.origin = origin;
        this.end = end;
        this.radius = radius;
        los = new BresenhamLine();
    }
    public LineAOE(Coord origin, Coord end, int radius, Radius radiusType, int minRange, int maxRange)
    {
        dijkstra = new DijkstraMap();
        rt = radiusType;
        switch (radiusType)
        {
            case DIAMOND:
                dijkstra.measurement = Measurement.MANHATTAN;
                break;
            case SQUARE:
                dijkstra.measurement = Measurement.CHEBYSHEV;
                break;
            default:
                dijkstra.measurement = Measurement.EUCLIDEAN;
                break;
        }
        this.origin = origin;
        this.end = end;
        this.radius = radius;
        reach.minDistance = minRange;
        reach.maxDistance = maxRange;
        los = new BresenhamLine();
    }
    private float[][] initDijkstra()
    {
        los.isReachable(origin.x, origin.y, end.x, end.y, dungeon, los.lastLine);
        ObjectDeque<Coord> lit = los.lastLine;

        dijkstra.initializeByResistance(dungeon);
        for(Coord p : lit)
        {
            dijkstra.setGoal(p);
        }
        if(radius == 0)
            return dijkstra.gradientMap;
        return dijkstra.partialScan(radius, null);
    }

    @Override
    public Coord getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Coord origin) {
        this.origin = origin;
        dijkstra.resetMap();
        dijkstra.clearGoals();
    }

    /**
     * Gets the same values returned by getLimitType(), getMinRange(), getMaxRange(), and getMetric() bundled into one
     * Reach object.
     *
     * @return a non-null Reach object.
     */
    @Override
    public Reach getReach() {
        return reach;
    }

    /**
     * Sets the same values as setLimitType(), setMinRange(), setMaxRange(), and setMetric() using one Reach object.
     *
     * @param reach a non-null Reach object.
     */
    @Override
    public void setReach(Reach reach) {
        if(reach != null)
            this.reach = reach;
    }


    public Coord getEnd() {
        return end;
    }

    public void setEnd(Coord end) {
        if (dungeon != null && end.isWithin(dungeon.length, dungeon[0].length) &&
                AreaUtils.verifyReach(reach, origin, end)) {
            this.end = end;
            dijkstra.resetMap();
            dijkstra.clearGoals();
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Radius getRadiusType()
    {
        return rt;
    }
    public void setRadiusType(Radius radiusType)
    {
        rt = radiusType;
        switch (radiusType)
        {
            case DIAMOND:
                dijkstra.measurement = Measurement.MANHATTAN;
                break;
            case SQUARE:
                dijkstra.measurement = Measurement.CHEBYSHEV;
                break;
            default:
                dijkstra.measurement = Measurement.EUCLIDEAN;
                break;
        }
    }

    @Override
    public void shift(Coord aim) {
        setEnd(aim);
    }

    @Override
    public boolean mayContainTarget(Collection<Coord> targets) {
        for (Coord p : targets)
        {
            if(rt.radius(origin.x, origin.y, p.x, p.y) + rt.radius(end.x, end.y, p.x, p.y) -
                    rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
                return true;
        }
        return false;
    }

    @Override
    public CoordObjectOrderedMap<ObjectList<Coord>> idealLocations(Collection<Coord> targets, Collection<Coord> requiredExclusions) {
        if(targets == null || targets.isEmpty())
            return new CoordObjectOrderedMap<>();
        if(requiredExclusions == null) requiredExclusions = new CoordOrderedSet();

        int totalTargets = targets.size();
        CoordObjectOrderedMap<ObjectList<Coord>> bestPoints = new CoordObjectOrderedMap<>(totalTargets * 8);

        Coord[] ts = targets.toArray(new Coord[0]);
        Coord[] exs = requiredExclusions.toArray(new Coord[0]);
        Coord t;

        float[][][] compositeMap = new float[ts.length][dungeon.length][dungeon[0].length];

        float[][] dungeonCopy = new float[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }
        DijkstraMap dt = new DijkstraMap(dungeon, dijkstra.measurement, true);
        Coord tempPt;
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];
            dt.resetMap();
            dt.clearGoals();

            los.isReachable(origin.x, origin.y, t.x, t.y, dungeon, los.lastLine);
            ObjectDeque<Coord> lit = los.lastLine;

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    if(dt.gradientMap[x][y] < DijkstraMap.FLOOR || !AreaUtils.verifyReach(reach, origin, tempPt))
                        dungeonCopy[x][y] = Float.NaN;
                }
            }
        }

        for (int i = 0; i < ts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dijkstra.measurement, true);

            t = ts[i];
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(origin.x, origin.y, t.x, t.y, dungeon, los.lastLine);
            ObjectDeque<Coord> lit = los.lastLine;

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);


            float dist;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius)
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][ts[i].x][ts[i].y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++) {
                    Arrays.fill(compositeMap[i][x], 99999f);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] == dungeonCopy[x][y]) ? dm.gradientMap[x][y] : 99999f;
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }
        float bestQuality = 99999 * ts.length;
        float[][] qualityMap = new float[dungeon.length][dungeon[0].length];
        for (int x = 0; x < qualityMap.length; x++) {
            for (int y = 0; y < qualityMap[x].length; y++) {
                qualityMap[x][y] = 0.0f;
                long bits = 0;
                for (int i = 0; i < ts.length; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 99999.0 && i < 63)
                        bits |= 1L << i;
                }
                if(qualityMap[x][y] < bestQuality)
                {
                    ObjectList<Coord> ap = new ObjectList<>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if((bits & (1L << i)) != 0)
                            ap.add(ts[i]);
                    }
                    if(ap.size() > 0) {
                        bestQuality = qualityMap[x][y];
                        bestPoints.clear();
                        bestPoints.put(Coord.get(x, y), ap);
                    }
                }
                else if(qualityMap[x][y] == bestQuality)
                {
                    ObjectList<Coord> ap = new ObjectList<>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if((bits & (1L << i)) != 0)
                            ap.add(ts[i]);
                    }

                    if (ap.size() > 0) {
                        bestPoints.put(Coord.get(x, y), ap);
                    }
                }
            }
        }

        return bestPoints;
    }

    @Override
    public CoordObjectOrderedMap<ObjectList<Coord>> idealLocations(Collection<Coord> priorityTargets, Collection<Coord> lesserTargets, Collection<Coord> requiredExclusions) {
        if(priorityTargets == null || priorityTargets.isEmpty())
            return idealLocations(lesserTargets, requiredExclusions);
        if(requiredExclusions == null) requiredExclusions = new CoordOrderedSet();

        //requiredExclusions.remove(origin);
        int totalTargets = priorityTargets.size() + lesserTargets.size();
        CoordObjectOrderedMap<ObjectList<Coord>> bestPoints = new CoordObjectOrderedMap<>(totalTargets * 8);

        Coord[] pts = priorityTargets.toArray(new Coord[0]);
        Coord[] lts = lesserTargets.toArray(new Coord[0]);
        Coord[] exs = requiredExclusions.toArray(new Coord[0]);
        Coord t;

        float[][][] compositeMap = new float[totalTargets][dungeon.length][dungeon[0].length];

        float[][] dungeonCopy = new float[dungeon.length][dungeon[0].length],
                dungeonPriorities = new float[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
            Arrays.fill(dungeonPriorities[i], 1f);
        }
        DijkstraMap dt = new DijkstraMap(dungeon, dijkstra.measurement, true);
        Coord tempPt;
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(origin.x, origin.y, t.x, t.y, dungeon, los.lastLine);
            ObjectDeque<Coord> lit = los.lastLine;

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    if(dt.gradientMap[x][y] < DijkstraMap.FLOOR || !AreaUtils.verifyReach(reach, origin, tempPt))
                        dungeonCopy[x][y] = Float.NaN;
                }
            }
        }

        for (int i = 0; i < pts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dijkstra.measurement, true);
            t = pts[i];
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(origin.x, origin.y, t.x, t.y, dungeon, los.lastLine);
            ObjectDeque<Coord> lit = los.lastLine;

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);


            float dist;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius) {
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                            dungeonPriorities[x][y] = dungeon[x][y];
                        }
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][pts[i].x][pts[i].y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++) {
                    Arrays.fill(compositeMap[i][x], 399999.0f);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] == dungeonCopy[x][y]) ? dm.gradientMap[x][y] : 399999.0f;
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }

        for (int i = pts.length; i < totalTargets; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dijkstra.measurement, true);
            t = lts[i - pts.length];
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(origin.x, origin.y, t.x, t.y, dungeon, los.lastLine);
            ObjectDeque<Coord> lit = los.lastLine;

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);

            float dist;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius)
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][lts[i - pts.length].x][lts[i - pts.length].y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++)
                {
                    Arrays.fill(compositeMap[i][x], 99999f);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if(dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] == dungeonCopy[x][y] && dungeonPriorities[x][y] < 1f)
                        compositeMap[i][x][y] = dm.gradientMap[x][y];
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }
        float bestQuality = 99999 * lts.length + 399999 * pts.length;
        float[][] qualityMap = new float[dungeon.length][dungeon[0].length];
        for (int x = 0; x < qualityMap.length; x++) {
            for (int y = 0; y < qualityMap[x].length; y++) {
                qualityMap[x][y] = 0f;
                long pbits = 0, lbits = 0;
                for (int i = 0; i < pts.length; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 399999.0 && i < 63)
                        pbits |= 1L << i;
                }
                for (int i = pts.length; i < totalTargets; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 99999.0 && i < 63)
                        lbits |= 1L << i;
                }
                if(qualityMap[x][y] < bestQuality)
                {
                    ObjectList<Coord> ap = new ObjectList<>();

                    for (int i = 0; i < pts.length && i < 63; ++i) {
                        if((pbits & (1L << i)) != 0)
                            ap.add(pts[i]);
                    }
                    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
                        if((lbits & (1L << i)) != 0)
                            ap.add(lts[i - pts.length]);
                    }

                    if(ap.size() > 0) {
                        bestQuality = qualityMap[x][y];
                        bestPoints.clear();
                        bestPoints.put(Coord.get(x, y), ap);
                    }
                }
                else if(qualityMap[x][y] == bestQuality) {
                    ObjectList<Coord> ap = new ObjectList<>();

                    for (int i = 0; i < pts.length && i < 63; ++i) {
                        if ((pbits & (1L << i)) != 0) {
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                        }
                    }
                    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
                        if ((lbits & (1L << i)) != 0)
                            ap.add(lts[i - pts.length]);
                    }

                    if (ap.size() > 0) {
                        bestPoints.put(Coord.get(x, y), ap);
                    }
                }
            }
        }

        return bestPoints;
    }

/*
    @Override
    public ArrayList<ArrayList<Coord>> idealLocations(Set<Coord> targets, Set<Coord> requiredExclusions) {
        int totalTargets = targets.size() + 1;
        int volume = (int)(rt.radius(1, 1, dungeon.length - 2, dungeon[0].length - 2) * radius * 2.1);
        ArrayList<ArrayList<Coord>> locs = new ArrayList<ArrayList<Coord>>(totalTargets);
        for(int i = 0; i < totalTargets; i++)
        {
            locs.add(new ArrayList<Coord>(volume));
        }
        if(totalTargets == 1)
            return locs;

        int ctr = 0;

        boolean[][] tested = new boolean[dungeon.length][dungeon[0].length];
        for (int x = 1; x < dungeon.length - 1; x += radius) {
            for (int y = 1; y < dungeon[x].length - 1; y += radius) {

                if(mayContainTarget(requiredExclusions, x, y))
                    continue;
                ctr = 0;
                for(Coord tgt : targets)
                {
                    if(rt.radius(origin.x, origin.y, tgt.x, tgt.y) + rt.radius(end.x, end.y, tgt.x, tgt.y) -
                        rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
                        ctr++;
                }
                if(ctr > 0)
                    locs.get(totalTargets - ctr).add(Coord.get(x, y));
            }
        }
        Coord it;
        for(int t = 0; t < totalTargets - 1; t++)
        {
            if(locs.get(t).size() > 0) {
                int numPoints = locs.get(t).size();
                for (int i = 0; i < numPoints; i++) {
                    it = locs.get(t).get(i);
                    for (int x = Math.max(1, it.x - radius / 2); x < it.x + (radius + 1) / 2 && x < dungeon.length - 1; x++) {
                        for (int y = Math.max(1, it.y - radius / 2); y <= it.y + (radius - 1) / 2 && y < dungeon[0].length - 1; y++)
                        {
                            if(tested[x][y])
                                continue;
                            tested[x][y] = true;

                            if(mayContainTarget(requiredExclusions, x, y))
                                continue;

                            ctr = 0;
                            for(Coord tgt : targets)
                            {
                                if(rt.radius(origin.x, origin.y, tgt.x, tgt.y) + rt.radius(end.x, end.y, tgt.x, tgt.y) -
                                        rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
                                    ctr++;
                            }
                            if(ctr > 0)
                                locs.get(totalTargets - ctr).add(Coord.get(x, y));
                        }
                    }
                }
            }
        }
        return locs;
    }
*/
    @Override
    public void setMap(float[][] map) {
        dungeon = map;
        dijkstra.resetMap();
        dijkstra.clearGoals();
    }

    @Override
    public CoordFloatOrderedMap findArea() {
        float[][] dmap = initDijkstra();
        dmap[origin.x][origin.y] = DijkstraMap.DARK;
        dijkstra.resetMap();
        dijkstra.clearGoals();
        return AreaUtils.dijkstraToHashMap(dmap);
    }

}
