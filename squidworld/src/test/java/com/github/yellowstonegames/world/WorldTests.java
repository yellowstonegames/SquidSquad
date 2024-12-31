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

    @Test
    public void testHexagonal() {
        for (int i = 0; i < 10; i++) {
            HexagonalWorldMap world = new HexagonalWorldMap(i, SIZE * 2, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(HexagonalWorldMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

    @Test
    public void testHyperelliptical() {
        for (int i = 0; i < 10; i++) {
            HyperellipticalWorldMap world = new HyperellipticalWorldMap(i, SIZE * 2, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(HyperellipticalWorldMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

    @Test
    public void testLatLon() {
        for (int i = 0; i < 10; i++) {
            LatLonWorldMap world = new LatLonWorldMap(i, SIZE * 2, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(LatLonWorldMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

    @Test
    public void testRoundSide() {
        for (int i = 0; i < 10; i++) {
            RoundSideWorldMap world = new RoundSideWorldMap(i, SIZE * 2, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(RoundSideWorldMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

    @Test
    public void testStretch() {
        for (int i = 0; i < 10; i++) {
            StretchWorldMap world = new StretchWorldMap(i, SIZE * 2, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(StretchWorldMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

    @Test
    public void testGlobe() {
        for (int i = 0; i < 10; i++) {
            GlobeMap world = new GlobeMap(i, SIZE, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(GlobeMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

    @Test
    public void testLocal() {
        for (int i = 0; i < 10; i++) {
            LocalMap world = new LocalMap(i, SIZE, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(LocalMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

    @Test
    public void testRotatingGlobe() {
        for (int i = 0; i < 10; i++) {
            RotatingGlobeMap world = new RotatingGlobeMap(i, SIZE, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(RotatingGlobeMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

    @Test
    public void testTiling() {
        for (int i = 0; i < 10; i++) {
            TilingWorldMap world = new TilingWorldMap(i, SIZE, SIZE);
            BlendedWorldMapView wmv = new BlendedWorldMapView(world);
            wmv.generate();
            int[][] cm0 = wmv.show();
            String ser = world.stringSerialize();
            System.out.println("Serialized data is " + ser.length() + " chars long.");
            wmv.setWorld(TilingWorldMap.recreateFromString(ser));
            wmv.generate();
            int[][] cm1 = wmv.show();
            Assert.assertEquals(Hasher.intArray2DHashBulk64.hash64(i, cm0), Hasher.intArray2DHashBulk64.hash64(i, cm1));
        }
    }

}
