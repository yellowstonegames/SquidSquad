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

package com.github.yellowstonegames.store.world;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.yellowstonegames.world.*;
import com.github.yellowstonegames.world.BiomeMapper.*;
import org.junit.Assert;
import org.junit.Test;

public class JsonWorldTest {
    @Test
    public void testEllipticalWorldMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        EllipticalWorldMap wmg, wmg2;
        wmg = new EllipticalWorldMap(12345, 200, 100);
        wmg.generate();
        JsonWorld.registerEllipticalWorldMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(EllipticalWorldMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testGlobeMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        GlobeMap wmg, wmg2;
        wmg = new GlobeMap(12345, 100, 100);
        wmg.generate();
        JsonWorld.registerGlobeMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(GlobeMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testHexagonalWorldMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        HexagonalWorldMap wmg, wmg2;
        wmg = new HexagonalWorldMap(12345, 200, 100);
        wmg.generate();
        JsonWorld.registerHexagonalWorldMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(HexagonalWorldMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testHyperellipticalWorldMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        HyperellipticalWorldMap wmg, wmg2;
        wmg = new HyperellipticalWorldMap(12345, 200, 100);
        wmg.generate();
        JsonWorld.registerHyperellipticalWorldMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(HyperellipticalWorldMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testLatLonWorldMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        LatLonWorldMap wmg, wmg2;
        wmg = new LatLonWorldMap(12345, 200, 100);
        wmg.generate();
        JsonWorld.registerLatLonWorldMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(LatLonWorldMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testLocalMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        LocalMap wmg, wmg2;
        wmg = new LocalMap(12345, 100, 100);
        wmg.generate();
        JsonWorld.registerLocalMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(LocalMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testMimicLocalMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        MimicLocalMap wmg, wmg2;
        wmg = new MimicLocalMap(12345);
        wmg.generate();
        JsonWorld.registerMimicLocalMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(MimicLocalMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testMimicWorldMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        MimicWorldMap wmg, wmg2;
        wmg = new MimicWorldMap(12345);
        wmg.generate();
        JsonWorld.registerMimicWorldMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(MimicWorldMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testRotatingGlobeMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        RotatingGlobeMap wmg, wmg2;
        wmg = new RotatingGlobeMap(12345, 100, 100);
        wmg.generate();
        JsonWorld.registerRotatingGlobeMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(RotatingGlobeMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testRoundSideWorldMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        RoundSideWorldMap wmg, wmg2;
        wmg = new RoundSideWorldMap(12345, 200, 100);
        wmg.generate();
        JsonWorld.registerRoundSideWorldMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(RoundSideWorldMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testStretchWorldMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        StretchWorldMap wmg, wmg2;
        wmg = new StretchWorldMap(12345, 200, 100);
        wmg.generate();
        JsonWorld.registerStretchWorldMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(StretchWorldMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testTilingWorldMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        TilingWorldMap wmg, wmg2;
        wmg = new TilingWorldMap(12345, 100, 100);
        wmg.generate();
        JsonWorld.registerTilingWorldMap(json);
        String data = json.toJson(wmg);
        wmg2 = json.fromJson(TilingWorldMap.class, data);
        Assert.assertEquals(wmg, wmg2);
    }

    @Test
    public void testSimpleBiomeMapper() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        SimpleBiomeMapper bm, bm2;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        bm = new SimpleBiomeMapper();
        bm.makeBiomes(world);
        JsonWorld.registerSimpleBiomeMapper(json);
        String data = json.toJson(bm);
        bm2 = json.fromJson(SimpleBiomeMapper.class, data);
        Assert.assertEquals(bm, bm2);
    }

    @Test
    public void testDetailedBiomeMapper() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        DetailedBiomeMapper bm, bm2;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        bm = new DetailedBiomeMapper();
        bm.makeBiomes(world);
        JsonWorld.registerDetailedBiomeMapper(json);
        String data = json.toJson(bm);
        bm2 = json.fromJson(DetailedBiomeMapper.class, data);
        Assert.assertEquals(bm, bm2);
    }

    @Test
    public void testBlendedBiomeMapper() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        BlendedBiomeMapper bm, bm2;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        bm = new BlendedBiomeMapper();
        bm.makeBiomes(world);
        JsonWorld.registerBlendedBiomeMapper(json);
        String data = json.toJson(bm);
        bm2 = json.fromJson(BlendedBiomeMapper.class, data);
        Assert.assertEquals(bm, bm2);
    }

    @Test
    public void testUnrealisticBiomeMapper() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        UnrealisticBiomeMapper bm, bm2;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        bm = new UnrealisticBiomeMapper();
        bm.makeBiomes(world);
        JsonWorld.registerUnrealisticBiomeMapper(json);
        String data = json.toJson(bm);
        bm2 = json.fromJson(UnrealisticBiomeMapper.class, data);
        Assert.assertEquals(bm, bm2);
    }

    @Test
    public void testDetailedWorldMapView() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        DetailedWorldMapView wmv, wmv2;
        GlobeMap world = new GlobeMap(1234567, 100, 100);
        world.generate();
        wmv = new DetailedWorldMapView(world);
        wmv.generate();
        JsonWorld.registerDetailedWorldMapView(json);
        JsonWorld.registerGlobeMap(json);
        String data = json.toJson(wmv);
        wmv2 = json.fromJson(DetailedWorldMapView.class, data);
        Assert.assertEquals(wmv, wmv2);
    }
}
