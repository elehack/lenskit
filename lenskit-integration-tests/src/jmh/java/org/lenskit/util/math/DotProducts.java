package org.lenskit.util.math;

import it.unimi.dsi.fastutil.doubles.DoubleLinkedOpenCustomHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.*;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;
import org.openjdk.jmh.annotations.*;

import java.util.Iterator;

@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class DotProducts {
    final static Generator<Integer> sizes = PrimitiveGenerators.integers(10, 500);
    final static Generator<Long> keys = PrimitiveGenerators.longs(0, 2000);
    final static Generator<Double> values = PrimitiveGenerators.doubles(1, 5);
    private final static int VECTORS_PER_INVOCATION = 1000;

    @State(Scope.Thread)
    public static class HashMaps {
        Long2DoubleMap[] maps;

        @Setup(Level.Iteration)
        public void setup() {
            maps = new Long2DoubleMap[VECTORS_PER_INVOCATION + 1];
            for (int i = 0; i < VECTORS_PER_INVOCATION + 1; i++) {
                int size = sizes.next();
                Long2DoubleOpenHashMap vec = new Long2DoubleOpenHashMap(size);
                for (int j = 0; j < size; j++) {
                    vec.put(keys.next(), values.next());
                }
                maps[i] = vec;
            }
        }
    }

    @State(Scope.Thread)
    public static class ArrayMaps {
        Long2DoubleMap[] maps;

        @Setup(Level.Iteration)
        public void setup() {
            maps = new Long2DoubleMap[VECTORS_PER_INVOCATION + 1];
            for (int i = 0; i < VECTORS_PER_INVOCATION + 1; i++) {
                int size = sizes.next();
                LongSet ks = new LongOpenHashSet(size);
                double[] vs = new double[size];
                while (ks.size() < size) {
                    ks.add(keys.next());
                }
                for (int j = 0; j < size; j++) {
                    vs[j] = values.next();
                }
                SortedKeyIndex idx = SortedKeyIndex.fromCollection(ks);
                maps[i] = Long2DoubleSortedArrayMap.fromArray(idx, vs);
            }
        }
    }

    @State(Scope.Thread)
    public static class SparseVectors {
        SparseVector[] maps;

        @Setup(Level.Iteration)
        public void setup() {
            maps = new SparseVector[VECTORS_PER_INVOCATION + 1];
            for (int i = 0; i < VECTORS_PER_INVOCATION + 1; i++) {
                int size = sizes.next();
                LongSet ks = new LongOpenHashSet(size);
                while (ks.size() < size) {
                    ks.add(keys.next());
                }
                MutableSparseVector msv = MutableSparseVector.create(ks);
                for (VectorEntry e: msv.view(VectorEntry.State.EITHER)) {
                    msv.set(e, values.next());
                }
                maps[i] = msv.freeze();
            }
        }
    }

    @Benchmark
    public void multiplyHashVectors(HashMaps maps) {
        for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
            double dp = Vectors.dotProduct(maps.maps[i], maps.maps[i+1]);
        }
    }

    @Benchmark
    public void multiplySortedVectors(ArrayMaps maps) {
        for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
            double dp = Vectors.dotProduct(maps.maps[i], maps.maps[i+1]);
        }
    }

    @Benchmark
    public void multiplySortedVectorsNaively(ArrayMaps maps) {
        for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
            double dp = naiveDotProduct(maps.maps[i], maps.maps[i+1]);
        }
    }

    @Benchmark
    public void multiplySortedVectorsSuperfast(ArrayMaps maps) {
        for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
            double dp = superfastDotProduct(maps.maps[i], maps.maps[i+1]);
        }
    }

    @Benchmark
    public void multiplySortedVectorsSuperSuperfast(ArrayMaps maps) {
        for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
            double dp = superSuperfastDotProduct(maps.maps[i], maps.maps[i+1]);
        }
    }

    @Benchmark
    public void multiplySortedVectorsKV(ArrayMaps maps) {
        for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
            Long2DoubleSortedArrayMap m1 = (Long2DoubleSortedArrayMap) maps.maps[i];
            Long2DoubleSortedArrayMap m2 = (Long2DoubleSortedArrayMap) maps.maps[i+1];
            double dp = kvDotProduct(m1.keyIndex(), m1.valueVector(),
                                     m2.keyIndex(), m2.valueVector());
        }
    }

    @Benchmark
    public void multiplySparseVectors(SparseVectors maps) {
        for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
            double dp = maps.maps[i].dot(maps.maps[i+1]);
        }
    }

    static double naiveDotProduct(Long2DoubleMap v1, Long2DoubleMap v2) {
        if (v1.size() > v2.size()) {
            // compute dot product the other way around for speed
            return naiveDotProduct(v2, v1);
        }

        double result = 0;

        Long2DoubleFunction v2d = Vectors.adaptDefaultValue(v2, 0.0);
        Iterator<Long2DoubleMap.Entry> iter = Vectors.fastEntryIterator(v1);
        while (iter.hasNext()) {
            Long2DoubleMap.Entry e = iter.next();
            long k = e.getLongKey();
            result += e.getDoubleValue() * v2d.get(k); // since default is 0
        }

        return result;
    }

    static double superfastDotProduct(Long2DoubleMap v1, Long2DoubleMap v2) {
        Long2DoubleSortedArrayMap sv1 = (Long2DoubleSortedArrayMap) v1;
        Long2DoubleSortedArrayMap sv2 = (Long2DoubleSortedArrayMap) v2;
        LongList kl1 = sv1.keyList();
        LongList kl2 = sv2.keyList();
        DoubleList vl1 = sv1.values();
        DoubleList vl2 = sv2.values();

        final int sz1 = v1.size();
        final int sz2 = v2.size();

        double result = 0;

        int i1 = 0, i2 = 0;
        while (i1 < sz1 && i2 < sz2) {
            final long k1 = kl1.getLong(i1);
            final long k2 = kl2.getLong(i2);
            if (k1 < k2) {
                i1++;
            } else if (k2 < k1) {
                i2++;
            } else {
                result += vl1.getDouble(i1) * vl2.getDouble(i2);
                i1++;
                i2++;
            }
        }

        return result;
    }

    static double superSuperfastDotProduct(Long2DoubleMap v1, Long2DoubleMap v2) {
        Long2DoubleSortedArrayMap sv1 = (Long2DoubleSortedArrayMap) v1;
        Long2DoubleSortedArrayMap sv2 = (Long2DoubleSortedArrayMap) v2;

        final int sz1 = v1.size();
        final int sz2 = v2.size();

        double result = 0;

        int i1 = 0, i2 = 0;
        while (i1 < sz1 && i2 < sz2) {
            final long k1 = sv1.getKeyByIndex(i1);
            final long k2 = sv2.getKeyByIndex(i2);
            if (k1 < k2) {
                i1++;
            } else if (k2 < k1) {
                i2++;
            } else {
                result += sv1.getValueByIndex(i1) * sv2.getValueByIndex(i2);
                i1++;
                i2++;
            }
        }

        return result;
    }

    static double kvDotProduct(SortedKeyIndex ks1, ArrayRealVector v1, SortedKeyIndex ks2, ArrayRealVector v2) {
        final int ub1 = ks1.getUpperBound();
        final int ub2 = ks2.getUpperBound();

        double[] va1 = v1.getDataRef();
        double[] va2 = v2.getDataRef();

        double result = 0;

        int i1 = ks1.getLowerBound();
        int i2 = ks2.getLowerBound();
        while (i1 < ub1 && i2 < ub2) {
            final long k1 = ks1.getKey(i1);
            final long k2 = ks2.getKey(i2);
            if (k1 < k2) {
                i1++;
            } else if (k2 < k1) {
                i2++;
            } else {
                result += va1[i1] * va2[i2];
                i1++;
                i2++;
            }
        }

        return result;
    }
}
