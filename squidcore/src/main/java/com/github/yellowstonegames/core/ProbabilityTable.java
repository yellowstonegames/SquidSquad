package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.*;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.tommyettinger.ds.support.LaserRandom;

/**
 * A generic method of holding a probability table to determine weighted random
 * outcomes.
 * <br>
 * The weights do not need to add up to any particular value; they will be
 * normalized when choosing a random entry. This class allows adding {@code T} items and the weights for
 * those items after the ProbabilityTable has been constructed with {@link #add(Object, int)} or
 * {@link #addAll(ObjectIntMap)} , as well as removing items entirely with {@link #remove(Object)} or
 * adjusting the weight for an existing item with {@link #add(Object, int)} or {@link #remove(Object, int)}.
 * You can also add a nested ProbabilityTable, which has its own weight and can be chosen like any other
 * item, except it makes its own random choice of its own {@code T} items; you can use the nested table
 * with {@link #add(ProbabilityTable, int)} and {@link #addAllNested(ObjectIntMap)}. Actually getting a
 * randomly-selected item is easy; just use {@link #random()}.
 * 
 * @see WeightedTable An alternative for when you want to track the items separately from their weights, and don't need nested tables.
 * 
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * 
 * @param <T> The type of object to be held in the table
 */
public class ProbabilityTable<T> {
    /**
     * The set of items that can be produced directly from {@link #random()} (without additional lookups).
     */
    public final NumberedSet<T> table;
    /**
     * The list of items that can be produced indirectly from {@link #random()} (looking up values from inside
     * the nested tables).
     */
    public final ObjectList<ProbabilityTable<T>> extraTable;
    public final IntList weights;
    public EnhancedRandom rng;
    protected int total, normalTotal;

    /**
     * Creates a new probability table with a random seed.
     */
    public ProbabilityTable() {
        this(new LaserRandom());
    }

    /**
     * Creates a new probability table with the provided source of randomness
     * used.
     *
     * @param rng the source of randomness
     */
    public ProbabilityTable(EnhancedRandom rng) {
        this.rng = rng == null ? new LaserRandom() : rng;
        table = new NumberedSet<>(64, 0.5f);
        extraTable = new ObjectList<>(16);
        weights = new IntList(64);
        total = 0;
        normalTotal = 0;
    }

    /**
     * Creates a new probability table with the provided long seed used.
     *
     * @param seed the RNG seed as a long
     */
    public ProbabilityTable(long seed) {
        this(new LaserRandom(seed));
    }

    /**
     * Creates a new probability table with the provided String seed used.
     *
     * @param seed the RNG seed as a String
     */
    public ProbabilityTable(String seed) {
        this(new LaserRandom(Hasher.purson.hash64(seed), Hasher.purson_.hash64(seed)));
    }

    /**
     * Returns an object randomly based on assigned weights.
     *
     * Returns null if no elements have been put in the table.
     *
     * @return the chosen object or null
     */
    public T random() {
        if (table.isEmpty() && extraTable.isEmpty()) {
            return null;
        }
        int index = (int) ((total * ((long)rng.next(31))) >>> 31), sz = table.size();
        for (int i = 0; i < sz; i++) {
            index -= weights.get(i);
            if (index < 0)
                return table.getAt(i);
        }
        for (int i = 0; i < extraTable.size(); i++) {
            index -= weights.get(sz + i);
            if(index < 0)
                return extraTable.get(i).random();
        }
        return null;//something went wrong, shouldn't have been able to get all the way through without finding an item
    }

    /**
     * Adds the given item to the table.
     *
     * Weight must be greater than 0.
     *
     * @param item the object to be added
     * @param weight the weight to be given to the added object
     * @return this for chaining
     */
    public ProbabilityTable<T> add(T item, int weight) {
        if(weight <= 0)
            return this;
        int i = table.indexOf(item);
        if (i < 0) {
            weights.insert(table.size(), weight);
            table.add(item);
            total += weight;
            normalTotal += weight;
        } else {
            int i2 = weights.get(i);
            int w = Math.max(0, i2 + weight);
            weights.set(i, w);
            total += w - i2;
            normalTotal += w - i2;
        }
        return this;
    }

    /**
     * Given an ObjectIntMap of T element keys and int weight values, adds all T keys with their corresponding weights
     * into this ProbabilityTable. You may want to use {@link ObjectIntMap#with(Object, Number, Object...)} to produce
     * the parameter, unless you already have one. You may also want to use {@link ObjectIntOrderedMap} to preserve the
     * iteration order of items in this ProbabilityTable.
     * @param itemsAndWeights an ObjectIntMap of T keys to int values, where a key will be an item this can retrieve
     *                        and a value will be its weight
     * @return this for chaining
     */
    public ProbabilityTable<T> addAll(ObjectIntMap<T> itemsAndWeights)
    {
        if(itemsAndWeights == null) return this;
        for(ObjectIntMap.Entry<T> ent : itemsAndWeights){
            add(ent.key, ent.value);
        }
        return this;
    }

