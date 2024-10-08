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

package com.github.yellowstonegames.seek.technique;

import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.*;

import java.util.Collection;
import java.util.Set;

/**
 * A simple struct-like class that stores various public fields which describe the targeting properties of a skill,
 * spell, tech, or any other game-specific term for a targeted (typically offensive) ability we call a Technique.
 * <br>
 * The typical usage of a Technique is:
 * <ul>
 * <li>Construct any AOE implementation the Technique would use (if the Technique affects multiple grid cells).</li>
 * <li>Construct the Technique (passing the AOE as a parameter if needed).</li>
 * <li>Call {@link #setMap(float[][])} before considering the Technique if it has not been called yet, if the physical
 * map (including doors and obstacles) has changed since setMap() was last called, or simply on every Technique every
 * time the map changes if there are few enemies with few Techniques. PERFORMING ANY SUBSEQUENT STEPS WITHOUT SETTING
 * THE MAP TO THE CURRENT ACTUAL PHYSICAL MAP WILL HAVE BAD CONSEQUENCES FOR LOGIC AND MAY CAUSE CRASHING BUGS DUE TO
 * ARRAY BOUNDS VIOLATIONS IF YOU HAVEN'T SET THE MAP ARRAY IN THE FIRST PLACE. The map should be bounded by wall chars
 * (1 or higher), which is done automatically by squidplace's dungeon generators and similar classes.</li>
 * <li>When the Technique is being considered by an AI, call {@link #idealLocations(Coord, Set, Set, Set)} with the
 * values of targets, lesserTargets, and/or priorityTargets set to beings that the AI can see (likely using FOV) and
 * wants to affect with this Technique (enemies for offensive Techniques, allies for supporting ones), and
 * requiredExclusions typically set to allies for offensive Techniques that can cause friendly-fire damage, or to null
 * for supporting ones or Techniques that don't affect allies.</li>
 * <li>If the Technique is being used for a player action, you can show the player what cells are valid targets with
 * {@link #possibleTargets(Coord)} or its other overload, or can validate choices without telling the player beforehand
 * using {@link #canTarget(Coord, Coord)} (this may be useful for cases where the player is activating an unknown magic
 * item like a scroll, and the actual range hasn't been revealed).</li>
 * <li>When an ideal location has been determined from the previous step, and the player or AI decides to use this
 * Technique on a specific target point, call {@link #apply(Coord, Coord)} with the user position as a Coord and the
 * chosen Coord, and proceed to process the effects of the Technique as fitting for your game on the returned Map of
 * Coord keys to Double values denoting how affected (from 0.0 for unaffected to 1.0 for full power) that Coord is.
 * An AI might decide which Technique to use by an aggression ("aggro" or "hatred") level tracked per-enemy, by weights
 * on Techniques for different situations, by choosing at random, or some combination of factors.</li>
 * </ul>
 * <br>
 * A Technique always has an AOE implementation that it uses to determine which cells it actually affects, and
 * Techniques that do not actually affect an area use the default single-cell "Area of Effect" implementation, PointAOE.
 * You typically will need to construct the implementing class of the AOE interface in a different way for each
 * implementation; BeamAOE, LineAOE, and ConeAOE depend on the user's position, BurstAOE and BlastAOE treat radii
 * differently from BeamAOE and LineAOE, and CloudAOE has a random component that can be given a seed.
 * <br>
 * A Technique has a String name, which typically should be in a form that can be presented to a user, and an Object
 * userData, which defaults to null but can be given some value not meant for users that records
 * any additional identifying characteristics the game needs for things like comparisons.
 */
public class Technique {
    public String name;
    public Object userData;
    public AOE aoe;
    protected float[][] map;

    /**
     * Creates a Technique that can target any adjacent single Coord, using
     * Chebyshev (8-way square) distance.
     */
    public Technique() {
        name = "Default Technique";
        aoe = new PointAOE(Coord.get(0, 0));
    }

    /**
     * Creates a Technique that can target any adjacent single Coord,
     * using Chebyshev (8-way square) distance.
     * @param name An identifier that may be displayed to the user. Also used for id.
     */
    public Technique(String name) {
        this.name = name;
        aoe = new PointAOE(Coord.get(0, 0));
    }

