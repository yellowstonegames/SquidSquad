package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Collection;
import java.util.function.Function;

public class Director<C, G extends Glider> {
    public Function<? super C, ? extends G> extractor;
    public Collection<C> container;

    protected long playTime = Long.MAX_VALUE;
    protected boolean playing = false;
    protected long duration = 500L;
    protected long elapsed = 0L;

    public Director(Function<? super C, ? extends G> fun){
        extractor = fun;
    }

    public Director(Function<? super C, ? extends G> fun, Collection<C> coll){
        this(fun);
        container = coll;
    }

    public Director(Function<? super C, ? extends G> fun, Collection<C> coll, long durationMillis){
        this(fun);
        container = coll;
        duration = durationMillis;
    }

    public void play() {
        playTime = TimeUtils.millis();
        playing = true;
    }

    public void pause(){
        elapsed += TimeUtils.timeSinceMillis(playTime);
        playing = false;
    }

    public void stop(){
        elapsed = 0L;
        playing = false;
    }

    public void step(){
        if(!playing) return;
        float fraction = (float) (elapsed + TimeUtils.timeSinceMillis(playTime)) / duration;
        float change = MathUtils.clamp(fraction, 0f, 1f);
        for(C con : container) {
            extractor.apply(con).setChange(change);
        }
        if(fraction > change) {
            stop();
        }
    }
}
