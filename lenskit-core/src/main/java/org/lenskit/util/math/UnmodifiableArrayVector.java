package org.lenskit.util.math;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * A vector that cannot be modified, useful for presenting vector views of arrays.
 */
public class UnmodifiableArrayVector extends ArrayRealVector {
    /**
     * Create an unmodifiable vector backed by an array.  Calls {@link ArrayRealVector#ArrayRealVector(double[], boolean)}
     * with a `copyArray` value of `false`.
     *
     * @param values The array.
     */
    public UnmodifiableArrayVector(double[] values) {
        super(values, false);
    }

    @Override
    public ArrayRealVector mapToSelf(UnivariateFunction function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RealVector mapAddToSelf(double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RealVector mapSubtractToSelf(double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RealVector mapMultiplyToSelf(double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RealVector mapDivideToSelf(double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEntry(int index, double value) throws OutOfRangeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubVector(int index, RealVector v) throws OutOfRangeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubVector(int index, double[] v) throws OutOfRangeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayRealVector combineToSelf(double a, double b, RealVector y) throws DimensionMismatchException {
        throw new UnsupportedOperationException();
    }
}
