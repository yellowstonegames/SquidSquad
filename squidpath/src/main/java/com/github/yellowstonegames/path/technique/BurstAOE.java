/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.path.DijkstraMap;

import java.util.Arrays;
import java.util.Collection;

/**
 * An AOE type that has a center and a radius, and uses shadowcasting to create a burst of rays from the center, out to
 * the distance specified by radius. You can specify the RadiusType to Radius.DIAMOND for Manhattan distance,
 * RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * This will produce floats for its {@link #findArea()} method which are equal to 1.0.
 *
 * This class uses {@link FOV} to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class BurstAOE implements AOE {
    private Coord center, origin;
    private int radius;
    private float[][] map, buffer;
    private Radius radiusType;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, AimLimit.FREE);

    public BurstAOE(Coord center, int radius, Radius radiusType)
    {
        this.center = center;
        this.radius = radius;
        this.radiusType = radiusType;
    }
    public BurstAOE(Coord center, int radius, Radius radiusType, int minRange, int maxRange)
    {
        this.center = center;
        this.radius = radius;
        this.radiusType = radiusType;
        reach.minDistance = minRange;
        reach.maxDistance = maxRange;
    }

    public Coord getCenter() {
        return center;
    }

    public void setCenter(Coord center) {

        if (map != null && center.isWithin(map.length, map[0].length) &&
                AreaUtils.verifyReach(reach, origin, center))
        {
            this.center = center;
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Radius getRadiusType() {
        return radiusType;
    }

    public void setRadiusType(Radius radiusType) {
        this.radiusType = radiusType;
    }

    @Override
    public void shift(Coord aim) {
        setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Collection<Coord> targets) {
        for (Coord p : targets)
        {
            if(radiusType.radius(center.x, center.y, p.x, p.y) <= radius)
                return true;
        }
        return false;
    }

    @Override
    public CoordObjectOrderedMap<ObjectList<Coord>> idealLocations(Collection<Coord> targets, Collection<Coord> requiredExclusions) {
        if(targets == null || targets.isEmpty())
            return new CoordObjectOrderedMap<>();
        if(requiredExclusions == null) requiredExclusions = new CoordOrderedSet();

        //requiredExclusions.remove(origin);
        int totalTargets = targets.size();
        CoordObjectOrderedMap<ObjectList<Coord>> bestPoints = new CoordObjectOrderedMap<>(totalTargets * 8);

        if(totalTargets == 0)
            return bestPoints;

        if(radius == 0)
        {
            for(Coord p : targets)
            {
                ObjectList<Coord> ap = ObjectList.with(p);
                bestPoints.put(p, ap);
            }
            return bestPoints;
        }
        Coord[] ts = targets.toArray(new Coord[0]);
        Coord[] exs = requiredExclusions.toArray(new Coord[0]);
        Coord t;

        float[][][] compositeMap = new float[ts.length][map.length][map[0].length];

        float[][] mapCopy = new float[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            System.arraycopy(map[i], 0, mapCopy[i], 0, map[i].length);
        }
        Coord tempPt;
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];

            FOV.reuseFOV(map, buffer, t.x, t.y, radius, radiusType);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    mapCopy[x][y] = (buffer[x][y] > 0.0 || !AreaUtils.verifyReach(reach, origin, tempPt)) ? Float.NaN : mapCopy[x][y];
                }
            }
        }

        Measurement dmm = Measurement.MANHATTAN;
        if(radiusType == Radius.SQUARE) dmm = Measurement.CHEBYSHEV;
        else if(radiusType == Radius.CIRCLE) dmm = Measurement.EUCLIDEAN;

        for (int i = 0; i < ts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(map, dmm, true);

            t = ts[i];
            FOV.reuseFOV(map, buffer, t.x, t.y, radius, radiusType);

            float dist;
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if (buffer[x][y] > 0f) {
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
                for (int x = 0; x < map.length; x++) {
                    Arrays.fill(compositeMap[i][x], 99999f);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && mapCopy[x][y] == mapCopy[x][y]) ? dm.gradientMap[x][y] : 99999f;
                }
            }
        }
        float bestQuality = 99999 * ts.length;
        float[][] qualityMap = new float[map.length][map[0].length];
        for (int x = 0; x < qualityMap.length; x++) {
            for (int y = 0; y < qualityMap[x].length; y++) {
                qualityMap[x][y] = 0f;
                long bits = 0;
                for (int i = 0; i < ts.length; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 99999f && i < 63)
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
                    }                }
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

        int totalTargets = priorityTargets.size() + lesserTargets.size();
        CoordObjectOrderedMap<ObjectList<Coord>> bestPoints = new CoordObjectOrderedMap<>(totalTargets * 8);

        if(radius == 0)
        {
            for(Coord p : priorityTargets)
            {
                ObjectList<Coord> ap = new ObjectList<>();
                ap.add(p);
                bestPoints.put(p, ap);
            }
            return bestPoints;
        }
        Coord[] pts = priorityTargets.toArray(new Coord[0]);
        Coord[] lts = lesserTargets.toArray(new Coord[0]);
        Coord[] exs = requiredExclusions.toArray(new Coord[0]);
        Coord t;

        float[][][] compositeMap = new float[totalTargets][map.length][map[0].length];

        float[][] mapCopy = new float[map.length][map[0].length],
                mapPriorities = new float[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            System.arraycopy(map[i], 0, mapCopy[i], 0, map[i].length);
            Arrays.fill(mapPriorities[i], 1f);
        }
        Coord tempPt;
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];
            FOV.reuseFOV(map, buffer, t.x, t.y, radius, radiusType);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    mapCopy[x][y] = (buffer[x][y] > 0f || !AreaUtils.verifyReach(reach, origin, tempPt)) ? Float.NaN : mapCopy[x][y];
                }
            }
        }

        Measurement dmm = Measurement.MANHATTAN;
        if(radiusType == Radius.SQUARE) dmm = Measurement.CHEBYSHEV;
        else if(radiusType == Radius.CIRCLE) dmm = Measurement.EUCLIDEAN;

        for (int i = 0; i < pts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(map, dmm);
            t = pts[i];

            FOV.reuseFOV(map, buffer, t.x, t.y, radius, radiusType);

            float dist;
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if (buffer[x][y] > 0f){
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius) {
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                            mapPriorities[x][y] = map[x][y];
                        }
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][t.x][t.y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < map.length; x++) {
                    Arrays.fill(compositeMap[i][x], 399999f);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if(dm.gradientMap[x][y] < DijkstraMap.FLOOR  && mapCopy[x][y] == mapCopy[x][y])
                        compositeMap[i][x][y] = dm.gradientMap[x][y];
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }

        for (int i = pts.length; i < totalTargets; ++i) {
            DijkstraMap dm = new DijkstraMap(map, dmm);
            t = lts[i - pts.length];

            FOV.reuseFOV(map, buffer, t.x, t.y, radius, radiusType);

            float dist;
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if (buffer[x][y] > 0f){
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius)
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][t.x][t.y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < map.length; x++)
                {
                    Arrays.fill(compositeMap[i][x], 99999f);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if(dm.gradientMap[x][y] < DijkstraMap.FLOOR  && mapCopy[x][y] == mapCopy[x][y] && mapPriorities[x][y] < 1f)
                        compositeMap[i][x][y] = dm.gradientMap[x][y];
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }
        float bestQuality = 99999 * lts.length + 399999 * pts.length;
        float[][] qualityMap = new float[map.length][map[0].length];
        for (int x = 0; x < qualityMap.length; x++) {
            for (int y = 0; y < qualityMap[x].length; y++) {
                qualityMap[x][y] = 0f;
                long pbits = 0, lbits = 0;
                for (int i = 0; i < pts.length; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 399999f && i < 63)
                        pbits |= 1L << i;
                }
                for (int i = pts.length; i < totalTargets; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 99999f && i < 63)
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
                else if(qualityMap[x][y] == bestQuality)
                {
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
                        if((lbits & (1L << i)) != 0)
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
        int maxEffect = (int)radiusType.volume2D(radius);
        ArrayList<ArrayList<Coord>> locs = new ArrayList<ArrayList<Coord>>(totalTargets);

        for(int i = 0; i < totalTargets; i++)
        {
            locs.add(new ArrayList<Coord>(maxEffect));
        }
        if(totalTargets == 1)
            return locs;

        int ctr = 0;
        if(radius < 1)
        {
            locs.get(totalTargets - 2).addAll(targets);
            return locs;
        }

        boolean[][] tested = new boolean[map.length][map[0].length];
        for (int x = 1; x < map.length - 1; x += radius) {
            BY_POINT:
            for (int y = 1; y < map[x].length - 1; y += radius) {
                for(Coord ex : requiredExclusions)
                {
                    if(radiusType.radius(x, y, ex.x, ex.y) <= radius)
                        continue BY_POINT;
                }
                ctr = 0;
                for(Coord tgt : targets)
                {
                    if(radiusType.radius(x, y, tgt.x, tgt.y) <= radius)
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
                    for (int x = Math.max(1, it.x - radius / 2); x < it.x + (radius + 1) / 2 && x < map.length - 1; x++) {
                        BY_POINT:
                        for (int y = Math.max(1, it.y - radius / 2); y <= it.y + (radius - 1) / 2 && y < map[0].length - 1; y++)
                        {
                            if(tested[x][y])
                                continue;
                            tested[x][y] = true;

                            for(Coord ex : requiredExclusions)
                            {
                                if(radiusType.radius(x, y, ex.x, ex.y) <= radius)
                                    continue BY_POINT;
                            }

                            ctr = 0;
                            for(Coord tgt : targets)
                            {
                                if(radiusType.radius(x, y, tgt.x, tgt.y) <= radius)
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
        this.map = map;
        buffer = ArrayTools.copy(map);
    }

    @Override
    public CoordFloatOrderedMap findArea() {
        return AreaUtils.arrayToMap(FOV.reuseFOV(map, buffer, center.x, center.y, radius, radiusType));
    }

    @Override
    public Coord getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Coord origin) {
        this.origin = origin;
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

}
