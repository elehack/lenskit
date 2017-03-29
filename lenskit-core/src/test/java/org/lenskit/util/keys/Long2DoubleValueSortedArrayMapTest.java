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

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.lenskit.util.math.Vectors;

import java.util.Map;
import java.util.Set;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someSets;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class Long2DoubleValueSortedArrayMapTest {
    @Test
    public void testEmptyMap() {
        Long2DoubleMap map = new Long2DoubleValueSortedArrayMap(SortedKeyIndex.empty(), new double[0]);
        assertThat(map.size(), equalTo(0));
        assertThat(map.isEmpty(), equalTo(true));
        assertThat(map.keySet(), hasSize(0));
        assertThat(map.entrySet(), hasSize(0));
        assertThat(map.values(), hasSize(0));
        assertThat(map.long2DoubleEntrySet(), hasSize(0));
        assertThat(map.get(42L), equalTo(0.0));
        assertThat(map.get((Long) 42L), nullValue());
    }

    @Test
    public void testSingletonMap() {
        Long2DoubleMap map = new Long2DoubleValueSortedArrayMap(SortedKeyIndex.create(42),
                                                                      new double[]{3.5});
        assertThat(map.get(42L), equalTo(3.5));
        assertThat(map.size(), equalTo(1));
        assertThat(map.isEmpty(), equalTo(false));
        assertThat(map.keySet(), contains(42L));
        assertThat(map.values(), contains(3.5));
        assertThat(map.entrySet(), hasSize(1));
        Map.Entry<Long, Double> ent = Iterables.getFirst(map.entrySet(), null);
        assertThat(ent, notNullValue());
        assertThat(ent.getKey(), equalTo(42L));
        assertThat(ent.getValue(), equalTo(3.5));
        assertThat(map.entrySet().contains(Pair.of(42L, 3.5)),
                   equalTo(true));
        assertThat(map.entrySet().contains(Pair.of(42L, 3.7)),
                   equalTo(false));
        assertThat(map.entrySet().contains(Pair.of(41L, 3.5)),
                   equalTo(false));
        assertThat(Iterables.getFirst(map.entrySet(), null),
                   equalTo((Object) Pair.of(42L, 3.5)));
        assertThat(Iterables.getLast(map.entrySet(), null),
                   equalTo((Object) Pair.of(42L, 3.5)));
    }

    @Test
    public void testTwoElementMap() {
        Long2DoubleMap map = new Long2DoubleValueSortedArrayMap(SortedKeyIndex.create(37, 42),
                                                                new double[]{3.5, 4.5});
        assertThat(map.get(37L), equalTo(3.5));
        assertThat(map.get(42L), equalTo(4.5));
        assertThat(map.size(), equalTo(2));
        assertThat(map.isEmpty(), equalTo(false));
        assertThat(map.keySet(), contains(42L, 37L));
        assertThat(map.values(), contains(4.5, 3.5));
        assertThat(map.entrySet(), hasSize(2));
        Map.Entry<Long, Double> ent = Iterables.getFirst(map.entrySet(), null);
        assertThat(ent, notNullValue());
        assertThat(ent.getKey(), equalTo(42L));
        assertThat(ent.getValue(), equalTo(4.5));
        assertThat(map.entrySet().contains(Pair.of(42L, 4.5)),
                   equalTo(true));
        assertThat(map.entrySet().contains(Pair.of(42L, 3.5)),
                   equalTo(false));
        assertThat(map.entrySet().contains(Pair.of(41L, 3.5)),
                   equalTo(false));
        assertThat(Iterables.getFirst(map.entrySet(), null),
                   equalTo((Object) Pair.of(42L, 4.5)));
        assertThat(Iterables.getLast(map.entrySet(), null),
                   equalTo((Object) Pair.of(37L, 3.5)));
    }

    @Test
    public void testCreateWithLists() {
        for (Set<Long> keys: someSets(longs(), integers(0, 500))) {
            SortedKeyIndex dom = SortedKeyIndex.fromCollection(keys);
            double[] values = new double[dom.size()];
            for (int i = 0; i < dom.size(); i++) {
                values[i] = doubles().next();
            }
            Long2DoubleMap map = new Long2DoubleValueSortedArrayMap(dom, values);
            assertThat(map.size(), equalTo(dom.size()));

            assertThat(map.size(), equalTo(keys.size()));
            assertThat(map.keySet(), equalTo(keys));
            for (Long k: keys) {
                assertThat(map.containsKey(k), equalTo(true));
                assertThat(map.get(k), equalTo(values[dom.getIndex(k)]));
            }

            // iterate
            double lv = Double.MAX_VALUE;
            for (Long2DoubleMap.Entry e: map.long2DoubleEntrySet()) {
                assertThat(e.getDoubleValue(), equalTo(values[dom.getIndex(e.getLongKey())]));
                assertThat(e.getDoubleValue(), lessThanOrEqualTo(lv));
                lv = e.getDoubleValue();
            }

            // fast iterate
            lv = Double.MAX_VALUE;
            for (Long2DoubleMap.Entry e: Vectors.fastEntries(map)) {
                assertThat(e.getDoubleValue(), equalTo(values[dom.getIndex(e.getLongKey())]));
                assertThat(e.getDoubleValue(), lessThanOrEqualTo(lv));
                lv = e.getDoubleValue();
            }
        }
    }
}
