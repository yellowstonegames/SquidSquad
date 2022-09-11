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
import com.github.tommyettinger.textra.ColorLookup;
import com.github.tommyettinger.textra.Font;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.Coord;

/**
 * Stores a sparse map of (unmoving) glyphs by their positions, as well as a dense grid of background colors, and allows
 * this group of chars and colors to be drawn easily.
 * Glyphs are represented by long values, and are handled by a {@link Font} (which this also stores) to get a char,
 * foreground color, and a variety of possible styles for each glyph. A GlyphMap also stores a {@link Viewport}, which
 * defaults to a {@link StretchViewport} but can be changed easily; this viewport affects what region of the GlyphMap is
 * drawn. You typically call one of the {@link #draw(Batch, float, float)} methods in your rendering code every frame,
 * some time after clearing the screen. There are draw() overloads that only render a subregion of the GlyphMap; one
 * uses a {@link Frustum} (almost always from {@link com.badlogic.gdx.graphics.Camera#frustum}) to draw only visible
 * glyphs and backgrounds, while another takes a specific rectangular area of cells. You should call
 * {@link #resize(int, int)} in your application's or screen's resize code, because it keeps the viewport accurate.
 */
public class GlyphMap {
    protected int gridWidth;
    protected int gridHeight;
    public IntLongOrderedMap map;
    public int[][] backgrounds = null;
    protected Font font;
    public Viewport viewport;
    /**
     * This is mostly here to help ColorLookup get RGBA8888 colors from valid descriptions, or null from invalid ones.
     * @param description a color description, as a lower-case String matching the format covered in {@link DescriptiveColor#describe(CharSequence)}
     * @return an Integer that either contains an RGBA8888 color (if description was valid) or null otherwise
     */
    public static int getRgba(final String description){
        if(description == null || description.length() == 0) return 256;
        int oklab = DescriptiveColor.describeOklab(description);
        if(oklab == 0) {
            Color c = Colors.get(description);
            if(c == null) return 256;
            return Color.rgba8888(c);
        }
        return DescriptiveColor.toRGBA8888(oklab);
    }

    /**
     * Constructs a bare-bones GlyphMap with size 64x64. Does not set {@link #font}, you will have to set it later.
     */
    public GlyphMap(){
        this(null, 64, 64, false);
    }

    /**
     * Constructs a 64x64 GlyphMap with the specified Font. You probably want {@link #GlyphMap(Font, int, int)} unless
     * your maps are always 64x64. This calls {@link #GlyphMap(Font, int, int, boolean)} with
     * squareCenteredCells=false.
     * @param font a Font that will be copied and used for the new GlyphMap
     */
    public GlyphMap(Font font){
        this(font, 64, 64, false);
    }

