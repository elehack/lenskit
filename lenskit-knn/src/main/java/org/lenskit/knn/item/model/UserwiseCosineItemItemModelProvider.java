/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.ItemSimilarityThreshold;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.util.IdBox;
import org.lenskit.util.collections.LLDMatrix;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.collections.UnlimitedLong2DoubleAccumulator;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Item-item model builder that only uses cosine similarity, but is faster on sparse matrices.
 */
public class UserwiseCosineItemItemModelProvider implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(UserwiseCosineItemItemModelProvider.class);
    private final RatingVectorPDAO dao;
    private final UserVectorNormalizer normalizer;
    private final Threshold threshold;
    private final int modelSize;

    @Inject
    public UserwiseCosineItemItemModelProvider(@Transient RatingVectorPDAO rvd,
                                               @Transient UserVectorNormalizer norm,
                                               @ItemSimilarityThreshold Threshold thresh,
                                               @ModelSize int msize) {
        dao = rvd;
        normalizer = norm;
        threshold = thresh;
        modelSize = msize;
    }

    @Override
    public SimilarityMatrixModel get() {
        logger.info("building item-item model user-by-user");

        LLDMatrix dotProducts = LLDMatrix.newMutableMatrix();
        Long2DoubleOpenHashMap sumsOfSquares = new Long2DoubleOpenHashMap();

        try (ObjectStream<IdBox<Long2DoubleMap>> users = dao.streamUsers()) {
            for (IdBox<Long2DoubleMap> user: users) {
                logger.trace("processing user {} with {} items",
                             user.getId(), user.getValue().size());
                Long2DoubleMap normed = normalizer.makeTransformation(user.getId(), user.getValue())
                                                  .apply(user.getValue());
                for (Long2DoubleMap.Entry e: Vectors.fastEntries(normed)) {
                    long id1 = e.getLongKey();
                    double v1 = e.getDoubleValue();
                    sumsOfSquares.addTo(id1, v1 * v1);
                    for (Long2DoubleMap.Entry e2: Vectors.fastEntries(normed)) {
                        long id2 = e2.getLongKey();
                        double v2 = e2.getDoubleValue();
                        if (id1 != id2) {
                            dotProducts.addTo(id1, id2, v1 * v2);
                        }
                    }
                }
            }
        }

        Map<Long, Long2DoubleMap> result;
        result = dotProducts.rows()
                            .stream()
                            .map((box -> finishVector(box.getId(), box.getValue(), sumsOfSquares)))
                            .collect(Collectors.toMap(IdBox::getId, IdBox::getValue));
        logger.info("computed similarities for {} items", result.size());
        return new SimilarityMatrixModel(result);
    }

    private IdBox<Long2DoubleMap> finishVector(long id, Long2DoubleMap vec, Long2DoubleOpenHashMap sumsOfSquares) {
        Long2DoubleAccumulator accum;
        if (modelSize > 0) {
            accum = new TopNLong2DoubleAccumulator(modelSize);
        } else {
            accum = new UnlimitedLong2DoubleAccumulator();
        }

        double len1 = Math.sqrt(sumsOfSquares.get(id));

        for (Long2DoubleMap.Entry e: Vectors.fastEntries(vec)) {
            long j = e.getLongKey();
            double sim = e.getDoubleValue() / (len1 * Math.sqrt(sumsOfSquares.get(j)));
            if (threshold.retain(sim)) {
                accum.put(j, sim);
            }
        }
        return IdBox.create(id, accum.finishMap());
    }
}
