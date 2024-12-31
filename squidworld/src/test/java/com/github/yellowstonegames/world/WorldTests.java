package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.Hasher;
import org.junit.Assert;
import org.junit.Test;

public class WorldTests {
    public static final int SIZE = 100;
    @Test
    public void testElliptical() {
        for (int i = 0; i < 10; i++) {
            EllipticalWorldMap world = new EllipticalWorldMap(i, SIZE * 2, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(EllipticalWorldMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }
}
