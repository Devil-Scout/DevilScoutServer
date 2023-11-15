package org.victorrobotics.devilscoutserver.data;

import org.victorrobotics.bluealliance.Match;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class MatchInfo {
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

  public MatchInfo(Match.Simple match) {
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
