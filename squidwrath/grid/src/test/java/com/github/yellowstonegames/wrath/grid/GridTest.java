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
import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

public class GridTest {
    @Test
    public void testCoord() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(ObjectList.class, new ObjectListSerializer(fury));
        ObjectList<Coord> data = ObjectList.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(-2, -3), Coord.get(100, 100));

        byte[] bytes = fury.serializeJavaObject(data);
        ObjectList<?> data2 = fury.deserializeJavaObject(bytes, ObjectList.class);
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testRegion() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Region.class);
        Region data = new Region(120, 120, Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            Region data2 = fury.deserializeJavaObject(bytes, Region.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordSet() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordSet.class, new CoordSetSerializer(fury));
        CoordSet data = CoordSet.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordSet data2 = fury.deserializeJavaObject(bytes, CoordSet.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordOrderedSet() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordOrderedSet.class, new CoordOrderedSetSerializer(fury));
        CoordOrderedSet data = CoordOrderedSet.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordOrderedSet data2 = fury.deserializeJavaObject(bytes, CoordOrderedSet.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordObjectMap() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordObjectMap.class, new CoordObjectMapSerializer(fury));
        CoordObjectMap<String> data = CoordObjectMap.with(Coord.get(0, 0), "foo", Coord.get(1, 1), "bar", Coord.get(2, 3), "baz", Coord.get(100, 100), "quux");

        byte[] bytes = fury.serializeJavaObject(data);
        CoordObjectMap<?> data2 = fury.deserializeJavaObject(bytes, CoordObjectMap.class);

        Assert.assertEquals(data, data2);
    }

    @Test
    public void testCoordObjectOrderedMap() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer(fury));
        CoordObjectOrderedMap<String> data = CoordObjectOrderedMap.with(Coord.get(0, 0), "foo", Coord.get(1, 1), "bar", Coord.get(2, 3), "baz", Coord.get(100, 100), "quux");

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordObjectOrderedMap<?> data2 = fury.deserializeJavaObject(bytes, CoordObjectOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordFloatMap() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordFloatMap.class, new CoordFloatMapSerializer(fury));
        CoordFloatMap data = CoordFloatMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 6.66, Coord.get(100, 100), 3.14159);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordFloatMap data2 = fury.deserializeJavaObject(bytes, CoordFloatMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordFloatOrderedMap() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer(fury));
        CoordFloatOrderedMap data = CoordFloatOrderedMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 6.66, Coord.get(100, 100), 3.14159);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordFloatOrderedMap data2 = fury.deserializeJavaObject(bytes, CoordFloatOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordLongMap() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordLongMap.class, new CoordLongMapSerializer(fury));
        CoordLongMap data = CoordLongMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 666, Coord.get(100, 100), 314159);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordLongMap data2 = fury.deserializeJavaObject(bytes, CoordLongMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordLongOrderedMap() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordLongOrderedMap.class, new CoordLongOrderedMapSerializer(fury));
        CoordLongOrderedMap data = CoordLongOrderedMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 666, Coord.get(100, 100), 314159);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordLongOrderedMap data2 = fury.deserializeJavaObject(bytes, CoordLongOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordIntMap() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordIntMap.class, new CoordIntMapSerializer(fury));
        CoordIntMap data = CoordIntMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 666, Coord.get(100, 100), 314159);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordIntMap data2 = fury.deserializeJavaObject(bytes, CoordIntMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordIntOrderedMap() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(CoordIntOrderedMap.class, new CoordIntOrderedMapSerializer(fury));
        CoordIntOrderedMap data = CoordIntOrderedMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 666, Coord.get(100, 100), 314159);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CoordIntOrderedMap data2 = fury.deserializeJavaObject(bytes, CoordIntOrderedMap.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.register(IGI.class);
        fury.registerSerializer(SpatialMap.class, new SpatialMapSerializer(fury));
        SpatialMap<IGI> data = new SpatialMap<>(8);
        data.add(new IGI(Coord.get(1, 2)));
        data.add(new IGI(Coord.get(2, 2)));
        data.add(new IGI(Coord.get(1, 3)));
        data.add(new IGI(Coord.get(2, 3)));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            SpatialMap<?> data2 = fury.deserializeJavaObject(bytes, SpatialMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRadiance() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Radiance.class, new RadianceSerializer(fury));

        Radiance data = new Radiance(5, 0xD0F055FF, 0.7f, 0.05f, 0.2f, 0.5f, -123);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            Radiance data2 = fury.deserializeJavaObject(bytes, Radiance.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLightSource() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(Radiance.class, new RadianceSerializer(fury));
        fury.registerSerializer(LightSource.class, new LightSourceSerializer(fury));

        LightSource data = new LightSource(Coord.get(1, 10), new Radiance(5, 0xD0F055FF, 0.7f, 0.05f, 0.2f, 0.5f, -123), 1f/6f, 0.125f);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            LightSource data2 = fury.deserializeJavaObject(bytes, LightSource.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLightingManager() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(int[].class);
        fury.register(int[][].class);
        fury.register(float[].class);
        fury.register(float[][].class);
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.register(Region.class);
        fury.registerSerializer(Radiance.class, new RadianceSerializer(fury));
        fury.registerSerializer(LightSource.class, new LightSourceSerializer(fury));
        fury.registerSerializer(ObjectDeque.class, new ObjectDequeSerializer(fury));
        fury.registerSerializer(LightingManager.class, new LightingManagerSerializer(fury));

        LightingManager data = new LightingManager(new float[10][10], 0x252033FF, Radius.CIRCLE, 4f);
        data.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            LightingManager data2 = fury.deserializeJavaObject(bytes, LightingManager.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLightingManagerRgb() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(int[].class);
        fury.register(int[][].class);
        fury.register(float[].class);
        fury.register(float[][].class);
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.register(Region.class);
        fury.registerSerializer(Radiance.class, new RadianceSerializer(fury));
        fury.registerSerializer(LightSource.class, new LightSourceSerializer(fury));
        fury.registerSerializer(ObjectDeque.class, new ObjectDequeSerializer(fury));
        fury.registerSerializer(LightingManagerRgb.class, new LightingManagerRgbSerializer(fury));

        LightingManagerRgb data = new LightingManagerRgb(new float[10][10], 0xFF858040, Radius.CIRCLE, 4f);
        data.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            LightingManagerRgb data2 = fury.deserializeJavaObject(bytes, LightingManagerRgb.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testVisionFramework() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(int[].class);
        fury.register(int[][].class);
        fury.register(float[].class);
        fury.register(float[][].class);
        fury.register(char[].class);
        fury.register(char[][].class);
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(Radiance.class, new RadianceSerializer(fury));
        fury.register(Region.class);
        fury.registerSerializer(LightSource.class, new LightSourceSerializer(fury));
        fury.registerSerializer(ObjectDeque.class, new ObjectDequeSerializer(fury));
        fury.registerSerializer(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer(fury));
        fury.registerSerializer(LightingManager.class, new LightingManagerSerializer(fury));
        fury.registerSerializer(VisionFramework.class, new VisionFrameworkSerializer(fury));

        VisionFramework data = new VisionFramework();
        data.restart(ArrayTools.fill('.', 10, 10), Coord.get(3, 3), 2f, DescriptiveColor.describeOklab("darker gray 9 yellow"));
        data.lighting.addLight(3, 3, new Radiance(3f, 0xFF9966AA, 0.2f, 0f, 0f, 0f));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            VisionFramework data2 = fury.deserializeJavaObject(bytes, VisionFramework.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testVisionFrameworkRgb() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(int[].class);
        fury.register(int[][].class);
        fury.register(float[].class);
        fury.register(float[][].class);
        fury.register(char[].class);
        fury.register(char[][].class);
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(Radiance.class, new RadianceSerializer(fury));
        fury.register(Region.class);
        fury.registerSerializer(LightSource.class, new LightSourceSerializer(fury));
        fury.registerSerializer(ObjectDeque.class, new ObjectDequeSerializer(fury));
        fury.registerSerializer(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer(fury));
        fury.registerSerializer(LightingManagerRgb.class, new LightingManagerRgbSerializer(fury));
        fury.registerSerializer(VisionFrameworkRgb.class, new VisionFrameworkRgbSerializer(fury));

        VisionFrameworkRgb data = new VisionFrameworkRgb();
        data.restart(ArrayTools.fill('.', 10, 10), Coord.get(3, 3), 2f, DescriptiveColorRgb.describe("darker gray 9 yellow"));
        data.lighting.addLight(3, 3, new Radiance(3f, 0x9966AAFF, 0.2f, 0f, 0f, 0f));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            VisionFrameworkRgb data2 = fury.deserializeJavaObject(bytes, VisionFrameworkRgb.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testNoise() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Noise.class);

        Noise data = new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f);
        data.setFractalSpiral(true);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            Noise data2 = fury.deserializeJavaObject(bytes, Noise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(FoamNoise.class);

        FoamNoise data = new FoamNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            FoamNoise data2 = fury.deserializeJavaObject(bytes, FoamNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(FoamplexNoise.class);

        FoamplexNoise data = new FoamplexNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            FoamplexNoise data2 = fury.deserializeJavaObject(bytes, FoamplexNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(PhantomNoise.class);

        PhantomNoise data = new PhantomNoise(1234, 8, 7f);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            PhantomNoise data2 = fury.deserializeJavaObject(bytes, PhantomNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testTaffyNoise() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(TaffyNoise.class);

        TaffyNoise data = new TaffyNoise(1234, 8, 7f);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            TaffyNoise data2 = fury.deserializeJavaObject(bytes, TaffyNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testFlanNoise() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(FlanNoise.class);

        FlanNoise data = new FlanNoise(1234, 8, 7f, 2);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            FlanNoise data2 = fury.deserializeJavaObject(bytes, FlanNoise.class);
            Assert.assertEquals(data.getNoise(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}), data2.getNoise(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCyclicNoise() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(CyclicNoise.class);

        CyclicNoise data = new CyclicNoise(-9876543210L, 8);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            CyclicNoise data2 = fury.deserializeJavaObject(bytes, CyclicNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSorbetNoise() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(SorbetNoise.class);

        SorbetNoise data = new SorbetNoise(-9876543210L, 8);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            SorbetNoise data2 = fury.deserializeJavaObject(bytes, SorbetNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimplexNoise() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(SimplexNoise.class);

        SimplexNoise data = new SimplexNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            SimplexNoise data2 = fury.deserializeJavaObject(bytes, SimplexNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(SimplexNoiseScaled.class);

        SimplexNoiseScaled data = new SimplexNoiseScaled(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            SimplexNoiseScaled data2 = fury.deserializeJavaObject(bytes, SimplexNoiseScaled.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(SimplexNoiseHard.class);

        SimplexNoiseHard data = new SimplexNoiseHard(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            SimplexNoiseHard data2 = fury.deserializeJavaObject(bytes, SimplexNoiseHard.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(OpenSimplex2.class);

        OpenSimplex2 data = new OpenSimplex2(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            OpenSimplex2 data2 = fury.deserializeJavaObject(bytes, OpenSimplex2.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testOpenSimplex2Smooth() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(OpenSimplex2Smooth.class);

        OpenSimplex2Smooth data = new OpenSimplex2Smooth(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            OpenSimplex2Smooth data2 = fury.deserializeJavaObject(bytes, OpenSimplex2Smooth.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testPerlinNoise() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(PerlinNoise.class);

        PerlinNoise data = new PerlinNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            PerlinNoise data2 = fury.deserializeJavaObject(bytes, PerlinNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(ValueNoise.class);

        ValueNoise data = new ValueNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            ValueNoise data2 = fury.deserializeJavaObject(bytes, ValueNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(com.github.yellowstonegames.grid.FlawedPointHash.FlowerHash.class);
        fury.registerSerializer(BasicHashNoise.class, new BasicHashNoiseSerializer(fury));

        BasicHashNoise data = new BasicHashNoise(-987654321, new FlawedPointHash.FlowerHash(123456789));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            BasicHashNoise data2 = fury.deserializeJavaObject(bytes, BasicHashNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(HighDimensionalValueNoise.class);

        HighDimensionalValueNoise data = new HighDimensionalValueNoise(1234, 8);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            HighDimensionalValueNoise data2 = fury.deserializeJavaObject(bytes, HighDimensionalValueNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testWhiteNoise() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(WhiteNoise.class);

        WhiteNoise data = new WhiteNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            WhiteNoise data2 = fury.deserializeJavaObject(bytes, WhiteNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(PerlueNoise.class);

        PerlueNoise data = new PerlueNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            PerlueNoise data2 = fury.deserializeJavaObject(bytes, PerlueNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(BadgerNoise.class);

        BadgerNoise data = new BadgerNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            BadgerNoise data2 = fury.deserializeJavaObject(bytes, BadgerNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(SnakeNoise.class);

        SnakeNoise data = new SnakeNoise(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            SnakeNoise data2 = fury.deserializeJavaObject(bytes, SnakeNoise.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Noise.class);
        fury.register(NoiseWrapper.class);

        NoiseWrapper data = new NoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            NoiseWrapper data2 = fury.deserializeJavaObject(bytes, NoiseWrapper.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Noise.class);
        fury.register(RadialNoiseWrapper.class);

        RadialNoiseWrapper data = new RadialNoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true, 10f, 20.125f);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            RadialNoiseWrapper data2 = fury.deserializeJavaObject(bytes, RadialNoiseWrapper.class);
            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testNoiseAdjustment() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Noise.class);
        fury.register(NoiseAdjustment.class);

        NoiseAdjustment data = new NoiseAdjustment(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), Interpolations.exp5In);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            NoiseAdjustment data2 = fury.deserializeJavaObject(bytes, NoiseAdjustment.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(PointPair.class, new PointPairSerializer(fury));
        PointPair<Coord> data = new PointPair<>(Coord.get(0, 0), Coord.get(1, 1));

        byte[] bytes = fury.serializeJavaObject(data);
        {
            PointPair<?> data2 = fury.deserializeJavaObject(bytes, PointPair.class);
            Assert.assertEquals(data, data2);
        }
    }
}
