package org.grouplens.lenskit.iterative;

/**
 * Initializer for double values.  This is used for things like initializing parameters for an
 * iterative method.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface ValueInitializer {
    /**
     * Return another initializer value.
     * @return The value.
     */
    double getValue();
}