    /**
     * Removes the possibility of generating the given T item, except by nested ProbabilityTable results.
     * Returns true iff the item was removed.
     * @param item the item to make less likely or impossible
     * @return true if the probability changed or false if nothing changed
     */
    public boolean remove(T item)
    {
        return remove(item, weight(item));
    }

    /**
     * Reduces the likelihood of generating the given T item by the given weight, which can reduce the chance below 0
     * and thus remove the item entirely. Does not affect nested ProbabilityTables. The value for weight must be greater
     * than 0, otherwise this simply returns false without doing anything. Returns true iff the probabilities changed.
     * @param item the item to make less likely or impossible
     * @param weight how much to reduce the item's weight by, as a positive non-zero int (greater values here subtract
     *               more from the item's weight)
     * @return true if the probability changed or false if nothing changed
     */
    public boolean remove(T item, int weight)
    {
        if(weight <= 0)
            return false;
        int idx = table.indexOf(item);
        if(idx < 0)
            return false;
        int o = weights.get(idx);
        weights.minus(idx, weight);
        int w = weights.get(idx);
        if(w <= 0)
        {
            table.removeAt(idx);
            weights.removeAt(idx);
        }
        w = Math.min(o, o - w);
        total -= w;
        normalTotal -= w;
        return true;
    }

    /**
     * Given an Iterable of T item keys to remove, this tries to remove each item in items, though it can't affect items
     * in nested ProbabilityTables, and returns true if any probabilities were changed.
     * @param items an Iterable of T items that will all be removed from the normal (non-nested) items in this
     * @return true if the probabilities changed, or false otherwise
     */
    public boolean removeAll(Iterable<T> items)
    {
        boolean changed = false;
        for(T t : items)
        {
            changed |= remove(t);
        }
        return changed;
    }

    /**
     * Given an ObjectIntMap of T item keys and int weight values, reduces the weights in this ProbabilityTable for
     * all T keys by their corresponding weights, removing them if the weight becomes 0 or less. You may want to use
     * {@link ObjectIntMap#with(Object, Number, Object...)} to produce the parameter, unless you already have one.
     * Returns true iff the probabilities changed.
     * @param itemsAndWeights an OrderedMap of T keys to Integer values, where a key will be an item that should be
     *                        reduced in weight or removed and a value will be that item's weight
     * @return true if the probabilities changed or false otherwise
     */
    public boolean removeAll(ObjectIntMap<T> itemsAndWeights)
    {
        if(itemsAndWeights == null) return false;
        boolean changed = false;
        for(ObjectIntMap.Entry<T> ent : itemsAndWeights){
            changed |= remove(ent.key, ent.value);
        }
        return changed;
    }

    /**
     * Adds the given probability table as a possible set of results for this table.
     * The table parameter should not be the same object as this ProbabilityTable, nor should it contain cycles
     * that could reference this object from inside the values of table. This could cause serious issues that would
     * eventually terminate in a StackOverflowError if the cycles randomly repeated for too long. Only the first case
     * is checked for (if the contents of this and table are equivalent, it returns without doing anything; this also
     * happens if table is empty or null).
     *
     * Weight must be greater than 0.
     *
     * @param table the ProbabilityTable to be added; should not be the same as this object (avoid cycles)
     * @param weight the weight to be given to the added table
     * @return this for chaining
     */
    public ProbabilityTable<T> add(ProbabilityTable<T> table, int weight) {
        if(weight <= 0 || table == null || contentEquals(table) || table.total <= 0)
            return this;
        weights.add(weight);
        extraTable.add(table);
        total += weight;
        return this;
    }

    /**
     * Given an OrderedMap of ProbabilityTable keys and Integer weight values, adds all keys as nested tables with their
     * corresponding weights into this ProbabilityTable. All ProbabilityTable keys should have the same T type as this
     * ProbabilityTable. You may want to use {@link ObjectIntMap#with(Object, Number, Object...)} to produce the
     * parameter, unless you already have one.
     * <br>
     * The same rules apply to this as apply to {@link #add(ProbabilityTable, int)}; that is, no key in itemsAndWeights
     * can be the same object as this ProbabilityTable, nor should any key contain cycles that could reference this
     * object from inside the values of a key. This could cause serious issues that would eventually terminate in a
     * StackOverflowError if the cycles randomly repeated for too long. Only the first case is checked for (if the
     * contents of this and a key are equivalent, it ignores that key; this also
     * happens if a key is empty or null).

     * @param itemsAndWeights an ObjectIntMap of T keys to int values, where a key will be an item this can retrieve
     *                        and a value will be its weight
     * @return this for chaining
     */
    public ProbabilityTable<T> addAllNested(ObjectIntMap<ProbabilityTable<T>> itemsAndWeights)
    {
        if(itemsAndWeights == null) return this;
        for(ObjectIntMap.Entry<ProbabilityTable<T>> ent : itemsAndWeights){
            add(ent.key, ent.value);
        }
        return this;
    }

