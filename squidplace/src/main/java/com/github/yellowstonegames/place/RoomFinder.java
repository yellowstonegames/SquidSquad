package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Region;

import java.util.Arrays;

/**
 * A small class that can analyze a dungeon or other map and identify areas as being "room" or "corridor" based on how
 * thick the walkable areas are (corridors are at most 2 cells wide at their widest, rooms are anything else). Most
 * methods of this class return 2D char arrays or Lists thereof, with the subset of the map that is in a specific region
 * kept the same, but everything else replaced with '#'.
 */
public class RoomFinder {
    /**
     * A copy of the dungeon map, however it was passed to the constructor.
     */
    public char[][] map,
    /**
     * A simplified version of the dungeon map, using '#' for walls and '.' for floors.
     */
    basic;

    public int[][] environment;
    /**
     * Not likely to be used directly, but there may be things you can do with these that are cumbersome using only
     * RoomFinder's simpler API.
     */
    public ObjectObjectOrderedMap<Region, ObjectList<Region>> rooms,
    /**
     * Not likely to be used directly, but there may be things you can do with these that are cumbersome using only
     * RoomFinder's simpler API.
     */
    corridors,
    /**
     * Not likely to be used directly, but there may be things you can do with these that are cumbersome using only
     * RoomFinder's simpler API. Won't be assigned a value if this class is constructed with a 2D char array; it needs
     * the two-arg constructor using the environment produced by a PlaceGenerator.
     */
    caves;

    public Region allRooms, allCaves, allCorridors, allFloors;

    /**
     * When a RoomFinder is constructed, it stores all points of rooms that are adjacent to another region here.
     */
    public Coord[] connections,
    /**
     * Potential doorways, where a room is adjacent to a corridor.
     */
    doorways,
    /**
     * Cave mouths, where a cave is adjacent to another type of terrain.
     */
    mouths;
    public int width, height;

    /**
     * Constructs a RoomFinder given a dungeon map, and finds rooms, corridors, and their connections on the map. Does
     * not find caves; if a collection of caves is requested from this, it will be non-null but empty.
     * @param dungeon a 2D char array that uses '#', box drawing characters, or ' ' for walls.
     */
    public RoomFinder(char[][] dungeon)
    {
        this(dungeon, DungeonTools.ROOM_FLOOR);
    }

    /**
     * Constructs a RoomFinder given a dungeon map and a general kind of environment for the whole map, then finds
     * rooms, corridors, and their connections on the map. Defaults to treating all areas as cave unless
     * {@code environmentKind} is {@link DungeonTools#ROOM_FLOOR} (or its equivalent, 1).
     * @param dungeon a 2D char array that uses '#', box drawing characters, or ' ' for walls.
     * @param environmentKind if 1 ({@link DungeonTools#ROOM_FLOOR}), this will find rooms and corridors, else caves
     */
    public RoomFinder(char[][] dungeon, int environmentKind)
    {
        if(dungeon.length <= 0)
            return;
        width = dungeon.length;
        height = dungeon[0].length;
        map = new char[width][height];
        environment = new int[width][height];
        for (int i = 0; i < width; i++) {
            System.arraycopy(dungeon[i], 0, map[i], 0, height);
        }
        rooms = new ObjectObjectOrderedMap<>(32);
        corridors = new ObjectObjectOrderedMap<>(32);
        caves = new ObjectObjectOrderedMap<>(8);

        basic = DungeonTools.simplifyDungeon(map);

        if(environmentKind == DungeonTools.ROOM_FLOOR) {

            allFloors = new Region(basic, '.');
            allRooms = allFloors.copy().retract8way().flood(allFloors, 2);
            allCorridors = allFloors.copy().andNot(allRooms);
            allCaves = new Region(width, height);

            environment = allCorridors.writeInts(
                    allRooms.writeInts(environment, DungeonTools.ROOM_FLOOR),
                    DungeonTools.CORRIDOR_FLOOR);


            Region d = allCorridors.copy().fringe().and(allRooms);
            connections = doorways = d.asCoords();
            mouths = new Coord[0];
            ObjectList<Region> rs = allRooms.split(), cs = allCorridors.split();

            for (Region sep : cs) {
                Region someDoors = sep.copy().fringe().and(allRooms);
                Coord[] doors = someDoors.asCoords();
                ObjectList<Region> near = new ObjectList<>(4);
                for (int i = 0; i < doors.length; i++) {
                    near.addAll(Region.whichContain(doors[i].x, doors[i].y, rs));
                }
                corridors.put(sep, near);
            }

            for (Region sep : rs) {
                Region aroundDoors = sep.copy().fringe().and(allCorridors);
                Coord[] doors = aroundDoors.asCoords();
                ObjectList<Region> near = new ObjectList<>(10);
                for (int i = 0; i < doors.length; i++) {
                    near.addAll(Region.whichContain(doors[i].x, doors[i].y, cs));
                }
                rooms.put(sep, near);
            }
        }
        else
        {
            allCaves = new Region(basic, '.');
            allFloors = new Region(width, height);
            allRooms = new Region(width, height);
            allCorridors = new Region(width, height);
            caves.put(allCaves, new ObjectList<>());
            connections = mouths = allCaves.copy().andNot(allCaves.copy().retract8way()).retract().asCoords();
            doorways = new Coord[0];
            environment = allCaves.writeInts(environment, DungeonTools.CAVE_FLOOR);

        }
    }

