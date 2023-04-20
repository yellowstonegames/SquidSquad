package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;

/**
 * A NoiseWrapper that makes its output radially symmetric around a given center point.
 * The center point is typically given in the constructor, and can be set later with {@link #setCenter(float, float)}.
 * There are 5 divisions by default, but this can be changed to any positive int with {@link #setDivisions(int)}. If the
 * number of divisions is even, you can make every other division a mirror image with {@link #setMirror(boolean)}.
 */
public class RadialNoiseWrapper extends NoiseWrapper {
    /**
     * The center of the noise regarding radial symmetry; the x-coordinate.
     */
    public float centerX;
    /**
     * The center of the noise regarding radial symmetry; the y-coordinate.
     */
    public float centerY;

    /**
     * How many times the noise should repeat in one loop around the center.
     */
    public int divisions = 5;

    /**
     * If {@link #divisions} is an even number and this is true, this will flip the noise for odd-numbered divisions,
     * making division 1 a mirror image of division 0, division 3 a mirror image of division 2, and so on.
     */
    public boolean mirror = false;

    public RadialNoiseWrapper() {
        super();
    }

    public RadialNoiseWrapper(INoise toWrap, float centerX, float centerY) {
        super(toWrap);
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public RadialNoiseWrapper(INoise toWrap, float frequency, int mode, int octaves, float centerX, float centerY) {
        super(toWrap, frequency, mode, octaves);
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public RadialNoiseWrapper(INoise toWrap, long seed, float frequency, int mode, int octaves, float centerX, float centerY) {
        super(toWrap, seed, frequency, mode, octaves);
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public RadialNoiseWrapper(INoise toWrap, long seed, float frequency, int mode, int octaves, boolean fractalSpiral, float centerX, float centerY) {
        super(toWrap, seed, frequency, mode, octaves, fractalSpiral);
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public RadialNoiseWrapper(RadialNoiseWrapper other) {
        super(other);
        this.centerX = other.centerX;
        this.centerY = other.centerY;
        this.divisions = other.divisions;
        this.mirror = other.mirror;
    }

    public float getCenterX() {
        return centerX;
    }

    public RadialNoiseWrapper setCenterX(float centerX) {
        this.centerX = centerX;
        return this;
    }

    public float getCenterY() {
        return centerY;
    }

    public RadialNoiseWrapper setCenterY(float centerY) {
        this.centerY = centerY;
        return this;
    }
    public RadialNoiseWrapper setCenter(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        return this;
    }

    public int getDivisions() {
        return divisions;
    }

    public RadialNoiseWrapper setDivisions(int divisions) {
        this.divisions = divisions;
        return this;
    }

    public boolean isMirror() {
        return mirror;
    }

    public RadialNoiseWrapper setMirror(boolean mirror) {
        this.mirror = mirror;
        return this;
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return Math.min(wrapped.getMaxDimension(), 3);
    }

    @Override
    public String getTag() {
        return "RadN";
    }

    @Override
    public String serializeToString() {
        return "`" + Serializer.serialize(wrapped) + '~' +
                seed + '~' +
                frequency + '~' +
                mode + '~' +
                octaves + '~' +
                (fractalSpiral ? '1' : '0') + '~' +
                centerX + '~' +
                centerY + '~' +
                divisions + '~' +
                (mirror ? '1' : '0') + '`';
    }

    @Override
    public RadialNoiseWrapper deserializeFromString(String data) {
        int pos = data.indexOf('`', data.indexOf('`', 2) + 1)+1;
        setWrapped(Serializer.deserialize(data.substring(1, pos)));
        setSeed(Base.BASE10.readLong(data, pos+1, pos = data.indexOf('~', pos+2)));
        setFrequency(Float.parseFloat(data.substring(pos+1, pos = data.indexOf('~', pos+2))));
        setMode(Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+2)));
        setOctaves(Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+2)));
        setFractalSpiral(data.charAt(pos+1) == '1');
        pos = data.indexOf('~', pos+2);
        centerX = Float.parseFloat(data.substring(pos+1, pos = data.indexOf('~', pos+2)));
        centerY = Float.parseFloat(data.substring(pos+1, pos = data.indexOf('~', pos+2)));
        divisions = Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+2));
        mirror = data.charAt(pos+1) == '1';
        return this;
    }

    @Override
    public RadialNoiseWrapper copy() {
        return new RadialNoiseWrapper(this);
    }
}
