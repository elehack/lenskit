/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.util.keys;

import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.NoSuchElementException;

/**
 * Compact array map sorted in decreasing order of value.
 */
public class Long2DoubleValueSortedArrayMap extends AbstractLong2DoubleMap {
    private static final long serialVersionUID = 1L;

    private final SortedKeyIndex keys;
    private final double[] values;
    private final int[] positions;

    Long2DoubleValueSortedArrayMap(SortedKeyIndex ks, double[] vs) {
        keys = ks;
        values = vs;
        positions = new int[keys.size()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = i + keys.getLowerBound();
        }
        IntArrays.quickSort(positions, new AbstractIntComparator() {
            @Override
            public int compare(int k1, int k2) {
                return Doubles.compare(values[k2], values[k1]);
            }
        });
    }

    @Override
    public FastEntrySet long2DoubleEntrySet() {
        return new EntrySet();
    }

    @Override
    public double get(long key) {
        int idx = keys.tryGetIndex(key);
        if (idx >= 0) {
            return values[idx];
        } else {
            return defaultReturnValue();
        }
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public LongSet keySet() {
        return new KeySet();
    }

    @Override
    public boolean containsKey(long k) {
        return keys.tryGetIndex(k) >= 0;
    }

    /**
     * Sort the map by key instead of value.
     * @return This map, sorted by key.
     */
    public Long2DoubleSortedArrayMap sortedByKey() {
        return Long2DoubleSortedArrayMap.wrap(keys, values);
    }

    private Entry entry(int idx) {
        return new BasicEntry(keys.getKey(idx), values[idx]);
    }

    private class EntrySet extends AbstractObjectSet<Entry> implements Long2DoubleMap.FastEntrySet {
        @Override
        public ObjectIterator<Entry> fastIterator() {
            return new FastEntryIter();
        }

        @Override
        public ObjectIterator<Entry> iterator() {
            return new EntryIter();
        }

        @Override
        public int size() {
            return keys.size();
        }
    }

    private class KeySet extends AbstractLongSet {
        @Override
        public LongIterator iterator() {
            return new KeyIter();
        }

        @Override
        public boolean contains(long k) {
            return keys.tryGetIndex(k) >= 0;
        }

        @Override
        public int size() {
            return keys.size();
        }
    }

    private class KeyIter extends AbstractLongIterator {
        int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < positions.length;
        }

        @Override
        public long nextLong() {
            if (pos >= positions.length) throw new NoSuchElementException();
            return keys.getKey(positions[pos++]);
        }
    }

    private class EntryIter extends AbstractObjectIterator<Entry> {
        int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < positions.length;
        }

        @Override
        public Entry next() {
            if (pos >= positions.length) throw new NoSuchElementException();
            return entry(positions[pos++]);
        }
    }

    private class FastEntryIter extends AbstractObjectIterator<Entry> {
        int pos = 0;
        IndirectEntry entry = new IndirectEntry(0);

        @Override
        public boolean hasNext() {
            return pos < positions.length;
        }

        @Override
        public Entry next() {
            if (pos >= positions.length) throw new NoSuchElementException();
            entry.index = positions[pos++];
            return entry;
        }
    }

    private class IndirectEntry implements Entry {
        int index;

        public IndirectEntry(int idx) {
            index = idx;
        }

        @Override
        public long getLongKey() {
            return keys.getKey(index);
        }

        @Override
        public double setValue(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getDoubleValue() {
            return values[index];
        }

        @Override
        public Long getKey() {
            return getLongKey();
        }

        @Override
        public Double getValue() {
            return getDoubleValue();
        }

        @Override
        public Double setValue(Double value) {
            throw new UnsupportedOperationException();
        }
    }
}
