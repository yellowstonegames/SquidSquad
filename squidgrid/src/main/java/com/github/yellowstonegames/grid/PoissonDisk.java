package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.TrigTools;

public class PoissonDisk {
    private static final float inverseRootTwo = 1f / (float)Math.sqrt(2f);
    public static ObjectOrderedSet<Coord> sampleMap(char[][] map,
                                                    float minimumDistance, LaserRandom rng, char... blocking)
    {
        return sampleMap(Coord.get(1, 1), Coord.get(map.length - 2, map[0].length - 2),
                map, minimumDistance, rng, blocking);
    }

    public static ObjectOrderedSet<Coord> sampleMap(Coord minPosition, Coord maxPosition, char[][] map,
                                              float minimumDistance, LaserRandom rng, char... blocking) {
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

}
