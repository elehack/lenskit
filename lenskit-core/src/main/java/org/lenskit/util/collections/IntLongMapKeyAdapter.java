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
package org.lenskit.util.collections;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.longs.AbstractLong2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.objects.*;

import java.util.Iterator;
import java.util.Set;

/**
 * Wrap an {@link it.unimi.dsi.fastutil.ints.Int2DoubleMap} as a {@link Long2DoubleMap}.
 */
class IntLongMapKeyAdapter extends AbstractLong2DoubleMap {
    private final Int2DoubleMap delegate;

    public IntLongMapKeyAdapter(Int2DoubleMap m) {
        delegate = m;
    }

    @Override
    public Long2DoubleMap.FastEntrySet long2DoubleEntrySet() {
        return new EntrySet();
    }

    @Override
    public DoubleCollection values() {
        return delegate.values();
    }

    @Override
    public boolean containsValue(double value) {
        return delegate.containsValue(value);
    }

    @Override
    public double get(long key) {
        if (key < Integer.MIN_VALUE || key > Integer.MAX_VALUE) {
            return delegate.defaultReturnValue();
        } else {
            return delegate.get((int) key);
        }
    }

    public boolean containsKey(long key) {
        if (key < Integer.MIN_VALUE || key > Integer.MAX_VALUE) {
            return false;
        } else {
            return delegate.containsKey((int) key);
        }
    }

    @Override
    public void defaultReturnValue(double rv) {
        delegate.defaultReturnValue(rv);
    }

    @Override
    public double defaultReturnValue() {
        return delegate.defaultReturnValue();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    private class EntrySet extends AbstractObjectSet<Entry> implements FastEntrySet {
        @Override
        public ObjectIterator<Entry> fastIterator() {
            return new AbstractObjectIterator<Entry>() {
                private Set<Int2DoubleMap.Entry> iset = delegate.int2DoubleEntrySet();
                private Iterator<Int2DoubleMap.Entry> iter =
                        iset instanceof Int2DoubleMap.FastEntrySet
                                ? ((Int2DoubleMap.FastEntrySet) iset).fastIterator()
                                : iset.iterator();
                FastEntry entry = new FastEntry();

                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Entry next() {
                    Int2DoubleMap.Entry e = iter.next();
                    entry.update(e.getIntKey(), e.getDoubleValue());
                    return entry;
                }
            };
        }

        @Override
        public ObjectIterator<Entry> iterator() {
            return ObjectIterators.asObjectIterator(
                    Iterators.transform(delegate.int2DoubleEntrySet().iterator(),
                                        (e) -> new BasicEntry(e.getIntKey(), e.getDoubleValue())));
        }

        @Override
        public int size() {
            return delegate.size();
        }
    }

    private static class FastEntry extends BasicEntry {
        FastEntry() {
            super(0,0);
        }

        private void update(long k, double v) {
            key = k;
            value = v;
        }
    }
}
