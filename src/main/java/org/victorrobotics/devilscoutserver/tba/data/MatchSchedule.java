package org.victorrobotics.devilscoutserver.tba.data;

import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.devilscoutserver.tba.cache.Cacheable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class MatchSchedule implements Cacheable<List<Match.Simple>> {
  public static class MatchInfo {
    enum MatchLevel {
      QUAL,
      QUARTER,
      SEMI,
      FINAL,

      @JsonEnumDefaultValue
      UNKNOWN;

      private static final MatchLevel[] VALUES = values();

      static MatchLevel of(Match.Level level) {
        int ordinal = level.ordinal();
        return ordinal >= VALUES.length ? VALUES[VALUES.length - 1] : VALUES[ordinal];
      }
    }

    private final String     key;
    private final MatchLevel level;
    private final int        set;
    private final int        match;

    private int[]   blue;
    private int[]   red;
    private long    time;
    private boolean completed;

    MatchInfo(Match.Simple match) {
      this.key = match.key;
      this.level = MatchLevel.of(match.level);
      this.set = match.setNumber;
      this.match = match.matchNumber;

      this.blue = parseTeamKeys(match.blueAlliance.teamKeys);
      this.red = parseTeamKeys(match.redAlliance.teamKeys);
      this.completed = match.winner != null;
      this.time = completed ? match.predictedTime.getTime() : match.actualTime.getTime();
    }

    public boolean update(Match.Simple match) {
      if (!Objects.equals(key, match.key)) {
        throw new IllegalArgumentException();
      }

      boolean change = false;

      int[] matchBlueAlliance = parseTeamKeys(match.blueAlliance.teamKeys);
      if (!Arrays.equals(blue, matchBlueAlliance)) {
        blue = matchBlueAlliance;
        change = true;
      }

      int[] matchRedAlliance = parseTeamKeys(match.redAlliance.teamKeys);
      if (!Arrays.equals(red, matchRedAlliance)) {
        red = matchRedAlliance;
        change = true;
      }

      boolean matchIsComplete = match.winner != null;
      if (completed != matchIsComplete) {
        completed = matchIsComplete;
        change = true;
      }

      long matchTime = matchIsComplete ? match.actualTime.getTime() : match.predictedTime.getTime();
      if (time != matchTime) {
        time = matchTime;
        change = true;
      }

      return change;
    }

    @Override
    public String toString() {
      return key;
    }

    private static int[] parseTeamKeys(List<String> teamKeys) {
      int[] teams = new int[teamKeys.size()];
      for (int i = 0; i < teams.length; i++) {
        teams[i] = Integer.parseInt(teamKeys.get(i)
                                            .substring(3));
      }
      return teams;
    }

    @OpenApiRequired
    @OpenApiExample("2023nyrr_qm1")
    public String getKey() {
      return key;
    }

    @OpenApiRequired
    public MatchLevel getLevel() {
      return level;
    }

    @OpenApiRequired
    @OpenApiExample("1")
    public int getSet() {
      return set;
    }

    @OpenApiRequired
    @OpenApiExample("1")
    public int getMatch() {
      return match;
    }

    @OpenApiRequired
    @OpenApiExample("[2228,1585,578]")
    @SuppressWarnings("java:S2384")
    public int[] getBlue() {
      return blue;
    }

    @OpenApiRequired
    @OpenApiExample("[1559,9996,5740]")
    @SuppressWarnings("java:S2384")
    public int[] getRed() {
      return red;
    }

    @OpenApiRequired
    @OpenApiExample("1697891944000")
    public long getTime() {
      return time;
    }

    @OpenApiRequired
    @OpenApiExample("true")
    public boolean isCompleted() {
      return completed;
    }
  }

  private static final Comparator<String> MATCH_KEY_COMPARATOR = (key1, key2) -> {
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
  };

  private final ConcurrentNavigableMap<String, MatchInfo> matchMap;
  private final Collection<MatchInfo>                     matches;

  public MatchSchedule() {
    matchMap = new ConcurrentSkipListMap<>(MATCH_KEY_COMPARATOR);
    matches = Collections.unmodifiableCollection(matchMap.values());
  }

  @Override
  public boolean update(List<Match.Simple> matches) {
    boolean change = false;
    List<String> matchKeys = new ArrayList<>();
    for (Match.Simple match : matches) {
      String key = matchKey(match);
      matchKeys.add(key);

      MatchInfo info = matchMap.get(key);
      if (info == null) {
        matchMap.put(key, new MatchInfo(match));
        change = true;
      } else {
        change |= info.update(match);
      }
    }
    change |= matchMap.keySet()
                      .retainAll(matchKeys);

    return change;
  }

  @JsonValue
  public Collection<MatchInfo> matches() {
    return matches;
  }

  private static String matchKey(Match.Simple match) {
    return match.level.ordinal() + "_" + match.setNumber + "_" + match.matchNumber;
  }
}