package com.github.yellowstonegames.world;

/*
#define H(s,v)   sin(s + v.zxy - cos(s.zxy + v.yzx) + cos(s.yzx + v) )

void mainImage( out vec4 O, vec2 U )
{
    U = U/8. + sin(iTime);

    vec3 d = vec3(11.1, 14.3, 8.2),
         t = iTime/d,
         v = (U.x * H( vec3(2,6,4), t - d.yzx)
                + U.y * H( vec3(3,7,5), t + d    )
                ) /4.,
         K = vec3(3,5,6);

    O.rgb = H(3.+K,((v+H(K,v))/2. +H(1.62+K, (v+H(K,v))/2. ) *3.)) * .5 + .5;
}
 */

import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.Point3Float;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * A Plasma, which is like an {@link com.github.yellowstonegames.grid.INoise}, but naturally operates on multiple
 * float components at once, typically to render colors (but not necessarily).
 * <br>
 * From <a href="https://www.shadertoy.com/view/7sdSz7">this ShaderToy</a> mostly by Fabrice Neyret.
 */
public class ElfPlasma {

    /**
     * Can be {@link Point3Float#set(float, float, float)} to any finite, non-zero values, but the recommended
     * range is between 5.0f and 20.0f for each component.
     */
    public final Point3Float seed = new Point3Float();
    private transient final Point3Float tA = new Point3Float();
    private transient final Point3Float tB = new Point3Float();

    private static final Point3Float fixedA = new Point3Float(2, 6, 4);
    private static final Point3Float fixedB = new Point3Float(3, 7, 5);
    private static final Point3Float fixedC = new Point3Float(3, 5, 6);
    private static final Point3Float fixedC3 = new Point3Float(6, 8, 9);
    private static final Point3Float fixedCphi = new Point3Float(4.618f, 6.618f, 7.618f);

    private transient float time = 0f;
    private transient final Point3Float waveA = new Point3Float();
    private transient final Point3Float waveB = new Point3Float();

    /**
     * Constructs an ElfPlasma with default parameters in a recommended range, {@code 11.1f, 14.3f, 8.2f} .
     */
    public ElfPlasma() {
        this(11.1f, 14.3f, 8.2f);
    }

    /**
     * Uses and discards the given long seed {@code s} to generate three random floats between 7 and 18 each, using
     * them as the three-part seed.
     * @param s any long
     */
    public ElfPlasma(long s) {
        this(Hasher.randomize1Float(s) * 11f + 7f,
                Hasher.randomize1Float(s + 1) * 11f + 7f,
                Hasher.randomize1Float(s + 2) * 11f + 7f);
    }
    /**
     * Constructs an ElfPlasma with the given three floats as seeds. Each s parameter should usually be
     * between about 5 and 20, but this isn't a requirement. No parameter should be 0, NaN, or infinite.
     * @param sx should be between 5.0f and 20.0f
     * @param sy should be between 5.0f and 20.0f
     * @param sz should be between 5.0f and 20.0f
     */
    public ElfPlasma(float sx, float sy, float sz) {
        seed.set(sx, sy, sz);
    }
    /**
     * Performs various trigonometry operations on a mix of coordinates from s and from v, using each component
     * of s and each component of v to affect each component of out. Each resulting component of {@code out} will
     * be between -1 and 1, both inclusive.
     *
     * @param out will be overwritten
     * @param s   seed vector
     * @param v   value vector
     * @return out, after changes
     */
    public Point3Float sway(Point3Float out, Point3Float s, Point3Float v) {
        return out.set(
                TrigTools.sinSmoother(s.x + v.z - cosSmoother(s.z + v.y) + cosSmoother(s.y + v.x)),
                TrigTools.sinSmoother(s.y + v.x - cosSmoother(s.x + v.z) + cosSmoother(s.z + v.y)),
                TrigTools.sinSmoother(s.z + v.y - cosSmoother(s.y + v.x) + cosSmoother(s.x + v.z)) 
        );
    }

    /**
     * Sets {@code out} to the result of trigonometry operations on a mix of all other parameters, using each
     * float parameter to affect each component of out. Each resulting component of {@code out} will
     * be between -1 and 1, both inclusive.
     * @param out will be overwritten
     * @param sx seed x
     * @param sy seed y
     * @param sz seed z
     * @param vx value x
     * @param vy value y
     * @param vz value z
     * @return out, after changes
     */
    public Point3Float sway(Point3Float out, float sx, float sy, float sz, float vx, float vy, float vz) {
        return out.set(
                TrigTools.sinSmoother(sx + vz - cosSmoother(sz + vy) + cosSmoother(sy + vx)),
                TrigTools.sinSmoother(sy + vx - cosSmoother(sx + vz) + cosSmoother(sz + vy)),
                TrigTools.sinSmoother(sz + vy - cosSmoother(sy + vx) + cosSmoother(sx + vz)) 
        );
    }

