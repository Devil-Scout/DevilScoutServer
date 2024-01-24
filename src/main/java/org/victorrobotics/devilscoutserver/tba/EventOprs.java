package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event.OPRs;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EventOprs implements Cacheable<OPRs> {
  public final class EventTeamOprs {
    private double opr;
    private double dpr;
    private double ccwm;

    public EventTeamOprs() {
      this.opr = Double.NaN;
      this.dpr = Double.NaN;
      this.ccwm = Double.NaN;
    }

    public String getEventKey() {
      return eventKey;
    }

    public double getOpr() {
      return opr;
    }

    public double getDpr() {
      return dpr;
    }

    public double getCcwm() {
      return ccwm;
    }

    @Override
    public int hashCode() {
      return Objects.hash(opr, dpr, ccwm);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof EventTeamOprs other)) return false;

      return Double.doubleToLongBits(opr) == Double.doubleToLongBits(other.opr)
          && Double.doubleToLongBits(dpr) == Double.doubleToLongBits(other.dpr)
          && Double.doubleToLongBits(ccwm) == Double.doubleToLongBits(other.ccwm);
    }
  }

  private final Map<Integer, EventTeamOprs> teamOprs;
  private final String eventKey;

  public EventOprs(String eventKey, OPRs oprs) {
    this.eventKey = eventKey;
    this.teamOprs = new LinkedHashMap<>();
    update(oprs);
  }

  @Override
  @SuppressWarnings("java:S1244") // floating point equality
  public boolean update(OPRs oprs) {
    Set<Integer> teams = new LinkedHashSet<>();
    oprs.offensivePowerRatings.forEach((teamKey, opr) -> teams.add(parseTeamKey(teamKey)));
    oprs.defensivePowerRatings.forEach((teamKey, opr) -> teams.add(parseTeamKey(teamKey)));
    oprs.contributionsToWinMargin.forEach((teamKey, opr) -> teams.add(parseTeamKey(teamKey)));

    boolean mods = false;
    mods |= teamOprs.keySet()
                    .retainAll(teams);

    for (int team : teams) {
      EventTeamOprs teamOpr = teamOprs.computeIfAbsent(team, t -> new EventTeamOprs());
      String key = "frc" + team;

      double opr = oprs.offensivePowerRatings.get(key);
      if (teamOpr.opr != opr) {
        teamOpr.opr = opr;
        mods = true;
      }

      double dpr = oprs.defensivePowerRatings.get(key);
      if (teamOpr.dpr != dpr) {
        teamOpr.dpr = dpr;
        mods = true;
      }

      double ccwm = oprs.contributionsToWinMargin.get(key);
      if (teamOpr.ccwm != ccwm) {
        teamOpr.ccwm = ccwm;
        mods = true;
      }
    }

    return mods;
  }

  public EventTeamOprs get(int team) {
    return teamOprs.get(team);
  }

  public Set<Map.Entry<Integer, EventTeamOprs>> entrySet() {
    return teamOprs.entrySet();
  }

  private static int parseTeamKey(String teamKey) {
    return Integer.parseInt(teamKey.substring(3));
  }
}
