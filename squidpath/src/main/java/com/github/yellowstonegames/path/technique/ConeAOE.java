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
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.path.DijkstraMap;

import java.util.Arrays;
import java.util.Collection;

/**
 * An AOE type that has an origin, a radius, an angle, and a span; it will blast from the origin to a length equal to
 * radius along the angle (in degrees), moving somewhat around corners/obstacles, and also spread a total of span
 * degrees around the angle (a span of 90 will affect a full quadrant, centered on angle). You can specify the
 * RadiusType to Radius.DIAMOND for Manhattan distance, RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 * <br>
 * RADIUS.CIRCLE (Euclidean measurement) will produce the most real-looking cones. This will produce floats for its
 * {@link #findArea()} method which are greater than 0.0 and less than or equal to 1.0.
 * <br>
 * This class uses {@link FOV} to create its area of effect.
 */
public class ConeAOE implements AOE {
    private Coord origin;
    private float radius, angle, span;
    private float[][] map, tmpMap;
    private Radius radiusType;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, AimLimit.FREE);

    public ConeAOE(Coord origin, Coord endCenter, float span, Radius radiusType)
    {
        this.origin = origin;
        radius = radiusType.radius(origin.x, origin.y, endCenter.x, endCenter.y);
        angle = TrigTools.atan2Deg360(endCenter.y - origin.y, endCenter.x - origin.x);
        this.span = span;
        this.radiusType = radiusType;
    }
    public ConeAOE(Coord origin, int radius, float angle, float span, Radius radiusType)
    {
        this.origin = origin;
        this.radius = radius;
        this.angle = angle;
        this.span = span;
        this.radiusType = radiusType;
    }

    @Override
    public Coord getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Coord origin) {
        this.origin = origin;
    }

    @Override
    public AimLimit getLimitType() {
        return reach.limit;
    }

    @Override
    public int getMinRange() {
        return reach.minDistance;
    }

    @Override
    public int getMaxRange() {
        return reach.maxDistance;
    }

    @Override
    public Radius getMetric() {
        return reach.metric;
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

    @Override
    public void setLimitType(AimLimit limitType) {
        reach.limit = limitType;

    }

    @Override
    public void setMinRange(int minRange) {
        reach.minDistance = minRange;
    }

    @Override
    public void setMaxRange(int maxRange) {
        reach.maxDistance = maxRange;

    }

    @Override
    public void setMetric(Radius metric) {
        reach.metric = metric;
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        if (reach.limit == null || reach.limit == AimLimit.FREE ||
                (reach.limit == AimLimit.EIGHT_WAY && (int) (angle+0.5f) % 45 == 0) ||
                (reach.limit == AimLimit.DIAGONAL && (int) (angle+0.5f) % 90 == 45) ||
                (reach.limit == AimLimit.ORTHOGONAL && (int) (angle+0.5f) % 90 == 0)) {
            this.angle = angle;
        }
    }

    public void setEndCenter(Coord endCenter) {
        if (AreaUtils.verifyLimit(reach.limit, origin, endCenter)) {
            angle = TrigTools.atan2Deg360(endCenter.y - origin.y, endCenter.x - origin.x);
        }
    }

    public float getSpan() {
        return span;
    }

    public void setSpan(float span) {
        this.span = span;
    }

    public Radius getRadiusType() {
        return radiusType;
    }

    public void setRadiusType(Radius radiusType) {
        this.radiusType = radiusType;
    }

    @Override
    public void shift(Coord aim) {
        setEndCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Collection<Coord> targets) {
        for (Coord p : targets) {
            if (radiusType.radius(origin.x, origin.y, p.x, p.y) <= radius) {
                float d = (angle - TrigTools.atan2Deg360(p.y - origin.y, p.x - origin.x) + 360f) % 360f;
                if(d > 180f)
                    d = 360f - d;
                if(d < span * 0.5f)
                    return true;
            }
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

        Coord[] ts = targets.toArray(new Coord[0]);
        Coord[] exs = requiredExclusions.toArray(new Coord[0]);
        Coord t;

        float[][][] compositeMap = new float[totalTargets][map.length][map[0].length];
        float tAngle;


        float[][] dungeonCopy = new float[map.length][map[0].length];
        Coord tempPt;

        for (int i = 0; i < exs.length; i++) {
            t = exs[i];
            tAngle = TrigTools.atan2Deg360(t.y - origin.y, t.x - origin.x);
            FOV.reuseRippleFOV(map, tmpMap, 3, origin.x, origin.y, radius, radiusType, tAngle, span);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    dungeonCopy[x][y] = !(origin.x == x && origin.y == y) && (tmpMap[x][y] > 0.0 || !AreaUtils.verifyLimit(reach.limit, origin, tempPt)) ? Float.NaN : map[x][y];
                }
            }
        }
        
        DijkstraMap dm = new DijkstraMap(map, radiusType.matchingMeasurement(), true);
        for (int i = 0; i < ts.length; ++i) {
            dm.initializeByResistance(map);
            t = ts[i];
            tAngle = TrigTools.atan2Deg360(t.y - origin.y, t.x - origin.x);

            FOV.reuseRippleFOV(map, tmpMap, 3, origin.x, origin.y, radius, radiusType, tAngle, span);

            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if (tmpMap[x][y] > 0.0)
                    {
                        compositeMap[i][x][y] = dm.physicalMap[x][y];
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][t.x][t.y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < map.length; x++) {
                    Arrays.fill(compositeMap[i][x], 99999f);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null, true);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[0].length; y++) {
                    if(dm.gradientMap[x][y] < DijkstraMap.FLOOR && dungeonCopy[x][y] == dungeonCopy[x][y])
                        compositeMap[i][x][y] = dm.gradientMap[x][y];
                    else
                        compositeMap[i][x][y] = 99999f;
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
                    if(compositeMap[i][x][y] < 99999.0 && i < 63)
                        bits |= 1L << i;
                }
                if(qualityMap[x][y] < bestQuality)
                {
                    ObjectList<Coord> ap = new ObjectList<>(8);

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
                else if(qualityMap[x][y] == bestQuality) {
                    ObjectList<Coord> ap = new ObjectList<>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if ((bits & (1L << i)) != 0)
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
        if(priorityTargets == null)
            return idealLocations(lesserTargets, requiredExclusions);
        if(requiredExclusions == null) requiredExclusions = new CoordOrderedSet();

        //requiredExclusions.remove(origin);
        int totalTargets = priorityTargets.size() + lesserTargets.size();
        CoordObjectOrderedMap<ObjectList<Coord>> bestPoints = new CoordObjectOrderedMap<>(totalTargets * 8);

        if(totalTargets == 0)
            return bestPoints;

        Coord[] pts = priorityTargets.toArray(new Coord[0]);
        Coord[] lts = lesserTargets.toArray(new Coord[0]);
        Coord[] exs = requiredExclusions.toArray(new Coord[0]);
        Coord t;

        float[][][] compositeMap = new float[totalTargets][map.length][map[0].length];
        float tAngle;

        float[][] dungeonCopy = new float[map.length][map[0].length],
                dungeonPriorities = new float[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            System.arraycopy(map[i], 0, dungeonCopy[i], 0, map[i].length);
            Arrays.fill(dungeonPriorities[i], 1f);
        }
        Coord tempPt;
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];
            tAngle = TrigTools.atan2Deg360(t.y - origin.y, t.x - origin.x);
            FOV.reuseRippleFOV(map, tmpMap, 3, origin.x, origin.y, radius, radiusType, tAngle, span);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    dungeonCopy[x][y] = (tmpMap[x][y] > 0.0 || !AreaUtils.verifyLimit(reach.limit, origin, tempPt)) ? Float.NaN : dungeonCopy[x][y];
                }
            }
        }

        Measurement dmm = Measurement.MANHATTAN;
        if(radiusType == Radius.SQUARE) dmm = Measurement.CHEBYSHEV;
        else if(radiusType == Radius.CIRCLE) dmm = Measurement.EUCLIDEAN;

        for (int i = 0; i < pts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(map, dmm, true);
            t = pts[i];
            tAngle = TrigTools.atan2Deg360(t.y - origin.y, t.x - origin.x);
            FOV.reuseRippleFOV(map, tmpMap, 3, origin.x, origin.y, radius, radiusType, tAngle, span);

            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if (tmpMap[x][y] > 0f){
                        compositeMap[i][x][y] = dm.physicalMap[x][y];
                        dungeonPriorities[x][y] = map[x][y];
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][pts[i].x][pts[i].y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < map.length; x++) {
                    Arrays.fill(compositeMap[i][x], 399999f);
                }
                continue;
            }



            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null, true);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] == dungeonCopy[x][y]) ? dm.gradientMap[x][y] : 399999f;
                }
            }
        }

        for (int i = pts.length; i < totalTargets; ++i) {
            DijkstraMap dm = new DijkstraMap(map, dmm, true);
            t = lts[i - pts.length];

            tAngle = TrigTools.atan2Deg360(t.y - origin.y, t.x - origin.x);

            FOV.reuseRippleFOV(map, tmpMap, 3, origin.x, origin.y, radius, radiusType, tAngle, span);

            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    if (tmpMap[x][y] > 0f){
                        compositeMap[i][x][y] = dm.physicalMap[x][y];
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][lts[i - pts.length].x][lts[i - pts.length].y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < map.length; x++)
                {
                    Arrays.fill(compositeMap[i][x], 99999f);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null, true);
            for (int x = 0; x < map.length; x++) {
                for (int y = 0; y < map[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR && dungeonCopy[x][y] == dungeonCopy[x][y] && dungeonPriorities[x][y] < 1f) ? dm.gradientMap[x][y] : 99999f;
                }
            }
        }
        float bestQuality = 99999 * lts.length + 399999 * pts.length;
        float[][] qualityMap = new float[map.length][map[0].length];
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

    @Override
    public void setMap(float[][] map) {
        this.map = map;
        this.tmpMap = ArrayTools.copy(map);
    }

    @Override
    public CoordFloatOrderedMap findArea() {
        CoordFloatOrderedMap r = AreaUtils.arrayToMap(FOV.reuseRippleFOV(map, tmpMap, 3, origin.x, origin.y, radius,
                radiusType, angle, span));
        r.remove(origin);
        return r;
    }

}
