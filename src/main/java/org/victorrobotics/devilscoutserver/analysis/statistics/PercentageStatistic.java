package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.Entry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class PercentageStatistic extends Statistic {
  public final int      total;
  public final int[]    counts;
  public final String[] labels;

  public PercentageStatistic(String name, Collection<String> labels, int[] counts, int total) {
    super(StatisticType.PERCENTAGE, name);
    this.labels = labels.toArray(new String[labels.size()]);
    this.counts = counts.clone();
    this.total = total;
  }

  public static PercentageStatistic direct(String name, Map<String, List<Entry>> entryMap,
                                           List<String> labels, String path) {
    return computed(name, entryMap, labels, entry -> entry.getIntegers(path));
  }

  @SuppressWarnings("java:S4276") // use ToDoubleFunction<Entry> instead
  public static PercentageStatistic computed(String name, Map<String, List<Entry>> entryMap,
                                             Collection<String> labels,
                                             Function<Entry, Collection<Integer>> function) {
    int total = 0;
    int[] counts = new int[labels.size()];
    for (List<Entry> entries : entryMap.values()) {
      if (entries.isEmpty()) continue;

      int total2 = 0;
      int[] counts2 = new int[labels.size()];

      for (Entry entry : entries) {
        Collection<Integer> indexes = function.apply(entry);
        if (indexes != null) {
          for (int index : indexes) {
            counts2[index]++;
          }
          total2++;
        }
      }

      if (total2 == 0) continue;

      int threshold = (total2 + 1) / 2;
      for (int i = 0; i < counts.length; i++) {
        if (counts2[i] >= threshold) {
          counts[i]++;
        }
      }
      total++;
    }
    return new PercentageStatistic(name, labels, counts, total);
  }
}
