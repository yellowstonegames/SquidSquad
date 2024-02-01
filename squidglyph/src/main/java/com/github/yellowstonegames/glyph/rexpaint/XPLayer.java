/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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
