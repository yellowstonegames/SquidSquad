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
package it.unimi.dsi.fastutil.doubles;

import com.github.tommyettinger.ds.support.sort.DoubleComparator;
import it.unimi.dsi.fastutil.doubles.DoubleSemiIndirectHeaps;
import it.unimi.dsi.fastutil.ints.IntArrays;

import java.util.NoSuchElementException;

/**
 * A type-specific heap-based semi-indirect priority queue.
 *
 * <p>
 * Instances of this class use as reference list a <em>reference array</em>, which must be provided
 * to each constructor. The priority queue is represented using a heap. The heap is enlarged as
 * needed, but it is never shrunk. Use the {@link #trim()} method to reduce its size, if necessary.
 */
public class DoubleHeapSemiIndirectPriorityQueue {
	/** The reference array. */
	protected final double refArray[];
	/** The semi-indirect heap. */
	protected int heap[] = IntArrays.EMPTY_ARRAY;
	/** The number of elements in this queue. */
	protected int size;
	/** The type-specific comparator used in this queue. */
	protected DoubleComparator c;

	/**
	 * Creates a new empty queue without elements with a given capacity and comparator.
	 *
	 * @param refArray the reference array.
	 * @param capacity the initial capacity of this queue.
	 * @param c the comparator used in this queue, or {@code null} for the natural order.
	 */
	public DoubleHeapSemiIndirectPriorityQueue(double[] refArray, int capacity, DoubleComparator c) {
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
	public DoubleHeapSemiIndirectPriorityQueue(double[] refArray, int capacity) {
		this(refArray, capacity, null);
	}

	/**
	 * Creates a new empty queue with capacity equal to the length of the reference array and a given
	 * comparator.
	 *
	 * @param refArray the reference array.
	 * @param c the comparator used in this queue, or {@code null} for the natural order.
	 */
	public DoubleHeapSemiIndirectPriorityQueue(double[] refArray, DoubleComparator c) {
		this(refArray, refArray.length, c);
	}

	/**
	 * Creates a new empty queue with capacity equal to the length of the reference array and using the
	 * natural order.
	 * 
	 * @param refArray the reference array.
	 */
	public DoubleHeapSemiIndirectPriorityQueue(final double[] refArray) {
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
	public DoubleHeapSemiIndirectPriorityQueue(final double[] refArray, final int[] a, int size, final DoubleComparator c) {
		this(refArray, 0, c);
		this.heap = a;
		this.size = size;
		DoubleSemiIndirectHeaps.makeHeap(refArray, a, size, c);
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
	public DoubleHeapSemiIndirectPriorityQueue(final double[] refArray, final int[] a, final DoubleComparator c) {
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
	public DoubleHeapSemiIndirectPriorityQueue(final double[] refArray, final int[] a, int size) {
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
	public DoubleHeapSemiIndirectPriorityQueue(final double[] refArray, final int[] a) {
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

	public void enqueue(int x) {
		ensureElement(x);
		if (size == heap.length) heap = IntArrays.grow(heap, size + 1);
		heap[size++] = x;
		DoubleSemiIndirectHeaps.upHeap(refArray, heap, size, size - 1, c);
	}

	public int dequeue() {
		if (size == 0) throw new NoSuchElementException();
		final int result = heap[0];
		heap[0] = heap[--size];
		if (size != 0) DoubleSemiIndirectHeaps.downHeap(refArray, heap, size, 0, c);
		return result;
	}
	
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
	public void changed() {
		DoubleSemiIndirectHeaps.downHeap(refArray, heap, size, 0, c);
	}

	/** Rebuilds this heap in a bottom-up fashion (in linear time). */
	public void allChanged() {
		DoubleSemiIndirectHeaps.makeHeap(refArray, heap, size, c);
	}

	public int size() {
		return size;
	}

	public void clear() {
		size = 0;
	}

    /** Trims the backing array so that it has exactly {@link #size()} elements. */
	public void trim() {
		heap = IntArrays.trim(heap, size);
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
