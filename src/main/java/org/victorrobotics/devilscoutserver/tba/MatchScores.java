package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MatchScores implements Cacheable<List<Match.Simple>> {
  private final String               teamKey;
  private final Map<String, Integer> scores;

  public MatchScores(int team, List<Match.Simple> matches) {
    teamKey = "frc" + team;
    scores = new LinkedHashMap<>();
    update(matches);
  }

  @Override
  public boolean update(List<Match.Simple> matches) {
    boolean change = false;
    List<String> matchKeys = new ArrayList<>();

    for (Match.Simple match : matches) {
      int score = -1;
      if (match.redAlliance.teamKeys.contains(teamKey)) {
        score = match.redAlliance.score;
      } else if (match.blueAlliance.teamKeys.contains(teamKey)) {
        score = match.blueAlliance.score;
      }
      if (score == -1) continue;

      matchKeys.add(match.key);
      Integer previousScore = scores.get(match.key);
      if (previousScore == null || score != previousScore) {
        scores.put(match.key, score);
        change = true;
      }
    }

    change |= scores.keySet()
                    .retainAll(matchKeys);
    return change;
  }

  public Collection<Integer> getScores() {
    return scores.values();
  }

  @Override
  public String toString() {
    return getScores().toString();
  }
}
