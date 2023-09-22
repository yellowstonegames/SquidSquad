/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.ObjectList;

/**
 * Contains methods to draw anti-aliased lines based on floating-point coordinates.
 * <br>
 * Because of the way this line is calculated, endpoints may be swapped and
 * therefore the list may not be in start-to-end order.
 * <br>
 * <a href="http://freespace.virgin.net/hugo.elias/graphics/x_wuline.htm">Based on work by Hugo Elias</a>
 * which is in turn based on work by Wu.
 * @author <a href="http://squidpony.com">Eben Howard</a> - howard@squidpony.com
 */
public class EliasWuLine {
    
    private ObjectList<Coord> path;
    private float[][] lightMap;
    private int width = -1, height = -1;
    private float threshold;

    public EliasWuLine() {
        path = new ObjectList<>();
    }

    public float[][] lightMap(float startx, float starty, float endx, float endy) {
        line(startx, starty, endx, endy);
        return lightMap;
    }

    /**
     * Gets the line between the two points.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return
     */
    public ObjectList<Coord> line(float startx, float starty, float endx, float endy) {
        threshold = 0f;
        path.clear();
        int tw = (int) (Math.max(startx, endx) + 1);
        int th = (int) (Math.max(starty, endy) + 1);
        if(width < tw || height < th)
            lightMap = new float[width = tw][height = th];
        else
            ArrayTools.fill(lightMap, 0f);
        runLine(startx, starty, endx, endy);
        return path;
    }
    /**
     * Gets the line between the two points.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @param brightnessThreshold between 0.0 (default) and 1.0; only Points with higher brightness will be included
     * @return
     */
    public ObjectList<Coord> line(float startx, float starty, float endx, float endy,
                                                float brightnessThreshold) {
        threshold = brightnessThreshold;
        path.clear();
        int tw = (int) (Math.max(startx, endx) + 1);
        int th = (int) (Math.max(starty, endy) + 1);
        if(width < tw || height < th)
            lightMap = new float[width = tw][height = th];
        else
            ArrayTools.fill(lightMap, 0f);
        runLine(startx, starty, endx, endy);
        return path;
    }
    public ObjectList<Coord> line(Coord start, Coord end) {
        return line(start.x, start.y, end.x, end.y);
    }
    public ObjectList<Coord> line(Coord start, Coord end, float brightnessThreshold) {
        return line(start.x, start.y, end.x, end.y, brightnessThreshold);
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
    private void mark(float x, float y, float c) {
        //check bounds overflow from antialiasing
        if (x > -1 && x < width && y > -1 && y < height && c > threshold) {
            path.add(Coord.get((int) x, (int) y));
            lightMap[(int) x][(int) y] = c;
        }
    }

    private float frac(float x) {
        return x - (int) (x);
    }

    private float invfrac(float x) {
        return 1 - x + (int) (x);
    }

    private void runLine(float startx, float starty, float endx, float endy) {
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
            mark(ix1, iy1, brightness1);
            mark(ix1, iy1 + 1, brightness2);
        } else {
            mark(iy1, ix1, brightness1);
            mark(iy1 + 1, ix1, brightness2);
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
                mark(x, yf, brightness1);
                mark(x, yf + 1, brightness2);
            } else {
                mark(yf, x, brightness1);
                mark(yf + 1, x, brightness2);
            }

            yf += grad;
        }
        
        brightness1 = invfrac(yend) * xgap;
        brightness2 = frac(yend) * xgap;

        if (shallow) {
            mark(ix2, iy2, brightness1);
            mark(ix2, iy2 + 1, brightness2);
        } else {
            mark(iy2, ix2, brightness1);
            mark(iy2 + 1, ix2, brightness2);
        }

    }
}
