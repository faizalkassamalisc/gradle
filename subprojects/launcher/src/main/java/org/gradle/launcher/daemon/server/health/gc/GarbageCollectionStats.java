/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.launcher.daemon.server.health.gc;

import org.gradle.internal.util.NumberUtil;

import java.lang.management.MemoryUsage;
import java.util.Set;

public class GarbageCollectionStats {
    final private double rate;
    final private long used;
    final private long max;
    final private long count;

    public GarbageCollectionStats(Set<GarbageCollectionEvent> events) {
        this.rate = calculateRate(events);
        this.used = calculateAverageUsage(events);
        this.max = calculateMaxSize(events);
        this.count = getCount(events);
    }

    static long getCount(Set<GarbageCollectionEvent> events) {
        long lastCount = 0;

        for (GarbageCollectionEvent event : events) {
            lastCount = event.getCount();
        }

        return lastCount == GarbageCollectionEvent.COUNT_UNAVAILABLE ? events.size() : lastCount;
    }

    static double calculateRate(Set<GarbageCollectionEvent> events) {
        if (events.size() < 2) {
            return Double.NaN;
        }

        long firstGC = 0;
        long lastGC = 0;
        long lastCount = GarbageCollectionEvent.COUNT_UNAVAILABLE;
        for (GarbageCollectionEvent event : events) {
            if (event.getCount() != GarbageCollectionEvent.COUNT_UNAVAILABLE) {
                // Skip if this was a polling event and the garbage collector did not fire in between events
                if (event.getCount() == lastCount || event.getCount() == 0) {
                    continue;
                }
            }

            lastCount = event.getCount();

            if (firstGC == 0) {
                firstGC = event.getTimestamp();
            } else {
                lastGC = event.getTimestamp();
            }
        }
        long elapsed = lastGC - firstGC;
        long gcCount = (lastCount == GarbageCollectionEvent.COUNT_UNAVAILABLE ? events.size() : lastCount) - 1;
        return ((double)gcCount) / elapsed * 1000;
    }

    static long calculateAverageUsage(Set<GarbageCollectionEvent> events) {
        if (events.size() < 1) {
            return 0;
        }

        long total = 0;
        long lastCount = GarbageCollectionEvent.COUNT_UNAVAILABLE;
        for (GarbageCollectionEvent event : events) {
            if (event.getCount() != GarbageCollectionEvent.COUNT_UNAVAILABLE) {
                // Skip if this was a polling event and the garbage collector did not fire in between events
                if (event.getCount() == lastCount || event.getCount() == 0) {
                    continue;
                }
            }

            MemoryUsage usage = event.getUsage();
            if (event.getCount() != GarbageCollectionEvent.COUNT_UNAVAILABLE) {
                if (lastCount == GarbageCollectionEvent.COUNT_UNAVAILABLE) {
                    total += (usage.getUsed() * event.getCount());
                } else {
                    total += (usage.getUsed() * (event.getCount() - lastCount));
                }
            } else {
                total += usage.getUsed();
            }

            lastCount = event.getCount();
        }
        long gcCount = lastCount == GarbageCollectionEvent.COUNT_UNAVAILABLE ? events.size() : lastCount;
        return total / gcCount;
    }

    static long calculateMaxSize(Set<GarbageCollectionEvent> events) {
        if (events.size() < 2) {
            return 0;
        }

        // Maximum pool size is fixed, so we should only need to get it from the first event
        MemoryUsage usage = events.iterator().next().getUsage();
        return usage.getMax();
    }

    public double getRate() {
        return rate;
    }

    public int getUsage() {
        return NumberUtil.percentOf(used, max);
    }

    public double getUsed() {
        return used;
    }

    public long getMax() {
        return max;
    }

    public long getCount() {
        return count;
    }
}
