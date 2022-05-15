/*
 * Copyright (c) 2022 See AUTHORS file.
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

import com.github.tommyettinger.ds.ObjectIntOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.HilbertCurve;
import com.github.yellowstonegames.grid.Region;

/**
 * Generate dungeons based on a random, winding, looping path through 3D space, requiring a character to move up and
 * down as well as north/south/east/west to get through the dungeon. Uses techniques from MixedGenerator.
 * Uses a Moore Curve, which is related to Hilbert Curves but loops back to its starting point, and stretches and
 * distorts the grid to make sure a visual correlation isn't obvious.
 * <br>
 * The name comes from a vivid dream I had about gigantic, multi-colored snakes that completely occupied a roguelike
 * dungeon. Shortly after, I made the connection to the Australian mythology I'd heard about the Rainbow Serpent, which
 * in some stories dug water-holes and was similarly gigantic.
 */
public class SerpentDeepMapGenerator {
    private MixedGenerator[] mix;
    private int[] columns, rows;
    private int width, height, depth;
    private ObjectList<ObjectOrderedSet<Coord>> linksUp,linksDown;
    private EnhancedRandom random;

    /**
     * This prepares a map generator that will generate a map with the given width, height and depth, using the given
     * IRNG. The intended purpose is to carve a long path that loops through the whole dungeon's 3D space, while
     * hopefully maximizing the amount of rooms the player encounters. You call the different carver-adding methods to
     * affect what the dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(),
     * defaulting to only caves if none are called. You call generate() after adding carvers, which returns a char[][][]
     * for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param depth the number of levels deep to create
     * @param rng an IRNG object to use for random choices; this make a lot of random choices.
     * @see MixedGenerator
     */
    public SerpentDeepMapGenerator(int width, int height, int depth, EnhancedRandom rng) {
        this(width, height, depth, rng, 0.3);
    }
    /**
     * This prepares a map generator that will generate a map with the given width, height and depth, using the given
     * IRNG, and will branch out to other nearby rooms that (probably) do not have staircases between layers.
     * The intended purpose is to carve a long path that loops through the whole dungeon's 3D space, while
     * hopefully maximizing the amount of rooms the player encounters. You call the different carver-adding methods to
     * affect what the dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(),
     * defaulting to only caves if none are called. You call generate() after adding carvers, which returns a char[][][]
     * for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param depth the number of levels deep to create
     * @param rng an IRNG object to use for random choices; this make a lot of random choices.
     * @param branchingChance the odds from 0.0 to 1.0 that a branch will be created near each necessary room.
     * @see MixedGenerator
     */
    public SerpentDeepMapGenerator(int width, int height, int depth, EnhancedRandom rng, double branchingChance)
    {
        if(width <= 2 || height <= 2)
            throw new IllegalArgumentException("width and height must be greater than 2");
        if(depth < 1)
            throw new IllegalArgumentException("depth must be at least 1");
        HilbertCurve.init3D();
        random = rng;
        this.width = width;
        this.height = height;
        this.depth = depth;
        int numLayers = (int)Math.ceil(depth / 4.0);
        long alterations = random.nextLong();
        long columnAlterations = alterations & 0xFFFFFFFFL;
        float columnBase = (width - 2) / (Long.bitCount(columnAlterations) + 16.0f);
        long rowAlterations = alterations >>> 32;
        float rowBase = (height - 2) / (Long.bitCount(rowAlterations) + 16.0f);

        columns = new int[16];
        rows = new int[16];
        linksUp = new ObjectList<>(depth);
        linksDown = new ObjectList<>(depth);
        for (int i = 0; i < depth; i++) {
            linksUp.add(new ObjectOrderedSet<>(80));
            linksDown.add(new ObjectOrderedSet<>(80));
        }
        float csum = 0, rsum = 0;
        long b = 3;
        for (int i = 0; i < 16; i++, b <<= 2) {
            columns[i] = (int)((csum += columnBase * (1 + Long.bitCount(columnAlterations & b))));
            rows[i] = (int)((rsum += rowBase * (1 + Long.bitCount(rowAlterations & b))));
        }

        ObjectList<ObjectObjectOrderedMap<Coord, ObjectList<Coord>>> connections = new ObjectList<>(depth);
        for (int i = 0; i < depth; i++) {
            connections.add(new ObjectObjectOrderedMap<>(80));
        }
        int m = random.nextInt(0x800 * numLayers);
        int x = HilbertCurve.getXMoore3D(m, numLayers), y = HilbertCurve.getYMoore3D(m, numLayers),
                z = (int)(HilbertCurve.getZMoore3D(m, numLayers) * depth / (8f * numLayers)),
                sx = x, sy = y, sz = z, tz = z;
        int r = random.nextInt(12, 33);
        m += r;
        for (int i = 0; i < 0x800 * numLayers; r = random.nextInt(12, 33), i += r, m = (m + r) % (0x800 * numLayers)) {
            int tx = x, ty = y;
            do {
                ObjectList<Coord> cl = new ObjectList<>(4);

                for (int j = 0;
                     j < 2;
                     j++) {
                    int x2 = random.nextInt(Math.max(0, tx - 2), tx);
                    int x3 = Math.min(random.nextInt(tx + 1, tx + 3), 15);
                    int y2 = random.nextInt(Math.max(0, ty - 2), ty);
                    int y3 = Math.min(random.nextInt(ty + 1, ty + 3), 15);
                    if (random.nextBoolean())
                        x2 = x3;
                    if (random.nextBoolean())
                        y2 = y3;
                    cl.add(Coord.get(columns[x2], rows[y2]));
                    if (random.nextDouble() >= branchingChance)
                        break;
                }

                ObjectList<Coord> connect = connections.get(tz).get(Coord.get(columns[tx], rows[ty]));
                if(connect != null)
                    connect.addAll(cl);
                else
                    connections.get(tz).put(Coord.get(columns[tx], rows[ty]), new ObjectList<>(cl));

                x = HilbertCurve.getXMoore3D(m, numLayers);
                y = HilbertCurve.getYMoore3D(m, numLayers);
                z = (int)Math.floor(HilbertCurve.getZMoore3D(m, numLayers) * depth / (8f * numLayers));
                if(z != tz)
                    cl.clear();
                cl.add(Coord.get(columns[x], rows[y]));

                if (tz == z) {
                    ObjectList<Coord> conn = connections.get(z).get(Coord.get(columns[tx], rows[ty]));
                    if(conn != null)
                        conn.addAll(cl);
                    else
                        connections.get(z).put(Coord.get(columns[tx], rows[ty]), new ObjectList<>(cl));
                    break;
                }
                else {
                    if (z > tz) {
                        linksDown.get(tz).add(Coord.get(tx, ty));
                        tz++;
                        linksUp.get(tz).add(Coord.get(tx, ty));
                    }
                    else
                    {
                        linksUp.get(tz).add(Coord.get(tx, ty));
                        tz--;
                        linksDown.get(tz).add(Coord.get(tx, ty));
                    }
                }
            }while (true);
        }

        do {
            ObjectList<Coord> cl = new ObjectList<>(4);

            for (int j = 0;
                 j < 2;
                 j++) {
                int x2 = random.nextInt(Math.max(0, x - 2), x);
                int x3 = random.nextInt(x + 1, Math.min(x + 3, 15));
                int y2 = random.nextInt(Math.max(0, y - 2), y);
                int y3 = random.nextInt(y + 1, Math.min(y + 3, 15));
                if (x3 < 16 && random.nextBoolean())
                    x2 = x3;
                if (y3 < 16 && random.nextBoolean())
                    y2 = y3;
                cl.add(Coord.get(columns[x2], rows[y2]));
                if (Math.min(random.nextDouble(), random.nextDouble()) >= branchingChance)
                    break;
            }

            ObjectList<Coord> connect = connections.get(tz).get(Coord.get(columns[x], rows[y]));
            if(connect != null)
                connect.addAll(cl);
            else
                connections.get(tz).put(Coord.get(columns[x], rows[y]), new ObjectList<>(cl));

            if(sz != tz)
                cl.clear();
            cl.add(Coord.get(columns[x], rows[y]));

            if (tz == sz) {
                connections.get(sz).get(Coord.get(columns[x], rows[y])).add(
                        Coord.get(columns[sx], rows[sy]));
                break;
            }
            else {
                if (sz > tz) {
                    linksDown.get(tz).add(Coord.get(x, y));
                    tz++;
                    linksUp.get(tz).add(Coord.get(x, y));
                }
                else
                {
                    linksUp.get(tz).add(Coord.get(x, y));
                    tz--;
                    linksDown.get(tz).add(Coord.get(x, y));
                }
            }
        }while (true);

        mix = new MixedGenerator[depth];
        for (int i = 0; i < depth; i++) {
            mix[i] = new MixedGenerator(width, height, random, connections.get(i), 0.35f);
        }
    }
    /**
     * Changes the number of "carvers" that will create caves from one room to the next. If count is 0 or less, no caves
     * will be made. If count is at least 1, caves are possible, and higher numbers relative to the other carvers make
     * caves more likely. Carvers are shuffled when used, then repeat if exhausted during generation. Since typically
     * about 30-40 rooms are carved, large totals for carver count aren't really needed; aiming for a total of 10
     * between the count of putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making caves between rooms; only matters in relation to other carvers
     */
    public void putCaveCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putCaveCarvers(count);
        }
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putBoxRoomCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putBoxRoomCarvers(count);
        }
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. This also
     * ensures walls will be placed around the room, only allowing corridors and small cave openings to pass. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledBoxRoomCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putWalledBoxRoomCarvers(count);
        }
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putRoundRoomCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putRoundRoomCarvers(count);
        }
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. This also ensures walls will be placed around the room, only allowing corridors and small cave openings to
     * pass. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledRoundRoomCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putWalledRoundRoomCarvers(count);
        }
    }

    /**
     * This generates a new map by stretching a 32x32x(multiple of 8) grid of potential rooms to fit the width, height,
     * and depth passed to the constructor, randomly expanding columns and rows before contracting the whole to fit
     * perfectly. This uses the Moore Curve, a space-filling curve that loops around on itself, to guarantee that the
     * rooms will always have a long path through the dungeon, going up and down as well as north/south/east/west, that,
     * if followed completely, will take you back to your starting room. Some small branches are possible, and large
     * rooms may merge with other rooms nearby. This uses MixedGenerator.
     * @see MixedGenerator
     * @return a char[][][] where the outermost array is layers, then inside that are x and y in order (z x y)
     */
    public char[][][] generate()
    {
        char[][][] dungeon = new char[depth][][];
        Region[] floors = new Region[depth];
        int dlimit = (height + width) / 3;
        for (int i = 0; i < depth; i++) {
            dungeon[i] = mix[i].generate();
            floors[i] = new Region(dungeon[i], '.');
            floors[i].size();
        }
        Region near = new Region(width, height), nearAbove = new Region(width, height);
        //using actual dungeon space per layer, not row/column 3D grid space
        ObjectList<ObjectOrderedSet<Coord>> ups = new ObjectList<>(depth),
                downs = new ObjectList<>(depth);
        for (int i = 0; i < depth; i++) {
            ups.add(new ObjectOrderedSet<>(40));
            downs.add(new ObjectOrderedSet<>(40));
            ObjectOrderedSet<Coord> above = new ObjectOrderedSet<>();
            if (i > 0) {
                if(linksDown.get(i - 1).size() == 0)
                    continue;
                above.addAll(linksDown.get(i - 1));
                Coord higher = above.random(random);
                while(above.size() > 0)
                {
                    nearAbove.empty().insert(columns[higher.x], rows[higher.y]).flood(floors[i - 1], dlimit);
                    near.empty().insert(columns[higher.x], rows[higher.y]).flood(floors[i], dlimit).and(nearAbove);
                    Coord subLink = near.singleRandom(random);
                    ups.get(i).add(subLink);
                    downs.get(i-1).add(subLink);
                    for(Coord abv : linksDown.get(i-1))
                    {
                        if(nearAbove.contains(columns[abv.x], rows[abv.y]))
                            above.remove(abv);
                    }
                    if(above.isEmpty())
                        break;
                    higher = above.random(random);
                }
            }
        }

        for (int i = 0; i < depth; i++) {
            ObjectIntOrderedMap<Coord> used = new ObjectIntOrderedMap<>(128);
            for(Coord up : ups.get(i))
            {
                int count = used.getOrDefault(up, 10000);
                if(count != 10000 && count > 1)
                    continue;
                dungeon[i][up.x][up.y] = '<';

                used.put(up, (count == 10000) ? 1 : count + 1);
            }
            used.clear();
            for(Coord down : downs.get(i))
            {
                int count = used.getOrDefault(down, 10000);
                if(count != 10000 && count > 1)
                    continue;
                dungeon[i][down.x][down.y] = '>';

                used.put(down, (count == 10000) ? 1 : count + 1);
            }
        }
        return dungeon;
    }

    /**
     * Gets an array (length equals depth) of 2D int arrays representing the environments for levels.
     * @return an array of 2D int arrays, where each 2D array is a level's environment
     */
    public int[][][] getEnvironments()
    {
        int[][][] env = new int[depth][][];
        for (int i = 0; i < depth; i++) {
            env[i] = mix[i].getEnvironment();
        }
        return env;
    }

    /**
     * Gets a 2D int array representing the environment for the requested level.
     * @param level the level to get from the generated dungeon; will be clamped between 0 and depth - 1
     * @return a 2D int array representing the requested level's environment
     */
    public int[][] getEnvironment(int level)
    {
        return mix[Math.max(0, Math.min(depth - 1, level))].getEnvironment();
    }
}
