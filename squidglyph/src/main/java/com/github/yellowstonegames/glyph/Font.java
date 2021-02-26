package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.LongList;
import com.github.tommyettinger.ds.support.BitConversion;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import regexodus.Category;

public class Font implements Disposable {
    public IntObjectMap<TextureRegion> mapping;
    public Texture parentTexture;
    public boolean isMSDF, isMono;
    public float msdfCrispness = 1f;
    public float cellWidth = 1f, cellHeight = 1f, originalCellWidth = 1f, originalCellHeight = 1f,
            scaleX = 1f, scaleY = 1f;
    public static final long BOLD = 1L << 30, OBLIQUE = 1L << 29,
            UNDERLINE = 1L << 28, STRIKETHROUGH = 1L << 27,
            SUBSCRIPT = 1L << 25, MIDSCRIPT = 2L << 25, SUPERSCRIPT = 3L << 25;

    private final float[] vertices = new float[20];

    private final LongList tempGlyphs = new LongList(128);

    public static final String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "uniform mat4 u_projTrans;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "	v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "	v_color.a = v_color.a * (255.0/254.0);\n"
            + "	v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "}\n";
    public static final String msdfFragmentShader =  "#ifdef GL_ES\n"
            + "	precision mediump float;\n"
            + "	precision mediump int;\n"
            + "#endif\n"
            + "\n"
            + "uniform sampler2D u_texture;\n"
            + "uniform float u_smoothing;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "  vec3 sdf = texture2D(u_texture, v_texCoords).rgb;\n"
            + "  gl_FragColor = vec4(v_color.rgb, clamp((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) * u_smoothing + 0.5, 0.0, 1.0) * v_color.a);\n"
            + "}\n";
    public ShaderProgram shader = null;

    private static int indexAfter(String text, String search, int from){
        return ((from = text.indexOf(search, from)) < 0 ? text.length() : from + search.length());
    }
    public Font(String fntName, String textureName, boolean isMSDF){
        this(fntName, textureName, isMSDF, 0f, 0f, 0f, 0f);
    }

    public Font(Font toCopy){
        isMSDF = toCopy.isMSDF;
        isMono = toCopy.isMono;
        msdfCrispness = toCopy.msdfCrispness;
        parentTexture = toCopy.parentTexture;
        cellWidth = toCopy.cellWidth;
        cellHeight = toCopy.cellHeight;
        scaleX = toCopy.scaleX;
        scaleY = toCopy.scaleY;
        originalCellWidth = toCopy.originalCellWidth;
        originalCellHeight = toCopy.originalCellHeight;
        mapping = new IntObjectMap<>(toCopy.mapping.size());
        for(IntObjectMap.Entry<TextureRegion> e : toCopy.mapping){
            if(e.value == null) continue;
            mapping.put(e.key, new TextureRegion(parentTexture, e.value.getU(), e.value.getV(), e.value.getU2(), e.value.getV2()));
        }
        mapping.defaultValue = mapping.getOrDefault(' ', mapping.get(0));
        if(toCopy.shader != null)
            shader = new ShaderProgram(toCopy.shader.getVertexShaderSource(),
                    toCopy.shader.getFragmentShaderSource());
    }

    public Font(String fntName, String textureName, boolean isMSDF,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.isMSDF = isMSDF;
        if(isMSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if(!shader.isCompiled())
                Gdx.app.error("squidglyph", "Font shader failed to compile: " + shader.getLog());
        }
        FileHandle fntHandle, textureHandle;
        String fnt;
        if((textureHandle = Gdx.files.internal(textureName)).exists()
                || (textureHandle = Gdx.files.classpath(textureName)).exists()) {
            parentTexture = new Texture(textureHandle);
            if(isMSDF) {
                parentTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        }
        else {
            throw new RuntimeException("Missing texture file: " + textureName);
        }

        if((fntHandle = Gdx.files.internal(fntName)).exists()
                || (fntHandle = Gdx.files.classpath(fntName)).exists()) {
            fnt = fntHandle.readString("UTF8");
        }
        else {
            throw new RuntimeException("Missing font file: " + fntName);
        }
        int idx = indexAfter(fnt, "\nchars count=", 0);
        int size = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, "\nchar id=", idx));
        mapping = new IntObjectMap<>(size);
        float minWidth = Float.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            int c = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, " x=", idx));
            int x = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, " y=", idx));
            int y = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, " width=", idx));
            int h = DigitTools.intFromDec(fnt, idx = indexAfter(fnt, " height=", idx), idx = indexAfter(fnt, " xadvance=", idx));
            int w = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, "\nchar id=", idx));
            x += xAdjust;
            y += yAdjust;
            w += widthAdjust;
            h += heightAdjust;
            minWidth = Math.min(minWidth, w);
            cellWidth = Math.max(w, cellWidth);
            cellHeight = Math.max(h, cellHeight);
            mapping.put(c, new TextureRegion(parentTexture, x, y, w, h));
        }
        mapping.defaultValue = mapping.getOrDefault(' ', mapping.get(0));
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
        isMono = minWidth == cellWidth;
    }
    public Font scale(float horizontal, float vertical) {
        scaleX *= horizontal;
        scaleY *= vertical;
        cellWidth *= horizontal;
        cellHeight *= vertical;
        return this;
    }
    public Font scaleTo(float width, float height) {
        scaleX = width / originalCellWidth;
        scaleY = height / originalCellHeight;
        cellWidth  = width;
        cellHeight = height;
        return this;
    }

    /**
     * Must be called before drawing anything with an MSDF font; does not need to be called for other fonts unless you
     * are mixing them with MSDF fonts or other shaders. This also resets the Batch color to white, in case it had been
     * left with a different setting before.
     * @param batch the Batch to instruct to use the appropriate shader for this font; often a SpriteBatch
     */
    public void enableShader(Batch batch) {
        if(isMSDF) {
            if (batch.getShader() != shader) {
                batch.setShader(shader);
                shader.setUniformf("u_smoothing", 5f * msdfCrispness * (cellHeight / originalCellHeight + cellWidth / originalCellWidth));
//            msdfShader.setUniformf("u_smoothing", 0.09375f * msdfCrispness * cellHeight);
            }
        }
        else {
            batch.setShader(null);
        }
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);
    }

    /**
     * Draws the specified text at the given x,y position (in world space) with a white foreground.
     * @param batch typically a SpriteBatch
     * @param text typically a String, but this can also be a StringBuilder or some custom class
     * @param x the x position in world space to start drawing the text at (lower left corner)
     * @param y the y position in world space to start drawing the text at (lower left corner)
     */
    public void drawText(Batch batch, CharSequence text, float x, float y) {
        drawText(batch, text, x, y, -2);
    }
    /**
     * Draws the specified text at the given x,y position (in world space) with the given foreground color.
     * @param batch typically a SpriteBatch
     * @param text typically a String, but this can also be a StringBuilder or some custom class
     * @param x the x position in world space to start drawing the text at (lower left corner)
     * @param y the y position in world space to start drawing the text at (lower left corner)
     * @param color an int color; typically this is RGBA, but custom shaders or Batches can use other kinds of color
     */
    public void drawText(Batch batch, CharSequence text, float x, float y, int color) {
        batch.setPackedColor(BitConversion.reversedIntBitsToFloat(color & -2));
        TextureRegion current;
        for (int i = 0, n = text.length(); i < n; i++) {
            batch.draw(current = mapping.get(text.charAt(i)), x, y, current.getRegionWidth(), current.getRegionHeight());
            x += current.getRegionWidth();
        }
    }

    /**
     * Draws a grid made of rectangular blocks of int colors (typically RGBA) at the given x,y position in world space.
     * The {@code colors} parameter should be a rectangular 2D array, and because any colors that are the default int
     * value {@code 0} will be treated as transparent RGBA values, if a value is not assigned to a slot in the array
     * then nothing will be drawn there. This is usually called before other methods that draw foreground text.
     * <br>
     * Internally, this is substantially more complex than the other drawing methods; it uses
     * {@link Batch#draw(Texture, float[], int, int)} to draw each rectangle with minimal overhead, and this also means
     * it is unaffected by the batch color. If you want to alter the colors using a shader, the shader will receive each
     * color in {@code colors} as its {@code a_color} attribute, the same as if it was passed via the batch color.
     * @param batch typically a SpriteBatch
     * @param colors a 2D rectangular array of int colors (typically RGBA)
     * @param x the x position in world space to draw the text at (lower left corner)
     * @param y the y position in world space to draw the text at (lower left corner)
     */
    public void drawBlocks(Batch batch, int[][] colors, float x, float y) {
        final TextureRegion block = mapping.get(0);
        if(block == null) return;
        final float u = block.getU() + (block.getU2() - block.getU()) * 0.25f,
                v = block.getV() + (block.getV2() - block.getV()) * 0.25f,
                u2 = block.getU2() - (block.getU2() - block.getU()) * 0.25f,
                v2 = block.getV2() - (block.getV2() - block.getV()) * 0.25f;
        vertices[0] = x;
        vertices[1] = y;
        //vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;

        vertices[5] = x;
        vertices[6] = y + cellHeight;
        //vertices[7] = color;
        vertices[8] = u;
        vertices[9] = v2;

        vertices[10] = x + cellWidth;
        vertices[11] = y + cellHeight;
        //vertices[12] = color;
        vertices[13] = u2;
        vertices[14] = v2;

        vertices[15] = x + cellWidth;
        vertices[16] = y;
        //vertices[17] = color;
        vertices[18] = u2;
        vertices[19] = v;
        for (int xi = 0, xn = colors.length, yn = colors[0].length; xi < xn; xi++) {
            for (int yi = 0; yi < yn; yi++) {
                vertices[2] = vertices[7] = vertices[12] = vertices[17] =
                        BitConversion.reversedIntBitsToFloat(colors[xi][yi] & -2);
                batch.draw(parentTexture, vertices, 0, 20);
                vertices[1] = vertices[16] += cellHeight;
                vertices[6] = vertices[11] += cellHeight;
            }
            vertices[0] = vertices[5] += cellWidth;
            vertices[10] = vertices[15] += cellWidth;
            vertices[1] = vertices[16] = y;
            vertices[6] = vertices[11] = y + cellHeight;
        }
    }

    /**
     * Draws the specified text at the given x,y position (in world space), parsing an extension of libGDX markup
     * and using it to determine color, size, position, shape, strikethrough, underline, and case of the given
     * CharSequence. The text drawn will start as white, with the normal size as by {@link #cellWidth} and
     * {@link #cellHeight}, normal case, and without bold, italic, superscript, subscript, strikethrough, or
     * underline. Markup starts with {@code [}; the next non-letter character determines what that piece of markup
     * toggles. Markup this knows:
     * <ul>
     *     <li>{@code [[} escapes a literal left bracket.</li>
     *     <li>{@code []} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is an RGBA8888 int color with optional alpha, changes the color.</li>
     *     <li>{@code [|some description]}, where "some description" is a lower-case color description as per
     *     {@link com.github.yellowstonegames.core.DescriptiveColor#describeOklab(CharSequence, int, int)},
     *     changes the color. You don't have to include the "|" character, since this is the default.</li>
     * </ul>
     * <br>
     * Parsing markup for a full screen every frame typically isn't necessary, and you may want to store the most recent
     * glyphs by calling {@link #markup(String, LongList)} and render its result with
     * {@link #drawGlyphs(Batch, LongList, float, float)} every frame.
     * @param batch typically a SpriteBatch
     * @param text typically a String with markup, but this can also be a StringBuilder or some custom class
     * @param x the x position in world space to start drawing the text at (lower left corner)
     * @param y the y position in world space to start drawing the text at (lower left corner)
     * @return the number of glyphs drawn
     */
    public int drawMarkupText(Batch batch, String text, float x, float y) {
        tempGlyphs.clear();
        markup(text, tempGlyphs);
        final int n = tempGlyphs.size();
        for (int i = 0; i < n; i++) {
            x += drawGlyph(batch, tempGlyphs.get(i), x, y);
        }
        return n;
    }

    /**
     * Draws the specified LongList of glyphs with a Batch at a given x, y position, drawing the full LongList.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned by {@link #markup(String, LongList)}
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     * @return the number of glyphs drawn
     */
    public int drawGlyphs(Batch batch, LongList glyphs, float x, float y) {
        return drawGlyphs(batch, glyphs, 0, glyphs.size(), x, y);
    }
    /**
     * Draws the specified LongList of glyphs with a Batch at a given x, y position, starting at offset in glyphs and
     * attempting to draw a count of glyphs equal to length. This may draw fewer glyphs if the LongList is exhausted.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned by {@link #markup(String, LongList)}
     * @param offset the first position in glyphs to use
     * @param length how many items in glyphs to try to draw
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     * @return the number of glyphs drawn
     */
    public int drawGlyphs(Batch batch, LongList glyphs, int offset, int length, float x, float y) {
        int drawn = 0;
        for (int i = offset, n = glyphs.size(); i < n && drawn < length; i++, drawn++) {
            x += drawGlyph(batch, glyphs.get(i), x, y);
        }
        return drawn;
    }

    /**
     * Draws the specified glyph with a Batch at the given x, y position. The glyph contains multiple types of data all
     * packed into one {@code long}: the bottom 16 bits store a {@code char}, the roughly 16 bits above that store
     * formatting (bold, underline, superscript, etc.), and the remaining upper 32 bits store color as RGBA.
     * @param batch typically a SpriteBatch
     * @param glyph a long storing a char, format, and color; typically part of a longer formatted text as a LongList
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     */
    public float drawGlyph(Batch batch, long glyph, float x, float y) {
        float x0 = 0f, x1 = 0f, x2 = 0f, x3 = 0f;
        float y0 = 0f, y1 = 0f, y2 = 0f, y3 = 0f;
        float color = BitConversion.reversedIntBitsToFloat((int) (glyph >>> 32) & -2);
        final float xPx = 1f, xPx2 = 2f;
        TextureRegion tr = mapping.get((char) glyph);
        if (tr == null) return 0f;
        float u, v, u2, v2;
        u = tr.getU();
        v = tr.getV();
        u2 = tr.getU2();
        v2 = tr.getV2();
        float w = tr.getRegionWidth() * scaleX, changedW = w, h = tr.getRegionHeight() * scaleY;
        if ((glyph & OBLIQUE) != 0L) {
            x0 += cellHeight * 0.2f;
            x1 -= cellHeight * 0.2f;
            x2 -= cellHeight * 0.2f;
            x3 += cellHeight * 0.2f;
        }
        final long script = (glyph & SUPERSCRIPT);
        if (script == SUPERSCRIPT) {
            x2 -= w * 0.5f;
            x3 -= w * 0.5f;
            y1 += h * 0.5f;
            y2 += h * 0.5f;
            if(!isMono)
                changedW *= 0.5f;
        }
        else if (script == SUBSCRIPT) {
            x2 -= w * 0.5f;
            x3 -= w * 0.5f;
            y0 -= h * 0.5f;
            y3 -= h * 0.5f;
            if(!isMono)
                changedW *= 0.5f;
        }
        else if(script == MIDSCRIPT) {
            x2 -= w * 0.5f;
            x3 -= w * 0.5f;
            y0 -= h * 0.25f;
            y1 += h * 0.25f;
            y2 += h * 0.25f;
            y3 -= h * 0.25f;
            if(!isMono)
                changedW *= 0.5f;
        }

        vertices[0] = x + x0;
        vertices[1] = y + y0 + h;
        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;

        vertices[5] = x + x1;
        vertices[6] = y + y1;
        vertices[7] = color;
        vertices[8] = u;
        vertices[9] = v2;

        vertices[10] = x + x2 + w;
        vertices[11] = y + y2;
        vertices[12] = color;
        vertices[13] = u2;
        vertices[14] = v2;

        vertices[15] = x + x3 + w;
        vertices[16] = y + y3 + h;
        vertices[17] = color;
        vertices[18] = u2;
        vertices[19] = v;
        batch.draw(parentTexture, vertices, 0, 20);
        if ((glyph & BOLD) != 0L) {
            vertices[0] += xPx;
            vertices[5] += xPx;
            vertices[10] += xPx;
            vertices[15] += xPx;
            batch.draw(parentTexture, vertices, 0, 20);
            vertices[0] -= xPx2;
            vertices[5] -= xPx2;
            vertices[10] -= xPx2;
            vertices[15] -= xPx2;
            batch.draw(parentTexture, vertices, 0, 20);
        }
        if ((glyph & UNDERLINE) != 0L) {
            final TextureRegion under = mapping.get('_');
            if (under != null) {
                final float underU = under.getU() + (under.getU2() - under.getU()) * 0.375f,
                        underV = under.getV(),
                        underU2 = under.getU2() - (under.getU2() - under.getU()) * 0.375f,
                        underV2 = under.getV2();
                vertices[0] = x;
                vertices[1] = y + h;
                vertices[2] = color;
                vertices[3] = underU;
                vertices[4] = underV;

                vertices[5] = x;
                vertices[6] = y;
                vertices[7] = color;
                vertices[8] = underU;
                vertices[9] = underV2;

                vertices[10] = x + w;
                vertices[11] = y;
                vertices[12] = color;
                vertices[13] = underU2;
                vertices[14] = underV2;

                vertices[15] = x + w;
                vertices[16] = y + h;
                vertices[17] = color;
                vertices[18] = underU2;
                vertices[19] = underV;
                batch.draw(parentTexture, vertices, 0, 20);
            }
        }
        if ((glyph & STRIKETHROUGH) != 0L) {
            final TextureRegion dash = mapping.get('-');
            if (dash != null) {
                final float dashU = dash.getU() + (dash.getU2() - dash.getU()) * 0.375f,
                        dashV = dash.getV(),
                        dashU2 = dash.getU2() - (dash.getU2() - dash.getU()) * 0.375f,
                        dashV2 = dash.getV2();

                vertices[0] = x;
                vertices[1] = y + h;
                vertices[2] = color;
                vertices[3] = dashU;
                vertices[4] = dashV;

                vertices[5] = x;
                vertices[6] = y;
                vertices[7] = color;
                vertices[8] = dashU;
                vertices[9] = dashV2;

                vertices[10] = x + w;
                vertices[11] = y;
                vertices[12] = color;
                vertices[13] = dashU2;
                vertices[14] = dashV2;

                vertices[15] = x + w;
                vertices[16] = y + h;
                vertices[17] = color;
                vertices[18] = dashU2;
                vertices[19] = dashV;
                batch.draw(parentTexture, vertices, 0, 20);
            }
        }
        return changedW;
    }

    /**
     * Reads markup from text, along with the chars to receive markup, processes it, and appends into appendTo as a
     * series of {@code long} glyphs. Parses an extension of libGDX markup and uses it to determine color, size,
     * position, shape, strikethrough, underline, and case of the given CharSequence. The text drawn will start as
     * white, with the normal size as by {@link #cellWidth} and {@link #cellHeight}, normal case, and without bold,
     * italic, superscript, subscript, strikethrough, or underline. Markup starts with {@code [}; the next non-letter
     * character determines what that piece of markup toggles. Markup this knows:
     * <ul>
     *     <li>{@code [[} escapes a literal left bracket.</li>
     *     <li>{@code []} clears all markup to the initial state without any applied.</li>
     *     <li>{@code [*]} toggles bold mode.</li>
     *     <li>{@code [/]} toggles italic (technically, oblique) mode.</li>
     *     <li>{@code [^]} toggles superscript mode (and turns off subscript or midscript mode).</li>
     *     <li>{@code [=]} toggles midscript mode (and turns off superscript or subscript mode).</li>
     *     <li>{@code [.]} toggles subscript mode (and turns off superscript or midscript mode).</li>
     *     <li>{@code [_]} toggles underline mode.</li>
     *     <li>{@code [~]} toggles strikethrough mode.</li>
     *     <li>{@code [!]} toggles all upper case mode.</li>
     *     <li>{@code [,]} toggles all lower case mode.</li>
     *     <li>{@code [;]} toggles capitalize each word mode.</li>
     *     <li>{@code [#HHHHHHHH]}, where HHHHHHHH is an RGBA8888 int color with optional alpha, changes the color.</li>
     *     <li>{@code [|some description]}, where "some description" is a lower-case color description as per
     *     {@link com.github.yellowstonegames.core.DescriptiveColor#describeOklab(CharSequence, int, int)},
     *     changes the color. You don't have to include the "|" character, since this is the default.</li>
     * </ul>

     * @param text text with markup
     * @param appendTo a LongList that stores color, font formatting, and a char in each long
     * @return appendTo, for chaining
     */
    public LongList markup(String text, LongList appendTo) {
        boolean capitalize = false, previousWasLetter = false,
                capsLock = false, lowerCase = false;
        int c;
        long color = 0xFFFFFFFF00000000L;
        final long COLOR_MASK = color;
        long current = color;
        for (int i = 0, n = text.length(); i < n; i++) {
            if(text.charAt(i) == '['){
                if(++i < n && (c = text.charAt(i)) != '['){
                    if(c == ']'){
                        color = 0xFFFFFFFF00000000L;
                        current = color;
                        capitalize = false;
                        capsLock = false;
                        lowerCase = false;
                        continue;
                    }
                    int len = text.indexOf(']', i) - i;
                    switch (c) {
                        case '*':
                            current ^= BOLD;
                            break;
                        case '/':
                            current ^= OBLIQUE;
                            break;
                        case '^':
                            if((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if((current & SUPERSCRIPT) == MIDSCRIPT)
                                current &= ~MIDSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | MIDSCRIPT;
                            break;
                        case '_':
                            current ^= UNDERLINE;
                            break;
                        case '~':
                            current ^= STRIKETHROUGH;
                            break;
                        case ';':
                            capitalize = !capitalize;
                            capsLock = false;
                            lowerCase = false;
                            break;
                        case '!':
                            capsLock = !capsLock;
                            capitalize = false;
                            lowerCase = false;
                            break;
                        case ',':
                            lowerCase = !lowerCase;
                            capitalize = false;
                            capsLock = false;
                            break;
                        case '#':
                            if (len >= 7 && len < 9)
                                color = DigitTools.longFromHex(text, i + 1, i + 7) << 40 | 0x000000FF00000000L;
                            else if (len >= 9)
                                color = DigitTools.longFromHex(text, i + 1, i + 9) << 32;
                            else
                                color = COLOR_MASK;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        case '|':
                            color = (long) DescriptiveColor.toRGBA8888(DescriptiveColor.describeOklab(text, i + 1, len)) << 32;
                            current = (current & ~COLOR_MASK) | color;
                            break;
                        default:
                            if (c >= 'a' && c <= 'z')
                            {
                                color = (long) DescriptiveColor.toRGBA8888(DescriptiveColor.describeOklab(text, i, len)) << 32;
                            }
                            else {
                                // attempt to look up a known Color name from Colors
                                Color gdxColor = Colors.get(text.substring(i, len));
                                if(gdxColor == null) color = -1L << 32; // opaque white
                                else color = (long) Color.rgba8888(gdxColor) << 32;
                            }
                            current = (current & ~COLOR_MASK) | color;
                    }
                    i += len;
                }
                else {
                    appendTo.add(current | '[');
                }
            } else {
                char ch = text.charAt(i);
                if(Category.Ll.contains(ch)) { //we need to use this instead of Character.isLowerCase() because of GWT.
                    if((capitalize && !previousWasLetter) || capsLock) {
                        ch = Category.caseUp(ch); // again, GWT's toUpperCase() is not ideal.
                    }
                    previousWasLetter = true;
                }
                else if(Category.Lu.contains(ch)) { //we need to use this instead of Character.isUpperCase() because of GWT.
                    if((capitalize && previousWasLetter) || lowerCase) {
                        ch = Category.caseFold(ch); // yet again, GWT's toLowerCase() is not ideal.
                    }
                    previousWasLetter = true;
                }
                else {
                    previousWasLetter = false;
                }
                appendTo.add(current | ch);
            }
        }
        return appendTo;
    }


    /**
     * Releases all resources of this object.
     */
    @Override
    public void dispose() {
        mapping = null;
        parentTexture.dispose();
    }
}
