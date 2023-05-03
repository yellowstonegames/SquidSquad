package com.github.yellowstonegames.glyph.rexpaint;

import com.github.tommyettinger.ds.ObjectList;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
/**
 * Allows importing REXPaint's .xp files to a format SquidGlyph can read and display.
 * Imported from <a href="https://github.com/biscon/xpreader">biscon's xpreader project</a>.
 * <br>
 * Created by bison on 02-01-2016.
 */
public class REXReader {
    static public XPFile loadXP(String filename) throws IOException {
        byte[] compressed = Files.readAllBytes(new File(filename).toPath());
//        System.out.println("Size of compressed data: " + compressed.length);
        byte[] decompressed = CompressionUtils.gzipDecodeByteArray(compressed);
//        System.out.println("Size of decompressed data: " + decompressed.length);
        ByteBuffer bb = ByteBuffer.wrap(decompressed);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int version = bb.getInt();
        int layerCount = bb.getInt();
//        System.out.println("version is " + version + ", layers = " + layerCount);
        ObjectList<XPLayer> layers = new ObjectList<>();

        // x-value with "index/height", and y-value with "index%height"

        for(int i=0; i < layerCount; i++) {
            XPLayer layer = new XPLayer();
            layer.width = bb.getInt();
            layer.height = bb.getInt();
            layer.data = new XPChar[layer.width][layer.height];
            for(int index = 0; index < (layer.width*layer.height); index++)
            {
                int x = index / layer.height;
                int y = index % layer.height;
                char ch = (char) bb.getInt();

                byte fR = bb.get();
                byte fG = bb.get();
                byte fB = bb.get();
                byte bR = bb.get();
                byte bG = bb.get();
                byte bB = bb.get();
                int fgcol = pack(fR, fG, fB, 255);
                int bgcol = pack(bR, bG, bB, 255);
                //System.out.println("char=" + int_ch + ", fgcol =" + fgcol + ", bgcol =" + bgcol);
                XPChar xpch = new XPChar();
                xpch.fgColor = fgcol;
                xpch.bgColor = bgcol;
                xpch.code = ch;
                layer.data[x][y] = xpch;
            }
            layers.add(layer);
        }
        return new XPFile(version, layerCount, layers);
    }

    public static int pack(int r, int g, int b, int a)
    {
//        return (r << 24) | (g << 16) | (b << 8) | (a);
        return (r << 24) |
                ((g & 0xFF) << 16) |
                ((b & 0xFF) << 8)  |
                ((a & 0xFF));
    }
}
