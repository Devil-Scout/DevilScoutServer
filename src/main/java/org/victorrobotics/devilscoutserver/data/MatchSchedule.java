package org.victorrobotics.devilscoutserver.data;

import org.victorrobotics.bluealliance.Match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class MatchSchedule {
  private final SortedMap<String, MatchInfo> matchMap;

  private final String          eventKey;
  private long                  timestamp;
  private Collection<MatchInfo> matches;

  public MatchSchedule(String eventKey) {
    this.eventKey = eventKey;
    matchMap = new TreeMap<>((key1, key2) -> {
      int index1 = key1.indexOf("_");
      int index2 = key2.indexOf("_");

      int level1 = Integer.parseInt(key1.substring(0, index1));
      int level2 = Integer.parseInt(key2.substring(0, index2));
      if (level1 != level2) return Integer.compare(level1, level2);

      int lastIndex1 = key1.lastIndexOf("_");
      int lastIndex2 = key2.lastIndexOf("_");

      int set1 = Integer.parseInt(key1.substring(index1 + 1, lastIndex1));
      int set2 = Integer.parseInt(key2.substring(index2 + 1, lastIndex2));
      if (set1 != set2) return Integer.compare(set1, set2);

      int num1 = Integer.parseInt(key1.substring(lastIndex1 + 1));
      int num2 = Integer.parseInt(key2.substring(lastIndex2 + 1));
      return Integer.compare(num1, num2);
    });
    matches = Collections.unmodifiableCollection(matchMap.values());
  }

  @OpenApiExample("1700067337228")
  @OpenApiRequired
  public long getTimestamp() {
    return timestamp;
  }

  @OpenApiExample("2023nyrr")
  @OpenApiRequired
  public String getEventKey() {
    return eventKey;
  }

  @OpenApiRequired
  public Collection<MatchInfo> getMatches() {
    return matches;
  }

  public synchronized void update(Collection<Match.Simple> list) {
    boolean change = false;
    List<String> keys = new ArrayList<>(list.size());
    for (Match.Simple match : list) {
      String key = matchToKey(match);
      keys.add(key);

      MatchInfo info = matchMap.get(key);
      if (info == null) {
        matchMap.put(key, new MatchInfo(match));
        change = true;
      } else {
        change |= info.update(match);
      }
    }
    change |= matchMap.keySet()
                      .retainAll(keys);

    if (change) {
      timestamp = System.currentTimeMillis();
    }
  }

  private static String matchToKey(Match.Simple match) {
    return match.level.ordinal() + "_" + match.setNumber + "_" + match.matchNumber;
  }
}
