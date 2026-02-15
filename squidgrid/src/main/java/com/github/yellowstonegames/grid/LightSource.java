package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.BitConversion;
import com.github.yellowstonegames.core.ISerializersNeeded;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.util.Arrays;
import java.util.List;

/**
 * A tiny data wrapper around a {@link #radiance} and a Coord {@link #position} for that Radiance, as well as,
 * optionally, an angle to project a cone of light in and a span for how wide of a cone to project.
 */
public class LightSource implements ISerializersNeeded {
    public Radiance radiance;
    public Coord position;
    /**
     * How wide of an arc the light source covers, measured in turns (0-1).
     */
    public float span = 1f;
    /**
     * The angle the light source emits light towards, measured in turns (0-1, counterclockwise from right).
     * If span is 1 or greater, this has no effect.
     */
    public float direction;

    public LightSource() {
        this(Coord.get(0,0), new Radiance(), 1f, 0f);
    }

    public LightSource(Coord position, Radiance radiance) {
        this(position, radiance, 1f, 0f);
    }
    public LightSource(Coord position, Radiance radiance, float spanTurns, float directionTurns) {
        this.radiance = radiance;
        this.position = position;
        this.span = spanTurns;
        this.direction = directionTurns;
    }

    public Radiance getRadiance() {
        return radiance;
    }

    public void setRadiance(Radiance radiance) {
        this.radiance = radiance;
    }

    public Coord getPosition() {
        return position;
    }

    public void setPosition(Coord position) {
        this.position = position;
    }

    public float getSpan() {
        return span;
    }

    /**
     * @param spanTurns how wide of an arc this should spread light into, measured in turns (0-1)
     */
    public void setSpan(float spanTurns) {
        this.span = spanTurns;
    }

    public float getDirection() {
        return direction;
    }

    /**
     * @param directionTurns what angle the light should project in, measured in turns (0-1, counterclockwise from right)
     */
    public void setDirection(float directionTurns) {
        this.direction = directionTurns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LightSource)) return false;

        LightSource that = (LightSource) o;
        return Float.compare(span, that.span) == 0 && Float.compare(direction, that.direction) == 0 && radiance.equals(that.radiance) && position.equals(that.position);
    }

    @Override
    public int hashCode() {
        int result = radiance.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + BitConversion.floatToRawIntBits(span);
        result = 31 * result + BitConversion.floatToRawIntBits(direction);
        return result;
    }

    @Override
    public String toString() {
        return "LightSource{" +
                "radiance=" + radiance +
                ", position=" + position +
                ", span=" + span +
                ", direction=" + direction +
                '}';
    }

    public String stringSerialize() {
        return radiance.stringSerialize() + "`" +
                Base.SIMPLE64.signed(position.x) + "`" +
                Base.SIMPLE64.signed(position.y) + "`" +
                Base.SIMPLE64.signed(span) + "`" +
                Base.SIMPLE64.signed(direction) + "`";
    }

    public LightSource stringDeserialize(String data) {
        if(data == null) return this;
        int idx = data.indexOf("``", 1);
        radiance.stringDeserialize(data);
        position = Coord.get(
                Base.SIMPLE64.readShort(data, idx + 2, idx = data.indexOf('`', idx + 2)),
                Base.SIMPLE64.readShort(data, idx + 1, idx = data.indexOf('`', idx + 1))
        );
        span = Base.SIMPLE64.readFloatExact(data, idx + 1, idx = data.indexOf('`', idx + 1));
        direction = Base.SIMPLE64.readFloatExact(data, idx + 1, idx = data.indexOf('`', idx + 1));
        return this;
    }

    public static LightSource recreateFromString(String data) {
        return new LightSource().stringDeserialize(data);
    }

    @GwtIncompatible
    @Override
    public List<Class<?>> getSerializersNeeded() {
        return Arrays.asList(Coord.class, Radiance.class);
    }
}
