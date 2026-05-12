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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.ds.EnhancedCollection;
import com.github.tommyettinger.ds.IntSet;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectSet;
import com.github.tommyettinger.ds.Utilities;

import java.io.IOException;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.github.tommyettinger.ds.Utilities.tableSize;

/**
 * A variant on jdkgdxds' {@link IntSet} class that only holds Coord items, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold items placed into here, you
 * should use a normal {@link ObjectSet} instead, since some optimizations here require Coord items to be in the pool.
 * <br>
 * If no initialCapacity is supplied, or if this must resize to enter a Coord, this will use a capacity at least as
 * large as the Coord cache, as defined by {@link Coord#getCacheWidth()} by {@link Coord#getCacheHeight()}. While this
 * means that any resizing will potentially make this use much more memory, it avoids a situation where some dense key
 * sets could take hundreds of times longer than they should. It also usually doesn't use drastically more memory unless
 * the Coord pool has been expanded quite a bit. If the Coord pool hasn't been expanded, each set should use about 1MB
 * of memory or less when created with the default constructor.
 * <br>
 * This tends to perform significantly better with a high low factor, such as 0.9f, instead of a lower one like 0.5f .
 * It also performs its best when the initial capacity is sufficient to hold every item this needs without resizing, but
 * it typically only has to resize once if it has to resize at all.
 */
public class CoordPureSet extends AbstractSet<Coord> implements Iterable<Coord>, EnhancedCollection<Coord> {
    public CoordPureSet() {
        this(Coord.getCacheWidth() * Coord.getCacheHeight(), 0.9f);
    }

    protected int size;

    protected int[] keyTable;
    protected boolean hasZeroValue;

    /**
     * Between 0f (exclusive) and 1f (inclusive, if you're careful), this determines how full the backing table
     * can get before this increases their size. Larger values use less memory but make the data structure slower.
     */
    protected float loadFactor;

    /**
     * Precalculated value of {@code (int)(keyTable.length * loadFactor)}, used to determine when to resize.
     */
    protected int threshold;

    /**
     * Used by {@link #place(int)} to bit shift the upper bits of an {@code int} into a usable range (&gt;= 0 and &lt;=
     * {@link #mask}). The shift can be negative, which is convenient to match the number of bits in mask: if mask is a 7-bit
     * number, a shift of -7 shifts the upper 7 bits into the lowest 7 positions. This class sets the shift &gt; 32 and &lt; 64,
     * which when used with an int will still move the upper bits of an int to the lower bits due to Java's implicit modulus on
     * shifts.
     * <p>
     * {@link #mask} can also be used to mask the low bits of a number, which may be faster for some hashcodes, if
     * {@link #place(int)} is overridden.
     */
    protected int shift;

    /**
     * A bitmask used to confine hash codes to the size of the table. Must be all 1-bits in its low positions, ie a power of two
     * minus 1. If {@link #place(int)} is overridden, this can be used instead of {@link #shift} to isolate usable bits of a
     * hash.
     */
    protected int mask;

    /**
     * Creates a new set with a load factor of {@link Utilities#getDefaultLoadFactor()}.
     *
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
     */
    public CoordPureSet(int initialCapacity) {
        this(initialCapacity, Utilities.getDefaultLoadFactor());
    }

    /**
     * Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity items before
     * growing the backing table.
     *
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
     * @param loadFactor      what fraction of the capacity can be filled before this has to resize; 0 &lt; loadFactor &lt;= 1
     */
    public CoordPureSet(int initialCapacity, float loadFactor) {
        if (loadFactor <= 0f || loadFactor > 1f) {
            throw new IllegalArgumentException("loadFactor must be > 0 and <= 1: " + loadFactor);
        }
        this.loadFactor = loadFactor;

        int tableSize = tableSize(initialCapacity, loadFactor);
        mask = tableSize - 1;
        threshold = Math.min((int) (tableSize * (double) loadFactor + 1), mask);
        shift = BitConversion.countLeadingZeros(mask) + 32;

        keyTable = new int[tableSize];
    }

    /**
     * Creates a new instance containing the items in the specified iterator.
     *
     * @param coll an iterator that will have its remaining contents added to this
     */
    public CoordPureSet(Iterator<Coord> coll) {
        this();
        addAll(coll);
    }

