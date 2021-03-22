package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.TrigTools;

public class PoissonDisk {
    private static final float inverseRootTwo = 1f / (float)Math.sqrt(2f);


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
    public static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> sampleCircle(Coord center, float radius, float minimumDistance,
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
    public static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> sampleCircle(Coord center, float radius, float minimumDistance,
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
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @return an ObjectList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> sampleRectangle(Coord minPosition, Coord maxPosition, float minimumDistance,
                                                    int maxX, int maxY)
    {
        return sampleRectangle(minPosition, maxPosition, minimumDistance, maxX, maxY, 10, new LaserRandom());
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
    public static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> sampleRectangle(Coord minPosition, Coord maxPosition, float minimumDistance,
                                                    int maxX, int maxY, int pointsPerIteration, EnhancedRandom rng)
    {
        return sample(minPosition, maxPosition, 0f, minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }


    public static ObjectOrderedSet<Coord> sampleMap(char[][] map,
                                                    float minimumDistance, EnhancedRandom rng, char... blocking)
    {
        return sampleMap(Coord.get(1, 1), Coord.get(map.length - 2, map[0].length - 2),
                map, minimumDistance, rng, blocking);
    }

    public static ObjectOrderedSet<Coord> sampleMap(Coord minPosition, Coord maxPosition, char[][] map,
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
        ObjectOrderedSet<Coord> points = new ObjectOrderedSet<>(128);

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

                float newX = radius * TrigTools.sin_(angle);
                float newY = radius * TrigTools.cos_(angle);
                Coord q = point.translateCapped(Math.round(newX), Math.round(newY), width, height);
                int frustration = 0;
                while(restricted && !valid.contains(q) && frustration < 8)
                {
                    angle = rng.nextFloat();
                    newX = radius * TrigTools.sin_(angle);
                    newY = radius * TrigTools.cos_(angle);
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
    protected static ObjectObjectOrderedMap<Coord, ObjectList<Coord>> sample(Coord minPos, Coord maxPos,
                                                                                   float maxSampleRadius, float radius,
                                                                                   int xBound, int yBound,
                                                                                   int pointsPerTry, EnhancedRandom random) {
        radius = Math.max(1.0001f, radius);
        maxSampleRadius *= maxSampleRadius;
        final float radius2 = radius * radius;
        final float iCellSize = 1f / (radius * inverseRootTwo);
        final float ik = 1f / pointsPerTry;
        final float width = maxPos.x - minPos.x, height = maxPos.y - minPos.y;
        final Coord gridCenter = minPos.average(maxPos);
        final int gridWidth = Math.min((int) Math.ceil(width * iCellSize), xBound);
        final int gridHeight = Math.min((int) Math.ceil(height * iCellSize), yBound);
        final float[][] gridX = new float[gridWidth][gridHeight];
        final float[][] gridY = new float[gridWidth][gridHeight];
        final FloatList qx = new FloatList(false, gridWidth + gridHeight);
        final FloatList qy = new FloatList(false, gridWidth + gridHeight);
        final ObjectObjectOrderedMap<Coord, ObjectList<Coord>> graph = new ObjectObjectOrderedMap<>(8 + (int) (gridWidth * gridHeight * iCellSize));
        // Pick the first sample.
        graph.put(sample(width * 0.5f, height * 0.5f, iCellSize, qx, qy, gridX, gridY), new ObjectList<>(4));

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
                final float x = px + radius * TrigTools.cos_(seed);
                final float y = py + radius * TrigTools.sin_(seed);
                seed += ik;

                // Accept candidates that are inside the allowed extent
                // and farther than 2 * radius to all existing samples.
                if (x >= 0 && x < width && y >= 0 && y < height && far(x, y, iCellSize, radius2,
                        gridCenter, maxSampleRadius, gridX, gridY)) {
                    final Coord sam = sample(x, y, iCellSize, qx, qy, gridX, gridY);
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
    private static boolean far(float x, float y, float iCellSize, float radius2, Coord gridCenter, float maxSampleRadius, float[][] gridX, float[][] gridY){
        if(maxSampleRadius != 0f && gridCenter.distanceSq(x, y) > maxSampleRadius) return false;
        final int i = (int)(x * iCellSize);
        final int j = (int)(y * iCellSize);
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
    private static Coord sample(float x, float y, float iCellSize, FloatList qx, FloatList qy, float[][] gridX, float[][] gridY){
        final int gx = (int)(x * iCellSize), gy = (int)(y * iCellSize);
        gridX[gx][gy] = x;
        gridY[gx][gy] = y;
        qx.add(x);
        qy.add(y);
        return Coord.get((int)x, (int)y);
    }
}
