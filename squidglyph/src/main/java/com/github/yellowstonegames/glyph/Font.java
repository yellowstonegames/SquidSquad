package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.support.BitConversion;
import com.github.yellowstonegames.core.DigitTools;

public class Font {
    public IntObjectMap<TextureRegion> mapping;
    public Texture parentTexture;
    public boolean isMSDF;
    public float msdfCrispness = 1.2f;
    public float cellWidth = 1f, cellHeight = 1f;

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
    }
    public Font scale(float horizontal, float vertical) {
        cellWidth *= horizontal;
        cellHeight *= vertical;
        msdfCrispness = (msdfCrispness - 1f) / Math.max(horizontal, vertical) + 1f;
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
            msdfShader.setUniformf("u_smoothing", 0.09375f * msdfCrispness * cellWidth);
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
     * Draws the specified text at the given x,y position (in world space) with a white foreground.
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
}
