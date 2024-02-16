package org.victorrobotics.devilscoutserver.years._2024;

import org.victorrobotics.bluealliance.Event.WinLossRecord;
import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.statistics.BooleanStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.NumberStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.OprStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.PieChartStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.RadarStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.RadarStatistic.RadarPoint;
import org.victorrobotics.devilscoutserver.analysis.statistics.RankingPointsStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.analysis.statistics.StringStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.WltStatistic;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.OprsCache;
import org.victorrobotics.devilscoutserver.tba.OprsCache.TeamOpr;
import org.victorrobotics.devilscoutserver.tba.RankingsCache;

import java.util.List;
import java.util.Map;

public final class CrescendoAnalyzer extends Analyzer<CrescendoAnalyzer.Data> {
  enum DrivetrainType {
    SWERVE("Swerve"),
    TANK("Tank"),
    MECANUM("Mecanum"),
    OTHER("Other");

    static final DrivetrainType[] VALUES = values();

    final String value;

    DrivetrainType(String value) {
      this.value = value;
    }

    static DrivetrainType of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  static record DriveTeamRadar(Number communication,
                               Number strategy,
                               Number adaptability,
                               Number professionalism) {}

  static record Data(WinLossRecord wlt,
                     Map<String, Integer> rankingPoints,
                     TeamOpr opr,
                     DriveTeamRadar driveTeamRadar,
                     DrivetrainType drivetrain,
                     Integer weight,
                     Integer size) {}

  public CrescendoAnalyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB, MatchScheduleCache matchScheduleCache,
                           OprsCache teamOprsCache, RankingsCache rankingsCache) {
    super(matchEntryDB, pitEntryDB, driveTeamEntryDB, matchScheduleCache, teamOprsCache,
          rankingsCache);
  }

  @Override
  protected Data computeData(Handle handle) {
    return new Data(handle.getRankings()
                          .getWinLossRecord(),
                    Map.of(), handle.getOpr(), driveTeamRadar(handle),
                    DrivetrainType.of(mostCommon(extractData(handle.getPitEntries(),
                                                             "/specs/drivetrain",
                                                             DataEntry::getInteger))),
                    mostCommon(extractData(handle.getPitEntries(), "/specs/weight",
                                           DataEntry::getInteger)),
                    mostCommon(extractData(handle.getPitEntries(), "/specs/size",
                                           DataEntry::getInteger)));
  }

  private static DriveTeamRadar driveTeamRadar(Handle handle) {
    return new DriveTeamRadar(average(extractDataDeep(handle.getDriveTeamEntries(),
                                                      "/communication", DataEntry::getInteger,
                                                      Analyzer::average)),
                              average(extractDataDeep(handle.getDriveTeamEntries(), "/strategy",
                                                      DataEntry::getInteger, Analyzer::average)),
                              average(extractDataDeep(handle.getDriveTeamEntries(), "/adaptability",
                                                      DataEntry::getInteger, Analyzer::average)),
                              average(extractDataDeep(handle.getDriveTeamEntries(),
                                                      "/professionalism", DataEntry::getInteger,
                                                      Analyzer::average)));
  }

  @Override
  protected List<StatisticsPage> generateStatistics(Data data) {
    return List.of(new StatisticsPage("Summary",
                                      List.of(new WltStatistic(data.wlt()),
                                              new RankingPointsStatistic(data.rankingPoints()),
                                              new OprStatistic(data.opr()), driveTeamRadar(data))));
  }

  private static RadarStatistic driveTeamRadar(Data data) {
    return new RadarStatistic("Drive Team", 5, List.of(
                                                       new RadarPoint("Communication",
                                                                      data.driveTeamRadar()
                                                                          .communication()),
                                                       new RadarPoint("Strategy",
                                                                      data.driveTeamRadar()
                                                                          .strategy()),
                                                       new RadarPoint("Adaptability",
                                                                      data.driveTeamRadar()
                                                                          .adaptability()),
                                                       new RadarPoint("Professionalism",
                                                                      data.driveTeamRadar()
                                                                          .professionalism())));
  }

  private StatisticsPage specsPage(Handle handle) {
    return new StatisticsPage("Specs",
                              List.of(StringStatistic.mostCommonDirectPit("Drivetrain",
                                                                          handle.getPitEntries(),
                                                                          "/specs/drivetrain",
                                                                          List.of("Swerve", "Tank",
                                                                                  "Mecanum",
                                                                                  "Other")),
                                      StringStatistic.mostCommonDirectPit("Weight",
                                                                          handle.getPitEntries(),
                                                                          "/specs/weight"),
                                      StringStatistic.mostCommonDirectPit("Chassis Size",
                                                                          handle.getPitEntries(),
                                                                          "/specs/size"),
                                      NumberStatistic.directMatch("Speed", handle.getMatchEntries(),
                                                                  "/general/speed")));
  }

  private StatisticsPage autoPage(Handle handle) {
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

  private StatisticsPage teleopPage(Handle handle) {
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

  private StatisticsPage endgamePage(Handle handle) {
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
