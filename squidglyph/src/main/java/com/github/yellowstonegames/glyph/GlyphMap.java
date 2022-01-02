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

package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntLongOrderedMap;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.Coord;
import com.github.tommyettinger.textra.Font;

public class GlyphMap {
    protected int gridWidth;
    protected int gridHeight;
    public IntLongOrderedMap map;
    public int[][] backgrounds = null;
    public Font font;
    public Viewport viewport;
    /**
     * This is mostly here to help ColorLookup get RGBA8888 colors from valid descriptions, or null from invalid ones.
     * @param description a color description, as a lower-case String matching the format covered in {@link DescriptiveColor#describe(CharSequence)}
     * @return an Integer that either contains an RGBA8888 color (if description was valid) or null otherwise
     */
    public static Integer getRgba(final String description){
        if(description == null || description.length() == 0) return null;
        int oklab = DescriptiveColor.describeOklab(description);
        if(oklab == 0) {
            Color c = Colors.get(description);
            if(c == null) return null;
            return Color.rgba8888(c);
        }
        return DescriptiveColor.toRGBA8888(oklab);
    }

    /**
     * Does not set {@link #font}, you will have to set it later.
     */
    public GlyphMap(){
        map = new IntLongOrderedMap(4096);
        viewport = new StretchViewport(64, 64);
    }
    public GlyphMap(Font font){
        this(font, 64, 64);
    }
    public GlyphMap(Font font, int gridWidth, int gridHeight){
        this.font = new Font(font);
        this.font.setColorLookup(GlyphMap::getRgba);
        if(this.font.distanceField != Font.DistanceFieldType.STANDARD)
            this.font.distanceFieldCrispness *= Math.sqrt(font.cellWidth) + Math.sqrt(font.cellHeight) + 1;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        map = new IntLongOrderedMap(gridWidth * gridHeight);
        viewport = new StretchViewport(gridWidth, gridHeight);
        viewport.setScreenWidth((int) (gridWidth * font.cellWidth));
        viewport.setScreenHeight((int) (gridHeight * font.cellHeight));
        this.font.scaleTo(1f, 1f);
    }

    /**
     * Combines the up-to-15-bit non-negative ints x and y into one non-negative result int.
     * This is used to get the keys for a GlyphMap given x,y inputs. Because jdkgdxds uses
     * Fibonacci hashing to scramble keys, there's no concern here from leaving all of x in
     * the lower half of the bits and all of y in the upper half.
     * @param x an up-to-15-bit non-negative int (between 0 and 32767, inclusive)
     * @param y an up-to-15-bit non-negative int (between 0 and 32767, inclusive)
     * @return a non-negative int that combines x and y into one value
     */
    public static int fuse(final int x, final int y)
    {
        return (x & 0x7FFF) | (y << 16 & 0x7FFF0000);
    }

    /**
     * Combines the up-to-15-bit non-negative x and y components of the Coord {@code xy} into one
     * non-negative result int. This is used to get the keys for a GlyphMap given x,y inputs. Because
     * jdkgdxds uses Fibonacci hashing to scramble keys, there's no concern here from leaving all of
     * x in the lower half of the bits and all of y in the upper half.
     * @param xy a Coord with non-negative x and y
     * @return a non-negative int that combines the Coord's x and y into one value
     */
    public static int fuse(final Coord xy)
    {
        return (xy.x & 0x7FFF) | (xy.y << 16 & 0x7FFF0000);
    }

    /**
     * Given a fused x,y pair as an int (typically produced by {@link #fuse(int, int)}), this
     * gets the x component from it, as an int.
     * @param fused a fused x,y pair as an int as produced by {@link #fuse(int, int)}
     * @return the x component of fused
     */
    public static int extractX(int fused){
        return fused & 0x7FFF;
    }

    /**
     * Given a fused x,y pair as an int (typically produced by {@link #fuse(int, int)}), this
     * gets the y component from it, as an int.
     * @param fused a fused x,y pair as an int as produced by {@link #fuse(int, int)}
     * @return the y component of fused
     */
    public static int extractY(int fused){
        return fused >>> 16 & 0x7FFF;
    }

