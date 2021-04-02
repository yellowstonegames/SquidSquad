package com.github.yellowstonegames.path;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Direction;
import com.github.yellowstonegames.grid.Radius;
import com.github.yellowstonegames.grid.Region;

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;

/**
 * Calculates the Zone of Influence, also known as Zone of Control, for different points on a map.
 * Uses Region for faster storage and manipulation of zones; it's suggested if you use this class to be
 * somewhat familiar with the methods for manipulating data in that class, though a Region can also be used just
 * like a Collection of Coord values.
 * <br>
 * Created by Tommy Ettinger on 1/14/2018.
 */
public class ZoneOfInfluence implements Serializable {
    private static final long serialVersionUID = 2L;
    protected DijkstraMap dijkstra;
    protected Coord[][] influences;
    protected Region[] groups;
    protected boolean completed;
    protected Radius radius;
    /**
     * Constructs a Zone of Influence map. Takes a (quite possibly jagged) array of arrays of Coord influences, where
     * the elements of the outer array represent different groups of influencing "factions" or groups that exert control
     * over nearby areas, and the Coord elements of the inner array represent individual spots that are part of those
     * groups and share influence with all Coord in the same inner array. Also takes a char[][] for a map, which can be
     * the simplified map with only '#' for walls and '.' for floors, or the final map (with chars like '~' for deep
     * water as well as walls and floors), and a Radius enum that will be used to determine how distance is calculated.
     * <br>
     * Call {@link #calculate()} when you want information out of this.
     * @param influences an outer array containing influencing groups, each an array containing Coords that influence
     * @param map a char[][] that is used as an area map; should be bounded
     * @param radiusStrategy a Radius enum that corresponds to how distance should be measured
     */
    public ZoneOfInfluence(Coord[][] influences, char[][] map, Radius radiusStrategy) {
        this.influences = influences;
        groups = new Region[influences.length];
        radius = radiusStrategy == null ? Radius.CIRCLE : radiusStrategy;
        dijkstra = new DijkstraMap(map, radius.matchingMeasurement());
    }
    /**
     * Constructs a Zone of Influence map. Takes an arrays of Coord influences, where each Coord is treated as both a
     * one-element group of influencing "factions" or groups that exert control over nearby areas, and the individual
     * spot that makes up one of those groups and spreads influence. Also takes a char[][] for a map, which can be the
     * simplified map with only '#' for walls and '.' for floors, or the final map (with chars like '~' for deep water
     * as well as walls and floors), and a Radius enum that will be used to determine how distance is calculated.
     * <br>
     * Call {@link #calculate()} when you want information out of this.
     * @param influences an array containing Coords that each have their own independent influence
     * @param map a char[][] that is used as an area map; should be bounded
     * @param radiusStrategy a Radius enum that corresponds to how distance should be measured
     * @see com.github.yellowstonegames.grid.PoissonDisk PoissonDisk provides a good way to generate evenly spaced Coords
     */
    public ZoneOfInfluence(Coord[] influences, char[][] map, Radius radiusStrategy) {
        this.influences = new Coord[influences.length][];
        for (int i = 0; i < influences.length; i++) {
            this.influences[i] = new Coord[] { influences[i] };
        }
        groups = new Region[influences.length];
        radius = radiusStrategy == null ? Radius.CIRCLE : radiusStrategy;
        dijkstra = new DijkstraMap(map, radius.matchingMeasurement());
    }
    /**
     * Constructs a Zone of Influence map. Takes a Collection of Coord influences, where each Coord is treated as both a
     * one-element group of influencing "factions" or groups that exert control over nearby areas, and the individual
     * spot that makes up one of those groups and spreads influence. Also takes a char[][] for a map, which can be the
     * simplified map with only '#' for walls and '.' for floors, or the final map (with chars like '~' for deep water
     * as well as walls and floors), and a Radius enum that will be used to determine how distance is calculated.
     * <br>
     * It often makes sense to use a Region as the Collection of Coord.
     * <br>
     * Call {@link #calculate()} when you want information out of this.
     * @param influences A Collection of Coord, such as a Region, where each Coord has independent influence
     * @param map a char[][] that is used as an area map; should be bounded
     * @param radiusStrategy a Radius enum that corresponds to how distance should be measured
     */
    public ZoneOfInfluence(Collection<Coord> influences, char[][] map, Radius radiusStrategy) {
        this.influences = new Coord[influences.size()][];
        int i = 0;
        for(Coord c : influences) {
            this.influences[i++] = new Coord[]{c};
        }
        groups = new Region[this.influences.length];
        radius = radiusStrategy == null ? Radius.CIRCLE : radiusStrategy;
        dijkstra = new DijkstraMap(map, radius.matchingMeasurement());
    }

