package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

/**
 * Handles multiple objects that are each associated with a {@link Glider}, and allows starting and stopping the gliding
 * animation for all of those objects at once. This must be given a Function that can extract some form of Glider out of
 * a C object; this is typically a method reference, such as to a getter. A Director has a Collection of C objects,
 * often an {@link com.github.tommyettinger.ds.ObjectList} or perhaps the Values collection of an
 * {@link com.github.tommyettinger.ds.ObjectObjectMap}, that it will go through and update the Glider in each C. User
 * code typically calls {@link #step()} every frame; this is a no-op if the Director is not playing. If the Director is
 * playing, then step() calculates the amount of the glide animation that has been completed, loops over each C in the
 * Collection, extracts a Glider from each C, calls {@link Glider#setChange(float)} on those Gliders with the calculated
 * amount, and potentially calls {@link #stop()} if the animation has completed. You start the Director playing with
 * {@link #play()}, and can pause it temporarily with {@link #pause()} (you resume a paused Director with play() again),
 * or stop the Director entirely with {@link #stop()} (which sets it back to the beginning of the animation).
 * @param <C> a type that contains or is otherwise associated with a {@link Glider}, such as a {@link CoordGlider}
 */
public class Director<C> {
    public @Nonnull Function<? super C, ? extends Glider> extractor;
    public Collection<C> container;

    protected long playTime = Long.MAX_VALUE;
    protected boolean playing = false;
    protected long duration = 500L;
    protected long elapsed = 0L;

    public Director(@Nonnull Function<? super C, ? extends Glider> fun){
        extractor = fun;
    }

    public Director(@Nonnull Function<? super C, ? extends Glider> fun, Collection<C> coll){
        this(fun);
        container = coll;
    }

    public Director(@Nonnull Function<? super C, ? extends Glider> fun, Collection<C> coll, long durationMillis){
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
        if(!playing || container == null) return;
        float fraction = (float) (elapsed + TimeUtils.timeSinceMillis(playTime)) / duration;
        float change = MathUtils.clamp(fraction, 0f, 1f);
        for(C con : container) {
            extractor.apply(con).setChange(change);
        }
        if(fraction > change) {
            stop();
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    /**
     * Gets the duration of each glide animation, in milliseconds. The default duration is 500 ms if unspecified.
     * @return the duration of each glide, in milliseconds.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration of each glide, in milliseconds, and if the duration changed, calls {@link #stop()}. The
     * default duration is 500 ms if unspecified.
     * @param duration how long each glide animation should take, in milliseconds
     */
    public void setDuration(long duration) {
        if(this.duration != (this.duration = duration)) stop();
    }
}
