package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.analysis.data.NumberSummary;
import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.Collection;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public final class NumberStatistic extends Statistic {
  @JsonUnwrapped
  public final NumberSummary data;

  public NumberStatistic(String name, NumberSummary data) {
    super(StatisticType.NUMBER, name);
    this.data = data;
  }

  // TODO: remove all below here

  public static NumberStatistic fromData(String name, Iterable<? extends Number> numbers) {
    return new NumberStatistic(name, null);
  }

  public static NumberStatistic directMatch(String name,
                                            Iterable<? extends Collection<DataEntry>> matchEntries,
                                            String path) {
    return new NumberStatistic(name, null);
  }

  public static NumberStatistic
      computedMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                    Function<DataEntry, Number> function) {
    return new NumberStatistic(name, null);
  }

  public static NumberStatistic directPit(String name, Iterable<DataEntry> pitEntries,
                                          String path) {
    return new NumberStatistic(name, null);
  }

  public static NumberStatistic computedPit(String name, Iterable<DataEntry> pitEntries,
                                            Function<DataEntry, Number> function) {
    return new NumberStatistic(name, null);
  }
}
