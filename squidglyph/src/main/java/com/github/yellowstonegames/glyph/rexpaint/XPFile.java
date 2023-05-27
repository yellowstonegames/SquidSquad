/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.glyph.GlyphGrid;

/**
 * Imported from <a href="https://github.com/biscon/xpreader">biscon's xpreader project</a>.
 * <br>
 * Created by bison on 02-01-2016.
 */
public class XPFile {
    protected int version;
    protected int layerCount;
    protected ObjectList<XPLayer> layers;

    public XPFile(int version, int layerCount, ObjectList<XPLayer> layers) {
        this.version = version;
        this.layerCount = layerCount;
        this.layers = layers;
    }
    public void intoGlyphGrid(GlyphGrid terminal)
    {
        if(layers.size() < 1)
            return;
        if(terminal.backgrounds == null)
            terminal.backgrounds = new int[layers.get(0).width][layers.get(0).height];
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).intoGlyphGrid(terminal);
        }
    }

    public XPLayer getLayer(int i)
    {
        return layers.get(i);
    }

    public int layerCount()
    {
        return layerCount;
    }
}
