package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.Collection;
import java.util.function.Function;

public final class BooleanStatistic extends Statistic {
  public final Double percent;

  public BooleanStatistic(String name, Double percent) {
    super(StatisticType.BOOLEAN, name);
    this.percent = percent;
  }

  public static BooleanStatistic directMatch(String name,
                                             Iterable<? extends Collection<DataEntry>> matchEntries,
                                             String path) {
    return computedMatch(name, matchEntries, entry -> entry.getBoolean(path));
  }

  @SuppressWarnings("java:S4276") // use Predicate<Entry> instead
  public static BooleanStatistic
      computedMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                    Function<DataEntry, Boolean> function) {
    int yes = 0;
    int total = 0;
    for (Collection<DataEntry> entries : matchEntries) {
      if (entries.isEmpty()) continue;

      int yes2 = 0;
      int no2 = 0;
      for (DataEntry entry : entries) {
        Boolean value = function.apply(entry);
        if (value == null) continue;

        if (value) {
          yes2++;
        } else {
          no2++;
        }
      }

      if (yes2 == 0 && no2 == 0) continue;

      if (yes2 > no2) {
        yes++;
      }
      total++;
    }

    Double percent = total == 0 ? null : ((double) yes / total);
    return new BooleanStatistic(name, percent);
  }

  public static BooleanStatistic directPit(String name, Iterable<DataEntry> pitEntries,
                                           String path) {
    return computedPit(name, pitEntries, entry -> entry.getBoolean(path));
  }

  @SuppressWarnings("java:S4276") // use Predicate<Entry> instead
  public static BooleanStatistic computedPit(String name, Iterable<DataEntry> pitEntries,
                                             Function<DataEntry, Boolean> function) {
    int yes = 0;
    int total = 0;
    for (DataEntry entry : pitEntries) {
      Boolean value = function.apply(entry);
      if (value == null) continue;

      if (value) {
        yes++;
      }
      total++;
    }

    double percent = total == 0 ? -1 : ((double) yes / total);
    return new BooleanStatistic(name, percent);
  }
}
