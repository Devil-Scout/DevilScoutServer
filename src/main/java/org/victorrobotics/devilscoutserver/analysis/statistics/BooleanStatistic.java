package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.Entry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;

public final class BooleanStatistic extends Statistic {
  public final int yes;
  public final int no;

  public BooleanStatistic(String name, int yes, int no) {
    super(StatisticType.BOOLEAN, name);
    this.yes = yes;
    this.no = no;
  }

  public static BooleanStatistic direct(String name, Map<String, List<Entry>> entryMap,
                                        String path) {
    return computed(name, entryMap, entry -> {
      JsonNode node = entry.json()
                           .at(path);
      return node.isBoolean() ? node.booleanValue() : null;
    });
  }

  @SuppressWarnings("java:S4276") // use Predicate<Entry> instead
  public static BooleanStatistic computed(String name, Map<String, List<Entry>> entryMap,
                                          Function<Entry, Boolean> function) {
    int yes = 0;
    int no = 0;
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

      if (yes2 > 2 * no2) {
        yes++;
      } else if (no2 > 2 * yes2) {
        no++;
      }
    }
    return new BooleanStatistic(name, yes, no);
  }
}