    /**
     * Constructs a RoomFinder given a dungeon map and an environment map (typically obtained with
     * {@link PlaceGenerator#getEnvironment()} after generating a map with {@link PlaceGenerator#getPlaceGrid()}), and
     * finds rooms, corridors, caves, and their connections on the map.
     * @param dungeon a 2D char array that uses '#' for walls.
     * @param environment a 2D int array using constants from DungeonTools; typically produced by a call to
     *                    getEnvironment() in a PlaceGenerator after dungeon generation.
     */
    public RoomFinder(char[][] dungeon, int[][] environment)
    {
        if(dungeon.length <= 0)
            return;
        width = dungeon.length;
        height = dungeon[0].length;
        map = ArrayTools.copy(dungeon);
        this.environment = ArrayTools.copy(environment);
        rooms = new ObjectObjectOrderedMap<>(32);
        corridors = new ObjectObjectOrderedMap<>(32);
        caves = new ObjectObjectOrderedMap<>(32);
        basic = DungeonTools.simplifyDungeon(map);
        allFloors = new Region(basic, '.');
        allRooms = new Region(environment, DungeonTools.ROOM_FLOOR);
        allCorridors = new Region(environment, DungeonTools.CORRIDOR_FLOOR);
        allCaves = new Region(environment, DungeonTools.CAVE_FLOOR);
        Region d = allCorridors.copy().fringe().and(allRooms),
                m = allCaves.copy().fringe().and(allRooms.copy().or(allCorridors));
        doorways = d.asCoords();
        mouths = m.asCoords();
        connections = new Coord[doorways.length + mouths.length];
        System.arraycopy(doorways, 0, connections, 0, doorways.length);
        System.arraycopy(mouths, 0, connections, doorways.length, mouths.length);

        ObjectList<Region> rs = allRooms.split(), cs = allCorridors.split(), vs = allCaves.split();

        for (Region sep : cs) {
            Region someDoors = sep.copy().fringe().and(allRooms);
            Coord[] doors = someDoors.asCoords();
            ObjectList<Region> near = new ObjectList<>(16);
            for (int i = 0; i < doors.length; i++) {
                near.addAll(Region.whichContain(doors[i].x, doors[i].y, rs));
            }
            someDoors.remake(sep).fringe().and(allCaves);
            doors = someDoors.asCoords();
            for (int i = 0; i < doors.length; i++) {
                near.addAll(Region.whichContain(doors[i].x, doors[i].y, vs));
            }
            corridors.put(sep, near);
        }

        for (Region sep : rs) {
            Region aroundDoors = sep.copy().fringe().and(allCorridors);
            Coord[] doors = aroundDoors.asCoords();
            ObjectList<Region> near = new ObjectList<>(32);
            for (int i = 0; i < doors.length; i++) {
                near.addAll(Region.whichContain(doors[i].x, doors[i].y, cs));
            }
            aroundDoors.remake(sep).fringe().and(allCaves);
            doors = aroundDoors.asCoords();
            for (int i = 0; i < doors.length; i++) {
                near.addAll(Region.whichContain(doors[i].x, doors[i].y, vs));
            }
            rooms.put(sep, near);
        }
        for (Region sep : vs) {
            Region aroundMouths = sep.copy().fringe().and(allCorridors);
            Coord[] maws = aroundMouths.asCoords();
            ObjectList<Region> near = new ObjectList<>(48);
            for (int i = 0; i < maws.length; i++) {
                near.addAll(Region.whichContain(maws[i].x, maws[i].y, cs));
            }
            aroundMouths.remake(sep).fringe().and(allRooms);
            maws = aroundMouths.asCoords();
            for (int i = 0; i < maws.length; i++) {
                near.addAll(Region.whichContain(maws[i].x, maws[i].y, rs));
            }
            caves.put(sep, near);
        }
    }
    public RoomFinder reset(char[][] dungeon) {
        return reset(dungeon, DungeonTools.ROOM_FLOOR);
    }
    public RoomFinder reset(char[][] dungeon, int environmentKind){
        if(width != dungeon.length || height != dungeon[0].length){
            width = dungeon.length;
            height = dungeon[0].length;
            map = ArrayTools.copy(dungeon);
            environment = new int[width][height];
            allCaves.resizeAndEmpty(width, height);
        }
        else {
            ArrayTools.insert(dungeon, map, 0, 0);
            ArrayTools.fill(environment, 0);
            allCaves.clear();
        }
        rooms.clear();
        corridors.clear();
        caves.clear();
        basic = DungeonTools.simplifyDungeon(map);

        if(environmentKind == DungeonTools.ROOM_FLOOR) {
            allFloors.refill(basic, '.');
            allRooms.remake(allFloors).retract8way().flood(allFloors, 2);
            allCorridors.remake(allFloors).andNot(allRooms);

            environment = allCorridors.writeInts(
                    allRooms.writeInts(environment, DungeonTools.ROOM_FLOOR),
                    DungeonTools.CORRIDOR_FLOOR);

            Region d = allCorridors.copy().fringe().and(allRooms);
            connections = doorways = d.asCoords();
            mouths = new Coord[0];
            ObjectList<Region> rs = allRooms.split(), cs = allCorridors.split();

            for (Region sep : cs) {
                Region someDoors = sep.copy().fringe().and(allRooms);
                Coord[] doors = someDoors.asCoords();
                ObjectList<Region> near = new ObjectList<>(4);
                for (int i = 0; i < doors.length; i++) {
                    near.addAll(Region.whichContain(doors[i].x, doors[i].y, rs));
                }
                corridors.put(sep, near);
            }

            for (Region sep : rs) {
                Region aroundDoors = sep.copy().fringe().and(allCorridors);
                Coord[] doors = aroundDoors.asCoords();
                ObjectList<Region> near = new ObjectList<>(10);
                for (int i = 0; i < doors.length; i++) {
                    near.addAll(Region.whichContain(doors[i].x, doors[i].y, cs));
                }
                rooms.put(sep, near);
            }
        }
        else {
            allCaves.refill(basic, '.');
            allFloors.resizeAndEmpty(width, height);
            allRooms.resizeAndEmpty(width, height);
            allCorridors.resizeAndEmpty(width, height);
            caves.put(allCaves, new ObjectList<>());
            connections = mouths = allCaves.copy().andNot(allCaves.copy().retract8way()).retract().asCoords();
            doorways = new Coord[0];
            environment = allCaves.writeInts(environment, DungeonTools.CAVE_FLOOR);
        }
        return this;
    }
    public RoomFinder reset(char[][] dungeon, int[][] environment){
        if(width != dungeon.length || height != dungeon[0].length){
            width = dungeon.length;
            height = dungeon[0].length;
            map = ArrayTools.copy(dungeon);
            this.environment = ArrayTools.copy(environment);
            allCaves.resizeAndEmpty(width, height);
        }
        else {
            ArrayTools.insert(dungeon, map, 0, 0);
            ArrayTools.insert(environment, this.environment, 0, 0);
            allCaves.clear();
        }
        rooms.clear();
        corridors.clear();
        caves.clear();
        basic = DungeonTools.simplifyDungeon(map);
        allFloors.refill(basic, '.');
        allRooms.refill(this.environment, DungeonTools.ROOM_FLOOR);
        allCorridors.refill(this.environment, DungeonTools.CORRIDOR_FLOOR);
        allCaves.refill(this.environment, DungeonTools.CAVE_FLOOR);

        Region d = allCorridors.copy().fringe().and(allRooms),
                m = allCaves.copy().fringe().and(allRooms.copy().or(allCorridors));
        doorways = d.asCoords();
        mouths = m.asCoords();
        connections = new Coord[doorways.length + mouths.length];
        System.arraycopy(doorways, 0, connections, 0, doorways.length);
        System.arraycopy(mouths, 0, connections, doorways.length, mouths.length);

        ObjectList<Region> rs = allRooms.split(), cs = allCorridors.split(), vs = allCaves.split();

        for (Region sep : cs) {
            Region someDoors = sep.copy().fringe().and(allRooms);
            Coord[] doors = someDoors.asCoords();
            ObjectList<Region> near = new ObjectList<>(16);
            for (int i = 0; i < doors.length; i++) {
                near.addAll(Region.whichContain(doors[i].x, doors[i].y, rs));
            }
            someDoors.remake(sep).fringe().and(allCaves);
            doors = someDoors.asCoords();
            for (int i = 0; i < doors.length; i++) {
                near.addAll(Region.whichContain(doors[i].x, doors[i].y, vs));
            }
            corridors.put(sep, near);
        }

        for (Region sep : rs) {
            Region aroundDoors = sep.copy().fringe().and(allCorridors);
            Coord[] doors = aroundDoors.asCoords();
            ObjectList<Region> near = new ObjectList<>(32);
            for (int i = 0; i < doors.length; i++) {
                near.addAll(Region.whichContain(doors[i].x, doors[i].y, cs));
            }
            aroundDoors.remake(sep).fringe().and(allCaves);
            doors = aroundDoors.asCoords();
            for (int i = 0; i < doors.length; i++) {
                near.addAll(Region.whichContain(doors[i].x, doors[i].y, vs));
            }
            rooms.put(sep, near);
        }
        for (Region sep : vs) {
            Region aroundMouths = sep.copy().fringe().and(allCorridors);
            Coord[] maws = aroundMouths.asCoords();
            ObjectList<Region> near = new ObjectList<>(48);
            for (int i = 0; i < maws.length; i++) {
                near.addAll(Region.whichContain(maws[i].x, maws[i].y, cs));
            }
            aroundMouths.remake(sep).fringe().and(allRooms);
            maws = aroundMouths.asCoords();
            for (int i = 0; i < maws.length; i++) {
                near.addAll(Region.whichContain(maws[i].x, maws[i].y, rs));
            }
            caves.put(sep, near);
        }

        return this;
    }

