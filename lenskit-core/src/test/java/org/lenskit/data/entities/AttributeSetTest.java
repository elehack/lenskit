/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.data.entities;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class AttributeSetTest {
    @Test
    public void testEmptySet() {
        AttributeSet set = AttributeSet.create();
        assertThat(set.isEmpty(), equalTo(true));
        assertThat(set, hasSize(0));
    }

    @Test
    public void testRatingSet() {
        AttributeSet set = AttributeSet.create(CommonAttributes.USER_ID,
                                               CommonAttributes.ITEM_ID,
                                               CommonAttributes.RATING);
        assertThat(set.isEmpty(), equalTo(false));
        assertThat(set, hasSize(3));
        assertThat(set, (Matcher) contains(CommonAttributes.USER_ID,
                                           CommonAttributes.ITEM_ID,
                                           CommonAttributes.RATING));
        assertThat(set.getNames(), contains("user", "item", "rating"));
        assertThat(set.contains(CommonAttributes.USER_ID), equalTo(true));
        assertThat(set.contains(CommonAttributes.TIMESTAMP), equalTo(false));
        assertThat(set.getNames().contains("user"),
                   equalTo(true));
        assertThat(set.getNames().contains("timestamp"),
                   equalTo(false));
    }
}
