package org.grouplens.lenskit.iterative;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test the constant-value initializer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConstantValueInitializerTest {
    @Test
    public void testCVI() throws Exception {
        ValueInitializer init = new ConstantValueInitializer(0);
        assertThat(init.getValue(), equalTo(0.0));
    }
}
