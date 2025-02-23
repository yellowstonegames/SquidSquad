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
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * A type-specific heap-based indirect priority queue.
 *
 * <p>
 * Instances of this class use an additional <em>inversion array</em>, of the same length of the
 * reference array, to keep track of the heap position containing a given element of the reference
 * array. The priority queue is represented using a heap. The heap is enlarged as needed, but it is
 * never shrunk. Use the {@link #trim()} method to reduce its size, if necessary.
 *
 * @implSpec This implementation does <em>not</em> allow one to enqueue several times the same
 *           index.
 */
public class FloatHeapIndirectPriorityQueue extends FloatHeapSemiIndirectPriorityQueue {
	/** The inversion array. */
	protected final int inv[];

	/**
	 * Creates a new empty queue with a given capacity and comparator.
	 *
	 * @param refArray the reference array.
	 * @param capacity the initial capacity of this queue.
	 * @param c the comparator used in this queue, or {@code null} for the natural order.
	 */
	public FloatHeapIndirectPriorityQueue(float[] refArray, int capacity, FloatComparator c) {
		super(refArray, capacity, c);
		this.inv = new int[refArray.length];
		Arrays.fill(inv, -1);
	}

	/**
	 * Creates a new empty queue with a given capacity and using the natural order.
	 *
	 * @param refArray the reference array.
	 * @param capacity the initial capacity of this queue.
	 */
	public FloatHeapIndirectPriorityQueue(float[] refArray, int capacity) {
		this(refArray, capacity, null);
	}

	/**
	 * Creates a new empty queue with capacity equal to the length of the reference array and a given
	 * comparator.
	 *
	 * @param refArray the reference array.
	 * @param c the comparator used in this queue, or {@code null} for the natural order.
	 */
	public FloatHeapIndirectPriorityQueue(float[] refArray, FloatComparator c) {
		this(refArray, refArray.length, c);
	}

	/**
	 * Creates a new empty queue with capacity equal to the length of the reference array and using the
	 * natural order.
	 * 
	 * @param refArray the reference array.
	 */
	public FloatHeapIndirectPriorityQueue(float[] refArray) {
		this(refArray, refArray.length, null);
	}

	/**
	 * Wraps a given array in a queue using a given comparator.
	 *
	 * <p>
	 * The queue returned by this method will be backed by the given array. The first {@code size}
	 * element of the array will be rearranged so to form a heap (this is more efficient than enqueing
	 * the elements of {@code a} one by one).
	 *
	 * @param refArray the reference array.
	 * @param a an array of indices into {@code refArray}.
	 * @param size the number of elements to be included in the queue.
	 * @param c the comparator used in this queue, or {@code null} for the natural order.
	 */
	public FloatHeapIndirectPriorityQueue(final float[] refArray, final int[] a, final int size, final FloatComparator c) {
		this(refArray, 0, c);
		this.heap = a;
		this.size = size;
		int i = size;
		while (i-- != 0) {
			if (inv[a[i]] != -1) throw new IllegalArgumentException("Index " + a[i] + " appears twice in the heap");
			inv[a[i]] = i;
		}
		FloatIndirectHeaps.makeHeap(refArray, a, inv, size, c);
	}

	/**
	 * Wraps a given array in a queue using a given comparator.
	 *
	 * <p>
	 * The queue returned by this method will be backed by the given array. The elements of the array
	 * will be rearranged so to form a heap (this is more efficient than enqueing the elements of
	 * {@code a} one by one).
	 *
	 * @param refArray the reference array.
	 * @param a an array of indices into {@code refArray}.
	 * @param c the comparator used in this queue, or {@code null} for the natural order.
	 */
	public FloatHeapIndirectPriorityQueue(final float[] refArray, final int[] a, final FloatComparator c) {
		this(refArray, a, a.length, c);
	}

	/**
	 * Wraps a given array in a queue using the natural order.
	 *
	 * <p>
	 * The queue returned by this method will be backed by the given array. The first {@code size}
	 * element of the array will be rearranged so to form a heap (this is more efficient than enqueing
	 * the elements of {@code a} one by one).
	 *
	 * @param refArray the reference array.
	 * @param a an array of indices into {@code refArray}.
	 * @param size the number of elements to be included in the queue.
	 */
	public FloatHeapIndirectPriorityQueue(final float[] refArray, final int[] a, int size) {
		this(refArray, a, size, null);
	}

	/**
	 * Wraps a given array in a queue using the natural order.
	 *
	 * <p>
	 * The queue returned by this method will be backed by the given array. The elements of the array
	 * will be rearranged so to form a heap (this is more efficient than enqueing the elements of
	 * {@code a} one by one).
	 *
	 * @param refArray the reference array.
	 * @param a an array of indices into {@code refArray}.
	 */
	public FloatHeapIndirectPriorityQueue(final float[] refArray, final int[] a) {
		this(refArray, a, a.length);
	}

	@Override
	public void enqueue(final int x) {
		if (inv[x] >= 0) throw new IllegalArgumentException("Index " + x + " belongs to the queue");
		if (size == heap.length) heap = IntArrays.grow(heap, size + 1);
		inv[heap[size] = x] = size++;
		FloatIndirectHeaps.upHeap(refArray, heap, inv, size, size - 1, c);
	}

	@Override
	public boolean contains(final int index) {
		return inv[index] >= 0;
	}

	@Override
	public int dequeue() {
		if (size == 0) throw new NoSuchElementException();
		final int result = heap[0];
		if (--size != 0) inv[heap[0] = heap[size]] = 0;
		inv[result] = -1;
		if (size != 0) FloatIndirectHeaps.downHeap(refArray, heap, inv, size, 0, c);
		return result;
	}

	@Override
	public void changed() {
		FloatIndirectHeaps.downHeap(refArray, heap, inv, size, 0, c);
	}

	@Override
	public void changed(final int index) {
		final int pos = inv[index];
		if (pos < 0) throw new IllegalArgumentException("Index " + index + " does not belong to the queue");
		final int newPos = FloatIndirectHeaps.upHeap(refArray, heap, inv, size, pos, c);
		FloatIndirectHeaps.downHeap(refArray, heap, inv, size, newPos, c);
	}

	/** Rebuilds this queue in a bottom-up fashion (in linear time). */
	@Override
	public void allChanged() {
		FloatIndirectHeaps.makeHeap(refArray, heap, inv, size, c);
	}

	@Override
	public boolean remove(final int index) {
		final int result = inv[index];
		if (result < 0) return false;
		inv[index] = -1;
		if (result < --size) {
			inv[heap[result] = heap[size]] = result;
			final int newPos = FloatIndirectHeaps.upHeap(refArray, heap, inv, size, result, c);
			FloatIndirectHeaps.downHeap(refArray, heap, inv, size, newPos, c);
		}
		return true;
	}

	@Override
	public void clear() {
		size = 0;
		Arrays.fill(inv, -1);
	}
}
