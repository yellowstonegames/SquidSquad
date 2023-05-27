/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.yellowstonegames.store.old;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.yellowstonegames.old.v300.*;
import org.junit.Assert;
import org.junit.Test;

public class JsonOldTest {
    @Test
    public void testLightRNG() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerLightRNG(json);
        LightRNG random = new LightRNG(123456789);
        random.nextLong();
        String data = json.toJson(random);
        System.out.println(data);
        LightRNG random2 = json.fromJson(LightRNG.class, data);
        System.out.println(Long.toString(random2.getSelectedState(0), 36));
        Assert.assertEquals(random.nextLong(), random2.nextLong());
    }

    @Test
    public void testDiverRNG() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerDiverRNG(json);
        DiverRNG random = new DiverRNG(123456789);
        random.nextLong();
        String data = json.toJson(random);
        System.out.println(data);
        DiverRNG random2 = json.fromJson(DiverRNG.class, data);
        System.out.println(Long.toString(random2.getSelectedState(0), 36));
        Assert.assertEquals(random.nextLong(), random2.nextLong());
    }

    @Test
    public void testLinnormRNG() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerLinnormRNG(json);
        LinnormRNG random = new LinnormRNG(123456789);
        random.nextLong();
        String data = json.toJson(random);
        System.out.println(data);
        LinnormRNG random2 = json.fromJson(LinnormRNG.class, data);
        System.out.println(Long.toString(random2.getSelectedState(0), 36));
        Assert.assertEquals(random.nextLong(), random2.nextLong());
    }

    @Test
    public void testThrustAltRNG() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerThrustAltRNG(json);
        ThrustAltRNG random = new ThrustAltRNG(123456789);
        random.nextLong();
        String data = json.toJson(random);
        System.out.println(data);
        ThrustAltRNG random2 = json.fromJson(ThrustAltRNG.class, data);
        System.out.println(Long.toString(random2.getSelectedState(0), 36));
        Assert.assertEquals(random.nextLong(), random2.nextLong());
    }

    @Test
    public void testGWTRNG() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerGWTRNG(json);
        GWTRNG random = new GWTRNG(123456789);
        random.nextLong();
        String data = json.toJson(random);
        System.out.println(data);
        GWTRNG random2 = json.fromJson(GWTRNG.class, data);
        System.out.println(Long.toString(random2.getSelectedState(0), 36));
        Assert.assertEquals(random.nextLong(), random2.nextLong());
    }

    @Test
    public void testSilkRNG() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerSilkRNG(json);
        SilkRNG random = new SilkRNG(123456789);
        random.nextLong();
        String data = json.toJson(random);
        System.out.println(data);
        SilkRNG random2 = json.fromJson(SilkRNG.class, data);
        System.out.println(Long.toString(random2.getSelectedState(0), 36));
        Assert.assertEquals(random.nextLong(), random2.nextLong());
    }

    @Test
    public void testLongPeriodRNG() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerLongPeriodRNG(json);
        LongPeriodRNG random = new LongPeriodRNG(123456789);
        random.nextLong();
        String data = json.toJson(random);
        System.out.println(data);
        LongPeriodRNG random2 = json.fromJson(LongPeriodRNG.class, data);
        System.out.println(Long.toString(random2.getSelectedState(0), 36));
        Assert.assertEquals(random.nextLong(), random2.nextLong());
    }

    @Test
    public void testXoshiroStarPhi32RNG() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerXoshiroStarPhi32RNG(json);
        XoshiroStarPhi32RNG random = new XoshiroStarPhi32RNG(123456789);
        random.nextLong();
        String data = json.toJson(random);
        System.out.println(data);
        XoshiroStarPhi32RNG random2 = json.fromJson(XoshiroStarPhi32RNG.class, data);
        System.out.println(Long.toString(random2.getSelectedState(0), 36));
        Assert.assertEquals(random.nextLong(), random2.nextLong());
    }

    @Test
    public void testLowStorageShuffler() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerLowStorageShuffler(json);
        LowStorageShuffler shuffler = new LowStorageShuffler(5, 12345, 67890);
        shuffler.next();
        String data = json.toJson(shuffler);
        System.out.println(data);
        LowStorageShuffler shuffler2 = json.fromJson(LowStorageShuffler.class, data);
        System.out.println(Long.toString(shuffler2.getKey0(), 36));
        System.out.println(Long.toString(shuffler2.getKey1(), 36));
        Assert.assertEquals(shuffler.next(), shuffler2.next());
    }

    @Test
    public void testCrossHashYolk() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerCrossHashYolk(json);
        CrossHash.Yolk hasher = CrossHash.Yolk.halphas;
        String data = json.toJson(hasher);
        System.out.println(data);
        CrossHash.Yolk hasher2 = json.fromJson(CrossHash.Yolk.class, data);
        Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
        Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
        Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
        Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
        long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
        Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
        Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
    }

    @Test
    public void testCrossHashCurlup() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerCrossHashCurlup(json);
        CrossHash.Curlup hasher = CrossHash.Curlup.halphas;
        String data = json.toJson(hasher);
        System.out.println(data);
        CrossHash.Curlup hasher2 = json.fromJson(CrossHash.Curlup.class, data);
        Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
        Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
        Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
        Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
        long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
        Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
        Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
    }

    @Test
    public void testCrossHashMist() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonOld.registerCrossHashMist(json);
        CrossHash.Mist hasher = CrossHash.Mist.psi;
        String data = json.toJson(hasher);
        System.out.println(data);
        CrossHash.Mist hasher2 = json.fromJson(CrossHash.Mist.class, data);
        Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
        Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
        Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
        Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
        long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
        Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
        Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
    }
}
