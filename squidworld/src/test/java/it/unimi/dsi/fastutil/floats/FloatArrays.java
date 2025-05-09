/*
	* Copyright (C) 2002-2024 Sebastiano Vigna
	*
	* Licensed under the Apache License, Version 2.0 (the "License");
	* you may not use this file except in compliance with the License.
	* You may obtain a copy of the License at
	*
	*     http://www.apache.org/licenses/LICENSE-2.0
	*
	* Unless required by applicable law or agreed to in writing, software
	* distributed under the License is distributed on an "AS IS" BASIS,
	* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	* See the License for the specific language governing permissions and
	* limitations under the License.
	*
	*
	*
	* For the sorting and binary search code:
	*
	* Copyright (C) 1999 CERN - European Organization for Nuclear Research.
	*
	*   Permission to use, copy, modify, distribute and sell this software and
	*   its documentation for any purpose is hereby granted without fee,
	*   provided that the above copyright notice appear in all copies and that
	*   both that copyright notice and this permission notice appear in
	*   supporting documentation. CERN makes no representations about the
	*   suitability of this software for any purpose. It is provided "as is"
	*   without expressed or implied warranty.
	*/
package it.unimi.dsi.fastutil.floats;

import com.github.tommyettinger.ds.support.sort.FloatComparator;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Hash;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class providing static methods and objects that do useful things with type-specific arrays.
 *
 * <p>
 * In particular, the {@code forceCapacity()}, {@code ensureCapacity()}, {@code grow()},
 * {@code trim()} and {@code setLength()} methods allow to handle arrays much like array lists. This
 * can be very useful when efficiency (or syntactic simplicity) reasons make array lists unsuitable.
 *
 * <h2>Sorting</h2>
 *
 * <p>
 * There are several sorting methods available. The main theme is that of letting you choose the
 * sorting algorithm you prefer (i.e., trading stability of mergesort for no memory allocation in
 * quicksort).
 * 
 * <h3>Parallel operations</h3> Some algorithms provide a parallel version that will by default use
 * the {@linkplain ForkJoinPool#commonPool() common pool}, but this can be overridden by calling the
 * function in a task already in the {@link ForkJoinPool} that the operation should run in. For
 * example, something along the lines of
 * "{@code poolToParallelSortIn.invoke(() -> parallelQuickSort(arrayToSort))}" will run the parallel
 * sort in {@code poolToParallelSortIn} instead of the default pool.
 *
 * Some algorithms also provide an explicit <em>indirect</em> sorting facility, which makes it
 * possible to sort an array using the values in another array as comparator.
 *
 * <p>
 * However, if you wish to let the implementation choose an algorithm for you, both
 * {@link #stableSort} and {@link #unstableSort} methods are available, which dynamically chooses an
 * algorithm based on unspecified criteria (but most likely stability, array size, and array element
 * type).
 *
 * <p>
 * All comparison-based algorithm have an implementation based on a type-specific comparator.
 *
 * <p>
 * As a general rule, sequential radix sort is significantly faster than quicksort or mergesort, in
 * particular on random-looking data. In the parallel case, up to a few cores parallel radix sort is
 * still the fastest, but at some point quicksort exploits parallelism better.
 *
 * <p>
 * If you are fine with not knowing exactly which algorithm will be run (in particular, not knowing
 * exactly whether a support array will be allocated), the dual-pivot parallel sorts in
 * {@link java.util.Arrays} are about 50% faster than the classical single-pivot implementation used
 * here.
 *
 * <p>
 * In any case, if sorting time is important I suggest that you benchmark your sorting load with
 * your data distribution and on your architecture.
 *
 * @see java.util.Arrays
 */
public final class FloatArrays {
	private FloatArrays() {
	}

	/** A static, final, empty array. */
	public static final float[] EMPTY_ARRAY = {};
	/**
	 * A static, final, empty array to be used as default array in allocations. An object distinct from
	 * {@link #EMPTY_ARRAY} makes it possible to have different behaviors depending on whether the user
	 * required an empty allocation, or we are just lazily delaying allocation.
	 *
	 * @see java.util.ArrayList
	 */
	public static final float[] DEFAULT_EMPTY_ARRAY = {};

	/**
	 * Forces an array to contain the given number of entries, preserving just a part of the array.
	 *
	 * @param array an array.
	 * @param length the new minimum length for this array.
	 * @param preserve the number of elements of the array that must be preserved in case a new
	 *            allocation is necessary.
	 * @return an array with {@code length} entries whose first {@code preserve} entries are the same as
	 *         those of {@code array}.
	 */
	public static float[] forceCapacity(final float[] array, final int length, final int preserve) {
		final float[] t = new float[length];
		System.arraycopy(array, 0, t, 0, preserve);
		return t;
	}

	/**
	 * Ensures that an array can contain the given number of entries.
	 *
	 * <p>
	 * If you cannot foresee whether this array will need again to be enlarged, you should probably use
	 * {@code grow()} instead.
	 *
	 * @param array an array.
	 * @param length the new minimum length for this array.
	 * @return {@code array}, if it contains {@code length} entries or more; otherwise, an array with
	 *         {@code length} entries whose first {@code array.length} entries are the same as those of
	 *         {@code array}.
	 */
	public static float[] ensureCapacity(final float[] array, final int length) {
		return ensureCapacity(array, length, array.length);
	}

	/**
	 * Ensures that an array can contain the given number of entries, preserving just a part of the
	 * array.
	 *
	 * @param array an array.
	 * @param length the new minimum length for this array.
	 * @param preserve the number of elements of the array that must be preserved in case a new
	 *            allocation is necessary.
	 * @return {@code array}, if it can contain {@code length} entries or more; otherwise, an array with
	 *         {@code length} entries whose first {@code preserve} entries are the same as those of
	 *         {@code array}.
	 */
	public static float[] ensureCapacity(final float[] array, final int length, final int preserve) {
		return length > array.length ? forceCapacity(array, length, preserve) : array;
	}

	/**
	 * Grows the given array to the maximum between the given length and the current length increased by
	 * 50%, provided that the given length is larger than the current length.
	 *
	 * <p>
	 * If you want complete control on the array growth, you should probably use
	 * {@code ensureCapacity()} instead.
	 *
	 * @param array an array.
	 * @param length the new minimum length for this array.
	 * @return {@code array}, if it can contain {@code length} entries; otherwise, an array with
	 *         max({@code length},{@code array.length}/&phi;) entries whose first {@code array.length}
	 *         entries are the same as those of {@code array}.
	 */
	public static float[] grow(final float[] array, final int length) {
		return grow(array, length, array.length);
	}

	/**
	 * Grows the given array to the maximum between the given length and the current length increased by
	 * 50%, provided that the given length is larger than the current length, preserving just a part of
	 * the array.
	 *
	 * <p>
	 * If you want complete control on the array growth, you should probably use
	 * {@code ensureCapacity()} instead.
	 *
	 * @param array an array.
	 * @param length the new minimum length for this array.
	 * @param preserve the number of elements of the array that must be preserved in case a new
	 *            allocation is necessary.
	 * @return {@code array}, if it can contain {@code length} entries; otherwise, an array with
	 *         max({@code length},{@code array.length}/&phi;) entries whose first {@code preserve}
	 *         entries are the same as those of {@code array}.
	 */
	public static float[] grow(final float[] array, final int length, final int preserve) {
		if (length > array.length) {
			final int newLength = (int)Math.max(Math.min((long)array.length + (array.length >> 1), Arrays.MAX_ARRAY_SIZE), length);
			final float[] t = new float[newLength];
			System.arraycopy(array, 0, t, 0, preserve);
			return t;
		}
		return array;
	}

	/**
	 * Trims the given array to the given length.
	 *
	 * @param array an array.
	 * @param length the new maximum length for the array.
	 * @return {@code array}, if it contains {@code length} entries or less; otherwise, an array with
	 *         {@code length} entries whose entries are the same as the first {@code length} entries of
	 *         {@code array}.
	 *
	 */
	public static float[] trim(final float[] array, final int length) {
		if (length >= array.length) return array;
		final float[] t = length == 0 ? EMPTY_ARRAY : new float[length];
		System.arraycopy(array, 0, t, 0, length);
		return t;
	}

	/**
	 * Sets the length of the given array.
	 *
	 * @param array an array.
	 * @param length the new length for the array.
	 * @return {@code array}, if it contains exactly {@code length} entries; otherwise, if it contains
	 *         <em>more</em> than {@code length} entries, an array with {@code length} entries whose
	 *         entries are the same as the first {@code length} entries of {@code array}; otherwise, an
	 *         array with {@code length} entries whose first {@code array.length} entries are the same
	 *         as those of {@code array}.
	 *
	 */
	public static float[] setLength(final float[] array, final int length) {
		if (length == array.length) return array;
		if (length < array.length) return trim(array, length);
		return ensureCapacity(array, length);
	}

	/**
	 * Returns a copy of a portion of an array.
	 *
	 * @param array an array.
	 * @param offset the first element to copy.
	 * @param length the number of elements to copy.
	 * @return a new array containing {@code length} elements of {@code array} starting at
	 *         {@code offset}.
	 */
	public static float[] copy(final float[] array, final int offset, final int length) {
		ensureOffsetLength(array, offset, length);
		final float[] a = length == 0 ? EMPTY_ARRAY : new float[length];
		System.arraycopy(array, offset, a, 0, length);
		return a;
	}

	/**
	 * Returns a copy of an array.
	 *
	 * @param array an array.
	 * @return a copy of {@code array}.
	 */
	public static float[] copy(final float[] array) {
		return array.clone();
	}

	/**
	 * Fills the given array with the given value.
	 *
	 * @param array an array.
	 * @param value the new value for all elements of the array.
	 * @deprecated Please use the corresponding {@link java.util.Arrays} method.
	 */
	@Deprecated
	public static void fill(final float[] array, final float value) {
		int i = array.length;
		while (i-- != 0) array[i] = value;
	}

	/**
	 * Fills a portion of the given array with the given value.
	 *
	 * @param array an array.
	 * @param from the starting index of the portion to fill (inclusive).
	 * @param to the end index of the portion to fill (exclusive).
	 * @param value the new value for all elements of the specified portion of the array.
	 * @deprecated Please use the corresponding {@link java.util.Arrays} method.
	 */
	@Deprecated
	public static void fill(final float[] array, final int from, int to, final float value) {
		ensureFromTo(array, from, to);
		if (from == 0) while (to-- != 0) array[to] = value;
		else for (int i = from; i < to; i++) array[i] = value;
	}

	/**
	 * Returns true if the two arrays are elementwise equal.
	 *
	 * @param a1 an array.
	 * @param a2 another array.
	 * @return true if the two arrays are of the same length, and their elements are equal.
	 * @deprecated Please use the corresponding {@link java.util.Arrays} method, which is intrinsified
	 *             in recent JVMs.
	 */
	@Deprecated
	public static boolean equals(final float[] a1, final float[] a2) {
		int i = a1.length;
		if (i != a2.length) return false;
		while (i-- != 0) if (!(Float.floatToIntBits(a1[i]) == Float.floatToIntBits(a2[i]))) return false;
		return true;
	}

	/**
	 * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits an array.
	 *
	 * <p>
	 * This method may be used whenever an array range check is needed.
	 *
	 * <p>
	 * In Java 9 and up, this method should be considered deprecated in favor of the
	 * {@link java.util.Objects#checkFromToIndex(int, int, int)} method, which may be intrinsified in
	 * recent JVMs.
	 *
	 * @param a an array.
	 * @param from a start index (inclusive).
	 * @param to an end index (exclusive).
	 * @throws IllegalArgumentException if {@code from} is greater than {@code to}.
	 * @throws ArrayIndexOutOfBoundsException if {@code from} or {@code to} are greater than the array
	 *             length or negative.
	 */
	public static void ensureFromTo(final float[] a, final int from, final int to) {
		Arrays.ensureFromTo(a.length, from, to);
	}

	/**
	 * Ensures that a range given by an offset and a length fits an array.
	 *
	 * <p>
	 * This method may be used whenever an array range check is needed.
	 *
	 * <p>
	 * In Java 9 and up, this method should be considered deprecated in favor of the
	 * {@link java.util.Objects#checkFromIndexSize(int, int, int)} method, which may be intrinsified in
	 * recent JVMs.
	 *
	 * @param a an array.
	 * @param offset a start index.
	 * @param length a length (the number of elements in the range).
	 * @throws IllegalArgumentException if {@code length} is negative.
	 * @throws ArrayIndexOutOfBoundsException if {@code offset} is negative or
	 *             {@code offset}+{@code length} is greater than the array length.
	 */
	public static void ensureOffsetLength(final float[] a, final int offset, final int length) {
		Arrays.ensureOffsetLength(a.length, offset, length);
	}

	/**
	 * Ensures that two arrays are of the same length.
	 *
	 * @param a an array.
	 * @param b another array.
	 * @throws IllegalArgumentException if the two argument arrays are not of the same length.
	 */
	public static void ensureSameLength(final float[] a, final float[] b) {
		if (a.length != b.length) throw new IllegalArgumentException("Array size mismatch: " + a.length + " != " + b.length);
	}

	private static final int QUICKSORT_NO_REC = 16;
	private static final int PARALLEL_QUICKSORT_NO_FORK = 8192;
	private static final int QUICKSORT_MEDIAN_OF_9 = 128;
	private static final int MERGESORT_NO_REC = 16;

	private static ForkJoinPool getPool() {
		// Make sure to update Arrays.drv, BigArrays.drv, and src/it/unimi/dsi/fastutil/Arrays.java as well
		ForkJoinPool current = ForkJoinTask.getPool();
		return current == null ? ForkJoinPool.commonPool() : current;
	}

	/**
	 * Swaps two elements of an anrray.
	 *
	 * @param x an array.
	 * @param a a position in {@code x}.
	 * @param b another position in {@code x}.
	 */
	public static void swap(final float[] x, final int a, final int b) {
		final float t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	/**
	 * Swaps two sequences of elements of an array.
	 *
	 * @param x an array.
	 * @param a a position in {@code x}.
	 * @param b another position in {@code x}.
	 * @param n the number of elements to exchange starting at {@code a} and {@code b}.
	 */
	public static void swap(final float[] x, int a, int b, final int n) {
		for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
	}

	private static int med3(final float[] x, final int a, final int b, final int c, FloatComparator comp) {
		final int ab = comp.compare(x[a], x[b]);
		final int ac = comp.compare(x[a], x[c]);
		final int bc = comp.compare(x[b], x[c]);
		return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
	}

	private static void selectionSort(final float[] a, final int from, final int to, final FloatComparator comp) {
		for (int i = from; i < to - 1; i++) {
			int m = i;
			for (int j = i + 1; j < to; j++) if (comp.compare(a[j], a[m]) < 0) m = j;
			if (m != i) {
				final float u = a[i];
				a[i] = a[m];
				a[m] = u;
			}
		}
	}

	private static void insertionSort(final float[] a, final int from, final int to, final FloatComparator comp) {
		for (int i = from; ++i < to;) {
			float t = a[i];
			int j = i;
			for (float u = a[j - 1]; comp.compare(t, u) < 0; u = a[--j - 1]) {
				a[j] = u;
				if (from == j - 1) {
					--j;
					break;
				}
			}
			a[j] = t;
		}
	}

	/**
	 * Sorts the specified range of elements according to the order induced by the specified comparator
	 * using quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * Note that this implementation does not allocate any object, contrarily to the implementation used
	 * to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
	 *
	 * @param x the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 *
	 */
	public static void quickSort(final float[] x, final int from, final int to, final FloatComparator comp) {
		final int len = to - from;
		// Selection sort on smallest arrays
		if (len < QUICKSORT_NO_REC) {
			selectionSort(x, from, to, comp);
			return;
		}
		// Choose a partition element, v
		int m = from + len / 2;
		int l = from;
		int n = to - 1;
		if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
			int s = len / 8;
			l = med3(x, l, l + s, l + 2 * s, comp);
			m = med3(x, m - s, m, m + s, comp);
			n = med3(x, n - 2 * s, n - s, n, comp);
		}
		m = med3(x, l, m, n, comp); // Mid-size, med of 3
		final float v = x[m];
		// Establish Invariant: v* (<v)* (>v)* v*
		int a = from, b = a, c = to - 1, d = c;
		while (true) {
			int comparison;
			while (b <= c && (comparison = comp.compare(x[b], v)) <= 0) {
				if (comparison == 0) swap(x, a++, b);
				b++;
			}
			while (c >= b && (comparison = comp.compare(x[c], v)) >= 0) {
				if (comparison == 0) swap(x, c, d--);
				c--;
			}
			if (b > c) break;
			swap(x, b++, c--);
		}
		// Swap partition elements back to middle
		int s;
		s = Math.min(a - from, b - a);
		swap(x, from, b - s, s);
		s = Math.min(d - c, to - d - 1);
		swap(x, b, to - s, s);
		// Recursively sort non-partition-elements
		if ((s = b - a) > 1) quickSort(x, from, from + s, comp);
		if ((s = d - c) > 1) quickSort(x, to - s, to, comp);
	}

	/**
	 * Sorts an array according to the order induced by the specified comparator using quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * Note that this implementation does not allocate any object, contrarily to the implementation used
	 * to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
	 *
	 * @param x the array to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 *
	 */
	public static void quickSort(final float[] x, final FloatComparator comp) {
		quickSort(x, 0, x.length, comp);
	}

	protected static class ForkJoinQuickSortComp extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		private final int from;
		private final int to;
		private final float[] x;
		private final FloatComparator comp;

		public ForkJoinQuickSortComp(final float[] x, final int from, final int to, final FloatComparator comp) {
			this.from = from;
			this.to = to;
			this.x = x;
			this.comp = comp;
		}

		@Override
		protected void compute() {
			final float[] x = this.x;
			final int len = to - from;
			if (len < PARALLEL_QUICKSORT_NO_FORK) {
				quickSort(x, from, to, comp);
				return;
			}
			// Choose a partition element, v
			int m = from + len / 2;
			int l = from;
			int n = to - 1;
			int s = len / 8;
			l = med3(x, l, l + s, l + 2 * s, comp);
			m = med3(x, m - s, m, m + s, comp);
			n = med3(x, n - 2 * s, n - s, n, comp);
			m = med3(x, l, m, n, comp);
			final float v = x[m];
			// Establish Invariant: v* (<v)* (>v)* v*
			int a = from, b = a, c = to - 1, d = c;
			while (true) {
				int comparison;
				while (b <= c && (comparison = comp.compare(x[b], v)) <= 0) {
					if (comparison == 0) swap(x, a++, b);
					b++;
				}
				while (c >= b && (comparison = comp.compare(x[c], v)) >= 0) {
					if (comparison == 0) swap(x, c, d--);
					c--;
				}
				if (b > c) break;
				swap(x, b++, c--);
			}
			// Swap partition elements back to middle
			int t;
			s = Math.min(a - from, b - a);
			swap(x, from, b - s, s);
			s = Math.min(d - c, to - d - 1);
			swap(x, b, to - s, s);
			// Recursively sort non-partition-elements
			s = b - a;
			t = d - c;
			if (s > 1 && t > 1) invokeAll(new ForkJoinQuickSortComp(x, from, from + s, comp), new ForkJoinQuickSortComp(x, to - t, to, comp));
			else if (s > 1) invokeAll(new ForkJoinQuickSortComp(x, from, from + s, comp));
			else invokeAll(new ForkJoinQuickSortComp(x, to - t, to, comp));
		}
	}

	/**
	 * Sorts the specified range of elements according to the order induced by the specified comparator
	 * using a parallel quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * @param x the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 */
	public static void parallelQuickSort(final float[] x, final int from, final int to, final FloatComparator comp) {
		ForkJoinPool pool = getPool();
		if (to - from < PARALLEL_QUICKSORT_NO_FORK || pool.getParallelism() == 1) quickSort(x, from, to, comp);
		else {
			pool.invoke(new ForkJoinQuickSortComp(x, from, to, comp));
		}
	}

	/**
	 * Sorts an array according to the order induced by the specified comparator using a parallel
	 * quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * @param x the array to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 */
	public static void parallelQuickSort(final float[] x, final FloatComparator comp) {
		parallelQuickSort(x, 0, x.length, comp);
	}

	private static int med3(final float[] x, final int a, final int b, final int c) {
		final int ab = (Float.compare((x[a]), (x[b])));
		final int ac = (Float.compare((x[a]), (x[c])));
		final int bc = (Float.compare((x[b]), (x[c])));
		return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
	}

	private static void selectionSort(final float[] a, final int from, final int to) {
		for (int i = from; i < to - 1; i++) {
			int m = i;
			for (int j = i + 1; j < to; j++) if ((Float.compare((a[j]), (a[m])) < 0)) m = j;
			if (m != i) {
				final float u = a[i];
				a[i] = a[m];
				a[m] = u;
			}
		}
	}

	private static void insertionSort(final float[] a, final int from, final int to) {
		for (int i = from; ++i < to;) {
			float t = a[i];
			int j = i;
			for (float u = a[j - 1]; (Float.compare((t), (u)) < 0); u = a[--j - 1]) {
				a[j] = u;
				if (from == j - 1) {
					--j;
					break;
				}
			}
			a[j] = t;
		}
	}

	/**
	 * Sorts the specified range of elements according to the natural ascending order using quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * Note that this implementation does not allocate any object, contrarily to the implementation used
	 * to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
	 *
	 * @param x the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */

	public static void quickSort(final float[] x, final int from, final int to) {
		final int len = to - from;
		// Selection sort on smallest arrays
		if (len < QUICKSORT_NO_REC) {
			selectionSort(x, from, to);
			return;
		}
		// Choose a partition element, v
		int m = from + len / 2;
		int l = from;
		int n = to - 1;
		if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
			int s = len / 8;
			l = med3(x, l, l + s, l + 2 * s);
			m = med3(x, m - s, m, m + s);
			n = med3(x, n - 2 * s, n - s, n);
		}
		m = med3(x, l, m, n); // Mid-size, med of 3
		final float v = x[m];
		// Establish Invariant: v* (<v)* (>v)* v*
		int a = from, b = a, c = to - 1, d = c;
		while (true) {
			int comparison;
			while (b <= c && (comparison = (Float.compare((x[b]), (v)))) <= 0) {
				if (comparison == 0) swap(x, a++, b);
				b++;
			}
			while (c >= b && (comparison = (Float.compare((x[c]), (v)))) >= 0) {
				if (comparison == 0) swap(x, c, d--);
				c--;
			}
			if (b > c) break;
			swap(x, b++, c--);
		}
		// Swap partition elements back to middle
		int s;
		s = Math.min(a - from, b - a);
		swap(x, from, b - s, s);
		s = Math.min(d - c, to - d - 1);
		swap(x, b, to - s, s);
		// Recursively sort non-partition-elements
		if ((s = b - a) > 1) quickSort(x, from, from + s);
		if ((s = d - c) > 1) quickSort(x, to - s, to);
	}

	/**
	 * Sorts an array according to the natural ascending order using quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * Note that this implementation does not allocate any object, contrarily to the implementation used
	 * to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
	 *
	 * @param x the array to be sorted.
	 *
	 */
	public static void quickSort(final float[] x) {
		quickSort(x, 0, x.length);
	}

	protected static class ForkJoinQuickSort extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		private final int from;
		private final int to;
		private final float[] x;

		public ForkJoinQuickSort(final float[] x, final int from, final int to) {
			this.from = from;
			this.to = to;
			this.x = x;
		}

		@Override

		protected void compute() {
			final float[] x = this.x;
			final int len = to - from;
			if (len < PARALLEL_QUICKSORT_NO_FORK) {
				quickSort(x, from, to);
				return;
			}
			// Choose a partition element, v
			int m = from + len / 2;
			int l = from;
			int n = to - 1;
			int s = len / 8;
			l = med3(x, l, l + s, l + 2 * s);
			m = med3(x, m - s, m, m + s);
			n = med3(x, n - 2 * s, n - s, n);
			m = med3(x, l, m, n);
			final float v = x[m];
			// Establish Invariant: v* (<v)* (>v)* v*
			int a = from, b = a, c = to - 1, d = c;
			while (true) {
				int comparison;
				while (b <= c && (comparison = (Float.compare((x[b]), (v)))) <= 0) {
					if (comparison == 0) swap(x, a++, b);
					b++;
				}
				while (c >= b && (comparison = (Float.compare((x[c]), (v)))) >= 0) {
					if (comparison == 0) swap(x, c, d--);
					c--;
				}
				if (b > c) break;
				swap(x, b++, c--);
			}
			// Swap partition elements back to middle
			int t;
			s = Math.min(a - from, b - a);
			swap(x, from, b - s, s);
			s = Math.min(d - c, to - d - 1);
			swap(x, b, to - s, s);
			// Recursively sort non-partition-elements
			s = b - a;
			t = d - c;
			if (s > 1 && t > 1) invokeAll(new ForkJoinQuickSort(x, from, from + s), new ForkJoinQuickSort(x, to - t, to));
			else if (s > 1) invokeAll(new ForkJoinQuickSort(x, from, from + s));
			else invokeAll(new ForkJoinQuickSort(x, to - t, to));
		}
	}

	/**
	 * Sorts the specified range of elements according to the natural ascending order using a parallel
	 * quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * @param x the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void parallelQuickSort(final float[] x, final int from, final int to) {
		ForkJoinPool pool = getPool();
		if (to - from < PARALLEL_QUICKSORT_NO_FORK || pool.getParallelism() == 1) quickSort(x, from, to);
		else {
			pool.invoke(new ForkJoinQuickSort(x, from, to));
		}
	}

	/**
	 * Sorts an array according to the natural ascending order using a parallel quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * @param x the array to be sorted.
	 *
	 */
	public static void parallelQuickSort(final float[] x) {
		parallelQuickSort(x, 0, x.length);
	}

	private static int med3Indirect(final int[] perm, final float[] x, final int a, final int b, final int c) {
		final float aa = x[perm[a]];
		final float bb = x[perm[b]];
		final float cc = x[perm[c]];
		final int ab = (Float.compare((aa), (bb)));
		final int ac = (Float.compare((aa), (cc)));
		final int bc = (Float.compare((bb), (cc)));
		return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
	}

	private static void insertionSortIndirect(final int[] perm, final float[] a, final int from, final int to) {
		for (int i = from; ++i < to;) {
			int t = perm[i];
			int j = i;
			for (int u = perm[j - 1]; (Float.compare((a[t]), (a[u])) < 0); u = perm[--j - 1]) {
				perm[j] = u;
				if (from == j - 1) {
					--j;
					break;
				}
			}
			perm[j] = t;
		}
	}

	/**
	 * Sorts the specified range of elements according to the natural ascending order using indirect
	 * quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code x[perm[i]] &le; x[perm[i + 1]]}.
	 *
	 * <p>
	 * Note that this implementation does not allocate any object, contrarily to the implementation used
	 * to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
	 *
	 * @param perm a permutation array indexing {@code x}.
	 * @param x the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */

	public static void quickSortIndirect(final int[] perm, final float[] x, final int from, final int to) {
		final int len = to - from;
		// Selection sort on smallest arrays
		if (len < QUICKSORT_NO_REC) {
			insertionSortIndirect(perm, x, from, to);
			return;
		}
		// Choose a partition element, v
		int m = from + len / 2;
		int l = from;
		int n = to - 1;
		if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
			int s = len / 8;
			l = med3Indirect(perm, x, l, l + s, l + 2 * s);
			m = med3Indirect(perm, x, m - s, m, m + s);
			n = med3Indirect(perm, x, n - 2 * s, n - s, n);
		}
		m = med3Indirect(perm, x, l, m, n); // Mid-size, med of 3
		final float v = x[perm[m]];
		// Establish Invariant: v* (<v)* (>v)* v*
		int a = from, b = a, c = to - 1, d = c;
		while (true) {
			int comparison;
			while (b <= c && (comparison = (Float.compare((x[perm[b]]), (v)))) <= 0) {
				if (comparison == 0) IntArrays.swap(perm, a++, b);
				b++;
			}
			while (c >= b && (comparison = (Float.compare((x[perm[c]]), (v)))) >= 0) {
				if (comparison == 0) IntArrays.swap(perm, c, d--);
				c--;
			}
			if (b > c) break;
			IntArrays.swap(perm, b++, c--);
		}
		// Swap partition elements back to middle
		int s;
		s = Math.min(a - from, b - a);
		IntArrays.swap(perm, from, b - s, s);
		s = Math.min(d - c, to - d - 1);
		IntArrays.swap(perm, b, to - s, s);
		// Recursively sort non-partition-elements
		if ((s = b - a) > 1) quickSortIndirect(perm, x, from, from + s);
		if ((s = d - c) > 1) quickSortIndirect(perm, x, to - s, to);
	}

	/**
	 * Sorts an array according to the natural ascending order using indirect quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code x[perm[i]] &le; x[perm[i + 1]]}.
	 *
	 * <p>
	 * Note that this implementation does not allocate any object, contrarily to the implementation used
	 * to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
	 *
	 * @param perm a permutation array indexing {@code x}.
	 * @param x the array to be sorted.
	 */
	public static void quickSortIndirect(final int[] perm, final float[] x) {
		quickSortIndirect(perm, x, 0, x.length);
	}

	protected static class ForkJoinQuickSortIndirect extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		private final int from;
		private final int to;
		private final int[] perm;
		private final float[] x;

		public ForkJoinQuickSortIndirect(final int[] perm, final float[] x, final int from, final int to) {
			this.from = from;
			this.to = to;
			this.x = x;
			this.perm = perm;
		}

		@Override

		protected void compute() {
			final float[] x = this.x;
			final int len = to - from;
			if (len < PARALLEL_QUICKSORT_NO_FORK) {
				quickSortIndirect(perm, x, from, to);
				return;
			}
			// Choose a partition element, v
			int m = from + len / 2;
			int l = from;
			int n = to - 1;
			int s = len / 8;
			l = med3Indirect(perm, x, l, l + s, l + 2 * s);
			m = med3Indirect(perm, x, m - s, m, m + s);
			n = med3Indirect(perm, x, n - 2 * s, n - s, n);
			m = med3Indirect(perm, x, l, m, n);
			final float v = x[perm[m]];
			// Establish Invariant: v* (<v)* (>v)* v*
			int a = from, b = a, c = to - 1, d = c;
			while (true) {
				int comparison;
				while (b <= c && (comparison = (Float.compare((x[perm[b]]), (v)))) <= 0) {
					if (comparison == 0) IntArrays.swap(perm, a++, b);
					b++;
				}
				while (c >= b && (comparison = (Float.compare((x[perm[c]]), (v)))) >= 0) {
					if (comparison == 0) IntArrays.swap(perm, c, d--);
					c--;
				}
				if (b > c) break;
				IntArrays.swap(perm, b++, c--);
			}
			// Swap partition elements back to middle
			int t;
			s = Math.min(a - from, b - a);
			IntArrays.swap(perm, from, b - s, s);
			s = Math.min(d - c, to - d - 1);
			IntArrays.swap(perm, b, to - s, s);
			// Recursively sort non-partition-elements
			s = b - a;
			t = d - c;
			if (s > 1 && t > 1) invokeAll(new ForkJoinQuickSortIndirect(perm, x, from, from + s), new ForkJoinQuickSortIndirect(perm, x, to - t, to));
			else if (s > 1) invokeAll(new ForkJoinQuickSortIndirect(perm, x, from, from + s));
			else invokeAll(new ForkJoinQuickSortIndirect(perm, x, to - t, to));
		}
	}

	/**
	 * Sorts the specified range of elements according to the natural ascending order using a parallel
	 * indirect quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code x[perm[i]] &le; x[perm[i + 1]]}.
	 *
	 * @param perm a permutation array indexing {@code x}.
	 * @param x the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void parallelQuickSortIndirect(final int[] perm, final float[] x, final int from, final int to) {
		ForkJoinPool pool = getPool();
		if (to - from < PARALLEL_QUICKSORT_NO_FORK || pool.getParallelism() == 1) quickSortIndirect(perm, x, from, to);
		else {
			pool.invoke(new ForkJoinQuickSortIndirect(perm, x, from, to));
		}
	}

	/**
	 * Sorts an array according to the natural ascending order using a parallel indirect quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code x[perm[i]] &le; x[perm[i + 1]]}.
	 *
	 * @param perm a permutation array indexing {@code x}.
	 * @param x the array to be sorted.
	 *
	 */
	public static void parallelQuickSortIndirect(final int[] perm, final float[] x) {
		parallelQuickSortIndirect(perm, x, 0, x.length);
	}

	/**
	 * Stabilizes a permutation.
	 *
	 * <p>
	 * This method can be used to stabilize the permutation generated by an indirect sorting, assuming
	 * that initially the permutation array was in ascending order (e.g., the identity, as usually
	 * happens). This method scans the permutation, and for each non-singleton block of elements with
	 * the same associated values in {@code x}, permutes them in ascending order. The resulting
	 * permutation corresponds to a stable sort.
	 *
	 * <p>
	 * Usually combining an unstable indirect sort and this method is more efficient than using a stable
	 * sort, as most stable sort algorithms require a support array.
	 *
	 * <p>
	 * More precisely, assuming that {@code x[perm[i]] &le; x[perm[i + 1]]}, after stabilization we will
	 * also have that {@code x[perm[i]] = x[perm[i + 1]]} implies {@code perm[i] &le; perm[i + 1]}.
	 *
	 * @param perm a permutation array indexing {@code x} so that it is sorted.
	 * @param x the sorted array to be stabilized.
	 * @param from the index of the first element (inclusive) to be stabilized.
	 * @param to the index of the last element (exclusive) to be stabilized.
	 */
	public static void stabilize(final int[] perm, final float[] x, final int from, final int to) {
		int curr = from;
		for (int i = from + 1; i < to; i++) {
			if (x[perm[i]] != x[perm[curr]]) {
				if (i - curr > 1) IntArrays.parallelQuickSort(perm, curr, i);
				curr = i;
			}
		}
		if (to - curr > 1) IntArrays.parallelQuickSort(perm, curr, to);
	}

	/**
	 * Stabilizes a permutation.
	 *
	 * <p>
	 * This method can be used to stabilize the permutation generated by an indirect sorting, assuming
	 * that initially the permutation array was in ascending order (e.g., the identity, as usually
	 * happens). This method scans the permutation, and for each non-singleton block of elements with
	 * the same associated values in {@code x}, permutes them in ascending order. The resulting
	 * permutation corresponds to a stable sort.
	 *
	 * <p>
	 * Usually combining an unstable indirect sort and this method is more efficient than using a stable
	 * sort, as most stable sort algorithms require a support array.
	 *
	 * <p>
	 * More precisely, assuming that {@code x[perm[i]] &le; x[perm[i + 1]]}, after stabilization we will
	 * also have that {@code x[perm[i]] = x[perm[i + 1]]} implies {@code perm[i] &le; perm[i + 1]}.
	 *
	 * @param perm a permutation array indexing {@code x} so that it is sorted.
	 * @param x the sorted array to be stabilized.
	 */
	public static void stabilize(final int[] perm, final float[] x) {
		stabilize(perm, x, 0, perm.length);
	}

	private static int med3(final float[] x, final float[] y, final int a, final int b, final int c) {
		int t;
		final int ab = (t = (Float.compare((x[a]), (x[b])))) == 0 ? (Float.compare((y[a]), (y[b]))) : t;
		final int ac = (t = (Float.compare((x[a]), (x[c])))) == 0 ? (Float.compare((y[a]), (y[c]))) : t;
		final int bc = (t = (Float.compare((x[b]), (x[c])))) == 0 ? (Float.compare((y[b]), (y[c]))) : t;
		return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
	}

	private static void swap(final float[] x, final float[] y, final int a, final int b) {
		final float t = x[a];
		final float u = y[a];
		x[a] = x[b];
		y[a] = y[b];
		x[b] = t;
		y[b] = u;
	}

	private static void swap(final float[] x, final float[] y, int a, int b, final int n) {
		for (int i = 0; i < n; i++, a++, b++) swap(x, y, a, b);
	}

	private static void selectionSort(final float[] a, final float[] b, final int from, final int to) {
		for (int i = from; i < to - 1; i++) {
			int m = i, u;
			for (int j = i + 1; j < to; j++) if ((u = (Float.compare((a[j]), (a[m])))) < 0 || u == 0 && (Float.compare((b[j]), (b[m])) < 0)) m = j;
			if (m != i) {
				float t = a[i];
				a[i] = a[m];
				a[m] = t;
				t = b[i];
				b[i] = b[m];
				b[m] = t;
			}
		}
	}

	/**
	 * Sorts the specified range of elements of two arrays according to the natural lexicographical
	 * ascending order using quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of elements in
	 * the same position in the two provided arrays will be considered a single key, and permuted
	 * accordingly. In the end, either {@code x[i] &lt; x[i + 1]} or {@code x[i]
	 * == x[i + 1]} and {@code y[i] &le; y[i + 1]}.
	 *
	 * @param x the first array to be sorted.
	 * @param y the second array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */

	public static void quickSort(final float[] x, final float[] y, final int from, final int to) {
		final int len = to - from;
		if (len < QUICKSORT_NO_REC) {
			selectionSort(x, y, from, to);
			return;
		}
		// Choose a partition element, v
		int m = from + len / 2;
		int l = from;
		int n = to - 1;
		if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
			int s = len / 8;
			l = med3(x, y, l, l + s, l + 2 * s);
			m = med3(x, y, m - s, m, m + s);
			n = med3(x, y, n - 2 * s, n - s, n);
		}
		m = med3(x, y, l, m, n); // Mid-size, med of 3
		final float v = x[m], w = y[m];
		// Establish Invariant: v* (<v)* (>v)* v*
		int a = from, b = a, c = to - 1, d = c;
		while (true) {
			int comparison, t;
			while (b <= c && (comparison = (t = (Float.compare((x[b]), (v)))) == 0 ? (Float.compare((y[b]), (w))) : t) <= 0) {
				if (comparison == 0) swap(x, y, a++, b);
				b++;
			}
			while (c >= b && (comparison = (t = (Float.compare((x[c]), (v)))) == 0 ? (Float.compare((y[c]), (w))) : t) >= 0) {
				if (comparison == 0) swap(x, y, c, d--);
				c--;
			}
			if (b > c) break;
			swap(x, y, b++, c--);
		}
		// Swap partition elements back to middle
		int s;
		s = Math.min(a - from, b - a);
		swap(x, y, from, b - s, s);
		s = Math.min(d - c, to - d - 1);
		swap(x, y, b, to - s, s);
		// Recursively sort non-partition-elements
		if ((s = b - a) > 1) quickSort(x, y, from, from + s);
		if ((s = d - c) > 1) quickSort(x, y, to - s, to);
	}

	/**
	 * Sorts two arrays according to the natural lexicographical ascending order using quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of elements in
	 * the same position in the two provided arrays will be considered a single key, and permuted
	 * accordingly. In the end, either {@code x[i] &lt; x[i + 1]} or {@code x[i]
	 * == x[i + 1]} and {@code y[i] &le; y[i + 1]}.
	 *
	 * @param x the first array to be sorted.
	 * @param y the second array to be sorted.
	 */
	public static void quickSort(final float[] x, final float[] y) {
		ensureSameLength(x, y);
		quickSort(x, y, 0, x.length);
	}

	protected static class ForkJoinQuickSort2 extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		private final int from;
		private final int to;
		private final float[] x, y;

		public ForkJoinQuickSort2(final float[] x, final float[] y, final int from, final int to) {
			this.from = from;
			this.to = to;
			this.x = x;
			this.y = y;
		}

		@Override

		protected void compute() {
			final float[] x = this.x;
			final float[] y = this.y;
			final int len = to - from;
			if (len < PARALLEL_QUICKSORT_NO_FORK) {
				quickSort(x, y, from, to);
				return;
			}
			// Choose a partition element, v
			int m = from + len / 2;
			int l = from;
			int n = to - 1;
			int s = len / 8;
			l = med3(x, y, l, l + s, l + 2 * s);
			m = med3(x, y, m - s, m, m + s);
			n = med3(x, y, n - 2 * s, n - s, n);
			m = med3(x, y, l, m, n);
			final float v = x[m], w = y[m];
			// Establish Invariant: v* (<v)* (>v)* v*
			int a = from, b = a, c = to - 1, d = c;
			while (true) {
				int comparison, t;
				while (b <= c && (comparison = (t = (Float.compare((x[b]), (v)))) == 0 ? (Float.compare((y[b]), (w))) : t) <= 0) {
					if (comparison == 0) swap(x, y, a++, b);
					b++;
				}
				while (c >= b && (comparison = (t = (Float.compare((x[c]), (v)))) == 0 ? (Float.compare((y[c]), (w))) : t) >= 0) {
					if (comparison == 0) swap(x, y, c, d--);
					c--;
				}
				if (b > c) break;
				swap(x, y, b++, c--);
			}
			// Swap partition elements back to middle
			int t;
			s = Math.min(a - from, b - a);
			swap(x, y, from, b - s, s);
			s = Math.min(d - c, to - d - 1);
			swap(x, y, b, to - s, s);
			s = b - a;
			t = d - c;
			// Recursively sort non-partition-elements
			if (s > 1 && t > 1) invokeAll(new ForkJoinQuickSort2(x, y, from, from + s), new ForkJoinQuickSort2(x, y, to - t, to));
			else if (s > 1) invokeAll(new ForkJoinQuickSort2(x, y, from, from + s));
			else invokeAll(new ForkJoinQuickSort2(x, y, to - t, to));
		}
	}

	/**
	 * Sorts the specified range of elements of two arrays according to the natural lexicographical
	 * ascending order using a parallel quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of elements in
	 * the same position in the two provided arrays will be considered a single key, and permuted
	 * accordingly. In the end, either {@code x[i] &lt; x[i + 1]} or {@code x[i]
	 * == x[i + 1]} and {@code y[i] &le; y[i + 1]}.
	 *
	 * @param x the first array to be sorted.
	 * @param y the second array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void parallelQuickSort(final float[] x, final float[] y, final int from, final int to) {
		ForkJoinPool pool = getPool();
		if (to - from < PARALLEL_QUICKSORT_NO_FORK || pool.getParallelism() == 1) quickSort(x, y, from, to);
		else {
			pool.invoke(new ForkJoinQuickSort2(x, y, from, to));
		}
	}

	/**
	 * Sorts two arrays according to the natural lexicographical ascending order using a parallel
	 * quicksort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas McIlroy,
	 * &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11),
	 * pages 1249&minus;1265, 1993.
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of elements in
	 * the same position in the two provided arrays will be considered a single key, and permuted
	 * accordingly. In the end, either {@code x[i] &lt; x[i + 1]} or {@code x[i]
	 * == x[i + 1]} and {@code y[i] &le; y[i + 1]}.
	 *
	 * @param x the first array to be sorted.
	 * @param y the second array to be sorted.
	 */
	public static void parallelQuickSort(final float[] x, final float[] y) {
		ensureSameLength(x, y);
		parallelQuickSort(x, y, 0, x.length);
	}

	/**
	 * Sorts an array according to the natural ascending order, potentially dynamically choosing an
	 * appropriate algorithm given the type and size of the array. The sort will be stable unless it is
	 * provable that it would be impossible for there to be any difference between a stable and unstable
	 * sort for the given type, in which case stability is meaningless and thus unspecified.
	 *
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @since 8.3.0
	 */
	public static void unstableSort(final float[] a, final int from, final int to) {
		// TODO For some TBD threshold, delegate to java.util.Arrays.sort if under it.
		if (to - from >= RADIX_SORT_MIN_THRESHOLD) {
			radixSort(a, from, to);
		} else {
			quickSort(a, from, to);
		}
	}

	/**
	 * Sorts the specified range of elements according to the natural ascending order potentially
	 * dynamically choosing an appropriate algorithm given the type and size of the array. No assurance
	 * is made of the stability of the sort.
	 *
	 * @param a the array to be sorted.
	 * @since 8.3.0
	 */
	public static void unstableSort(final float[] a) {
		unstableSort(a, 0, a.length);
	}

	/**
	 * Sorts the specified range of elements according to the order induced by the specified comparator,
	 * potentially dynamically choosing an appropriate algorithm given the type and size of the array.
	 * No assurance is made of the stability of the sort.
	 *
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 * @since 8.3.0
	 */
	public static void unstableSort(final float[] a, final int from, final int to, FloatComparator comp) {
		quickSort(a, from, to, comp);
	}

	/**
	 * Sorts an array according to the order induced by the specified comparator, potentially
	 * dynamically choosing an appropriate algorithm given the type and size of the array. No assurance
	 * is made of the stability of the sort.
	 *
	 * @param a the array to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 * @since 8.3.0
	 */
	public static void unstableSort(final float[] a, FloatComparator comp) {
		unstableSort(a, 0, a.length, comp);
	}

	/**
	 * Sorts the specified range of elements according to the natural ascending order using mergesort,
	 * using a given pre-filled support array.
	 *
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of
	 * the sort. Moreover, no support arrays will be allocated.
	 *
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param supp a support array containing at least {@code to} elements, and whose entries are
	 *            identical to those of {@code a} in the specified range. It can be {@code null}, in
	 *            which case {@code a} will be cloned.
	 */

	public static void mergeSort(final float[] a, final int from, final int to, float[] supp) {
		int len = to - from;
		// Insertion sort on smallest arrays
		if (len < MERGESORT_NO_REC) {
			insertionSort(a, from, to);
			return;
		}
		if (supp == null) supp = java.util.Arrays.copyOf(a, to);
		// Recursively sort halves of a into supp
		final int mid = (from + to) >>> 1;
		mergeSort(supp, from, mid, a);
		mergeSort(supp, mid, to, a);
		// If list is already sorted, just copy from supp to a. This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if ((Float.compare((supp[mid - 1]), (supp[mid])) <= 0)) {
			System.arraycopy(supp, from, a, from, len);
			return;
		}
		// Merge sorted halves (now in supp) into a
		for (int i = from, p = from, q = mid; i < to; i++) {
			if (q >= to || p < mid && (Float.compare((supp[p]), (supp[q])) <= 0)) a[i] = supp[p++];
			else a[i] = supp[q++];
		}
	}

	/**
	 * Sorts the specified range of elements according to the natural ascending order using mergesort.
	 *
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of
	 * the sort. An array as large as {@code a} will be allocated by this method.
	 * 
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void mergeSort(final float[] a, final int from, final int to) {
		mergeSort(a, from, to, (float[])null);
	}

	/**
	 * Sorts an array according to the natural ascending order using mergesort.
	 *
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of
	 * the sort. An array as large as {@code a} will be allocated by this method.
	 * 
	 * @param a the array to be sorted.
	 */
	public static void mergeSort(final float[] a) {
		mergeSort(a, 0, a.length);
	}

	/**
	 * Sorts the specified range of elements according to the order induced by the specified comparator
	 * using mergesort, using a given pre-filled support array.
	 *
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of
	 * the sort. Moreover, no support arrays will be allocated.
	 * 
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 * @param supp a support array containing at least {@code to} elements, and whose entries are
	 *            identical to those of {@code a} in the specified range. It can be {@code null}, in
	 *            which case {@code a} will be cloned.
	 */
	public static void mergeSort(final float[] a, final int from, final int to, FloatComparator comp, float[] supp) {
		int len = to - from;
		// Insertion sort on smallest arrays
		if (len < MERGESORT_NO_REC) {
			insertionSort(a, from, to, comp);
			return;
		}
		if (supp == null) supp = java.util.Arrays.copyOf(a, to);
		// Recursively sort halves of a into supp
		final int mid = (from + to) >>> 1;
		mergeSort(supp, from, mid, comp, a);
		mergeSort(supp, mid, to, comp, a);
		// If list is already sorted, just copy from supp to a. This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (comp.compare(supp[mid - 1], supp[mid]) <= 0) {
			System.arraycopy(supp, from, a, from, len);
			return;
		}
		// Merge sorted halves (now in supp) into a
		for (int i = from, p = from, q = mid; i < to; i++) {
			if (q >= to || p < mid && comp.compare(supp[p], supp[q]) <= 0) a[i] = supp[p++];
			else a[i] = supp[q++];
		}
	}

	/**
	 * Sorts the specified range of elements according to the order induced by the specified comparator
	 * using mergesort.
	 *
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of
	 * the sort. An array as large as {@code a} will be allocated by this method.
	 *
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 */
	public static void mergeSort(final float[] a, final int from, final int to, FloatComparator comp) {
		mergeSort(a, from, to, comp, (float[])null);
	}

	/**
	 * Sorts an array according to the order induced by the specified comparator using mergesort.
	 *
	 * <p>
	 * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of
	 * the sort. An array as large as {@code a} will be allocated by this method.
	 * 
	 * @param a the array to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 */
	public static void mergeSort(final float[] a, FloatComparator comp) {
		mergeSort(a, 0, a.length, comp);
	}

	/**
	 * Sorts an array according to the natural ascending order, potentially dynamically choosing an
	 * appropriate algorithm given the type and size of the array. The sort will be stable unless it is
	 * provable that it would be impossible for there to be any difference between a stable and unstable
	 * sort for the given type, in which case stability is meaningless and thus unspecified.
	 *
	 * <p>
	 * An array as large as {@code a} may be allocated by this method.
	 *
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @since 8.3.0
	 */
	public static void stableSort(final float[] a, final int from, final int to) {
		// Due to subtle differences between Float/Double.compare and operator compare, it is
		// not safe to delegate this to java.util.Arrays.sort(double[], int, int)
		mergeSort(a, from, to);
	}

	/**
	 * Sorts the specified range of elements according to the natural ascending order potentially
	 * dynamically choosing an appropriate algorithm given the type and size of the array. The sort will
	 * be stable unless it is provable that it would be impossible for there to be any difference
	 * between a stable and unstable sort for the given type, in which case stability is meaningless and
	 * thus unspecified.
	 *
	 * <p>
	 * An array as large as {@code a} may be allocated by this method.
	 *
	 * @param a the array to be sorted.
	 * @since 8.3.0
	 */
	public static void stableSort(final float[] a) {
		stableSort(a, 0, a.length);
	}

	/**
	 * Sorts the specified range of elements according to the order induced by the specified comparator,
	 * potentially dynamically choosing an appropriate algorithm given the type and size of the array.
	 * The sort will be stable unless it is provable that it would be impossible for there to be any
	 * difference between a stable and unstable sort for the given type, in which case stability is
	 * meaningless and thus unspecified.
	 *
	 * <p>
	 * An array as large as {@code a} may be allocated by this method.
	 *
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 * @since 8.3.0
	 */
	public static void stableSort(final float[] a, final int from, final int to, FloatComparator comp) {
		mergeSort(a, from, to, comp);
	}

	/**
	 * Sorts an array according to the order induced by the specified comparator, potentially
	 * dynamically choosing an appropriate algorithm given the type and size of the array. The sort will
	 * be stable unless it is provable that it would be impossible for there to be any difference
	 * between a stable and unstable sort for the given type, in which case stability is meaningless and
	 * thus unspecified.
	 *
	 * <p>
	 * An array as large as {@code a} may be allocated by this method.
	 *
	 * @param a the array to be sorted.
	 * @param comp the comparator to determine the sorting order.
	 * @since 8.3.0
	 */
	public static void stableSort(final float[] a, FloatComparator comp) {
		stableSort(a, 0, a.length, comp);
	}

	/**
	 * Searches a range of the specified array for the specified value using the binary search
	 * algorithm. The range must be sorted prior to making this call. If it is not sorted, the results
	 * are undefined. If the range contains multiple elements with the specified value, there is no
	 * guarantee which one will be found.
	 *
	 * @param a the array to be searched.
	 * @param from the index of the first element (inclusive) to be searched.
	 * @param to the index of the last element (exclusive) to be searched.
	 * @param key the value to be searched for.
	 * @return index of the search key, if it is contained in the array; otherwise,
	 *         {@code (-(<i>insertion point</i>) - 1)}. The <i>insertion point</i> is defined as the the
	 *         point at which the value would be inserted into the array: the index of the first element
	 *         greater than the key, or the length of the array, if all elements in the array are less
	 *         than the specified key. Note that this guarantees that the return value will be &ge; 0 if
	 *         and only if the key is found.
	 * @see java.util.Arrays
	 */

	public static int binarySearch(final float[] a, int from, int to, final float key) {
		float midVal;
		to--;
		while (from <= to) {
			final int mid = (from + to) >>> 1;
			midVal = a[mid];
			if (midVal < key) from = mid + 1;
			else if (midVal > key) to = mid - 1;
			else return mid;
		}
		return -(from + 1);
	}

	/**
	 * Searches an array for the specified value using the binary search algorithm. The range must be
	 * sorted prior to making this call. If it is not sorted, the results are undefined. If the range
	 * contains multiple elements with the specified value, there is no guarantee which one will be
	 * found.
	 *
	 * @param a the array to be searched.
	 * @param key the value to be searched for.
	 * @return index of the search key, if it is contained in the array; otherwise,
	 *         {@code (-(<i>insertion point</i>) - 1)}. The <i>insertion point</i> is defined as the the
	 *         point at which the value would be inserted into the array: the index of the first element
	 *         greater than the key, or the length of the array, if all elements in the array are less
	 *         than the specified key. Note that this guarantees that the return value will be &ge; 0 if
	 *         and only if the key is found.
	 * @see java.util.Arrays
	 */
	public static int binarySearch(final float[] a, final float key) {
		return binarySearch(a, 0, a.length, key);
	}

	/**
	 * Searches a range of the specified array for the specified value using the binary search algorithm
	 * and a specified comparator. The range must be sorted following the comparator prior to making
	 * this call. If it is not sorted, the results are undefined. If the range contains multiple
	 * elements with the specified value, there is no guarantee which one will be found.
	 *
	 * @param a the array to be searched.
	 * @param from the index of the first element (inclusive) to be searched.
	 * @param to the index of the last element (exclusive) to be searched.
	 * @param key the value to be searched for.
	 * @param c a comparator.
	 * @return index of the search key, if it is contained in the array; otherwise,
	 *         {@code (-(<i>insertion point</i>) - 1)}. The <i>insertion point</i> is defined as the the
	 *         point at which the value would be inserted into the array: the index of the first element
	 *         greater than the key, or the length of the array, if all elements in the array are less
	 *         than the specified key. Note that this guarantees that the return value will be &ge; 0 if
	 *         and only if the key is found.
	 * @see java.util.Arrays
	 */
	public static int binarySearch(final float[] a, int from, int to, final float key, final FloatComparator c) {
		float midVal;
		to--;
		while (from <= to) {
			final int mid = (from + to) >>> 1;
			midVal = a[mid];
			final int cmp = c.compare(midVal, key);
			if (cmp < 0) from = mid + 1;
			else if (cmp > 0) to = mid - 1;
			else return mid; // key found
		}
		return -(from + 1);
	}

	/**
	 * Searches an array for the specified value using the binary search algorithm and a specified
	 * comparator. The range must be sorted following the comparator prior to making this call. If it is
	 * not sorted, the results are undefined. If the range contains multiple elements with the specified
	 * value, there is no guarantee which one will be found.
	 *
	 * @param a the array to be searched.
	 * @param key the value to be searched for.
	 * @param c a comparator.
	 * @return index of the search key, if it is contained in the array; otherwise,
	 *         {@code (-(<i>insertion point</i>) - 1)}. The <i>insertion point</i> is defined as the the
	 *         point at which the value would be inserted into the array: the index of the first element
	 *         greater than the key, or the length of the array, if all elements in the array are less
	 *         than the specified key. Note that this guarantees that the return value will be &ge; 0 if
	 *         and only if the key is found.
	 * @see java.util.Arrays
	 */
	public static int binarySearch(final float[] a, final float key, final FloatComparator c) {
		return binarySearch(a, 0, a.length, key, c);
	}

	/** The size of a digit used during radix sort (must be a power of 2). */
	public static final int DIGIT_BITS = 8;
	/** The mask to extract a digit of {@link #DIGIT_BITS} bits. */
	public static final int DIGIT_MASK = (1 << DIGIT_BITS) - 1;
	/** The number of digits per element. */
	public static final int DIGITS_PER_ELEMENT = Float.SIZE / DIGIT_BITS;
	public static final int RADIXSORT_NO_REC = 1024;
	private static final int RADIXSORT_NO_REC_SMALL = 64;
	private static final int PARALLEL_RADIXSORT_NO_FORK = 1024;
	// The thresholds were determined on an Intel i7 8700K.
	/** Threshold <em>hint</em> for using a radix sort vs a comparison based sort. */
	static final int RADIX_SORT_MIN_THRESHOLD = 2000;

	/**
	 * This method fixes negative numbers so that the combination exponent/significand is
	 * lexicographically sorted.
	 */
	public static int fixFloat(final float f) {
		//TODO: if using this to sort general-use float arrays, make this use the commented block!
		return Float.floatToIntBits(f);
//		final int i = Float.floatToIntBits(f);
//		return i ^ (i >> 31 & 0x7FFFFFFF);
	}

	/**
	 * Sorts the specified array using radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * @implSpec This implementation is significantly faster than quicksort already at small sizes (say,
	 *           more than 5000 elements), but it can only sort in ascending order.
	 *
	 * @param a the array to be sorted.
	 */
	public static void radixSort(final float[] a) {
		radixSort(a, 0, a.length);
	}

	/**
	 * Sorts the specified range of an array using radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * @implSpec This implementation is significantly faster than quicksort already at small sizes (say,
	 *           more than 5000 elements), but it can only sort in ascending order.
	 *
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void radixSort(final float[] a, final int from, final int to) {
		if (to - from < RADIXSORT_NO_REC) {
			quickSort(a, from, to);
			return;
		}
		final int maxLevel = DIGITS_PER_ELEMENT - 1;
		final int stackSize = ((1 << DIGIT_BITS) - 1) * (DIGITS_PER_ELEMENT - 1) + 1;
		int stackPos = 0;
		final int[] offsetStack = new int[stackSize];
		final int[] lengthStack = new int[stackSize];
		final int[] levelStack = new int[stackSize];
		offsetStack[stackPos] = from;
		lengthStack[stackPos] = to - from;
		levelStack[stackPos++] = 0;
		final int[] count = new int[1 << DIGIT_BITS];
		final int[] pos = new int[1 << DIGIT_BITS];
		while (stackPos > 0) {
			final int first = offsetStack[--stackPos];
			final int length = lengthStack[stackPos];
			final int level = levelStack[stackPos];
			final int signMask = level % DIGITS_PER_ELEMENT == 0 ? 1 << DIGIT_BITS - 1 : 0;
			final int shift = (DIGITS_PER_ELEMENT - 1 - level % DIGITS_PER_ELEMENT) * DIGIT_BITS; // This is the shift
																									// that extract the
																									// right byte from a
																									// key
			// Count keys.
			for (int i = first + length; i-- != first;) count[(fixFloat(a[i]) >>> shift & DIGIT_MASK ^ signMask)]++;
			// Compute cumulative distribution
			int lastUsed = -1;
			for (int i = 0, p = first; i < 1 << DIGIT_BITS; i++) {
				if (count[i] != 0) lastUsed = i;
				pos[i] = (p += count[i]);
			}
			final int end = first + length - count[lastUsed];
			// i moves through the start of each block
			for (int i = first, c = -1, d; i <= end; i += count[c], count[c] = 0) {
				float t = a[i];
				c = (fixFloat(t) >>> shift & DIGIT_MASK ^ signMask);
				if (i < end) { // When all slots are OK, the last slot is necessarily OK.
					while ((d = --pos[c]) > i) {
						final float z = t;
						t = a[d];
						a[d] = z;
						c = (fixFloat(t) >>> shift & DIGIT_MASK ^ signMask);
					}
					a[i] = t;
				}
				if (level < maxLevel && count[c] > 1) {
					if (count[c] < RADIXSORT_NO_REC) quickSort(a, i, i + count[c]);
					else {
						offsetStack[stackPos] = i;
						lengthStack[stackPos] = count[c];
						levelStack[stackPos++] = level + 1;
					}
				}
			}
		}
	}

	protected static final class Segment {
		private final int offset, length, level;

		private Segment(final int offset, final int length, final int level) {
			this.offset = offset;
			this.length = length;
			this.level = level;
		}

		@Override
		public String toString() {
			return "Segment [offset=" + offset + ", length=" + length + ", level=" + level + "]";
		}
	}

	private static final Segment POISON_PILL = new Segment(-1, -1, -1);

	/**
	 * Sorts the specified range of an array using parallel radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void parallelRadixSort(final float[] a, final int from, final int to) {
		ForkJoinPool pool = getPool();
		if (to - from < PARALLEL_RADIXSORT_NO_FORK || pool.getParallelism() == 1) {
			quickSort(a, from, to);
			return;
		}
		final int maxLevel = DIGITS_PER_ELEMENT - 1;
		final LinkedBlockingQueue<Segment> queue = new LinkedBlockingQueue<>();
		queue.add(new Segment(from, to - from, 0));
		final AtomicInteger queueSize = new AtomicInteger(1);
		final int numberOfThreads = pool.getParallelism();
		final ExecutorCompletionService<Void> executorCompletionService = new ExecutorCompletionService<>(pool);
		for (int j = numberOfThreads; j-- != 0;) executorCompletionService.submit(() -> {
			final int[] count = new int[1 << DIGIT_BITS];
			final int[] pos = new int[1 << DIGIT_BITS];
			for (;;) {
				if (queueSize.get() == 0) for (int i = numberOfThreads; i-- != 0;) queue.add(POISON_PILL);
				final Segment segment = queue.take();
				if (segment == POISON_PILL) return null;
				final int first = segment.offset;
				final int length = segment.length;
				final int level = segment.level;
				final int signMask = level % DIGITS_PER_ELEMENT == 0 ? 1 << DIGIT_BITS - 1 : 0;
				final int shift = (DIGITS_PER_ELEMENT - 1 - level % DIGITS_PER_ELEMENT) * DIGIT_BITS; // This is the
																										// shift that
																										// extract the
																										// right byte
																										// from a key
				// Count keys.
				for (int i = first + length; i-- != first;) count[(fixFloat(a[i]) >>> shift & DIGIT_MASK ^ signMask)]++;
				// Compute cumulative distribution
				int lastUsed = -1;
				for (int i = 0, p = first; i < 1 << DIGIT_BITS; i++) {
					if (count[i] != 0) lastUsed = i;
					pos[i] = (p += count[i]);
				}
				final int end = first + length - count[lastUsed];
				// i moves through the start of each block
				for (int i = first, c = -1, d; i <= end; i += count[c], count[c] = 0) {
					float t = a[i];
					c = (fixFloat(t) >>> shift & DIGIT_MASK ^ signMask);
					if (i < end) {
						while ((d = --pos[c]) > i) {
							final float z = t;
							t = a[d];
							a[d] = z;
							c = (fixFloat(t) >>> shift & DIGIT_MASK ^ signMask);
						}
						a[i] = t;
					}
					if (level < maxLevel && count[c] > 1) {
						if (count[c] < PARALLEL_RADIXSORT_NO_FORK) quickSort(a, i, i + count[c]);
						else {
							queueSize.incrementAndGet();
							queue.add(new Segment(i, count[c], level + 1));
						}
					}
				}
				queueSize.decrementAndGet();
			}
		});
		Throwable problem = null;
		for (int i = numberOfThreads; i-- != 0;) try {
			executorCompletionService.take().get();
		} catch (Exception e) {
			problem = e.getCause(); // We keep only the last one. They will be logged anyway.
		}
		if (problem != null) throw (problem instanceof RuntimeException) ? (RuntimeException)problem : new RuntimeException(problem);
	}

	/**
	 * Sorts the specified array using parallel radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * @param a the array to be sorted.
	 */
	public static void parallelRadixSort(final float[] a) {
		parallelRadixSort(a, 0, a.length);
	}

	/**
	 * Sorts the specified array using indirect radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code a[perm[i]] &le; a[perm[i + 1]]}.
	 *
	 * @implSpec This implementation will allocate, in the stable case, a support array as large as
	 *           {@code perm} (note that the stable version is slightly faster).
	 *
	 * @param perm a permutation array indexing {@code a}.
	 * @param a the array to be sorted.
	 * @param stable whether the sorting algorithm should be stable.
	 */
	public static void radixSortIndirect(final int[] perm, final float[] a, final boolean stable) {
		radixSortIndirect(perm, a, 0, perm.length, stable);
	}

	/**
	 * Sorts the specified array using indirect radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code a[perm[i]] &le; a[perm[i + 1]]}.
	 *
	 * @implSpec This implementation will allocate, in the stable case, a support array as large as
	 *           {@code perm} (note that the stable version is slightly faster).
	 *
	 * @param perm a permutation array indexing {@code a}.
	 * @param a the array to be sorted.
	 * @param from the index of the first element of {@code perm} (inclusive) to be permuted.
	 * @param to the index of the last element of {@code perm} (exclusive) to be permuted.
	 * @param stable whether the sorting algorithm should be stable.
	 */
	public static void radixSortIndirect(final int[] perm, final float[] a, final int from, final int to, final boolean stable) {
		if (to - from < RADIXSORT_NO_REC) {
			quickSortIndirect(perm, a, from, to);
			if (stable) stabilize(perm, a, from, to);
			return;
		}
		final int maxLevel = DIGITS_PER_ELEMENT - 1;
		final int stackSize = ((1 << DIGIT_BITS) - 1) * (DIGITS_PER_ELEMENT - 1) + 1;
		int stackPos = 0;
		final int[] offsetStack = new int[stackSize];
		final int[] lengthStack = new int[stackSize];
		final int[] levelStack = new int[stackSize];
		offsetStack[stackPos] = from;
		lengthStack[stackPos] = to - from;
		levelStack[stackPos++] = 0;
		final int[] count = new int[1 << DIGIT_BITS];
		final int[] pos = new int[1 << DIGIT_BITS];
		final int[] support = stable ? new int[perm.length] : null;
		while (stackPos > 0) {
			final int first = offsetStack[--stackPos];
			final int length = lengthStack[stackPos];
			final int level = levelStack[stackPos];
			final int signMask = level % DIGITS_PER_ELEMENT == 0 ? 1 << DIGIT_BITS - 1 : 0;
			final int shift = (DIGITS_PER_ELEMENT - 1 - level % DIGITS_PER_ELEMENT) * DIGIT_BITS; // This is the shift
																									// that extract the
																									// right byte from a
																									// key
			// Count keys.
			for (int i = first + length; i-- != first;) count[(fixFloat(a[perm[i]]) >>> shift & DIGIT_MASK ^ signMask)]++;
			// Compute cumulative distribution
			int lastUsed = -1;
			for (int i = 0, p = stable ? 0 : first; i < 1 << DIGIT_BITS; i++) {
				if (count[i] != 0) lastUsed = i;
				pos[i] = (p += count[i]);
			}
			if (stable) {
				for (int i = first + length; i-- != first;) support[--pos[(fixFloat(a[perm[i]]) >>> shift & DIGIT_MASK ^ signMask)]] = perm[i];
				System.arraycopy(support, 0, perm, first, length);
				for (int i = 0, p = first; i <= lastUsed; i++) {
					if (level < maxLevel && count[i] > 1) {
						if (count[i] < RADIXSORT_NO_REC) {
							quickSortIndirect(perm, a, p, p + count[i]);
							if (stable) stabilize(perm, a, p, p + count[i]);
						} else {
							offsetStack[stackPos] = p;
							lengthStack[stackPos] = count[i];
							levelStack[stackPos++] = level + 1;
						}
					}
					p += count[i];
				}
				java.util.Arrays.fill(count, 0);
			} else {
				final int end = first + length - count[lastUsed];
				// i moves through the start of each block
				for (int i = first, c = -1, d; i <= end; i += count[c], count[c] = 0) {
					int t = perm[i];
					c = (fixFloat(a[t]) >>> shift & DIGIT_MASK ^ signMask);
					if (i < end) { // When all slots are OK, the last slot is necessarily OK.
						while ((d = --pos[c]) > i) {
							final int z = t;
							t = perm[d];
							perm[d] = z;
							c = (fixFloat(a[t]) >>> shift & DIGIT_MASK ^ signMask);
						}
						perm[i] = t;
					}
					if (level < maxLevel && count[c] > 1) {
						if (count[c] < RADIXSORT_NO_REC) {
							quickSortIndirect(perm, a, i, i + count[c]);
							if (stable) stabilize(perm, a, i, i + count[c]);
						} else {
							offsetStack[stackPos] = i;
							lengthStack[stackPos] = count[c];
							levelStack[stackPos++] = level + 1;
						}
					}
				}
			}
		}
	}

	/**
	 * Sorts the specified range of an array using parallel indirect radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code a[perm[i]] &le; a[perm[i + 1]]}.
	 *
	 * @param perm a permutation array indexing {@code a}.
	 * @param a the array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param stable whether the sorting algorithm should be stable.
	 */
	public static void parallelRadixSortIndirect(final int[] perm, final float[] a, final int from, final int to, final boolean stable) {
		ForkJoinPool pool = getPool();
		if (to - from < PARALLEL_RADIXSORT_NO_FORK || pool.getParallelism() == 1) {
			radixSortIndirect(perm, a, from, to, stable);
			return;
		}
		final int maxLevel = DIGITS_PER_ELEMENT - 1;
		final LinkedBlockingQueue<Segment> queue = new LinkedBlockingQueue<>();
		queue.add(new Segment(from, to - from, 0));
		final AtomicInteger queueSize = new AtomicInteger(1);
		final int numberOfThreads = pool.getParallelism();
		final ExecutorCompletionService<Void> executorCompletionService = new ExecutorCompletionService<>(pool);
		final int[] support = stable ? new int[perm.length] : null;
		for (int j = numberOfThreads; j-- != 0;) executorCompletionService.submit(() -> {
			final int[] count = new int[1 << DIGIT_BITS];
			final int[] pos = new int[1 << DIGIT_BITS];
			for (;;) {
				if (queueSize.get() == 0) for (int i = numberOfThreads; i-- != 0;) queue.add(POISON_PILL);
				final Segment segment = queue.take();
				if (segment == POISON_PILL) return null;
				final int first = segment.offset;
				final int length = segment.length;
				final int level = segment.level;
				final int signMask = level % DIGITS_PER_ELEMENT == 0 ? 1 << DIGIT_BITS - 1 : 0;
				final int shift = (DIGITS_PER_ELEMENT - 1 - level % DIGITS_PER_ELEMENT) * DIGIT_BITS; // This is the
																										// shift that
																										// extract the
																										// right byte
																										// from a key
				// Count keys.
				for (int i = first + length; i-- != first;) count[(fixFloat(a[perm[i]]) >>> shift & DIGIT_MASK ^ signMask)]++;
				// Compute cumulative distribution
				int lastUsed = -1;
				for (int i = 0, p = first; i < 1 << DIGIT_BITS; i++) {
					if (count[i] != 0) lastUsed = i;
					pos[i] = (p += count[i]);
				}
				if (stable) {
					for (int i = first + length; i-- != first;) support[--pos[(fixFloat(a[perm[i]]) >>> shift & DIGIT_MASK ^ signMask)]] = perm[i];
					System.arraycopy(support, first, perm, first, length);
					for (int i = 0, p = first; i <= lastUsed; i++) {
						if (level < maxLevel && count[i] > 1) {
							if (count[i] < PARALLEL_RADIXSORT_NO_FORK) radixSortIndirect(perm, a, p, p + count[i], stable);
							else {
								queueSize.incrementAndGet();
								queue.add(new Segment(p, count[i], level + 1));
							}
						}
						p += count[i];
					}
					java.util.Arrays.fill(count, 0);
				} else {
					final int end = first + length - count[lastUsed];
					// i moves through the start of each block
					for (int i = first, c = -1, d; i <= end; i += count[c], count[c] = 0) {
						int t = perm[i];
						c = (fixFloat(a[t]) >>> shift & DIGIT_MASK ^ signMask);
						if (i < end) { // When all slots are OK, the last slot is necessarily OK.
							while ((d = --pos[c]) > i) {
								final int z = t;
								t = perm[d];
								perm[d] = z;
								c = (fixFloat(a[t]) >>> shift & DIGIT_MASK ^ signMask);
							}
							perm[i] = t;
						}
						if (level < maxLevel && count[c] > 1) {
							if (count[c] < PARALLEL_RADIXSORT_NO_FORK) radixSortIndirect(perm, a, i, i + count[c], stable);
							else {
								queueSize.incrementAndGet();
								queue.add(new Segment(i, count[c], level + 1));
							}
						}
					}
				}
				queueSize.decrementAndGet();
			}
		});
		Throwable problem = null;
		for (int i = numberOfThreads; i-- != 0;) try {
			executorCompletionService.take().get();
		} catch (Exception e) {
			problem = e.getCause(); // We keep only the last one. They will be logged anyway.
		}
		if (problem != null) throw (problem instanceof RuntimeException) ? (RuntimeException)problem : new RuntimeException(problem);
	}

	/**
	 * Sorts the specified array using parallel indirect radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code a[perm[i]] &le; a[perm[i + 1]]}.
	 *
	 * @param perm a permutation array indexing {@code a}.
	 * @param a the array to be sorted.
	 * @param stable whether the sorting algorithm should be stable.
	 */
	public static void parallelRadixSortIndirect(final int[] perm, final float[] a, final boolean stable) {
		parallelRadixSortIndirect(perm, a, 0, a.length, stable);
	}

	/**
	 * Sorts the specified pair of arrays lexicographically using radix sort.
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of elements in
	 * the same position in the two provided arrays will be considered a single key, and permuted
	 * accordingly. In the end, either {@code a[i] &lt; a[i + 1]} or {@code a[i] == a[i + 1]} and
	 * {@code b[i] &le; b[i + 1]}.
	 *
	 * @param a the first array to be sorted.
	 * @param b the second array to be sorted.
	 */
	public static void radixSort(final float[] a, final float[] b) {
		ensureSameLength(a, b);
		radixSort(a, b, 0, a.length);
	}

	/**
	 * Sorts the specified range of elements of two arrays using radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of elements in
	 * the same position in the two provided arrays will be considered a single key, and permuted
	 * accordingly. In the end, either {@code a[i] &lt; a[i + 1]} or {@code a[i] == a[i + 1]} and
	 * {@code b[i] &le; b[i + 1]}.
	 *
	 * @param a the first array to be sorted.
	 * @param b the second array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void radixSort(final float[] a, final float[] b, final int from, final int to) {
		if (to - from < RADIXSORT_NO_REC) {
			quickSort(a, b, from, to);
			return;
		}
		final int layers = 2;
		final int maxLevel = DIGITS_PER_ELEMENT * layers - 1;
		final int stackSize = ((1 << DIGIT_BITS) - 1) * (layers * DIGITS_PER_ELEMENT - 1) + 1;
		int stackPos = 0;
		final int[] offsetStack = new int[stackSize];
		final int[] lengthStack = new int[stackSize];
		final int[] levelStack = new int[stackSize];
		offsetStack[stackPos] = from;
		lengthStack[stackPos] = to - from;
		levelStack[stackPos++] = 0;
		final int[] count = new int[1 << DIGIT_BITS];
		final int[] pos = new int[1 << DIGIT_BITS];
		while (stackPos > 0) {
			final int first = offsetStack[--stackPos];
			final int length = lengthStack[stackPos];
			final int level = levelStack[stackPos];
			final int signMask = level % DIGITS_PER_ELEMENT == 0 ? 1 << DIGIT_BITS - 1 : 0;
			final float[] k = level < DIGITS_PER_ELEMENT ? a : b; // This is the key array
			final int shift = (DIGITS_PER_ELEMENT - 1 - level % DIGITS_PER_ELEMENT) * DIGIT_BITS; // This is the shift
																									// that extract the
																									// right byte from a
																									// key
			// Count keys.
			for (int i = first + length; i-- != first;) count[(fixFloat(k[i]) >>> shift & DIGIT_MASK ^ signMask)]++;
			// Compute cumulative distribution
			int lastUsed = -1;
			for (int i = 0, p = first; i < 1 << DIGIT_BITS; i++) {
				if (count[i] != 0) lastUsed = i;
				pos[i] = (p += count[i]);
			}
			final int end = first + length - count[lastUsed];
			// i moves through the start of each block
			for (int i = first, c = -1, d; i <= end; i += count[c], count[c] = 0) {
				float t = a[i];
				float u = b[i];
				c = (fixFloat(k[i]) >>> shift & DIGIT_MASK ^ signMask);
				if (i < end) { // When all slots are OK, the last slot is necessarily OK.
					while ((d = --pos[c]) > i) {
						c = (fixFloat(k[d]) >>> shift & DIGIT_MASK ^ signMask);
						float z = t;
						t = a[d];
						a[d] = z;
						z = u;
						u = b[d];
						b[d] = z;
					}
					a[i] = t;
					b[i] = u;
				}
				if (level < maxLevel && count[c] > 1) {
					if (count[c] < RADIXSORT_NO_REC) quickSort(a, b, i, i + count[c]);
					else {
						offsetStack[stackPos] = i;
						lengthStack[stackPos] = count[c];
						levelStack[stackPos++] = level + 1;
					}
				}
			}
		}
	}

	/**
	 * Sorts the specified range of elements of two arrays using a parallel radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of elements in
	 * the same position in the two provided arrays will be considered a single key, and permuted
	 * accordingly. In the end, either {@code a[i] &lt; a[i + 1]} or {@code a[i] == a[i + 1]} and
	 * {@code b[i] &le; b[i + 1]}.
	 *
	 * @param a the first array to be sorted.
	 * @param b the second array to be sorted.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void parallelRadixSort(final float[] a, final float[] b, final int from, final int to) {
		ForkJoinPool pool = getPool();
		if (to - from < PARALLEL_RADIXSORT_NO_FORK || pool.getParallelism() == 1) {
			quickSort(a, b, from, to);
			return;
		}
		final int layers = 2;
		if (a.length != b.length) throw new IllegalArgumentException("Array size mismatch.");
		final int maxLevel = DIGITS_PER_ELEMENT * layers - 1;
		final LinkedBlockingQueue<Segment> queue = new LinkedBlockingQueue<>();
		queue.add(new Segment(from, to - from, 0));
		final AtomicInteger queueSize = new AtomicInteger(1);
		final int numberOfThreads = pool.getParallelism();
		final ExecutorCompletionService<Void> executorCompletionService = new ExecutorCompletionService<>(pool);
		for (int j = numberOfThreads; j-- != 0;) executorCompletionService.submit(() -> {
			final int[] count = new int[1 << DIGIT_BITS];
			final int[] pos = new int[1 << DIGIT_BITS];
			for (;;) {
				if (queueSize.get() == 0) for (int i = numberOfThreads; i-- != 0;) queue.add(POISON_PILL);
				final Segment segment = queue.take();
				if (segment == POISON_PILL) return null;
				final int first = segment.offset;
				final int length = segment.length;
				final int level = segment.level;
				final int signMask = level % DIGITS_PER_ELEMENT == 0 ? 1 << DIGIT_BITS - 1 : 0;
				final float[] k = level < DIGITS_PER_ELEMENT ? a : b; // This is the key array
				final int shift = (DIGITS_PER_ELEMENT - 1 - level % DIGITS_PER_ELEMENT) * DIGIT_BITS;
				// Count keys.
				for (int i = first + length; i-- != first;) count[(fixFloat(k[i]) >>> shift & DIGIT_MASK ^ signMask)]++;
				// Compute cumulative distribution
				int lastUsed = -1;
				for (int i = 0, p = first; i < 1 << DIGIT_BITS; i++) {
					if (count[i] != 0) lastUsed = i;
					pos[i] = (p += count[i]);
				}
				final int end = first + length - count[lastUsed];
				for (int i = first, c = -1, d; i <= end; i += count[c], count[c] = 0) {
					float t = a[i];
					float u = b[i];
					c = (fixFloat(k[i]) >>> shift & DIGIT_MASK ^ signMask);
					if (i < end) { // When all slots are OK, the last slot is necessarily OK.
						while ((d = --pos[c]) > i) {
							c = (fixFloat(k[d]) >>> shift & DIGIT_MASK ^ signMask);
							final float z = t;
							final float w = u;
							t = a[d];
							u = b[d];
							a[d] = z;
							b[d] = w;
						}
						a[i] = t;
						b[i] = u;
					}
					if (level < maxLevel && count[c] > 1) {
						if (count[c] < PARALLEL_RADIXSORT_NO_FORK) quickSort(a, b, i, i + count[c]);
						else {
							queueSize.incrementAndGet();
							queue.add(new Segment(i, count[c], level + 1));
						}
					}
				}
				queueSize.decrementAndGet();
			}
		});
		Throwable problem = null;
		for (int i = numberOfThreads; i-- != 0;) try {
			executorCompletionService.take().get();
		} catch (Exception e) {
			problem = e.getCause(); // We keep only the last one. They will be logged anyway.
		}
		if (problem != null) throw (problem instanceof RuntimeException) ? (RuntimeException)problem : new RuntimeException(problem);
	}

	/**
	 * Sorts two arrays using a parallel radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of elements in
	 * the same position in the two provided arrays will be considered a single key, and permuted
	 * accordingly. In the end, either {@code a[i] &lt; a[i + 1]} or {@code a[i] == a[i + 1]} and
	 * {@code b[i] &le; b[i + 1]}.
	 *
	 * @param a the first array to be sorted.
	 * @param b the second array to be sorted.
	 */
	public static void parallelRadixSort(final float[] a, final float[] b) {
		ensureSameLength(a, b);
		parallelRadixSort(a, b, 0, a.length);
	}

	private static void insertionSortIndirect(final int[] perm, final float[] a, final float[] b, final int from, final int to) {
		for (int i = from; ++i < to;) {
			int t = perm[i];
			int j = i;
			for (int u = perm[j - 1]; (Float.compare((a[t]), (a[u])) < 0) || (Float.compare((a[t]), (a[u])) == 0) && (Float.compare((b[t]), (b[u])) < 0); u = perm[--j - 1]) {
				perm[j] = u;
				if (from == j - 1) {
					--j;
					break;
				}
			}
			perm[j] = t;
		}
	}

	/**
	 * Sorts the specified pair of arrays lexicographically using indirect radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code a[perm[i]] &le; a[perm[i + 1]]} or {@code a[perm[i]] == a[perm[i + 1]]} and
	 * {@code b[perm[i]] &le; b[perm[i + 1]]}.
	 *
	 * @implSpec This implementation will allocate, in the stable case, a further support array as large
	 *           as {@code perm} (note that the stable version is slightly faster).
	 *
	 * @param perm a permutation array indexing {@code a}.
	 * @param a the array to be sorted.
	 * @param b the second array to be sorted.
	 * @param stable whether the sorting algorithm should be stable.
	 */
	public static void radixSortIndirect(final int[] perm, final float[] a, final float[] b, final boolean stable) {
		ensureSameLength(a, b);
		radixSortIndirect(perm, a, b, 0, a.length, stable);
	}

	/**
	 * Sorts the specified pair of arrays lexicographically using indirect radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must be
	 * exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
	 * {@code a[perm[i]] &le; a[perm[i + 1]]} or {@code a[perm[i]] == a[perm[i + 1]]} and
	 * {@code b[perm[i]] &le; b[perm[i + 1]]}.
	 *
	 * @implSpec This implementation will allocate, in the stable case, a further support array as large
	 *           as {@code perm} (note that the stable version is slightly faster).
	 *
	 * @param perm a permutation array indexing {@code a}.
	 * @param a the array to be sorted.
	 * @param b the second array to be sorted.
	 * @param from the index of the first element of {@code perm} (inclusive) to be permuted.
	 * @param to the index of the last element of {@code perm} (exclusive) to be permuted.
	 * @param stable whether the sorting algorithm should be stable.
	 */
	public static void radixSortIndirect(final int[] perm, final float[] a, final float[] b, final int from, final int to, final boolean stable) {
		if (to - from < RADIXSORT_NO_REC_SMALL) {
			insertionSortIndirect(perm, a, b, from, to);
			return;
		}
		final int layers = 2;
		final int maxLevel = DIGITS_PER_ELEMENT * layers - 1;
		final int stackSize = ((1 << DIGIT_BITS) - 1) * (layers * DIGITS_PER_ELEMENT - 1) + 1;
		int stackPos = 0;
		final int[] offsetStack = new int[stackSize];
		final int[] lengthStack = new int[stackSize];
		final int[] levelStack = new int[stackSize];
		offsetStack[stackPos] = from;
		lengthStack[stackPos] = to - from;
		levelStack[stackPos++] = 0;
		final int[] count = new int[1 << DIGIT_BITS];
		final int[] pos = new int[1 << DIGIT_BITS];
		final int[] support = stable ? new int[perm.length] : null;
		while (stackPos > 0) {
			final int first = offsetStack[--stackPos];
			final int length = lengthStack[stackPos];
			final int level = levelStack[stackPos];
			final int signMask = level % DIGITS_PER_ELEMENT == 0 ? 1 << DIGIT_BITS - 1 : 0;
			final float[] k = level < DIGITS_PER_ELEMENT ? a : b; // This is the key array
			final int shift = (DIGITS_PER_ELEMENT - 1 - level % DIGITS_PER_ELEMENT) * DIGIT_BITS; // This is the shift
																									// that extract the
																									// right byte from a
																									// key
			// Count keys.
			for (int i = first + length; i-- != first;) count[(fixFloat(k[perm[i]]) >>> shift & DIGIT_MASK ^ signMask)]++;
			// Compute cumulative distribution
			int lastUsed = -1;
			for (int i = 0, p = stable ? 0 : first; i < 1 << DIGIT_BITS; i++) {
				if (count[i] != 0) lastUsed = i;
				pos[i] = (p += count[i]);
			}
			if (stable) {
				for (int i = first + length; i-- != first;) support[--pos[(fixFloat(k[perm[i]]) >>> shift & DIGIT_MASK ^ signMask)]] = perm[i];
				System.arraycopy(support, 0, perm, first, length);
				for (int i = 0, p = first; i < 1 << DIGIT_BITS; i++) {
					if (level < maxLevel && count[i] > 1) {
						if (count[i] < RADIXSORT_NO_REC_SMALL) insertionSortIndirect(perm, a, b, p, p + count[i]);
						else {
							offsetStack[stackPos] = p;
							lengthStack[stackPos] = count[i];
							levelStack[stackPos++] = level + 1;
						}
					}
					p += count[i];
				}
				java.util.Arrays.fill(count, 0);
			} else {
				final int end = first + length - count[lastUsed];
				// i moves through the start of each block
				for (int i = first, c = -1, d; i <= end; i += count[c], count[c] = 0) {
					int t = perm[i];
					c = (fixFloat(k[t]) >>> shift & DIGIT_MASK ^ signMask);
					if (i < end) { // When all slots are OK, the last slot is necessarily OK.
						while ((d = --pos[c]) > i) {
							final int z = t;
							t = perm[d];
							perm[d] = z;
							c = (fixFloat(k[t]) >>> shift & DIGIT_MASK ^ signMask);
						}
						perm[i] = t;
					}
					if (level < maxLevel && count[c] > 1) {
						if (count[c] < RADIXSORT_NO_REC_SMALL) insertionSortIndirect(perm, a, b, i, i + count[c]);
						else {
							offsetStack[stackPos] = i;
							lengthStack[stackPos] = count[c];
							levelStack[stackPos++] = level + 1;
						}
					}
				}
			}
		}
	}

	private static void selectionSort(final float[][] a, final int from, final int to, final int level) {
		final int layers = a.length;
		final int firstLayer = level / DIGITS_PER_ELEMENT;
		for (int i = from; i < to - 1; i++) {
			int m = i;
			for (int j = i + 1; j < to; j++) {
				for (int p = firstLayer; p < layers; p++) {
					if (a[p][j] < a[p][m]) {
						m = j;
						break;
					} else if (a[p][j] > a[p][m]) break;
				}
			}
			if (m != i) {
				for (int p = layers; p-- != 0;) {
					final float u = a[p][i];
					a[p][i] = a[p][m];
					a[p][m] = u;
				}
			}
		}
	}

	/**
	 * Sorts the specified array of arrays lexicographically using radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the provided arrays. Tuples of
	 * elements in the same position will be considered a single key, and permuted accordingly.
	 *
	 * @param a an array containing arrays of equal length to be sorted lexicographically in parallel.
	 */
	public static void radixSort(final float[][] a) {
		radixSort(a, 0, a[0].length);
	}

	/**
	 * Sorts the specified array of arrays lexicographically using radix sort.
	 *
	 * <p>
	 * The sorting algorithm is a tuned radix sort adapted from Peter M. McIlroy, Keith Bostic and M.
	 * Douglas McIlroy, &ldquo;Engineering radix sort&rdquo;, <i>Computing Systems</i>, 6(1), pages
	 * 5&minus;27 (1993).
	 *
	 * <p>
	 * This method implements a <em>lexicographical</em> sorting of the provided arrays. Tuples of
	 * elements in the same position will be considered a single key, and permuted accordingly.
	 *
	 * @param a an array containing arrays of equal length to be sorted lexicographically in parallel.
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 */
	public static void radixSort(final float[][] a, final int from, final int to) {
		if (to - from < RADIXSORT_NO_REC_SMALL) {
			selectionSort(a, from, to, 0);
			return;
		}
		final int layers = a.length;
		final int maxLevel = DIGITS_PER_ELEMENT * layers - 1;
		for (int p = layers,
				l = a[0].length; p-- != 0;) if (a[p].length != l) throw new IllegalArgumentException("The array of index " + p + " has not the same length of the array of index 0.");
		final int stackSize = ((1 << DIGIT_BITS) - 1) * (layers * DIGITS_PER_ELEMENT - 1) + 1;
		int stackPos = 0;
		final int[] offsetStack = new int[stackSize];
		final int[] lengthStack = new int[stackSize];
		final int[] levelStack = new int[stackSize];
		offsetStack[stackPos] = from;
		lengthStack[stackPos] = to - from;
		levelStack[stackPos++] = 0;
		final int[] count = new int[1 << DIGIT_BITS];
		final int[] pos = new int[1 << DIGIT_BITS];
		final float[] t = new float[layers];
		while (stackPos > 0) {
			final int first = offsetStack[--stackPos];
			final int length = lengthStack[stackPos];
			final int level = levelStack[stackPos];
			final int signMask = level % DIGITS_PER_ELEMENT == 0 ? 1 << DIGIT_BITS - 1 : 0;
			final float[] k = a[level / DIGITS_PER_ELEMENT]; // This is the key array
			final int shift = (DIGITS_PER_ELEMENT - 1 - level % DIGITS_PER_ELEMENT) * DIGIT_BITS; // This is the shift
																									// that extract the
																									// right byte from a
																									// key
			// Count keys.
			for (int i = first + length; i-- != first;) count[(fixFloat(k[i]) >>> shift & DIGIT_MASK ^ signMask)]++;
			// Compute cumulative distribution
			int lastUsed = -1;
			for (int i = 0, p = first; i < 1 << DIGIT_BITS; i++) {
				if (count[i] != 0) lastUsed = i;
				pos[i] = (p += count[i]);
			}
			final int end = first + length - count[lastUsed];
			// i moves through the start of each block
			for (int i = first, c = -1, d; i <= end; i += count[c], count[c] = 0) {
				for (int p = layers; p-- != 0;) t[p] = a[p][i];
				c = (fixFloat(k[i]) >>> shift & DIGIT_MASK ^ signMask);
				if (i < end) { // When all slots are OK, the last slot is necessarily OK.
					while ((d = --pos[c]) > i) {
						c = (fixFloat(k[d]) >>> shift & DIGIT_MASK ^ signMask);
						for (int p = layers; p-- != 0;) {
							final float u = t[p];
							t[p] = a[p][d];
							a[p][d] = u;
						}
					}
					for (int p = layers; p-- != 0;) a[p][i] = t[p];
				}
				if (level < maxLevel && count[c] > 1) {
					if (count[c] < RADIXSORT_NO_REC_SMALL) selectionSort(a, i, i + count[c], level + 1);
					else {
						offsetStack[stackPos] = i;
						lengthStack[stackPos] = count[c];
						levelStack[stackPos++] = level + 1;
					}
				}
			}
		}
	}

	/**
	 * Shuffles the specified array fragment using the specified pseudorandom number generator.
	 *
	 * @param a the array to be shuffled.
	 * @param from the index of the first element (inclusive) to be shuffled.
	 * @param to the index of the last element (exclusive) to be shuffled.
	 * @param random a pseudorandom number generator.
	 * @return {@code a}.
	 */
	public static float[] shuffle(final float[] a, final int from, final int to, final Random random) {
		for (int i = to - from; i-- != 0;) {
			final int p = random.nextInt(i + 1);
			final float t = a[from + i];
			a[from + i] = a[from + p];
			a[from + p] = t;
		}
		return a;
	}

	/**
	 * Shuffles the specified array using the specified pseudorandom number generator.
	 *
	 * @param a the array to be shuffled.
	 * @param random a pseudorandom number generator.
	 * @return {@code a}.
	 */
	public static float[] shuffle(final float[] a, final Random random) {
		for (int i = a.length; i-- != 0;) {
			final int p = random.nextInt(i + 1);
			final float t = a[i];
			a[i] = a[p];
			a[p] = t;
		}
		return a;
	}

	/**
	 * Reverses the order of the elements in the specified array.
	 *
	 * @param a the array to be reversed.
	 * @return {@code a}.
	 */
	public static float[] reverse(final float[] a) {
		final int length = a.length;
		for (int i = length / 2; i-- != 0;) {
			final float t = a[length - i - 1];
			a[length - i - 1] = a[i];
			a[i] = t;
		}
		return a;
	}

	/**
	 * Reverses the order of the elements in the specified array fragment.
	 *
	 * @param a the array to be reversed.
	 * @param from the index of the first element (inclusive) to be reversed.
	 * @param to the index of the last element (exclusive) to be reversed.
	 * @return {@code a}.
	 */
	public static float[] reverse(final float[] a, final int from, final int to) {
		final int length = to - from;
		for (int i = length / 2; i-- != 0;) {
			final float t = a[from + length - i - 1];
			a[from + length - i - 1] = a[from + i];
			a[from + i] = t;
		}
		return a;
	}

	/** A type-specific content-based hash strategy for arrays. */
	private static final class ArrayHashStrategy implements Hash.Strategy<float[]>, java.io.Serializable {
		private static final long serialVersionUID = -7046029254386353129L;

		@Override
		public int hashCode(final float[] o) {
			return java.util.Arrays.hashCode(o);
		}

		@Override
		public boolean equals(final float[] a, final float[] b) {
			return java.util.Arrays.equals(a, b);
		}
	}

	/**
	 * A type-specific content-based hash strategy for arrays.
	 *
	 * <p>
	 * This hash strategy may be used in custom hash collections whenever keys are arrays, and they must
	 * be considered equal by content. This strategy will handle {@code null} correctly, and it is
	 * serializable.
	 */
	public static final Hash.Strategy<float[]> HASH_STRATEGY = new ArrayHashStrategy();
}
