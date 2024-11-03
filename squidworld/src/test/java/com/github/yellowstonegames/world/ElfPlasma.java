package com.github.yellowstonegames.world;

/*
#define H(s,v)   sin( s + (v).zxy - cos(s.zxy + (v).yzx) + cos(s.yzx + v) )

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

import com.github.yellowstonegames.grid.Point3Float;

import static com.github.tommyettinger.digital.TrigTools.sin;
import static com.github.tommyettinger.digital.TrigTools.cos;

/**
 * From <a href="https://www.shadertoy.com/view/7sdSz7">this ShaderToy</a>.
 */
public class ElfPlasma {

    public Point3Float seed;
    private transient final Point3Float tA = new Point3Float();
    private transient final Point3Float tB = new Point3Float();
    private transient final Point3Float tC = new Point3Float();

    private static final Point3Float fixedA = new Point3Float(2, 6, 4);
    private static final Point3Float fixedB = new Point3Float(3, 7, 5);
    private static final Point3Float fixedC = new Point3Float(3, 5, 6);
    private static final Point3Float fixedC3 = new Point3Float(6, 8, 9);
    private static final Point3Float fixedCphi = new Point3Float(4.618f, 6.618f, 7.618f);

    public ElfPlasma(float sx, float sy, float sz) {
        seed = new Point3Float(sx, sy, sz).fract().mul(0.25f).add(0.1f, 0.1f, 0.1f);
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
                sin(s.x + v.z - cos(s.z + v.y) + cos(s.y + v.x)),
                sin(s.y + v.x - cos(s.x + v.z) + cos(s.z + v.y)),
                sin(s.z + v.y - cos(s.y + v.x) + cos(s.x + v.z))
        );
    }

    /**
     *
     * @param out will be overwritten
     * @param in the input x,y,time point
     * @return out, after changes
     */
    public Point3Float plasma(Point3Float out, Point3Float in) {
        float time = tA.z = in.z;
        float x = tA.x = in.x * 0.125f + cos(time);
        float y = tA.y = in.y * 0.125f + sin(time);
        out.set(seed).mul(time);
        tB.set(tA).add(seed);
        tA.sub(seed.y, seed.z, seed.x);
        sway(tA, fixedA, tA).mul(x);
        sway(tB, fixedB, tB).mul(y);
        out.set(tA).add(tB).mul(0.25f);
        sway(tA, fixedC, out).add(out).mul(0.5f);
        sway(tB, fixedCphi, sway(tC, fixedC, out).add(out).mul(0.5f)).mul(3f);
        sway(out, fixedC3, tA.add(tB)).mul(0.5f).add(0.5f, 0.5f, 0.5f);
        return out;
    }

    public String stringSerialize() {
        return seed.toString();
    }

    public ElfPlasma stringDeserialize(String data) {
        seed.fromString(data);
        return this;
    }
}