    /**
     * Creates a new set identical to the specified set.
     */
    public CoordPureSet(CoordPureSet set) {
        this((int) (set.keyTable.length * set.loadFactor), set.loadFactor);
        System.arraycopy(set.keyTable, 0, keyTable, 0, set.keyTable.length);
        size = set.size;
        hasZeroValue = set.hasZeroValue;
    }

    /**
     * Creates a new set using all distinct items in the given Collection of Coord.
     *
     * @param coll a Collection of Coord that will be used in full, except for duplicate items
     */
    public CoordPureSet(Collection<Coord> coll) {
        this(coll.size());
        addAll(coll);
    }

    /**
     * Creates a new set using {@code length} items from the given {@code array}, starting at {@code} offset (inclusive).
     *
     * @param array  an array to draw items from
     * @param offset the first index in array to draw an item from
     * @param length how many items to take from array; bounds-checking is the responsibility of the using code
     */
    public CoordPureSet(Coord[] array, int offset, int length) {
        this(length);
        addAll(array, offset, length);
    }

    /**
     * Creates a new set containing all the items in the given array.
     *
     * @param array an array that will be used in full, except for duplicate items
     */
    public CoordPureSet(Coord[] array) {
        this(array, 0, array.length);
    }

    /**
     * Returns an index &gt;= 0 and &lt;= {@link #mask} for the specified {@code item}.
     *
     * @param item any int; it is usually mixed and shifted or masked here
     * @return an index between 0 and {@link #mask} (both inclusive)
     */
    protected int place(int item) {
        return BitConversion.imul(item ^ 0xC143F257, 0xFAB9E45B) >>> shift;
    }

    /**
     * Returns true if the key was not already in the set.
     */
    @Override
    public boolean add(Coord key) {
        int code = key.encode();
        if (code == 0) {
            if (hasZeroValue) return false;
            hasZeroValue = true;
            size++;
            return true;
        }
        int[] keyTable = this.keyTable;

        for (int i = place(code); ; i = i + 1 & mask) {
            int other = keyTable[i];
            if (code == other)
                return false; // Existing key was found.
            if (other == 0) {
                keyTable[i] = code;
                if (++size >= threshold) {
                    resize(keyTable.length << 1);
                }
                return true;
            }
        }
    }

    public boolean addAll(Collection<? extends Coord> array) {
        ensureCapacity(array.size());
        boolean changed = false;
        for (Coord c : array) {
            changed |= add(c);
        }
        return changed;
    }

