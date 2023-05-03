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
    public void draw(GlyphGrid terminal)
    {
        if(layers.size() < 1)
            return;
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).draw(terminal);
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
