package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.support.LaserRandom;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Meant to take a fixed-size set of items and produce a shuffled stream of them such that an element is never chosen in
 * quick succession; that is, there should always be a gap between the same item's occurrences. This is an Iterable of
 * T, not a Collection, because it can iterate without stopping, infinitely, unless you break out of a foreach loop that
 * iterates through one of these, or call the iterator's next() method only a limited number of times. Collections have
 * a size that can be checked, but Iterables can be infinite (and in this case, this one is).
 * Created by Tommy Ettinger on 5/21/2016.
 * @param <T> the type of items to iterate over; ideally, the items are unique
 */
public class GapShuffler<T> implements Iterator<T>, Iterable<T>, Serializable {
    private static final long serialVersionUID = 1L;
    public LaserRandom random;
    protected ObjectList<T> elements;
    protected int index;
    protected GapShuffler() {
        random = new LaserRandom();
        elements = new ObjectList<>();
        index = 0;
    }

    public GapShuffler(T single)
    {
        random = new LaserRandom();
        elements = new ObjectList<>(1);
        elements.add(single);
        index = 0;
    }
    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded LaserRandom, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(Collection<T> elements)
    {
        this(elements, new LaserRandom());

    }

    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded LaserRandom, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(Collection<T> elements, String seed)
    {
        this(elements, new LaserRandom(Hasher.gaap.hash64(seed), Hasher.furfur.hash64(seed)));
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given LaserRandom, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in items is unique, but that is not necessary and does not affect how this works. The
     * random parameter is copied so externally using it won't change the order this produces its values; the
     * random field is used whenever the iterator needs to re-shuffle the internal ordering of items.
     * @param items a Collection of T that will not be modified
     * @param random a LaserRandom that can be pre-seeded; will be copied and not used directly
     */
    public GapShuffler(Collection<T> items, LaserRandom random)
    {
        this.random = random.copy();
        elements = new ObjectList<>(items);
        elements.shuffle(this.random);
        index = 0;
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given LaserRandom, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in items is unique, but that is not necessary and does not affect how this works. The
     * random parameter will be copied if {@code shareRNG} is true, otherwise the reference will be shared (which could
     * make the results of this GapShuffler depend on outside code, though it will always maintain a gap between
     * identical elements if the elements are unique).
     * @param items a Collection of T that will not be modified
     * @param random a LaserRandom that can be pre-seeded; will be copied and not used directly
     * @param shareRNG if false, {@code random} will be copied and no reference will be kept; if true, {@code random} will be shared with the outside code
     */
    public GapShuffler(Collection<T> items, LaserRandom random, boolean shareRNG)
    {
        this.random = shareRNG ? random : random.copy();
        elements = new ObjectList<>(items);
        elements.shuffle(this.random);
        index = 0;
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded LaserRandom, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(T[] elements)
    {
        this(elements, new LaserRandom());
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded LaserRandom, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(T[] elements, CharSequence seed)
    {
        this(elements, new LaserRandom(Hasher.gaap.hash64(seed), Hasher.furfur.hash64(seed)));
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given LaserRandom, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in items is unique, but that is not necessary and does not affect how this works. The
     * random parameter is copied so externally using it won't change the order this produces its values; the random field is
     * used whenever the iterator needs to re-shuffle the internal ordering of items.
     * @param items a Collection of T that will not be modified
     * @param random a LaserRandom that can be pre-seeded; will be copied and not used directly
     */
    public GapShuffler(T[] items, LaserRandom random)
    {
        this.random = random.copy();
        elements = ObjectList.with(items);
        elements.shuffle(this.random);
        index = 0;
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given LaserRandom, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have at least one "gap" element between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in items is unique, but that is not necessary and does not affect how this works. The
     * random parameter will be copied if {@code shareRNG} is false, otherwise the reference will be shared (which could
     * make the results of this GapShuffler depend on outside code, though it will always maintain a gap between
     * identical elements if the elements are unique).
     * @param items a Collection of T that will not be modified
     * @param random a LaserRandom that can be pre-seeded; will be copied and not used directly
     * @param shareRNG if false, {@code random} will be copied and no reference will be kept; if true, {@code random} will be shared with the outside code
     */
    public GapShuffler(T[] items, LaserRandom random, boolean shareRNG)
    {
        this.random = shareRNG ? random : random.copy();
        elements = ObjectList.with(items);
        elements.shuffle(this.random);
        index = 0;
    }

    /**
     * Gets the next element of the infinite sequence of T this shuffles through. This class can be used as an
     * Iterator or Iterable of type T.
     * @return the next element in the infinite sequence
     */
    public T next() {
        int size = elements.size();
        if(size == 1)
        {
            return elements.get(0);
        }
        if(index >= size)
        {
            final int n = size - 1;
            for (int i = n; i > 1; i--) {
                Collections.swap(elements, random.nextInt(i), i - 1);
            }
            Collections.swap(elements, 1 + random.nextInt(n), n);
            index = 0;
        }
        return elements.get(index++);
    }
    /**
     * Returns {@code true} if the iteration has more elements.
     * This is always the case for GapShuffler.
     *
     * @return {@code true} always
     */
    @Override
    public boolean hasNext() {
        return true;
    }
    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always throws this exception
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported");
    }

    /**
     * Returns an <b>infinite</b> iterator over elements of type {@code T}; the returned iterator is this object.
     * You should be prepared to break out of any for loops that use this once you've gotten enough elements!
     * The remove() method is not supported by this iterator and hasNext() will always return true.
     *
     * @return an infinite Iterator over elements of type T.
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    public LaserRandom getRNG() {
        return random;
    }


    /**
     * Sets the LaserRandom this uses to shuffle the order of elements, always copying the given LaserRandom before using it. Always
     * reshuffles the order, which may eliminate a gap that should have been present, so treat the sequence before and
     * after like separate GapShuffler objects.
     * @param random a LaserRandom, which can have been seeded; always copied
     */
    public void setRNG(LaserRandom random) {
        setRNG(random, false);
    }

    /**
     * Sets the LaserRandom this uses to shuffle the order of elements, optionally sharing a reference between outside code and
     * the internal LaserRandom (when {@code shareRNG} is true). Always reshuffles the order, which may eliminate a gap that
     * should have been present, so treat the sequence before and after like separate GapShuffler objects.
     * @param random a LaserRandom, which can have been seeded; optionally copied
     * @param shareRNG if false, {@code random} will be copied and no reference will be kept; if true, {@code random} will be shared with the outside code
     */
    public void setRNG(LaserRandom random, boolean shareRNG) {
        this.random = shareRNG ? random : random.copy();
        elements.shuffle(this.random);
    }

    /**
     * The internal items used here are protected, but you can still use this method to put a shallow copy of them into
     * some other Collection. If the type {@code T} is mutable, changes to individual items will be reflected in this
     * GapShuffler, so be careful in that case (if T is immutable, like if it is String, then there's nothing to need to
     * be careful about). This copies each item in the GapShuffler's sequence once, in no particular order, but it may
     * give a prediction of what items this will return in the future (or has already returned).
     * @param coll a Collection that will have each of the possible items this can produce added into it, in no particular order
     */
    public void fillInto(Collection<T> coll) {
        coll.addAll(elements);
    }
    
}
