package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.github.tommyettinger.ds.CharList;
import com.github.tommyettinger.ds.IntIntMap;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.support.BitConversion;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import regexodus.Category;

import java.util.Arrays;

import static com.github.yellowstonegames.core.DigitTools.intFromDec;

public class Font implements Disposable {

    /**
     * Describes the region of a glyph in a larger TextureRegion, carrying a little more info about the offsets that
     * apply to where the glyph is rendered.
     */
    public static class GlyphRegion extends TextureRegion {
        /**
         * The offset from the left of the original image to the left of the packed image, after whitespace was removed
         * for packing.
         */
        public float offsetX;

        /**
         * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was
         * removed for packing.
         */
        public float offsetY;

        /**
         * How far to move the "cursor" to the right after drawing this GlyphRegion. Uses the same unit as
         * {@link #offsetX}.
         */
        public float xAdvance;

        /**
         * Creates a GlyphRegion from a parent TextureRegion (typically from an atlas), along with the lower-left x and
         * y coordinates, the width, and the height of the GlyphRegion.
         * @param textureRegion a TextureRegion, typically from a TextureAtlas
         * @param x the x-coordinate of the left side of the texture, in pixels
         * @param y the y-coordinate of the lower side of the texture, in pixels
         * @param width the width of the GlyphRegion, in pixels
         * @param height the height of the GlyphRegion, in pixels
         */
        public GlyphRegion(TextureRegion textureRegion, int x, int y, int width, int height) {
            super(textureRegion, x, y, width, height);
        }

        /**
         * Copies another GlyphRegion.
         * @param other the other GlyphRegion to copy
         */
        public GlyphRegion(GlyphRegion other) {
            super(other);
            offsetX = other.offsetX;
            offsetY = other.offsetY;
            xAdvance = other.xAdvance;
        }

        /**
         * Flips the region, adjusting the offset so the image appears to be flipped as if no whitespace has been
         * removed for packing.
         * @param x true if this should flip x to be -x
         * @param y true if this should flip y to be -y
         */
        @Override
        public void flip (boolean x, boolean y) {
            super.flip(x, y);
            if (x) {
                offsetX = -offsetX;
                xAdvance = -xAdvance; // TODO: not sure if this is the expected behavior...
            }
            if (y) offsetY = -offsetY;
        }
    }

    /**
     * Defines what types of distance field font this can use and render.
     * STANDARD has no distance field.
     * SDF is the signed distance field technique Hiero is compatible with, and uses only an alpha channel.
     * MSDF is the multi-channel signed distance field technique, which is sharper but uses the RGB channels.
     */
    public enum DistanceFieldType {
        /**
         * Used by normal fonts with no distance field effect.
         * If the font has a large image that is downscaled, you may want to call {@link #setTextureFilter()}.
         */
        STANDARD,
        /**
         * Used by Signed Distance Field fonts that are compatible with {@link DistanceFieldFont}, and may be created
         * by Hiero with its Distance Field effect. You may want to set the {@link #distanceFieldCrispness} field to a
         * higher or lower value depending on the range used to create the font in Hiero; this can take experimentation.
         */
        SDF,
        /**
         * Used by Multi-channel Signed Distance Field fonts, which are harder to create but can be more crisp than SDF
         * fonts, with hard corners where the corners were hard in the original font. If you want to create your own
         * MSDF font, you can use <a href="https://github.com/tommyettinger/Glamer">the Glamer font generator tool</a>,
         * which puts a lot of padding for each glyph to ensure it renders evenly, or you can use one of several other
         * MSDF font generators, which may have an uneven baseline and look shaky when rendered for some fonts. You may
         * want to set the {@link #distanceFieldCrispness} field to a higher or lower value based on preference.
         */
        MSDF
    }

    //// members section

    public IntObjectMap<GlyphRegion> mapping;
    public Array<TextureRegion> parents;
    public DistanceFieldType distanceField = DistanceFieldType.STANDARD;
    public boolean isMono;
    public IntIntMap kerning;
    /**
     * When {@link #distanceField} is {@link DistanceFieldType#SDF} or {@link DistanceFieldType#MSDF}, this determines
     * how much the edges of the glyphs should be aliased sharply (higher values) or anti-aliased softly (lower values).
     * The default value is 1.
     */
    public float distanceFieldCrispness = 1f;
    /**
     * Only actually refers to a "cell" when {@link #isMono} is true; otherwise refers to the largest width of any
     * glyph in the font, after scaling.
     */
    public float cellWidth = 1f;
    /**
     * Refers to the largest height of any glyph in the font, after scaling.
     */
    public float cellHeight = 1f;
    /**
     * Only actually refers to a "cell" when {@link #isMono} is true; otherwise refers to the largest width of any
     * glyph in the font, before any scaling.
     */
    public float originalCellWidth = 1f;
    /**
     * Refers to the largest height of any glyph in the font, before any scaling.
     */
    public float originalCellHeight = 1f;
    /**
     * Scale multiplier for width.
     */
    public float scaleX = 1f;
    /**
     * Scale multiplier for height.
     */
    public float scaleY = 1f;

    public static final long BOLD = 1L << 30, OBLIQUE = 1L << 29,
            UNDERLINE = 1L << 28, STRIKETHROUGH = 1L << 27,
            SUBSCRIPT = 1L << 25, MIDSCRIPT = 2L << 25, SUPERSCRIPT = 3L << 25;

    private final float[] vertices = new float[20];
    private final Layout tempLayout = Pools.obtain(Layout.class);
    /**
     * Must be in lexicographic order because we use {@link Arrays#binarySearch(char[], int, int, char)} to
     * verify if a char is present.
     */
    private final CharList breakChars = CharList.with(
            '\t',    // horizontal tab
            ' ',     // space
            '-',     // ASCII hyphen-minus
            '\u00AD',// soft hyphen
            '\u2000',// Unicode space
            '\u2001',// Unicode space
            '\u2002',// Unicode space
            '\u2003',// Unicode space
            '\u2004',// Unicode space
            '\u2005',// Unicode space
            '\u2006',// Unicode space
            '\u2008',// Unicode space
            '\u2009',// Unicode space
            '\u200A',// Unicode space (hair-width)
            '\u200B',// Unicode space (zero-width)
            '\u2010',// hyphen (not minus)
            '\u2012',// figure dash
            '\u2013',// en dash
            '\u2014',// em dash
            '\u2027' // hyphenation point
    );