    /**
     * Adds {@code out} with the result of trigonometry operations on a mix of all other parameters, using each
     * float parameter to affect each component of out. Each resulting component of {@code out} will have a value
     * added to it between -1 and 1, both inclusive.
     * @param out will be overwritten
     * @param sx seed x
     * @param sy seed y
     * @param sz seed z
     * @param vx value x
     * @param vy value y
     * @param vz value z
     * @return out, after changes
     */
    public Point3Float addSway(Point3Float out, float sx, float sy, float sz, float vx, float vy, float vz) {
        return out.add(
                TrigTools.sinSmoother(sx + vz - cosSmoother(sz + vy) + cosSmoother(sy + vx)),
                TrigTools.sinSmoother(sy + vx - cosSmoother(sx + vz) + cosSmoother(sz + vy)),
                TrigTools.sinSmoother(sz + vy - cosSmoother(sy + vx) + cosSmoother(sx + vz))
        );
    }

    /**
     * Uses the given point {@code in} as an x,y,time input, and overwrites {@code out} with continuous noise.
     *
     * @param out will be overwritten
     * @param in the input x,y,time point; will not be changed
     * @return out, after changes
     */
    public Point3Float plasma(Point3Float out, Point3Float in) {
        time = in.z * 0.75f;
        float x = in.x * 128f + TrigTools.cosSmoother(time); // U.x
        float y = in.y * 128f + TrigTools.sinSmoother(time); // U.y

        out.set(time, time, time).div(seed); // out = time / d
        tB.set(out).add(seed);          // tB = t + d
        out.sub(seed.y, seed.z, seed.x);// out = t - d.yzx
        sway(tA, fixedA, out);
        sway(tB, fixedB, tB);

        tA.mul(x);   // tA is an unnamed value
        tB.mul(y);    // tB is an unnamed value
        out.set(tA).add(tB).mul(0.25f); // out = v
        sway(tA, fixedC, out).add(out).mul(0.5f); // tA = (v+H(K,v))/2.
        sway(tB, fixedCphi, tA).mul(3f); // tB = H(1.62+K, (v+H(K,v))/2. ) *3.
        sway(out, fixedC3, tA.add(tB)).mul(0.5f).plus(0.5f);
        return out;
    }

    /**
     * Stores the time and performs some calculations that depend only on time and seed, to avoid having to
     * recalculate them in every call to {@link #plasmaWave(Point3Float, float, float)}.
     * This is analogous to setting uniforms in a shader based on the current time.
     *
     * @param t the time for this wave; should increase linearly and continuously
     * @return this, for chaining
     */
    public ElfPlasma wave(float t) {
        time = t * 0.75f;
        tA.set(time, time, time).div(seed); // t = time / d
        tB.set(tA).add(seed);               // tB = t + d
        tA.sub(seed.y, seed.z, seed.x);     // tA = t - d.yzx
        sway(waveA, fixedA, tA);
        sway(waveB, fixedB, tB);
        return this;
    }
    /**
     * Requires {@link #wave(float)} to be called before a batch of these calls with the same time t.
     * If x and y change by only small amounts, the different values returned in {@code out} will
     * also change continuously.
     *
     * @param out will be overwritten
     * @param x position
     * @param y position
     * @return out, after changes
     */
    public Point3Float plasmaWave(Point3Float out, float x, float y) {
        tA.set(waveA);
        tB.set(waveB);

        tA.mul(x * 128f + TrigTools.cosSmoother(time));
        tB.mul(y * 128f + TrigTools.sinSmoother(time));
        out.set(tA).add(tB).mul(0.25f); // out = v
        sway(tA, fixedC, out).add(out).mul(0.5f); // tA = (v+H(K,v))/2.
        sway(tB, fixedCphi, tA).mul(3f); // tB = H(1.62+K, (v+H(K,v))/2. ) *3.
        sway(out, fixedC3, tA.add(tB)).mul(0.5f).plus(0.5f);
        return out;
    }
/*
void mainImage( out vec4 O, vec2 U )
{
    U = U/8. + sin(iTime);

    vec3 d = vec3(11.1, 14.3, 8.2),
         t = iTime/d,
         v = (U.x * H( vec3(2,6,4), t - d.yzx)
                + U.y * H( vec3(3,7,5), t + d    )
                ) /4.,
         K = vec3(3,5,6);

    O.rgb = H(3.+K,((v+H(K,v))/2. +H(1.62+K, (v+H(K,v))/2. ) *3.)) * .5 + .5;
}
 */

    public String stringSerialize() {
        return seed.toString();
    }

    public ElfPlasma stringDeserialize(String data) {
        seed.fromString(data);
        return this;
    }
}
