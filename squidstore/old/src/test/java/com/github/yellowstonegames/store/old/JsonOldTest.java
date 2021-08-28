package com.github.yellowstonegames.store.old;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.yellowstonegames.old.v300.DiverRNG;
import com.github.yellowstonegames.old.v300.GWTRNG;
import com.github.yellowstonegames.old.v300.LightRNG;
import com.github.yellowstonegames.old.v300.SilkRNG;
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
}
