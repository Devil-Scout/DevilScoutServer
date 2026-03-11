package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.bluealliance.Event.OPRs;
import org.victorrobotics.devilscoutserver.analysis.AnalysisCache;
import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.tba.OprsCache.Oprs;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class OprsCache extends BlueAllianceCache<String, OPRs, Oprs> {
  public static class TeamOpr {
    private double opr;
    private double dpr;
    private double ccwm;

    public TeamOpr() {
      this.opr = Double.NaN;
      this.dpr = Double.NaN;
      this.ccwm = Double.NaN;
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
      if (!(obj instanceof TeamOpr other)) return false;

      return Double.doubleToLongBits(opr) == Double.doubleToLongBits(other.opr)
          && Double.doubleToLongBits(dpr) == Double.doubleToLongBits(other.dpr)
          && Double.doubleToLongBits(ccwm) == Double.doubleToLongBits(other.ccwm);
    }

  }

  public class Oprs implements Cacheable<OPRs> {
    private final String                eventKey;
    private final Map<Integer, TeamOpr> teamOprs;

    public Oprs(String eventKey, OPRs oprs) {
      this.eventKey = eventKey;
      this.teamOprs = new LinkedHashMap<>();
      update(oprs);
    }

    @Override
    public boolean update(OPRs oprs) {
      if (oprs == null || oprs.oprs() == null) {
        boolean change = !teamOprs.isEmpty();
        teamOprs.clear();
        return change;
      }

      Set<Integer> teams = new LinkedHashSet<>();
      oprs.oprs()
          .forEach((teamKey, opr) -> teams.add(parseTeamKey(teamKey)));
      oprs.dprs()
          .forEach((teamKey, opr) -> teams.add(parseTeamKey(teamKey)));
      oprs.ccwms()
          .forEach((teamKey, opr) -> teams.add(parseTeamKey(teamKey)));

      boolean mods = teamOprs.keySet()
                             .retainAll(teams);

      for (int team : teams) {
        if (updateTeam(team, oprs)) {
          mods = true;
          analysis.scheduleRefresh(eventKey, team);
        }
        mods |= updateTeam(team, oprs);
      }

      return mods;
    }

    @SuppressWarnings("java:S1244") // floating point equality
    private boolean updateTeam(int team, OPRs oprs) {
      String teamKey = "frc" + team;
      TeamOpr teamOpr = teamOprs.computeIfAbsent(team, t -> new TeamOpr());
      boolean mods = false;

      Double opr = oprs.oprs()
                       .get(teamKey);
      if (opr != null && opr != teamOpr.opr) {
        teamOpr.opr = opr;
        mods = true;
      }

      Double dpr = oprs.dprs()
                       .get(teamKey);
      if (dpr != null && dpr != teamOpr.dpr) {
        teamOpr.dpr = dpr;
        mods = true;
      }

      Double ccwm = oprs.ccwms()
                        .get(teamKey);
      if (ccwm != null && ccwm != teamOpr.ccwm) {
        teamOpr.ccwm = ccwm;
        mods = true;
      }

      return mods;
    }

    public TeamOpr get(int team) {
      return teamOprs.get(team);
    }

    public Set<Map.Entry<Integer, TeamOpr>> entrySet() {
      return teamOprs.entrySet();
    }

    private static int parseTeamKey(String teamKey) {
      return Integer.parseInt(teamKey.substring(3));
    }
  }

  private final AnalysisCache analysis;

  public OprsCache(AnalysisCache analysis) {
    this.analysis = analysis;
  }

  @Override
  protected Endpoint<OPRs> getEndpoint(String eventKey) {
    return Event.OPRs.endpointForEvent(eventKey);
  }

  @Override
  protected Oprs createValue(String key, OPRs data) {
    return new Oprs(key, data);
  }
}
