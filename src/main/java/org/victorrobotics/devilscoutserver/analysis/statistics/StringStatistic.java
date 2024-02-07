package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StringStatistic extends Statistic {
  public final String value;

  public StringStatistic(String name, String value) {
    super(StatisticType.STRING, name);
    this.value = value;
  }

  public static StringStatistic
      mostCommonDirectMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                            String path) {
    return mostCommonComputedMatch(name, matchEntries, entry -> getPath(entry, path));
  }

  public static StringStatistic
      mostCommonDirectMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                            String path, List<String> labels) {
    return mostCommonComputedMatch(name, matchEntries, entry -> getPath(entry, path, labels));
  }

  public static StringStatistic
      mostCommonComputedMatch(String name, Iterable<? extends Collection<DataEntry>> matchEntries,
                              Function<DataEntry, String> function) {
    Map<String, Integer> counts = new LinkedHashMap<>();
    Map<String, Integer> counts2 = new LinkedHashMap<>();
    for (Collection<DataEntry> entries : matchEntries) {
      counts2.clear();

      for (DataEntry entry : entries) {
        String val = function.apply(entry);
        if (val == null) continue;
        counts2.compute(val, StringStatistic::increment);
      }

      int maxCount = 0;
      String mostFrequent = null;
      for (Map.Entry<String, Integer> entry : counts2.entrySet()) {
        if (entry.getValue() > maxCount) {
          maxCount = entry.getValue();
          mostFrequent = entry.getKey();
        }
      }

      if (mostFrequent != null) {
        counts2.compute(mostFrequent, StringStatistic::increment);
      }
    }

    int maxCount = 0;
    String mostFrequent = null;
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      if (entry.getValue() > maxCount) {
        maxCount = entry.getValue();
        mostFrequent = entry.getKey();
      }
    }

    return new StringStatistic(name, mostFrequent);
  }

  public static StringStatistic mostCommonDirectPit(String name, Iterable<DataEntry> pitEntries,
                                                    String path) {
    return mostCommonComputedPit(name, pitEntries, entry -> getPath(entry, path));
  }

  public static StringStatistic mostCommonDirectPit(String name, Iterable<DataEntry> pitEntries,
                                                    String path, List<String> labels) {
    return mostCommonComputedPit(name, pitEntries, entry -> getPath(entry, path, labels));
  }

  public static StringStatistic mostCommonComputedPit(String name, Iterable<DataEntry> pitEntries,
                                                      Function<DataEntry, String> function) {
    Map<String, Integer> counts = new LinkedHashMap<>();

    for (DataEntry entry : pitEntries) {
      String val = function.apply(entry);
      if (val == null) continue;
      counts.compute(val, StringStatistic::increment);
    }

    int maxCount = 0;
    int distinctMaxCount = 0;
    String mostFrequent = null;
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      if (entry.getValue() > maxCount) {
        maxCount = entry.getValue();
        distinctMaxCount = 1;
        mostFrequent = entry.getKey();
      } else if (entry.getValue() == maxCount) {
        distinctMaxCount++;
      }
    }

    return new StringStatistic(name, distinctMaxCount == 1 ? mostFrequent : null);
  }

  private static Integer increment(String keyIgnored, Integer oldValue) {
    return oldValue == null ? 1 : (oldValue + 1);
  }

  private static String getPath(DataEntry entry, String path) {
    Integer value = entry.getInteger(path);
    return value == null ? null : value.toString();
  }

  private static String getPath(DataEntry entry, String path, List<String> labels) {
    Integer index = entry.getInteger(path);
    return index == null ? null : labels.get(index);
  }
}
