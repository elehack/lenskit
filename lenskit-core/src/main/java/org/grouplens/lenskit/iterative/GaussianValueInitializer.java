package org.grouplens.lenskit.iterative;

import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.lenskit.core.Parameter;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.*;
import java.util.Random;

/**
 * Value initializer that draws from a Gaussian (normal) distribution.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class GaussianValueInitializer implements ValueInitializer {
    /**
     * The mean of the Gaussian distribution from which initial values will be drawn.
     */
    @Documented
    @DefaultDouble(0.0)
    @Qualifier
    @Parameter(Double.class)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Mean {}

    /**
     * The variance of the Gaussian distribution from which to draw initial values.  The
     * variance is the square of the standard deviation.
     */
    @Documented
    @DefaultDouble(0.0)
    @Qualifier
    @Parameter(Double.class)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Variance {}

    private final Random random;
    private final double mean;
    private final double standardDeviation;

    /**
     * Construct a new Gaussian value initializer.
     * @param rng The random number generator to use.
     * @param mu The mean value to return.
     */
    @Inject
    public GaussianValueInitializer(Random rng, @Mean double mu, @Variance double var) {
        random = rng;
        mean = mu;
        standardDeviation = Math.sqrt(var);
    }

    @Override
    public double getValue() {
        return random.nextGaussian() * standardDeviation + mean;
    }
}
