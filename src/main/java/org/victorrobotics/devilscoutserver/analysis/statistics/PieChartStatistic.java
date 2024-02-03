package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class PieChartStatistic extends Statistic {
  public static record LabeledCount(String label,
                                    int count) {}

  public final List<LabeledCount> slices;
  public final int                total;

  public PieChartStatistic(String name) {
    super(StatisticType.PIE_CHART, name);
    this.slices = null;
    this.total = 0;
  }

  public PieChartStatistic(String name, List<LabeledCount> slices, int total) {
    super(StatisticType.PIE_CHART, name);
    this.slices = List.copyOf(slices);
    this.total = total;
  }

  public static PieChartStatistic
      directMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                  List<String> labels, String path) {
    return computedMatch(name, matchEntries, labels, entry -> entry.getIntegers(path));
  }

  public static PieChartStatistic
      computedMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                    List<String> labels, Function<DataEntry, Collection<Integer>> function) {
    int total = 0;
    int[] counts = new int[labels.size()];
    for (Collection<DataEntry> entries : matchEntries) {
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

    if (total == 0) {
      return new PieChartStatistic(name);
    }

    List<LabeledCount> slices = new ArrayList<>();
    for (int i = 0; i < counts.length; i++) {
      slices.add(new LabeledCount(labels.get(i), counts[i]));
    }
    return new PieChartStatistic(name, slices, total);
  }

  public static PieChartStatistic
      computedMatchCounts(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                          List<String> labels, Function<DataEntry, List<Integer>> function) {
    int total = 0;
    int[] counts = new int[labels.size()];
    for (Collection<DataEntry> entries : matchEntries) {
      if (entries.isEmpty()) continue;

      int total2 = 0;
      int[] counts2 = new int[labels.size()];

      for (DataEntry entry : entries) {
        List<Integer> counts3 = function.apply(entry);
        if (counts3 != null) {
          for (int i = 0; i < counts3.size(); i++) {
            counts2[i] += counts3.get(i);
          }
          total2++;
        }
      }

      if (total2 == 0) continue;

      for (int i = 0; i < counts.length; i++) {
        counts[i] += (counts2[i] + total2 / 2) / total2;
      }
      total++;
    }

    if (total == 0) {
      return new PieChartStatistic(name);
    }

    List<LabeledCount> slices = new ArrayList<>();
    for (int i = 0; i < counts.length; i++) {
      slices.add(new LabeledCount(labels.get(i), counts[i]));
    }
    return new PieChartStatistic(name, slices, total);
  }

  public static PieChartStatistic directPit(String name, Iterable<DataEntry> pitEntries,
                                            List<String> labels, String path) {
    return computedPit(name, pitEntries, labels, entry -> entry.getIntegers(path));
  }

  public static PieChartStatistic computedPit(String name, Iterable<DataEntry> pitEntries,
                                              List<String> labels,
                                              Function<DataEntry, Collection<Integer>> function) {
    int total = 0;
    int[] counts = new int[labels.size()];
    for (DataEntry entry : pitEntries) {
      Collection<Integer> indexes = function.apply(entry);
      if (indexes == null) continue;

      for (int index : indexes) {
        counts[index]++;
      }
      total++;
    }

    if (total == 0) {
      return new PieChartStatistic(name);
    }

    List<LabeledCount> slices = new ArrayList<>();
    for (int i = 0; i < counts.length; i++) {
      slices.add(new LabeledCount(labels.get(i), counts[i]));
    }
    return new PieChartStatistic(name, slices, total);
  }
}