    /**
     * Must be in lexicographic order because we use {@link Arrays#binarySearch(char[], int, int, char)} to
     * verify if a char is present.
     */
    private final CharList spaceChars = CharList.with(
            '\t',    // horizontal tab
            ' ',     // space
            '\u2000',// Unicode space
            '\u2001',// Unicode space
            '\u2002',// Unicode space
            '\u2003',// Unicode space
            '\u2004',// Unicode space
            '\u2005',// Unicode space
            '\u2006',// Unicode space
            '\u2008',// Unicode space
            '\u2009',// Unicode space
            '\u200A',// Unicode space (hair-width)
            '\u200B' // Unicode space (zero-width)
    );

    /**
     * The standard libGDX vertex shader source, which is also used by the MSDF shader.
     */
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

    /**
     * Fragment shader source meant for MSDF fonts. This is automatically used when {@link #enableShader(Batch)} is
     * called and the {@link #distanceField} is {@link DistanceFieldType#MSDF}.
     */
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

    /**
     * The ShaderProgram used to render this font, as used by {@link #enableShader(Batch)}.
     * If this is null, the font will be rendered with the Batch's default shader.
     * It may be set to a custom ShaderProgram if {@link #distanceField} is set to {@link DistanceFieldType#MSDF},
     * or to one created by {@link DistanceFieldFont#createDistanceFieldShader()} if distanceField is set to
     * {@link DistanceFieldType#SDF}. It can be set to a user-defined ShaderProgram; if it is meant to render
     * MSDF or SDF fonts, then the ShaderProgram should have a {@code uniform float u_smoothing;} that will be
     * set by {@link #enableShader(Batch)}. Values passed to u_smoothing can vary a lot, depending on how the
     * font was initially created, its current scale, and its {@link #distanceFieldCrispness} field. You can
     * also use a user-defined ShaderProgram with a font using {@link DistanceFieldType#STANDARD}, which may be
     * easier and can use any uniforms you normally could with a ShaderProgram, since enableShader() won't
     * change any of the uniforms.
     */
    public ShaderProgram shader = null;

    //// utilities

    private static int indexAfter(String text, String search, int from){
        return ((from = text.indexOf(search, from)) < 0 ? text.length() : from + search.length());
    }

    /**
     * Returns true if {@code c} is a lower-case letter, or false otherwise.
     * Similar to {@link Character#isLowerCase(char)}, but should actually work on GWT.
     * @param c a char to check
     * @return true if c is a lower-case letter, or false otherwise.
     */
    public static boolean isLowerCase(char c) {
        return Category.Ll.contains(c);
    }

    /**
     * Returns true if {@code c} is an upper-case letter, or false otherwise.
     * Similar to {@link Character#isUpperCase(char)}, but should actually work on GWT.
     * @param c a char to check
     * @return true if c is an upper-case letter, or false otherwise.
     */
    public static boolean isUpperCase(char c) {
        return Category.Lu.contains(c);
    }

    //// constructor section

    /**
     * Constructs a Font by reading in the given .fnt file and loading any images it specifies. Tries an internal handle
     * first, then a classpath handle. Does not use a distance field effect.
     * @param fntName the file path and name to a .fnt file this will load
     */
    public Font(String fntName){
        this(fntName, DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f);
    }
    /**
     * Constructs a Font by reading in the given .fnt file and loading any images it specifies. Tries an internal handle
     * first, then a classpath handle. Uses the specified distance field effect.
     * @param fntName the file path and name to a .fnt file this will load
     * @param distanceField
     */
    public Font(String fntName, DistanceFieldType distanceField){
        this(fntName, distanceField, 0f, 0f, 0f, 0f);
    }

    /**
     * Constructs a Font by reading in the given .fnt file and the given Texture by filename. Tries an internal handle
     * first, then a classpath handle. Does not use a distance field effect.
     * @param fntName the file path and name to a .fnt file this will load
     */
    public Font(String fntName, String textureName){
        this(fntName, textureName, DistanceFieldType.STANDARD, 0f, 0f, 0f, 0f);
    }
    /**
     * Constructs a Font by reading in the given .fnt file and the given Texture by filename. Tries an internal handle
     * first, then a classpath handle. Uses the specified distance field effect.
     * @param fntName the file path and name to a .fnt file this will load
     * @param distanceField
     */
    public Font(String fntName, String textureName, DistanceFieldType distanceField){
        this(fntName, textureName, distanceField, 0f, 0f, 0f, 0f);
    }

    /**
     * Copy constructor; does not copy the font's {@link #shader}, if it has one (it keeps a reference), but will fully
     * copy everything else.
     * @param toCopy another Font to copy
     */
    public Font(Font toCopy){
        distanceField = toCopy.distanceField;
        isMono = toCopy.isMono;
        distanceFieldCrispness = toCopy.distanceFieldCrispness;
        parents = new Array<>(toCopy.parents);
        cellWidth = toCopy.cellWidth;
        cellHeight = toCopy.cellHeight;
        scaleX = toCopy.scaleX;
        scaleY = toCopy.scaleY;
        originalCellWidth = toCopy.originalCellWidth;
        originalCellHeight = toCopy.originalCellHeight;
        mapping = new IntObjectMap<>(toCopy.mapping.size());
        for(IntObjectMap.Entry<GlyphRegion> e : toCopy.mapping){
            if(e.value == null) continue;
            mapping.put(e.key, new GlyphRegion(e.value));
        }
        mapping.defaultValue = new GlyphRegion(toCopy.mapping.defaultValue);
        kerning = toCopy.kerning == null ? null : new IntIntMap(toCopy.kerning);
        // the shader is not copied, because there isn't much point in having different copies of a ShaderProgram.
        if(toCopy.shader != null)
            shader = toCopy.shader;
    }