    /**
     * Finds the zones of influence for each of the influences (inner arrays of Coord) this was constructed with, and
     * returns all zones as a Region array. This has each zone of influence overlap with its neighbors; this
     * is useful to find borders using {@link Region#and(Region)}, and borders are typically between 1 and
     * 2 cells wide. You can get a different region if you want region A without the overlapping areas it shares with
     * region B by using {@link Region#andNot(Region)}. Merging two zones A and B can be done with
     * {@link Region#or(Region)}. You can transform the data into a boolean[][] easily with
     * {@link Region#decode()}, where true is contained in the zone and false is not. The methods
     * {@link Region#fringe()}, {@link Region#expand()}, {@link Region#singleRandom(EnhancedRandom)}, and
     * {@link Region#separatedBlue(float)} are also potentially useful for this sort of data. You should save
     * the {@code Region[]} for later use if you want to call
     * {@link #nearestInfluences(Region[], Coord)}.
     * <br>
     * The first Region in the returned Region[] will correspond to the area influenced by the first
     * Coord[] in the nested array passed to the constructor (or the first Coord if a non-nested array was passed); the
     * second will correspond to the second, and so on. The length of the Region[] this returns will equal the
     * number of influence groups.
     * @return a Region array, with each item storing a zone's area
     */
    public Region[] calculate()
    {
        for (int i = 0; i < influences.length; i++) {
            for (int j = 0; j < influences[i].length; j++) {
                dijkstra.setGoal(influences[i][j]);
            }
        }
        dijkstra.scan(null, null);
        final float[][] scannedAll = dijkstra.gradientMap;

        for (int i = 0; i < influences.length; i++) {
            groups[i] = increasing(scannedAll, influences[i]);
        }
        completed = true;
        return groups;
    }
    protected Region increasing(float[][] dm, Coord[] inf) {
        ObjectOrderedSet<Coord> open = new ObjectOrderedSet<>(inf), fresh = new ObjectOrderedSet<>(64);
        Direction[] dirs = (radius == Radius.DIAMOND) ? Direction.CARDINALS : Direction.OUTWARDS;
        Region influenced = new Region(dijkstra.width, dijkstra.height);
        final int width = dm.length;
        final int height = width == 0 ? 0 : dm[0].length;

        int numAssigned = open.size();
        float diff;
        while (numAssigned > 0) {
            numAssigned = 0;
            for (Coord cell : open) {
                influenced.insert(cell);
                for (int d = 0; d < dirs.length; d++) {
                    Coord adj = cell.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                    	/* Outside the map */
                    	continue;
                    if (!open.contains(adj) && dm[adj.x][adj.y] < DijkstraMap.FLOOR && !influenced.contains(adj)) {
                        //h = heuristic(dirs[d]);
                        diff = dm[adj.x][adj.y] - dm[cell.x][cell.y];
                        if (diff <= 1.0 && diff >= 0) {
                            fresh.add(adj);
                            influenced.insert(adj);
                            ++numAssigned;
                        }
                    }
                }
            }

            open.clear();
            open.addAll(fresh);
            fresh.clear();
        }

        return influenced;
    }

    /**
     * Given the zones resulting from this class' {@link #calculate()} method and a Coord to check, finds the indices of
     * all influencing groups in zones that have the Coord in their area, and returns all such indices as a
     * newly-allocated {@link IntList}.
     * @param zones a Region[] returned by calculate
     * @param point the Coord to test
     * @return an IntVLA where each element is the index of an influencing group in zones
     */
    public IntList nearestInfluences(Region[] zones, Coord point)
    {
        IntList found = new IntList(4);
        for (int i = 0; i < zones.length; i++) {
            if(zones[i].contains(point))
                found.add(i);
        }
        return found;
    }
    /**
     * This can be given a Coord to check in the results of the latest calculate() call. Finds the indices of all
     * influencing groups in zones that have the Coord in their area, and returns all such indices as a newly-allocated
     * {@link IntList}.
     *
     * @param point the Coord to test
     * @return an IntVLA where each element is the index of an influencing group in zones
     */
    public IntList nearestInfluences(Coord point)
    {
        if(!completed)
            return new IntList(0);
        IntList found = new IntList(4);
        for (short i = 0; i < groups.length; i++) {
            if(groups[i].contains(point))
                found.add(i);
        }
        return found;
    }

    /**
     * Gets the influencing groups; ideally the result should not be changed without setting it back with setInfluences.
     * @return influences a jagged array of Coord arrays, where the inner arrays are groups of influences
     */
    public Coord[][] getInfluences() {
        return influences;
    }

    /**
     * Changes the influencing groups. This also invalidates the last calculation for the purposes of nearestInfluences,
     * at least for the overload that takes only a Coord.
     * @param influences a jagged array of Coord arrays, where the inner arrays are groups of influences
     */
    public void setInfluences(Coord[][] influences) {
        if(groups.length == influences.length)
        {
            for (int i = 0; i < groups.length; i++) {
                groups[i].clear();
            }
        }
        else
            groups = new Region[influences.length];
        this.influences = influences;
        completed = false;
    }
}
