package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class NumberStatistic extends Statistic {
  public final int    count;
  public final double min;
  public final double q1;
  public final double median;
  public final double q3;
  public final double max;

  public NumberStatistic(String name, Collection<? extends Number> numbers) {
    super(StatisticType.NUMBER, name);

    double[] values = new double[numbers.size()];
    int index = 0;
    for (Number number : numbers) {
      values[index++] = number.doubleValue();
    }
    Arrays.sort(values);

    count = values.length;
    if (count == 0) {
      min = 0;
      q1 = 0;
      median = 0;
      q3 = 0;
      max = 0;
    } else if (count == 1) {
      min = values[0];
      q1 = values[0];
      median = values[0];
      q3 = values[0];
      max = values[0];
    } else {
      min = values[0];
      max = values[count - 1];

      if (count % 2 == 0) {
        median = 0.5 * (values[count / 2 - 1] + values[count / 2]);
      } else {
        median = values[count / 2];
      }

      if (count % 4 < 2) {
        q1 = 0.5 * (values[count / 4 - 1] + values[count / 4]);
        q3 = 0.5 * (values[count - count / 4 - 1] + values[count - count / 4]);
      } else {
        q1 = values[count / 4];
        q3 = values[count * 3 / 4];
      }
    }
  }

  public static NumberStatistic direct(String name, Map<String, List<DataEntry>> entryMap,
                                       String path) {
    return computed(name, entryMap, entry -> entry.getInteger(path));
  }

  @SuppressWarnings("java:S4276") // use ToDoubleFunction<Entry> instead
  public static NumberStatistic computed(String name, Map<String, List<DataEntry>> entryMap,
                                         Function<DataEntry, Integer> function) {
    List<Double> numbers = new ArrayList<>();
    for (List<DataEntry> entries : entryMap.values()) {
      if (entries.isEmpty()) continue;

      double sum = 0;
      int count = 0;
      for (DataEntry entry : entries) {
        Integer value = function.apply(entry);
        if (value != null) {
          sum += value.doubleValue();
          count++;
        }
      }
      if (count != 0) {
        numbers.add(sum / count);
      }
    }
    return new NumberStatistic(name, numbers);
  }
}
