package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.ds.*;
import com.github.yellowstonegames.grid.Coord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GlyphMap {
    protected int gridWidth;
    protected int gridHeight;
    public ObjectLongOrderedMap<Coord> map;
    public int[][] backgrounds = null;
    public Font font;
    public Viewport viewport;

    /**
     * Does not set {@link #font}, you will have to set it later.
     */
    public GlyphMap(){
        map = new ObjectLongOrderedMap<Coord>(4096){
            @Override
            protected int place(@Nonnull Object item) {
                final Coord c = ((Coord)item);
                final int x = c.x, y = c.y;
                return y + ((x + y) * (x + y + 1) >> 1) & mask;
            }

            @Override
            protected boolean equate(@Nonnull Object left, @Nullable Object right) {
                return left == right;
            }
        };
        viewport = new StretchViewport(64, 64);
    }
    public GlyphMap(Font font){
        this(font, 64, 64);
    }
    public GlyphMap(Font font, int gridWidth, int gridHeight){
        Coord.expandPoolTo(gridWidth, gridHeight);
        this.font = new Font(font);
        if(this.font.distanceField != Font.DistanceFieldType.STANDARD)
            this.font.distanceFieldCrispness *= Math.sqrt(font.cellWidth) + Math.sqrt(font.cellHeight) + 1;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        map = new ObjectLongOrderedMap<Coord>(gridWidth * gridHeight){
            @Override
            protected int place(@Nonnull Object item) {
                final Coord c = ((Coord)item);
                final int x = c.x, y = c.y;
                return y + ((x + y) * (x + y + 1) >> 1) & mask;
            }

            @Override
            protected boolean equate(@Nonnull Object left, @Nullable Object right) {
                return left == right;
            }
        };
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
        map.put(Coord.get(x, y), (codepoint & 0xFFFFFFFFL) | 0xFFFFFFFE00000000L);
    }

    public void put(int x, int y, int codepoint, int color) {
        map.put(Coord.get(x, y), (codepoint & 0xFFFFFFFFL) | (long) color << 32);
    }

    public void put(int x, int y, long glyph) {
        map.put(Coord.get(x, y), glyph);
    }

    public void put(Coord fused, long glyph){
        map.put(fused, glyph);
    }

    public void draw(Batch batch, float x, float y) {
        viewport.apply(false);
        font.enableShader(batch);
        if(backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        Coord pos;
        ObjectList<Coord> order = map.order();
        for(int i = 0, n = order.size(); i < n; i++) {
            pos = order.get(i);
            font.drawGlyph(batch, map.getAt(i), x + pos.x * font.cellWidth, y + pos.y * font.cellHeight);
        }
    }

    public void draw(Batch batch, float x, float y, Frustum limit) {
        viewport.apply(false);
        font.enableShader(batch);
        if(backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        Coord pos;
        ObjectList<Coord> order = map.order();
        float xPos, yPos, cellWidth = font.cellWidth * 2f, cellHeight = font.cellHeight * 2f;
        for(int i = 0, n = order.size(); i < n; i++) {
            pos = order.get(i);
            xPos = x + pos.x * font.cellWidth;
            yPos = y + pos.y * font.cellHeight;
            if(limit.boundsInFrustum(xPos, yPos, 0, cellWidth, cellHeight, 1f))
                font.drawGlyph(batch, map.getAt(i), xPos, yPos);
        }
    }

    public void draw(Batch batch, float x, float y, int startX, int startY, int endX, int endY) {
        viewport.apply(false);
        font.enableShader(batch);
        if(backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        Coord pos;
        long glyph;
        for (int xx = startX; xx < endX; xx++) {
            for (int yy = startY; yy < endY; yy++) {
                pos = Coord.get(xx, yy);
                glyph = map.getOrDefault(pos, 0L);
                if(glyph != 0L)
                    font.drawGlyph(batch, glyph, x + xx * font.cellWidth, y + yy * font.cellHeight);
            }
        }
    }

    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, false);
    }
}