    /**
     * Returns the weight of the item if the item is in the table. Returns zero
     * if the item is not in the table.
     *
     * @param item the item searched for
     * @return the weight of the item, or zero
     */
    public int weight(T item) {
        int i = table.indexOf(item);
        return i < 0 ? 0 : weights.get(i);
    }

    /**
     * Returns the weight of the extra table if present. Returns zero
     * if the extra table is not present.
     *
     * @param item the extra ProbabilityTable to search for
     * @return the weight of the ProbabilityTable, or zero
     */
    public int weight(ProbabilityTable<T> item) {
        int i = extraTable.indexOf(item);
        return i < 0 ? 0 : weights.get(i + table.size());
    }

    /**
     * Provides a set of the items in this table, without reference to their
     * weight. Includes nested ProbabilityTable values, but as is the case throughout
     * this class, cyclical references to ProbabilityTable values that reference this
     * table will result in significant issues (such as a {@link StackOverflowError}
     * crashing your program).
     *
     * @return an ObjectOrderedSet of all items stored; iteration order should be predictable
     */
    public ObjectOrderedSet<T> items() {
        ObjectOrderedSet<T> os = new ObjectOrderedSet<>(table);
        for (int i = 0; i < extraTable.size(); i++) {
            os.addAll(extraTable.get(i).items());
        }
        return os;
    }

    /**
     * Provides a set of the items in this table that are not in nested tables, without
     * reference to their weight. These are the items that are simple to access, hence
     * the name. If you want the items that are in both the top-level and nested tables,
     * you can use {@link #items()}. Note that this returns a direct reference to the
     * simple items in this ProbabilityTable, so you generally shouldn't modify the
     * returned Set.
     * @return a predictably-ordered set of the items in the top-level table
     */
    public NumberedSet<T> simpleItems()
    {
        return table;
    }

    /**
     * Provides a set of the nested ProbabilityTable values in this table, without reference
     * to their weight. Does not include normal values (non-table); for that, use items().
     *
     * @return a "sorted" set of all nested tables stored, really sorted in insertion order
     */
    public ObjectList<ProbabilityTable<T>> tables() {
        return extraTable;
    }

    /**
     * Sets the current random number generator to the given EnhancedRandom.
     * @param random an EnhancedRandom, typically with a seed you want control over; for instance, a {@link LaserRandom}
     */
    public void setRandom(EnhancedRandom random)
    {
        if(random != null)
            rng = random;
    }

    /**
     * Gets the random number generator (an EnhancedRandom) this uses.
     * @return the EnhancedRandom used by this class
     */
    public EnhancedRandom getRandom()
    {
        return rng;
    }

    /**
     * Copies this ProbabilityTable so nothing in the copy is shared with the original, except for the T items (which
     * might not be possible to copy). The RNG is also copied.
     * @return a copy of this ProbabilityTable; no references should be shared except for T items
     */
    public ProbabilityTable<T> copy()
    {
        ProbabilityTable<T> n = new ProbabilityTable<>(rng.copy());
        n.weights.addAll(weights);
        n.table.addAll(table);
        for (int i = 0; i < extraTable.size(); i++) {
            n.extraTable.add(extraTable.get(i).copy());
        }
        n.total = total;
        n.normalTotal = normalTotal;
        return n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProbabilityTable<?> that = (ProbabilityTable<?>) o;

        if (!table.equals(that.table)) return false;
        if (!extraTable.equals(that.extraTable)) return false;
        return weights.equals(that.weights);
    }

    /**
     * Can avoid some checks that {@link #equals(Object)} needs because this always takes a ProbabilityTable.
     * @param o another ProbabilityTable
     * @return true if both ProbabilityTables are equivalent in contents and likelihoods, not necessarily random state
     */
    public boolean contentEquals(ProbabilityTable<T> o) {
        if (this == o) return true;
        if (o == null) return false;

        if (!table.equals(o.table)) return false;
        if (!extraTable.equals(o.extraTable)) return false;
        return weights.equals(o.weights);
    }

    @Override
    public int hashCode() {
        int result = table.hashCode();
        result = 421 * result + extraTable.hashCode();
        result = 83 * result + weights.hashCode();
        return result;
    }
}
