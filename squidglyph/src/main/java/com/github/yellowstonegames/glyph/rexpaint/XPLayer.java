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

    public void intoGlyphGrid(GlyphGrid terminal)
    {
        for(int y=0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (data[x][y].code != 32 && (data[x][y].fgColor & 0xFE) != 0) {
                    terminal.put(x, height - 1 - y, data[x][y].code, data[x][y].fgColor);
                }
                if ((data[x][y].bgColor & 0xFE) != 0) {
                    terminal.backgrounds[x][height - 1 - y] = data[x][y].bgColor;
                }
            }
        }
    }
}
