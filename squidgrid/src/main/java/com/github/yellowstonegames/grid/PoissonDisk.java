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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.LaserRandom;
import com.github.tommyettinger.digital.TrigTools;

public class PoissonDisk {
    private static final float inverseRootTwo = (float)Math.sqrt(0.5f);


    private PoissonDisk() {
    }

    /**
     * Get a list of Coords, each randomly positioned around the given center out to the given radius (measured with
     * Euclidean distance, so a true circle), but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param center the center of the circle to spray Coords into
     * @param radius the radius of the circle to spray Coords into
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @return an ObjectList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static CoordObjectOrderedMap<ObjectList<Coord>> sampleCircle(Coord center, float radius, float minimumDistance,
                                                 int maxX, int maxY)
    {
        return sampleCircle(center, radius, minimumDistance, maxX, maxY, 10, new LaserRandom());
    }

    /**
     * Get a list of Coords, each randomly positioned around the given center out to the given radius (measured with
     * Euclidean distance, so a true circle), but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param center the center of the circle to spray Coords into
     * @param radius the radius of the circle to spray Coords into
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @param pointsPerIteration with small radii, this can be around 5; with larger ones, 30 is reasonable
     * @param rng an IRNG to use for all random sampling.
     * @return an ObjectList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static CoordObjectOrderedMap<ObjectList<Coord>> sampleCircle(Coord center, float radius, float minimumDistance,
                                                 int maxX, int maxY, int pointsPerIteration, EnhancedRandom rng)
    {
        int radius2 = Math.round(radius);
        return sample(center.translate(-radius2, -radius2), center.translate(radius2, radius2), radius, minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Coord with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Coord with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @return an ObjectList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static CoordObjectOrderedMap<ObjectList<Coord>> sampleRectangle(Coord minPosition, Coord maxPosition, float minimumDistance)
    {
        return sampleRectangle(minPosition, maxPosition, minimumDistance, maxPosition.x + 1, maxPosition.y + 1, 10, new LaserRandom());
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Coord with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Coord with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param pointsPerIteration with small areas, this can be around 5; with larger ones, 30 is reasonable
     * @param rng an IRNG to use for all random sampling.
     * @return an ObjectList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static CoordObjectOrderedMap<ObjectList<Coord>> sampleRectangle(
            Coord minPosition, Coord maxPosition, float minimumDistance, int pointsPerIteration, EnhancedRandom rng)
    {
        return sample(minPosition, maxPosition, 0f, minimumDistance, maxPosition.x + 1, maxPosition.y + 1, pointsPerIteration, rng);
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Coord with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Coord with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @param pointsPerIteration with small areas, this can be around 5; with larger ones, 30 is reasonable
     * @param rng an IRNG to use for all random sampling.
     * @return an ObjectList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static CoordObjectOrderedMap<ObjectList<Coord>> sampleRectangle(Coord minPosition, Coord maxPosition, float minimumDistance,
                                                    int maxX, int maxY, int pointsPerIteration, EnhancedRandom rng)
    {
        return sample(minPosition, maxPosition, 0f, minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }


    public static CoordOrderedSet sampleMap(char[][] map,
                                                    float minimumDistance, EnhancedRandom rng, char... blocking)
    {
        return sampleMap(Coord.get(1, 1), Coord.get(map.length - 2, map[0].length - 2),
                map, minimumDistance, rng, blocking);
    }

    public static CoordOrderedSet sampleMap(Coord minPosition, Coord maxPosition, char[][] map,
                                                    float minimumDistance, EnhancedRandom rng, char... blocking) {
        int width = map.length;
        int height = map[0].length;
        boolean restricted = (blocking != null && blocking.length > 0);
        Coord dimensions = maxPosition.subtract(minPosition);
        float cellSize = Math.max(minimumDistance * inverseRootTwo, 1f);
        float minimumDistance2 = minimumDistance * minimumDistance;
        int gridWidth = (int) (dimensions.x / cellSize) + 1;
        int gridHeight = (int) (dimensions.y / cellSize) + 1;
        Coord[][] grid = new Coord[gridWidth][gridHeight];
        ObjectList<Coord> activePoints = new ObjectList<>();
        CoordOrderedSet points = new CoordOrderedSet(128);

        //add first point
        Region valid = new Region(map, blocking).notAnd(new Region(width, height).insertRectangle(minPosition.x,
                minPosition.y, maxPosition.x - minPosition.x + 1, maxPosition.y - minPosition.y + 1));

        Coord p = valid.singleRandom(rng);
        if (p == null)
            return points;
        Coord index = p.subtract(minPosition).divide(cellSize);

        grid[index.x][index.y] = p;

        activePoints.add(p);
        points.add(p);

        //end add first point

        while (activePoints.size() != 0) {
            int listIndex = rng.nextInt(activePoints.size());

            Coord point = activePoints.get(listIndex);
            boolean found = false;

            for (int k = 0; k < 20; k++) {
                //add next point
                //get random point around
                float d = rng.nextFloat();
                float radius = minimumDistance + minimumDistance * d;
                float angle = rng.nextFloat();;

                float newX = radius * TrigTools.sinTurns(angle);
                float newY = radius * TrigTools.cosTurns(angle);
                Coord q = point.translateCapped(Math.round(newX), Math.round(newY), width, height);
                int frustration = 0;
                while(restricted && !valid.contains(q) && frustration < 8)
                {
                    angle = rng.nextFloat();
                    newX = radius * TrigTools.sinTurns(angle);
                    newY = radius * TrigTools.cosTurns(angle);
                    q = point.translateCapped(Math.round(newX), Math.round(newY), width, height);
                    frustration++;
                }

                //end get random point around

                if (q.x >= minPosition.x && q.x <= maxPosition.x &&
                        q.y >= minPosition.y && q.y <= maxPosition.y) {
                    Coord qIndex = q.subtract(minPosition).divide((int) Math.ceil(cellSize));
                    boolean tooClose = false;

                    for (int i = Math.max(0, qIndex.x - 2); i < Math.min(gridWidth, qIndex.x + 3) && !tooClose; i++) {
                        for (int j = Math.max(0, qIndex.y - 2); j < Math.min(gridHeight, qIndex.y + 3); j++) {
                            if (grid[i][j] != null && grid[i][j].distanceSq(q) < minimumDistance2) {
                                tooClose = true;
                                break;
                            }
                        }
                    }
                    if (!tooClose) {
                        found = true;
                        activePoints.add(q);
                        if(!restricted || valid.contains(q))
                            points.add(q);
                        grid[qIndex.x][qIndex.y] = q;
                    }
                }
                //end add next point
            }

            if (!found)
                activePoints.remove(listIndex);
        }
        return points;
    }
    protected static CoordObjectOrderedMap<ObjectList<Coord>> sample(Coord minPos, Coord maxPos,
                                                                                   float maxSampleRadius, float radius,
                                                                                   int xBound, int yBound,
                                                                                   int pointsPerTry, EnhancedRandom random) {
        radius = Math.max(1.0001f, radius);
        maxSampleRadius *= maxSampleRadius;
        final float radius2 = radius * radius;
        final float iCellSize = 1f / (radius * inverseRootTwo);
        final float ik = 1f / pointsPerTry;
        final float width = maxPos.x - minPos.x + 1, height = maxPos.y - minPos.y + 1;
        final Coord gridCenter = minPos.average(maxPos);
        final int gridWidth = Math.min((int) Math.ceil(width * iCellSize), xBound);
        final int gridHeight = Math.min((int) Math.ceil(height * iCellSize), yBound);
        final float[][] gridX = new float[gridWidth][gridHeight];
        final float[][] gridY = new float[gridWidth][gridHeight];
        final FloatList qx = new FloatList(false, gridWidth + gridHeight);
        final FloatList qy = new FloatList(false, gridWidth + gridHeight);
        final CoordObjectOrderedMap<ObjectList<Coord>> graph = new CoordObjectOrderedMap<>(8 + (int) (gridWidth * gridHeight * iCellSize));
        // Pick the first sample.
        graph.put(sample(width * 0.5f, height * 0.5f, iCellSize, qx, qy, gridX, gridY, minPos), new ObjectList<>(4));

        // Pick a random existing sample from the queue.
        PICKING:
        while (qx.notEmpty()) {
            final int i = random.nextInt(qx.size());
            final float px = qx.get(i);
            final float py = qy.get(i);
            final Coord parent = Coord.get((int) px, (int) py);
            float seed = random.nextFloat();
            // Make a new candidate.
            for (int j = 0; j < pointsPerTry; j++) {
                final float x = px + radius * TrigTools.cosTurns(seed);
                final float y = py + radius * TrigTools.sinTurns(seed);
                seed += ik;

                // Accept candidates that are inside the allowed extent
                // and farther than 2 * radius to all existing samples.
                if (x >= minPos.x && x < maxPos.x + 0.99999994f && y >= minPos.y && y < maxPos.y + 0.99999994f && far(x, y, iCellSize, radius2,
                        gridCenter, maxSampleRadius, gridX, gridY, minPos)) {
                    final Coord sam = sample(x, y, iCellSize, qx, qy, gridX, gridY, minPos);
                    graph.get(parent).add(sam);
                    graph.put(sam, new ObjectList<>(4));
                    continue PICKING;
                }
            }

            // If none of k candidates were accepted, remove it from the queue.
            qx.removeAt(i);
            qy.removeAt(i);
        }
        return graph;
    }
    private static boolean far(float x, float y, float iCellSize, float radius2, Coord gridCenter, float maxSampleRadius, float[][] gridX, float[][] gridY, Coord minPos){
        if(maxSampleRadius != 0f && gridCenter.distanceSq(x, y) > maxSampleRadius) return false;
        final int i = (int)((x - minPos.x) * iCellSize);
        final int j = (int)((y - minPos.y) * iCellSize);
        final int gridWidth = gridX.length;
        final int i0 = Math.max(i - 2, 0);
        final int j0 = Math.max(j - 2, 0);
        final int i1 = Math.min(i + 3, gridWidth);
        final int j1 = Math.min(j + 3, gridX[0].length);
        for (int xx = i0; xx < i1; xx++) {
            for (int yy = j0; yy < j1; yy++) {
                float dx = gridX[xx][yy];
                if(dx >= 0){
                    dx -= x;
                    final float dy = gridY[xx][yy] - y;
                    dx = dx * dx + dy * dy;
                    if(dx < radius2) return false;
                }
            }
        }
        return true;
    }
    private static Coord sample(float x, float y, float invCellSize, FloatList qx, FloatList qy, float[][] gridX, float[][] gridY, Coord minPos){
        final int gx = (int)((x - minPos.x) * invCellSize), gy = (int)((y - minPos.y) * invCellSize);
        gridX[gx][gy] = x;
        gridY[gx][gy] = y;
        qx.add(x);
        qy.add(y);
        return Coord.get((int)x, (int)y);
    }
}
