package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.bluealliance.Match.Alliance.Color;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public class MatchSchedule<S extends ScoreBreakdown> implements Cacheable<List<Match>> {
  public static class MatchInfo<S extends ScoreBreakdown> {
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

    private final BiFunction<Match.ScoreBreakdown, Boolean, S> statsFunction;

    private int[]   blue;
    private int[]   red;
    private long    time;
    private boolean completed;

    private S redStatistics;
    private S blueStatistics;

    MatchInfo(BiFunction<Match.ScoreBreakdown, Boolean, S> statsFunction, Match match) {
      this.key = match.key;
      this.level = MatchLevel.of(match.level);
      this.set = match.setNumber;
      this.number = match.matchNumber;
      this.statsFunction = statsFunction;

      String setStr = key.substring(key.lastIndexOf('_') + 1, key.lastIndexOf('m'));
      if (Character.isDigit(setStr.charAt(setStr.length() - 1))) {
        this.name = level + " " + set + "-" + number;
      } else {
        this.name = level + " " + number;
      }

      update(match);
    }

    public boolean update(Match match) {
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

      boolean matchIsComplete = match.winningAlliance != Color.NONE;
      if (completed != matchIsComplete) {
        completed = matchIsComplete;
        change = true;
      }

      long matchTime = matchIsComplete ? match.actualTime.getTime() : match.predictedTime.getTime();
      if (time != matchTime) {
        time = matchTime;
        change = true;
      }

      if (match.blueScore == null) {
        change |= blueStatistics != null;
        blueStatistics = null;
      } else {
        S blueStats = statsFunction.apply(match.blueScore, wonMatch(match, Color.BLUE));
        if (!Objects.equals(blueStatistics, blueStats)) {
          blueStatistics = blueStats;
          change = true;
        }
      }

      if (match.redScore == null) {
        change |= redStatistics != null;
        redStatistics = null;
      } else {
        S redStats = statsFunction.apply(match.redScore, wonMatch(match, Color.RED));
        if (!Objects.equals(redStatistics, redStats)) {
          redStatistics = redStats;
          change = true;
        }
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

    private static Boolean wonMatch(Match match, Color color) {
      if (match.winningAlliance == null || match.winningAlliance == Color.NONE) {
        return null;
      } else if (match.winningAlliance == color) {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
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

  private final ConcurrentNavigableMap<String, MatchInfo<S>> matchMap;
  private final ConcurrentMap<Integer, S>                    teamMatches;
  private final Collection<MatchInfo<S>>                     matches;
  private final BiFunction<Match.ScoreBreakdown, Boolean, S> statsFunction;
  private final Function<Collection<S>, S>                   statsMergeFunction;

  public MatchSchedule(BiFunction<Match.ScoreBreakdown, Boolean, S> statsFunction,
                       Function<Collection<S>, S> statsMergeFunction, List<Match> matches) {
    this.matchMap = new ConcurrentSkipListMap<>(MATCH_KEY_COMPARATOR);
    this.matches = Collections.unmodifiableCollection(matchMap.values());
    this.teamMatches = new ConcurrentHashMap<>();
    this.statsFunction = statsFunction;
    this.statsMergeFunction = statsMergeFunction;
    update(matches);
  }

  @Override
  public boolean update(List<Match> matches) {
    boolean change = false;

    // Update match schedule
    Collection<String> matchKeys = new ArrayList<>();
    for (Match match : matches) {
      String key = matchKey(match);
      matchKeys.add(key);

      MatchInfo<S> info = matchMap.get(key);
      if (info == null) {
        info = new MatchInfo<>(statsFunction, match);
        matchMap.put(key, info);
        change = true;
      } else {
        change |= info.update(match);
      }
    }

    change |= matchMap.keySet()
                      .retainAll(matchKeys);
    if (!change) return false;

    // If schedule was updated, update team statistics
    Map<Integer, Collection<S>> stats = new LinkedHashMap<>();
    for (MatchInfo<S> match : matchMap.values()) {
      for (int team : match.red) {
        stats.computeIfAbsent(team, t -> new ArrayList<>())
             .add(match.redStatistics);
      }
      for (int team : match.blue) {
        stats.computeIfAbsent(team, t -> new ArrayList<>())
             .add(match.blueStatistics);
      }
    }

    teamMatches.clear();
    for (Map.Entry<Integer, Collection<S>> entry : stats.entrySet()) {
      teamMatches.put(entry.getKey(), statsMergeFunction.apply(entry.getValue()));
    }

    return true;
  }

  @JsonValue
  public Collection<MatchInfo<S>> matches() {
    return matches;
  }

  public MatchInfo<S> getMatch(String key) {
    return matchMap.values()
                   .stream()
                   .filter(e -> e.key.equals(key))
                   .findFirst()
                   .orElseGet(() -> null);
  }

  public S getTeamBreakdown(int team) {
    return teamMatches.get(team);
  }

  private static String matchKey(Match match) {
    return match.level.ordinal() + "_" + match.setNumber + "_" + match.matchNumber;
  }
}
