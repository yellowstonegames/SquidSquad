package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.QualityPalette;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.textra.*;

import java.nio.ByteBuffer;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class AnimatedLogoGenerator extends ApplicationAdapter {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 200;
    public static final int FRAMERATE = 60;
    public static final int FRAMES = FRAMERATE * 3;
    int frame = 0;
    ScreenViewport viewport;
    SpriteBatch batch;
    Font[] fonts = new Font[10];
    AnimatedGif gif;
    Array<Pixmap> pms = new Array<>(Pixmap.class);
    String squid = " Squid....";
    String squad = " Squad....";

    @Override
    public void create() {
        viewport = new ScreenViewport();
        gif = new AnimatedGif();

        batch = new SpriteBatch();

        for (int i = 0; i < fonts.length; i++) {
            fonts[i] = KnownFonts.getTillana(Font.DistanceFieldType.SDF).scaleHeightTo(80f - i * 6);
        }

        for (int i = 0; i < FRAMES; i++) {
            render();
            frame++;
        }
        QualityPalette pal = new QualityPalette();
        pal.analyze(pms);
        gif.setPalette(pal);
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.MARTEN);
        gif.setDitherStrength(0.2f);
        gif.write(Gdx.files.local("out/logo.gif"), pms, FRAMERATE);
        Gdx.app.exit();

    }

    @Override
    public void render() {
        ScreenUtils.clear(0.05f, 0.15f, 0.35f, 1);

        float time = (frame / (float)FRAMES);
//        viewport.apply(true);
        batch.begin();

        float x0 = 10, x1 = 10, y = 150;
        for (int i = 0, n = squid.length(); i < n; i++) {
            long glyph0 = fonts[i].markupGlyph(squid.charAt(i), "[#B9F][#]");
            long glyph1 = fonts[i].markupGlyph(squad.charAt(i), "[#B9F][#]");
            fonts[i].enableShader(batch);
//            fonts[i].resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), viewport);
            x0 += fonts[i].drawGlyph(batch, glyph0, x0, y + TrigTools.sinSmootherTurns(time + (i / (float)n)) * i, 0, 1, 1, 0, 2f);
            x1 += fonts[i].drawGlyph(batch, glyph1, x1, y + TrigTools.sinSmootherTurns(time + (i / (float)n)) * i - 100, 0, 1, 1, 0, 2f);
        }
        batch.end();

        // Modified Pixmap.createFromFrameBuffer() code that uses RGB instead of RGBA
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        final Pixmap pm = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
        ByteBuffer pixels = pm.getPixels();
        Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
        // End Pixmap.createFromFrameBuffer() modified code

        pms.add(pm);
//        pms.add(Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    }

    @Override
    public void resize(int width, int height) {
//        label.getFont().resizeDistanceField(width, height);
        viewport.update(width, height, true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Logo generator");
        config.setWindowedMode(WIDTH, HEIGHT);
        config.setResizable(true);
        config.setForegroundFPS(FRAMERATE);
        config.useVsync(true);
        config.disableAudio(true);
        new Lwjgl3Application(new AnimatedLogoGenerator(), config);
    }
}
