package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.*;

/**
 * Utility methods for more easily constructing data structures, particularly those in jdkgdxds.
 * All static methods; meant to be imported with {@code import static squidpony.Maker.*}.
 * Constructing generic-typed maps here is not type-safe.
 */
public class Maker {
    /**
     * Makes an ObjectObjectOrderedMap with key and value types inferred from the types of k0 and v0, and considers all
     * parameters key-value pairs, casting the Objects at positions 0, 2, 4... etc. to K and the objects at positions
     * 1, 3, 5... etc. to V. If rest has an odd-number length, then it discards the last item. If any pair of items in
     * rest cannot be cast to the correct type of K or V, then this inserts nothing for that pair. You can pass an array
     * containing keys and values for rest, but k0 and v0 can't be inside that array.
     * @param k0 the first key; used to infer the types of other keys if generic parameters aren't specified.
     * @param v0 the first value; used to infer the types of other values if generic parameters aren't specified.
     * @param rest an array or vararg of keys and values in pairs; should contain alternating K, V, K, V... elements
     * @param <K> the type of keys in the returned ObjectObjectOrderedMap; if not specified, will be inferred from k0
     * @param <V> the type of values in the returned ObjectObjectOrderedMap; if not specified, will be inferred from v0
     * @return a freshly-made ObjectObjectOrderedMap with K keys and V values, using k0, v0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ObjectObjectOrderedMap<K, V> orderedMap(K k0, V v0, Object... rest)
    {
        if(rest == null || rest.length == 0)
        {
            ObjectObjectOrderedMap<K, V> map = new ObjectObjectOrderedMap<>(2);
            if(k0 != null)
                map.put(k0, v0);
            return map;
        }
        ObjectObjectOrderedMap<K, V> map = new ObjectObjectOrderedMap<>(1 + (rest.length >>> 1));
        map.put(k0, v0);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                if(rest[i] != null) 
                    map.put((K) rest[i], (V) rest[i + 1]);
            }catch (ClassCastException ignored) {
            }
        }
        return map;
    }

    /**
     * Makes an empty ObjectObjectOrderedMap; needs key and value types to be specified in order to work. For an empty
     * ObjectObjectOrderedMap with String keys and Coord values, you could use {@code Maker.<String, Coord>orderedMap();}.
     * Using the new keyword is probably just as easy in this case; this method is provided for completeness relative to
     * orderedMap() with 2 or more parameters.
     * @param <K> the type of keys in the returned ObjectObjectOrderedMap; cannot be inferred and must be specified
     * @param <V> the type of values in the returned ObjectObjectOrderedMap; cannot be inferred and must be specified
     * @return an empty ObjectObjectOrderedMap with the given key and value types.
     */
    public static <K, V> ObjectObjectOrderedMap<K, V> orderedMap()
    {
        return new ObjectObjectOrderedMap<>();
    }
    /**
     * Makes an ObjectObjectMap with key and value types inferred from the types of k0 and v0, and considers all
     * parameters key-value pairs, casting the Objects at positions 0, 2, 4... etc. to K and the objects at positions
     * 1, 3, 5... etc. to V. If rest has an odd-number length, then it discards the last item. If any pair of items in
     * rest cannot be cast to the correct type of K or V, then this inserts nothing for that pair. You can pass an
     * array containing keys and values for rest, but k0 and v0 can't be inside that array.
     * @param k0 the first key; used to infer the types of other keys if generic parameters aren't specified.
     * @param v0 the first value; used to infer the types of other values if generic parameters aren't specified.
     * @param rest an array or vararg of keys and values in pairs; should contain alternating K, V, K, V... elements
     * @param <K> the type of keys in the returned ObjectObjectMap; if not specified, will be inferred from k0
     * @param <V> the type of values in the returned ObjectObjectMap; if not specified, will be inferred from v0
     * @return a freshly-made ObjectObjectMap with K keys and V values, using k0, v0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ObjectObjectMap<K, V> map(K k0, V v0, Object... rest)
    {
        if(rest == null || rest.length == 0)
        {
            ObjectObjectMap<K, V> map = new ObjectObjectMap<>(2);
            if(k0 != null)
                map.put(k0, v0);
            return map;
        }
        ObjectObjectMap<K, V> map = new ObjectObjectMap<>(1 + (rest.length >>> 1));
        map.put(k0, v0);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                if(rest[i] != null)
                    map.put((K) rest[i], (V) rest[i + 1]);
            }catch (ClassCastException ignored) {
            }
        }
        return map;
    }

    /**
     * Makes an empty ObjectObjectMap; needs key and value types to be specified in order to work. For an empty
     * ObjectObjectMap with String keys and Coord values, you could use {@code Maker.<String, Coord>map();}. Using
     * the new keyword is probably just as easy in this case; this method is provided for completeness relative to
     * map() with 2 or more parameters.
     * @param <K> the type of keys in the returned ObjectObjectMap; cannot be inferred and must be specified
     * @param <V> the type of values in the returned ObjectObjectMap; cannot be inferred and must be specified
     * @return an empty ObjectObjectMap with the given key and value types.
     */
    public static <K, V> ObjectObjectMap<K, V> map()
    {
        return new ObjectObjectMap<>();
    }

