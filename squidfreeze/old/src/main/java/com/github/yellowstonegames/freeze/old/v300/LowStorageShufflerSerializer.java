package com.github.yellowstonegames.freeze.old.v300;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.old.v300.LowStorageShuffler;

/**
 * Serializer for {@link LowStorageShuffler}; doesn't need anything else registered.
 */
public class LowStorageShufflerSerializer extends Serializer<LowStorageShuffler> {
    public LowStorageShufflerSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final LowStorageShuffler data) {
        output.writeInt(data.getBound(), true);
        output.writeInt(data.getKey0());
        output.writeInt(data.getKey1());
        output.writeInt(data.getIndex(), true);
    }

    @Override
    public LowStorageShuffler read(final Kryo kryo, final Input input, final Class<? extends LowStorageShuffler> dataClass) {
        return new LowStorageShuffler(input.readInt(true), input.readInt(), input.readInt()).setIndex(input.readInt(true));
    }

    @Override
    public LowStorageShuffler copy(Kryo kryo, LowStorageShuffler original) {
        return new LowStorageShuffler(original.getBound(), original.getKey0(), original.getKey1())
                .setIndex(original.getIndex());
    }
}

