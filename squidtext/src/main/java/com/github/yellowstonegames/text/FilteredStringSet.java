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

import com.github.tommyettinger.ds.CaseInsensitiveSet;
import com.github.tommyettinger.ds.CharList;
import com.github.tommyettinger.ds.ObjectSet;
import com.github.tommyettinger.ds.Utilities;
import com.github.tommyettinger.function.CharPredicate;
import com.github.tommyettinger.function.CharToCharFunction;
import com.github.yellowstonegames.core.annotations.Beta;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * A customizable variant on ObjectSet that always uses String keys, but only considers any character in an item (for
 * equality and hashing purposes) if that character satisfies a predicate. This can also edit the characters that pass
 * the filter, such as by changing their case during comparisons (and hashing). You will usually want to call
 * {@link #setFilter(CharPredicate)} and/or {@link #setEditor(CharToCharFunction)} to change the behavior of hashing and
 * equality before you enter any items, unless you have specified the filter and/or editor you want in the constructor.
 * <br>
 * You can use this class as a replacement for {@link CaseInsensitiveSet} if you set the editor to a method reference to
 * {@link Character#toUpperCase(char)}. You can go further by setting the editor to make the hashing and equality checks
 * ignore characters that don't satisfy a predicate, such as {@link Character#isLetter(char)}.
 * <br>
 * Be advised that if you use some (most) checks in {@link Character} for properties of a char, and you try to use them
 * on GWT, those checks will not work as expected for non-ASCII characters. Some other platforms might also be affected,
 * such as TeaVM, but it isn't clear yet which platforms have full Unicode support. You can consider depending upon
 * <a href="https://github.com/tommyettinger/RegExodus">RegExodus</a> for more cross-platform Unicode support; a method
 * reference to {@code Category.L::contains} acts like {@code Character::isLetter}, but works on GWT.
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
	 * Creates a new set with an initial capacity of 51 and a load factor of {@link Utilities#getDefaultLoadFactor()}.
	 * This uses the specified filter and editor.
	 *
	 * @param filter a CharPredicate that should return true iff a character should be considered for equality/hashing
	 * @param editor a CharToCharFunction that will take a char from a key String and return a potentially different char
	 */
	public FilteredStringSet (CharPredicate filter, CharToCharFunction editor) {
		super();
		this.filter = filter;
		this.editor = editor;
	}

	/**
	 * Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity items before
	 * growing the backing table.
	 * This uses the specified filter and editor.
	 *
	 * @param filter a CharPredicate that should return true iff a character should be considered for equality/hashing
	 * @param editor a CharToCharFunction that will take a char from a key String and return a potentially different char
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 * @param loadFactor      what fraction of the capacity can be filled before this has to resize; 0 &lt; loadFactor &lt;= 1
	 */
	public FilteredStringSet (CharPredicate filter, CharToCharFunction editor, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		this.filter = filter;
		this.editor = editor;
	}

	/**
	 * Creates a new set identical to the specified set.
	 *
	 * @param set another FilteredStringSet to copy
	 */
	public FilteredStringSet (FilteredStringSet set) {
		super(set);
		filter = set.filter;
		editor = set.editor;
	}

	/**
	 * Creates a new set that contains all distinct elements in {@code coll}.
	 * This uses the specified filter and editor, including while it enters the items in coll.
	 *
	 * @param filter a CharPredicate that should return true iff a character should be considered for equality/hashing
	 * @param editor a CharToCharFunction that will take a char from a key String and return a potentially different char
	 * @param coll a Collection implementation to copy, such as an ObjectList or a Set that doesn't subclass ObjectSet
	 */
	public FilteredStringSet (CharPredicate filter, CharToCharFunction editor, Collection<? extends String> coll) {
		this(filter, editor, coll.size(), Utilities.getDefaultLoadFactor());
		addAll(coll);
	}

	/**
	 * Creates a new set using {@code length} items from the given {@code array}, starting at {@code} offset (inclusive).
	 * This takes a String array, not a String array, though Strings can be put into a String array (along with
	 * StringBuilders and similar Strings).
	 * This considers all characters in a String key and does not edit them.
	 *
	 * @param filter a CharPredicate that should return true iff a character should be considered for equality/hashing
	 * @param editor a CharToCharFunction that will take a char from a key String and return a potentially different char
	 * @param array  an array to draw items from
	 * @param offset the first index in array to draw an item from
	 * @param length how many items to take from array; bounds-checking is the responsibility of the using code
	 */
	public FilteredStringSet (CharPredicate filter, CharToCharFunction editor, String[] array, int offset, int length) {
		this(filter, editor, length, Utilities.getDefaultLoadFactor());
		addAll(array, offset, length);
	}

	/**
	 * Creates a new set containing all the items in the given array.
	 * This takes a String array, not a String array, though Strings can be put into a String array (along with
	 * StringBuilders and similar Strings).
	 * This considers all characters in a String key and does not edit them.
	 *
	 * @param filter a CharPredicate that should return true iff a character should be considered for equality/hashing
	 * @param editor a CharToCharFunction that will take a char from a key String and return a potentially different char
	 * @param array an array that will be used in full, except for duplicate items
	 */
	public FilteredStringSet (CharPredicate filter, CharToCharFunction editor, String[] array) {
		this(filter, editor, array, 0, array.length);
	}

	public CharPredicate getFilter() {
		return filter;
	}

	/**
	 * Sets the filter that determines which characters in a String are considered for equality and hashing, then
	 * returns this object, for chaining. Common CharPredicate filters you might use could be method references to
	 * {@link Character#isLetter(char)} or {@link CharList#contains(char)}, for example. If the filter returns true for
	 * a given character, that character will be used for hashing/equality; otherwise it will be ignored.
	 * The default filter always returns true. If the filter changes, that invalidates anything previously entered into
	 * this, so before changing the filter <em>this clears the entire data structure</em>, removing all existing items.
	 * @param filter a CharPredicate that should return true iff a character should be considered for equality/hashing
	 * @return this, for chaining
	 */
	public FilteredStringSet setFilter(CharPredicate filter) {
		clear();
		this.filter = filter;
		return this;
	}

	public CharToCharFunction getEditor() {
		return editor;
	}

	/**
	 * Sets the editor that can alter the characters in a String when they are being used for equality and hashing. This
	 * does not apply any changes to the Strings in this data structure; it only affects how they are hashed or
	 * compared. Common CharToCharFunction editors you might use could be a method reference to
	 * {@link Character#toUpperCase(char)} (useful for case-insensitivity) or a lambda that could do... anything.
	 * The default filter returns the char it is passed without changes. If the editor changes, that invalidates
	 * anything previously entered into this, so before changing the editor <em>this clears the entire data
	 * structure</em>, removing all existing items.
	 * @param editor a CharToCharFunction that will take a char from a key String and return a potentially different char
	 * @return this, for chaining
	 */
	public FilteredStringSet setEditor(CharToCharFunction editor) {
		clear();
		this.editor = editor;
		return this;
	}

	/**
	 * Equivalent to calling {@code mySet.setFilter(filter).setEditor(editor)}, but only clears the data structure once.
	 * @see #setFilter(CharPredicate)
	 * @see #setEditor(CharToCharFunction)
	 * @param filter a CharPredicate that should return true iff a character should be considered for equality/hashing
	 * @param editor a CharToCharFunction that will take a char from a key String and return a potentially different char
	 * @return this, for chaining
	 */
	public FilteredStringSet setModifiers(CharPredicate filter, CharToCharFunction editor) {
		clear();
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
		FilteredStringSet set = new FilteredStringSet(filter, editor, 1, Utilities.getDefaultLoadFactor());
		set.add(item);
		return set;
	}

	public static FilteredStringSet with (CharPredicate filter, CharToCharFunction editor, String... array) {
        return new FilteredStringSet(filter, editor, array);
	}

}
