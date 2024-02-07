package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class NumberStatistic extends Statistic {
  public final Double mean;
  public final Double stddev;
  public final Double max;

  public NumberStatistic(String name) {
    super(StatisticType.NUMBER, name);
    this.mean = null;
    this.stddev = null;
    this.max = null;
  }

  public NumberStatistic(String name, Double mean, Double stddev, Double max) {
    super(StatisticType.NUMBER, name);
    this.mean = mean;
    this.stddev = stddev;
    this.max = max;
  }

  public static NumberStatistic fromData(String name, Iterable<? extends Number> numbers) {
    double max = Double.NEGATIVE_INFINITY;
    double sum = 0;
    double squaredSum = 0;
    int count = 0;

    for (Number num : numbers) {
      double val = num.doubleValue();
      sum += val;
      squaredSum += val * val;
      count++;
      max = Math.max(val, max);
    }

    if (count == 0) {
      return new NumberStatistic(name, null, null, null);
    }

    Double mean = sum / count;
    Double stddev = Math.sqrt(Math.abs(squaredSum - (sum * sum / count)) / count);

    return new NumberStatistic(name, mean, stddev, max);
  }

  public static NumberStatistic directMatch(String name,
                                            Iterable<? extends Collection<DataEntry>> matchEntries,
                                            String path) {
    return computedMatch(name, matchEntries, entry -> entry.getInteger(path));
  }

  public static NumberStatistic
      computedMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                    Function<DataEntry, Number> function) {
    Collection<Double> numbers = new ArrayList<>();

    for (Collection<DataEntry> entries : matchEntries) {
      if (entries.isEmpty()) continue;

      double sum = 0;
      int count = 0;
      for (DataEntry entry : entries) {
        Number value = function.apply(entry);
        if (value == null) continue;

        sum += value.doubleValue();
        count++;
      }
      if (count != 0) {
        numbers.add(sum / count);
      }
    }

    return fromData(name, numbers);
  }

  public static NumberStatistic directPit(String name, Iterable<DataEntry> pitEntries,
                                          String path) {
    return computedPit(name, pitEntries, entry -> entry.getInteger(path));
  }

  public static NumberStatistic computedPit(String name, Iterable<DataEntry> pitEntries,
                                            Function<DataEntry, Number> function) {
    List<Number> numbers = new ArrayList<>();

    for (DataEntry entry : pitEntries) {
      Number value = function.apply(entry);
      if (value != null) {
        numbers.add(value);
      }
    }

    return fromData(name, numbers);
  }
}
