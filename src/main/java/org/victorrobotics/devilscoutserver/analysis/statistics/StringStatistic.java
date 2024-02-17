package org.victorrobotics.devilscoutserver.analysis.statistics;

public class StringStatistic extends Statistic {
  public final String value;

  public StringStatistic(String name, String value) {
    super(StatisticType.STRING, name);
    this.value = value;
  }

  public StringStatistic(String name, Object value) {
    this(name, value == null ? null : value.toString());
  }

  public StringStatistic(String name, Number number, String suffix) {
    this(name, number == null ? null : (formatNumber(number) + suffix));
  }

  private static String formatNumber(Number number) {
    // Integers are always 0 decimal places
    if (number instanceof Integer || number instanceof Long) {
      return number.toString();
    }

    double value = number.doubleValue();
    if (!Double.isFinite(value)) {
      return "NaN";
    } else if (Math.abs(value) >= 100) {
      return Long.toString(Math.round(value));
    } else if (Math.abs(value) >= 1) {
      return String.format("%.1f", value);
    } else {
      return String.format("%.2f", value);
    }
  }
}
