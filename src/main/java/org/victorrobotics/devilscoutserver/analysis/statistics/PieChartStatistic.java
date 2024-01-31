package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class PieChartStatistic extends Statistic {
  public final int      total;
  public final int[]    counts;
  public final String[] labels;

  public PieChartStatistic(String name, Collection<String> labels, int[] counts, int total) {
    super(StatisticType.PERCENTAGE, name);
    this.labels = labels.toArray(new String[labels.size()]);
    this.counts = counts.clone();
    this.total = total;
  }

  public static PieChartStatistic direct(String name, Map<String, List<DataEntry>> matchEntries,
                                         List<String> labels, String path) {
    return computed(name, matchEntries.values(), labels, entry -> entry.getIntegers(path));
  }

  @SuppressWarnings("java:S4276") // use ToDoubleFunction<Entry> instead
  public static PieChartStatistic computed(String name, Iterable<List<DataEntry>> matchEntries,
                                           Collection<String> labels,
                                           Function<DataEntry, Collection<Integer>> function) {
    int total = 0;
    int[] counts = new int[labels.size()];
    for (List<DataEntry> entries : matchEntries) {
      if (entries.isEmpty()) continue;

      int total2 = 0;
      int[] counts2 = new int[labels.size()];

      for (DataEntry entry : entries) {
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
    return new PieChartStatistic(name, labels, counts, total);
  }
}
