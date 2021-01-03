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
import com.github.yellowstonegames.core.DigitTools;

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
    public static ShaderProgram msdfShader = null;

    private static int indexAfter(String text, String search, int from){
        return ((from = text.indexOf(search, from)) < 0 ? text.length() : from + search.length());
    }
    public Font(String fntName, String textureName, boolean isMSDF) {
        this.isMSDF = isMSDF;
        if(isMSDF && msdfShader == null) {
            msdfShader = new ShaderProgram(vertexShader, msdfFragmentShader);
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
            cellWidth = w;
            cellHeight = h;
            mapping.put(c, new TextureRegion(parentTexture, x, y, w, h));
        }
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
        if(isMSDF){
            if(!batch.getShader().equals(msdfShader))
                batch.setShader(msdfShader);
            msdfShader.setUniformf("u_smoothing", 3.5f * cellHeight / (msdfCrispness * originalCellHeight));
//            msdfShader.setUniformf("u_smoothing", 0.09375f * msdfCrispness * cellHeight);
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
     * Releases all resources of this object.
     */
    @Override
    public void dispose() {
        mapping = null;
        parentTexture.dispose();
    }
}
