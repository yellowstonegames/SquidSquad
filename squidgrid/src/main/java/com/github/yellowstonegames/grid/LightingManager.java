package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.core.DescriptiveColor;

/**
 * A convenience class that makes dealing with multiple colored light sources easier.
 * All fields are public and documented to encourage their use alongside the API methods. The typical usage case for
 * this class is when a game has complex lighting needs that should be consolidated into one LightingManager per level,
 * where a level corresponds to a {@code char[][]}. After constructing a LightingManager with the resistances for a
 * level, you should add all light sources with their positions, either using {@link #addLight(int, int, Radiance)} or
 * by directly putting keys and values into {@link #lights}. Then you can calculate the visible cells once lighting is
 * considered (which may include distant lit cells with unseen but unobstructing cells between the viewer and the light)
 * using {@link #calculateFOV(Coord)}, which should be called every time the viewer moves. You can update the flicker
 * and strobe effects on all Radiance objects, which is typically done every frame, using {@link #update()} or
 * {@link #updateAll()} (updateAll() is for when there is no viewer), and once that update() call has been made you can
 * call {@link #draw(int[][])} to change a 2D int array that holds packed int colors (such as the backgrounds in a
 * GlyphMap). To place user-interface lighting effects that don't affect the in-game-world lights, you can use
 * {@link #updateUI(Coord, Radiance)}, which is called after {@link #update()} but before {@link #draw(int[][])}.
 * <br>
 * Created by Tommy Ettinger on 11/2/2018.
 */
public class LightingManager {

    /**
     * How light should spread; usually {@link Radius#CIRCLE} unless gameplay reasons need it to be SQUARE or DIAMOND.
     */
    public Radius radiusStrategy;
    /**
     * The 2D array of light-resistance values from 0.0f to 1.0f for each cell on the map, as produced by
     * {@link FOV#generateResistances(char[][])}.
     */
    public float[][] resistances;
    /**
     * What the "viewer" (as passed to {@link #calculateFOV(Coord)}) can see either nearby without light or because an
     * area in line-of-sight has light in it. Edited by {@link #calculateFOV(Coord)} and {@link  #update()}, but
     * not {@link #updateUI(Coord, Radiance)} (which is meant for effects that are purely user-interface).
     */
    public float[][] fovResult;
    /**
     * A 2D array of floats that are either 0.0f if a cell has an obstruction between it and the viewer, or greater than
     * 0.0f otherwise.
     */
    public float[][] losResult;
    /**
     * Temporary storage array used for calculations involving {@link #fovResult}; it sometimes may make sense for other
     * code to use this as temporary storage as well.
     */
    public float[][] tempFOV;

    public float[][] lightingStrength;
    public float[][] tempLightingStrength;
    /**
     * A 2D array that stores the color of light in each cell, as a packed Oklab int color. This 2D array is the size of
     * the map, as defined by {@link #resistances} initially and later available in {@link #width} and {@link #height}.
     */
    public int[][] colorLighting;
    /**
     * Temporary storage array used for calculations involving {@link #colorLighting}; it sometimes may make sense for
     * other code to use this as temporary storage as well. To make effective use of this field, you will probably need
     * to be reading the source for LightingManager.
     */
    public int[][] tempColorLighting;
    /**
     * Width of the 2D arrays used in this, as obtained from {@link #resistances}.
     */
    public int width;
    /**
     * Height of the 2D arrays used in this, as obtained from {@link #resistances}.
     */
    public int height;
    /**
     * The packed float color to mix background cells with when a cell has lighting and is within line-of-sight, but has
     * no background color to start with (its color is a packed Oklab int, {@link DescriptiveColor#TRANSPARENT}).
     */
    public int backgroundColor;
    /**
     * How far the viewer can see without light; defaults to 4.0f cells, and you are encouraged to change this member
     * field if the vision range changes after construction.
     */
    public float viewerRange;
    /**
     * A mapping from positions as {@link Coord} objects to {@link Radiance} objects that describe the color, lighting
     * radius, and changes over time of any in-game lights that should be shown on the map and change FOV. You can edit
     * this manually or by using {@link #moveLight(int, int, int, int)}, {@link #addLight(int, int, Radiance)}, and
     * {@link #removeLight(int, int)}.
     */
    public ObjectObjectOrderedMap<Coord, Radiance> lights;

