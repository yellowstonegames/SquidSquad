package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.support.TricycleRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.tommyettinger.textra.Font;
public class ScriptGenerator extends ApplicationAdapter {
    Font font;
    TricycleRandom random;
//    WaveFunctionCollapse wfc;
    int[][] source;
    IntObjectOrderedMap<int[][]> glyphSections;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Script Generator");
        config.setWindowedMode(720, 640);
        config.disableAudio(true);
        config.useVsync(true);
        config.setForegroundFPS(1);
        new Lwjgl3Application(new ScriptGenerator(), config);
    }

    @Override
    public void create() {
        font = new Font("Cozette-standard.fnt");
        random = new TricycleRandom(1234567890L);
        Pixmap pix = new Pixmap(Gdx.files.classpath("Cozette-standard.png"));
        final int w = pix.getWidth(), h = pix.getHeight();
        source = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                source[x][y] = pix.getPixel(x, y);
            }
        }
//        wfc = new WaveFunctionCollapse(source, 4, 128, 128, false, false, 1, 0);
        char[] alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        glyphSections = new IntObjectOrderedMap<>(alpha.length);
        for (int c : alpha){
            Font.GlyphRegion gr = font.mapping.get(c);
            assert gr != null;
            final int gw = gr.getRegionWidth(), gh = gr.getRegionHeight(), gx = gr.getRegionX(), gy = gr.getRegionY();
            int[][] section = new int[gw][gh];
            for (int i = 0; i < gw; i++) {
                System.arraycopy(source[gx + i], gy, section[i], 0, gh);
            }
            glyphSections.put(c, section);
//            int b = 0, pos = 1;
//            for (int i = -1; i < gw - 4; i++) {
//                for (int j = -1; j < gh - 4; j++) {
//
//                }
//            }
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void render() {
        int[][] res = new int[source.length][source[0].length];
        int x = 0, y = 0;
        for(int[][] section : glyphSections.values()) {
            ArrayTools.insert(section, res, x, y);
            x += section.length;
            if (x >= 100) {
                x = 0;
                y += section[0].length;
            }
        }
//        int count = 0;
//        while (true){
//            if(wfc.run(random, 100000)) {
//                res = wfc.result();
//                int sum = 0;
//                for (int y = 0; y < res[0].length; y++) {
//                    for (int x = 0; x < res.length; x++) {
//                        sum |= res[x][y];
//                    }
//                }
//                if(sum != 0)
//                    break;
//                else
//                    System.out.println("Made an empty result");
//            }
//            System.out.println("made " + (count += 100000) + " attempts");
//        }

        System.out.println();
        for (int j = 0; j < res[0].length; j++) {
            for (int i = 0; i < res.length; i++) {
                switch (res[i][j])
                {
                    case 0xFF:
                        System.out.print('#');
                        break;
                    case 0xFFFFFFFF:
                        System.out.print('.');
                        break;
                    default:
                        System.out.print(' ');
                        break;
                }
            }
            System.out.println();
        }
        Gdx.app.exit();
    }
}
