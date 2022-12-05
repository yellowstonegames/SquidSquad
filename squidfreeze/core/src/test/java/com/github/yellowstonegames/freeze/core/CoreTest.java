package com.github.yellowstonegames.freeze.core;

import com.github.yellowstonegames.core.*;
import org.junit.Assert;
import org.junit.Test;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;

public class CoreTest {
    @Test
    public void testDiceRule() {
        Kryo kryo = new Kryo();
        kryo.register(Dice.Rule.class, new DiceRuleSerializer());

        Dice.Rule data = new Dice.Rule("3>4d6");

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            Dice d1 = new Dice(123L);
            Dice d2 = new Dice(123L);
            Dice.Rule data2 = kryo.readObject(input, Dice.Rule.class);
            Assert.assertEquals(d1.runRollRule(data), d2.runRollRule(data2));
            Assert.assertEquals(d1.runRollRule(data), d2.runRollRule(data2));
            Assert.assertEquals(data, data2);
        }
    }

}
