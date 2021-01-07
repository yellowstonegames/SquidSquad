package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.github.tommyettinger.ds.IntLongMap;
import com.github.tommyettinger.ds.IntLongOrderedMap;

public class GlyphMap {
    public IntLongOrderedMap map;
    public int[][] backgrounds = null;
    public Font font;

    /**
     * Does not set {@link #font}, you will have to set it later.
     */
    public GlyphMap(){
        this(2048);
    }
    public GlyphMap(Font font){
        this(font, 2048);
    }
    public GlyphMap(Font font, int capacity){
        this.font = font;
        map = new IntLongOrderedMap(capacity);
    }
    /**
     * Does not set {@link #font}, you will have to set it later.
     * @param capacity how many glyphs this should be able to hold at the start, before resizing
     */
    public GlyphMap(int capacity){
        map = new IntLongOrderedMap(capacity);
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
        font.enableShader(batch);
        if(backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        int pos;
        for(IntLongMap.Entry e : map) {
            pos = e.key;
            font.drawGlyph(batch, e.value, x + extractX(pos) * font.cellWidth, y + extractY(pos) * font.cellHeight);
        }
    }
}
