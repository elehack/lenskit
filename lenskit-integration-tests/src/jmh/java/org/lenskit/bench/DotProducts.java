package org.lenskit.bench;

import com.sun.tools.corba.se.idl.PrimitiveGen;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.CombinedGenerators;
import net.java.quickcheck.generator.Generators;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.text.EventFormat;
import org.grouplens.lenskit.data.text.Formats;
import org.grouplens.lenskit.data.text.TextEventDAO;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;
import org.lenskit.util.math.Vectors;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.util.Arrays;

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
            maps = new Long2DoubleMap[VECTORS_PER_INVOCATION];
            for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
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
            maps = new Long2DoubleMap[VECTORS_PER_INVOCATION];
            for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
                int size = sizes.next();
                long[] ks = new long[size];
                double[] vs = new double[size];
                for (int j = 0; j < size; j++) {
                    ks[j] = keys.next();
                    vs[j] = values.next();
                }
                SortedKeyIndex idx = SortedKeyIndex.create(ks);
                maps[i] = Long2DoubleSortedArrayMap.fromArray(idx, vs);
            }
        }
    }

    @Benchmark
    public void multiplyHashVectors(HashMaps maps) {
        for (int i = 0; i < VECTORS_PER_INVOCATION; i++) {
            for (int j = 0; j < VECTORS_PER_INVOCATION; j++) {
                double dp = Vectors.dotProduct(maps.maps[i], maps.maps[j]);
            }
        }
    }
}