    /**
     * Creates a Technique that can target a Coord at a range specified by the given AOE's minRange and maxRange,
     * using a distance metric from the AOE, and use that target Coord for the given AOE.
     * @param name An identifier that may be displayed to the user. Also used for id.
     * @param aoe An implementation of the AOE interface; typically needs construction beforehand.
     */
    public Technique(String name, AOE aoe) {
        this.name = name;
        this.aoe = aoe;
    }

    /**
     * Creates a Technique that can target a Coord at a range specified by the given AOE's minRange and maxRange,
     * using a distance metric from the AOE, and use that target Coord for the given AOE. Takes a userData parameter,
     * which can be any Object, any array, or null.
     * @param name An identifier that may be displayed to the user.
     * @param userData An identifier that should always be internal, and will probably never be shown to the user.
     * @param aoe An implementation of the AOE interface; typically needs construction beforehand.
     */
    public Technique(String name, Object userData, AOE aoe) {
        this.name = name;
        this.userData = userData;
        this.aoe = aoe;
    }


    /**
     * VITAL: Call this method before any calls to idealLocations() or apply(), and call it again if the map changes.
     * <br>
     * This simple method sets the map that this Technique can find targets in to a given float 2D array with 1f or
     * higher for walls and any lower float treated as a floor for most purposes (certain AOE implementations may treat
     * values between 0f and 1f differently from just floors and walls, specifically any that use
     * FOV internally and can yield values other than 1.0 from their findArea() method, like BlastAOE and ConeAOE).
     * @param map A float 2D array representing a resistance map, as used and produced by {@link FOV}
     */
    public void setMap(float[][] map)
    {
        this.map = map;
        aoe.setMap(map);
    }

    /**
     * VITAL: Call this method before any calls to idealLocations() or apply(), and call it again if the map changes.
     * <br>
     * This simple method sets the map that this Technique can find targets in to a given char 2D array with '#' for
     * walls and any other character (including characters for open and closed doors) treated as a floor for most
     * purposes (certain AOE implementations may treat open and closed doors differently, specifically any that use
     * FOV internally and can yield values other than 1.0 from their findArea() method, like BlastAOE and ConeAOE).
     * <br>
     * This simply calls {@code setMap(FOV.generateSimpleResistances(map));}.
     * @param map A char 2D array with '#' for walls and bounded edges.
     */
    public void setMap(char[][] map)
    {
        setMap(FOV.generateSimpleResistances(map));
    }

    /**
     * Get a mapping of Coord keys representing locations to apply this Technique to, to ArrayList of Coord values
     * representing which targets (by their location) are affected by choosing that Coord. All targets with this method
     * are valued equally, and the ideal location affects as many as possible without hitting any requiredExclusions.
     * <br>
     * YOU MUST CALL setMap() with the current map status at some point before using this method, and call it again if
     * the map changes. Failure to do so can cause serious bugs, from logic errors where monsters consider a door
     * closed when it is open or vice versa, to an ArrayIndexOutOfBoundsException being thrown if the player moved to a
     * differently-sized map and the Technique tries to use the previous map with coordinates from the new one.
     *
     * @param user The location of the user of this Technique
     * @param targets Set of Coord of desirable targets to include in the area of this Technique, as many as possible.
     * @param requiredExclusions Set of Coord where each value is something this Technique will really try to avoid.
     * @return OrderedMap of Coord keys representing target points to pass to apply, to ArrayList of Coord values representing what targets' locations will be affected.
     */
    public CoordObjectOrderedMap<ObjectList<Coord>> idealLocations(Coord user, Collection<Coord> targets, Collection<Coord> requiredExclusions) {
        aoe.setOrigin(user);
        return aoe.idealLocations(targets, requiredExclusions);

    }

