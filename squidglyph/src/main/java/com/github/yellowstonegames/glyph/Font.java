package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.yellowstonegames.core.DigitTools;
    
public class Font {
    public IntObjectMap<TextureRegion> mapping;
    public Texture parentTexture;

    private static int indexAfter(String text, String search, int from){
        return text.indexOf(search, from < 0 ? text.length() : from) + search.length();
    }
    public Font(String fntName, String textureName, boolean isMSDF) {
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
        int size = DigitTools.intFromBin(fnt, idx, idx = indexAfter(fnt, "\nchar id=", idx));
        mapping = new IntObjectMap<>(size);
        for (int i = 0; i < size ; i++) {
            int c = DigitTools.intFromBin(fnt, idx, idx = indexAfter(fnt, "\n x=", idx));
            int x = DigitTools.intFromBin(fnt, idx, idx = indexAfter(fnt, "\n y=", idx));
            int y = DigitTools.intFromBin(fnt, idx, idx = indexAfter(fnt, "\n width=", idx));
            int w = DigitTools.intFromBin(fnt, idx, idx = indexAfter(fnt, "\n height=", idx));
            int h = DigitTools.intFromBin(fnt, idx, idx = indexAfter(fnt, "\nchar id=", idx));
            mapping.put(c, new TextureRegion(parentTexture, x, y, w, h));
        }
    }
}
