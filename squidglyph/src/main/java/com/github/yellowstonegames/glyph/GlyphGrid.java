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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntLongOrderedMap;
import com.github.tommyettinger.function.IntIntPredicate;
import com.github.tommyettinger.textra.ColorLookup;
import com.github.tommyettinger.textra.Font;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.Coord;

import javax.annotation.Nullable;

/**
 * Stores a sparse map of (unmoving) glyphs by their positions, as a dense grid of background colors, and potentially
 * Actors that can move around and perform Actions.
 * Glyphs are represented by long values, and are handled by a {@link Font} (which this also stores) to get a char,
 * foreground color, and a variety of possible styles for each glyph. A GlyphGrid also stores a {@link Viewport}, which
 * defaults to a {@link StretchViewport} but can be changed easily; this viewport affects what region of the GlyphGrid
 * is drawn. This is a scene2d Group and Actor, and must be added to a Stage to function correctly. If you want to draw
 * a limited portion of the grid for performance, you can use an overload of {@link #draw(Batch, Frustum)} and manually
 * draw the GlyphGrid, or keep using {@link Stage#draw()} and set the {@link #startX}, {@link #startY}, {@link #endX},
 * and {@link #endY} fields to restrict the drawn area. You should call {@link #resize(int, int)} in your application's
 * or screen's resize code, because it keeps the viewport accurate.
 * <br>
 * {@link com.badlogic.gdx.scenes.scene2d.Action} behavior is unfortunately a little complex, but issues can usually be
 * resolved by moving around the order in which {@link Stage#act()} {@link Stage#draw()}, and map-changing methods are
 * called. Typically, if you {@link #put(int, long)} every visible glyph every frame, you would do that first, then call
 * Stage.act(), and only then call Stage.draw(). This allows Actions (in act()) to affect the GlyphGrid without the map
 * being overwritten before it can be drawn. There are some useful Actions specialized to working with GlyphGrid in
 * {@link GridAction}, and some non-specialized support code for Actions in {@link MoreActions}.
 */
public class GlyphGrid extends Group {
    protected int gridWidth;
    protected int gridHeight;
    public IntLongOrderedMap map;
    public int[][] backgrounds = null;
    protected Font font;
    public Viewport viewport;
    public int startX, startY, endX, endY;

    /**
     * Constructs a bare-bones GlyphGrid with size 64x64. Does not set {@link #font}, you will have to set it later.
     */
    public GlyphGrid() {
        this(null, 64, 64, false);
    }

    /**
     * Constructs a 64x64 GlyphGrid with the specified Font. You probably want {@link #GlyphGrid(Font, int, int)} unless
     * your maps are always 64x64. This calls {@link #GlyphGrid(Font, int, int, boolean)} with
     * squareCenteredCells=false.
     *
     * @param font a Font that will be copied and used for the new GlyphGrid
     */
    public GlyphGrid(Font font) {
        this(font, 64, 64, false);
    }

    /**
     * Constructs a GlyphGrid with the specified size in cells wide and cells tall for its grid, using the specified
     * Font (which will be copied). This calls {@link #GlyphGrid(Font, int, int, boolean)} with
     * squareCenteredCells=false.
     *
     * @param font       a Font that will be copied and used for the new GlyphGrid
     * @param gridWidth  how many cells wide the grid should be
     * @param gridHeight how many cells tall the grid should be
     */
    public GlyphGrid(Font font, int gridWidth, int gridHeight) {
        this(font, gridWidth, gridHeight, false);
    }

