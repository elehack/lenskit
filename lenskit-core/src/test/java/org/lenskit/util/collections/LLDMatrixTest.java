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

import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import org.junit.Test;
import org.lenskit.util.IdBox;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class LLDMatrixTest {
    @Test
    public void testNewEmpty() {
        LLDMatrix empty = LLDMatrix.newMutableMatrix();
        assertThat(empty.rows(), hasSize(0));
        assertThat(empty.get(42, 37), equalTo(0.0));
    }

    @Test
    public void testSet() {
        LLDMatrix empty = LLDMatrix.newMutableMatrix();
        empty.put(42, 37, 3.5);
        assertThat(empty.get(42, 37), equalTo(3.5));
        assertThat(empty.get(37, 42), equalTo(0.0));
        assertThat(empty.rows(), hasSize(1));
        assertThat(empty.rows(), contains(IdBox.create(42, Long2DoubleMaps.singleton(37, 3.5))));
    }

    @Test
    public void testAddEmpty() {
        LLDMatrix empty = LLDMatrix.newMutableMatrix();
        assertThat(empty.addTo(42, 37, 3.5),
                   equalTo(0.0));
        assertThat(empty.get(42, 37), equalTo(3.5));
        assertThat(empty.get(37, 42), equalTo(0.0));
        assertThat(empty.rows(), hasSize(1));
        assertThat(empty.rows(), contains(IdBox.create(42, Long2DoubleMaps.singleton(37, 3.5))));
    }

    @Test
    public void testSetAddEmpty() {
        LLDMatrix empty = LLDMatrix.newMutableMatrix();
        empty.put(42, 37, 2);
        assertThat(empty.addTo(42, 37, 3.5),
                   equalTo(2.0));
        assertThat(empty.get(42, 37), equalTo(5.5));
        assertThat(empty.get(37, 42), equalTo(0.0));
        assertThat(empty.rows(), hasSize(1));
        assertThat(empty.rows(), contains(IdBox.create(42, Long2DoubleMaps.singleton(37, 5.5))));
    }
}