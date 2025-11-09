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

import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.Point3Float;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * From <a href="https://www.shadertoy.com/view/7sdSz7">this ShaderToy</a>.
 */
public class ElfPlasma {

    public Point3Float seed;
    private transient final Point3Float tA = new Point3Float();
    private transient final Point3Float tB = new Point3Float();

    private static final Point3Float fixedA = new Point3Float(2, 6, 4);
    private static final Point3Float fixedB = new Point3Float(3, 7, 5);
    private static final Point3Float fixedC = new Point3Float(3, 5, 6);
    private static final Point3Float fixedC3 = new Point3Float(6, 8, 9);
    private static final Point3Float fixedCphi = new Point3Float(4.618f, 6.618f, 7.618f);

    private float time = 0f;
    private final Point3Float waveA = new Point3Float();
    private final Point3Float waveB = new Point3Float();

    public ElfPlasma() {
        this(11.1f, 14.3f, 8.2f);
    }
    public ElfPlasma(float sx, float sy, float sz) {
        seed = new Point3Float(sx, sy, sz);
    }
    /**
     * Was called H.
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
    
    public Point3Float sway(Point3Float out, float sx, float sy, float sz, float vx, float vy, float vz) {
        return out.set(
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
        x = x * 128f + TrigTools.cosSmoother(time); // U.x
        y = y * 128f + TrigTools.sinSmoother(time); // U.y

        tA.set(waveA);
        tB.set(waveB);

        tA.mul(x);   // tA is an unnamed value
        tB.mul(y);    // tB is an unnamed value
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

    //(in.z + LineWobble.bicubicWobble(0x12341234, in.x) + LineWobble.bicubicWobble(0x56785678, in.y)));
    public String stringSerialize() {
        return seed.toString();
    }

    public ElfPlasma stringDeserialize(String data) {
        seed.fromString(data);
        return this;
    }
}
