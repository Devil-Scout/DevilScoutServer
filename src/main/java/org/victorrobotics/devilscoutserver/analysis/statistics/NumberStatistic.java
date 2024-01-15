package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.Arrays;
import java.util.Collection;

public class NumberStatistic extends Statistic {
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

    int size = values.length;
    min = values[0];
    max = values[size - 1];

    if (size % 2 == 0) {
      median = 0.5 * (values[size / 2 - 1] + values[size / 2]);
    } else {
      median = values[size / 2];
    }

    if (size % 4 < 2) {
      q1 = 0.5 * (values[size / 4 - 1] + values[size / 4]);
      q3 = 0.5 * (values[size - size / 4 - 1] + values[size - size / 4]);
    } else {
      q1 = values[size / 4];
      q3 = values[size * 3 / 4];
    }
  }
}
