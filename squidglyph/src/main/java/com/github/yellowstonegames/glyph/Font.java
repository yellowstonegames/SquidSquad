package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.support.BitConversion;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import regexodus.Category;

public class Font implements Disposable {
    public IntObjectMap<TextureRegion> mapping;
    public Texture parentTexture;
    public boolean isMSDF;
    public float msdfCrispness = 1f;
    public float cellWidth = 1f, cellHeight = 1f, originalCellWidth = 1f, originalCellHeight = 1f;

    private final float[] vertices = new float[20];

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
        msdfCrispness = toCopy.msdfCrispness;
        parentTexture = toCopy.parentTexture;
        cellWidth = toCopy.cellWidth;
        cellHeight = toCopy.cellHeight;
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
        for (int i = 0; i < size; i++) {
            int c = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, " x=", idx));
            int x = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, " y=", idx));
            int y = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, " width=", idx));
            int w = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, " height=", idx));
            int h = DigitTools.intFromDec(fnt, idx, idx = indexAfter(fnt, "\nchar id=", idx));
            x += xAdjust;
            y += yAdjust;
            w += widthAdjust;
            h += heightAdjust;
            cellWidth = w;
            cellHeight = h;
            mapping.put(c, new TextureRegion(parentTexture, x, y, w, h));
        }
        mapping.defaultValue = mapping.getOrDefault(' ', mapping.get(0));
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
    }
    public Font scale(float horizontal, float vertical) {
        cellWidth *= horizontal;
        cellHeight *= vertical;
//        msdfCrispness = (msdfCrispness - 1f) * vertical + 1f;
        return this;
    }
    public Font scaleTo(float width, float height) {
//        msdfCrispness = (msdfCrispness - 1f) * (height / cellHeight) + 1f;
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
        for (int i = 0, n = text.length(); i < n; i++, x += cellWidth) {
            batch.draw(mapping.get(text.charAt(i)), x, y, cellWidth, cellHeight);
        }
    }

    /**
     * Draws a single char with a foreground color, encoded in one long, at the given x,y position in world space.
     * The {@code colorGlyph} parameter is a long that contains two separate values in its upper 32 and lower 32 bits;
     * the upper 32 bits store a color, usually as RGBA, while the lower 32 bits store a codepoint, or more commonly, a
     * char that will be drawn with that color. Some code may return long values with this format to pass around chars
     * with colors without creating objects to store them.
     * @param batch typically a SpriteBatch
     * @param colorGlyph a long encoding a color (usually RGBA) in its upper 32 bits, and a char or codepoint in its lower 32 bits.
     * @param x the x position in world space to draw the text at (lower left corner)
     * @param y the y position in world space to draw the text at (lower left corner)
     */
    public void drawGlyph(Batch batch, long colorGlyph, float x, float y) {
        batch.setPackedColor(BitConversion.reversedIntBitsToFloat((int) (colorGlyph >>> 32) & -2));
        batch.draw(mapping.get((int) colorGlyph), x, y, cellWidth, cellHeight);
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
        assert block != null;
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
     *     {@link com.github.yellowstonegames.core.DescriptiveColor#describeIPT(CharSequence, int, int)},
     *     changes the color. You don't have to include the "|" character, since this is the default.</li>
     * </ul>
     * @param batch typically a SpriteBatch
     * @param text typically a String with markup, but this can also be a StringBuilder or some custom class
     * @param x the x position in world space to start drawing the text at (lower left corner)
     * @param y the y position in world space to start drawing the text at (lower left corner)
     */
    public void drawMarkupText(Batch batch, String text, float x, float y) {
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);
        boolean bold = false, oblique = false,
                underline = false, strikethrough = false,
                superscript = false, midscript = false, subscript = false,
                capitalize = false, previousWasLetter = false,
                capsLock = false, lowerCase = false;
        float x0 = 0f, x1 = 0f, x2 = 0f, x3 = 0f;
        float y0 = 0f, y1 = 0f, y2 = 0f, y3 = 0f;
        int c;
        float color = Color.WHITE_FLOAT_BITS, d = 0f;
        final float xPx = 1f, xPx2 = xPx + xPx;
//        final float yPx = cellHeight / (originalCellHeight * parentTexture.getHeight());
        final TextureRegion under = mapping.get('_');
        assert under != null;
        final TextureRegion dash = mapping.get('-');
        assert dash != null;
        TextureRegion tr;
        final float underU = under.getU() + (under.getU2() - under.getU()) * 0.375f,
                underV = under.getV(),
                underU2 = under.getU2() - (under.getU2() - under.getU()) * 0.375f,
                underV2 = under.getV2();
        final float dashU = dash.getU() + (dash.getU2() - dash.getU()) * 0.375f,
                dashV = dash.getV(),
                dashU2 = dash.getU2() - (dash.getU2() - dash.getU()) * 0.375f,
                dashV2 = dash.getV2();
        float u, v, u2, v2;
        u = under.getU() + (under.getU2() - under.getU()) * 0.25f;
        v = under.getV() + (under.getV2() - under.getV()) * 0.25f;
        u2 = under.getU2() - (under.getU2() - under.getU()) * 0.25f;
        v2 = under.getV2() - (under.getV2() - under.getV()) * 0.25f;
        vertices[0] = x;
        vertices[1] = y + cellHeight;
        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;

        vertices[5] = x;
        vertices[6] = y;
        vertices[7] = color;
        vertices[8] = u;
        vertices[9] = v2;

        vertices[10] = x + cellWidth;
        vertices[11] = y;
        vertices[12] = color;
        vertices[13] = u2;
        vertices[14] = v2;

        vertices[15] = x + cellWidth;
        vertices[16] = y + cellHeight;
        vertices[17] = color;
        vertices[18] = u2;
        vertices[19] = v;
        for (int i = 0, n = text.length(); i < n; i++, d += cellWidth) {
            if(text.charAt(i) == '['){
                if(++i < n && (c = text.charAt(i)) != '['){
                    if(c == ']'){
                        color = (Color.WHITE_FLOAT_BITS);
                        if(bold) {
                            bold = false;
//                            x0 += cellWidth * 0.125f;
//                            x1 += cellWidth * 0.125f;
//                            x2 -= cellWidth * 0.125f;
//                            x3 -= cellWidth * 0.125f;
//                            y0 -= cellHeight * 0.09375f;
//                            y1 += cellHeight * 0.09375f;
//                            y2 += cellHeight * 0.09375f;
//                            y3 -= cellHeight * 0.09375f;
                        }
                        if(oblique) {
                            oblique = false;
                            x0 -= cellWidth * 0.2f;
                            x1 += cellWidth * 0.2f;
                            x2 += cellWidth * 0.2f;
                            x3 -= cellWidth * 0.2f;
                        }
                        if(superscript) {
                            superscript = false;
                            midscript = false;
                            x2 += cellWidth * 0.5f;
                            x3 += cellWidth * 0.5f;
                            y1 -= cellHeight * 0.5f;
                            y2 -= cellHeight * 0.5f;
                        }
                        if(midscript) {
                            superscript = false;
                            subscript = false;
                            x2 += cellWidth * 0.5f;
                            x3 += cellWidth * 0.5f;
                            y0 += cellHeight * 0.25f;
                            y1 -= cellHeight * 0.25f;
                            y2 -= cellHeight * 0.25f;
                            y3 += cellHeight * 0.25f;
                        }
                        if(subscript) {
                            subscript = false;
                            midscript = false;
                            x2 += cellWidth * 0.5f;
                            x3 += cellWidth * 0.5f;
                            y0 += cellHeight * 0.5f;
                            y3 += cellHeight * 0.5f;
                        }
                        underline = false;
                        strikethrough = false;
                        capitalize = false;
                        capsLock = false;
                        lowerCase = false;
                        d -= cellWidth;
                        continue;
                    }
                    int len = text.indexOf(']', i) - i;
                    switch (c) {
                        case '*':
                            if (bold = !bold) {
//                                x0 -= cellWidth * 0.125f;
//                                x1 -= cellWidth * 0.125f;
//                                x2 += cellWidth * 0.125f;
//                                x3 += cellWidth * 0.125f;
//                                y0 += cellHeight * 0.09375f;
//                                y1 -= cellHeight * 0.09375f;
//                                y2 -= cellHeight * 0.09375f;
//                                y3 += cellHeight * 0.09375f;
                            } else {
//                                x0 += cellWidth * 0.125f;
//                                x1 += cellWidth * 0.125f;
//                                x2 -= cellWidth * 0.125f;
//                                x3 -= cellWidth * 0.125f;
//                                y0 -= cellHeight * 0.09375f;
//                                y1 += cellHeight * 0.09375f;
//                                y2 += cellHeight * 0.09375f;
//                                y3 -= cellHeight * 0.09375f;
                            }
                            break;
                        case '/':
                            if (oblique = !oblique) {
                                x0 += cellWidth * 0.2f;
                                x1 -= cellWidth * 0.2f;
                                x2 -= cellWidth * 0.2f;
                                x3 += cellWidth * 0.2f;
                            } else {
                                x0 -= cellWidth * 0.2f;
                                x1 += cellWidth * 0.2f;
                                x2 += cellWidth * 0.2f;
                                x3 -= cellWidth * 0.2f;
                            }
                            break;
                        case '^':
                            if (superscript = !superscript) {
                                x2 -= cellWidth * 0.5f;
                                x3 -= cellWidth * 0.5f;
                                y1 += cellHeight * 0.5f;
                                y2 += cellHeight * 0.5f;
                                if (subscript) {
                                    subscript = false;
                                    x2 += cellWidth * 0.5f;
                                    x3 += cellWidth * 0.5f;
                                    y0 += cellHeight * 0.5f;
                                    y3 += cellHeight * 0.5f;
                                }
                                if(midscript){
                                    midscript = false;
                                    x2 += cellWidth * 0.5f;
                                    x3 += cellWidth * 0.5f;
                                    y0 += cellHeight * 0.25f;
                                    y1 -= cellHeight * 0.25f;
                                    y2 -= cellHeight * 0.25f;
                                    y3 += cellHeight * 0.25f;
                                }
                            } else {
                                x2 += cellWidth * 0.5f;
                                x3 += cellWidth * 0.5f;
                                y1 -= cellHeight * 0.5f;
                                y2 -= cellHeight * 0.5f;
                            }
                            break;
                        case '.':
                            if (subscript = !subscript) {
                                x2 -= cellWidth * 0.5f;
                                x3 -= cellWidth * 0.5f;
                                y0 -= cellHeight * 0.5f;
                                y3 -= cellHeight * 0.5f;
                                if (superscript) {
                                    superscript = false;
                                    x2 += cellWidth * 0.5f;
                                    x3 += cellWidth * 0.5f;
                                    y1 -= cellHeight * 0.5f;
                                    y2 -= cellHeight * 0.5f;
                                }
                                if(midscript){
                                    midscript = false;
                                    x2 += cellWidth * 0.5f;
                                    x3 += cellWidth * 0.5f;
                                    y0 += cellHeight * 0.25f;
                                    y1 -= cellHeight * 0.25f;
                                    y2 -= cellHeight * 0.25f;
                                    y3 += cellHeight * 0.25f;
                                }
                            } else {
                                x2 += cellWidth * 0.5f;
                                x3 += cellWidth * 0.5f;
                                y0 += cellHeight * 0.5f;
                                y3 += cellHeight * 0.5f;
                            }
                            break;
                        case '=':
                            if(midscript = !midscript) {
                                x2 -= cellWidth * 0.5f;
                                x3 -= cellWidth * 0.5f;
                                y0 -= cellHeight * 0.25f;
                                y1 += cellHeight * 0.25f;
                                y2 += cellHeight * 0.25f;
                                y3 -= cellHeight * 0.25f;
                                if (superscript) {
                                    superscript = false;
                                    x2 += cellWidth * 0.5f;
                                    x3 += cellWidth * 0.5f;
                                    y1 -= cellHeight * 0.5f;
                                    y2 -= cellHeight * 0.5f;
                                }
                                if (subscript) {
                                    subscript = false;
                                    x2 += cellWidth * 0.5f;
                                    x3 += cellWidth * 0.5f;
                                    y0 += cellHeight * 0.5f;
                                    y3 += cellHeight * 0.5f;
                                }
                            }
                            else {
                                x2 += cellWidth * 0.5f;
                                x3 += cellWidth * 0.5f;
                                y0 += cellHeight * 0.25f;
                                y1 -= cellHeight * 0.25f;
                                y2 -= cellHeight * 0.25f;
                                y3 += cellHeight * 0.25f;
                            }
                            break;
                        case '_':
                            underline = !underline;
                            break;
                        case '~':
                            strikethrough = !strikethrough;
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
                                color = (BitConversion.reversedIntBitsToFloat(DigitTools.intFromHex(text, i + 1, i + 7) << 8 | 0xFE));
                            else if (len >= 9)
                                color = (BitConversion.reversedIntBitsToFloat(DigitTools.intFromHex(text, i + 1, i + 9) & -2));
                            else
                                color = (Color.WHITE_FLOAT_BITS);
                            break;
                        case '|':
                            color = (BitConversion.reversedIntBitsToFloat(
                                    DescriptiveColor.toRGBA8888(DescriptiveColor.describeIPT(text, i + 1, len)) & -2));
                            break;
                        default:
                            if (c >= 'a' && c <= 'z')
                                color = (BitConversion.reversedIntBitsToFloat(
                                        DescriptiveColor.toRGBA8888(DescriptiveColor.describeIPT(text, i, len)) & -2));
                    }
                    i += len;
                    d -= cellWidth;
                }
                else {
                    batch.draw(mapping.get('['), x, y, cellWidth, cellHeight);
                }
            } else {
                char ch = text.charAt(i);
                if(Category.Ll.contains(ch)) {
                    if(capitalize && !previousWasLetter) {
                        ch = Character.toUpperCase(ch);
                    }
                    else if(capsLock) {
                        ch = Character.toUpperCase(ch);
                    }
                    previousWasLetter = true;
                }
                else if(Category.Lu.contains(ch)) {
                    if(capitalize && previousWasLetter) {
                        ch = Character.toLowerCase(ch);
                    }
                    else if(lowerCase) {
                        ch = Character.toLowerCase(ch);
                    }
                    previousWasLetter = true;
                }
                else {
                    previousWasLetter = false;
                }
                tr = mapping.get(ch);
                if(tr == null) continue;
                u = tr.getU();
                v = tr.getV();
                u2 = tr.getU2();
                v2 = tr.getV2();
                vertices[0] = x + x0 + d;
                vertices[1] = y + y0 + cellHeight;
                vertices[2] = color;
                vertices[3] = u;
                vertices[4] = v;

                vertices[5] = x + x1 + d;
                vertices[6] = y + y1;
                vertices[7] = color;
                vertices[8] = u;
                vertices[9] = v2;

                vertices[10] = x + x2 + cellWidth + d;
                vertices[11] = y + y2;
                vertices[12] = color;
                vertices[13] = u2;
                vertices[14] = v2;

                vertices[15] = x + x3 + cellWidth + d;
                vertices[16] = y + y3 + cellHeight;
                vertices[17] = color;
                vertices[18] = u2;
                vertices[19] = v;
                batch.draw(parentTexture, vertices, 0, 20);
                if(bold){
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
                if(underline){
                    vertices[0] = x + d;
                    vertices[1] = y + cellHeight;
                    vertices[2] = color;
                    vertices[3] = underU;
                    vertices[4] = underV;

                    vertices[5] = x + d;
                    vertices[6] = y;
                    vertices[7] = color;
                    vertices[8] = underU;
                    vertices[9] = underV2;

                    vertices[10] = x + cellWidth + d;
                    vertices[11] = y;
                    vertices[12] = color;
                    vertices[13] = underU2;
                    vertices[14] = underV2;

                    vertices[15] = x + cellWidth + d;
                    vertices[16] = y + cellHeight;
                    vertices[17] = color;
                    vertices[18] = underU2;
                    vertices[19] = underV;
                    batch.draw(parentTexture, vertices, 0, 20);
                }
                if(strikethrough){
                    vertices[0] = x + d;
                    vertices[1] = y + cellHeight;
                    vertices[2] = color;
                    vertices[3] = dashU;
                    vertices[4] = dashV;

                    vertices[5] = x + d;
                    vertices[6] = y;
                    vertices[7] = color;
                    vertices[8] = dashU;
                    vertices[9] = dashV2;

                    vertices[10] = x + cellWidth + d;
                    vertices[11] = y;
                    vertices[12] = color;
                    vertices[13] = dashU2;
                    vertices[14] = dashV2;

                    vertices[15] = x + cellWidth + d;
                    vertices[16] = y + cellHeight;
                    vertices[17] = color;
                    vertices[18] = dashU2;
                    vertices[19] = dashV;
                    batch.draw(parentTexture, vertices, 0, 20);
                }
            }
        }

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
