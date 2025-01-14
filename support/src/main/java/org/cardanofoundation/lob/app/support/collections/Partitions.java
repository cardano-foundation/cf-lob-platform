package org.cardanofoundation.lob.app.support.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class Partitions {

    public static<T> Iterable<Partition<T>> partition(Iterable<T> it, int size) {
        return toPartitionList(Iterables.partition(it, size));
    }

    private static <T> List<Partition<T>> toPartitionList(Iterable<? extends Iterable<T>> iterable) {
        val partitionList = new ArrayList<Partition<T>>();

        int partitionIndex = 0;
        for (val partition : iterable) {
            partitionIndex++;
            partitionList.add(new Partition<>(partition, partitionIndex, Iterables.size(iterable)));
        }

        return partitionList;
    }

    @AllArgsConstructor
    @Getter
    public static class Partition<T> {

        private final Iterable<T> elements;

        private final int partitionIndex;

        private final int totalPartitions;

        public Set<T> asSet() {
            return Sets.newHashSet(elements);
        }

        public boolean isFirst() {
            return partitionIndex == 1;
        }

        public boolean isLast() {
            return partitionIndex == totalPartitions;
        }
    }

}
