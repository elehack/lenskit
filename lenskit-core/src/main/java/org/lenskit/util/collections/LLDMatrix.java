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
import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.util.IdBox;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * A long-to-long-to-double sparse matrix.  Missing values are assumed 0.
 *
 * Instances can be created with {#newMutableMatrix()}.
 */
public class LLDMatrix implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long2ObjectMap<Long2DoubleOpenHashMap> rows;

    private LLDMatrix() {
        rows = new Long2ObjectOpenHashMap<>();
    }

    /**
     * Create a new mutable LLD matrix.
     * @return The new matrix.
     */
    public static LLDMatrix newMutableMatrix() {
        return new LLDMatrix();
    }

    /**
     * Get the value at a specified row and column.
     * @param row The row key.
     * @param col The column key.
     * @return The value.
     */
    public double get(long row, long col) {
        return getRow(row).get(col);
    }

    /**
     * Set the value at a specified row and column.
     * @param row The row key.
     * @param col The column key.
     * @param val The value to set.
     * @return The old value, if present (or 0).
     */
    public double put(long row, long col, double val) {
        Long2DoubleOpenHashMap rv = rows.get(row);
        if (rv == null) {
            rv = new Long2DoubleOpenHashMap();
            rows.put(row, rv);
        }
        return rv.put(col, val);
    }

    /**
     * Add to the value at a specific location. Missing values are assumed 0.
     * @param row The row key.
     * @param col The column key.
     * @param incr The value by which to increase the value.
     * @return The old value, if present (or 0).
     */
    public double addTo(long row, long col, double incr) {
        Long2DoubleOpenHashMap rv = rows.get(row);
        if (rv == null) {
            rv = new Long2DoubleOpenHashMap();
            rows.put(row, rv);
        }
        return rv.addTo(col, incr);
    }

    /**
     * Get a row as a map.
     * @param row The row key.
     * @return The row vector as a map.  If no entries exist with the specified row, the map will be empty
     *         **and immutable**.
     */
    @Nonnull
    public Long2DoubleMap getRow(long row) {
        return getRow(row, false);
    }

    /**
     * Get a row as a map.
     * @param row The row key.
     * @param insert If `true`, insert and create a new row if this row does not exist.
     * @return The row vector as a map.
     */
    @Nonnull
    public Long2DoubleMap getRow(long row, boolean insert) {
        Long2DoubleMap r = rows.get(row);
        if (r == null) {
            if (insert) {
                r = new Long2DoubleOpenHashMap();
                rows.put(row, (Long2DoubleOpenHashMap) r);
            } else {
                r = Long2DoubleMaps.EMPTY_MAP;
            }
        }
        return r;
    }

    /**
     * Clear the a particular row.
     * @param row The row to clear.
     */
    public void clearRow(long row) {
        rows.remove(row);
    }

    /**
     * Get the set of row IDs that have been used.
     * @return The set of row IDs.
     */
    public LongSet rowIds() {
        return rows.keySet();
    }

    /**
     * Get the collection of rows as an iterable collection.
     * @return The collection of rows.
     */
    public Collection<IdBox<Long2DoubleMap>> rows() {
        return new AbstractCollection<IdBox<Long2DoubleMap>>() {
            @Override
            public Iterator<IdBox<Long2DoubleMap>> iterator() {
                return Iterators.transform(rows.entrySet().iterator(),
                                           (e) -> IdBox.create(e.getKey(), e.getValue()));
            }

            @Override
            public int size() {
                return rows.size();
            }
        };
    }
}