    /**
     * Get a mapping of Coord keys representing locations to apply this Technique to, to ArrayList of Coord values
     * representing which targets (by their location) are effected by choosing that Coord. This method will strongly
     * prefer including priorityTargets in its area, especially multiple one if possible, and primarily uses
     * lesserTargets as a tiebreaker if two locations have the same number of included priorityTargets but one has more
     * lesserTargets in its area.
     * <br>
     * YOU MUST CALL setMap() with the current map status at some point before using this method, and call it again if
     * the map changes. Failure to do so can cause serious bugs, from logic errors where monsters consider a door
     * closed when it is open or vice versa, to an ArrayIndexOutOfBoundsException being thrown if the player moved to a
     * differently-sized map and the Technique tries to use the previous map with coordinates from the new one.
     *
     * @param user The location of the user of this Technique
     * @param priorityTargets Set of Coord of important targets to include in the area of this Technique, preferring to target a single priorityTarget over four lesserTargets.
     * @param lesserTargets Set of Coord of desirable targets to include in the area of this Technique, as many as possible without excluding priorityTargets.
     * @param requiredExclusions Set of Coord where each value is something this Technique will really try to avoid.
     * @return OrderedMap of Coord keys representing target points to pass to apply, to ArrayList of Coord values representing what targets' locations will be affected.
     */
    public CoordObjectOrderedMap<ObjectList<Coord>> idealLocations(Coord user, Set<Coord> priorityTargets, Set<Coord> lesserTargets, Set<Coord> requiredExclusions) {
        aoe.setOrigin(user);
        return aoe.idealLocations(priorityTargets, lesserTargets, requiredExclusions);
    }

    /**
     * This does one last validation of the location aimAt (checking that it is within the valid range for this
     * Technique) before getting the area affected by the AOE targeting that cell. It considers the origin of the AOE
     * to be the Coord parameter user, for purposes of directional limitations and for AOE implementations that need
     * the user's location, such as ConeAOE and LineAOE.
     * <br>
     * YOU MUST CALL setMap() with the current map status at some point before using this method, and call it again if
     * the map changes. Failure to do so can cause serious bugs, from logic errors where monsters consider a door
     * closed when it is open or vice versa, to an ArrayIndexOutOfBoundsException being thrown if the player moved to a
     * differently-sized map and the Technique tries to use the previous map with coordinates from the new one.
     *
     * @param user The position of the Technique's user, x first, y second.
     * @param aimAt A target Coord typically obtained from idealLocations that determines how to position the AOE.
     * @return a CoordFloatOrderedMap with values from 1.0 (fully affected) to 0.0 (unaffected).
     */
    public CoordFloatOrderedMap apply(Coord user, Coord aimAt)
    {
        aoe.setOrigin(user);
        aoe.shift(aimAt);
        return aoe.findArea();
    }

