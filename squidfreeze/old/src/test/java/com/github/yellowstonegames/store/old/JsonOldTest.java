package com.github.yellowstonegames.freeze.old;

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
}
