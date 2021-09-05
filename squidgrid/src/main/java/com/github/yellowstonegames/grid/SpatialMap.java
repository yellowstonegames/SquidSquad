package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.yellowstonegames.core.annotations.Beta;

import javax.annotation.Nonnull;
import java.util.AbstractCollection;
import java.util.Iterator;

@Beta
public class SpatialMap<V extends IGridIdentified> extends AbstractCollection<V> {
    public CoordObjectOrderedMap<V> positionMap;
    public IntObjectOrderedMap<V> idMap;

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

    @Override
    public Iterator<V> iterator() {
        return idMap.values().iterator();
    }

    @Override
    public int size() {
        return idMap.size();
    }

    @Override
    public boolean contains(Object o) {
        return idMap.containsValue(o);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Using remove(Object) is not allowed; use removePosition(Coord) or removeId(int).");
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

    @Override
    public void clear() {
        idMap.clear();
        positionMap.clear();
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
}
