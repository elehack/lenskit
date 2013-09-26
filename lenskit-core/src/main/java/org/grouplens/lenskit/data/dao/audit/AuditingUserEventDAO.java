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
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Auditing wrapper for a {@link UserEventDAO}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class AuditingUserEventDAO implements UserEventDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuditingUserEventDAO.class);
    private final AuditController controller;
    private final UserEventDAO delegate;

    @Inject
    public AuditingUserEventDAO(AuditController ctrl, @Audited UserEventDAO dao) {
        controller = ctrl;
        delegate = dao;
    }

    @Override
    public Cursor<UserHistory<Event>> streamEventsByUser() {
        String name = String.format("userProfiles[%d]", controller.freshId());
        logger.debug("opening cursor {}", name);
        return AuditedCursor.wrap(logger, name, delegate.streamEventsByUser());
    }

    @Override
    @Nullable
    public UserHistory<Event> getEventsForUser(long user) {
        return delegate.getEventsForUser(user);
    }

    @Override
    @Nullable
    public <E extends Event> UserHistory<E> getEventsForUser(long user, Class<E> type) {
        return delegate.getEventsForUser(user, type);
    }
}
