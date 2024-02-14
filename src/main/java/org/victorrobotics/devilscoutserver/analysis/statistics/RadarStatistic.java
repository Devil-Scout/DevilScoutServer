package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class RadarStatistic extends Statistic {
  public static record RadarPoint(String label,
                                  Number value) {}

  public final double           max;
  public final List<RadarPoint> points;

  public RadarStatistic(String name, double max, List<RadarPoint> points) {
    super(StatisticType.RADAR, name);
    this.max = max;
    this.points = List.copyOf(points);
  }

  @SuppressWarnings("java:S3242") // Iterable instead of List
  public static RadarStatistic directMatch(String name,
                                           Iterable<? extends Collection<DataEntry>> pitEntries,
                                           List<String> paths, double max, List<String> labels) {
    List<Function<DataEntry, Number>> functions = new ArrayList<>();
    for (String path : paths) {
      functions.add(entry -> entry.getInteger(path));
    }
    return computedMatch(name, pitEntries, max, labels, functions);
  }

  public static RadarStatistic
      computedMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries, double max,
                    List<String> labels, List<Function<DataEntry, Number>> functions) {
    int[] counts = new int[labels.size()];
    double[] sums = new double[labels.size()];
    int[] counts2 = new int[labels.size()];
    double[] sums2 = new double[labels.size()];

    for (Collection<DataEntry> entries : matchEntries) {
      Arrays.fill(counts2, 0);
      Arrays.fill(sums2, 0);

      for (DataEntry entry : entries) {
        for (int i = 0; i < sums.length; i++) {
          Number val = functions.get(i)
                                .apply(entry);
          if (val == null) continue;

          counts2[i]++;
          sums2[i] += val.doubleValue();
        }
      }

      for (int i = 0; i < sums2.length; i++) {
        if (counts2[i] == 0) continue;

        sums[i] += sums2[i] / counts2[i];
        counts[i]++;
      }
    }

    List<RadarPoint> points = new ArrayList<>();

    for (int i = 0; i < sums.length; i++) {
      String label = labels.get(i);
      if (counts[i] == 0) {
        points.add(new RadarPoint(label, null));
      } else {
        double value = sums[i] / counts[i];
        points.add(new RadarPoint(label, value));
      }
    }

    return new RadarStatistic(name, max, points);
  }

  @SuppressWarnings("java:S3242") // Iterable instead of List
  public static RadarStatistic directPit(String name, Iterable<DataEntry> pitEntries,
                                         List<String> paths, double max, List<String> labels) {
    List<Function<DataEntry, Number>> functions = new ArrayList<>();
    for (String path : paths) {
      functions.add(entry -> entry.getInteger(path));
    }
    return computedPit(name, pitEntries, max, labels, functions);
  }

  public static RadarStatistic computedPit(String name, Iterable<DataEntry> pitEntries, double max,
                                           List<String> labels,
                                           List<Function<DataEntry, Number>> functions) {
    int[] counts = new int[functions.size()];
    double[] sums = new double[functions.size()];
    for (DataEntry entry : pitEntries) {
      for (int i = 0; i < sums.length; i++) {
        Number val = functions.get(i)
                              .apply(entry);
        if (val == null) continue;

        counts[i]++;
        sums[i] += val.doubleValue();
      }
    }

    List<RadarPoint> points = new ArrayList<>();

    for (int i = 0; i < sums.length; i++) {
      String label = labels.get(i);
      if (counts[i] == 0) {
        points.add(new RadarPoint(label, null));
      } else {
        double value = sums[i] / counts[i];
        points.add(new RadarPoint(label, value));
      }
    }

    return new RadarStatistic(name, max, points);
  }
}
