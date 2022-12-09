/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.Arrangeable;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.yellowstonegames.core.annotations.Beta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * A kind of Map-like data structure that allows lookup by int id or Coord position, and retains its insertion order.
 * This requires its {@code V} values to implement {@link IGridIdentified}, which makes this able to associate each V
 * with its own position and id. The most commonly-used non-standard method here is probably
 * {@link #move(Coord, Coord)}, which gets the value at a position and attempts to move it to another position, updating
 * the value's internal position in the process. There are variants on most methods for id ({@link #getById(int)}),
 * position ({@link #getByPosition(Coord)}), iteration order index ({@link #getAt(int)}), and often the value itself
 * ({@link #contains(Object)}).
 * @param <V> the type of all values; this must implement {@link IGridIdentified}
 */
@Beta
public class SpatialMap<V extends IGridIdentified> extends AbstractCollection<V> implements Arrangeable {
    public final CoordObjectOrderedMap<V> positionMap;
    public final IntObjectOrderedMap<V> idMap;

    public SpatialMap(){
        this(16, 0.75f);
    }

    public SpatialMap(int capacity){
        this(capacity, 0.75f);
    }
    public SpatialMap(int capacity, float loadFactor){
        positionMap = new CoordObjectOrderedMap<>(capacity, loadFactor);
        idMap = new IntObjectOrderedMap<>(capacity, loadFactor);
    }

    public SpatialMap(SpatialMap<? extends V> other) {
        positionMap = new CoordObjectOrderedMap<>(other.positionMap);
        idMap = new IntObjectOrderedMap<>(other.idMap);
    }

    public boolean add(@Nonnull V value){
        Coord pos = value.getCoordPosition();
        int id = value.getIdentifier();
        if(idMap.containsKey(id)){
            final V v = idMap.replace(id, value);
            assert v != null;
            final Coord old = v.getCoordPosition();
            positionMap.alter(old, pos);
            positionMap.replace(pos, value);
            return true;
        }
        else if(positionMap.containsKey(pos)){
            return false;
        }
        else {
            positionMap.put(pos, value);
            idMap.put(id, value);
            return true;
        }
    }

    public Collection<V> values() {
        return idMap.values();
    }

    public Set<Coord> positions() {
        return positionMap.keySet();
    }

    public PrimitiveCollection.OfInt ids() {
        return idMap.keySet();
    }

    @Override
    public Iterator<V> iterator() {
        return idMap.values().iterator();
    }

    public Iterator<Coord> positionIterator() {
        return positionMap.keySet().iterator();
    }

    public PrimitiveIterator.OfInt idIterator() {
        return idMap.keySet().iterator();
    }

    @Override
    public int size() {
        return idMap.size();
    }

    @Override
    public boolean contains(Object o) {
        return idMap.containsValue(o);
    }

    public boolean containsPosition(Coord position) {
        return positionMap.containsKey(position);
    }

    public boolean containsId(int id) {
        return idMap.containsKey(id);
    }

    @Override
    public boolean remove(Object o) {
        if(o instanceof IGridIdentified) {
            return removeId(((IGridIdentified) o).getIdentifier());
        }
        return false;
    }

    public boolean removePosition(Coord pos){
        V v = positionMap.remove(pos);
        if(v == null) return false;
        idMap.remove(v.getIdentifier());
        return true;
    }

    public boolean removeId(int id){
        V v = idMap.remove(id);
        if(v == null) return false;
        positionMap.remove(v.getCoordPosition());
        return true;
    }

    public boolean removeAt(int index){
        if(index < 0 || index >= idMap.size()) return false;
        idMap.removeAt(index);
        positionMap.removeAt(index);
        return true;
    }

    @Nullable
    public V getAt(int index) {
        if(index < 0 || index >= idMap.size()) return null;
        return idMap.getAt(index);
    }
    @Nullable
    public V getById(int id) {
        return idMap.get(id);
    }
    @Nullable
    public V getByPosition(Coord position) {
        return positionMap.get(position);
    }
    @Nullable
    public V getByPosition(int x, int y) {
        return positionMap.get(Coord.get(x, y));
    }

    /**
     * Attempts to move the V located at {@code oldPosition} to {@code newPosition} without changing its position in the
     * iteration order. If this succeeds, it returns the moved V and sets the internal position in that V using
     * {@link IGridIdentified#setCoordPosition(Coord)}. This can fail if there is already a V at {@code newPosition}, in
     * which case this returns that V without changing anything. It can also fail if there isn't a V at
     * {@code oldPosition}, in which case this returns null.
     * @param oldPosition the Coord to look up for the V to move
     * @param newPosition the Coord to try to move the V into
     * @return on success, the moved V; on failure because newPosition is occupied, that occupant; on failure because
     * there wasn't a V present to move, null
     */
    @Nullable
    public V move(Coord oldPosition, Coord newPosition) {
        V occupant = positionMap.getOrDefault(newPosition, null);
        if(occupant != null) return occupant;
        occupant = positionMap.get(oldPosition);
        if(occupant == null) return null;
        positionMap.alter(oldPosition, newPosition);
        occupant.setCoordPosition(newPosition);
        return occupant;
    }
    /**
     * Attempts to move the V with the given {@code id} to {@code newPosition} without changing its position in
     * the iteration order. If this succeeds, it returns the moved V and sets the internal position in that V using
     * {@link IGridIdentified#setCoordPosition(Coord)}. This can fail if there is already a V at {@code newPosition}, in
     * which case this returns that V without changing anything. It can also fail if there isn't a V with
     * {@code id}, in which case this returns null.
     * @param id the int ID to look up for the V to move
     * @param newPosition the Coord to try to move the V into
     * @return on success, the moved V; on failure because newPosition is occupied, that occupant; on failure because
     * there wasn't a V present to move, null
     */
    @Nullable
    public V move(int id, Coord newPosition){
        V mover = idMap.get(id);
        if(mover == null) return null;
        return move(mover.getCoordPosition(), newPosition);
    }
    /**
     * Attempts to move the V at the given {@code index} in the iteration order to {@code newPosition} without changing
     * its position in the iteration order. If this succeeds, it returns the moved V and sets the internal position in
     * that V using {@link IGridIdentified#setCoordPosition(Coord)}. This can fail if there is already a V at
     * {@code newPosition}, in which case this returns that V without changing anything. It can also fail if index is
     * negative or &gt;= the size of this SpatialMap, in which case this returns null.
     * @param index the int index in the iteration order for the V to move
     * @param newPosition the Coord to try to move the V into
     * @return on success, the moved V; on failure because newPosition is occupied, that occupant; on failure because
     * there the index was out of bounds, null
     */
    @Nullable
    public V moveAt(int index, Coord newPosition){
        if(index < 0 || index >= positionMap.size()) return null;
        Coord pos = positionMap.keyAt(index);
        return move(pos, newPosition);
    }

    @Override
    public void clear() {
        idMap.clear();
        positionMap.clear();
    }

    @Override
    public void swap(int first, int second) {
        idMap.swap(first, second);
        positionMap.swap(first, second);
    }

    @Override
    public void reverse() {
        idMap.reverse();
        positionMap.reverse();
    }

    @Override
    public String toString() {
        if (positionMap.size() == 0) { return "{}"; }
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('{');
        IntList idOrder = idMap.order();
        for (int i = 0, n = idOrder.size(); i < n; i++) {
            int id = idOrder.get(i);
            V v = idMap.get(id);
            if(v == null) continue;
            if (i > 0) { buffer.append(", "); }
            buffer.append('#').append(id).append(" at ").append(v.getCoordPosition()).append('=').append(v);
        }
        buffer.append('}');
        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpatialMap<?> that = (SpatialMap<?>) o;

        if (!positionMap.equals(that.positionMap)) return false;
        return idMap.equals(that.idMap);
    }

    @Override
    public int hashCode() {
        int result = positionMap.hashCode();
        result = 31 * result + idMap.hashCode();
        return result;
    }
}
