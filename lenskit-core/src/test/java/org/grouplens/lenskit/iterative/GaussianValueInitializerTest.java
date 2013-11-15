package org.grouplens.lenskit.iterative;

import org.junit.Test;

import java.util.Random;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

/**
 * Test the Gaussian value initializer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class GaussianValueInitializerTest {
    private static final int NUM_TRIALS = 1000;

    @Test
    public void testZeroMean() {
        ValueInitializer init = new GaussianValueInitializer(new Random(), 0, 1.0);
        double sum = 0;
        for (int i = 0; i < NUM_TRIALS; i++) {
            sum += init.getValue();
        }
        assertThat(sum / NUM_TRIALS, closeTo(0, 0.1));
    }

    @Test
    public void testNonzeroMean() {
        ValueInitializer init = new GaussianValueInitializer(new Random(), 5, 1.0);
        double sum = 0;
        for (int i = 0; i < NUM_TRIALS; i++) {
            sum += init.getValue();
        }
        assertThat(sum / NUM_TRIALS, closeTo(5, 0.1));
    }
}
