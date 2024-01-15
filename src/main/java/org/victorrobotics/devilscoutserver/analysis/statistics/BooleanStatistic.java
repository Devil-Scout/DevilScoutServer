package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.Entry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class BooleanStatistic extends Statistic {
  public final int yes;
  public final int no;
  public final int total;

  public BooleanStatistic(String name, int yes, int no, int total) {
    super(StatisticType.BOOLEAN, name);
    this.yes = yes;
    this.no = no;
    this.total = total;
  }

  public static BooleanStatistic direct(String name, Map<String, List<Entry>> entryMap,
                                        String path) {
    return computed(name, entryMap, entry -> entry.getBoolean(path));
  }

  @SuppressWarnings("java:S4276") // use Predicate<Entry> instead
  public static BooleanStatistic computed(String name, Map<String, List<Entry>> entryMap,
                                          Function<Entry, Boolean> function) {
    int yes = 0;
    int no = 0;
    int total = 0;
    for (List<Entry> entries : entryMap.values()) {
      if (entries.isEmpty()) continue;

      int yes2 = 0;
      int no2 = 0;
      for (Entry entry : entries) {
        Boolean value = function.apply(entry);
        if (value != null) {
          if (value) {
            yes2++;
          } else {
            no2++;
          }
        }
      }

      if (yes2 == 0 && no2 == 0) continue;

      if (yes2 > no2) {
        yes++;
      } else if (no2 > yes2) {
        no++;
      }
      total++;
    }
    return new BooleanStatistic(name, yes, no, total);
  }
}
