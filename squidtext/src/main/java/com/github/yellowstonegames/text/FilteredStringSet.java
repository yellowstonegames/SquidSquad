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

package com.github.yellowstonegames.text;

import com.github.tommyettinger.ds.ObjectSet;
import com.github.tommyettinger.ds.Utilities;
import com.github.tommyettinger.function.CharPredicate;
import com.github.tommyettinger.function.CharToCharFunction;
import com.github.yellowstonegames.core.annotations.Beta;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * A customizable variant on ObjectSet that always uses String keys, but only considers any character in an item (for
 * equality and hashing purposes) if that character satisfies a predicate. This can also edit the characters that pass
 * the filter, such as by changing their case during comparisons (and hashing).
 */
@Beta
public class FilteredStringSet extends ObjectSet<String> {
	protected CharPredicate 		filter = c -> true;
	protected CharToCharFunction 	editor = c -> c;
	/**
	 * Creates a new set with an initial capacity of 51 and a load factor of {@link Utilities#getDefaultLoadFactor()}.
	 * This considers all characters in a String key and does not edit them.
	 */
	public FilteredStringSet () {
		super();
	}

	/**
	 * Creates a new set with the specified initial capacity a load factor of {@link Utilities#getDefaultLoadFactor()}.
	 * This set will hold initialCapacity items before growing the backing table.
	 * This considers all characters in a String key and does not edit them.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public FilteredStringSet (int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity items before
	 * growing the backing table.
	 * This considers all characters in a String key and does not edit them.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 * @param loadFactor      what fraction of the capacity can be filled before this has to resize; 0 &lt; loadFactor &lt;= 1
	 */
	public FilteredStringSet (int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Creates a new set identical to the specified set.
	 *
	 * @param set another FilteredStringSet to copy
	 */
	public FilteredStringSet (FilteredStringSet set) {
		filter = set.filter;
		editor = set.editor;
		loadFactor = set.loadFactor;
		threshold = set.threshold;
		mask = set.mask;
		shift = set.shift;
		keyTable = Arrays.copyOf(set.keyTable, set.keyTable.length);
		size = set.size;
		hashMultiplier = set.hashMultiplier;
	}

	/**
	 * Creates a new set that contains all distinct elements in {@code coll}.
	 * This considers all characters in a String key and does not edit them.
	 *
	 * @param coll a Collection implementation to copy, such as an ObjectList or a Set that doesn't subclass ObjectSet
	 */
	public FilteredStringSet (Collection<? extends String> coll) {
		super(coll);
	}

	/**
	 * Creates a new set using {@code length} items from the given {@code array}, starting at {@code} offset (inclusive).
	 * This takes a String array, not a String array, though Strings can be put into a String array (along with
	 * StringBuilders and similar Strings).
	 * This considers all characters in a String key and does not edit them.
	 *
	 * @param array  an array to draw items from
	 * @param offset the first index in array to draw an item from
	 * @param length how many items to take from array; bounds-checking is the responsibility of the using code
	 */
	public FilteredStringSet (String[] array, int offset, int length) {
		super(array, offset, length);
	}

	/**
	 * Creates a new set containing all of the items in the given array.
	 * This takes a String array, not a String array, though Strings can be put into a String array (along with
	 * StringBuilders and similar Strings).
	 * This considers all characters in a String key and does not edit them.
	 *
	 * @param array an array that will be used in full, except for duplicate items
	 */
	public FilteredStringSet (String[] array) {
		super(array);
	}

	public CharPredicate getFilter() {
		return filter;
	}

	public FilteredStringSet setFilter(CharPredicate filter) {
		this.filter = filter;
		return this;
	}

	public CharToCharFunction getEditor() {
		return editor;
	}

	public FilteredStringSet setEditor(CharToCharFunction editor) {
		this.editor = editor;
		return this;
	}

	public FilteredStringSet setModifiers(CharPredicate filter, CharToCharFunction editor) {
		this.filter = filter;
		this.editor = editor;
		return this;
	}

	protected long hashHelper(String s) {
		long hash = 0x9E3779B97F4A7C15L + hashMultiplier; // golden ratio
		for (int i = 0, len = s.length(); i < len; i++) {
			char c = s.charAt(i);
			if(filter.test(c)){
				hash = (hash + editor.applyAsChar(c)) * hashMultiplier;
			}
		}
		return hash;
	}
	@Override
	protected int place (Object item) {
		if (item instanceof String) {
			return (int)(hashHelper((String) item) >>> shift);
		}
		return super.place(item);
	}

	@Override
	protected boolean equate (Object left, @Nullable Object right) {
		if (left == right)
			return true;
		if(right == null) return false;
		if ((left instanceof String) && (right instanceof String)) {
			String l = (String)left, r = (String)right;
			int llen = l.length(), rlen = r.length();
			int cl = -1, cr = -1;
			int i = 0, j = 0;
			while (i < llen || j < rlen) {
				if(i == llen) cl = -1;
				else {
					while (i < llen && !filter.test((char) (cl = l.charAt(i++)))) {
						cl = -1;
					}
				}
				if(j == rlen) cr = -1;
				else {
					while (j < rlen && !filter.test((char) (cr = r.charAt(j++)))) {
						cr = -1;
					}
				}
				if(cl != cr && editor.applyAsChar((char)cl) != editor.applyAsChar((char)cr))
					return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode () {
		int h = size;
		String[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			String key = keyTable[i];
			if (key != null) {h += hashHelper(key);}
		}
		return h;
	}

	public static FilteredStringSet with (CharPredicate filter, CharToCharFunction editor, String item) {
		FilteredStringSet set = new FilteredStringSet(1).setModifiers(filter, editor);
		set.add(item);
		return set;
	}

	public static FilteredStringSet with (CharPredicate filter, CharToCharFunction editor, String... array) {
		FilteredStringSet set = new FilteredStringSet(array.length).setModifiers(filter, editor);
		set.addAll(array);
		return set;
	}

}