    /**
     * Constructs a new Font by reading in a .fnt file with the given name (an internal handle is tried first, then a
     * classpath handle) and loading any images specified in that file. No distance field effect is used.
     * @param fntName the path and filename of a .fnt file this will load; may be internal or classpath
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a .fnt file with the given name (an internal handle is tried first, then a
     * classpath handle) and loading any images specified in that file. The specified distance field effect is used.
     * @param fntName
     * @param distanceField
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("squidglyph", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("squidglyph", "SDF shader failed to compile: " + shader.getLog());
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried, then classpath),
     * and no distance field effect.
     * @param fntName
     * @param textureName
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, String textureName,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureName, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font by reading in a Texture from the given named path (internal is tried, then classpath),
     * and the specified distance field effect.
     * @param fntName
     * @param textureName
     * @param distanceField
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, String textureName, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("squidglyph", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("squidglyph", "SDF shader failed to compile: " + shader.getLog());
        }
        FileHandle textureHandle;
        if ((textureHandle = Gdx.files.internal(textureName)).exists()
                || (textureHandle = Gdx.files.classpath(textureName)).exists()) {
            parents = Array.with(new TextureRegion(new Texture(textureHandle)));
            if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF) {
                parents.first().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        } else {
            throw new RuntimeException("Missing texture file: " + textureName);
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a font using the given TextureRegion that holds all of its glyphs, with no distance field effect.
     * @param fntName
     * @param textureRegion
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, TextureRegion textureRegion,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureRegion, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a font using the given TextureRegion that holds all of its glyphs, with the specified distance field
     * effect.
     * @param fntName
     * @param textureRegion
     * @param distanceField
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, TextureRegion textureRegion, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("squidglyph", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("squidglyph", "SDF shader failed to compile: " + shader.getLog());
        }
        this.parents = Array.with(textureRegion);
        if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF) {
            textureRegion.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a Font using the given TextureRegion Array, with no distance field effect.
     * @param fntName
     * @param textureRegions
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, Array<TextureRegion> textureRegions,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(fntName, textureRegions, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }
    /**
     * Constructs a Font using the given TextureRegion Array and specified distance field effect.
     * @param fntName
     * @param textureRegions
     * @param distanceField
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(String fntName, Array<TextureRegion> textureRegions, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("squidglyph", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("squidglyph", "SDF shader failed to compile: " + shader.getLog());
        }
        this.parents = textureRegions;
        if ((distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF)
                && textureRegions != null)
        {
            for(TextureRegion parent : textureRegions)
                parent.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        loadFNT(fntName, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }

    /**
     * Constructs a new Font from the existing BitmapFont, using its same Textures and TextureRegions for glyphs, and
     * without a distance field effect.
     * @param bmFont
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(BitmapFont bmFont,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this(bmFont, DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust);
    }
    /**
     * Constructs a new Font from the existing BitmapFont, using its same Textures and TextureRegions for glyphs, and
     * with the specified distance field effect.
     * @param bmFont
     * @param distanceField
     * @param xAdjust how many pixels to offset each character's x-position by, moving to the right
     * @param yAdjust how many pixels to offset each character's y-position by, moving up
     * @param widthAdjust how many pixels to add to the used width of each character, using more to the right
     * @param heightAdjust how many pixels to add to the used height of each character, using more above
     */
    public Font(BitmapFont bmFont, DistanceFieldType distanceField,
                float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        this.distanceField = distanceField;
        if (distanceField == DistanceFieldType.MSDF) {
            shader = new ShaderProgram(vertexShader, msdfFragmentShader);
            if (!shader.isCompiled())
                Gdx.app.error("squidglyph", "MSDF shader failed to compile: " + shader.getLog());
        }
        else if(distanceField == DistanceFieldType.SDF){
            shader = DistanceFieldFont.createDistanceFieldShader();
            if(!shader.isCompiled())
                Gdx.app.error("squidglyph", "SDF shader failed to compile: " + shader.getLog());
        }
        this.parents = bmFont.getRegions();
        if ((distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF)
                && parents != null)
        {
            for(TextureRegion parent : parents)
                parent.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        BitmapFont.BitmapFontData data = bmFont.getData();
        mapping = new IntObjectMap<>(128);
        int minWidth = Integer.MAX_VALUE;
        for (BitmapFont.Glyph[] page : data.glyphs) {
            if (page == null) continue;
            for (BitmapFont.Glyph glyph : page) {
                if (glyph != null) {
                    int x = glyph.srcX, y = glyph.srcY, w = glyph.width, h = glyph.height, a = glyph.xadvance;
                    x += xAdjust;
                    y += yAdjust;
                    a += widthAdjust;
                    h += heightAdjust;
                    minWidth = Math.min(minWidth, a);
                    cellWidth = Math.max(a, cellWidth);
                    cellHeight = Math.max(h, cellHeight);
                    GlyphRegion gr = new GlyphRegion(bmFont.getRegion(glyph.page), x, y, w, h);
                    if(glyph.id == 10)
                    {
                        a = 0;
                        gr.offsetX = 0;
                    }
                    else {
                        gr.offsetX = glyph.xoffset;
                    }
                    gr.offsetY = -h - glyph.yoffset;
                    gr.xAdvance = a;
                    mapping.put(glyph.id & 0xFFFF, gr);
                    if(glyph.kerning != null) {
                        if(kerning == null) kerning = new IntIntMap(128);
                        for (int b = 0; b < glyph.kerning.length; b++) {
                            byte[] kern = glyph.kerning[b];
                            if(kern != null) {
                                int k;
                                for (int i = 0; i < 512; i++) {
                                    k = kern[i];
                                    if (k != 0) {
                                        kerning.put(glyph.id << 16 | (b << 9 | i), k);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(mapping.notEmpty()) {
            mapping.defaultValue = mapping.getOrDefault(data.missingGlyph == null
                    ? ' '
                    : data.missingGlyph.id, mapping.getOrDefault(' ', mapping.values().iterator().next()));
        }
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
        isMono = minWidth == cellWidth && kerning == null;
        scale(bmFont.getScaleX(), bmFont.getScaleY());
    }
    /**
     * The gritty parsing code that pulls relevant info from a FNT file and uses it to assemble the
     * many {@code TextureRegion}s this has for each glyph.
     * @param fntName the file name of the .fnt file; can be internal or classpath
     * @param xAdjust added to the x-position for each glyph in the font
     * @param yAdjust added to the y-position for each glyph in the font
     * @param widthAdjust added to the glyph width for each glyph in the font
     * @param heightAdjust added to the glyph height for each glyph in the font
     */
    protected void loadFNT(String fntName, float xAdjust, float yAdjust, float widthAdjust, float heightAdjust) {
        FileHandle fntHandle;
        String fnt;
        if ((fntHandle = Gdx.files.internal(fntName)).exists()
                || (fntHandle = Gdx.files.classpath(fntName)).exists()) {
            fnt = fntHandle.readString("UTF8");
        } else {
            throw new RuntimeException("Missing font file: " + fntName);
        }
        int idx = indexAfter(fnt, " pages=", 0);
        int pages = intFromDec(fnt, idx, idx = indexAfter(fnt, "\npage id=", idx));
        if (parents == null || parents.size < pages) {
            if (parents == null) parents = new Array<>(true, pages, TextureRegion.class);
            else parents.clear();
            FileHandle textureHandle;
            for (int i = 0; i < pages; i++) {
                String textureName = fnt.substring(idx = indexAfter(fnt, "file=\"", idx), idx = fnt.indexOf('"', idx));
                if ((textureHandle = Gdx.files.internal(textureName)).exists()
                        || (textureHandle = Gdx.files.classpath(textureName)).exists()) {
                    parents.add(new TextureRegion(new Texture(textureHandle)));
                    if (distanceField == DistanceFieldType.SDF || distanceField == DistanceFieldType.MSDF)
                        parents.peek().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                } else {
                    throw new RuntimeException("Missing texture file: " + textureName);
                }

            }
        }
        int size = intFromDec(fnt, idx = indexAfter(fnt, "\nchars count=", idx), idx = indexAfter(fnt, "\nchar id=", idx));
        mapping = new IntObjectMap<>(size);
        int minWidth = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            int c = intFromDec(fnt, idx, idx = indexAfter(fnt, " x=", idx));
            int x = intFromDec(fnt, idx, idx = indexAfter(fnt, " y=", idx));
            int y = intFromDec(fnt, idx, idx = indexAfter(fnt, " width=", idx));
            int w = intFromDec(fnt, idx, idx = indexAfter(fnt, " height=", idx));
            int h = intFromDec(fnt, idx, idx = indexAfter(fnt, " xoffset=", idx));
            int xo = intFromDec(fnt, idx, idx = indexAfter(fnt, " yoffset=", idx));
            int yo = intFromDec(fnt, idx, idx = indexAfter(fnt, " xadvance=", idx));
            int a = intFromDec(fnt, idx, idx = indexAfter(fnt, " page=", idx));
            int p = intFromDec(fnt, idx, idx = indexAfter(fnt, "\nchar id=", idx));

            x += xAdjust;
            y += yAdjust;
            a += widthAdjust;
            h += heightAdjust;
            minWidth = Math.min(minWidth, a);
            cellWidth = Math.max(a, cellWidth);
            cellHeight = Math.max(h, cellHeight);
            GlyphRegion gr = new GlyphRegion(parents.get(p), x, y, w, h);
            if(c == 10)
            {
                a = 0;
                gr.offsetX = 0;
            }
            else
                gr.offsetX = xo;
            gr.offsetY = yo;
            gr.xAdvance = a;
            mapping.put(c, gr);
        }
        idx = indexAfter(fnt, "\nkernings count=", 0);
        if(idx < fnt.length()){
            int kernings = intFromDec(fnt, idx, idx = indexAfter(fnt, "\nkerning first=", idx));
            kerning = new IntIntMap(kernings);
            for (int i = 0; i < kernings; i++) {
                int first = intFromDec(fnt, idx, idx = indexAfter(fnt, " second=", idx));
                int second = intFromDec(fnt, idx, idx = indexAfter(fnt, " amount=", idx));
                int amount = intFromDec(fnt, idx, idx = indexAfter(fnt, "\nkerning first=", idx));
                kerning.put(first << 16 | second, amount);
            }
        }
        if(mapping.notEmpty())
        {
            mapping.defaultValue = mapping.getOrDefault(' ', mapping.values().iterator().next());
        }
        originalCellWidth = cellWidth;
        originalCellHeight = cellHeight;
        isMono = minWidth == cellWidth && kerning == null;
    }

    //// usage section

    /**
     * Assembles two chars into a kerning pair that can be looked up as a key in {@link #kerning}.
     * @param first the first char
     * @param second the second char
     * @return a kerning pair that can be looked up in {@link #kerning}
     */
    public int kerningPair(char first, char second) {
        return first << 16 | (second & 0xFFFF);
    }

    /**
     * Scales the font by the given horizontal and vertical multipliers.
     * @param horizontal how much to multiply the width of each glyph by
     * @param vertical how much to multiply the height of each glyph by
     * @return this Font, for chaining
     */
    public Font scale(float horizontal, float vertical) {
        scaleX *= horizontal;
        scaleY *= vertical;
        cellWidth *= horizontal;
        cellHeight *= vertical;
        return this;
    }

    /**
     * Scales the font so it will have the given width and height.
     * @param width the target width of the font, in world units
     * @param height the target height of the font, in world units
     * @return this Font, for chaining
     */
    public Font scaleTo(float width, float height) {
        scaleX = width / originalCellWidth;
        scaleY = height / originalCellHeight;
        cellWidth  = width;
        cellHeight = height;
        return this;
    }

    /**
     * Calls {@link #setTextureFilter(Texture.TextureFilter, Texture.TextureFilter)} with
     * {@link Texture.TextureFilter#Linear} for both min and mag filters.
     * This is the most common usage for setting the texture filters, and is appropriate when you have
     * a large TextureRegion holding the font and you normally downscale it. This is automatically done
     * for {@link DistanceFieldType#SDF} and {@link DistanceFieldType#MSDF} fonts, but you may also want
     * to use it for {@link DistanceFieldType#STANDARD} fonts when downscaling (they can look terrible
     * if the default {@link Texture.TextureFilter#Nearest} filter is used).
     * Note that this sets the filter on every Texture that holds a TextureRegion used by the font, so
     * it may affect the filter on other parts of an atlas.
     * @return this, for chaining
     */
    public Font setTextureFilter() {
        return setTextureFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    /**
     * Sets the texture filters on each Texture that holds a TextureRegion used by the font to the given
     * {@code minFilter} and {@code magFilter}. You may want to use this to set a font using
     * {@link DistanceFieldType#STANDARD} to use a better TextureFilter for smooth downscaling, like
     * {@link Texture.TextureFilter#MipMapLinearLinear} or just
     * {@link Texture.TextureFilter#Linear}. You might, for some reason, want to
     * set a font using {@link DistanceFieldType#SDF} or {@link DistanceFieldType#MSDF} to use TextureFilters
     * other than its default {@link Texture.TextureFilter#Linear}.
     * Note that this may affect the filter on other parts of an atlas.
     * @return this, for chaining
     */
    public Font setTextureFilter(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter) {
        for(TextureRegion parent : parents){
            parent.getTexture().setFilter(minFilter, magFilter);
        }
        return this;
    }

    /**
     * Must be called before drawing anything with an MSDF font; does not need to be called for other fonts unless you
     * are mixing them with MSDF fonts or other shaders. This also resets the Batch color to white, in case it had been
     * left with a different setting before. If this Font is not an MSDF font, then this resets batch's shader to the
     * default (using {@code batch.setShader(null)}).
     * @param batch the Batch to instruct to use the appropriate shader for this font; should usually be a SpriteBatch
     */
    public void enableShader(Batch batch) {
        if(distanceField == DistanceFieldType.MSDF) {
            if (batch.getShader() != shader) {
                batch.setShader(shader);
                shader.setUniformf("u_smoothing", 7f * distanceFieldCrispness * Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth));
            }
        } else if(distanceField == DistanceFieldType.SDF){
            if (batch.getShader() != shader) {
                batch.setShader(shader);
                final float scale = Math.max(cellHeight / originalCellHeight, cellWidth / originalCellWidth) * 0.5f + 0.5f;
                shader.setUniformf("u_smoothing", (distanceFieldCrispness / (scale * scale)));
            }
        } else {
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
        GlyphRegion current;
        for (int i = 0, n = text.length(); i < n; i++) {
            batch.draw(current = mapping.get(text.charAt(i)), x + current.offsetX, y + current.offsetY, current.getRegionWidth(), current.getRegionHeight());
            x += current.getRegionWidth();
        }
    }


    /**
     * Draws a grid made of rectangular blocks of int colors (typically RGBA) at the given x,y position in world space.
     * The {@code colors} parameter should be a rectangular 2D array, and because any colors that are the default int
     * value {@code 0} will be treated as transparent RGBA values, if a value is not assigned to a slot in the array
     * then nothing will be drawn there. This is usually called before other methods that draw foreground text.
     * <br>
     * Internally, this uses {@link Batch#draw(Texture, float[], int, int)} to draw each rectangle with minimal
     * overhead, and this also means it is unaffected by the batch color. If you want to alter the colors using a
     * shader, the shader will receive each color in {@code colors} as its {@code a_color} attribute, the same as if it
     * was passed via the batch color.
     * @param batch typically a SpriteBatch
     * @param colors a 2D rectangular array of int colors (typically RGBA)
     * @param x the x position in world space to draw the text at (lower left corner)
     * @param y the y position in world space to draw the text at (lower left corner)
     */
    public void drawBlocks(Batch batch, int[][] colors, float x, float y) {
        final TextureRegion block = mapping.get(0);
        if(block == null) return;
        final Texture parent = block.getTexture();
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
                batch.draw(parent, vertices, 0, 20);
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
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up in
     *     {@link Colors}, changes the color.</li>
     * </ul>
     * <br>
     * Parsing markup for a full screen every frame typically isn't necessary, and you may want to store the most recent
     * glyphs by calling {@link #markup(String, Layout)} and render its result with
     * {@link #drawGlyphs(Batch, Layout, float, float)} every frame.
     * @param batch typically a SpriteBatch
     * @param text typically a String with markup, but this can also be a StringBuilder or some custom class
     * @param x the x position in world space to start drawing the text at (lower left corner)
     * @param y the y position in world space to start drawing the text at (lower left corner)
     * @return the number of glyphs drawn
     */
    public int drawMarkupText(Batch batch, String text, float x, float y) {
        Layout layout = tempLayout;
        layout.clear();
        markup(text, tempLayout);
        final int lines = layout.lines();
        int drawn = 0;
        for (int ln = 0; ln < lines; ln++) {
            Line line = layout.getLine(ln);
            int n = line.glyphs.size();
            drawn += n;
            if (kerning != null) {
                int kern = -1, amt = 0;
                long glyph;
                for (int i = 0; i < n; i++) {
                    kern = kern << 16 | (int) ((glyph = line.glyphs.get(i)) & 0xFFFF);
                    amt = kerning.getOrDefault(kern, 0);
                    x += drawGlyph(batch, glyph, x + amt, y) + amt;
                }
            } else {
                for (int i = 0; i < n; i++) {
                    x += drawGlyph(batch, line.glyphs.get(i), x, y);
                }
            }
            y -= cellHeight;
        }
        return drawn;
    }

        /**
     * Draws the specified Layout of glyphs with a Batch at a given x, y position, drawing the full layout.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned as part of {@link #markup(String, Layout)}
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     * @return the number of glyphs drawn
     */
    public int drawGlyphs(Batch batch, Layout glyphs, float x, float y) {
        return drawGlyphs(batch, glyphs, x, y, Align.left);
    }
    /**
     * Draws the specified Layout of glyphs with a Batch at a given x, y position, using {@code align} to
     * determine how to position the text. Typically, align is {@link Align#left}, {@link Align#center}, or
     * {@link Align#right}, which make the given x,y point refer to the lower-left corner, center-bottom edge point, or
     * lower-right corner, respectively.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned by {@link #markup(String, Layout)}
     * @param x the x position in world space to start drawing the glyph at (where this is depends on align)
     * @param y the y position in world space to start drawing the glyph at (where this is depends on align)
     * @param align an {@link Align} constant; if {@link Align#left}, x and y refer to the lower left corner
     * @return the number of glyphs drawn
     */
    public int drawGlyphs(Batch batch, Layout glyphs, float x, float y, int align) {
        int drawn = 0;
        final int lines = glyphs.lines();
        for (int ln = 0; ln < lines; ln++) {
            drawn += drawGlyphs(batch, glyphs.getLine(ln), x, y, align);
            y -= cellHeight;
        }
        return drawn;
    }

    /**
     * Draws the specified Line of glyphs with a Batch at a given x, y position, drawing the full Line.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned as part of {@link #markup(String, Layout)}
     * @param x the x position in world space to start drawing the glyph at (lower left corner)
     * @param y the y position in world space to start drawing the glyph at (lower left corner)
     * @return the number of glyphs drawn
     */
    public int drawGlyphs(Batch batch, Line glyphs, float x, float y) {
        if(glyphs == null) return 0;
        return drawGlyphs(batch, glyphs, x, y, Align.left);
    }
    /**
     * Draws the specified Line of glyphs with a Batch at a given x, y position, using {@code align} to
     * determine how to position the text. Typically, align is {@link Align#left}, {@link Align#center}, or
     * {@link Align#right}, which make the given x,y point refer to the lower-left corner, center-bottom edge point, or
     * lower-right corner, respectively.
     * @param batch typically a SpriteBatch
     * @param glyphs typically returned as part of {@link #markup(String, Layout)}
     * @param x the x position in world space to start drawing the glyph at (where this is depends on align)
     * @param y the y position in world space to start drawing the glyph at (where this is depends on align)
     * @param align an {@link Align} constant; if {@link Align#left}, x and y refer to the lower left corner
     * @return the number of glyphs drawn
     */
    public int drawGlyphs(Batch batch, Line glyphs, float x, float y, int align) {
        if(glyphs == null) return 0;
        int drawn = 0;
        if(Align.isCenterHorizontal(align))
            x -= glyphs.width * 0.5f;
        else if(Align.isRight(align))
            x -= glyphs.width;
        if(kerning != null) {
            int kern = -1;
            float amt = 0;
            long glyph;
            for (int i = 0, n = glyphs.glyphs.size(); i < n; i++, drawn++) {
                kern = kern << 16 | (int) ((glyph = glyphs.glyphs.get(i)) & 0xFFFF);
                amt = kerning.getOrDefault(kern, 0) * scaleX;
                x += drawGlyph(batch, glyph, x + amt, y) + amt;
            }
        }
        else {
            for (int i = 0, n = glyphs.glyphs.size(); i < n; i++, drawn++) {
                x += drawGlyph(batch, glyphs.glyphs.get(i), x, y);
            }
        }
        return drawn;
    }

    /**
     * Gets the distance to advance the cursor after drawing {@code glyph}, scaled by {@link #scaleX} as if drawing.
     * This handles monospaced fonts correctly and ensures that for variable-width fonts, subscript, midscript, and
     * superscript halve the advance amount. This does not consider kerning, if the font has it.
     * @param glyph a long encoding the color, style information, and char of a glyph, as from a {@link Line}
     * @return the (possibly non-integer) amount to advance the cursor when you draw the given glyph, not counting kerning
     */
    public float xAdvance(long glyph){
        GlyphRegion tr = mapping.get((char) glyph);
        if (tr == null) return 0f;
        float changedW = tr.xAdvance * scaleX;
        if (isMono) {
            changedW += tr.offsetX * scaleX;
        }
        else if((glyph & SUPERSCRIPT) != 0L){
            changedW *= 0.5f;
        }
        return changedW;
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
        GlyphRegion tr = mapping.get((char) glyph);
        if (tr == null) return 0f;
        Texture tex = tr.getTexture();
        float x0 = 0f, x1 = 0f, x2 = 0f, x3 = 0f;
        float y0 = 0f, y1 = 0f, y2 = 0f, y3 = 0f;
        float color = BitConversion.reversedIntBitsToFloat(((int) (glyph >>> 32) & -256) | (int)(batch.getColor().a * 255.999f));
        final float iw = 1f / tex.getWidth();
        float u, v, u2, v2;
        u = tr.getU();
        v = tr.getV();
        u2 = tr.getU2();
        v2 = tr.getV2();
        float w = tr.getRegionWidth() * scaleX, changedW = tr.xAdvance * scaleX, h = tr.getRegionHeight() * scaleY;
        if (isMono) {
            changedW += tr.offsetX * scaleX;
        } else {
            x += tr.offsetX * scaleX;
        }
        float yt = y + cellHeight - h - tr.offsetY * scaleY;
        if ((glyph & OBLIQUE) != 0L) {
            x0 += h * 0.2f;
            x1 -= h * 0.2f;
            x2 -= h * 0.2f;
            x3 += h * 0.2f;
        }
        final long script = (glyph & SUPERSCRIPT);
        if (script == SUPERSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y1 += cellHeight * 0.375f;
            y2 += cellHeight * 0.375f;
            y0 += cellHeight * 0.375f;
            y3 += cellHeight * 0.375f;
            if(!isMono)
                changedW *= 0.5f;
        }
        else if (script == SUBSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y1 -= cellHeight * 0.125f;
            y2 -= cellHeight * 0.125f;
            y0 -= cellHeight * 0.125f;
            y3 -= cellHeight * 0.125f;
            if(!isMono)
                changedW *= 0.5f;
        }
        else if(script == MIDSCRIPT) {
            w *= 0.5f;
            h *= 0.5f;
            y0 += cellHeight * 0.125f;
            y1 += cellHeight * 0.125f;
            y2 += cellHeight * 0.125f;
            y3 += cellHeight * 0.125f;
            if(!isMono)
                changedW *= 0.5f;
        }

        vertices[0] = x + x0;
        vertices[1] = yt + y0 + h;
        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;

        vertices[5] = x + x1;
        vertices[6] = yt + y1;
        vertices[7] = color;
        vertices[8] = u;
        vertices[9] = v2;

        vertices[10] = x + x2 + w;
        vertices[11] = yt + y2;
        vertices[12] = color;
        vertices[13] = u2;
        vertices[14] = v2;

        vertices[15] = x + x3 + w;
        vertices[16] = yt + y3 + h;
        vertices[17] = color;
        vertices[18] = u2;
        vertices[19] = v;
        batch.draw(tex, vertices, 0, 20);
        if ((glyph & BOLD) != 0L) {
            vertices[0] +=  1f;
            vertices[5] +=  1f;
            vertices[10] += 1f;
            vertices[15] += 1f;
            batch.draw(tex, vertices, 0, 20);
            vertices[0] -=  2f;
            vertices[5] -=  2f;
            vertices[10] -= 2f;
            vertices[15] -= 2f;
            batch.draw(tex, vertices, 0, 20);
            vertices[0] +=  0.5f;
            vertices[5] +=  0.5f;
            vertices[10] += 0.5f;
            vertices[15] += 0.5f;
            batch.draw(tex, vertices, 0, 20);
            vertices[0] +=  1f;
            vertices[5] +=  1f;
            vertices[10] += 1f;
            vertices[15] += 1f;
            batch.draw(tex, vertices, 0, 20);

        }
        if ((glyph & UNDERLINE) != 0L) {
            final GlyphRegion under = mapping.get('_');
            if (under != null) {
                final float underU = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.25f,
                        underV = under.getV(),
                        underU2 = under.getU() + (under.xAdvance - under.offsetX) * iw * 0.75f,
                        underV2 = under.getV2(),
                        hu = under.getRegionHeight() * scaleY, yu = y + cellHeight - hu - under.offsetY * scaleY;
                vertices[0] = x - 1f;
                vertices[1] = yu + hu;
                vertices[2] = color;
                vertices[3] = underU;
                vertices[4] = underV;

                vertices[5] = x - 1f;
                vertices[6] = yu;
                vertices[7] = color;
                vertices[8] = underU;
                vertices[9] = underV2;

                vertices[10] = x + changedW + 1f;
                vertices[11] = yu;
                vertices[12] = color;
                vertices[13] = underU2;
                vertices[14] = underV2;

                vertices[15] = x + changedW + 1f;
                vertices[16] = yu + hu;
                vertices[17] = color;
                vertices[18] = underU2;
                vertices[19] = underV;
                batch.draw(under.getTexture(), vertices, 0, 20);
            }
        }
        if ((glyph & STRIKETHROUGH) != 0L) {
            final GlyphRegion dash = mapping.get('-');
            if (dash != null) {
                final float dashU = dash.getU() + (dash.xAdvance - dash.offsetX) * iw * 0.625f,
                        dashV = dash.getV(),
                        dashU2 = dashU + iw,
                        dashV2 = dash.getV2(),
                        hd = dash.getRegionHeight() * scaleY, yd = y + cellHeight - hd - dash.offsetY * scaleY;
                x0 = x - (dash.offsetX);
                vertices[0] = x0 - 1f;
                vertices[1] = yd + hd;
                vertices[2] = color;
                vertices[3] = dashU;
                vertices[4] = dashV;

                vertices[5] = x0 - 1f;
                vertices[6] = yd;
                vertices[7] = color;
                vertices[8] = dashU;
                vertices[9] = dashV2;

                vertices[10] = x0 + changedW + 1f;
                vertices[11] = yd;
                vertices[12] = color;
                vertices[13] = dashU2;
                vertices[14] = dashV2;

                vertices[15] = x0 + changedW + 1f;
                vertices[16] = yd + hd;
                vertices[17] = color;
                vertices[18] = dashU2;
                vertices[19] = dashV;
                batch.draw(dash.getTexture(), vertices, 0, 20);
            }
        }
        return changedW;
    }

    /**
     * Reads markup from text, along with the chars to receive markup, processes it, and appends into appendTo, which is
     * a {@link Layout} holding one or more {@link Line}s. A common way of getting a Layout is with
     * {@code Pools.obtain(Layout.class)}; you can free the Layout when you are done using it with
     * {@link Pools#free(Object)}. This parses an extension of libGDX markup and uses it to determine color, size,
     * position, shape, strikethrough, underline, and case of the given CharSequence. The text drawn will start as
     * white, with the normal size as determined by the font's metrics and scale ({@link #scaleX} and {@link #scaleY}),
     * normal case, and without bold, italic, superscript, subscript, strikethrough, or underline. Markup starts with
     * {@code [}; the next character determines what that piece of markup toggles. Markup this knows:
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
     *     <li>{@code [COLORNAME]}, where "COLORNAME" is a typically-upper-case color name that will be looked up in
     *     {@link Colors}, changes the color.</li>
     * </ul>
     * You can render {@code appendTo} using {@link #drawGlyphs(Batch, Layout, float, float)}.
     * @param text text with markup
     * @param appendTo a Layout that stores one or more Line objects, carrying color, style, chars, and size
     * @return appendTo, for chaining
     */
    public Layout markup(String text, Layout appendTo) {
        boolean capitalize = false, previousWasLetter = false,
                capsLock = false, lowerCase = false;
        int c;
        final long COLOR_MASK = 0xFFFFFFFF00000000L;
        long baseColor = (long) (appendTo.getBaseColor()) << 32;
        long color = baseColor;
        long current = color;
        if(appendTo.font == null || !appendTo.font.equals(this))
        {
            appendTo.clear();
            appendTo.font(this);
        }
        appendTo.peekLine().height = cellHeight;
        float targetWidth = appendTo.getTargetWidth();
        int kern = -1;
        for (int i = 0, n = text.length(); i < n; i++) {
            if(text.charAt(i) == '['){
                if(++i < n && (c = text.charAt(i)) != '['){
                    if(c == ']'){
                        color = baseColor;
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
                            if ((current & SUPERSCRIPT) == SUPERSCRIPT)
                                current &= ~SUPERSCRIPT;
                            else
                                current |= SUPERSCRIPT;
                            break;
                        case '.':
                            if ((current & SUPERSCRIPT) == SUBSCRIPT)
                                current &= ~SUBSCRIPT;
                            else
                                current = (current & ~SUPERSCRIPT) | SUBSCRIPT;
                            break;
                        case '=':
                            if ((current & SUPERSCRIPT) == MIDSCRIPT)
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
                                color = baseColor;
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
                                Color gdxColor = Colors.get(text.substring(i, i + len));
                                if(gdxColor == null) color = baseColor;
                                else color = (long) Color.rgba8888(gdxColor) << 32;
                            }
                            current = (current & ~COLOR_MASK) | color;
                    }
                    i += len;
                }
                else {
                    float w;
                    if (kerning == null) {
                        w = (appendTo.peekLine().width += xAdvance(current | '['));
                    } else {
                        kern = kern << 16 | '[';
                        w = (appendTo.peekLine().width += xAdvance(current | '[') + kerning.getOrDefault(kern, 0) * scaleX);
                    }
                    appendTo.add(current | '[');
                    if(targetWidth > 0 && w > targetWidth) {
                        Line earlier = appendTo.peekLine();
                        Line later = appendTo.pushLine();
                        if(later == null){
                            // here, the max lines have been reached, and an ellipsis may need to be added
                            // to the last line.
                            if(appendTo.ellipsis != null) {
                                for (int j = earlier.glyphs.size() - 1 - appendTo.ellipsis.length(); j >= 0; j--) {
                                    int leading = 0;
                                    while (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size(), (char) earlier.glyphs.get(j)) >= 0) {
                                        ++leading;
                                        --j;
                                    }
                                    float change = 0f, changeNext = 0f;
                                    long currE, curr;
                                    if (kerning == null) {
                                        for (int k = j + 1, e = 0; k < earlier.glyphs.size(); k++, e++) {
                                            change += xAdvance(earlier.glyphs.get(k));
                                            if (--leading < 0 && (e < appendTo.ellipsis.length())) {
                                                float adv = xAdvance(currE = baseColor | appendTo.ellipsis.charAt(e));
//                                                appendTo.add(currE);
                                                changeNext += adv;
                                            }
                                        }
                                    } else {
                                        int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                        int k2e = appendTo.ellipsis.charAt(0) & 0xFFFF, k3e = -1;
                                        for (int k = j + 1, e = 0; k < earlier.glyphs.size(); k++, e++) {
                                            currE = baseColor | appendTo.ellipsis.charAt(e);
                                            curr = earlier.glyphs.get(k);
                                            k2 = k2 << 16 | (char) curr;
                                            k2e = k2e << 16 | (char) currE;
                                            float adv = xAdvance(curr);
                                            change += adv + kerning.getOrDefault(k2, 0) * scaleX;
                                            if (--leading < 0 && (e < appendTo.ellipsis.length())) {
                                                changeNext += xAdvance(currE) + kerning.getOrDefault(k2e, 0) * scaleX;
//                                                appendTo.add(currE);
                                            }
                                        }
                                    }
                                    earlier.glyphs.truncate(j + 1);
                                    for (int e = 0; e < appendTo.ellipsis.length(); e++) {
                                        earlier.glyphs.add(baseColor | appendTo.ellipsis.charAt(e));
                                    }
                                    earlier.width = earlier.width - change + changeNext;
                                    return appendTo;
                                }
                            }
                        }
                        else {
                            for (int j = earlier.glyphs.size() - 2; j >= 0; j--) {
                                if (Arrays.binarySearch(breakChars.items, 0, breakChars.size(), (char) earlier.glyphs.get(j)) >= 0) {
                                    int leading = 0;
                                    while (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size(), (char) earlier.glyphs.get(j)) >= 0) {
                                        ++leading;
                                        --j;
                                    }
                                    float change = 0f, changeNext = 0f;
                                    long curr;
                                    if (kerning == null) {
                                        for (int k = j + 1; k < earlier.glyphs.size(); k++) {
                                            float adv = xAdvance(curr = earlier.glyphs.get(k));
                                            change += adv;
                                            if (--leading < 0) {
                                                appendTo.add(curr);
                                                changeNext += adv;
                                            }
                                        }
                                    } else {
                                        int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                        for (int k = j + 1; k < earlier.glyphs.size(); k++) {
                                            curr = earlier.glyphs.get(k);
                                            k2 = k2 << 16 | (char) curr;
                                            float adv = xAdvance(curr);
                                            change += adv + kerning.getOrDefault(k2, 0) * scaleX;
                                            if (--leading < 0) {
                                                k3 = k3 << 16 | (char) curr;
                                                changeNext += adv + kerning.getOrDefault(k3, 0) * scaleX;
                                                appendTo.add(curr);
                                            }
                                        }
                                    }
                                    earlier.glyphs.truncate(j + 1);
                                    later.width = changeNext;
                                    earlier.width -= change;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                char ch = text.charAt(i);
                if (isLowerCase(ch)) {
                    if ((capitalize && !previousWasLetter) || capsLock) {
                        ch = Character.toUpperCase(ch);
                    }
                    previousWasLetter = true;
                } else if (isUpperCase(ch)) {
                    if ((capitalize && previousWasLetter) || lowerCase) {
                        ch = Character.toLowerCase(ch);
                    }
                    previousWasLetter = true;
                } else {
                    previousWasLetter = false;
                }
                float w;
                if (kerning == null) {
                    w = (appendTo.peekLine().width += xAdvance(current | ch));
                } else {
                    kern = kern << 16 | (int) ((current | ch) & 0xFFFF);
                    w = (appendTo.peekLine().width += xAdvance(current | ch) + kerning.getOrDefault(kern, 0) * scaleX);
                }
                appendTo.add(current | ch);
                if((targetWidth > 0 && w > targetWidth) || appendTo.atLimit) {
                    Line earlier = appendTo.peekLine();
                    Line later = appendTo.pushLine();
                    if(later == null){
                        // here, the max lines have been reached, and an ellipsis may need to be added
                        // to the last line.
                        if(appendTo.ellipsis != null) {
                            for (int j = earlier.glyphs.size() - 1; j >= 0; j--) {
                                int leading = 0;
                                while (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size(), (char) earlier.glyphs.get(j)) < 0 && j > 0) {
                                    ++leading;
                                    --j;
                                }
                                while (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size(), (char) earlier.glyphs.get(j)) >= 0 && j > 0) {
                                    ++leading;
                                    --j;
                                }
                                float change = 0f, changeNext = 0f;
                                long currE, curr;
                                if (kerning == null) {
                                    for (int k = j + 1, e = 0; k < earlier.glyphs.size(); k++, e++) {
                                        change += xAdvance(earlier.glyphs.get(k));
                                        if ((e < appendTo.ellipsis.length())) {
                                            float adv = xAdvance(currE = baseColor | appendTo.ellipsis.charAt(e));
//                                                appendTo.add(currE);
                                            changeNext += adv;
                                        }
                                    }
                                } else {
                                    int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                    int k2e = appendTo.ellipsis.charAt(0) & 0xFFFF, k3e = -1;
                                    for (int k = j + 1, e = 0; k < earlier.glyphs.size(); k++, e++) {
                                        curr = earlier.glyphs.get(k);
                                        k2 = k2 << 16 | (char) curr;
                                        float adv = xAdvance(curr);
                                        change += adv + kerning.getOrDefault(k2, 0) * scaleX;
                                        if ((e < appendTo.ellipsis.length())) {
                                            currE = baseColor | appendTo.ellipsis.charAt(e);
                                            k2e = k2e << 16 | (char) currE;
                                            changeNext += xAdvance(currE) + kerning.getOrDefault(k2e, 0) * scaleX;
//                                                appendTo.add(currE);
                                        }
                                    }
                                }
                                if (earlier.width + changeNext < appendTo.getTargetWidth()) {
                                    for (int e = 0; e < appendTo.ellipsis.length(); e++) {
                                        earlier.glyphs.add(baseColor | appendTo.ellipsis.charAt(e));
                                    }
                                    earlier.width = earlier.width + changeNext;
                                    return appendTo;
                                }
                                if (earlier.width - change + changeNext < appendTo.getTargetWidth()) {
                                    earlier.glyphs.truncate(j + 1);
                                    for (int e = 0; e < appendTo.ellipsis.length(); e++) {
                                        earlier.glyphs.add(baseColor | appendTo.ellipsis.charAt(e));
                                    }
                                    earlier.width = earlier.width - change + changeNext;
                                    return appendTo;
                                }
                            }
                        }
                    }
                    else {
                        for (int j = earlier.glyphs.size() - 2; j >= 0; j--) {
                            if (Arrays.binarySearch(breakChars.items, 0, breakChars.size(), (char) earlier.glyphs.get(j)) >= 0) {
                                int leading = 0;
                                while (Arrays.binarySearch(spaceChars.items, 0, spaceChars.size(), (char) earlier.glyphs.get(j)) >= 0) {
                                    ++leading;
                                    --j;
                                }
                                float change = 0f, changeNext = 0f;
                                long curr;
                                if (kerning == null) {
                                    for (int k = j + 1; k < earlier.glyphs.size(); k++) {
                                        float adv = xAdvance(curr = earlier.glyphs.get(k));
                                        change += adv;
                                        if (--leading < 0) {
                                            appendTo.add(curr);
                                            changeNext += adv;
                                        }
                                    }
                                } else {
                                    int k2 = ((int) earlier.glyphs.get(j) & 0xFFFF), k3 = -1;
                                    for (int k = j + 1; k < earlier.glyphs.size(); k++) {
                                        curr = earlier.glyphs.get(k);
                                        k2 = k2 << 16 | (char) curr;
                                        float adv = xAdvance(curr);
                                        change += adv + kerning.getOrDefault(k2, 0) * scaleX;
                                        if (--leading < 0) {
                                            k3 = k3 << 16 | (char) curr;
                                            changeNext += adv + kerning.getOrDefault(k3, 0) * scaleX;
                                            appendTo.add(curr);
                                        }
                                    }
                                }
                                earlier.glyphs.truncate(j + 1);
                                later.width = changeNext;
                                earlier.width -= change;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return appendTo;
    }

    /**
     * Releases all resources of this object.
     */
    @Override
    public void dispose() {
        Pools.free(tempLayout);
        if(shader != null)
            shader.dispose();
    }
}
