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

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.yellowstonegames.core.MathTools;
import com.github.tommyettinger.digital.TrigTools;

import java.util.List;

/**
 * Basic radius strategy implementations likely to be used for roguelikes.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum Radius {

    /**
     * In an unobstructed area the FOV would be a square.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with no additional cost for diagonal movement.
     */
    SQUARE,
    /**
     * In an unobstructed area the FOV would be a diamond.
     *
     * This is the shape that would represent movement radius in a 4-way
     * movement scheme.
     */
    DIAMOND,
    /**
     * In an unobstructed area the FOV would be a circle.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with all movement cost the same based on distance from
     * the source
     */
    CIRCLE;

    public float radius(Coord start, Coord end) {
        float dx = start.x - end.x;
        float dy = start.y - end.y;
        return radius(dx, dy);
    }
    public float radius(Coord end) {
        return radius(end.x, end.y);
    }

    public float radius(float startx, float starty, float endx, float endy) {
        float dx = startx - endx;
        float dy = starty - endy;
        return radius(dx, dy);
    }

    public float radius(float dx, float dy) {
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        switch (this) {
            case SQUARE:
                return Math.max(dx, dy);//radius is longest axial distance
            case CIRCLE:
                return (float) Math.sqrt(dx * dx + dy * dy);//standard spherical radius
            default:
                return dx + dy;//radius is the manhattan distance
        }
    }

    public Coord onUnitShape(float distance, EnhancedRandom rng) {
        int x, y;
        switch (this) {
            case SQUARE:
                x = rng.nextInt((int) -distance, (int) distance + 1);
                y = rng.nextInt((int) -distance, (int) distance + 1);
                break;
            case DIAMOND:
                x = rng.nextInt((int) -distance, (int) distance + 1);
                y = rng.nextInt((int) -distance, (int) distance + 1);
                if (radius(x, y) > distance) {
                    if (x > 0) {
                        if (y > 0) {
                            x = (int) (distance - x);
                            y = (int) (distance - y);
                        } else {
                            x = (int) (distance - x);
                            y = (int) (-distance - y);
                        }
                    } else {
                        if (y > 0) {
                            x = (int) (-distance - x);
                            y = (int) (distance - y);
                        } else {
                            x = (int) (-distance - x);
                            y = (int) (-distance - y);
                        }
                    }
                }
                break;
            default: // CIRCLE
                float radius = distance * (float) Math.sqrt(rng.nextFloat());
                float theta = rng.nextFloat();
                x = Math.round(TrigTools.cosTurns(theta) * radius);
                y = Math.round(TrigTools.sinTurns(theta) * radius);
        }

        return Coord.get(x, y);
    }

    public float area(float radiusLength)
    {
        switch (this) {
            case SQUARE:
                return (radiusLength * 2 + 1) * (radiusLength * 2 + 1);
            case DIAMOND:
                return radiusLength * (radiusLength + 1) * 2 + 1;
            default:
                return TrigTools.PI * radiusLength * radiusLength + 1;
        }
    }

    private int clamp(int n, int min, int max)
    {
        return Math.min(Math.max(min, n), max - 1);
    }

    public CoordOrderedSet perimeter(Coord center, int radiusLength, boolean surpassEdges, int width, int height)
    {
        CoordOrderedSet rim = new CoordOrderedSet(4 * radiusLength);
        if(!surpassEdges && (center.x < 0 || center.x >= width || center.y < 0 || center.y > height))
            return rim;
        if(radiusLength < 1) {
            rim.add(center);
            return rim;
        }
        switch (this) {
            case SQUARE: {
                for (int i = center.x - radiusLength; i <= center.x + radiusLength; i++) {
                    int x = i;
                    if(!surpassEdges) x = clamp(i, 0, width);
                    rim.add(Coord.get(x, clamp(center.y - radiusLength, 0, height)));
                    rim.add(Coord.get(x, clamp(center.y + radiusLength, 0, height)));
                }
                for (int j = center.y - radiusLength; j <= center.y + radiusLength; j++) {
                    int y = j;
                    if(!surpassEdges) y = clamp(j, 0, height);
                    rim.add(Coord.get(clamp(center.x - radiusLength, 0, height), y));
                    rim.add(Coord.get(clamp(center.x + radiusLength, 0, height), y));
                }
            }
            break;
            case DIAMOND: {
                int xUp = center.x + radiusLength, xDown = center.x - radiusLength,
                        yUp = center.y + radiusLength, yDown = center.y - radiusLength;
                if(!surpassEdges) {
                    xDown = clamp(xDown, 0, width);
                    xUp = clamp(xUp, 0, width);
                    yDown = clamp(yDown, 0, height);
                    yUp = clamp(yUp, 0, height);
                }

                rim.add(Coord.get(xDown, center.y));
                rim.add(Coord.get(xUp, center.y));
                rim.add(Coord.get(center.x, yDown));
                rim.add(Coord.get(center.x, yUp));

                for (int i = xDown + 1, c = 1; i < center.x; i++, c++) {
                    int x = i;
                    if(!surpassEdges) x = clamp(i, 0, width);
                    rim.add(Coord.get(x, clamp(center.y - c, 0, height)));
                    rim.add(Coord.get(x, clamp(center.y + c, 0, height)));
                }
                for (int i = center.x + 1, c = 1; i < center.x + radiusLength; i++, c++) {
                    int x = i;
                    if(!surpassEdges) x = clamp(i, 0, width);
                    rim.add(Coord.get(x, clamp(center.y + radiusLength - c, 0, height)));
                    rim.add(Coord.get(x, clamp(center.y - radiusLength + c, 0, height)));
                }
            }
            break;
            default: {
                float theta;
                int x, y, denom = 1;
                boolean anySuccesses;
                while(denom <= 256) {
                    anySuccesses = false;
                    for (int i = 1; i <= denom; i+=2)
                    {
                        theta = i / denom;
                        x = (int) (TrigTools.cosTurns(theta) * (radiusLength + 0.25)) + center.x;
                        y = (int) (TrigTools.sinTurns(theta) * (radiusLength + 0.25)) + center.y;
                        
                        if (!surpassEdges) {
                            x = clamp(x, 0, width);
                            y = clamp(y, 0, height);
                        }
                        Coord p = Coord.get(x, y);
                        boolean test = !rim.contains(p);

                        rim.add(p);
                        anySuccesses = test || anySuccesses;
                    }
                    if(!anySuccesses)
                        break;
                    denom *= 2;
                }

            }
        }
        return rim;
    }
    public Coord extend(Coord center, Coord middle, int radiusLength, boolean surpassEdges, int width, int height)
    {
        if(!surpassEdges && (center.x < 0 || center.x >= width || center.y < 0 || center.y > height ||
                middle.x < 0 || middle.x >= width || middle.y < 0 || middle.y > height))
            return Coord.get(0, 0);
        if(radiusLength < 1) {
            return center;
        }
        float theta = TrigTools.atan2Turns(middle.y - center.y, middle.x - center.x),
                cosTheta = TrigTools.cosTurns(theta), sinTheta = TrigTools.sinTurns(theta);

        Coord end = Coord.get(middle.x, middle.y);
        switch (this) {
            case SQUARE:
            case DIAMOND:
            {
                int rad2 = 0;
                if(surpassEdges)
                {
                    while (radius(center.x, center.y, end.x, end.y) < radiusLength) {
                        rad2++;
                        end = Coord.get((int) Math.round(cosTheta * rad2) + center.x
                                , (int) Math.round(sinTheta * rad2) + center.y);
                    }
                }
                else {
                    while (radius(center.x, center.y, end.x, end.y) < radiusLength) {
                        rad2++;
                        end = Coord.get(clamp((int) Math.round(cosTheta * rad2) + center.x, 0, width)
                                      , clamp((int) Math.round(sinTheta * rad2) + center.y, 0, height));
                        if (end.x == 0 || end.x == width - 1 || end.y == 0 || end.y == height - 1)
                            return end;
                    }
                }

                return end;
            }
            default:
            {
                end = Coord.get(clamp( (int) Math.round(cosTheta * radiusLength) + center.x, 0, width)
                        , clamp( (int) Math.round(sinTheta * radiusLength) + center.y, 0, height));
                if(!surpassEdges) {
                    long edgeLength;
//                    if (end.x == 0 || end.x == width - 1 || end.y == 0 || end.y == height - 1)
                    if (end.x < 0)
                    {
                        // wow, we lucked out here. the only situation where cos(angle) is 0 is if the angle aims
                        // straight up or down, and then x cannot be < 0 or >= width.
                        edgeLength = Math.round((-center.x) / cosTheta);
                        end = end.setY(clamp((int) Math.round(sinTheta * edgeLength) + center.y, 0, height));
                    }
                    else if(end.x >= width)
                    {
                        // wow, we lucked out here. the only situation where cos(angle) is 0 is if the angle aims
                        // straight up or down, and then x cannot be < 0 or >= width.
                        edgeLength = Math.round((width - 1 - center.x) / cosTheta);
                        end = end.setY(clamp((int) Math.round(sinTheta * edgeLength) + center.y, 0, height));
                    }

                    if (end.y < 0)
                    {
                        // wow, we lucked out here. the only situation where sin(angle) is 0 is if the angle aims
                        // straight left or right, and then y cannot be < 0 or >= height.
                        edgeLength = Math.round((-center.y) / sinTheta);
                        end = end.setX(clamp((int) Math.round(cosTheta * edgeLength) + center.x, 0, width));
                    }
                    else if(end.y >= height)
                    {
                        // wow, we lucked out here. the only situation where sin(angle) is 0 is if the angle aims
                        // straight left or right, and then y cannot be < 0 or >= height.
                        edgeLength = Math.round((height - 1 - center.y) / sinTheta);
                        end = end.setX(clamp((int) Math.round(cosTheta * edgeLength) + center.x, 0, width));
                    }
                }
                return end;
            }
        }
    }

    public boolean inRange(int startx, int starty, int endx, int endy, int minRange, int maxRange)
    {
        float dist = radius(startx, starty, endx, endy);
        return dist >= minRange - 0.001 && dist <= maxRange + 0.001;
    }

    public int roughDistance(int xPos, int yPos) {
        int x = Math.abs(xPos), y = Math.abs(yPos);
        switch (this) {
            case CIRCLE:
            {
                if(x == y) return 3 * x;
                else if(x < y) return 3 * x + 2 * (y - x);
                else return 3 * y + 2 * (x - y);
            }
            case DIAMOND:
                return 2 * (x + y);
            default:
                return 2 * Math.max(x, y);
        }
    }

    public List<Coord> pointsInside(int centerX, int centerY, int radiusLength, boolean surpassEdges, int width, int height)
    {
        return pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, null);
    }
    public List<Coord> pointsInside(Coord center, int radiusLength, boolean surpassEdges, int width, int height)
    {
        if(center == null) return null;
        return pointsInside(center.x, center.y, radiusLength, surpassEdges, width, height, null);
    }

    public List<Coord> pointsInside(int centerX, int centerY, int radiusLength, boolean surpassEdges, int width, int height, List<Coord> buf)
    {
        List<Coord> contents = buf == null ? new ObjectList<Coord>((int)Math.ceil(area(radiusLength))) : buf;
        if(!surpassEdges && (centerX < 0 || centerX >= width || centerY < 0 || centerY >= height))
            return contents;
        if(radiusLength < 1) {
            contents.add(Coord.get(centerX, centerY));
            return contents;
        }
        switch (this) {
            case SQUARE: {
                for (int i = centerX - radiusLength; i <= centerX + radiusLength; i++) {
                    for (int j = centerY - radiusLength; j <= centerY + radiusLength; j++) {
                        if(!surpassEdges && (i < 0 || j < 0 || i >= width || j >= height))
                            continue;
                        contents.add(Coord.get(i, j));
                    }
                }
            }
            break;
            case DIAMOND: {
                for (int i = centerX - radiusLength; i <= centerX + radiusLength; i++) {
                    for (int j = centerY - radiusLength; j <= centerY + radiusLength; j++) {
                        if ((Math.abs(centerX - i) + Math.abs(centerY- j) > radiusLength) ||
                                (!surpassEdges && (i < 0 || j < 0 || i >= width || j >= height)))
                            continue;
                        contents.add(Coord.get(i, j));
                    }
                }
            }
            break;
            default: {
                float high, changedX;
                int rndX, rndY;
                for (int dx = -radiusLength; dx <= radiusLength; ++dx) {
                    changedX = dx - 0.25f * (dx >> 31 | -dx >>> 31); // project nayuki signum
                    rndX = Math.round(changedX);
                    high = (float) Math.sqrt(radiusLength * radiusLength - changedX * changedX);
                    if (surpassEdges || !(centerX + rndX < 0||
                            centerX + rndX >= width))
                        contents.add(Coord.get(centerX + rndX, centerY));
                    for (float dy = high; dy >= 0.75f; --dy) {
                        rndY = Math.round(dy - 0.25f);
                        if (surpassEdges || !(centerX + rndX < 0 || centerY + rndY < 0 ||
                                centerX + rndX >= width || centerY + rndY >= height))
                            contents.add(Coord.get(centerX + rndX, centerY + rndY));
                        if (surpassEdges || !(centerX + rndX < 0 || centerY - rndY < 0 ||
                                centerX + rndX >= width || centerY - rndY >= height))
                            contents.add(Coord.get(centerX + rndX, centerY - rndY));
                    }
                }
            }
        }
        return contents;
    }

    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center} using Chebyshev measurement (making
     * a square). Appends Coords to {@code buf} if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will not produce Coords with x or y less
     * than 0 or greater than {@code width} or {@code height}; if surpassEdges is true, then it can produce any Coords
     * in the actual radius.
     * @param centerX the center Coord x
     * @param centerY the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with negative x/y or past width/height
     * @param width the width of the area this can place Coords (exclusive, not relative to center, usually map width)
     * @param height the height of the area this can place Coords (exclusive, not relative to center, usually map height)
     * @return a new List containing the points within radiusLength of the center

     */
    public static List<Coord> inSquare(int centerX, int centerY, int radiusLength, boolean surpassEdges, int width, int height)
    {
        return SQUARE.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, null);
    }
    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center} using Manhattan measurement (making
     * a diamond). Appends Coords to {@code buf} if it is non-null, and returns either buf or a freshly-allocated List
     * of Coord. If {@code surpassEdges} is false, which is the normal usage, this will not produce Coords with x or y
     * less than 0 or greater than {@code width} or {@code height}; if surpassEdges is true, then it can produce any
     * Coords in the actual radius.
     * @param centerX the center Coord x
     * @param centerY the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with negative x/y or past width/height
     * @param width the width of the area this can place Coords (exclusive, not relative to center, usually map width)
     * @param height the height of the area this can place Coords (exclusive, not relative to center, usually map height)
     * @return a new List containing the points within radiusLength of the center
     */
    public static List<Coord> inDiamond(int centerX, int centerY, int radiusLength, boolean surpassEdges, int width, int height)
    {
        return DIAMOND.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, null);
    }
    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center} using Euclidean measurement (making
     * a circle). Appends Coords to {@code buf} if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will not produce Coords with x or y less
     * than 0 or greater than {@code width} or {@code height}; if surpassEdges is true, then it can produce any Coords
     * in the actual radius.
     * @param centerX the center Coord x
     * @param centerY the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with negative x/y or past width/height
     * @param width the width of the area this can place Coords (exclusive, not relative to center, usually map width)
     * @param height the height of the area this can place Coords (exclusive, not relative to center, usually map height)
     * @return a new List containing the points within radiusLength of the center
     */
    public static List<Coord> inCircle(int centerX, int centerY, int radiusLength, boolean surpassEdges, int width, int height)
    {
        return CIRCLE.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, null);
    }

    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center} using Chebyshev measurement (making
     * a square). Appends Coords to {@code buf} if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will not produce Coords with x or y less
     * than 0 or greater than {@code width} or {@code height}; if surpassEdges is true, then it can produce any Coords
     * in the actual radius.
     * @param centerX the center Coord x
     * @param centerY the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with negative x/y or past width/height
     * @param width the width of the area this can place Coords (exclusive, not relative to center, usually map width)
     * @param height the height of the area this can place Coords (exclusive, not relative to center, usually map height)
     * @param buf the List of Coord to append points to; may be null to create a new List
     * @return buf, after appending Coords to it, or a new List if buf was null
     */
    public static List<Coord> inSquare(int centerX, int centerY, int radiusLength, boolean surpassEdges, int width, int height, List<Coord> buf)
    {
        return SQUARE.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, buf);
    }
    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center} using Manhattan measurement (making
     * a diamond). Appends Coords to {@code buf} if it is non-null, and returns either buf or a freshly-allocated List
     * of Coord. If {@code surpassEdges} is false, which is the normal usage, this will not produce Coords with x or y
     * less than 0 or greater than {@code width} or {@code height}; if surpassEdges is true, then it can produce any
     * Coords in the actual radius.
     * @param centerX the center Coord x
     * @param centerY the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with negative x/y or past width/height
     * @param width the width of the area this can place Coords (exclusive, not relative to center, usually map width)
     * @param height the height of the area this can place Coords (exclusive, not relative to center, usually map height)
     * @param buf the List of Coord to append points to; may be null to create a new List
     * @return buf, after appending Coords to it, or a new List if buf was null
     */
    public static List<Coord> inDiamond(int centerX, int centerY, int radiusLength, boolean surpassEdges, int width, int height, List<Coord> buf)
    {
        return DIAMOND.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, buf);
    }
    /**
     * Gets a List of all Coord points within {@code radiusLength} of {@code center} using Euclidean measurement (making
     * a circle). Appends Coords to {@code buf} if it is non-null, and returns either buf or a freshly-allocated List of
     * Coord. If {@code surpassEdges} is false, which is the normal usage, this will not produce Coords with x or y less
     * than 0 or greater than {@code width} or {@code height}; if surpassEdges is true, then it can produce any Coords
     * in the actual radius.
     * @param centerX the center Coord x
     * @param centerY the center Coord x
     * @param radiusLength the inclusive distance from (centerX,centerY) for Coords to use in the List
     * @param surpassEdges usually should be false; if true, can produce Coords with negative x/y or past width/height
     * @param width the width of the area this can place Coords (exclusive, not relative to center, usually map width)
     * @param height the height of the area this can place Coords (exclusive, not relative to center, usually map height)
     * @param buf the List of Coord to append points to; may be null to create a new List
     * @return buf, after appending Coords to it, or a new List if buf was null
     */
    public static List<Coord> inCircle(int centerX, int centerY, int radiusLength, boolean surpassEdges, int width, int height, List<Coord> buf)
    {
        return CIRCLE.pointsInside(centerX, centerY, radiusLength, surpassEdges, width, height, buf);
    }

    /**
     * Given an Iterable of Coord (such as a List or Set), a distance to expand outward by (using this Radius), and the
     * bounding height and width of the map, gets a "thickened" group of Coord as a Set where each Coord in points has
     * been expanded out by an amount no greater than distance. As an example, you could call this on a line generated
     * by Bresenham, OrthoLine, or an LOS object's getLastPath() method, and expand the line into a thick "brush stroke"
     * where this Radius affects the shape of the ends. This will never produce a Coord with negative x or y, a Coord
     * with x greater than or equal to width, or a Coord with y greater than or equal to height.
     * @param distance the distance, as measured by this Radius, to expand each Coord on points up to
     * @param width the bounding width of the map (exclusive)
     * @param height the bounding height of the map (exclusive)
     * @param points an Iterable (such as a List or Set) of Coord that this will make a "thickened" version of
     * @return a Set of Coord that covers a wider area than what points covers; each Coord will be unique (it's a Set)
     */
    public CoordOrderedSet expand(int distance, int width, int height, Iterable<Coord> points)
    {
        List<Coord> around = pointsInside(Coord.get(distance, distance), distance, false, width, height);
        CoordOrderedSet expanded = new CoordOrderedSet(around.size() * 16, 0.25f);
        int tx, ty;
        for(Coord pt : points)
        {
            for(Coord ar : around)
            {
                tx = pt.x + ar.x - distance;
                ty = pt.y + ar.y - distance;
                if(tx >= 0 && tx < width && ty >= 0 && ty < height)
                    expanded.add(Coord.get(tx, ty));
            }
        }
        return expanded;
    }
    /**
     * Gets the appropriate {@link Measurement} to pass to a constructor if you already have a Radius.
     * Matches SQUARE to CHEBYSHEV, DIAMOND to MANHATTAN, and CIRCLE to EUCLIDEAN.
     * 
     * @see Measurement#matchingMeasurement(Radius) an equivalent method in Measurement
     * @see Measurement#matchingRadius() a method to do the inverse of this and get a Radius from a Measurement
     *
     * @return a {@link Measurement} that matches this; SQUARE to CHEBYSHEV, DIAMOND to MANHATTAN, etc.
     */
    public Measurement matchingMeasurement() {
        switch (this)
        {
            case SQUARE:
                return Measurement.CHEBYSHEV;
            case DIAMOND:
                return Measurement.MANHATTAN;
            default:
                return Measurement.EUCLIDEAN;
        }
    }

}
