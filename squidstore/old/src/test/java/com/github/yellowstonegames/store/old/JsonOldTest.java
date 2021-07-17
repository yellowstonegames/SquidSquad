package com.github.yellowstonegames.store.old;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.yellowstonegames.old.v300.LightRNG;
import org.junit.Assert;
import org.junit.Test;

public class JsonOldTest {
    @Test
    public void testDistinctRandom() {
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

}
