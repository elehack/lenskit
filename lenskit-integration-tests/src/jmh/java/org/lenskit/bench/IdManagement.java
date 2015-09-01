package org.lenskit.bench;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.java.quickcheck.Generator;
import org.grouplens.lenskit.indexes.IdIndexMapping;
import org.grouplens.lenskit.indexes.IdIndexMappingBuilder;
import org.grouplens.lenskit.indexes.MutableIdIndexMapping;
import org.openjdk.jmh.annotations.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.java.quickcheck.generator.PrimitiveGenerators.longs;

@State(Scope.Thread)
public class IdManagement {
    MutableIdIndexMapping mutable;
    IdIndexMapping immutable;

    @Setup(Level.Iteration)
    public void setup() {
        mutable = new MutableIdIndexMapping();
        IdIndexMappingBuilder bld = new IdIndexMappingBuilder();
        for (long k = 0; k < 20000; k++) {
            bld.add(k);
            mutable.internId(k);
        }
        immutable = bld.build();
    }

    @Benchmark
    public void testMutableIntern() throws IOException {
        BufferedWriter out = Files.newBufferedWriter(Paths.get("/dev/null"), Charsets.UTF_8);
        Generator<Long> keys = longs(0, 19999);
        for (int i = 0; i < 100000; i++) {
            out.write(mutable.getIndex(keys.next()));
        }
    }

    @Benchmark
    public void testImmutableIntern() throws IOException {
        BufferedWriter out = Files.newBufferedWriter(Paths.get("/dev/null"), Charsets.UTF_8);
        Generator<Long> keys = longs(0, 19999);
        for (int i = 0; i < 100000; i++) {
            out.write(immutable.getIndex(keys.next()));
        }
    }
}