    /**
     * Gets all the rooms this found during construction, returning them as an ObjectList of 2D char arrays, where an
     * individual room is "masked" so only its contents have normal map chars and the rest have only '#'.
     * @return an ObjectList of 2D char arrays representing rooms.
     */
    public ObjectList<char[][]> findRooms()
    {
        ObjectList<char[][]> rs = new ObjectList<>(rooms.size());
        for(Region r : rooms.keySet())
        {
            rs.add(r.writeCharsToOff(map, '#'));
        }
        return rs;
    }

    /**
     * Gets all the corridors this found during construction, returning them as an ObjectList of 2D char arrays, where an
     * individual corridor is "masked" so only its contents have normal map chars and the rest have only '#'.
     * @return an ObjectList of 2D char arrays representing corridors.
     */
    public ObjectList<char[][]> findCorridors()
    {
        ObjectList<char[][]> cs = new ObjectList<>(corridors.size());
        for(Region c : corridors.keySet())
        {
            cs.add(c.writeCharsToOff(map, '#'));
        }
        return cs;
    }

    /**
     * Gets all the caves this found during construction, returning them as an ObjectList of 2D char arrays, where an
     * individual room is "masked" so only its contents have normal map chars and the rest have only '#'. Will only
     * return a non-empty collection if the two-arg constructor was used and the environment contains caves.
     * @return an ObjectList of 2D char arrays representing caves.
     */
    public ObjectList<char[][]> findCaves()
    {
        ObjectList<char[][]> vs = new ObjectList<>(caves.size());
        for(Region v : caves.keySet())
        {
            vs.add(v.writeCharsToOff(map, '#'));
        }
        return vs;
    }
    /**
     * Gets all the rooms, corridors, and caves this found during construction, returning them as an ObjectList of 2D
     * char arrays, where an individual room or corridor is "masked" so only its contents have normal map chars and the
     * rest have only '#'.
     * @return an ObjectList of 2D char arrays representing rooms, corridors, or caves.
     */
    public ObjectList<char[][]> findRegions()
    {
        ObjectList<char[][]> rs = new ObjectList<>(rooms.size() + corridors.size() + caves.size());
        for(Region r : rooms.keySet())
        {
            rs.add(r.writeCharsToOff(map, '#'));
        }
        for(Region c : corridors.keySet())
        {
            rs.add(c.writeCharsToOff(map, '#'));
        }
        for(Region v : caves.keySet())
        {
            rs.add(v.writeCharsToOff(map, '#'));
        }
        return rs;
    }
    private static char[][] defaultFill(int width, int height)
    {
        char[][] d = new char[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(d[x], '#');
        }
        return d;
    }

