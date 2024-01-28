package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

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

public class MatchSchedule implements Cacheable<List<Match.Simple>> {
  public static class MatchInfo {
    enum MatchLevel {
      QUAL("Qualification"),
      QUARTER("Quarterfinal"),
      SEMI("Semifinal"),
      FINAL("Final"),

      @JsonEnumDefaultValue
      UNKNOWN("Custom");

      private static final MatchLevel[] VALUES = values();

      private final String name;

      MatchLevel(String name) {
        this.name = name;
      }

      @Override
      public String toString() {
        return name;
      }

      static MatchLevel of(Match.Level level) {
        int ordinal = level.ordinal();
        return ordinal >= VALUES.length ? VALUES[VALUES.length - 1] : VALUES[ordinal];
      }
    }

    private final String     key;
    private final String     name;
    private final MatchLevel level;
    private final int        set;
    private final int        number;

    private int[]   blue;
    private int[]   red;
    private long    time;
    private boolean completed;

    MatchInfo(Match.Simple match) {
      this.key = match.key;
      this.level = MatchLevel.of(match.level);
      this.set = match.setNumber;
      this.number = match.matchNumber;

      String setStr = key.substring(key.lastIndexOf('_') + 1, key.lastIndexOf('m'));
      if (Character.isDigit(setStr.charAt(setStr.length() - 1))) {
        this.name = level + " " + set + "-" + number;
      } else {
        this.name = level + " " + number;
      }

      update(match);
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

    public String getKey() {
      return key;
    }

    public String getName() {
      return name;
    }

    public MatchLevel getLevel() {
      return level;
    }

    public int getSet() {
      return set;
    }

    public int getNumber() {
      return number;
    }

    @SuppressWarnings("java:S2384")
    public int[] getBlue() {
      return blue;
    }

    @SuppressWarnings("java:S2384")
    public int[] getRed() {
      return red;
    }

    public long getTime() {
      return time;
    }

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

  public MatchSchedule(List<Match.Simple> matches) {
    this.matchMap = new ConcurrentSkipListMap<>(MATCH_KEY_COMPARATOR);
    this.matches = Collections.unmodifiableCollection(matchMap.values());
    update(matches);
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

  public MatchInfo getMatch(String key) {
    return matchMap.values()
                   .stream()
                   .filter(e -> e.key.equals(key))
                   .findFirst()
                   .orElseGet(() -> null);
  }

  private static String matchKey(Match.Simple match) {
    return match.level.ordinal() + "_" + match.setNumber + "_" + match.matchNumber;
  }
}