    /**
     * A GreasedRegion that stores any cells that are in line-of-sight or are close enough to a cell in line-of-sight to
     * potentially cast light into such a cell. Depends on the highest {@link Radiance#range} in {@link #lights}.
     */
    public Region noticeable;

    /**
     * Unlikely to be used except during serialization; makes a LightingManager for a 20x20 fully visible level.
     * The viewer vision range will be 4.0f, and lights will use a circular shape.
     */
    public LightingManager()
    {
        this(new float[20][20], DescriptiveColor.BLACK, Radius.CIRCLE, 4.0f);
    }

    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations. This will use a solid
     * black background when it casts light on cells without existing lighting. The viewer vision range will be 4.0f, and
     * lights will use a circular shape.
     * @param resistance a resistance array as produced by DungeonUtility
     */
    public LightingManager(float[][] resistance)
    {
        this(resistance, DescriptiveColor.BLACK, Radius.CIRCLE, 4.0f);
    }
    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations.
     * @param resistance a resistance array as produced by DungeonUtility
     * @param backgroundColor the background color to use, as a color description
     * @param radiusStrategy the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                       of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingManager(float[][] resistance, String backgroundColor, Radius radiusStrategy, float viewerVisionRange)
    {
        this(resistance, DescriptiveColor.describeOklab(backgroundColor), radiusStrategy, viewerVisionRange);
    }
    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations.
     * @param resistance a resistance array as produced by DungeonUtility
     * @param backgroundColor the background color to use, as a packed Oklab int
     * @param radiusStrategy the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                       of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingManager(float[][] resistance, int backgroundColor, Radius radiusStrategy, float viewerVisionRange)
    {
        this.radiusStrategy = radiusStrategy;
        viewerRange = viewerVisionRange;
        this.backgroundColor = backgroundColor;
        resistances = resistance;
        width = resistances.length;
        height = resistances[0].length;
        fovResult = new float[width][height];
        tempFOV = new float[width][height];
        losResult = new float[width][height];
        colorLighting = ArrayTools.fill(DescriptiveColor.WHITE, width, height);
        lightingStrength = new float[width][height];
        tempColorLighting = new int[width][height];
        tempLightingStrength = new float[width][height];
        Coord.expandPoolTo(width, height);
        lights = new ObjectObjectOrderedMap<>(32);
        noticeable = new Region(width, height);
    }

    /**
     * Adds a Radiance as a light source at the given position. Overwrites any existing Radiance at the same position.
     * @param x the x-position to add the Radiance at
     * @param y the y-position to add the Radiance at
     * @param light a Radiance object that can have a changing radius, color, and various other effects on lighting
     * @return this for chaining
     */
    public LightingManager addLight(int x, int y, Radiance light)
    {
        return addLight(Coord.get(x, y), light);
    }
    /**
     * Adds a Radiance as a light source at the given position. Overwrites any existing Radiance at the same position.
     * @param position the position to add the Radiance at
     * @param light a Radiance object that can have a changing radius, color, and various other effects on lighting
     * @return this for chaining
     */
    public LightingManager addLight(Coord position, Radiance light)
    {
        lights.put(position, light);
        return this;
    }

    /**
     * Removes a Radiance as a light source from the given position, if any is present.
     * @param x the x-position to remove the Radiance from
     * @param y the y-position to remove the Radiance from
     * @return this for chaining
     */
    public LightingManager removeLight(int x, int y)
    {
        return removeLight(Coord.get(x, y));
    }
    /**
     * Removes a Radiance as a light source from the given position, if any is present.
     * @param position the position to remove the Radiance from
     * @return this for chaining
     */
    public LightingManager removeLight(Coord position)
    {
        lights.remove(position);
        return this;
    }
    /**
     * If a Radiance is present at oldX,oldY, this will move it to newX,newY and overwrite any existing Radiance at
     * newX,newY. If no Radiance is present at oldX,oldY, this does nothing.
     * @param oldX the x-position to move a Radiance from
     * @param oldY the y-position to move a Radiance from
     * @param newX the x-position to move a Radiance to
     * @param newY the y-position to move a Radiance to
     * @return this for chaining
     */
    public LightingManager moveLight(int oldX, int oldY, int newX, int newY)
    {
        return moveLight(Coord.get(oldX, oldY), Coord.get(newX, newY));
    }
    /**
     * If a Radiance is present at oldPosition, this will move it to newPosition and overwrite any existing Radiance at
     * newPosition. If no Radiance is present at oldPosition, this does nothing.
     * @param oldPosition the Coord to move a Radiance from
     * @param newPosition the Coord to move a Radiance to
     * @return this for chaining
     */
    public LightingManager moveLight(Coord oldPosition, Coord newPosition)
    {
        Radiance old = lights.get(oldPosition);
        if(old == null) return this;
        lights.alter(oldPosition, newPosition);
        return this;
    }