    public boolean addAll(List<Coord> array, int offset, int length) {
        if (offset + length > array.size()) {
            throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size());
        }
        ensureCapacity(length);
        boolean changed = false;
        for (int i = offset, n = offset + length; i < n; i++) {
            changed |= add(array.get(i));
        }
        return changed;
    }

    public boolean addAll(Coord... array) {
        return addAll(array, 0, array.length);
    }

    public boolean addAll(Coord[] array, int offset, int length) {
        ensureCapacity(length);
        int oldSize = size;
        for (int i = offset, n = i + length; i < n; i++) {
            add(array[i]);
        }
        return size != oldSize;
    }

    public boolean addAll(CoordPureSet set) {
        ensureCapacity(set.size);
        int oldSize = size;
        if (set.hasZeroValue) {
            add(Coord.get(0, 0));
        }
        int[] keyTable = set.keyTable;
        for (int i = 0, n = keyTable.length; i < n; i++) {
            int key = keyTable[i];
            if (key != 0) {
                add(Coord.decode(key));
            }
        }
        return size != oldSize;
    }

    /**
     * Skips checks for existing keys, doesn't increment size, doesn't need to handle key 0.
     */
    protected void addResize(int key) {
        int[] keyTable = this.keyTable;
        for (int i = place(key); ; i = i + 1 & mask) {
            if (keyTable[i] == 0) {
                keyTable[i] = key;
                return;
            }
        }
    }

    /**
     * Returns true if the key was removed.
     */
    @Override
    public boolean remove(Object key) {
        if(!(key instanceof Coord))
            return false;
        int code = ((Coord)key).encode();
        if (code == 0) {
            if (hasZeroValue) {
                hasZeroValue = false;
                size--;
                return true;
            }
            return false;
        }

        int pos;
        int mask = this.mask;
        int[] keyTable = this.keyTable;
        for (int i = place(code); ; i = i + 1 & mask) {
            int other = keyTable[i];
            if (other == 0) {
                return false; // Nothing is present.
            }
            if (other == code) {
                pos = i; // Same key was found.
                break;
            }
        }
        int last, slot;
        size--;
        for (; ; ) {
            pos = ((last = pos) + 1) & mask;
            for (; ; ) {
                if ((code = keyTable[pos]) == 0) {
                    keyTable[last] = 0;
//					if(mask >= minCapacity && size < (threshold >>> 2))
//						resize(keyTable.length >>> 1);
                    return true;
                }
                slot = place(code);
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
                pos = (pos + 1) & mask;
            }
            keyTable[last] = code;
        }
    }

    /**
     * Returns true if the set has one or more items.
     */
    public boolean notEmpty() {
        return size != 0;
    }

    /**
     * Returns true if the set is empty.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Reduces the size of the backing arrays to be the specified capacity / loadFactor, or less. If the capacity is already less,
     * nothing is done. If the set contains more items than the specified capacity, the next highest power of two capacity is used
     * instead.
     */
    public void shrink(int maximumCapacity) {
        if (maximumCapacity < 0) {
            throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
        }
        int tableSize = tableSize(maximumCapacity, loadFactor);
        if (keyTable.length > tableSize) {
            resize(tableSize);
        }
    }

    /**
     * Clears the set and reduces the size of the backing arrays to be the specified capacity / loadFactor, if they are larger.
     */
    public void clear(int maximumCapacity) {
        int tableSize = tableSize(maximumCapacity, loadFactor);
        if (keyTable.length <= tableSize) {
            clear();
            return;
        }
        size = 0;
        hasZeroValue = false;
        resize(tableSize);
    }

    @Override
    public void clear() {
        if (size == 0) {
            return;
        }
        size = 0;
        Arrays.fill(keyTable, 0);
        hasZeroValue = false;
    }

    @Override
    public boolean contains(Object key) {
        if(!(key instanceof Coord))
            return false;
        int code = ((Coord)key).encode();
        if (code == 0) {
            return hasZeroValue;
        }
        int[] keyTable = this.keyTable;
        for (int i = place(code); ; i = i + 1 & mask) {
            int other = keyTable[i];
            if (code == other)
                return true;
            if (other == 0)
                return false;
        }
    }

    public Coord first() {
        if (hasZeroValue) {
            return Coord.get(0, 0);
        }
        int[] keyTable = this.keyTable;
        for (int i = 0, n = keyTable.length; i < n; i++) {
            if (keyTable[i] != 0) {
                return Coord.decode(keyTable[i]);
            }
        }
        throw new IllegalStateException("CoordPureSet is empty.");
    }

    /**
     * Increases the size of the backing array to accommodate the specified number of additional items / loadFactor. Useful before
     * adding many items to avoid multiple backing array resizes.
     */
    public void ensureCapacity(int additionalCapacity) {
        int tableSize = tableSize(size + additionalCapacity, loadFactor);
        if (keyTable.length < tableSize) {
            resize(tableSize);
        }
    }

    protected void resize(int newSize) {
        newSize = Math.max(newSize, Utilities.tableSize(Coord.getCacheWidth() * Coord.getCacheHeight(), loadFactor));
        int oldCapacity = keyTable.length;
        mask = newSize - 1;
        threshold = Math.min((int) (newSize * (double) loadFactor + 1), mask);
        shift = BitConversion.countLeadingZeros(mask) + 32;

        int[] oldKeyTable = keyTable;

        keyTable = new int[newSize];

        if (size > 0) {
            for (int i = 0; i < oldCapacity; i++) {
                int key = oldKeyTable[i];
                if (key != 0) {
                    addResize(key);
                }
            }
        }
    }

    /**
     * Effectively does nothing here because the hashMultiplier is not used currently.
     *
     * @return 1; a hashMultiplier is not used in this class
     */
    public int getHashMultiplier() {
        return 1;
    }

    /**
     * Effectively does nothing here because the hashMultiplier is not used currently.
     * Subclasses can use this to set some kind of identifier or user data, though.
     *
     * @param hashMultiplier any int; will not be used
     */
    public void setHashMultiplier(int hashMultiplier) {
    }

    public float getLoadFactor() {
        return loadFactor;
    }

    public void setLoadFactor(float loadFactor) {
        if (loadFactor <= 0f || loadFactor > 1f) {
            throw new IllegalArgumentException("loadFactor must be > 0 and <= 1: " + loadFactor);
        }
        this.loadFactor = loadFactor;
        int tableSize = tableSize(size, loadFactor);
        if (tableSize - 1 != mask) {
            resize(tableSize);
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        int[] keyTable = this.keyTable;
        for (int i = 0, n = keyTable.length; i < n; i++) {
            int k = keyTable[i];
            h += Coord.rosenbergStrongHashCode(Coord.pureDecodeX(k), Coord.pureDecodeY(k));
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Set))
            return false;
        Collection<?> c = (Collection<?>) o;
        if (c.size() != size())
            return false;
        try {
            return containsAll(c);
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }

    @Override
    public <S extends CharSequence & Appendable> S appendTo(S builder, String separator, boolean brackets) {
        try {
            if (size == 0) {
                builder.append("[]");
                return builder;
            }
            builder.append('[');
            int[] keyTable = this.keyTable;
            int i = keyTable.length;
            if (hasZeroValue) {
                Coord.get(0,0).appendTo(builder, ",", true);
            } else {
                while (i-- > 0) {
                    int key = keyTable[i];
                    if (key == 0) {
                        continue;
                    }
                    Coord.decode(key).appendTo(builder, ",", true);
                    break;
                }
            }
            while (i-- > 0) {
                int key = keyTable[i];
                if (key == 0) {
                    continue;
                }
                builder.append(", ");
                Coord.decode(key).appendTo(builder, ",", true);
            }
            builder.append(']');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder;
    }

    @Override
    public String toString() {
        return toString(", ", true);
    }

    /**
     * Reduces the size of the set to the specified size. If the set is already smaller than the specified
     * size, no action is taken. This indiscriminately removes items from the backing array until the
     * requested newSize is reached, or until the full backing array has had its elements removed.
     * <br>
     * This tries to remove from the end of the iteration order, but because the iteration order is not
     * guaranteed by an unordered set, this can remove essentially any item(s) from the set if it is larger
     * than newSize.
     *
     * @param newSize the target size to try to reach by removing items, if smaller than the current size
     */
    public void truncate(int newSize) {
        int[] keyTable = this.keyTable;
        newSize = Math.max(0, newSize);
        for (int i = keyTable.length - 1; i >= 0 && size > newSize; i--) {
            if (keyTable[i] != 0) {
                keyTable[i] = 0;
                --size;
            }
        }
        if (hasZeroValue && size > newSize) {
            hasZeroValue = false;
            --size;
        }
    }

    /**
     * Returns an iterator for the keys in the set. Remove is supported.
     */
    @Override
    public Iterator<Coord> iterator() {
        return new CoordPureSetIterator(this);
    }

    @Override
    public int size() {
        return size;
    }

    public static class CoordPureSetIterator implements Iterator<Coord> {
        static private final int INDEX_ILLEGAL = -2, INDEX_ZERO = -1;

        /**
         * This can be queried in place of calling {@link #hasNext()}. The method also performs
         * a check that the iterator is valid, where using the field does not check.
         */
        public boolean hasNext;
        /**
         * The next index in the set's key table to go to and return from {@link #next()} (or,
         * while discouraged because of boxing, {@link #next()}).
         */
        protected int nextIndex;
        /**
         * The current index in the set's key table; this is the index that will be removed if
         * {@link #remove()} is called.
         */
        protected int currentIndex;
        /**
         * The set to iterate over.
         */
        protected final CoordPureSet set;

        public CoordPureSetIterator(CoordPureSet set) {
            this.set = set;
            reset();
        }

        public void reset() {
            currentIndex = INDEX_ILLEGAL;
            nextIndex = INDEX_ZERO;
            if (set.hasZeroValue) {
                hasNext = true;
            } else {
                findNextIndex();
            }
        }

        protected void findNextIndex() {
            int[] keyTable = set.keyTable;
            for (int n = keyTable.length; ++nextIndex < n; ) {
                if (keyTable[nextIndex] != 0) {
                    hasNext = true;
                    return;
                }
            }
            hasNext = false;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public void remove() {
            int i = currentIndex;
            if (i == INDEX_ZERO && set.hasZeroValue) {
                set.hasZeroValue = false;
            } else if (i < 0) {
                throw new IllegalStateException("next must be called before remove.");
            } else {
                int[] keyTable = set.keyTable;
                int mask = set.mask, next = i + 1 & mask, key;
                while ((key = keyTable[next]) != 0) {
                    int placement = set.place(key);
                    if ((next - placement & mask) > (i - placement & mask)) {
                        keyTable[i] = key;
                        i = next;
                    }
                    next = next + 1 & mask;
                }
                keyTable[i] = 0;
                if (i != currentIndex) {
                    --nextIndex;
                }
            }
            currentIndex = INDEX_ILLEGAL;
            set.size--;
        }

        @Override
        public Coord next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            int key = nextIndex == INDEX_ZERO ? 0 : set.keyTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return Coord.decode(key);
        }

        /**
         * Returns a new {@link ObjectList} of Coord containing the remaining items.
         * Does not change the position of this iterator.
         */
        public ObjectList<Coord> toList() {
            ObjectList<Coord> list = new ObjectList<Coord>(set.size);
            int currentIdx = currentIndex, nextIdx = nextIndex;
            boolean hn = hasNext;
            while (hasNext) {
                list.add(next());
            }
            currentIndex = currentIdx;
            nextIndex = nextIdx;
            hasNext = hn;
            return list;
        }

        /**
         * Append the remaining items that this can iterate through into the given Collection of Coord.
         * Does not change the position of this iterator.
         *
         * @param coll any modifiable Collection of Coord; may have items appended into it
         * @return the given primitive collection
         */
        public Collection<Coord> appendInto(Collection<Coord> coll) {
            int currentIdx = currentIndex, nextIdx = nextIndex;
            boolean hn = hasNext;
            while (hasNext) {
                coll.add(next());
            }
            currentIndex = currentIdx;
            nextIndex = nextIdx;
            hasNext = hn;
            return coll;
        }
    }

    /**
     * Constructs an empty set given the type as a generic type argument.
     * This is usually less useful than just using the constructor, but can be handy
     * in some code-generation scenarios when you don't know how many arguments you will have.
     *
     * @return a new set containing nothing
     */
    public static CoordPureSet with () {
        return new CoordPureSet(0);
    }

    /**
     * Creates a new CoordSet that holds only the given item, but can be resized.
     * @param item one Coord item
     * @return a new CoordSet that holds the given item
     */
    public static CoordPureSet with (Coord item) {
        CoordPureSet set = new CoordPureSet(1);
        set.add(item);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordPureSet with (Coord item0, Coord item1) {
        CoordPureSet set = new CoordPureSet(2);
        set.add(item0, item1);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordPureSet with (Coord item0, Coord item1, Coord item2) {
        CoordPureSet set = new CoordPureSet(3);
        set.add(item0, item1, item2);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordPureSet with (Coord item0, Coord item1, Coord item2, Coord item3) {
        CoordPureSet set = new CoordPureSet(4);
        set.add(item0, item1, item2, item3);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordPureSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4) {
        CoordPureSet set = new CoordPureSet(5);
        set.add(item0, item1, item2, item3);
        set.add(item4);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordPureSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5) {
        CoordPureSet set = new CoordPureSet(6);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @param item6 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordPureSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5, Coord item6) {
        CoordPureSet set = new CoordPureSet(7);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5, item6);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @param item6 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordPureSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5, Coord item6, Coord item7) {
        CoordPureSet set = new CoordPureSet(8);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5, item6, item7);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * This overload will only be used when an array is supplied and the type of the
     * items requested is the component type of the array, or if varargs are used and
     * there are 9 or more arguments.
     * @param varargs a Coord varargs or Coord array; remember that varargs allocate
     * @return a new CoordSet that holds the given items
     */
    public static CoordPureSet with (Coord... varargs) {
        return new CoordPureSet(varargs);
    }
}
