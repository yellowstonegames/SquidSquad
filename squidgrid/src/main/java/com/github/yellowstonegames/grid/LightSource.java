package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.BitConversion;

/**
 * A tiny data wrapper around a {@link #radiance} and a Coord {@link #position} for that Radiance, as well as,
 * optionally, an angle to project a cone of light in and a span for how wide of a cone to project.
 */
public class LightSource {
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
        this(new Radiance(), Coord.get(0,0), 1f, 0f);
    }

    public LightSource(Radiance radiance, Coord position) {
        this(radiance, position, 1f, 0f);
    }
    public LightSource(Radiance radiance, Coord position, float span, float direction) {
        this.radiance = radiance;
        this.position = position;
        this.span = span;
        this.direction = direction;
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
     * @param span how wide of an arc this should spread light into, measured in turns (0-1)
     */
    public void setSpan(float span) {
        this.span = span;
    }

    public float getDirection() {
        return direction;
    }

    /**
     * @param direction what angle the light should project in, measured in turns (0-1, counterclockwise from right)
     */
    public void setDirection(float direction) {
        this.direction = direction;
    }

    @Override
    public final boolean equals(Object o) {
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
}
