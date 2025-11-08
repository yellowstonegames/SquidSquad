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
     *
     * @param out will be overwritten
     * @param in the input x,y,time point; will not be changed
     * @return out, after changes
     */
    public Point3Float plasma(Point3Float out, Point3Float in) {
        float time = TrigTools.sinSmoother(tA.z = in.z * 0.01f);
        float x = tA.x = in.x * 128f + (time); // U.x
        float y = tA.y = in.y * 128f + (time); // U.y
        out.set(tA.z, tA.z, tA.z).div(seed); // out = time / d
        tB.set(out).add(seed);          // tB = t + d
        out.sub(seed.y, seed.z, seed.x);// out = t - d.yzx
        sway(tA, fixedA, out).mul(x);   // tA is an unnamed value
        sway(tB, fixedB, tB).mul(y);    // tB is an unnamed value
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
