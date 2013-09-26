/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.dao.audit;

import org.grouplens.lenskit.cursors.Cursor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * A cursor that logs when it closes.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class AuditedCursor<E> implements Cursor<E> {
    private final Logger logger;
    private final String name;
    private final Cursor<E> cursor;

    /**
     * Create a new audited cursor.
     * @param log The logger to log cursor activity.
     * @param name The name of the cursor.
     * @param cur The underlying cursor to audit.
     * @param <E> The cursor element type.
     * @return An auditing cursor logging {@code cur} will be closed.
     */
    public static <E> Cursor<E> wrap(Logger log, String name, Cursor<E> cur) {
        return new AuditedCursor<E>(log, name, cur);
    }

    private AuditedCursor(Logger log, String n, Cursor<E> cur) {
        logger = log;
        name = n;
        cursor = cur;
    }

    @Override
    public int getRowCount() {
        return cursor.getRowCount();
    }

    @Override
    public boolean hasNext() {
        return cursor.hasNext();
    }

    @Override
    @Nonnull
    public E next() {
        return cursor.next();
    }

    @Override
    @Nonnull
    public E fastNext() {
        return cursor.fastNext();
    }

    @Override
    public Iterable<E> fast() {
        return cursor.fast();
    }

    @Override
    public void close() {
        try {
            cursor.close();
        } finally {
            logger.debug("closed cursor {}", name);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return cursor.iterator();
    }

    @Override
    public String toString() {
        return "AuditedCursor(" + name + ")";
    }
}
