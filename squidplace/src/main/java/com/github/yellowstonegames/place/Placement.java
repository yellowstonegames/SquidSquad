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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.FOV;
import com.github.yellowstonegames.grid.Radius;
import com.github.yellowstonegames.grid.Region;

/**
 * Utility class for finding areas where game-specific terrain features might be suitable to place.
 * Example placement for alongStraightWalls, using all regions where there's an extended straight wall in a room to
 * place a rack of bows (as curly braces): https://gist.github.com/tommyettinger/2b69a265bd93304f091b
 * @author Tommy Ettinger
 */
public class Placement {

    /**
     * The RoomFinder this uses internally to find placement areas only where they are appropriate.
     */
    public RoomFinder finder;

    private Region allRooms, allCorridors, allCaves, allFloors, nonRoom;
    private ObjectOrderedSet<ObjectOrderedSet<Coord>> alongStraightWalls,
            corners, centers;
    private ObjectOrderedSet<Coord> hidingPlaces;
    private Placement()
    {

    }

    /**
     * Constructs a Placement using the given RoomFinder, which will have collections of rooms, corridors, and caves.
     * A common use case for this class involves the Placement field that is constructed in a SectionDungeonGenerator
     * when generate() or generateRespectingStairs() in that class is called; if you use SectionDungeonGenerator, there
     * isn't much need for this constructor, since you can normally use the one created as a field in that class.
     * @param finder a RoomFinder that must not be null.
     */
    public Placement(RoomFinder finder)
    {
        if(finder == null)
            throw new UnsupportedOperationException("RoomFinder passed to Placement constructor cannot be null");

        this.finder = finder;

        /*
        allRooms = new Region(finder.width, finder.height);
        allCorridors = new Region(finder.width, finder.height);
        allCaves = new Region(finder.width, finder.height);
        allFloors = new Region(finder.width, finder.height);

        for(Region region : finder.rooms.keySet())
        {
            allRooms.or(region);
        }
        for(Region region : finder.corridors.keySet())
        {
            allCorridors.or(region);
        }
        for(Region region : finder.caves.keySet())
        {
            allCaves.or(region);
        }
        */
        allCorridors = finder.allCorridors;
        allRooms = finder.allRooms;
        allCaves = finder.allCaves;
        allFloors = allRooms.copy().or(allCorridors).or(allCaves);
        nonRoom = allCorridors.copy().or(allCaves).expand(2);
    }

    /**
     * Gets an ObjectOrderedSet of ObjectOrderedSet of Coord, where each inner ObjectOrderedSet of Coord refers to a placement
     * region along a straight wall with length 3 or more, not including corners. Each Coord refers to a single cell
     * along the straight wall. This could be useful for placing weapon racks in armories, chalkboards in schoolrooms
     * (tutorial missions, perhaps?), or even large paintings/murals in palaces.
     * @return a set of sets of Coord where each set of Coord is a wall's viable placement for long things along it
     */
    public ObjectOrderedSet<ObjectOrderedSet<Coord>> getAlongStraightWalls() {
        if(alongStraightWalls == null)
        {
            alongStraightWalls = new ObjectOrderedSet<>(32);
            Region working = new Region(finder.width, finder.height);
            for(Region region : finder.rooms.keySet()) {
                working.remake(region).retract().fringe().andNot(nonRoom);
                for (Region sp : working.split()) {
                    if (sp.size() >= 3)
                        alongStraightWalls.add(new ObjectOrderedSet<>(sp));
                }
            }
        }
        return alongStraightWalls;
    }

    /**
     * Gets an ObjectOrderedSet of ObjectOrderedSet of Coord, where each inner ObjectOrderedSet of Coord refers to a room's
     * corners, and each Coord is one of those corners. There are more uses for corner placement than I can list. This
     * doesn't always identify all corners, since it only finds ones in rooms, and a cave too close to a corner can
     * cause that corner to be ignored.
     * @return a set of sets of Coord where each set of Coord is a room's corners
     */
    public ObjectOrderedSet<ObjectOrderedSet<Coord>> getCorners() {
        if(corners == null)
        {
            corners = new ObjectOrderedSet<>(32);
            Region working = new Region(finder.width, finder.height);
            for(Region region : finder.rooms.keySet()) {
                working.remake(region).expand().retract8way().xor(region).andNot(nonRoom);
                ObjectOrderedSet<Coord> os = new ObjectOrderedSet<>(working);
                corners.add(os);
            }
        }
        return corners;
    }
    /**
     * Gets an ObjectOrderedSet of ObjectOrderedSet of Coord, where each inner ObjectOrderedSet of Coord refers to a room's cells
     * that are furthest from the walls, and each Coord is one of those central positions. There are many uses for this,
     * like finding a position to place a throne or shrine in a large room where it should be visible from all around.
     * This doesn't always identify all centers, since it only finds ones in rooms, and it can also find multiple
     * central points if they are all the same distance from a wall (common in something like a 3x7 room, where it will
     * find a 1x5 column as the centers of that room).
     * @return a set of sets of Coord where each set of Coord contains a room's cells that are furthest from the walls.
     */
    public ObjectOrderedSet<ObjectOrderedSet<Coord>> getCenters() {
        if(centers == null)
        {
            centers = new ObjectOrderedSet<>(32);
            Region working, working2;
            for(Region region : finder.rooms.keySet()) {

                working = null;
                working2 = region.copy().retract();
                for (int i = 2; i < 7; i++) {
                    if(working2.isEmpty())
                        break;
                    working = working2.copy();
                    working2.retract();
                }
                if(working == null)
                    continue;

                //working =
                //        differencePacked(
                //                working,
                //                nonRoom);
                centers.add(new ObjectOrderedSet<>(working));

            }
        }
        return centers;
    }

    /**
     * Gets an ObjectOrderedSet of Coord, where each Coord is hidden (using the given radiusStrategy and range for FOV
     * calculations) from any doorways or similar narrow choke-points where a character might be easily ambushed. If
     * multiple choke-points can see a cell (using shadow-casting FOV, which is asymmetrical), then the cell is very
     * unlikely to be included in the returned Coords, but if a cell is visible from one or no choke-points and is far
     * enough away, then it is more likely to be included.
     * @param radiusStrategy a Radius object that will be used to determine visibility.
     * @param range the minimum distance things are expected to hide at; often related to player FOV range
     * @return a Set of Coord where each Coord is either far away from or is concealed from a door-like area
     */
    public ObjectOrderedSet<Coord> getHidingPlaces(Radius radiusStrategy, float range) {
        if(hidingPlaces == null)
        {
            float[][] composite = new float[finder.width][finder.height],
                    resMap = FOV.generateResistances(finder.map),
                    temp = new float[finder.width][finder.height];
            Coord pt;
            for (int d = 0; d < finder.connections.length; d++) {
                pt = finder.connections[d];
                FOV.reuseFOV(resMap, temp, pt.x, pt.y, range, radiusStrategy);
                for (int x = 0; x < finder.width; x++) {
                    for (int y = 0; y < finder.height; y++) {
                        composite[x][y] += temp[x][y] * temp[x][y];
                    }
                }
            }

            hidingPlaces = new ObjectOrderedSet<>(new Region(composite, 0.25f).and(allFloors));
        }
        return hidingPlaces;
    }

    private static ObjectOrderedSet<Coord> arrayToSet(Coord[] arr)
    {
        return ObjectOrderedSet.with(arr);
    }
}
