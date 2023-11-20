package org.victorrobotics.devilscoutserver.data;

import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.bluealliance.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class EventInfo {
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

  private final String eventKey;

  private final SortedMap<String, MatchInfo> matchMap;
  private final SortedMap<Integer, TeamInfo> teamMap;

  private final Collection<MatchInfo> matches;
  private final Collection<TeamInfo>  teams;

  private long timestamp;

  public EventInfo(String eventKey) {
    this.eventKey = eventKey;
    teamMap = new TreeMap<>();
    matchMap = new TreeMap<>(MATCH_KEY_COMPARATOR);
    matches = Collections.unmodifiableCollection(matchMap.values());
    teams = Collections.unmodifiableCollection(teamMap.values());
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

  @OpenApiRequired
  public Collection<TeamInfo> getTeams() {
    return teams;
  }

  public synchronized void update(Iterable<Match.Simple> matches, Iterable<Team.Simple> teams) {
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

    Collection<Integer> teamKeys = new ArrayList<>();
    for (Team.Simple team : teams) {
      teamKeys.add(team.number);

      TeamInfo info = teamMap.get(team.number);
      if (info == null) {
        teamMap.put(team.number, new TeamInfo(team));
        change = true;
      }
    }
    change |= teamMap.keySet()
                     .retainAll(teamKeys);

    if (change) {
      timestamp = System.currentTimeMillis();
    }
  }

  private static String matchKey(Match.Simple match) {
    return match.level.ordinal() + "_" + match.setNumber + "_" + match.matchNumber;
  }

  static class TeamInfo {
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

    @JsonProperty
    private final int number;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String location;

    TeamInfo(Team.Simple team) {
      this.number = team.number;
      this.name = team.name;
      this.location = team.city + ", " + team.province + ", " + team.country;
    }

    @OpenApiExample("1559")
    @OpenApiRequired
    public int getNumber() {
      return number;
    }

    @OpenApiExample("Devil Tech")
    @OpenApiRequired
    public String getName() {
      return name;
    }

    @OpenApiExample("Victor, New York, USA")
    @OpenApiRequired
    public String getLocation() {
      return location;
    }
  }

  static class MatchInfo {
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

    @JsonProperty
    private final String key;

    @JsonProperty
    private final MatchLevel level;

    @JsonProperty
    private final int set;

    @JsonProperty
    private final int match;

    @JsonProperty
    private int[] blue;

    @JsonProperty
    private int[] red;

    @JsonProperty
    private long time;

    @JsonProperty
    private boolean complete;

    MatchInfo(Match.Simple match) {
      this.key = match.key;
      this.level = MatchLevel.of(match.level);
      this.set = match.setNumber;
      this.match = match.matchNumber;

      this.blue = parseTeamKeys(match.blueAlliance.teamKeys);
      this.red = parseTeamKeys(match.redAlliance.teamKeys);
      // TODO undo hack once library is patched
      this.complete = match.winner != null;
      this.time = complete ? match.predictedTime.getTime() : match.actualTime.getTime();
    }

    public boolean update(Match.Simple match) {
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
      if (complete != matchIsComplete) {
        complete = matchIsComplete;
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
    @OpenApiExample("QUAL")
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
    @SuppressWarnings("java:S2384")
    @OpenApiExample("[2228,1585,578]")
    public int[] getBlue() {
      return blue;
    }

    @OpenApiRequired
    @SuppressWarnings("java:S2384")
    @OpenApiExample("[1559,9996,5740]")
    public int[] getRed() {
      return red;
    }

    @SuppressWarnings("java:S2384")
    @OpenApiRequired
    @OpenApiExample("1697891944000")
    public long getTime() {
      return time;
    }

    @OpenApiRequired
    @OpenApiExample("true")
    public boolean isComplete() {
      return complete;
    }
  }
}
