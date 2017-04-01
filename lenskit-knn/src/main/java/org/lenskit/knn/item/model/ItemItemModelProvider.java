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

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.ItemSimilarityThreshold;
import org.lenskit.knn.item.MinCommonUsers;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.util.ProgressLogger;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.collections.UnlimitedLong2DoubleAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Build an item-item CF model from rating data.
 * This builder takes a very simple approach. It does not allow for vector
 * normalization and truncates on the fly.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@NotThreadSafe
public class ItemItemModelProvider implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelProvider.class);

    private final ItemSimilarity itemSimilarity;
    private final ItemItemBuildContext buildContext;
    private final Threshold threshold;
    private final NeighborIterationStrategy neighborStrategy;
    private final int minCommonUsers;
    private final int modelSize;

    @Inject
    public ItemItemModelProvider(@Transient ItemSimilarity similarity,
                                 @Transient ItemItemBuildContext context,
                                 @Transient @ItemSimilarityThreshold Threshold thresh,
                                 @Transient NeighborIterationStrategy nbrStrat,
                                 @MinCommonUsers int minCU,
                                 @ModelSize int size) {
        itemSimilarity = similarity;
        buildContext = context;
        threshold = thresh;
        neighborStrategy = nbrStrat;
        minCommonUsers = minCU;
        modelSize = size;
    }

    @Override
    public SimilarityMatrixModel get() {
        logger.info("building item-item model for {} items", buildContext.getItems().size());
        logger.debug("using similarity function {}", itemSimilarity);
        logger.debug("similarity function is {}",
                     itemSimilarity.isSparse() ? "sparse" : "non-sparse");
        logger.debug("similarity function is {}",
                     itemSimilarity.isSymmetric() ? "symmetric" : "non-symmetric");

        LongSortedSet allItems = buildContext.getItems();

        Long2ObjectMap<Long2DoubleAccumulator> rows = makeAccumulators(allItems);

        final int nitems = allItems.size();
        LongIterator outer = allItems.iterator();

        ProgressLogger progress = ProgressLogger.create(logger)
                                                .setCount(nitems)
                                                .setLabel("item-item model build")
                                                .setWindow(50)
                                                .start();
        final AtomicInteger npairs = new AtomicInteger(0);

        allItems.parallelStream().forEach((itemId1) -> {
            Long2DoubleSortedMap vec1 = buildContext.itemVector(itemId1);
            if (vec1.size() < minCommonUsers) {
                // if it doesn't have enough users, it can't have enough common users
                if (logger.isTraceEnabled()) {
                    logger.trace("item {} has {} (< {}) users, skipping", itemId1, vec1.size(), minCommonUsers);
                }
                progress.advance();
                return;
            }

            LongIterator itemIter = neighborStrategy.neighborIterator(buildContext, itemId1,
                                                                      itemSimilarity.isSymmetric());

            Long2DoubleAccumulator row = rows.get(itemId1);
            while (itemIter.hasNext()) {
                long itemId2 = itemIter.nextLong();
                if (itemId1 != itemId2) {
                    Long2DoubleSortedMap vec2 = buildContext.itemVector(itemId2);
                    if (!LongUtils.hasNCommonItems(vec1.keySet(), vec2.keySet(), minCommonUsers)) {
                        // items have insufficient users in common, skip them
                        continue;
                    }

                    double sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);
                    if (threshold.retain(sim)) {
                        synchronized(row) {
                            row.put(itemId2, sim);
                        }
                        npairs.incrementAndGet();
                        if (itemSimilarity.isSymmetric()) {
                            Long2DoubleAccumulator r2 = rows.get(itemId2);
                            synchronized (r2) {
                                r2.put(itemId1, sim);
                            }
                            npairs.incrementAndGet();
                        }
                    }
                }
            }

            progress.advance();
        });

        progress.finish();
        logger.info("built model of {} similarities for {} items in {}",
                    npairs, allItems.size(), progress.elapsedTime());

        return new SimilarityMatrixModel(finishRows(rows));
    }

    private Long2ObjectMap<Long2DoubleAccumulator> makeAccumulators(LongSet items) {
        Long2ObjectMap<Long2DoubleAccumulator> rows = new Long2ObjectOpenHashMap<>(items.size());
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            Long2DoubleAccumulator accum;
            if (modelSize == 0) {
                accum = new UnlimitedLong2DoubleAccumulator();
            } else {
                accum = new TopNLong2DoubleAccumulator(modelSize);
            }
            rows.put(item, accum);
        }
        return rows;
    }

    private Long2ObjectMap<Long2DoubleMap> finishRows(Long2ObjectMap<Long2DoubleAccumulator> rows) {
        Long2ObjectMap<Long2DoubleMap> results = new Long2ObjectOpenHashMap<>(rows.size());
        for (Long2ObjectMap.Entry<Long2DoubleAccumulator> e: rows.long2ObjectEntrySet()) {
            results.put(e.getLongKey(), e.getValue().finishMap());
        }
        return results;
    }
}
