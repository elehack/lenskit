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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A set of attributes.  This is a customized set implementation that is used to
 * make attribute storage more efficient.
 *
 * An attribute can only contain *one* attribute with a particular name.
 */
@Immutable
public class AttributeSet extends AbstractSet<Attribute<?>> {
    final Attribute<?>[] attributes;
    final Set<String> nameSet = new NameSet();

    AttributeSet(Attribute<?>[] attrs) {
        attributes = attrs;
    }

    /**
     * Construct a new attribute set builder.
     * @return An attribute set builder.
     */
    public static AttributeSetBuilder newBuilder() {
        return new AttributeSetBuilder();
    }

    /**
     * Create a new attribute set with some attributes.
     * @param attrs The attributes to put in the set.
     * @return An attribute set containing `attrs`.
     */
    public static AttributeSet create(Attribute<?>... attrs) {
        return newBuilder().add(attrs).build();
    }

    /**
     * Get the attribute names as a set.
     * @return The attribute names.
     */
    public Set<String> getNames() {
        return nameSet;
    }

    @Nonnull
    @Override
    public Iterator<Attribute<?>> iterator() {
        return new AttrIter();
    }

    @Override
    public int size() {
        return attributes.length;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Attribute) {
            return findIndex((Attribute<?>) o) >= 0;
        } else {
            return false;
        }
    }

    int findIndex(Attribute<?> attr) {
        final int n = attributes.length;
        for (int i = 0; i < n; i++) {
            if (attributes[i] == attr) {
                return i;
            }
        }
        return -1;
    }

    int findIndex(String name) {
        final int n = attributes.length;
        for (int i = 0; i < n; i++) {
            if (attributes[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private class AttrIter implements Iterator<Attribute<?>> {
        int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < attributes.length;
        }

        @Override
        public Attribute<?> next() {
            if (pos == attributes.length) {
                throw new NoSuchElementException();
            }
            Attribute<?> attr = attributes[pos];
            pos += 1;
            return attr;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("attribute sets are immutable");
        }
    }

    private class NameSet extends AbstractSet<String> {
        @Override
        public Iterator<String> iterator() {
            return new NameIter();
        }

        @Override
        public int size() {
            return attributes.length;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof String && findIndex((String) o) >= 0;
        }
    }

    private class NameIter implements Iterator<String> {
        int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < attributes.length;
        }

        @Override
        public String next() {
            if (pos == attributes.length) {
                throw new NoSuchElementException();
            }
            Attribute<?> attr = attributes[pos];
            pos += 1;
            return attr.getName();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("attribute sets are immutable");
        }
    }
}
