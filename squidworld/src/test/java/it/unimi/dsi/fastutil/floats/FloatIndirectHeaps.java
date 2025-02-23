/*
	* Copyright (C) 2003-2024 Paolo Boldi and Sebastiano Vigna
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
	*/
package it.unimi.dsi.fastutil.floats;

import com.github.tommyettinger.ds.support.sort.FloatComparator;

import java.util.Arrays;

/**
 * A class providing static methods and objects that do useful things with indirect heaps.
 *
 * <p>
 * An indirect heap is an extension of a semi-indirect heap using also an <em>inversion array</em>
 * of the same length as the reference array, satisfying the relation {@code heap[inv[i]]==i} when
 * {@code inv[i]&gt;=0}, and {@code inv[heap[i]]==i} for all elements in the heap.
 */
public final class FloatIndirectHeaps {
	private FloatIndirectHeaps() {
	}

	/**
	 * Moves the given element down into the indirect heap until it reaches the lowest possible
	 * position.
	 *
	 * @param refArray the reference array.
	 * @param heap the indirect heap (starting at 0).
	 * @param inv the inversion array.
	 * @param size the number of elements in the heap.
	 * @param i the index in the heap of the element to be moved down.
	 * @param c a type-specific comparator, or {@code null} for the natural order.
	 * @return the new position in the heap of the element of heap index {@code i}.
	 */

	public static int downHeap(final float[] refArray, final int[] heap, final int[] inv, final int size, int i, final FloatComparator c) {
		assert i < size;
		final int e = heap[i];
		final float E = refArray[e];
		int child;
		if (c == null) while ((child = (i << 1) + 1) < size) {
			int t = heap[child];
			final int right = child + 1;
			if (right < size && (Float.compare((refArray[heap[right]]), (refArray[t])) < 0)) t = heap[child = right];
			if ((Float.compare((E), (refArray[t])) <= 0)) break;
			heap[i] = t;
			inv[heap[i]] = i;
			i = child;
		}
		else while ((child = (i << 1) + 1) < size) {
			int t = heap[child];
			final int right = child + 1;
			if (right < size && c.compare(refArray[heap[right]], refArray[t]) < 0) t = heap[child = right];
			if (c.compare(E, refArray[t]) <= 0) break;
			heap[i] = t;
			inv[heap[i]] = i;
			i = child;
		}
		heap[i] = e;
		inv[e] = i;
		return i;
	}

	/**
	 * Moves the given element up in the indirect heap until it reaches the highest possible position.
	 *
	 * Note that in principle after this call the heap property may be violated.
	 *
	 * @param refArray the reference array.
	 * @param heap the indirect heap (starting at 0).
	 * @param inv the inversion array.
	 * @param size the number of elements in the heap.
	 * @param i the index in the heap of the element to be moved up.
	 * @param c a type-specific comparator, or {@code null} for the natural order.
	 * @return the new position in the heap of the element of heap index {@code i}.
	 */

	public static int upHeap(final float[] refArray, final int[] heap, final int[] inv, final int size, int i, final FloatComparator c) {
		assert i < size;
		final int e = heap[i];
		final float E = refArray[e];
		if (c == null) while (i != 0) {
			final int parent = (i - 1) >>> 1;
			final int t = heap[parent];
			if ((Float.compare((refArray[t]), (E)) <= 0)) break;
			heap[i] = t;
			inv[heap[i]] = i;
			i = parent;
		}
		else while (i != 0) {
			final int parent = (i - 1) >>> 1;
			final int t = heap[parent];
			if (c.compare(refArray[t], E) <= 0) break;
			heap[i] = t;
			inv[heap[i]] = i;
			i = parent;
		}
		heap[i] = e;
		inv[e] = i;
		return i;
	}

	/**
	 * Creates an indirect heap in the given array.
	 *
	 * @param refArray the reference array.
	 * @param offset the first element of the reference array to be put in the heap.
	 * @param length the number of elements to be put in the heap.
	 * @param heap the array where the heap is to be created.
	 * @param inv the inversion array.
	 * @param c a type-specific comparator, or {@code null} for the natural order.
	 */
	public static void makeHeap(final float[] refArray, final int offset, final int length, final int[] heap, final int[] inv, final FloatComparator c) {
		FloatArrays.ensureOffsetLength(refArray, offset, length);
		if (heap.length < length) throw new IllegalArgumentException("The heap length (" + heap.length + ") is smaller than the number of elements (" + length + ")");
		if (inv.length < refArray.length) throw new IllegalArgumentException("The inversion array length (" + heap.length + ") is smaller than the length of the reference array (" + refArray.length + ")");
		Arrays.fill(inv, 0, refArray.length, -1);
		int i = length;
		while (i-- != 0) inv[heap[i] = offset + i] = i;
		i = length >>> 1;
		while (i-- != 0) downHeap(refArray, heap, inv, length, i, c);
	}

	/**
	 * Creates an indirect heap from a given index array.
	 *
	 * @param refArray the reference array.
	 * @param heap an array containing indices into {@code refArray}.
	 * @param inv the inversion array.
	 * @param size the number of elements in the heap.
	 * @param c a type-specific comparator, or {@code null} for the natural order.
	 */
	public static void makeHeap(final float[] refArray, final int[] heap, final int[] inv, final int size, final FloatComparator c) {
		int i = size >>> 1;
		while (i-- != 0) downHeap(refArray, heap, inv, size, i, c);
	}
}
