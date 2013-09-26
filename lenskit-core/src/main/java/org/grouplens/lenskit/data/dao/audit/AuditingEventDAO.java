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
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * An event DAO that audits cursors.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class AuditingEventDAO implements EventDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuditingEventDAO.class);
    private final AuditController controller;
    private final EventDAO delegate;

    @Inject
    public AuditingEventDAO(AuditController ctrl, @Audited EventDAO dao) {
        controller = ctrl;
        delegate = dao;
    }

    @Override
    public Cursor<Event> streamEvents() {
        String name = String.format("events[%d]", controller.freshId());
        logger.debug("opening cursor {}", name);
        return AuditedCursor.wrap(logger, name, delegate.streamEvents());
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type) {
        String name = String.format("events(%s)[%d]", type, controller.freshId());
        logger.debug("opening cursor {}", name);
        return AuditedCursor.wrap(logger, name, delegate.streamEvents(type));
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder order) {
        String name = String.format("events(%s,%s)[%d]", type, order, controller.freshId());
        logger.debug("opening cursor {}", name);
        return AuditedCursor.wrap(logger, name, delegate.streamEvents(type, order));
    }
}
