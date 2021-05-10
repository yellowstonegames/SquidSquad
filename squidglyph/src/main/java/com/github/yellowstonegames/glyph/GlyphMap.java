package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.ds.IntLongMap;
import com.github.tommyettinger.ds.IntLongOrderedMap;

public class GlyphMap {
    protected int gridWidth;
    protected int gridHeight;
    public IntLongOrderedMap map;
    public int[][] backgrounds = null;
    public Font font;
    public Viewport viewport;

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

    public void draw(Batch batch, float x, float y) {
        viewport.apply(false);
        font.enableShader(batch);
        if(backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        int pos;
//        final int h = backgrounds[0].length - 1;
        for(IntLongMap.Entry e : map) {
            pos = e.key;
            font.drawGlyph(batch, e.value, x + extractX(pos) * font.cellWidth, y + (extractY(pos)) * font.cellHeight);
        }
    }

    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, false);
    }
}
