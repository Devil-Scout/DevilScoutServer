package org.victorrobotics.devilscoutserver.years._2024;

import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.statistics.BooleanStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.NumberStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.PieChartStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.RadarStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.analysis.statistics.StringStatistic;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.tba.EventOprsCache;
import org.victorrobotics.devilscoutserver.tba.EventTeamListCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;

import java.util.List;

public final class CrescendoAnalyzer extends Analyzer {
  public CrescendoAnalyzer(TeamDatabase teamDB, EventTeamListCache teamListCache,
                           EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB, MatchScheduleCache<?> matchScheduleCache,
                           EventOprsCache teamOprsCache) {
    super(teamDB, teamListCache, matchEntryDB, pitEntryDB, driveTeamEntryDB, matchScheduleCache,
          teamOprsCache);
  }

  @Override
  protected List<StatisticsPage> computeStatistics(DataHandle handle) {
    return List.of(summaryPage(handle), specsPage(handle), autoPage(handle), teleopPage(handle),
                   endgamePage(handle));
  }

  private StatisticsPage summaryPage(DataHandle handle) {
    return new StatisticsPage("Summary",
                              List.of(handle.wltStatistic(), handle.rpStatistic(),
                                      handle.oprStatistic(),
                                      RadarStatistic.directMatch("Drive Team",
                                                                 handle.getDriveTeamEntries(),
                                                                 List.of("/communication",
                                                                         "/strategy",
                                                                         "/adaptability",
                                                                         "/professionalism"),
                                                                 5,
                                                                 List.of("Communication",
                                                                         "Strategy", "Adaptability",
                                                                         "Professionalism"))));
  }

  private StatisticsPage specsPage(DataHandle handle) {
    return new StatisticsPage("Specs",
                              List.of(StringStatistic.mostCommonDirectPit("Drivetrain",
                                                                          handle.getPitEntries(),
                                                                          "/specs/drivetrain"),
                                      StringStatistic.mostCommonDirectPit("Weight",
                                                                          handle.getPitEntries(),
                                                                          "/specs/weight"),
                                      StringStatistic.mostCommonDirectPit("Chassis Size",
                                                                          handle.getPitEntries(),
                                                                          "/specs/size"),
                                      NumberStatistic.directMatch("Speed", handle.getMatchEntries(),
                                                                  "/general/speed")));
  }

  private StatisticsPage autoPage(DataHandle handle) {
    return new StatisticsPage("Auto",
                              List.of(PieChartStatistic.directMatch("Start Position",
                                                                    handle.getMatchEntries(),
                                                                    "/auto/start_pos",
                                                                    List.of("Next to amp",
                                                                            "Front of speaker",
                                                                            "Next to speaker",
                                                                            "Next to source")),
                                      NumberStatistic.computedMatch("Note Count",
                                                                    handle.getMatchEntries(),
                                                                    CrescendoAnalyzer::matchNoteCount)));
  }

  private static Integer matchNoteCount(DataEntry match) {
    List<Integer> actions = match.getIntegers("/auto/routine");
    int scoreCount = 0;
    int pickupCount = 0;
    for (Integer action : actions) {
      switch (action) {
        case 0, 1:
          scoreCount++;
          break;
        case 2:
          break;
        case 3:
          pickupCount++;
          break;
        default:
          return null;
      }
    }
    // Can't score more than you pick up
    return Math.min(scoreCount, pickupCount + 1);
  }

  private StatisticsPage teleopPage(DataHandle handle) {
    return new StatisticsPage("Teleop",
                              List.of(NumberStatistic.computedMatch("Cycles per Minute",
                                                                    handle.getMatchEntries(),
                                                                    CrescendoAnalyzer::matchCyclesPerMinute),
                                      PieChartStatistic.computedMatchCounts("Score Locations",
                                                                            handle.getMatchEntries(),
                                                                            List.of("Speaker",
                                                                                    "Amp"),
                                                                            CrescendoAnalyzer::matchScoreLocations),
                                      PieChartStatistic.computedMatchCounts("Score Accuracy",
                                                                            handle.getMatchEntries(),
                                                                            List.of("Successful",
                                                                                    "Missed"),
                                                                            CrescendoAnalyzer::matchScoreAccuracy),
                                      PieChartStatistic.computedMatchCounts("Pickup Locations",
                                                                            handle.getMatchEntries(),
                                                                            List.of("Source",
                                                                                    "Ground"),
                                                                            CrescendoAnalyzer::matchPickupLocations)));
  }

  private static Double matchCyclesPerMinute(DataEntry match) {
    // We are assuming 2 minutes of play time
    return (match.getInteger("/teleop/score_amp") + match.getInteger("/teleop/score_speaker"))
        / 2.0;
  }

  private static List<Integer> matchScoreLocations(DataEntry match) {
    return List.of(match.getInteger("/teleop/score_speaker"),
                   match.getInteger("/teleop/score_amp"));
  }

  private static List<Integer> matchScoreAccuracy(DataEntry match) {
    int scores = match.getInteger("/teleop/score_speaker") + match.getInteger("/teleop/score_amp");
    int attempts =
        match.getInteger("/teleop/pickup_source") + match.getInteger("/teleop/pickup_ground");
    return List.of(scores, attempts - scores);
  }

  private static List<Integer> matchPickupLocations(DataEntry match) {
    return List.of(match.getInteger("/teleop/pickup_source"),
                   match.getInteger("/teleop/pickup_ground"));
  }

  private StatisticsPage endgamePage(DataHandle handle) {
    return new StatisticsPage("Endgame",
                              List.of(PieChartStatistic.directMatch("Final Status",
                                                                    handle.getMatchEntries(),
                                                                    "/endgame/status",
                                                                    List.of("None", "Parked",
                                                                            "Onstage", "Harmony")),
                                      BooleanStatistic.directMatch("Trap", handle.getMatchEntries(),
                                                                   "/endgame/trap")));
  }
}