    /**
     * A convenience method that extracts the x and y components from {@code fused} and returns
     * them as a Coord.
     * @param fused a fused x,y pair as an int as produced by {@link #fuse(int, int)}
     * @return
     */
    public static Coord extractCoord(int fused) {
        return Coord.get(fused & 0x7FFF, fused >>> 16 & 0x7FFF);
    }

    public void put(int x, int y, int codepoint) {
        map.put(fuse(x, y), (codepoint & 0xFFFFFFFFL) | 0xFFFFFFFE00000000L);
    }

    public void put(int x, int y, int codepoint, int color) {
        map.put(fuse(x, y), (codepoint & 0xFFFFFFFFL) | (long) color << 32);
    }

    public void put(int x, int y, long glyph) {
        map.put(fuse(x, y), glyph);
    }

    public void put(int fused, long glyph){
        map.put(fused, glyph);
    }

    /**
     * Draws the entire GlyphMap at the given x,y for its lower-left corner. Does no clipping.
     * @param batch a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     * @param x x of the lower-left corner, in world units
     * @param y y of the lower-left corner, in world units
     */
    public void draw(Batch batch, float x, float y) {
        viewport.apply(false);
        font.enableShader(batch);
        if(backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        int pos;
        IntList order = map.order();
        for(int i = 0, n = order.size(); i < n; i++) {
            pos = order.get(i);
            font.drawGlyph(batch, map.getAt(i), x + extractX(pos) * font.cellWidth, y + extractY(pos) * font.cellHeight);
        }
    }

    /**
     * Draws part of the GlyphMap at the given x,y for its lower-left corner. Still iterates through all keys in order,
     * but only draws those that are visible in the given {@link Frustum} {@code limit}.
     * @param batch a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     * @param x x of the lower-left corner, in world units
     * @param y y of the lower-left corner, in world units
     * @param limit a Frustum, usually obtained from {@link com.badlogic.gdx.graphics.Camera#frustum}, that delimits what will be rendered
     */
    public void draw(Batch batch, float x, float y, Frustum limit) {
        viewport.apply(false);
        font.enableShader(batch);
        if(backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        int pos;
        float xPos, yPos, cellWidth = font.cellWidth * 2f, cellHeight = font.cellHeight * 2f;
        IntList order = map.order();
        for(int i = 0, n = order.size(); i < n; i++) {
            pos = order.get(i);
            xPos = x + extractX(pos) * font.cellWidth;
            yPos = y + extractY(pos) * font.cellHeight;
            if(limit.boundsInFrustum(xPos, yPos, 0, cellWidth, cellHeight, 1f))
                font.drawGlyph(batch, map.getAt(i), xPos, yPos);
        }
    }

    /**
     * Draws part of the GlyphMap at the given x,y for its lower-left corner. Only draws cells between startCellX
     * (inclusive) and endCellX (exclusive), and likewise for startCellY and endCellY, where those ints represent cell
     * positions and not screen or world positions. This only even considers keys that are in the given start-to-end
     * rectangle, and doesn't check keys or values outside it. This is probably the most efficient of the draw() methods
     * here, but requires you to know what the start and end bounds are.
     * @param batch a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     * @param x x of the lower-left corner, in world units
     * @param y y of the lower-left corner, in world units
     * @param startCellX the inclusive x of the lower-left corner, measured in cells, to start rendering at
     * @param startCellY the inclusive y of the lower-left corner, measured in cells, to start rendering at
     * @param endCellX the exclusive x of the upper-right corner, measured in cells, to stop rendering at
     * @param endCellY the exclusive y of the upper-right corner, measured in cells, to stop rendering at
     */
    public void draw(Batch batch, float x, float y, int startCellX, int startCellY, int endCellX, int endCellY) {
        viewport.apply(false);
        font.enableShader(batch);
        if(backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        int pos;
        long glyph;
        for (int xx = startCellX; xx < endCellX; xx++) {
            for (int yy = startCellY; yy < endCellY; yy++) {
                pos = fuse(xx, yy);
                glyph = map.getOrDefault(pos, 0L);
                if((glyph & 0x000000FE00000000L) != 0L) // if pos was found and glyph is not transparent
                    font.drawGlyph(batch, glyph, x + xx * font.cellWidth, y + yy * font.cellHeight);
            }
        }
    }

    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, false);
    }
}