    /**
     * A quick yes-or-no check for whether a {@code user} at a given Coord can use this Technique to target the given
     * Coord of a {@code possibleTarget}. There doesn't need to be a creature in the possibleTarget cell, since area of
     * effect Techniques could still affect nearby creatures. Returns true if possibleTarget is a viable target cell for
     * the given user Coord with this Technique, or false otherwise.
     * @param user the Coord of the starting cell of this Technique, usually the position of the Technique's user
     * @param possibleTarget a Coord that could maybe be a viable target
     * @return true if this Technique can be used to target {@code possibleTarget} from the position {@code user}, or false otherwise
     */
    public boolean canTarget(Coord user, Coord possibleTarget)
    {
        if(aoe == null || user == null || possibleTarget == null ||
                !AreaUtils.verifyReach(aoe.getReach(), user, possibleTarget)) return false;
        Radius radiusStrategy = aoe.getMetric();
        Coord[] path = BresenhamLine.lineArray(user, possibleTarget);
        float rad = radiusStrategy.radius(user.x, user.y, possibleTarget.x, possibleTarget.y);
        if(rad < aoe.getMinRange() || rad > aoe.getMaxRange()) {
            return false;
        }
        Coord p;
        for (int i = 0; i < path.length; i++) {
            p = path[i];
            if (p.x == possibleTarget.x && p.y == possibleTarget.y) {
                return true;//reached the end
            }
            if ((p.x != user.x || p.y != user.y) && map[p.x][p.y] == '#') {
                // if this isn't the starting cell and the map has a wall here, then stop and return false
                return false;
            }
        }
        return false;//never got to the target point
    }
    /**
     * Gets all possible target-able Coords when using this technique from the given Coord {@code user}, returning them
     * in a Region. Note that a Region can be used as a Collection of Coord with a fairly fast
     * {@link Region#contains(Coord)} method, or at least faster than an ArrayList of Coord. Region values
     * are mutable, like arrays, so if you want to edit the returned value you can do so without constructing additional
     * objects. This works by getting a FOV map (using shadowcasting and the same metric/radius type as the {@link #aoe}
     * field on this Technique) to figure out what cells are visible, then eliminating cells that don't match the
     * minimum range on the AOE or aren't legal targets because of its AimLimit.
     * 
     * @param user the position of the user of this Technique
     * @return all possible Coord values that can be used as targets for this Technique from the given starting Coord, as a Region
     */
    public Region possibleTargets(Coord user)
    {
        return possibleTargets(user, map);
    }
    /**
     * Gets all possible target-able Coords when using this technique from the given Coord {@code user}, returning them
     * in a Region. This takes a 2D float array as a resistance map, the same kind used by {@link FOV}, which
     * can be obtained with {@link FOV#generateResistances(char[][])} or
     * {@link FOV#generateSimpleResistances(char[][])}. Note that a Region can be used as a Collection
     * of Coord with a fairly fast {@link Region#contains(Coord)} method, or at least faster than an ArrayList of
     * Coord. Region values are mutable, like arrays, so if you want to edit the returned value you can do so
     * without constructing additional objects. This works by getting a FOV map (using shadowcasting and the same
     * metric/radius type as the {@link #aoe} field on this Technique) to figure out what cells are visible, then
     * eliminating cells that don't match the minimum range on the AOE or aren't legal targets because of its AimLimit.
     * <br>
     * This method isn't especially efficient because it needs to construct a temporary 2D array for the FOV to use, but
     * it is more efficient than {@link #possibleTargets(Coord)}, which needs to construct an additional temporary 2D
     * array. You may also want to reuse an existing resistance map, and you can with this method.
     * @param user the position of the user of this Technique
     * @param resistanceMap a 2D float array where walls are 1.0 and other values are less; often produced by {@link FOV#generateSimpleResistances(char[][])}
     * @return all possible Coord values that can be used as targets for this Technique from the given starting Coord, as a Region
     */
    public Region possibleTargets(Coord user, float[][] resistanceMap) {
        if (aoe.getMaxRange() <= 0) return new Region(user, map.length, map[0].length);
        float[][] fovmap = new float[map.length][map[0].length];
        FOV.reuseFOV(resistanceMap, fovmap, user.x, user.y, aoe.getMaxRange(), aoe.getMetric());
        float rangeBound = 1.0001f - ((float) aoe.getMinRange() / aoe.getMaxRange());
        AimLimit limit = aoe.getLimitType();
        if (limit == null) limit = AimLimit.FREE;
        switch (limit) {
            case ORTHOGONAL: {
                // Thanks, SillyNameRandomNumber, for finding this fix
                return new Region(fovmap, 0.0001f, rangeBound)
                        .removeRectangle(0, 0, user.x, user.y)
                        .removeRectangle(0, user.y + 1, user.x, map[0].length - user.y - 1)
                        .removeRectangle(user.x + 1, 0, map.length - user.x - 1, user.y)
                        .removeRectangle(user.x + 1, user.y + 1, map.length - user.x - 1, map[0].length - user.y - 1);
            }
            case DIAGONAL: {
                Region all = new Region(fovmap, 0.0001f, rangeBound), used = new Region(map.length, map[0].length);
                for (Coord c : all) {
                    if (Math.abs(c.x - user.x) == Math.abs(c.y - user.y))
                        used.insert(c);
                }
                return used;
            }
            case EIGHT_WAY: {
                Region all = new Region(fovmap, 0.0001f, rangeBound), used = new Region(map.length, map[0].length);
                for (Coord c : all) {
                    if (Math.abs(c.x - user.x) == Math.abs(c.y - user.y) || c.x == user.x || c.y == user.y)
                        used.insert(c);
                }
                return used;
            }
            default:
                return new Region(fovmap, 0.0001f, rangeBound);
        }
    }
}