    /**
     * Gets the Radiance at the given position, if present, or null if there is no light source there.
     * @param x the x-position to look up
     * @param y the y-position to look up
     * @return the Radiance at the given position, or null if none is present there
     */
    public Radiance get(int x, int y)
    {
        return lights.get(Coord.get(x, y));
    }
    /**
     * Gets the Radiance at the given position, if present, or null if there is no light source there.
     * @param position the position to look up
     * @return the Radiance at the given position, or null if none is present there
     */
    public Radiance get(Coord position)
    {
        return lights.get(position);
    }

    /**
     * Edits {@link #colorLighting} by adding in and mixing the colors in {@link #tempColorLighting}, with the strength
     * of light in tempColorLighting boosted by flare (which can be any finite float greater than -1f, but is usually
     * from 0f to 1f when increasing strength).
     * Primarily used internally, but exposed so outside code can do the same things this class can.
     * @param flare boosts the effective strength of lighting in {@link #tempColorLighting}; usually from 0 to 1
     */
    public void mixColoredLighting(float flare)
    {
        int[][] basis = colorLighting, other = tempColorLighting;
        float[][] basisStrength = lightingStrength, otherStrength = tempLightingStrength;
        flare += 1f;
        float bs;
        int b;
        float os;
        int o;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0) {
                    if (resistances[x][y] >= 1) {
                        os = 0f;
                        if (y > 0) {
                            if ((losResult[x][y - 1] > 0 && otherStrength[x][y - 1] > 0 && resistances[x][y - 1] < 1)
                                    || (x > 0 && losResult[x - 1][y - 1] > 0 && otherStrength[x - 1][y - 1] > 0 && resistances[x - 1][y - 1] < 1)
                                    || (x < width - 1 && losResult[x + 1][y - 1] > 0 && otherStrength[x + 1][y - 1] > 0 && resistances[x + 1][y - 1] < 1)) {
                                os = otherStrength[x][y];
                            }
                        }
                        if (y < height - 1) {
                            if ((losResult[x][y + 1] > 0 && otherStrength[x][y + 1] > 0 && resistances[x][y + 1] < 1)
                                    || (x > 0 && losResult[x - 1][y + 1] > 0 && otherStrength[x - 1][y + 1] > 0 && resistances[x - 1][y + 1] < 1)
                                    || (x < width - 1 && losResult[x + 1][y + 1] > 0 && otherStrength[x + 1][y + 1] > 0 && resistances[x + 1][y + 1] < 1)) {
                                os = otherStrength[x][y];
                            }
                        }
                        if (x > 0 && losResult[x - 1][y] > 0 && otherStrength[x - 1][y] > 0 && resistances[x - 1][y] < 1) {
                            os = otherStrength[x][y];
                        }
                        if (x < width - 1 && losResult[x + 1][y] > 0 && otherStrength[x + 1][y] > 0 && resistances[x + 1][y] < 1) {
                            os = otherStrength[x][y];
                        }
                        if(os > 0f) o = other[x][y];
                        else continue;
                    } else {
                        os = otherStrength[x][y];
                        o = other[x][y];
                    }
                    if (os <= 0f || (o & 0xFE000000) == 0f)
                        continue;
                    bs = basisStrength[x][y];
                    b = basis[x][y];
                    if ((b & 0xFF) == 0xFF) {
                        basis[x][y] = o;
                        basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                    } else {
                        if ((o & 0xFF) != 0xFF) {
                            final int
                                    sL = (b & 0xFF), sA = (b >>> 8) & 0xFF, sB = (b >>> 16) & 0xFF, sAlpha = b & 0xFE000000,
                                    eL = (o & 0xFF), eA = (o >>> 8) & 0xFF, eB = (o >>> 16) & 0xFF, eAlpha = o >>> 25;
                            final float change = ((os - bs) * 0.5f + 0.5f) * eAlpha * 0.007874016f;
                            basis[x][y] = (((int) (sL + change * (eL - sL)) & 0xFF)
                                    | (((int) (sA + change * (eA - sA)) & 0xFF) << 8)
                                    | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                                    | sAlpha);
                            basisStrength[x][y] = Math.min(1.0f, bs + os * change * flare);
                        } else {
                            basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                        }
                    }
                }
            }
        }
    }

    /**
     * Edits {@link #colorLighting} by adding in and mixing the given color where the light strength in {@link #tempFOV}
     * is greater than 0, with that strength boosted by flare (which can be any finite float greater than -1f, but is
     * usually from 0f to 1f when increasing strength).
     * Primarily used internally, but exposed so outside code can do the same things this class can.
     * @param flare boosts the effective strength of lighting in {@link #tempColorLighting}; usually from 0 to 1
     */
    public void mixColoredLighting(float flare, int color)
    {
        final int[][] basis = colorLighting;
        final float[][] basisStrength = lightingStrength;
        final float[][] otherStrength = tempFOV;
        flare += 1f;
        float bs, os;
        int b, o;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0) {
                    if (resistances[x][y] >= 1) {
                        os = 0f;
                        if (y > 0) {
                            if ((losResult[x][y - 1] > 0 && otherStrength[x][y - 1] > 0 && resistances[x][y - 1] < 1)
                            || (x > 0 && losResult[x - 1][y - 1] > 0 && otherStrength[x - 1][y - 1] > 0 && resistances[x - 1][y - 1] < 1)
                            || (x < width - 1 && losResult[x + 1][y - 1] > 0 && otherStrength[x + 1][y - 1] > 0 && resistances[x + 1][y - 1] < 1)) {
                                os = otherStrength[x][y];
                            }
                        }
                        if (y < height - 1) {
                            if ((losResult[x][y + 1] > 0 && otherStrength[x][y + 1] > 0 && resistances[x][y + 1] < 1)
                                    || (x > 0 && losResult[x - 1][y + 1] > 0 && otherStrength[x - 1][y + 1] > 0 && resistances[x - 1][y + 1] < 1)
                                    || (x < width - 1 && losResult[x + 1][y + 1] > 0 && otherStrength[x + 1][y + 1] > 0 && resistances[x + 1][y + 1] < 1)) {
                                os = (float) otherStrength[x][y];
                            }
                        }
                        if (x > 0 && losResult[x - 1][y] > 0 && otherStrength[x - 1][y] > 0 && resistances[x - 1][y] < 1) {
                            os = (float) otherStrength[x][y];
                        }
                        if (x < width - 1 && losResult[x + 1][y] > 0 && otherStrength[x + 1][y] > 0 && resistances[x + 1][y] < 1) {
                            os = (float) otherStrength[x][y];
                        }
                        if(os > 0f) o = color;
                        else continue;
                    } else {
                        if((os = (float) otherStrength[x][y]) != 0) o = color;
                        else continue;
                    }
                    bs = basisStrength[x][y];
                    b = basis[x][y];
                    if ((b & 0xFF) == 0xFF) {
                        basis[x][y] = o;
                        basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                    } else {
                        if ((o & 0xFF) != 0xFF) {
                            final int
                                    sL = (b & 0xFF), sA = (b >>> 8) & 0xFF, sB = (b >>> 16) & 0xFF, sAlpha = b & 0xFE000000,
                                    eL = (o & 0xFF), eA = (o >>> 8) & 0xFF, eB = (o >>> 16) & 0xFF, eAlpha = o >>> 25;
                            final float change = ((os - bs) * 0.5f + 0.5f) * eAlpha * 0.007874016f;
                            basis[x][y] = (((int) (sL + change * (eL - sL)) & 0xFF)
                                    | (((int) (sA + change * (eA - sA)) & 0xFF) << 8)
                                    | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                                    | sAlpha);
                            basisStrength[x][y] = Math.min(1.0f, bs + os * change * flare);
                        } else {
                            basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                        }
                    }
                }
            }
        }
    }

    /**
     * Typically called every frame, this updates the flicker and strobe effects of Radiance objects and applies those
     * changes in lighting color and strength to the various fields of this LightingManager. This will only have an
     * effect if {@link #calculateFOV(Coord)} or {@link #calculateFOV(int, int)} was called during the last time the
     * viewer position changed; typically calculateFOV() only needs to be called once per move, while update() needs to
     * be called once per frame. This method is usually called before each call to {@link #draw(int[][])}, but other
     * code may be between the calls and may affect the lighting in customized ways.
     */
    public void update()
    {
        Radiance radiance;
        ArrayTools.fill(lightingStrength, 0f);
        ArrayTools.fill(colorLighting, DescriptiveColor.WHITE);
        final int sz = lights.size();
        Coord pos;
        for (int i = 0; i < sz; i++) {
            pos = lights.keyAt(i);
            if(!noticeable.contains(pos))
                continue;
            radiance = lights.getAt(i);
            if(radiance == null) continue;
            FOV.reuseFOV(resistances, tempFOV, pos.x, pos.y, radiance.currentRange());
            mixColoredLighting(radiance.flare, radiance.color);
        }
    }
    /**
     * Typically called every frame when there isn't a single viewer, this updates the flicker and strobe effects of
     * Radiance objects and applies those changes in lighting color and strength to the various fields of this
     * LightingManager. This method is usually called before each call to {@link #draw(int[][])}, but other code may
     * be between the calls and may affect the lighting in customized ways. This overload has no viewer, so all cells
     * are considered visible unless they are fully obstructed (solid cells behind walls, for example). Unlike update(),
     * this method does not need {@link #calculateFOV(Coord)} to be called for it to work properly.
     */
    public void updateAll()
    {
        Radiance radiance;
        for (int x = 0; x < width; x++) {
            PER_CELL:
            for (int y = 0; y < height; y++) {
                for (int xx = Math.max(0, x - 1), xi = 0; xi < 3 && xx < width; xi++, xx++) {
                    for (int yy = Math.max(0, y - 1), yi = 0; yi < 3 && yy < height; yi++, yy++) {
                        if(resistances[xx][yy] < 1.0f){
                            losResult[x][y] = 1.0f;
                            continue PER_CELL;
                        }
                    }
                }
            }
        }
        ArrayTools.fill(lightingStrength, 0f);
        ArrayTools.fill(colorLighting, DescriptiveColor.WHITE);
        final int sz = lights.size();
        Coord pos;
        for (int i = 0; i < sz; i++) {
            pos = lights.keyAt(i);
            radiance = lights.getAt(i);
            if(radiance == null) continue;
            FOV.reuseFOV(resistances, tempFOV, pos.x, pos.y, radiance.currentRange());
            mixColoredLighting(radiance.flare, radiance.color);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0f) {
                    fovResult[x][y] = Math.min(Math.max(losResult[x][y] + lightingStrength[x][y], 0), 1);
                }
            }
        }
    }
    /**
     * Updates the flicker and strobe effects of a Radiance object and applies the lighting from just that Radiance to
     * just the {@link #colorLighting} field, without changing FOV. This method is meant to be used for GUI effects that
     * aren't representative of something a character in the game could interact with. It is usually called after
     * {@link #update()} and before each call to {@link #draw(int[][])}, but other code may be between the calls
     * and may affect the lighting in customized ways.
     * @param pos the position of the light effect
     * @param radiance the Radiance to update standalone, which does not need to be already added to this 
     */
    public void updateUI(Coord pos, Radiance radiance)
    {
        updateUI(pos.x, pos.y, radiance);
    }

    /**
     * Updates the flicker and strobe effects of a Radiance object and applies the lighting from just that Radiance to
     * just the {@link #colorLighting} field, without changing FOV. This method is meant to be used for GUI effects that
     * aren't representative of something a character in the game could interact with. It is usually called after
     * {@link #update()} and before each call to {@link #draw(int[][])}, but other code may be between the calls
     * and may affect the lighting in customized ways.
     * @param lightX the x-position of the light effect
     * @param lightY the y-position of the light effect
     * @param radiance the Radiance to update standalone, which does not need to be already added to this 
     */
    public void updateUI(int lightX, int lightY, Radiance radiance)
    {
        FOV.reuseFOV(resistances, tempFOV, lightX, lightY, radiance.currentRange());
        mixColoredLighting(radiance.flare, radiance.color);
    }

    /**
     * Given a 2D array that should hold RGBA8888 int colors, fills the 2D array with different colors based on what
     * lights are present in line of sight of the viewer and the various flicker or strobe effects that Radiance light
     * sources can do. You should usually call {@link #update()} before each call to draw(), but you may want to make
     * custom changes to the lighting in between those two calls (that is the only place those changes will be noticed).
     * A common use for this in text-based games uses a GlyphMap's backgrounds field as the parameter.
     * @param backgrounds a 2D int array, which will be modified in-place
     */
    public void draw(int[][] backgrounds)
    {
        int current;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0f && fovResult[x][y] > 0.0f) {
                        current = backgrounds[x][y];
                        if(current == 0f)
                            current = backgroundColor;
                        backgrounds[x][y] = DescriptiveColor.toRGBA8888(DescriptiveColor.lerpColorsBlended(current,
                                colorLighting[x][y], lightingStrength[x][y] * 0.4f));
                }
            }
        }
    }
    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. Sets
     * {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the given viewer position and any lights
     * in {@link #lights}.
     * @param viewer the position of the player or other viewer
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public float[][] calculateFOV(Coord viewer)
    {
        return calculateFOV(viewer.x, viewer.y);
    }

    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. Sets
     * {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the given viewer position and any lights
     * in {@link #lights}.
     * @param viewerX the x-position of the player or other viewer
     * @param viewerY the y-position of the player or other viewer
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public float[][] calculateFOV(int viewerX, int viewerY)
    {
        return calculateFOV(viewerX, viewerY, 0, 0, width, height);
    }

    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. This overload
     * allows the area this processes to be restricted to a rectangle between {@code minX} and {@code maxX} and between
     * {@code minY} and {@code maxY}, ignoring any lights outside that area (typically because they are a long way out
     * from the map's shown area). Sets {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the
     * given viewer position and any lights in {@link #lights}.
     * @param viewerX the x-position of the player or other viewer
     * @param viewerY the y-position of the player or other viewer
     * @param minX inclusive lower bound on x to calculate
     * @param minY inclusive lower bound on y to calculate
     * @param maxX exclusive upper bound on x to calculate
     * @param maxY exclusive upper bound on y to calculate
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public float[][] calculateFOV(int viewerX, int viewerY, int minX, int minY, int maxX, int maxY)
    {
        Radiance radiance;
        minX = Math.min(Math.max(minX, 0), width);
        maxX = Math.min(Math.max(maxX, 0), width);
        minY = Math.min(Math.max(minY, 0), height);
        maxY = Math.min(Math.max(maxY, 0), height);
        FOV.reuseFOV(resistances, fovResult, viewerX, viewerY, viewerRange, radiusStrategy);
        ArrayTools.fill(lightingStrength, 0f);
        ArrayTools.fill(colorLighting, DescriptiveColor.WHITE);
        final int sz = lights.size();
        float maxRange = 0, range;
        Coord pos;
        for (int i = 0; i < sz; i++) {
            pos = lights.keyAt(i);
            radiance = lights.getAt(i);
            if(radiance == null) continue;
            range = radiance.range;
            if(range > maxRange &&
                    pos.x + range >= minX && pos.x - range < maxX && pos.y + range >= minY && pos.y - range < maxY) 
                maxRange = range;
        }
        FOV.reuseLOS(resistances, losResult, viewerX, viewerY, minX, minY, maxX, maxY);
        noticeable.refill(losResult, 0.0001f, Float.POSITIVE_INFINITY).expand8way((int) Math.ceil(maxRange));
        for (int i = 0; i < sz; i++) {
            pos = lights.keyAt(i);
            if(!noticeable.contains(pos))
                continue;
            radiance = lights.getAt(i);
            if(radiance == null) continue;
            FOV.reuseFOV(resistances, tempFOV, pos.x, pos.y, radiance.range);
            mixColoredLighting(radiance.flare, radiance.color);
        }
        for (int x = Math.max(0, minX); x < maxX && x < width; x++) {
            for (int y = Math.max(0, minY); y < maxY && y < height; y++) {
                if (losResult[x][y] > 0.0f) {
                    fovResult[x][y] = Math.min(Math.max(fovResult[x][y] + lightingStrength[x][y], 0), 1);
                }
            }
        }
        return fovResult;
    }

}
