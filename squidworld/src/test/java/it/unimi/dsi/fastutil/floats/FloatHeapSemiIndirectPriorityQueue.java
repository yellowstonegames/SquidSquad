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

import java.util.NoSuchElementException;

import com.github.tommyettinger.ds.support.sort.FloatComparator;
import it.unimi.dsi.fastutil.IndirectPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * A type-specific heap-based semi-indirect priority queue.
 *
 * <p>
 * Instances of this class use as reference list a <em>reference array</em>, which must be provided
 * to each constructor. The priority queue is represented using a heap. The heap is enlarged as
 * needed, but it is never shrunk. Use the {@link #trim()} method to reduce its size, if necessary.
 *
 * @implSpec This implementation allows one to enqueue several time the same index, but you must be
 *           careful when calling {@link #changed()}.
 */
public class FloatHeapSemiIndirectPriorityQueue implements IndirectPriorityQueue {
	/** The reference array. */
	protected final float refArray[];
	/** The semi-indirect heap. */
	protected int heap[] = IntArrays.EMPTY_ARRAY;
	/** The number of elements in this queue. */
	protected int size;
	/** The type-specific comparator used in this queue. */
	protected FloatComparator c;

	/**
	 * Creates a new empty queue without elements with a given capacity and comparator.
	 *
	 * @param refArray the reference array.
	 * @param capacity the initial capacity of this queue.
	 * @param c the comparator used in this queue, or {@code null} for the natural order.
	 */
	public FloatHeapSemiIndirectPriorityQueue(float[] refArray, int capacity, FloatComparator c) {
		if (capacity > 0) this.heap = new int[capacity];
		this.refArray = refArray;
		this.c = c;
	}

	/**
	 * Creates a new empty queue with given capacity and using the natural order.
	 *
	 * @param refArray the reference array.
	 * @param capacity the initial capacity of this queue.
	 */
	public FloatHeapSemiIndirectPriorityQueue(float[] refArray, int capacity) {
		this(refArray, capacity, null);
	}

	/**
	 * Creates a new empty queue with capacity equal to the length of the reference array and a given
	 * comparator.
	 *
	 * @param refArray the reference array.
	 * @param c the comparator used in this queue, or {@code null} for the natural order.
	 */
	public FloatHeapSemiIndirectPriorityQueue(float[] refArray, FloatComparator c) {
		this(refArray, refArray.length, c);
	}

	/**
	 * Creates a new empty queue with capacity equal to the length of the reference array and using the
	 * natural order.
	 * 
	 * @param refArray the reference array.
	 */
	public FloatHeapSemiIndirectPriorityQueue(final float[] refArray) {
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
	public FloatHeapSemiIndirectPriorityQueue(final float[] refArray, final int[] a, int size, final FloatComparator c) {
		this(refArray, 0, c);
		this.heap = a;
		this.size = size;
		FloatSemiIndirectHeaps.makeHeap(refArray, a, size, c);
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
	public FloatHeapSemiIndirectPriorityQueue(final float[] refArray, final int[] a, final FloatComparator c) {
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
	public FloatHeapSemiIndirectPriorityQueue(final float[] refArray, final int[] a, int size) {
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
	public FloatHeapSemiIndirectPriorityQueue(final float[] refArray, final int[] a) {
		this(refArray, a, a.length);
	}

	/**
	 * Ensures that the given index is a valid reference.
	 *
	 * @param index an index in the reference array.
	 * @throws IndexOutOfBoundsException if the given index is negative or larger than the reference
	 *             array length.
	 */
	protected void ensureElement(final int index) {
		if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		if (index >= refArray.length) throw new IndexOutOfBoundsException("Index (" + index + ") is larger than or equal to reference array size (" + refArray.length + ")");
	}

	@Override
	public void enqueue(int x) {
		ensureElement(x);
		if (size == heap.length) heap = IntArrays.grow(heap, size + 1);
		heap[size++] = x;
		FloatSemiIndirectHeaps.upHeap(refArray, heap, size, size - 1, c);
	}

	@Override
	public int dequeue() {
		if (size == 0) throw new NoSuchElementException();
		final int result = heap[0];
		heap[0] = heap[--size];
		if (size != 0) FloatSemiIndirectHeaps.downHeap(refArray, heap, size, 0, c);
		return result;
	}

	@Override
	public int first() {
		if (size == 0) throw new NoSuchElementException();
		return heap[0];
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The caller <strong>must</strong> guarantee that when this method is called the index of the first
	 * element appears just once in the queue. Failure to do so will bring the queue in an inconsistent
	 * state, and will cause unpredictable behaviour.
	 */
	@Override
	public void changed() {
		FloatSemiIndirectHeaps.downHeap(refArray, heap, size, 0, c);
	}

	/** Rebuilds this heap in a bottom-up fashion (in linear time). */
	@Override
	public void allChanged() {
		FloatSemiIndirectHeaps.makeHeap(refArray, heap, size, c);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		size = 0;
	}

	/** Trims the backing array so that it has exactly {@link #size()} elements. */
	public void trim() {
		heap = IntArrays.trim(heap, size);
	}

	@Override
	public FloatComparator comparator() {
		return c;
	}

	/**
	 * Writes in the provided array the <em>front</em> of the queue, that is, the set of indices whose
	 * elements have the same priority as the top.
	 * 
	 * @param a an array whose initial part will be filled with the frnot (must be sized as least as the
	 *            heap size).
	 * @return the number of elements of the front.
	 */
	@Override
	public int front(final int[] a) {
		return c == null ? FloatSemiIndirectHeaps.front(refArray, heap, size, a) : FloatSemiIndirectHeaps.front(refArray, heap, size, a, c);
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("[");
		for (int i = 0; i < size; i++) {
			if (i != 0) s.append(", ");
			s.append(refArray[heap[i]]);
		}
		s.append("]");
		return s.toString();
	}
}