    /**
     * Makes an ObjectList of T given an array or vararg of T elements.
     * @param elements an array or vararg of T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated ObjectList containing all of elements, in order
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectList<T> list (T... elements) {
        if(elements == null) return null;
        return new ObjectList<>(elements);
    }

    /**
     * Makes an ObjectList of T given a single T element; avoids creating an array for varargs as
     * {@link #list(Object[])} would do, but only allows one item.
     * @param element a T item; may be null
     * @param <T> just about any non-primitive, non-array type (arrays would cause confusion with the vararg method)
     * @return a newly-allocated ObjectList containing only element
     */
    public static <T> ObjectList<T> list (T element) {
        ObjectList<T> list = new ObjectList<>(1);
        list.add(element);
        return list;
    }

    /**
     * Makes an ObjectOrderedSet of T given an array or vararg of T elements. Duplicate items in elements will have
     * all but one item discarded, using the later item in elements.
     * @param elements an array or vararg of T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated ObjectOrderedSet containing all of the non-duplicate items in elements, in order
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectOrderedSet<T> orderedSet(T... elements) {
        if(elements == null) return null;
        return ObjectOrderedSet.with(elements);
    }

    /**
     * Makes an ObjectOrderedSet of T given a single T element.
     * @param element a single T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated ObjectOrderedSet containing only {@code element}
     */
    public static <T> ObjectOrderedSet<T> orderedSet(T element) {
        ObjectOrderedSet<T> set = new ObjectOrderedSet<T>(1);
        set.add(element);
        return set;
    }

    /**
     * Makes an ObjectSet of T given an array or vararg of T elements. Duplicate items in elements will have
     * all but one item discarded, using the later item in elements.
     * @param elements an array or vararg of T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated ObjectSet containing all of the non-duplicate items in elements, in order
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectSet<T> set(T... elements) {
        if(elements == null) return null;
        return ObjectSet.with(elements);
    }

    /**
     * Makes an ObjectSet of T given a single T element.
     * @param element a single T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated ObjectSet containing only {@code element}
     */
    public static <T> ObjectSet<T> set(T element) {
        ObjectSet<T> set = new ObjectSet<T>(1);
        set.add(element);
        return set;
    }
    /**
     * Makes an CaseInsensitiveOrderedMap with the value type inferred from the type of v0, and considers all parameters
     * key-value pairs, casting the Objects at positions 0, 2, 4... etc. to CharSequence and the objects at positions
     * 1, 3, 5... etc. to V. If rest has an odd-number length, then it discards the last item. If any pair of items in
     * rest cannot be cast to the correct type of CharSequence or V (such as if the key is null), then this inserts
     * nothing for that pair. You can pass an array containing keys and values for rest, but k0 and v0 can't be inside
     * that array.
     * @param k0 the first key; this can be any implementation of CharSequence, and is typically a String
     * @param v0 the first value; used to infer the types of other values if generic parameters aren't specified
     * @param rest an array or vararg of keys and values in pairs; should contain alternating CharSequence, V, CharSequence, V... elements
     * @param <V> the type of values in the returned CaseInsensitiveOrderedMap; if not specified, will be inferred from v0
     * @return a freshly-made CaseInsensitiveOrderedMap with V values, using k0, v0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <V> CaseInsensitiveOrderedMap<V> caseInsensitiveOrderedMap(CharSequence k0, V v0, Object... rest)
    {
        if(rest == null || rest.length == 0)
        {
            CaseInsensitiveOrderedMap<V> map = new CaseInsensitiveOrderedMap<>(2);
            if(k0 != null)
                map.put(k0, v0);
            return map;
        }
        CaseInsensitiveOrderedMap<V> map = new CaseInsensitiveOrderedMap<>(1 + (rest.length >>> 1));
        map.put(k0, v0);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                if(rest[i] != null)
                    map.put((CharSequence) rest[i], (V) rest[i + 1]);
            }catch (ClassCastException ignored) {
            }
        }
        return map;
    }

    /**
     * Makes an empty CaseInsensitiveOrderedMap; needs the value type to be specified in order to work. For an empty
     * CaseInsensitiveOrderedMap with Coord values, you could use {@code Maker.<Coord>orderedMap();}.
     * Using the new keyword is probably just as easy in this case; this method is provided for completeness relative to
     * caseInsensitiveOrderedMap() with 2 or more parameters.
     * @param <V> the type of values in the returned CaseInsensitiveOrderedMap; cannot be inferred and must be specified
     * @return an empty CaseInsensitiveOrderedMap with the given key and value types.
     */
    public static <V> CaseInsensitiveOrderedMap<V> caseInsensitiveOrderedMap()
    {
        return new CaseInsensitiveOrderedMap<>();
    }

}
