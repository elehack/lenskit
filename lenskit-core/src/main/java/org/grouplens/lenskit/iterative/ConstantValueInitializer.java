package org.grouplens.lenskit.iterative;

import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.lenskit.core.Parameter;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Value initializer that returns a constant.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConstantValueInitializer implements ValueInitializer {
    private final double value;

    /**
     * Parameter: the value used by the constant initializer.
     */
    @Documented
    @DefaultDouble(0.1)
    @Qualifier
    @Parameter(Double.class)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Value {}

    /**
     * Construct a new constant value initializer.
     * @param v The value to return.
     */
    @Inject
    public ConstantValueInitializer(@Value double v) {
        value = v;
    }

    @Override
    public double getValue() {
        return value;
    }
}