    /**
     * Merges multiple 2D char arrays where the '#' character means "no value", and combines them so all cells with
     * value are on one map, with '#' filling any other cells. If regions is empty, this uses width and height to
     * construct a blank map, all '#'. It will also use width and height for the size of the returned 2D array.
     * @param regions An ObjectList of 2D char array regions, where '#' is an empty value and all others will be merged
     * @param width the width of any map this returns
     * @param height the height of any map this returns
     * @return a 2D char array that merges all non-'#' areas in regions, and fills the rest with '#'
     */
    public static char[][] merge(ObjectList<char[][]> regions, int width, int height)
    {
        if(regions == null || regions.isEmpty())
            return defaultFill(width, height);
        char[][] first = regions.get(0);
        char[][] dungeon = new char[Math.min(width, first.length)][Math.min(height, first[0].length)];
        for (int x = 0; x < first.length; x++) {
            Arrays.fill(dungeon[x], '#');
        }
        for(char[][] region : regions)
        {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(region[x][y] != '#')
                        dungeon[x][y] = region[x][y];
                }
            }
        }
        return dungeon;
    }

    /**
     * Takes an x, y position and finds the room, corridor, or cave at that position, if there is one, returning the
     * same 2D char array format as the other methods.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return a masked 2D char array where anything not in the current region is '#'
     */
    public char[][] regionAt(int x, int y)
    {

        ObjectOrderedSet<Region> regions = Region.whichContain(x, y, rooms.keySet());
        regions.addAll(Region.whichContain(x, y, corridors.keySet()));
        regions.addAll(Region.whichContain(x, y, caves.keySet()));
        Region found;
        if(regions.isEmpty())
            found = new Region(width, height);
        else
            found = regions.first();
        return found.writeCharsToOff(map, '#');
    }

    /**
     * Takes an x, y position and finds the room or corridor at that position and the rooms, corridors or caves that it
     * directly connects to, and returns the group as one merged 2D char array.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return a masked 2D char array where anything not in the current region or one nearby is '#'
     */
    public char[][] regionsNear(int x, int y)
    {
        ObjectOrderedSet<Region> atRooms = Region.whichContain(x, y, rooms.keySet()),
                atCorridors = Region.whichContain(x, y, corridors.keySet()),
                atCaves = Region.whichContain(x, y, caves.keySet()),
                regions = new ObjectOrderedSet<>(64);
        regions.addAll(atRooms);
        regions.addAll(atCorridors);
        regions.addAll(atCaves);
        Region found;
        if(regions.isEmpty())
            found = new Region(width, height);
        else
        {
            found = regions.first();
            ObjectList<ObjectList<Region>> near = new ObjectList<>(atRooms.size());
            for (int i = 0; i < atRooms.size(); i++)
                near.add(rooms.getAt(i));
            for (ObjectList<Region> links : near) {
                for(Region n : links)
                {
                    found.or(n);
                }
            }
            near.clear();
            for (int i = 0; i < atCorridors.size(); i++)
                near.add(corridors.getAt(i));
            for (ObjectList<Region> links : near) {
                for(Region n : links)
                {
                    found.or(n);
                }
            }
            near.clear();
            for (int i = 0; i < atCaves.size(); i++)
                near.add(caves.getAt(i));
            for (ObjectList<Region> links : near) {
                for(Region n : links)
                {
                    found.or(n);
                }
            }
        }
        return found.writeCharsToOff(map, '#');
    }

    /**
     * Takes an x, y position and finds the rooms or corridors that are directly connected to the room, corridor or cave
     * at that position, and returns the group as an ObjectList of 2D char arrays, one per connecting region.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return an ObjectList of masked 2D char arrays where anything not in a connected region is '#'
     */
    public ObjectList<char[][]> regionsConnected(int x, int y)
    {
        ObjectList<char[][]> regions = new ObjectList<>(10);
        ObjectList<ObjectList<Region>> near = new ObjectList<>(rooms.size());
        ObjectOrderedSet<Region> at = Region.whichContain(x, y, rooms.keySet());
        for (int i = 0; i < at.size(); i++)
            near.add(rooms.getAt(i));
        for (ObjectList<Region> links : near) {
            for(Region n : links)
            {
                regions.add(n.writeCharsToOff(map, '#'));
            }
        }
        at = Region.whichContain(x, y, corridors.keySet());
        for (int i = 0; i < at.size(); i++)
            near.add(corridors.getAt(i));
        for (ObjectList<Region> links : near) {
            for (Region n : links) {
                regions.add(n.writeCharsToOff(map, '#'));
            }
        }
        at = Region.whichContain(x, y, caves.keySet());
        for (int i = 0; i < at.size(); i++)
            near.add(caves.getAt(i));
        for (ObjectList<Region> links : near) {
            for(Region n : links)
            {
                regions.add(n.writeCharsToOff(map, '#'));
            }
        }

        return regions;
    }
}
