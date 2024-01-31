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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.BitConversion;
import com.github.yellowstonegames.core.IFlawed;
import com.github.yellowstonegames.core.annotations.Beta;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * An interface for point hashes that are statistically biased, as well as a holder for inner classes that implement
 * this. The point hashes here are mostly chosen because they are aesthetically interesting, at least on some of their
 * output bits.
 * <br>
 * Don't count on this class giving reliable output; it is marked Beta and will remain so. If you want to ensure a
 * particular behavior of a FlawedPointHash can be replicated, copy the implementation into your own code.
 */
@Beta
public interface FlawedPointHash extends IPointHash, IFlawed {
    /**
     * Produces hashes that show strong bias on one axis (usually the later axes matter more) and have nice-looking
     * patterns of dots. Better patterns are present in the higher bits.
     */
    class RugHash extends LongImpl implements FlawedPointHash {
        public RugHash() {
        }

        public RugHash(long state) {
            super(state);
        }

        public long getState() {
            return state;
        }

        public static int hashLongs(long x, long y, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
            return (int) (s >>> 32);
        }

        public static int hashLongs(long x, long y, long z, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + z);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (z + y);
            return (int) (s >>> 32);
        }

        public static int hashLongs(long x, long y, long z, long w, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + w);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            w = (w + 0x9E3779B97F4A7C15L ^ w) * (z + y);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (w + z);
            return (int) (s >>> 32);
        }

        public static int hashLongs(long x, long y, long z, long w, long u, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + u);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            w = (w + 0x9E3779B97F4A7C15L ^ w) * (z + y);
            u = (u + 0x9E3779B97F4A7C15L ^ u) * (w + z);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (u + w);
            return (int) (s >>> 32);
        }

        public static int hashLongs(long x, long y, long z, long w, long u, long v, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + v);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            w = (w + 0x9E3779B97F4A7C15L ^ w) * (z + y);
            u = (u + 0x9E3779B97F4A7C15L ^ u) * (w + z);
            v = (v + 0x9E3779B97F4A7C15L ^ v) * (u + w);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (v + u);
            return (int) (s >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int state) {
            return hashLongs(x, y, state);
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return hashLongs(x, y, z, state);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return hashLongs(x, y, z, w, state);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return hashLongs(x, y, z, w, u, state);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return hashLongs(x, y, z, w, u, v, state);
        }
    }

    /**
     * Extremely flawed if you're using this as a point hash, but meant to be aesthetically interesting, this produces
     * different symmetrical patterns in squares, as if on a quilt.
     */
    class QuiltHash extends LongImpl implements FlawedPointHash {
        private int size = 6;
        private long mask = (1L << size) - 1L;
        public QuiltHash() {
        }

        public QuiltHash(long state) {
            super(state);
        }

        public QuiltHash(long state, int size) {
            super(state);
            setSize(size);
        }

        public long getState() {
            return state;
        }

        public int getSize() {
            return 1 << size;
        }

        public void setSize(int size) {
            this.size = 32 - BitConversion.countLeadingZeros(max(1, size));
            mask = (1L << this.size) - 1L;
        }

        public long hashLongs(long x, long y, long s) {
            s ^= (x >> size) * 0xC13FA9A902A6328FL;
            s ^= (y >> size) * 0x91E10DA5C79E7B1DL;
            x *= x;
            y *= y;
            x = x >>> 1 & mask;
            y = y >>> 1 & mask;
            long t;
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
            return s;
        }

        public long hashLongs(long x, long y, long z, long s) {
            return hashLongs(x, hashLongs(y, z, s), s);
//            s ^= (x >> size) * 0xD1B54A32D192ED03L;
//            s ^= (y >> size) * 0xABC98388FB8FAC03L;
//            s ^= (z >> size) * 0x8CB92BA72F3D8DD7L;
//            x *= x;
//            y *= y;
//            z *= z;
//            x &= mask;
//            y &= mask;
//            z &= mask;
//            long t;
//            if (x < y && z < y) {
//                t = x;
//                x = y;
//                y = t;
//            }
//            else if(x < y && y < z){
//                t = x;
//                x = z;
//                z = t;
//            }
//            else if(y < x && x < z){
//                t = y;
//                y = z;
//                z = t;
//            }
//            else if(y < x && z < x){
//                t = y;
//                y = z;
//                z = t;
//            }
//
//            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + z);
//            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
//            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
//            s = (s + 0x9E3779B97F4A7C15L ^ s) * (z + y);
//            return (int) (s >>> 32);
        }

        public long hashLongs(long x, long y, long z, long w, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, w, s), s), s);
        }

        public long hashLongs(long x, long y, long z, long w, long u, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, u, s), s), s), s);
        }
        
        public long hashLongs(long x, long y, long z, long w, long u, long v, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, hashLongs(u, v, s), s), s), s), s);
        }

        @Override
        public int hashWithState(int x, int y, int state) {
            return (int)(hashLongs(x, y, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return (int)(hashLongs(x, y, z, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return (int)(hashLongs(x, y, z, w, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return (int)(hashLongs(x, y, z, w, u, state) >>> 32);
        }
        
        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return (int)(hashLongs(x, y, z, w, u, v, state) >>> 32);
        }
    }

    /**
     * Very similar to {@link QuiltHash}, but this doesn't change the pattern in different large squares, and instead
     * repeats a square or cube of symmetric and patterned results over and over (so it can be tiled).
     */
    class CubeHash extends LongImpl implements FlawedPointHash {
        private int size = 6;
        private long mask = (1L << size) - 1L;
        public CubeHash() {
        }

        public CubeHash(long state) {
            super(state);
        }

        public CubeHash(long state, int size) {
            super(state);
            setSize(size);
        }

        public long getState() {
            return state;
        }

        public int getSize() {
            return 1 << size;
        }

        public void setSize(int size) {
            this.size = 32 - BitConversion.countLeadingZeros(max(1, size));
            mask = (1L << this.size) - 1L;
        }

        public long hashLongs(long x, long y, long s) {
            x = x * x * 0xC13FA9A902A6328FL & mask;
            y = y * y * 0x91E10DA5C79E7B1DL & mask;
            long t;
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
            return s;
        }

        public long hashLongs(long x, long y, long z, long s) {
            // some randomizing, not great, doesn't need to be
            // the mask is a power of two minus 1, if larger, the cube of mirrors is larger.
            x = x * x * 0xD1B54A32D192ED03L & mask;
            y = y * y * 0xABC98388FB8FAC03L & mask;
            z = z * z * 0x8CB92BA72F3D8DD7L & mask;
            long t;
            // transpose part
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            if(x < z){
                t = x;
                x = z;
                z = t;
            }
            if(y < z){
                t = y;
                y = z;
                z = t;
            }
            // not actually sure why this works. It does incorporate all the states into the result.
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + z);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (z + y);
            return s;
        }

        public long hashLongs(long x, long y, long z, long w, long s) {
            x = x * x * 0xDB4F0B9175AE2165L & mask;
            y = y * y * 0xBBE0563303A4615FL & mask;
            z = z * z * 0xA0F2EC75A1FE1575L & mask;
            w = w * w * 0x89E182857D9ED689L & mask;
            long t;
            // transpose part
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            if(x < z){
                t = x;
                x = z;
                z = t;
            }
            if(y < z){
                t = y;
                y = z;
                z = t;
            }
            if (x < w) {
                t = x;
                x = w;
                w = t;
            }
            if(y < w){
                t = y;
                y = w;
                w = t;
            }
            if(z < w){
                t = z;
                z = w;
                w = t;
            }
            // not actually sure why this works. It does incorporate all the states into the result.
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + w);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            w = (w + 0x9E3779B97F4A7C15L ^ w) * (z + y);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (w + z);
            return s;
        }

        public long hashLongs(long x, long y, long z, long w, long u, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, u, s), s), s), s);
        }

        public long hashLongs(long x, long y, long z, long w, long u, long v, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, hashLongs(u, v, s), s), s), s), s);
        }

        @Override
        public int hashWithState(int x, int y, int state) {
            return (int)(hashLongs(x, y, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return (int)(hashLongs(x, y, z, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return (int)(hashLongs(x, y, z, w, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return (int)(hashLongs(x, y, z, w, u, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return (int)(hashLongs(x, y, z, w, u, v, state) >>> 32);
        }
    }

    /**
     * Like CubeHash, but with a squished Z axis in 3D and 4D, as well as a squashed W axis in 4D.
     */
    class SquishedCubeHash extends LongImpl implements FlawedPointHash {
        private int size = 6;
        private long mask = (1L << size) - 1L;
        public SquishedCubeHash() {
        }

        public SquishedCubeHash(long state) {
            super(state);
        }

        public SquishedCubeHash(long state, int size) {
            super(state);
            setSize(size);
        }

        public long getState() {
            return state;
        }

        public int getSize() {
            return 1 << size;
        }

        public void setSize(int size) {
            this.size = 32 - BitConversion.countLeadingZeros(max(1, size));
            mask = (1L << this.size) - 1L;
        }

        public long hashLongs(long x, long y, long s) {
            x = x * x * 0xC13FA9A902A6328FL & mask;
            y = y * y * 0x91E10DA5C79E7B1DL & mask;
            long t;
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
            return s;
        }

        public long hashLongs(long x, long y, long z, long s) {
//            z += z;

            x = x * x * 0xD1B54A32D192ED03L & mask;
            y = y * y * 0xABC98388FB8FAC03L & mask;
            z = z * z * 0x8CB92BA72F3D8DD7L & mask >>> 1;
            long t;
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            if(x < z){
                t = x;
                x = z;
                z = t;
            }
            if(y < z){
                t = y;
                y = z;
                z = t;
            }
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + z);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (z + y);
            return s;
        }

        public long hashLongs(long x, long y, long z, long w, long s) {
            x = x * x * 0xDB4F0B9175AE2165L & mask;
            y = y * y * 0xBBE0563303A4615FL & mask;
            z = z * z * 0xA0F2EC75A1FE1575L & mask >>> 1;
            w = w * w * 0x89E182857D9ED689L & mask >>> 1;
            long t;
            // transpose part
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            if(x < z){
                t = x;
                x = z;
                z = t;
            }
            if(y < z){
                t = y;
                y = z;
                z = t;
            }
            if (x < w) {
                t = x;
                x = w;
                w = t;
            }
            if(y < w){
                t = y;
                y = w;
                w = t;
            }
            if(z < w){
                t = z;
                z = w;
                w = t;
            }
            // not actually sure why this works. It does incorporate all the states into the result.
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + w);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            w = (w + 0x9E3779B97F4A7C15L ^ w) * (z + y);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (w + z);
            return s;
        }

        public long hashLongs(long x, long y, long z, long w, long u, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, u, s), s), s), s);
        }

        public long hashLongs(long x, long y, long z, long w, long u, long v, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, hashLongs(u, v, s), s), s), s), s);
        }

        @Override
        public int hashWithState(int x, int y, int state) {
            return (int)(hashLongs(x, y, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return (int)(hashLongs(x, y, z, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return (int)(hashLongs(x, y, z, w, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return (int)(hashLongs(x, y, z, w, u, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return (int)(hashLongs(x, y, z, w, u, v, state) >>> 32);
        }
    }

    /**
     * FNV32a is OK as a hash for bytes when used in some hash tables, but it has major issues on its low-order bits
     * when used as a point hash (the high bits aren't much better). Unfortunately, it is not aesthetically pleasing as
     * a point hash. Some usages might be able to use it to apply a grimy, glitchy effect.
     */
    class FNVHash extends IntImpl implements FlawedPointHash {

        public FNVHash() {
            super();
        }

        public FNVHash(int state) {
            super(state);
        }

        public int getState() {
            return state;
        }
        @Override
        public int hashWithState(int x, int y, int state) {
            return ((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193;
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return (((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193 ^ z) * 0x1000193;
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return ((((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193 ^ z) * 0x1000193 ^ w) * 0x1000193;
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return (((((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193 ^ z) * 0x1000193
                    ^ w) * 0x1000193 ^ u) * 0x1000193;
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return ((((((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193 ^ z) * 0x1000193
                    ^ w) * 0x1000193 ^ u) * 0x1000193 ^ v) * 0x1000193;
        }
    }

    class FlowerHash extends IPointHash.LongImpl implements FlawedPointHash {
        public FlowerHash() {
            super();
        }

        public FlowerHash(long state) {
            super(state);
        }

        public long getState() {
            return state;
        }

        public long hashLongs(long x, long y, long s) {
            x = Math.abs(x);
            y = Math.abs(y);
            return LongPointHash.hashAll(min(x, y), max(x, y), s);
        }

        public long hashLongs(long x, long y, long z, long s) {
            x = Math.abs(x);
            y = Math.abs(y);
            z = Math.abs(z);
            long min = min(x, min(y, z));
            long mid = max(min(x, y), min(max(x, y), z));
            long max = max(x, max(y, z));
            return LongPointHash.hashAll(min, mid, max, s);
        }

        public long hashLongs(long x, long y, long z, long w, long s) {
            x = Math.abs(x);
            y = Math.abs(y);
            z = Math.abs(z);
            w = Math.abs(w);
            long min = min(x, min(y, z));
            long mid = max(min(x, y), min(max(x, y), z));
            long max = max(x, max(y, z));
            if(w < max) {
                if(w >= mid) {
                    return LongPointHash.hashAll(min, mid, w, max, s);
                } else if(w >= min) {
                    return LongPointHash.hashAll(min, w, mid, max, s);
                } else {
                    return LongPointHash.hashAll(w, min, mid, max, s);
                }
            }
            return LongPointHash.hashAll(min, mid, max, w, s);
        }

        public long hashLongs(long x, long y, long z, long w, long u, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, u, s), s), s), s);
        }

        public long hashLongs(long x, long y, long z, long w, long u, long v, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, hashLongs(u, v, s), s), s), s), s);
        }

        @Override
        public int hashWithState(int x, int y, int state) {
            return (int)(hashLongs(x, y, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return (int)(hashLongs(x, y, z, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return (int)(hashLongs(x, y, z, w, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return (int)(hashLongs(x, y, z, w, u, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return (int)(hashLongs(x, y, z, w, u, v, state) >>> 32);
        }
    }

    /**
     * Makes smaller results on average, with a biased average at about 25% of the range instead of 50% of the range for
     * most non-biased hashes.
     */
    class LowLeaningHash extends IntImpl implements FlawedPointHash {
        public LowLeaningHash() {
            super();
        }

        public LowLeaningHash(int state) {
            super(state);
        }

        public int getState() {
            return state;
        }

        @Override
        public int hashWithState(int x, int y, int s) {
            s ^= x * 0x1827F5 ^ y * 0x123C21;
            s ^= (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27);
            return s & ((s ^ 0xD1B54A35) * 0x125493 ^ s >>> 11);
        }

        @Override
        public int hashWithState(int x, int y, int z, int s) {
            s ^= x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
            s ^= (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27);
            return s & ((s ^ 0xD1B54A35) * 0x125493 ^ s >>> 11);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int s) {
            s ^= x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
            s ^= (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27);
            return s & ((s ^ 0xD1B54A35) * 0x125493 ^ s >>> 11);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int s) {
            s ^= x * 0x1C3360 ^ y * 0x18DA3A ^ z * 0x15E6DA ^ w * 0x134D28 ^ u * 0x110280;
            s ^= (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27);
            return s & ((s ^ 0xD1B54A35) * 0x125493 ^ s >>> 11);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int s) {
            s ^= x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3;
            s ^= (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27);
            return s & ((s ^ 0xD1B54A35) * 0x125493 ^ s >>> 11);
        }
    }
}
