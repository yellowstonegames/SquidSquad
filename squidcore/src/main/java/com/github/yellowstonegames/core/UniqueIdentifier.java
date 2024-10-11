package com.github.yellowstonegames.core;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A substitute for the UUID class, since it isn't available on GWT.
 * The typical usage is to call {@link #next()} when you want a new UniqueIdentifier. If the app is closing down and
 * needs to save its state to be resumed later, {@link #GENERATOR} must be serialized as well, and deserialized before
 * calling {@link #next()} again after resuming. Without this last step, the generated identifiers are <em>likely</em>
 * to be unique, but not <em>guaranteed</em> to be unique.
 * <br>
 * This can be serialized out-of-the-box to Strings using {@link #stringSerialize()}, but if you do, so must the
 * {@link #GENERATOR} that produces new UniqueIdentifier instances and ensures they are unique.
 * If you are using Fury or another type of serializer that can use {@link Externalizable} objects, this can be sent
 * directly to that without needing any extra serialization code. Like with the String serialization, you must serialize
 * the {@link #GENERATOR} field and restore it when restarting from a serialized state.
 * <br>
 * This is also Comparable, for some reason (UUID is, but since these should all be random, it doesn't mean much).
 * UniqueIdentifier supports up to 2 to the 128 minus 1 unique instances, which should be far more than enough for
 * centuries of generation. If you were using UUID, it only supports 2 to the 122 unique random UUIDs, with a collision
 * 50% likely after 2 to the 61 UUIDs were generated. If this is used properly, it can't collide until all (2 to the 128
 * minus 1) identifiers have been generated.
 */
@Beta
public final class UniqueIdentifier implements Comparable<UniqueIdentifier>, Externalizable {

    private int a;
    private int b;
    private int c;
    private int d;

    /**
     * Creates a new, invalid UniqueIdentifier. All states will be 0.
     */
    public UniqueIdentifier(){
        a = 0;
        b = 0;
        c = 0;
        d = 0;
    }

    /**
     * Creates a new UniqueIdentifier that may or may not actually be unique. This uses the given states verbatim.
     * If both all states are 0, this will be treated as an invalid identifier. Most usage should prefer
     * {@link #next()} instead.
     *
     * @param hi the high 64 bits, as a long
     * @param lo the low 64 bits, as a long
     */
    public UniqueIdentifier(long hi, long lo){
        this.a = (int)(hi >>> 32);
        this.b = (int)hi;
        this.c = (int)(lo >>> 32);
        this.d = (int)lo;
    }

    /**
     * Creates a new UniqueIdentifier that may or may not actually be unique. This uses the given states verbatim.
     * If all states are 0, this will be treated as an invalid identifier. Most usage should prefer
     * {@link #next()} instead.
     *
     * @param stateA will be used verbatim for state a
     * @param stateB will be used verbatim for state b
     * @param stateC will be used verbatim for state c
     * @param stateD will be used verbatim for state d
     */
    public UniqueIdentifier(int stateA, int stateB, int stateC, int stateD){
        this.a = stateA;
        this.b = stateB;
        this.c = stateC;
        this.d = stateD;
    }

    /**
     * Given a String containing the output of {@link #stringSerialize()}, this creates a new UniqueIdentifier
     * with the same data as the UniqueIdentifier that was serialized.
     * @param serialized a String almost always produced by {@link #stringSerialize()}
     */
    public UniqueIdentifier(String serialized){
        a = Base.BASE16.readInt(serialized, 0, 8);
        b = Base.BASE16.readInt(serialized, 9, 17);
        c = Base.BASE16.readInt(serialized, 18, 26);
        d = Base.BASE16.readInt(serialized, 27, 35);
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public int getC() {
        return c;
    }

    public int getD() {
        return d;
    }

    public long getHi() {
        return (long) a << 32 | (b & 0xFFFFFFFFL);
    }

    public long getLo() {
        return (long) c << 32 | (d & 0xFFFFFFFFL);
    }

    /**
     * @return false if this instance was produced by {@link #UniqueIdentifier()} and not modified; true otherwise
     */
    public boolean isValid() {
        return ((a | b | c | d) != 0L);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueIdentifier that = (UniqueIdentifier) o;

        return a == that.a && b == that.b && c == that.c && d == that.d;
    }

    @Override
    public int hashCode() {
        return a ^ b ^ c ^ d;
    }

    @Override
    public String toString() {
        return "UniqueIdentifier{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                '}';
    }

    /**
     * Serializes this UniqueIdentifier to a String, where it can be read back by {@link #stringDeserialize(String)}.
     * This is different from most other stringSerialize() methods in that it always produces a 35-character String,
     * consisting of {@link #getA()}, then a {@code '_'}, then {@link #getB()}, then another underscore, c, underscore,
     * and finally d, with a, b, c, and d represented as unsigned hex int Strings.
     * @return a 33-character-long String storing this identifier; can be read back with {@link #stringDeserialize(String)}
     */
    public String stringSerialize() {
        StringBuilder sb = Base.BASE16.appendUnsigned(new StringBuilder(35), a).append('_');
        Base.BASE16.appendUnsigned(sb, b).append('_');
        Base.BASE16.appendUnsigned(sb, c).append('_');
        Base.BASE16.appendUnsigned(sb, d);
        return sb.toString();
    }

    /**
     * Reads back a String produced by {@link #stringSerialize()}, storing the result in this UniqueIdentifier.
     * @param data a String almost certainly produced by {@link #stringSerialize()}
     * @return this UniqueIdentifier, after it has been modified.
     */
    public UniqueIdentifier stringDeserialize(String data) {
        a = Base.BASE16.readInt(data, 0, 8);
        b = Base.BASE16.readInt(data, 9, 17);
        c = Base.BASE16.readInt(data, 18, 26);
        d = Base.BASE16.readInt(data, 27, 35);
        return this;
    }

    // Json.Serializable implementations, should be used by squidstore
//    @Override
//    public void write(Json json) {
//        json.writeObjectStart("ui");
//        json.writeValue("h", hi);
//        json.writeValue("l", lo);
//        json.writeObjectEnd();
//    }
//
//    @Override
//    public void read(Json json, JsonValue jsonData) {
//        jsonData = jsonData.get("ui");
//        hi = jsonData.getLong("h");
//        lo = jsonData.getLong("l");
//    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(a);
        out.writeInt(b);
        out.writeInt(c);
        out.writeInt(d);
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     */
    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        a = in.readInt();
        b = in.readInt();
        c = in.readInt();
        d = in.readInt();
    }

    /**
     * The {@link Generator} that actually produces unique identifiers.
     * If your application pauses and needs to be resumed later by loading serialized state,
     * you must include this field in what you serialize, and load it before creating any
     * additional UniqueIdentifier values with {@link #next()} or {@link Generator#generate()}.
     * Failure to maintain the previous GENERATOR value can result in identifiers not being unique.
     */
    public static Generator GENERATOR = new Generator();

    /**
     * Generates a UniqueIdentifier that will actually be unique, assuming {@link #GENERATOR}
     * is non-null and has had its state tracked with the rest of the program (see the docs
     * for {@link #GENERATOR}).
     * @return a new UniqueIdentifier that should be actually unique
     */
    public static UniqueIdentifier next() {
        return GENERATOR.generate();
    }

    @Override
    public int compareTo(UniqueIdentifier other) {
        int r = Integer.compare(a, other.a);
        if(r != 0) return r;
        r = Integer.compare(b, other.b);
        if(r != 0) return r;
        r = Integer.compare(c, other.c);
        if(r != 0) return r;
        return Integer.compare(d, other.d);
    }

    /**
     * The type used as a factory to produce UniqueIdentifiers that are actually unique for a given Generator.
     * This is used in {@link UniqueIdentifier#GENERATOR}, and can be used independently via {@link #generate()}.
     */
    public static final class Generator implements Externalizable {
        private int a;
        private int b;
        private int c;
        private int d;

        /**
         * Creates a new Generator with one of (2 to the 64) possible random initial states.
         */
        public Generator() {
            long state = Hasher.randomize3(System.currentTimeMillis()) ^ EnhancedRandom.seedFromMath();
            a = (int)(state>>>32);
            b = (int)state;
            state = Hasher.randomize3(state);
            c = (int)(state>>>32);
            d = (int)state;
        }

        /**
         * Creates a new Generator given two long values for state.
         * @param stateA may be any long
         * @param stateB may be any long unless both states are 0, in which case this is treated as 1
         */
        public Generator(long stateA, long stateB) {
            a = (int)(stateA>>>32);
            b = (int)stateA;
            stateB = (stateA | stateB) == 0L ? 1L : stateB;
            c = (int)(stateB>>>32);
            d = (int)stateB;
        }

        /**
         * Creates a new Generator using the given 4 states verbatim, unless they are all 0 (then it treats d as 1).
         * @param a may be any int unless all are 0
         * @param b may be any int unless all are 0
         * @param c may be any int unless all are 0
         * @param d may be any int unless all are 0
         */
        public Generator(int a, int b, int c, int d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = (a | b | c | d) == 0 ? 1 : d;
        }

        /**
         * Given a String containing the output of {@link #stringSerialize()}, this creates a new
         * UniqueIdentifier.Generator with the same data as the UniqueIdentifier.Generator that was serialized.
         * @param serialized a String almost always produced by {@link #stringSerialize()}
         */
        public Generator(String serialized){
            a = Base.BASE16.readInt(serialized, 0, 8);
            b = Base.BASE16.readInt(serialized, 9, 17);
            c = Base.BASE16.readInt(serialized, 18, 26);
            d = Base.BASE16.readInt(serialized, 27, 35);
        }

        /**
         * Creates a new UniqueIdentifier, advancing the state of this Generator in the process.
         * @return a new UniqueIdentifier that will not occur again from this Generator unless (2 to the 128) - 1 more identifiers are generated
         */
        public UniqueIdentifier generate(){
            // xoshiro algorithm
            int t = b << 9;
            c ^= a;
            d ^= b;
            b ^= c;
            a ^= d;
            c ^= t;
            d = (d << 11 | d >>> 21);
            return new UniqueIdentifier(a, b, c, d);
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public int getC() {
            return c;
        }

        public int getD() {
            return d;
        }

        public String stringSerialize() {
            StringBuilder sb = Base.BASE16.appendUnsigned(new StringBuilder(35), a).append('$');
            Base.BASE16.appendUnsigned(sb, b).append('$');
            Base.BASE16.appendUnsigned(sb, c).append('$');
            Base.BASE16.appendUnsigned(sb, d);
            return sb.toString();
        }

        public Generator stringDeserialize(String data) {
            a = Base.BASE16.readInt(data, 0, 8);
            b = Base.BASE16.readInt(data, 9, 17);
            c = Base.BASE16.readInt(data, 18, 26);
            d = Base.BASE16.readInt(data, 27, 35);
            return this;
        }

        // Json.Serializable implementations, should be used by squidstore
//        @Override
//        public void write(Json json) {
//            json.writeObjectStart("uig");
//            json.writeValue("a", stateA);
//            json.writeValue("b", stateB);
//            json.writeObjectEnd();
//        }
//
//        @Override
//        public void read(Json json, JsonValue jsonData) {
//            jsonData = jsonData.get("uig");
//            stateA = jsonData.getLong("a");
//            stateB = jsonData.getLong("b");
//        }

        /**
         * The object implements the writeExternal method to save its contents
         * by calling the methods of DataOutput for its primitive values or
         * calling the writeObject method of ObjectOutput for objects, strings,
         * and arrays.
         *
         * @param out the stream to write the object to
         * @throws IOException Includes any I/O exceptions that may occur
         * @serialData Overriding methods should use this tag to describe
         * the data layout of this Externalizable object.
         * List the sequence of element types and, if possible,
         * relate the element to a public/protected field and/or
         * method of this Externalizable class.
         */
        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(a);
            out.writeInt(b);
            out.writeInt(c);
            out.writeInt(d);
        }

        /**
         * The object implements the readExternal method to restore its
         * contents by calling the methods of DataInput for primitive
         * types and readObject for objects, strings and arrays.  The
         * readExternal method must read the values in the same sequence
         * and with the same types as were written by writeExternal.
         *
         * @param in the stream to read data from in order to restore the object
         * @throws IOException            if I/O errors occur
         */
        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            a = in.readInt();
            b = in.readInt();
            c = in.readInt();
            d = in.readInt();
        }
    }
}