    /**
     * Constructs a GlyphGrid with the specified size in cells wide and cells tall for its grid, using the specified
     * Font (which will be copied). If squareCenteredGlyphs is true, the Font copy this uses will be modified to have
     * extra space around glyphs so that they fit in square cells. For fonts that use gridGlyphs (the default behavior),
     * any box drawing characters will still take up the full cell, and will connect seamlessly.
     *
     * @param font                a Font that will be copied and used for the new GlyphGrid
     * @param gridWidth           how many cells wide the grid should be
     * @param gridHeight          how many cells tall the grid should be
     * @param squareCenteredCells if true, space will be added to make glyphs fit in square cells
     */
    public GlyphGrid(Font font, int gridWidth, int gridHeight, boolean squareCenteredCells) {
        super();
        setTransform(false);
        this.startX = 0;
        this.startY = 0;
        this.gridWidth = this.endX = gridWidth;
        this.gridHeight = this.endY = gridHeight;
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
     * to size 1x1 (this makes some calculations much easier inside GlyphGrid). This is the same as calling
     * {@code setFont(font, true)}.
     *
     * @param font a Font that will be used directly (not copied) and used to calculate the viewport dimensions
     */
    public void setFont(Font font) {
        setFont(font, true);
    }

    /**
     * Sets the Font this uses, but also configures the viewport to use the appropriate size cells, then scales the font
     * to size 1x1 (this makes some calculations much easier inside GlyphGrid). This can add spacing to cells so that
     * they are always square, while keeping the aspect ratio of {@code font} as it was passed in. Use squareCenter=true
     * to enable this; note that it modifies the Font more deeply than normally.
     *
     * @param font         a Font that will be used directly (not copied) and used to calculate the viewport dimensions
     * @param squareCenter if true, spacing will be added to the sides of each glyph so that they fit in square cells
     */
    public void setFont(Font font, boolean squareCenter) {
        if (font == null) return;
        this.font = font;
        font.useIntegerPositions(false);
        if (squareCenter) {
            if (font.distanceField == Font.DistanceFieldType.MSDF)
                font.distanceFieldCrispness *= Math.sqrt(font.cellWidth) + Math.sqrt(font.cellHeight) + 2f;
            viewport.setScreenWidth((int) (gridWidth * font.cellWidth));
            viewport.setScreenHeight((int) (gridHeight * font.cellHeight));
            float larger = Math.max(font.cellWidth, font.cellHeight);
            font.scaleTo(font.cellWidth / larger, font.cellHeight / larger).fitCell(1f, 1f, true);
        } else {
            if (font.distanceField == Font.DistanceFieldType.MSDF)
                font.distanceFieldCrispness *= Math.sqrt(font.cellWidth) + Math.sqrt(font.cellHeight) + 2f;
// not sure if we want this code or the above.
//            if (this.font.distanceField != Font.DistanceFieldType.STANDARD)
//                this.font.distanceFieldCrispness *= Math.sqrt(font.cellWidth) + Math.sqrt(font.cellHeight) + 1;
            viewport.setScreenWidth((int) (gridWidth * font.cellWidth));
            viewport.setScreenHeight((int) (gridHeight * font.cellHeight));
            font.scaleTo(1f, 1f);
        }
    }


    /**
     * Gets how wide the grid is, measured in discrete cells.
     *
     * @return how many cells wide the grid is
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Gets how high the grid is, measured in discrete cells.
     *
     * @return how many cells high the grid is
     */
    public int getGridHeight() {
        return gridHeight;
    }

    /**
     * Combines the up-to-15-bit non-negative ints x and y into one non-negative result int.
     * This is used to get the keys for a GlyphGrid given x,y inputs. Because jdkgdxds uses
     * Fibonacci hashing to scramble keys, there's no concern here from leaving all of x in
     * the lower half of the bits and all of y in the upper half.
     *
     * @param x an up-to-15-bit non-negative int (between 0 and 32767, inclusive)
     * @param y an up-to-15-bit non-negative int (between 0 and 32767, inclusive)
     * @return a non-negative int that combines x and y into one value
     */
    public static int fuse(final int x, final int y) {
        return (x & 0x7FFF) | (y << 16 & 0x7FFF0000);
    }

    /**
     * Combines the up-to-15-bit non-negative x and y components of the Coord {@code xy} into one
     * non-negative result int. This is used to get the keys for a GlyphGrid given x,y inputs. Because
     * jdkgdxds uses Fibonacci hashing to scramble keys, there's no concern here from leaving all of
     * x in the lower half of the bits and all of y in the upper half.
     *
     * @param xy a Coord with non-negative x and y
     * @return a non-negative int that combines the Coord's x and y into one value
     */
    public static int fuse(final Coord xy) {
        return (xy.x & 0x7FFF) | (xy.y << 16 & 0x7FFF0000);
    }

    /**
     * Given a fused x,y pair as an int (typically produced by {@link #fuse(int, int)}), this
     * gets the x component from it, as an int.
     *
     * @param fused a fused x,y pair as an int as produced by {@link #fuse(int, int)}
     * @return the x component of fused
     */
    public static int extractX(int fused) {
        return fused & 0x7FFF;
    }

    /**
     * Given a fused x,y pair as an int (typically produced by {@link #fuse(int, int)}), this
     * gets the y component from it, as an int.
     *
     * @param fused a fused x,y pair as an int as produced by {@link #fuse(int, int)}
     * @return the y component of fused
     */
    public static int extractY(int fused) {
        return fused >>> 16 & 0x7FFF;
    }

    /**
     * A convenience method that extracts the x and y components from {@code fused} and returns
     * them as a Coord.
     *
     * @param fused a fused x,y pair as an int as produced by {@link #fuse(int, int)}
     * @return
     */
    public static Coord extractCoord(int fused) {
        return Coord.get(fused & 0x7FFF, fused >>> 16 & 0x7FFF);
    }

    /**
     * Places a character (optionally with style information) at the specified cell, using white foreground color.
     *
     * @param x         x position of the cell, measured in cells on the grid
     * @param y         y position of the cell, measured in cells on the grid
     * @param codepoint the character, with or without style information, to place
     */
    public void put(int x, int y, int codepoint) {
        map.put(fuse(x, y), (codepoint & 0xFFFFFFFFL) | 0xFFFFFFFE00000000L);
    }

    /**
     * Places a character (optionally with style information) at the specified cell, using the given foreground color.
     *
     * @param x         x position of the cell, measured in cells on the grid
     * @param y         y position of the cell, measured in cells on the grid
     * @param codepoint the character, with or without style information, to place
     * @param color     the RGBA8888 color to use for the character
     */
    public void put(int x, int y, int codepoint, int color) {
        map.put(fuse(x, y), (codepoint & 0xFFFFFFFFL) | (long) color << 32);
    }

    /**
     * Places a character (optionally with style information) at the specified cell, using the given foreground color.
     *
     * @param x          x position of the cell, measured in cells on the grid
     * @param y          y position of the cell, measured in cells on the grid
     * @param simpleChar the character, without style information, to place
     * @param color      the RGBA8888 color to use for the character
     */
    public void put(int x, int y, char simpleChar, int color) {
        map.put(fuse(x, y), (simpleChar) | (long) color << 32);
    }

    /**
     * Places a glyph (optionally with style information and/or color) at the specified cell.
     *
     * @param x     x position of the cell, measured in cells on the grid
     * @param y     y position of the cell, measured in cells on the grid
     * @param glyph the glyph to place, as produced by {@link Font#markupGlyph(char, String, ColorLookup)}
     */
    public void put(int x, int y, long glyph) {
        map.put(fuse(x, y), glyph);
    }

    /**
     * Places a glyph (optionally with style information and/or color) at the specified cell (given as a fused value).
     * This put() method has the least overhead if you already have a fused int key and long glyph.
     *
     * @param fused a fused x,y position, as produced by {@link #fuse(int, int)}
     * @param glyph the glyph to place, as produced by {@link Font#markupGlyph(char, String, ColorLookup)}
     */
    public void put(int fused, long glyph) {
        map.put(fused, glyph);
    }

    /**
     * Draws the entire GlyphGrid at its position in world units. Does no clipping.
     *
     * @param batch a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     */
    public void draw(Batch batch) {
        getStage().setViewport(viewport);
        font.enableShader(batch);
        float x = getX(), y = getY();
        if (backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        int pos;
        IntList order = map.order();
        for (int i = 0, n = order.size(); i < n; i++) {
            pos = order.get(i);
            font.drawGlyph(batch, map.getAt(i), x + extractX(pos), y + extractY(pos));
        }
        super.drawChildren(batch, 1f);
    }

    /**
     * Draws part of the GlyphGrid at its position in world units. Still iterates through all keys in order,
     * but only draws those that are visible in the given {@link Frustum} {@code limit}.
     *
     * @param batch a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     * @param limit a Frustum, usually obtained from {@link com.badlogic.gdx.graphics.Camera#frustum}, that delimits what will be rendered
     */
    public void draw(Batch batch, Frustum limit) {
        getStage().setViewport(viewport);
        font.enableShader(batch);
        float x = getX(), y = getY();
        if (backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        int pos;
        float xPos, yPos, boundsWidth = 2f, boundsHeight = 2f;
        IntList order = map.order();
        for (int i = 0, n = order.size(); i < n; i++) {
            pos = order.get(i);
            xPos = x + extractX(pos);
            yPos = y + extractY(pos);
            if (limit.boundsInFrustum(xPos, yPos, 0, boundsWidth, boundsHeight, 1f))
                font.drawGlyph(batch, map.getAt(i), xPos, yPos);
        }
        super.drawChildren(batch, 1f);

    }

    /**
     * Draws part of the GlyphGrid at its position in world units. Only draws cells between startCellX
     * (inclusive) and endCellX (exclusive), and likewise for startCellY and endCellY, where those ints represent cell
     * positions and not screen or world positions. This only even considers keys that are in the given start-to-end
     * rectangle, and doesn't check keys or values outside it. This is probably the most efficient of the draw() methods
     * here, but requires you to know what the start and end bounds are. All of the start and end cell coordinates must
     * be non-negative.
     *
     * @param batch      a SpriteBatch, usually; must at least be compatible with SpriteBatch's attributes
     * @param startCellX the inclusive x of the lower-left corner, measured in cells, to start rendering at
     * @param startCellY the inclusive y of the lower-left corner, measured in cells, to start rendering at
     * @param endCellX   the exclusive x of the upper-right corner, measured in cells, to stop rendering at
     * @param endCellY   the exclusive y of the upper-right corner, measured in cells, to stop rendering at
     */
    public void draw(Batch batch, int startCellX, int startCellY, int endCellX, int endCellY) {
        getStage().setViewport(viewport);
        font.enableShader(batch);
        float x = getX(), y = getY();
        if (backgrounds != null)
            font.drawBlocks(batch, backgrounds, x, y);
        int pos;
        long glyph;
        for (int xx = startCellX; xx < endCellX; xx++) {
            for (int yy = startCellY; yy < endCellY; yy++) {
                pos = fuse(xx, yy);
                glyph = map.getOrDefault(pos, 0L);
                if ((glyph & 0x000000FE00000000L) != 0L) // if pos was found and glyph is not transparent
                    font.drawGlyph(batch, glyph, x + xx * font.cellWidth, y + yy * font.cellHeight);
            }
        }
        super.drawChildren(batch, 1f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.getColor().a *= parentAlpha;
        batch.setColor(batch.getColor());
        draw(batch, startX, startY, endX, endY);
    }

    /**
     * This should generally be called in the {@link com.badlogic.gdx.ApplicationListener#resize(int, int)} or
     * {@link com.badlogic.gdx.Screen#resize(int, int)} method when the screen size changes. This affects the viewport
     * only.
     *
     * @param screenWidth  the new screen width in pixels
     * @param screenHeight the new screen height in pixels
     */
    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, false);
        font.resizeDistanceField(screenWidth, screenHeight);
    }

    /**
     * Sets the visibility of each Actor child of this GlyphGrid based on the result of a predicate called on its x,y
     * grid position (rounded from its float position). If {@code predicate} returns true, an Actor will be set to be
     * visible, while if it returns false, the Actor will be set to be invisible.
     *
     * @param predicate will be given the rounded x,y positions of Actors and should return true if the Actor is visible
     */
    public void setVisibilities(IntIntPredicate predicate) {
        SnapshotArray<Actor> children = getChildren();
        Actor[] kids = children.begin();
        for (int i = 0, n = children.size; i < n; i++) {
            kids[i].setVisible(predicate.test(Math.round(kids[i].getX()), Math.round(kids[i].getY())));
        }
        children.end();
    }

    /**
     * Returns true if any children of this GlyphGrid currently have Actions, or false if none do.
     *
     * @return whether any children of this GlyphGrid currently have Actions
     */
    public boolean areChildrenActing() {
        SnapshotArray<Actor> children = getChildren();
        for (int i = 0, n = children.size; i < n; i++) {
            if (children.get(i).hasActions()) {
                return true;
            }
        }
        return false;
    }

    public MoreActions.LenientSequenceAction dyeFG(int x, int y, int newColor, float change, float duration, Runnable post) {
        final int fused = fuse(x, y);
        final long existing = map.getOrDefault(fused, 0),
                next = (existing & 0xFFFFFFFFL) | (long) DescriptiveColor.lerpColors((int) (existing >>> 32), newColor, change) << 32;
        TemporalAction temporal = new TemporalAction() {
            @Override
            protected void update(float percent) {
                map.put(fused, next);
            }
        };
        temporal.setDuration(duration);
        return new MoreActions.LenientSequenceAction(temporal, post == null ? null : Actions.run(post));
    }

    /**
     * Adds a new GlyphActor at (startX, startY) in world coordinates (which should have 1 world unit equal to 1 cell)
     * using the char shown with the given startColor and startRotation, and immediately starts changing color
     * to endColor, changing position so that it ends at the world coordinates (endX, endY), taking duration seconds to
     * complete before removing the GlyphActor. This doesn't return any kind of Action because it handles the creating
     * of a GlyphActor and its Actions on its own.
     *
     * @param startX        the starting x position in world coordinates
     * @param startY        the starting y position in world coordinates
     * @param endX          the ending x position in world coordinates
     * @param endY          the ending y position in world coordinates
     * @param shown         the char to show (the same char throughout the effect)
     * @param startColor    the starting Color
     * @param endColor      the Color to transition to
     * @param startRotation the starting rotation for the summoned glyph, in degrees
     * @param endRotation   the ending rotation for the summoned glyph, in degrees
     * @param duration      the duration in seconds for the effect
     */
    public void summon(float startX, float startY, float endX, float endY, char shown,
                       final int startColor, final int endColor, final float startRotation, final float endRotation,
                       float duration) {
        summon(0f, startX, startY, endX, endY, shown, startColor, endColor, startRotation, endRotation, duration, null);
    }

    /**
     * Adds a new GlyphActor at (startX, startY) in world coordinates (which should have 1 world unit equal to 1 cell)
     * using the char shown with the given startColor and startRotation, and after delay seconds, starts changing color
     * to endColor, changing position so that it ends at the world coordinates (endX, endY), taking duration seconds to
     * complete before running postRunnable (if it is non-null) and finally removing the GlyphActor. This doesn't return
     * any kind of Action because it handles the creating of a GlyphActor and its Actions on its own.
     *
     * @param delay         how long to wait in seconds before starting the effect
     * @param startX        the starting x position in world coordinates
     * @param startY        the starting y position in world coordinates
     * @param endX          the ending x position in world coordinates
     * @param endY          the ending y position in world coordinates
     * @param shown         the char to show (the same char throughout the effect)
     * @param startColor    the starting Color
     * @param endColor      the Color to transition to
     * @param startRotation the starting rotation for the summoned glyph, in degrees
     * @param endRotation   the ending rotation for the summoned glyph, in degrees
     * @param duration      the duration in seconds for the effect
     * @param postRunnable  a Runnable to execute after the summoning completes; may be null to do nothing.
     */
    public void summon(float delay, float startX, float startY, float endX, float endY, char shown,
                       final int startColor, final int endColor, final float startRotation, final float endRotation,
                       float duration, @Nullable Runnable postRunnable){
        summon(delay, startX, startY, endX, endY, shown, startColor, endColor, startRotation, endRotation, duration,
                Interpolation.linear, postRunnable);
    }
    /**
     * Adds a new GlyphActor at (startX, startY) in world coordinates (which should have 1 world unit equal to 1 cell)
     * using the char shown with the given startColor and startRotation, and after delay seconds, starts changing color
     * to endColor, changing position using the given Interpolation so that it ends at the world coordinates (endX,
     * endY), taking duration seconds to complete before running postRunnable (if it is non-null) and finally removing
     * the GlyphActor. This doesn't return any kind of Action because it handles the creating of a GlyphActor and its
     * Actions on its own.
     * @param delay how long to wait in seconds before starting the effect
     * @param startX the starting x position in world coordinates
     * @param startY the starting y position in world coordinates
     * @param endX the ending x position in world coordinates
     * @param endY the ending y position in world coordinates
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param startRotation the starting rotation for the summoned glyph, in degrees
     * @param endRotation the ending rotation for the summoned glyph, in degrees
     * @param duration the duration in seconds for the effect
     * @param moveInterpolation a non-null Interpolation that affects the rate at which the glyph moves
     * @param postRunnable a Runnable to execute after the summoning completes; may be null to do nothing.
     */
    public void summon(float delay, float startX, float startY, float endX, float endY, char shown,
                       final int startColor, final int endColor, final float startRotation, final float endRotation,
                       float duration, Interpolation moveInterpolation, @Nullable Runnable postRunnable)
    {
        final int nbActions = 2 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        int index = 0;
        final Action[] sequence = new Action[nbActions];
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        final GlyphActor glyph = new GlyphActor(shown, startColor, font);
        glyph.setPosition(startX, startY);
        glyph.setRotation(startRotation);
        addActor(glyph);
        sequence[index++] = Actions.parallel(
                new TemporalAction(duration) {
                    @Override
                    protected void update(float percent) {
                        glyph.setColor(DescriptiveColor.lerpColors(startColor, endColor, percent * 0.95f));
                    }
                },
                Actions.moveTo(endX, endY, duration, moveInterpolation),
                Actions.rotateTo(endRotation, duration));
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
        sequence[index] = Actions.run(() -> removeActor(glyph));
        glyph.addAction(Actions.sequence(sequence));
    }

    /**
     * Adds {@code count} new GlyphActors at (startX, startY) in world coordinates (which should have 1 world unit equal
     * to 1 cell) using the char shown with the given startColor, and immediately starts changing color to endColor,
     * changing position so that it ends at the world coordinates (endX, endY), taking duration seconds to
     * complete before finally removing the GlyphActors. The rotation of each GlyphActor this spawns will be different;
     * the given startRotation applies to the always-created GlyphActor directly above the start, and the endRotation
     * will be that GlyphActor's rotation when it is removed, but any other created GlyphActors will have their own
     * rotations based on the direction they move in. This doesn't return any kind of Action because it handles the
     * creating of a GlyphActor and its Actions on its own.
     * @param startX the starting x position in world coordinates
     * @param startY the starting y position in world coordinates
     * @param distance how far away each GlyphActor should move away from the start, in world coordinates
     * @param count how many chars to summon
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param startRotation the starting rotation for the top summoned glyph, in degrees
     * @param endRotation the ending rotation for the top summoned glyph, in degrees
     * @param duration the duration in seconds for the effect
     */
    public void burst(float startX, float startY, float distance, int count, char shown,
                       final int startColor, final int endColor, final float startRotation, final float endRotation,
                       float duration)
    {
        burst(0f, startX, startY, distance, count, shown, startColor, endColor, startRotation, endRotation, duration, null);
    }
    /**
     * Adds {@code count} new GlyphActors at (startX, startY) in world coordinates (which should have 1 world unit equal
     * to 1 cell) using the char shown with the given startColor, and after delay seconds, starts changing color to
     * endColor, changing position so that it ends at the world coordinates (endX, endY), taking duration seconds to
     * complete before running postRunnable (if it is non-null) and finally removing the GlyphActors. The rotation of
     * each GlyphActor this spawns will be different; the given startRotation applies to the always-created GlyphActor
     * directly above the start, and the endRotation will be that GlyphActor's rotation when it is removed, but any
     * other created GlyphActors will have their own rotations based on the direction they move in. This doesn't return
     * any kind of Action because it handles the creating of a GlyphActor and its Actions on its own.
     * @param delay how long to wait in seconds before starting the effect
     * @param startX the starting x position in world coordinates
     * @param startY the starting y position in world coordinates
     * @param distance how far away each GlyphActor should move away from the start, in world coordinates
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param startRotation the starting rotation for the top summoned glyph, in degrees
     * @param endRotation the ending rotation for the top summoned glyph, in degrees
     * @param duration the duration in seconds for the effect
     * @param postRunnable a Runnable to execute after the summoning completes; may be null to do nothing.
     */
    public void burst(float delay, float startX, float startY, float distance, int count, char shown,
                       final int startColor, final int endColor, final float startRotation, final float endRotation,
                       float duration, @Nullable Runnable postRunnable) {
                    for (int i = 0; i < count; i++) {
                        float angle = 360f * i / count + 90f;
                        summon(delay, startX, startY, startX + TrigTools.cosDeg(angle) * distance, startY + TrigTools.sinDeg(angle) * distance,
                                shown, startColor, endColor,
                                angle - 90f + startRotation, angle - 90f + endRotation,
                                duration, postRunnable);
                    }
    }

}
