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

import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.tantrum.jdkgdxds.IntObjectOrderedMapSerializer;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.world.*;
import com.github.yellowstonegames.world.BiomeMapper.*;
import com.github.yellowstonegames.world.PoliticalMapper.Faction;
import org.apache.fory.Fory;
import org.apache.fory.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

public class WorldTest {
    @Test
    public void testEllipticalWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(EllipticalWorldMap.class, new EllipticalWorldMapSerializer(fory));

        EllipticalWorldMap data = new EllipticalWorldMap(123, 200, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            EllipticalWorldMap data2 = fory.deserializeJavaObject(bytes, EllipticalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testGlobeMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(GlobeMap.class, new GlobeMapSerializer(fory));

        GlobeMap data = new GlobeMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            GlobeMap data2 = fory.deserializeJavaObject(bytes, GlobeMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testHexagonalWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(HexagonalWorldMap.class, new HexagonalWorldMapSerializer(fory));

        HexagonalWorldMap data = new HexagonalWorldMap(123, 200, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            HexagonalWorldMap data2 = fory.deserializeJavaObject(bytes, HexagonalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testHyperellipticalWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(HyperellipticalWorldMap.class, new HyperellipticalWorldMapSerializer(fory));

        HyperellipticalWorldMap data = new HyperellipticalWorldMap(123, 200, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            HyperellipticalWorldMap data2 = fory.deserializeJavaObject(bytes, HyperellipticalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testDiagonalWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(DiagonalWorldMap.class, new DiagonalWorldMapSerializer(fory));

        DiagonalWorldMap data = new DiagonalWorldMap(123, 200);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            DiagonalWorldMap data2 = fory.deserializeJavaObject(bytes, DiagonalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testLatLonWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(LatLonWorldMap.class, new LatLonWorldMapSerializer(fory));

        LatLonWorldMap data = new LatLonWorldMap(123, 200, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            LatLonWorldMap data2 = fory.deserializeJavaObject(bytes, LatLonWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLocalMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(LocalMap.class, new LocalMapSerializer(fory));

        LocalMap data = new LocalMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            LocalMap data2 = fory.deserializeJavaObject(bytes, LocalMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testMimicLocalMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(MimicLocalMap.class, new MimicLocalMapSerializer(fory));

        MimicLocalMap data = new MimicLocalMap(123);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            MimicLocalMap data2 = fory.deserializeJavaObject(bytes, MimicLocalMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testMimicWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(MimicWorldMap.class, new MimicWorldMapSerializer(fory));

        MimicWorldMap data = new MimicWorldMap(123);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            MimicWorldMap data2 = fory.deserializeJavaObject(bytes, MimicWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRotatingGlobeMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(RotatingGlobeMap.class, new RotatingGlobeMapSerializer(fory));

        RotatingGlobeMap data = new RotatingGlobeMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            RotatingGlobeMap data2 = fory.deserializeJavaObject(bytes, RotatingGlobeMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRoundSideWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(RoundSideWorldMap.class, new RoundSideWorldMapSerializer(fory));

        RoundSideWorldMap data = new RoundSideWorldMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            RoundSideWorldMap data2 = fory.deserializeJavaObject(bytes, RoundSideWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testStretchWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(StretchWorldMap.class, new StretchWorldMapSerializer(fory));

        StretchWorldMap data = new StretchWorldMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            StretchWorldMap data2 = fory.deserializeJavaObject(bytes, StretchWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testTilingWorldMap() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(TilingWorldMap.class, new TilingWorldMapSerializer(fory));

        TilingWorldMap data = new TilingWorldMap(123, 100, 100);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            TilingWorldMap data2 = fory.deserializeJavaObject(bytes, TilingWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimpleBiomeMapper() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(SimpleBiomeMapper.class, new SimpleBiomeMapperSerializer(fory));

        StretchWorldMap wmg = new StretchWorldMap(124, 200, 100);
        wmg.generate();
        SimpleBiomeMapper data = new SimpleBiomeMapper();
        data.makeBiomes(wmg);

        {
            byte[] bytes = fory.serializeJavaObject(data);
            SimpleBiomeMapper data2 = fory.deserializeJavaObject(bytes, SimpleBiomeMapper.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testDetailedBiomeMapper() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(DetailedBiomeMapper.class, new DetailedBiomeMapperSerializer(fory));

        StretchWorldMap wmg = new StretchWorldMap(124, 200, 100);
        wmg.generate();
        DetailedBiomeMapper data = new DetailedBiomeMapper();
        data.makeBiomes(wmg);

        {
            byte[] bytes = fory.serializeJavaObject(data);
            DetailedBiomeMapper data2 = fory.deserializeJavaObject(bytes, DetailedBiomeMapper.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testBlendedBiomeMapper() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(BlendedBiomeMapper.class, new BlendedBiomeMapperSerializer(fory));

        StretchWorldMap wmg = new StretchWorldMap(124, 200, 100);
        wmg.generate();
        BlendedBiomeMapper data = new BlendedBiomeMapper();
        data.makeBiomes(wmg);

        {
            byte[] bytes = fory.serializeJavaObject(data);
            BlendedBiomeMapper data2 = fory.deserializeJavaObject(bytes, BlendedBiomeMapper.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testUnrealisticBiomeMapper() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(UnrealisticBiomeMapper.class, new UnrealisticBiomeMapperSerializer(fory));

        StretchWorldMap wmg = new StretchWorldMap(124, 200, 100);
        wmg.generate();
        UnrealisticBiomeMapper data = new UnrealisticBiomeMapper();
        data.makeBiomes(wmg);

        {
            byte[] bytes = fory.serializeJavaObject(data);
            UnrealisticBiomeMapper data2 = fory.deserializeJavaObject(bytes, UnrealisticBiomeMapper.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testSimpleWorldMapView() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(SimpleWorldMapView.class, new SimpleWorldMapViewSerializer(fory));
        fory.registerSerializer(GlobeMap.class, new GlobeMapSerializer(fory));

        SimpleWorldMapView data;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        data = new SimpleWorldMapView(world);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            SimpleWorldMapView data2 = fory.deserializeJavaObject(bytes, SimpleWorldMapView.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testDetailedWorldMapView() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(DetailedWorldMapView.class, new DetailedWorldMapViewSerializer(fory));
        fory.registerSerializer(GlobeMap.class, new GlobeMapSerializer(fory));

        DetailedWorldMapView data;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        data = new DetailedWorldMapView(world);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            DetailedWorldMapView data2 = fory.deserializeJavaObject(bytes, DetailedWorldMapView.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testBlendedWorldMapView() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(BlendedWorldMapView.class, new BlendedWorldMapViewSerializer(fory));
        fory.registerSerializer(GlobeMap.class, new GlobeMapSerializer(fory));

        BlendedWorldMapView data;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        data = new BlendedWorldMapView(world);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            BlendedWorldMapView data2 = fory.deserializeJavaObject(bytes, BlendedWorldMapView.class);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testUnrealisticWorldMapView() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(UnrealisticWorldMapView.class, new UnrealisticWorldMapViewSerializer(fory));
        fory.registerSerializer(GlobeMap.class, new GlobeMapSerializer(fory));

        UnrealisticWorldMapView data;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        data = new UnrealisticWorldMapView(world);
        data.generate();
        {
            byte[] bytes = fory.serializeJavaObject(data);
            UnrealisticWorldMapView data2 = fory.deserializeJavaObject(bytes, UnrealisticWorldMapView.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testFaction() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(Faction.class, new FactionSerializer(fory));

        Faction data = new Faction(Language.randomLanguage(123), "The Joyous Land of Tormentia", "Tormentia", new String[]{Biome.DESERT_BIOME});
        {
            byte[] bytes = fory.serializeJavaObject(data);
            Faction data2 = fory.deserializeJavaObject(bytes, Faction.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testPoliticalMapper() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(Faction.class, new FactionSerializer(fory));
        fory.registerSerializer(IntObjectOrderedMap.class, new IntObjectOrderedMapSerializer(fory));
        fory.registerSerializer(StretchWorldMap.class, new StretchWorldMapSerializer(fory));
        fory.registerSerializer(BlendedBiomeMapper.class, new BlendedBiomeMapperSerializer(fory));
        fory.registerSerializer(PoliticalMapper.class, new PoliticalMapperSerializer(fory));
        PoliticalMapper data = new PoliticalMapper(new WhiskerRandom(123));
        StretchWorldMap w = new StretchWorldMap(123L, 20, 10, 1f);
        w.generate(12, 34);
        BlendedBiomeMapper bm = new BlendedBiomeMapper();
        bm.makeBiomes(w);
        data.generate(123L, w, bm, 5, 0.9f);
        {
            byte[] bytes = fory.serializeJavaObject(data);
            PoliticalMapper data2 = fory.deserializeJavaObject(bytes, PoliticalMapper.class);
            Assert.assertEquals(data, data2);
        }
    }
}
