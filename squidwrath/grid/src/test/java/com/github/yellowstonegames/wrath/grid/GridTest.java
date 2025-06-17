/*
 * Copyright (c) 2020-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yellowstonegames.wrath.grid;

import com.github.tommyettinger.crux.PointPair;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.tantrum.jdkgdxds.ObjectDequeSerializer;
import com.github.tommyettinger.tantrum.jdkgdxds.ObjectListSerializer;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.grid.*;
import org.apache.fory.Fory;
import org.apache.fory.config.Language;
import org.apache.fory.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

public class GridTest {
    @Test
    public void testCoord() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(ObjectList.class, new ObjectListSerializer(fory));
        ObjectList<Coord> data = ObjectList.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(-2, -3), Coord.get(100, 100));

        byte[] bytes = fory.serializeJavaObject(data);
        ObjectList<?> data2 = fory.deserializeJavaObject(bytes, ObjectList.class);
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testRegion() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(Region.class);
        Region data = new Region(120, 120, Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            Region data2 = fory.deserializeJavaObject(bytes, Region.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordSet() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordSet.class, new CoordSetSerializer(fory));
        CoordSet data = CoordSet.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordSet data2 = fory.deserializeJavaObject(bytes, CoordSet.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordOrderedSet() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordOrderedSet.class, new CoordOrderedSetSerializer(fory));
        CoordOrderedSet data = CoordOrderedSet.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordOrderedSet data2 = fory.deserializeJavaObject(bytes, CoordOrderedSet.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordObjectMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordObjectMap.class, new CoordObjectMapSerializer(fory));
        CoordObjectMap<String> data = CoordObjectMap.with(Coord.get(0, 0), "foo", Coord.get(1, 1), "bar", Coord.get(2, 3), "baz", Coord.get(100, 100), "quux");

        byte[] bytes = fory.serializeJavaObject(data);
        CoordObjectMap<?> data2 = fory.deserializeJavaObject(bytes, CoordObjectMap.class);

        Assert.assertEquals(data, data2);
    }

    @Test
    public void testCoordObjectOrderedMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer(fory));
        CoordObjectOrderedMap<String> data = CoordObjectOrderedMap.with(Coord.get(0, 0), "foo", Coord.get(1, 1), "bar", Coord.get(2, 3), "baz", Coord.get(100, 100), "quux");

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordObjectOrderedMap<?> data2 = fory.deserializeJavaObject(bytes, CoordObjectOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordFloatMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordFloatMap.class, new CoordFloatMapSerializer(fory));
        CoordFloatMap data = CoordFloatMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 6.66, Coord.get(100, 100), 3.14159);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordFloatMap data2 = fory.deserializeJavaObject(bytes, CoordFloatMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordFloatOrderedMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer(fory));
        CoordFloatOrderedMap data = CoordFloatOrderedMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 6.66, Coord.get(100, 100), 3.14159);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordFloatOrderedMap data2 = fory.deserializeJavaObject(bytes, CoordFloatOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordLongMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordLongMap.class, new CoordLongMapSerializer(fory));
        CoordLongMap data = CoordLongMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 666, Coord.get(100, 100), 314159);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordLongMap data2 = fory.deserializeJavaObject(bytes, CoordLongMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordLongOrderedMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordLongOrderedMap.class, new CoordLongOrderedMapSerializer(fory));
        CoordLongOrderedMap data = CoordLongOrderedMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 666, Coord.get(100, 100), 314159);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordLongOrderedMap data2 = fory.deserializeJavaObject(bytes, CoordLongOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordIntMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordIntMap.class, new CoordIntMapSerializer(fory));
        CoordIntMap data = CoordIntMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 666, Coord.get(100, 100), 314159);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordIntMap data2 = fory.deserializeJavaObject(bytes, CoordIntMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordIntOrderedMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(CoordIntOrderedMap.class, new CoordIntOrderedMapSerializer(fory));
        CoordIntOrderedMap data = CoordIntOrderedMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 666, Coord.get(100, 100), 314159);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CoordIntOrderedMap data2 = fory.deserializeJavaObject(bytes, CoordIntOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    public static class IGI implements IGridIdentified {
        public final int id;
        public Coord position;
        private static int COUNTER = 0;

        public IGI(){
            id = COUNTER++;
            position = Coord.get(0, 0);
        }
        public IGI(Coord pos){
            id = COUNTER++;
            position = pos;
        }
        public IGI(int id, Coord pos){
            this.id = id;
            position = pos;
        }

        @Override
        public int getIdentifier() {
            return id;
        }

        @Override
        public Coord getCoordPosition() {
            return position;
        }

        @Override
        public void setCoordPosition(Coord position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "IGI{" +
                    "id=" + id +
                    ", position=" + position +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IGI igi = (IGI) o;

            if (id != igi.id) return false;
            return position != null ? position.equals(igi.position) : igi.position == null;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
    @Test
    public void testSpatialMap() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.register(IGI.class);
        fory.registerSerializer(SpatialMap.class, new SpatialMapSerializer(fory));
        SpatialMap<IGI> data = new SpatialMap<>(8);
        data.add(new IGI(Coord.get(1, 2)));
        data.add(new IGI(Coord.get(2, 2)));
        data.add(new IGI(Coord.get(1, 3)));
        data.add(new IGI(Coord.get(2, 3)));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            SpatialMap<?> data2 = fory.deserializeJavaObject(bytes, SpatialMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRadiance() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Radiance.class, new RadianceSerializer(fory));

        Radiance data = new Radiance(5, 0xD0F055FF, 0.7f, 0.05f, 0.2f, 0.5f, -123);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            Radiance data2 = fory.deserializeJavaObject(bytes, Radiance.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLightSource() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(Radiance.class, new RadianceSerializer(fory));
        fory.registerSerializer(LightSource.class, new LightSourceSerializer(fory));

        LightSource data = new LightSource(Coord.get(1, 10), new Radiance(5, 0xD0F055FF, 0.7f, 0.05f, 0.2f, 0.5f, -123), 1f/6f, 0.125f);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            LightSource data2 = fory.deserializeJavaObject(bytes, LightSource.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLightingManager() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(int[].class);
        fory.register(int[][].class);
        fory.register(float[].class);
        fory.register(float[][].class);
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.register(Region.class);
        fory.registerSerializer(Radiance.class, new RadianceSerializer(fory));
        fory.registerSerializer(LightSource.class, new LightSourceSerializer(fory));
        fory.registerSerializer(ObjectDeque.class, new ObjectDequeSerializer(fory));
        fory.registerSerializer(LightingManager.class, new LightingManagerSerializer(fory));

        LightingManager data = new LightingManager(new float[10][10], 0x252033FF, Radius.CIRCLE, 4f);
        data.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            LightingManager data2 = fory.deserializeJavaObject(bytes, LightingManager.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLightingManagerRgb() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(int[].class);
        fory.register(int[][].class);
        fory.register(float[].class);
        fory.register(float[][].class);
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.register(Region.class);
        fory.registerSerializer(Radiance.class, new RadianceSerializer(fory));
        fory.registerSerializer(LightSource.class, new LightSourceSerializer(fory));
        fory.registerSerializer(ObjectDeque.class, new ObjectDequeSerializer(fory));
        fory.registerSerializer(LightingManagerRgb.class, new LightingManagerRgbSerializer(fory));

        LightingManagerRgb data = new LightingManagerRgb(new float[10][10], 0xFF858040, Radius.CIRCLE, 4f);
        data.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            LightingManagerRgb data2 = fory.deserializeJavaObject(bytes, LightingManagerRgb.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testVisionFramework() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(int[].class);
        fory.register(int[][].class);
        fory.register(float[].class);
        fory.register(float[][].class);
        fory.register(char[].class);
        fory.register(char[][].class);
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(Radiance.class, new RadianceSerializer(fory));
        fory.register(Region.class);
        fory.registerSerializer(LightSource.class, new LightSourceSerializer(fory));
        fory.registerSerializer(ObjectDeque.class, new ObjectDequeSerializer(fory));
        fory.registerSerializer(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer(fory));
        fory.registerSerializer(LightingManager.class, new LightingManagerSerializer(fory));
        fory.registerSerializer(VisionFramework.class, new VisionFrameworkSerializer(fory));

        VisionFramework data = new VisionFramework();
        data.restart(ArrayTools.fill('.', 10, 10), Coord.get(3, 3), 2f, DescriptiveColor.describeOklab("darker gray 9 yellow"));
        data.lighting.addLight(3, 3, new Radiance(3f, 0xFF9966AA, 0.2f, 0f, 0f, 0f));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            VisionFramework data2 = fory.deserializeJavaObject(bytes, VisionFramework.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testVisionFrameworkRgb() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(int[].class);
        fory.register(int[][].class);
        fory.register(float[].class);
        fory.register(float[][].class);
        fory.register(char[].class);
        fory.register(char[][].class);
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(Radiance.class, new RadianceSerializer(fory));
        fory.register(Region.class);
        fory.registerSerializer(LightSource.class, new LightSourceSerializer(fory));
        fory.registerSerializer(ObjectDeque.class, new ObjectDequeSerializer(fory));
        fory.registerSerializer(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer(fory));
        fory.registerSerializer(LightingManagerRgb.class, new LightingManagerRgbSerializer(fory));
        fory.registerSerializer(VisionFrameworkRgb.class, new VisionFrameworkRgbSerializer(fory));

        VisionFrameworkRgb data = new VisionFrameworkRgb();
        data.restart(ArrayTools.fill('.', 10, 10), Coord.get(3, 3), 2f, DescriptiveColorRgb.describe("darker gray 9 yellow"));
        data.lighting.addLight(3, 3, new Radiance(3f, 0x9966AAFF, 0.2f, 0f, 0f, 0f));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            VisionFrameworkRgb data2 = fory.deserializeJavaObject(bytes, VisionFrameworkRgb.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(Noise.class);

        Noise data = new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f);
        data.setFractalSpiral(true);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            Noise data2 = fory.deserializeJavaObject(bytes, Noise.class);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f), data2.getConfiguredNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f), data2.getConfiguredNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
            Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testFoamNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(FoamNoise.class);

        FoamNoise data = new FoamNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            FoamNoise data2 = fory.deserializeJavaObject(bytes, FoamNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testFoamplexNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(FoamplexNoise.class);

        FoamplexNoise data = new FoamplexNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            FoamplexNoise data2 = fory.deserializeJavaObject(bytes, FoamplexNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testPhantomNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(PhantomNoise.class);

        PhantomNoise data = new PhantomNoise(1234, 8, 7f);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            PhantomNoise data2 = fory.deserializeJavaObject(bytes, PhantomNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testTaffyNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(TaffyNoise.class);

        TaffyNoise data = new TaffyNoise(1234, 8, 7f);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            TaffyNoise data2 = fory.deserializeJavaObject(bytes, TaffyNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testFlanNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(FlanNoise.class);

        FlanNoise data = new FlanNoise(1234, 8, 7f, 2);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            FlanNoise data2 = fory.deserializeJavaObject(bytes, FlanNoise.class);
            Assert.assertEquals(data.getNoise(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}), data2.getNoise(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCyclicNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(CyclicNoise.class);

        CyclicNoise data = new CyclicNoise(-9876543210L, 8);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            CyclicNoise data2 = fory.deserializeJavaObject(bytes, CyclicNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSorbetNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(SorbetNoise.class);

        SorbetNoise data = new SorbetNoise(-9876543210L, 8);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            SorbetNoise data2 = fory.deserializeJavaObject(bytes, SorbetNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimplexNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(SimplexNoise.class);

        SimplexNoise data = new SimplexNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            SimplexNoise data2 = fory.deserializeJavaObject(bytes, SimplexNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimplexNoiseScaled() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(SimplexNoiseScaled.class);

        SimplexNoiseScaled data = new SimplexNoiseScaled(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            SimplexNoiseScaled data2 = fory.deserializeJavaObject(bytes, SimplexNoiseScaled.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimplexNoiseHard() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(SimplexNoiseHard.class);

        SimplexNoiseHard data = new SimplexNoiseHard(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            SimplexNoiseHard data2 = fory.deserializeJavaObject(bytes, SimplexNoiseHard.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testOpenSimplex2() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(OpenSimplex2.class);

        OpenSimplex2 data = new OpenSimplex2(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            OpenSimplex2 data2 = fory.deserializeJavaObject(bytes, OpenSimplex2.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testOpenSimplex2Smooth() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(OpenSimplex2Smooth.class);

        OpenSimplex2Smooth data = new OpenSimplex2Smooth(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            OpenSimplex2Smooth data2 = fory.deserializeJavaObject(bytes, OpenSimplex2Smooth.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testPerlinNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(PerlinNoise.class);

        PerlinNoise data = new PerlinNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            PerlinNoise data2 = fory.deserializeJavaObject(bytes, PerlinNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testValueNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(ValueNoise.class);

        ValueNoise data = new ValueNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            ValueNoise data2 = fory.deserializeJavaObject(bytes, ValueNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testBasicHashNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(com.github.yellowstonegames.grid.FlawedPointHash.FlowerHash.class);
        fory.registerSerializer(BasicHashNoise.class, new BasicHashNoiseSerializer(fory));

        BasicHashNoise data = new BasicHashNoise(-987654321, new FlawedPointHash.FlowerHash(123456789));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            BasicHashNoise data2 = fory.deserializeJavaObject(bytes, BasicHashNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testHighDimensionalValueNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(HighDimensionalValueNoise.class);

        HighDimensionalValueNoise data = new HighDimensionalValueNoise(1234, 8);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            HighDimensionalValueNoise data2 = fory.deserializeJavaObject(bytes, HighDimensionalValueNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testWhiteNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(WhiteNoise.class);

        WhiteNoise data = new WhiteNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            WhiteNoise data2 = fory.deserializeJavaObject(bytes, WhiteNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testPerlueNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(PerlueNoise.class);

        PerlueNoise data = new PerlueNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            PerlueNoise data2 = fory.deserializeJavaObject(bytes, PerlueNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testBadgerNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(BadgerNoise.class);

        BadgerNoise data = new BadgerNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            BadgerNoise data2 = fory.deserializeJavaObject(bytes, BadgerNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSnakeNoise() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(SnakeNoise.class);

        SnakeNoise data = new SnakeNoise(-9876543210L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            SnakeNoise data2 = fory.deserializeJavaObject(bytes, SnakeNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testNoiseWrapper() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(Noise.class);
        fory.register(NoiseWrapper.class);

        NoiseWrapper data = new NoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            NoiseWrapper data2 = fory.deserializeJavaObject(bytes, NoiseWrapper.class);
            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRadialNoiseWrapper() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(Noise.class);
        fory.register(RadialNoiseWrapper.class);

        RadialNoiseWrapper data = new RadialNoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true, 10f, 20.125f);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            RadialNoiseWrapper data2 = fory.deserializeJavaObject(bytes, RadialNoiseWrapper.class);
            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testNoiseAdjustment() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(Noise.class);
        fory.register(NoiseAdjustment.class);

        NoiseAdjustment data = new NoiseAdjustment(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), Interpolations.exp5In);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            NoiseAdjustment data2 = fory.deserializeJavaObject(bytes, NoiseAdjustment.class);
            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testPointPair() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.registerSerializer(Coord.class, new CoordSerializer(fory));
        fory.registerSerializer(PointPair.class, new PointPairSerializer(fory));
        PointPair<Coord> data = new PointPair<>(Coord.get(0, 0), Coord.get(1, 1));

        byte[] bytes = fory.serializeJavaObject(data);
        {
            PointPair<?> data2 = fory.deserializeJavaObject(bytes, PointPair.class);
            Assert.assertEquals(data, data2);
        }
    }
}
