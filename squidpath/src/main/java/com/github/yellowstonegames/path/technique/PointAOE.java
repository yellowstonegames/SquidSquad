/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.yellowstonegames.path.technique;

import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordFloatOrderedMap;
import com.github.yellowstonegames.grid.CoordObjectOrderedMap;
import com.github.yellowstonegames.grid.Radius;

import java.util.Collection;

/**
 * An AOE type that has a center Coord only and only affects that single Coord. Useful if you need an AOE implementation
 * for something that does not actually affect an area.
 * This will produce floats for its {@link #findArea()} method which are equal to 1.0.
 */
public class PointAOE implements AOE {
    private Coord center, origin;
    private int mapWidth, mapHeight;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, AimLimit.FREE);

    public PointAOE(Coord center)
    {
        this.center = center;
    }
    public PointAOE(Coord center, int minRange, int maxRange)
    {
        this.center = center;
        reach.minDistance = minRange;
        reach.maxDistance = maxRange;
    }

    public Coord getCenter() {
        return center;
    }


    public void setCenter(Coord center) {

        if (center.isWithin(mapWidth, mapHeight) &&
                AreaUtils.verifyReach(reach, origin, center))
        {
            this.center = center;
        }
    }

    @Override
    public void shift(Coord aim) {
        setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Collection<Coord> targets) {
        for (Coord p : targets)
        {
            if(center.x == p.x && center.y == p.y)
                return true;
        }
        return false;
    }

    @Override
    public CoordObjectOrderedMap<ObjectList<Coord>> idealLocations(Collection<Coord> targets, Collection<Coord> requiredExclusions) {
        if(targets == null)
            return new CoordObjectOrderedMap<>();

        int totalTargets = targets.size();
        CoordObjectOrderedMap<ObjectList<Coord>> bestPoints = new CoordObjectOrderedMap<>(totalTargets);

        if(totalTargets == 0)
            return bestPoints;


        double dist;
        for(Coord p : targets) {
            if(requiredExclusions != null && requiredExclusions.contains(p))
                continue;
            if (AreaUtils.verifyReach(reach, origin, p)) {

                dist = reach.metric.radius(origin.x, origin.y, p.x, p.y);
                if (dist <= reach.maxDistance && dist >= reach.minDistance) {
                    ObjectList<Coord> ap = ObjectList.with(p);
                    bestPoints.put(p, ap);
                }
            }
        }
        return bestPoints;
    }


    @Override
    public CoordObjectOrderedMap<ObjectList<Coord>> idealLocations(Collection<Coord> priorityTargets, Collection<Coord> lesserTargets, Collection<Coord> requiredExclusions) {
        if(priorityTargets == null || priorityTargets.isEmpty())
            return idealLocations(lesserTargets, requiredExclusions);

        int totalTargets = priorityTargets.size() + lesserTargets.size();
        CoordObjectOrderedMap<ObjectList<Coord>> bestPoints = new CoordObjectOrderedMap<>(totalTargets * 4);

        double dist;

        for(Coord p : priorityTargets) {
            if(requiredExclusions != null && requiredExclusions.contains(p))
                continue;
            if (AreaUtils.verifyReach(reach, origin, p)) {

                dist = reach.metric.radius(origin.x, origin.y, p.x, p.y);
                if (dist <= reach.maxDistance && dist >= reach.minDistance) {
                    ObjectList<Coord> ap = new ObjectList<>(4);
                    ap.add(p);
                    ap.add(p);
                    ap.add(p);
                    ap.add(p);
                    bestPoints.put(p, ap);
                }
            }
        }
        if(bestPoints.isEmpty()) {
            for (Coord p : lesserTargets) {
                if(requiredExclusions != null && requiredExclusions.contains(p))
                    continue;
                if (AreaUtils.verifyReach(reach, origin, p)) {

                    dist = reach.metric.radius(origin.x, origin.y, p.x, p.y);
                    if (dist <= reach.maxDistance && dist >= reach.minDistance) {
                        ObjectList<Coord> ap = ObjectList.with(p);
                        bestPoints.put(p, ap);
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

        boolean[][] tested = new boolean[dungeon.length][dungeon[0].length];
        for (int x = 1; x < dungeon.length - 1; x += radius) {
            BY_POINT:
            for (int y = 1; y < dungeon[x].length - 1; y += radius) {
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
                    for (int x = Math.max(1, it.x - radius / 2); x < it.x + (radius + 1) / 2 && x < dungeon.length - 1; x++) {
                        BY_POINT:
                        for (int y = Math.max(1, it.y - radius / 2); y <= it.y + (radius - 1) / 2 && y < dungeon[0].length - 1; y++)
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
        if (map != null && map.length > 0) {
            mapWidth = map.length;
            mapHeight = map[0].length;
        }
    }

    @Override
    public CoordFloatOrderedMap findArea() {
        return CoordFloatOrderedMap.with(center, 1f);
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
        this.reach = reach;
    }

}
