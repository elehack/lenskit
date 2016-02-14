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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Build a set of attributes for describing entities.
 */
public class AttributeSetBuilder {
    private Set<Attribute> attributes = new LinkedHashSet<>();
    private Set<String> names = new HashSet<>();

    /**
     * Add an attribute to the set.
     * @param attr The attribute to add.
     * @return The builder (for chaining).
     */
    public AttributeSetBuilder add(Attribute<?> attr) {
        addWithIndex(attr);
        return this;
    }

    @SuppressWarnings("unchecked")
    int addWithIndex(Attribute<?> attr) {
        if (!attributes.contains(attr) && names.contains(attr.getName())) {
            throw new IllegalArgumentException("attribute with name " + attr.getName() + "already added");
        }
        boolean added = attributes.add(attr);
        names.add(attr.getName());
        if (added) {
            return attributes.size() - 1;
        } else {
            return Iterables.indexOf(attributes, (Predicate) Predicates.equalTo(attr));
        }
    }

    /**
     * Add some attributes to the set.
     * @param attrs The attributes to add.
     * @return The builder (for chaining).
     */
    public AttributeSetBuilder add(Attribute<?>... attrs) {
        for (Attribute<?> a: attrs) {
            add(a);
        }
        return this;
    }

    public AttributeSet build() {
        return new AttributeSet(attributes.toArray(new Attribute[attributes.size()]));
    }
}
