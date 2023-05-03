package com.github.yellowstonegames.glyph.rexpaint;

import com.github.yellowstonegames.glyph.GlyphGrid;

/**
 * Imported from <a href="https://github.com/biscon/xpreader">biscon's xpreader project</a>.
 * <br>
 * Created by bison on 02-01-2016.
 */
public class XPLayer
{
    public int width;
    public int height;
    public XPChar[][] data;

    public void draw(GlyphGrid terminal)
    {
        for(int y=0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (data[x][y].code != 32) {
                    terminal.put(x, y, data[x][y].code, data[x][y].fgColor);
                    terminal.backgrounds[x][y] = data[x][y].bgColor;
                }
            }
        }
    }
}
