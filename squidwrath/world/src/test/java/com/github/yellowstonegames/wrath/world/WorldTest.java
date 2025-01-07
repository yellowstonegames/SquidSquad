/*
 * Copyright (c) 2020-2025 See AUTHORS file.
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

package com.github.yellowstonegames.wrath.world;

import com.github.yellowstonegames.world.*;
import com.github.yellowstonegames.world.BiomeMapper.*;
import org.apache.fury.Fury;
import org.apache.fury.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

public class WorldTest {
    @Test
    public void testEllipticalWorldMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(EllipticalWorldMap.class, new EllipticalWorldMapSerializer(fury));

        EllipticalWorldMap data = new EllipticalWorldMap(123, 200, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            EllipticalWorldMap data2 = fury.deserializeJavaObject(bytes, EllipticalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testGlobeMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(GlobeMap.class, new GlobeMapSerializer(fury));

        GlobeMap data = new GlobeMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            GlobeMap data2 = fury.deserializeJavaObject(bytes, GlobeMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testHexagonalWorldMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(HexagonalWorldMap.class, new HexagonalWorldMapSerializer(fury));

        HexagonalWorldMap data = new HexagonalWorldMap(123, 200, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            HexagonalWorldMap data2 = fury.deserializeJavaObject(bytes, HexagonalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testHyperellipticalWorldMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(HyperellipticalWorldMap.class, new HyperellipticalWorldMapSerializer(fury));

        HyperellipticalWorldMap data = new HyperellipticalWorldMap(123, 200, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            HyperellipticalWorldMap data2 = fury.deserializeJavaObject(bytes, HyperellipticalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testLatLonWorldMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(LatLonWorldMap.class, new LatLonWorldMapSerializer(fury));

        LatLonWorldMap data = new LatLonWorldMap(123, 200, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            LatLonWorldMap data2 = fury.deserializeJavaObject(bytes, LatLonWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLocalMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(LocalMap.class, new LocalMapSerializer(fury));

        LocalMap data = new LocalMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            LocalMap data2 = fury.deserializeJavaObject(bytes, LocalMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testMimicLocalMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(MimicLocalMap.class, new MimicLocalMapSerializer(fury));

        MimicLocalMap data = new MimicLocalMap(123);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            MimicLocalMap data2 = fury.deserializeJavaObject(bytes, MimicLocalMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testMimicWorldMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(MimicWorldMap.class, new MimicWorldMapSerializer(fury));

        MimicWorldMap data = new MimicWorldMap(123);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            MimicWorldMap data2 = fury.deserializeJavaObject(bytes, MimicWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRotatingGlobeMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(RotatingGlobeMap.class, new RotatingGlobeMapSerializer(fury));

        RotatingGlobeMap data = new RotatingGlobeMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            RotatingGlobeMap data2 = fury.deserializeJavaObject(bytes, RotatingGlobeMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRoundSideWorldMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(RoundSideWorldMap.class, new RoundSideWorldMapSerializer(fury));

        RoundSideWorldMap data = new RoundSideWorldMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            RoundSideWorldMap data2 = fury.deserializeJavaObject(bytes, RoundSideWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testStretchWorldMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(StretchWorldMap.class, new StretchWorldMapSerializer(fury));

        StretchWorldMap data = new StretchWorldMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            StretchWorldMap data2 = fury.deserializeJavaObject(bytes, StretchWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testTilingWorldMap() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(TilingWorldMap.class, new TilingWorldMapSerializer(fury));

        TilingWorldMap data = new TilingWorldMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            TilingWorldMap data2 = fury.deserializeJavaObject(bytes, TilingWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimpleBiomeMapper() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(SimpleBiomeMapper.class, new SimpleBiomeMapperSerializer(fury));

        StretchWorldMap wmg = new StretchWorldMap(124, 200, 100);
        wmg.generate();
        SimpleBiomeMapper data = new SimpleBiomeMapper();
        data.makeBiomes(wmg);

        {
            byte[] bytes = fury.serializeJavaObject(data);
            SimpleBiomeMapper data2 = fury.deserializeJavaObject(bytes, SimpleBiomeMapper.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testDetailedBiomeMapper() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(DetailedBiomeMapper.class, new DetailedBiomeMapperSerializer(fury));

        StretchWorldMap wmg = new StretchWorldMap(124, 200, 100);
        wmg.generate();
        DetailedBiomeMapper data = new DetailedBiomeMapper();
        data.makeBiomes(wmg);

        {
            byte[] bytes = fury.serializeJavaObject(data);
            DetailedBiomeMapper data2 = fury.deserializeJavaObject(bytes, DetailedBiomeMapper.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testBlendedBiomeMapper() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(BlendedBiomeMapper.class, new BlendedBiomeMapperSerializer(fury));

        StretchWorldMap wmg = new StretchWorldMap(124, 200, 100);
        wmg.generate();
        BlendedBiomeMapper data = new BlendedBiomeMapper();
        data.makeBiomes(wmg);

        {
            byte[] bytes = fury.serializeJavaObject(data);
            BlendedBiomeMapper data2 = fury.deserializeJavaObject(bytes, BlendedBiomeMapper.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testUnrealisticBiomeMapper() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(UnrealisticBiomeMapper.class, new UnrealisticBiomeMapperSerializer(fury));

        StretchWorldMap wmg = new StretchWorldMap(124, 200, 100);
        wmg.generate();
        UnrealisticBiomeMapper data = new UnrealisticBiomeMapper();
        data.makeBiomes(wmg);

        {
            byte[] bytes = fury.serializeJavaObject(data);
            UnrealisticBiomeMapper data2 = fury.deserializeJavaObject(bytes, UnrealisticBiomeMapper.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testDetailedWorldMapView() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(DetailedWorldMapView.class, new DetailedWorldMapViewSerializer(fury));
        fury.registerSerializer(GlobeMap.class, new GlobeMapSerializer(fury));

        DetailedWorldMapView data;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        data = new DetailedWorldMapView(world);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            DetailedWorldMapView data2 = fury.deserializeJavaObject(bytes, DetailedWorldMapView.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testBlendedWorldMapView() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(org.apache.fury.config.Language.JAVA).build();
        fury.registerSerializer(BlendedWorldMapView.class, new BlendedWorldMapViewSerializer(fury));
        fury.registerSerializer(GlobeMap.class, new GlobeMapSerializer(fury));

        BlendedWorldMapView data;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        data = new BlendedWorldMapView(world);
        data.generate();
        {
            byte[] bytes = fury.serializeJavaObject(data);
            BlendedWorldMapView data2 = fury.deserializeJavaObject(bytes, BlendedWorldMapView.class);
            Assert.assertEquals(data, data2);
        }
    }
}
