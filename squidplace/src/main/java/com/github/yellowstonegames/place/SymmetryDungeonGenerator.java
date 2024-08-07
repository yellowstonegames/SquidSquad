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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.Ordered;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.PoissonDisk;

import java.util.*;

/**
 * A variant on {@link MixedGenerator} that creates bi-radially symmetric maps (basically a yin-yang shape). Useful for
 * strategy games and possibly competitive multi-player games. The Coords passed to constructors as room positions do
 * not necessarily need to be symmetrical already; they will be duplicated in their mirror position in the dungeon.
 */
public class SymmetryDungeonGenerator extends MixedGenerator {

    public static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> removeSomeOverlap(int width, int height, Collection<Coord> sequence)
    {
        ObjectList<Coord> s2 = new ObjectList<>(sequence.size());
        for(Coord c : sequence)
        {
            if(c.x * 1.0 / width + c.y * 1.0 / height <= 1.0)
                s2.add(c);
        }
        return listToMap(s2);
    }
    public static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> removeSomeOverlap(int width, int height, ObjectObjectOrderedMap<Coord, ObjectList<Coord>> connections) {
        ObjectObjectOrderedMap<Coord, ObjectList<Coord>> om2 = new ObjectObjectOrderedMap<>(connections.size());
        Set<Coord> keyset = connections.keySet(), newkeys = new ObjectOrderedSet<>(connections.size());
        final double iw = 1.0 / width, ih = 1.0 / height;
        for (Coord c : keyset) {
            if (c.x * iw + c.y * ih <= 1.0) {
                newkeys.add(c);
            }
        }
        Coord[] keys = newkeys.toArray(new Coord[0]);
        for (int i = 0; i < keys.length; i++) {
            Coord c = keys[i];
            if (c.x * 1.0 / width + c.y * 1.0 / height <= 1.0) {
                ObjectList<Coord> cs = new ObjectList<>(4);
                for (Coord c2 : connections.get(c)) {
                    if (c2.x * 1.0 / width + c2.y * 1.0 / height <= 1.0) {
                        cs.add(c2);
                    } else if (keys[(i + 1) % keys.length].x * 1.0 / width +
                            keys[(i + 1) % keys.length].y * 1.0 / height <= 1.0) {
                        cs.add(keys[(i + 1) % keys.length]);
                    }

                }
                om2.put(c, cs);
            }
        }
        return om2;
    }
    /**
     * This prepares a map generator that will generate a map with width 80 and height 80, using a random seed.
     * This version of the constructor uses Poisson Disk sampling to generate the points it will draw caves and
     * corridors between, ensuring a minimum distance between points, but it does not ensure that paths between points
     * will avoid overlapping with rooms or other paths. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @see PoissonDisk used to ensure spacing for the points.
     */
    public SymmetryDungeonGenerator() {
        this(80, 80, new WhiskerRandom());
    }
    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses Poisson Disk sampling to generate the points it will draw caves and
     * corridors between, ensuring a minimum distance between points, but it does not ensure that paths between points
     * will avoid overlapping with rooms or other paths. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width  the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng    an RNG object to use for random choices; this make a lot of random choices.
     * @see PoissonDisk used to ensure spacing for the points.
     */
    public SymmetryDungeonGenerator(int width, int height, EnhancedRandom rng) {
        this(width, height, rng, basicPoints(width, height, rng));
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a ObjectList of Coord points from some other source to determine the path to add
     * rooms or caves to and then connect. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width    the width of the final map in cells
     * @param height   the height of the final map in cells
     * @param rng      an EnhancedRandom, such as a WhiskerRandom, to use for random choices; this make a lot of random choices.
     * @param sequence a ObjectList of Coord to connect in order; index 0 is the start, index size() - 1 is the end.
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, EnhancedRandom rng, ObjectList<Coord> sequence) {
        this(width, height, rng, listToMap(sequence), 1f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a ObjectList of Coord points from some other source to determine the path to add
     * rooms or caves to and then connect. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width    the width of the final map in cells
     * @param height   the height of the final map in cells
     * @param rng      an EnhancedRandom, such as a WhiskerRandom, to use for random choices; this make a lot of random choices.
     * @param sequence a ObjectList of Coord to connect in order; index 0 is the start, index size() - 1 is the end.
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, EnhancedRandom rng, Ordered<Coord> sequence) {
        this(width, height, rng, setToMap(sequence), 1f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a LinkedHashMap with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width       the width of the final map in cells
     * @param height      the height of the final map in cells
     * @param rng         an EnhancedRandom object to use for random choices; this makes a lot of random choices.
     * @param connections a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, EnhancedRandom rng, ObjectObjectOrderedMap<Coord, ObjectList<Coord>> connections) {
        this(width, height, rng, connections, 0.8f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a ObjectObjectOrderedMap with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width              the width of the final map in cells
     * @param height             the height of the final map in cells
     * @param rng                an EnhancedRandom object to use for random choices; this makes a lot of random choices.
     * @param connections        a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @param roomSizeMultiplier a float multiplier that will be applied to each room's width and height
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, EnhancedRandom rng, ObjectObjectOrderedMap<Coord, ObjectList<Coord>> connections, float roomSizeMultiplier) {
        super(width, height, rng, crossConnect(width, height, connections), roomSizeMultiplier);
    }

    protected static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> listToMap(ObjectList<Coord> sequence)
    {
        ObjectObjectOrderedMap<Coord, ObjectList<Coord>> conns = new ObjectObjectOrderedMap<>(sequence.size() - 1);
        for (int i = 0; i < sequence.size() - 1; i++) {
            Coord c1 = sequence.get(i), c2 = sequence.get(i+1);
            ObjectList<Coord> cs = new ObjectList<>(1);
            cs.add(c2);
            conns.put(c1, cs);
        }
        return conns;
    }
    protected static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> setToMap(Ordered<Coord> sequence)
    {
        ObjectObjectOrderedMap<Coord, ObjectList<Coord>> conns = new ObjectObjectOrderedMap<>(sequence.size() - 1);
        ObjectList<Coord> order = sequence.order();
        for (int i = 0; i < sequence.size() - 1; i++) {
            Coord c1 = order.get(i), c2 = order.get(i+1);
            ObjectList<Coord> cs = new ObjectList<>(1);
            cs.add(c2);
            conns.put(c1, cs);
        }
        return conns;
    }

    protected static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> crossConnect(int width, int height, Map<Coord, ObjectList<Coord>> connections)
    {
        ObjectObjectOrderedMap<Coord, ObjectList<Coord>> conns = new ObjectObjectOrderedMap<>(connections.size());
        for(Map.Entry<Coord, ObjectList<Coord>> entry : connections.entrySet())
        {
            conns.put(entry.getKey(), new ObjectList<>(entry.getValue()));
        }
        double lowest1 = 9999, lowest2 = 9999, lowest3 = 9999, lowest4 = 9999;
        Coord l1 = null, l2 = null, l3 = null, l4 = null, r1 = null, r2 = null, r3 = null, r4 = null;

        ObjectObjectOrderedMap.OrderedMapValues<Coord, ObjectList<Coord>> lVals = new ObjectObjectOrderedMap.OrderedMapValues<>(conns);
        ObjectObjectOrderedMap.OrderedMapValues<Coord, ObjectList<Coord>> rVals = new ObjectObjectOrderedMap.OrderedMapValues<>(conns);
        for(ObjectList<Coord> left : lVals)
        {
            rVals.resetIterator();
            for(ObjectList<Coord> right : rVals)
            {
                for (int i = 0; i < left.size(); i++) {
                    Coord lc = left.get(i);
                    for (int j = 0; j < right.size(); j++) {
                        Coord rc = right.get(j);
                        Coord rc2 = Coord.get(width - 1 - rc.x, height - 1 - rc.y);
                        double dist = lc.distanceD(rc2);
                        if (dist < 0.001)
                            continue;
                        if (dist < lowest1) {
                            lowest1 = dist;
                            l1 = lc;
                            r1 = rc2;
                        } else if (dist < lowest2 && !lc.equals(l1) && !rc2.equals(r1)) {
                            lowest2 = dist;
                            l2 = lc;
                            r2 = rc2;
                        } else if (dist < lowest3
                                && !lc.equals(l1) && !rc2.equals(r1) && !lc.equals(l2) && !rc2.equals(r2)) {
                            lowest3 = dist;
                            l3 = lc;
                            r3 = rc2;
                        } else if (dist < lowest4
                                && !lc.equals(l1) && !rc2.equals(r1)
                                && !lc.equals(l2) && !rc2.equals(r2)
                                && !lc.equals(l3) && !rc2.equals(r3)) {
                            lowest4 = dist;
                            l4 = lc;
                            r4 = rc2;
                        }
                    }
                }
            }
        }
        if(l1 != null && r1 != null)
        {
            if(conns.containsKey(l1))
            {
                conns.get(l1).add(r1);
            }
            else if(conns.containsKey(r1))
            {
                conns.get(r1).add(l1);
            }
        }
        if(l2 != null && r2 != null)
        {
            if(conns.containsKey(l2))
            {
                conns.get(l2).add(r2);
            }
            else if(conns.containsKey(r2))
            {
                conns.get(r2).add(l2);
            }
        }
        if(l3 != null && r3 != null)
        {
            if(conns.containsKey(l3))
            {
                conns.get(l3).add(r3);
            }
            else if(conns.containsKey(r3))
            {
                conns.get(r3).add(l3);
            }
        }
        if(l4 != null && r4 != null)
        {
            if(conns.containsKey(l4))
            {
                conns.get(l4).add(r4);
            }
            else if(conns.containsKey(r4))
            {
                conns.get(r4).add(l4);
            }
        }
        return conns;
    }
    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     * @return false if everything is normal, true if and only if this failed to mark because the position is walled
     */
    @Override
    protected boolean mark(int x, int y) {
        return super.mark(x, y) || super.mark(width - 1 - x, height - 1 - y);
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    @Override
    protected void markPiercing(int x, int y) {
        super.markPiercing(x, y);
        super.markPiercing(width - 1 - x, height - 1 - y);
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    @Override
    protected void wallOff(int x, int y) {
        super.wallOff(x, y);
        super.wallOff(width - 1 - x, height - 1 - y);
    }
    /**
     * Internal use. Marks a point's environment type as the appropriate kind of environment.
     * @param x x position to mark
     * @param y y position to mark
     * @param kind an int that should be one of the constants in MixedGenerator for environment types.
     */
    @Override
    protected void markEnvironment(int x, int y, int kind) {
        super.markEnvironment(x, y, kind);
        super.markEnvironment(width - 1 - x, height - 1 - y, kind);
    }

    @Override
    public String toString() {
        return "SymmetryDungeonGenerator{" +
                "width=" + width +
                ", height=" + height +
                ", roomWidth=" + roomWidth +
                ", roomHeight=" + roomHeight +
                '}';
    }
}
