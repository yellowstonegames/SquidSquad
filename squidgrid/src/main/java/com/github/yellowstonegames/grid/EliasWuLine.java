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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.ObjectList;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Contains methods to draw anti-aliased lines based on floating-point coordinates.
 * This isn't accurately drawing lines, because it has no way of determining if a line should be blocked by an obstacle.
 * This means every {@link #isReachable(Coord, Coord, float[][])} method returns false. This is most useful for
 * calculating a "soft" light map along a beam with anti-aliased edges.
 * <br>
 * Because of the way this line is calculated, endpoints may be swapped and
 * therefore the list may not be in start-to-end order.
 * <br>
 * <a href="http://freespace.virgin.net/hugo.elias/graphics/x_wuline.htm">Based on work by Hugo Elias</a>
 * which is in turn based on work by Wu.
 * @author <a href="http://squidpony.com">Eben Howard</a> - howard@squidpony.com
 */
public class EliasWuLine implements LineDrawer {
    
    private final ObjectList<Coord> path;
    private float[][] lightMap;
    private int width = 1, height = 1;
    private transient float threshold = 0;

    public EliasWuLine() {
        this(64, 64);
    }

    public EliasWuLine(int width, int height) {
        path = new ObjectList<>();
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        lightMap = new float[this.width][this.height];
    }

    public void setSize(int width, int height) {
        width = Math.max(1, width);
        height = Math.max(1, height);
        if(this.width != (this.width = width) || this.height != (this.height = height)) {
            lightMap = new float[this.width][this.height];
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        width = Math.max(1, width);
        if(this.width != (this.width = width)) {
            lightMap = new float[this.width][this.height];
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        height = Math.max(1, height);
        if(this.height != (this.height = height)) {
            for (int x = 0; x < width; x++) {
                lightMap[x] = new float[height];
            }
        }
    }

    public float[][] lightMap(float startx, float starty, float endx, float endy) {
        floatLine(startx, starty, endx, endy);
        return lightMap;
    }

    /**
     * Gets the line between the two points. Uses the previously set brightness threshold, or 0 if it has not been set.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return
     */
    public ObjectList<Coord> floatLine(float startx, float starty, float endx, float endy) {
        return floatLine(startx, starty, endx, endy, path, Integer.MAX_VALUE);
    }

    /**
     * Gets the line between the two points. Uses the previously set brightness threshold, or 0 if it has not been set.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return
     */
    public ObjectList<Coord> floatLine(float startx, float starty, float endx, float endy,
                                       ObjectList<Coord> buffer, int maxLength) {
        buffer.clear();
        ArrayTools.fill(lightMap, 0f);
        runLine(startx, starty, endx, endy, buffer, maxLength);
        return buffer;
    }

    /**
     * Gets the line between the two points.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @param brightnessThreshold between 0.0 (default) and 1.0; only Coords with higher brightness will be included
     * @return
     */
    public ObjectList<Coord> floatLine(float startx, float starty, float endx, float endy,
                                       ObjectList<Coord> buffer, int maxLength, float brightnessThreshold) {
        threshold = brightnessThreshold;
        buffer.clear();
        ArrayTools.fill(lightMap, 0f);
        runLine(startx, starty, endx, endy, buffer, maxLength);
        return buffer;
    }
    @Override
    public ObjectList<Coord> drawLine(Coord start, Coord end) {
        return drawLine(start.x, start.y, end.x, end.y);
    }
    public ObjectList<Coord> drawLine(Coord start, Coord end, float brightnessThreshold) {
        return floatLine(start.x, start.y, end.x, end.y, path, Integer.MAX_VALUE, brightnessThreshold);
    }

    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY) {
        return floatLine(startX, startY, targetX, targetY);
    }

    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength) {
        return floatLine(startX, startY, targetX, targetY, path, Integer.MAX_VALUE);
    }

    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, ObjectList<Coord> buffer) {
        return floatLine(startX, startY, targetX, targetY, buffer, Integer.MAX_VALUE);
    }

    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength, ObjectList<Coord> buffer) {
        return floatLine(startX, startY, targetX, targetY, buffer, maxLength);
    }

    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength, ObjectList<Coord> buffer, float brightnessThreshold) {
        return floatLine(startX, startY, targetX, targetY, buffer, maxLength, brightnessThreshold);
    }

    public ObjectList<Coord> getLastPath()
    {
        return path;
    }

    /**
     * Marks the location as having the visibility given.
     *
     * @param x
     * @param y
     * @param c
     */
    private void mark(float x, float y, float c, ObjectList<Coord> buffer, int maxLength) {
        //check bounds overflow from antialiasing
        if (buffer.size() < maxLength && x > -1 && x < width && y > -1 && y < height && c > threshold) {
            buffer.add(Coord.get((int) x, (int) y));
            lightMap[(int) x][(int) y] = c;
        }
    }

    private float frac(float x) {
        return x - (int) (x);
    }

    private float invfrac(float x) {
        return 1 - x + (int) (x);
    }

    private void runLine(float startx, float starty, float endx, float endy, ObjectList<Coord> buffer, int maxLength) {
        float x1 = startx, y1 = starty, x2 = endx, y2 = endy;
        float grad, xd, yd, xgap, xend, yend, yf, brightness1, brightness2;
        int x, ix1, ix2, iy1, iy2;
        boolean shallow = false;

        xd = x2 - x1;
        yd = y2 - y1;

        if (Math.abs(xd) > Math.abs(yd)) {
            shallow = true;
        }

        if (!shallow) {
            float temp = x1;
            x1 = y1;
            y1 = temp;
            temp = x2;
            x2 = y2;
            y2 = temp;
            xd = x2 - x1;
            yd = y2 - y1;
        }
        if (x1 > x2) {
            float temp = x1;
            x1 = x2;
            x2 = temp;
            temp = y1;
            y1 = y2;
            y2 = temp;
            xd = x2 - x1;
            yd = y2 - y1;
        }

        grad = yd / xd;

        //add the first end point
        xend = (int)(x1 + .5f);
        yend = y1 + grad * (xend - x1);

        xgap = invfrac(x1 + .5f);

        ix1 = (int) xend;
        iy1 = (int) yend;

        brightness1 = invfrac(yend) * xgap;
        brightness2 = frac(yend) * xgap;

        if (shallow) {
            mark(ix1, iy1, brightness1, buffer, maxLength);
            mark(ix1, iy1 + 1, brightness2, buffer, maxLength);
        } else {
            mark(iy1, ix1, brightness1, buffer, maxLength);
            mark(iy1 + 1, ix1, brightness2, buffer, maxLength);
        }

        yf = yend + grad;

        //add the second end point
        xend = (int) (x2 + .5f);
        yend = y2 + grad * (xend - x2);

        xgap = invfrac(x2 - .5f);

        ix2 = (int) xend;
        iy2 = (int) yend;

        //add the in-between points
        for (x = ix1 + 1; x < ix2; x++) {
            brightness1 = invfrac(yf);
            brightness2 = frac(yf);

            if (shallow) {
                mark(x, yf, brightness1, buffer, maxLength);
                mark(x, yf + 1, brightness2, buffer, maxLength);
            } else {
                mark(yf, x, brightness1, buffer, maxLength);
                mark(yf + 1, x, brightness2, buffer, maxLength);
            }

            yf += grad;
        }
        
        brightness1 = invfrac(yend) * xgap;
        brightness2 = frac(yend) * xgap;

        if (shallow) {
            mark(ix2, iy2, brightness1, buffer, maxLength);
            mark(ix2, iy2 + 1, brightness2, buffer, maxLength);
        } else {
            mark(iy2, ix2, brightness1, buffer, maxLength);
            mark(iy2 + 1, ix2, brightness2, buffer, maxLength);
        }

    }

    @Override
    public ObjectList<Coord> getLastLine() {
        return path;
    }

    @Override
    public boolean isReachable(@NonNull Coord start, @NonNull Coord target, float[][] resistanceMap, ObjectList<Coord> buffer) {
        return false;
    }

    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY, float[][] resistanceMap, ObjectList<Coord> buffer) {
        return false;
    }

    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength, float[][] resistanceMap, ObjectList<Coord> buffer) {
        return false;
    }

    @Override
    public boolean isReachable(@NonNull Coord start, @NonNull Coord target, float[][] resistanceMap) {
        return false;
    }

    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY, float[][] resistanceMap) {
        return false;
    }

    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength, float[][] resistanceMap) {
        return false;
    }

    @Override
    public Coord[] drawLineArray(Coord a, Coord b) {
        return drawLine(a, b).toArray(new Coord[0]);
    }

    @Override
    public Coord[] drawLineArray(int startX, int startY, int targetX, int targetY) {
        return drawLine(startX, startY, targetX, targetY).toArray(new Coord[0]);
    }

    @Override
    public Coord[] drawLineArray(int startX, int startY, int targetX, int targetY, int maxLength) {
        return drawLine(startX, startY, targetX, targetY, maxLength).toArray(new Coord[0]);
    }
}
