package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.tommyettinger.ds.support.TricycleRandom;
import com.github.yellowstonegames.grid.WaveFunctionCollapse;

public class ScriptGenerator extends ApplicationAdapter {
    Font font;
    TricycleRandom random;
    WaveFunctionCollapse wfc;
    int[][] source;

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
        font = new Font("CozetteAlphanumeric.fnt");
        random = new TricycleRandom(1234567890L);
        Pixmap pix = new Pixmap(Gdx.files.classpath("CozetteAlphanumeric.png"));
        final int w = pix.getWidth(), h = pix.getHeight();
        source = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                source[x][y] = pix.getPixel(x, y);
            }
        }
        wfc = new WaveFunctionCollapse(source, 4, 16, 16, false, false, 8, 0);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void render() {
        int count = 0;
        int[][] res;
        while (true){
            if(wfc.run(random, 1000)) {
                res = wfc.result();
                int sum = 0;
                for (int y = 0; y < res[0].length; y++) {
                    for (int x = 0; x < res.length; x++) {
                        sum |= res[x][y];
                    }
                }
                if(sum != 0)
                    break;
            }
            System.out.println("made " + (count += 1000) + "attempts");
        }

        System.out.println();
        for (int y = 0; y < res[0].length; y++) {
            for (int x = 0; x < res.length; x++) {
                switch (res[x][y])
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
    }
}