    /**
     * Constructs a GlyphMap with the specified size in cells wide and cells tall for its grid, using the specified
     * Font (which will be copied). This calls {@link #GlyphMap(Font, int, int, boolean)} with
     * squareCenteredCells=false.
     * @param font a Font that will be copied and used for the new GlyphMap
     * @param gridWidth how many cells wide the grid should be
     * @param gridHeight how many cells tall the grid should be
     */
    public GlyphMap(Font font, int gridWidth, int gridHeight) {
        this(font, gridWidth, gridHeight, false);
    }
    /**
     * Constructs a GlyphMap with the specified size in cells wide and cells tall for its grid, using the specified
     * Font (which will be copied). If squareCenteredGlyphs is true, the Font copy this uses will be modified to have
     * extra space around glyphs so that they fit in square cells. For fonts that use gridGlyphs (the default behavior),
     * any box drawing characters will still take up the full cell, and will connect seamlessly.
     * @param font a Font that will be copied and used for the new GlyphMap
     * @param gridWidth how many cells wide the grid should be
     * @param gridHeight how many cells tall the grid should be
     * @param squareCenteredCells if true, space will be added to make glyphs fit in square cells
     */
    public GlyphMap(Font font, int gridWidth, int gridHeight, boolean squareCenteredCells) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        map = new IntLongOrderedMap(gridWidth * gridHeight);
        viewport = new StretchViewport(gridWidth, gridHeight);
        if (font != null) {
            setFont(new Font(font), squareCenteredCells);
        }
    }

    public Font getFont() {
        return font;
    }

    /**
     * Sets the Font this uses, but also configures the viewport to use the appropriate size cells, then scales the font
     * to size 1x1 (this makes some calculations much easier inside GlyphMap). This is the same as calling
     * {@code setFont(font, true)}.
     * @param font a Font that will be used directly (not copied) and used to calculate the viewport dimensions
     */
    public void setFont(Font font) {
        setFont(font, true);
    }
    /**
     * Sets the Font this uses, but also configures the viewport to use the appropriate size cells, then scales the font
     * to size 1x1 (this makes some calculations much easier inside GlyphMap). This can add spacing to cells so that
     * they are always square, while keeping the aspect ratio of {@code font} as it was passed in. Use squareCenter=true
     * to enable this; note that it modifies the Font more deeply than normally.
     * @param font a Font that will be used directly (not copied) and used to calculate the viewport dimensions
     * @param squareCenter if true, spacing will be added to the sides of each glyph so that they fit in square cells
     */
    public void setFont(Font font, boolean squareCenter) {
        if (font == null) return;
        this.font = font;
        font.setColorLookup(ColorLookup.DESCRIPTIVE);
        if (squareCenter) {
            if (font.distanceField == Font.DistanceFieldType.MSDF)
                font.distanceFieldCrispness *= Math.sqrt(font.cellWidth) + Math.sqrt(font.cellHeight) + 2f;
            viewport.setScreenWidth((int) (gridWidth * font.cellWidth));
            viewport.setScreenHeight((int) (gridHeight * font.cellHeight));
            float larger = Math.max(font.cellWidth, font.cellHeight);
            font.scaleTo(font.cellWidth / larger, font.cellHeight / larger).fitCell(1f, 1f, true);
        }
        else {
            if (font.distanceField == Font.DistanceFieldType.MSDF)
                font.distanceFieldCrispness *= Math.sqrt(font.cellWidth) + Math.sqrt(font.cellHeight) + 2f;
// not sure if we want this code or the above.
//            if (this.font.distanceField != Font.DistanceFieldType.STANDARD)
//                this.font.distanceFieldCrispness *= Math.sqrt(font.cellWidth) + Math.sqrt(font.cellHeight) + 1;
            viewport.setScreenWidth((int) (gridWidth * font.cellWidth));
            viewport.setScreenHeight((int) (gridHeight * font.cellHeight));
            font.scaleTo(1f, 1f);
        }
        font.useIntegerPositions(false);
    }

    /**
     * Gets how wide the grid is, measured in discrete cells.
     * @return how many cells wide the grid is
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Gets how high the grid is, measured in discrete cells.
     * @return how many cells high the grid is
     */
    public int getGridHeight() {
        return gridHeight;
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

    /**
     * Places a character (optionally with style information) at the specified cell, using white foreground color.
     * @param x x position of the cell, measured in cells on the grid
     * @param y y position of the cell, measured in cells on the grid
     * @param codepoint the character, with or without style information, to place
     */
    public void put(int x, int y, int codepoint) {
        map.put(fuse(x, y), (codepoint & 0xFFFFFFFFL) | 0xFFFFFFFE00000000L);
    }

    /**
     * Places a character (optionally with style information) at the specified cell, using the given foreground color.
     * @param x x position of the cell, measured in cells on the grid
     * @param y y position of the cell, measured in cells on the grid
     * @param codepoint the character, with or without style information, to place
     * @param color the RGBA8888 color to use for the character
     */
    public void put(int x, int y, int codepoint, int color) {
        map.put(fuse(x, y), (codepoint & 0xFFFFFFFFL) | (long) color << 32);
    }

    /**
     * Places a character (optionally with style information) at the specified cell, using the given foreground color.
     * @param x x position of the cell, measured in cells on the grid
     * @param y y position of the cell, measured in cells on the grid
     * @param simpleChar the character, without style information, to place
     * @param color the RGBA8888 color to use for the character
     */
    public void put(int x, int y, char simpleChar, int color) {
        map.put(fuse(x, y), (simpleChar) | (long) color << 32);
    }

    /**
     * Places a glyph (optionally with style information and/or color) at the specified cell.
     * @param x x position of the cell, measured in cells on the grid
     * @param y y position of the cell, measured in cells on the grid
     * @param glyph the glyph to place, as produced by {@link Font#markupGlyph(char, String, ColorLookup)}
     */
    public void put(int x, int y, long glyph) {
        map.put(fuse(x, y), glyph);
    }

    /**
     * Places a glyph (optionally with style information and/or color) at the specified cell (given as a fused value).
     * This put() method has the least overhead if you already have a fused int key and long glyph.
     * @param fused a fused x,y position, as produced by {@link #fuse(int, int)}
     * @param glyph the glyph to place, as produced by {@link Font#markupGlyph(char, String, ColorLookup)}
     */
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
            font.drawGlyph(batch, map.getAt(i), x + extractX(pos), y + extractY(pos));
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
        float xPos, yPos, boundsWidth = 2f, boundsHeight = 2f;
        IntList order = map.order();
        for(int i = 0, n = order.size(); i < n; i++) {
            pos = order.get(i);
            xPos = x + extractX(pos);
            yPos = y + extractY(pos);
            if(limit.boundsInFrustum(xPos, yPos, 0, boundsWidth, boundsHeight, 1f))
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

    /**
     * This should generally be called in the {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} or
     * {@link com.badlogic.gdx.Screen#resize(int, int)} method when the screen size changes. This affects the viewport
     * only.
     * @param screenWidth the new screen width in pixels
     * @param screenHeight the new screen height in pixels
     */
    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, false);
        font.resizeDistanceField(screenWidth, screenHeight);
    }
}
