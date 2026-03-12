package org.victorrobotics.devilscoutserver.years._2025;

import org.victorrobotics.bluealliance.ScoreBreakdown.UnknownScoreBreakdown;
import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.statistics.OprStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.PieChartStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.RadarStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.RankingPointsStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.analysis.statistics.StringStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.WltStatistic;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.OprsCache;
import org.victorrobotics.devilscoutserver.tba.RankingsCache;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.AlgaeScore;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.AutoAction;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.CoralLevel;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.Disabled;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.DrivetrainType;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.FinalStatus;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.Fouls;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.GamePiece;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.StartPosition;

import java.util.List;
import java.util.Map;

public class ReefscapeAnalyzer extends Analyzer<UnknownScoreBreakdown, ReefscapeData> {
  public ReefscapeAnalyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB, MatchScheduleCache matchScheduleCache,
                           OprsCache teamOprsCache, RankingsCache rankingsCache) {
    super(matchEntryDB, pitEntryDB, driveTeamEntryDB, matchScheduleCache, teamOprsCache,
        rankingsCache);
  }

  @Override
  protected boolean isValidMatchEntry(DataEntry matchEntry,
                                      TeamScoreBreakdown<UnknownScoreBreakdown> breakdown) {
    return true;
  }

  @Override
  protected ReefscapeData computeData(Analyzer<UnknownScoreBreakdown, ReefscapeData>.Data inputs) {
    return new ReefscapeData(mapSingle(inputs.getRankings(), RankingsCache.Team::getWinLossRecord),
        Map.of(), inputs.getOpr(),
        average(extractMergeData(inputs.getDriveTeamEntries(), "/communication",
            DataEntry::getInteger, Analyzer::average)),
        average(extractMergeData(inputs.getDriveTeamEntries(), "/strategy", DataEntry::getInteger,
            Analyzer::average)),
        average(extractMergeData(inputs.getDriveTeamEntries(), "/adaptability",
            DataEntry::getInteger, Analyzer::average)),
        average(extractMergeData(inputs.getDriveTeamEntries(), "/professionalism",
            DataEntry::getInteger, Analyzer::average)),
        mostCommon(
            map(extractData(inputs.getPitEntries(), "/specs/drivetrain", DataEntry::getInteger),
                DrivetrainType::of)),
        mostCommon(extractData(inputs.getPitEntries(), "/specs/weight", DataEntry::getInteger)),
        mostCommon(extractData(inputs.getPitEntries(), "/specs/size", DataEntry::getInteger)),
        counts(map(extractMergeData(inputs.getMatchEntries(), "/auto/start_pos",
            DataEntry::getInteger, Analyzer::mostCommon), StartPosition::of)),
        mostCommon(extractData(inputs.getPitEntries(), "/auto/score", DataEntry::getInteger)),
        counts(map(extractMergeData(inputs.getMatchEntries(), "/auto/scoring",
            DataEntry::getIntegers, Analyzer::mostCommon).stream()
                                                         .flatMap(List::stream)
                                                         .toList(),
            AutoAction::of)),
        nullableMap(List.of(
            nullableMapEntry(CoralLevel.L1,
                average(extractMergeData(inputs.getMatchEntries(), "/teleop/coral_l1",
                    DataEntry::getInteger, Analyzer::mostCommon))),
            nullableMapEntry(CoralLevel.L2,
                average(extractMergeData(inputs.getMatchEntries(), "/teleop/coral_l2",
                    DataEntry::getInteger, Analyzer::mostCommon))),
            nullableMapEntry(CoralLevel.L3,
                average(extractMergeData(inputs.getMatchEntries(), "/teleop/coral_l3",
                    DataEntry::getInteger, Analyzer::mostCommon))),
            nullableMapEntry(CoralLevel.L4,
                average(extractMergeData(inputs.getMatchEntries(), "/teleop/coral_l4",
                    DataEntry::getInteger, Analyzer::mostCommon))))),
        nullableMap(List.of(
            nullableMapEntry(AlgaeScore.PROCESSOR,
                average(extractMergeData(inputs.getMatchEntries(), "/teleop/algae_processor",
                    DataEntry::getInteger, Analyzer::mostCommon))),
            nullableMapEntry(AlgaeScore.BARGE_NET,
                average(extractMergeData(inputs.getMatchEntries(), "/teleop/algae_net",
                    DataEntry::getInteger, Analyzer::mostCommon))))),
        counts(map(extractMergeData(inputs.getMatchEntries(), "/teleop/pickups",
            DataEntry::getIntegers, Analyzer::mostCommon).stream()
                                                         .flatMap(List::stream)
                                                         .toList(),
            GamePiece::of)),
        counts(map(extractMergeData(inputs.getMatchEntries(), "/endgame/status",
            DataEntry::getInteger, Analyzer::mostCommon), FinalStatus::of)),
        average(map(extractMergeData(inputs.getMatchEntries(), "/summary/fall",
            DataEntry::getBoolean, Analyzer::mostCommon), b -> b ? 1 : 0)),
        average(map(extractMergeData(inputs.getMatchEntries(), "/summary/damage",
            DataEntry::getBoolean, Analyzer::mostCommon), b -> b ? 1 : 0)),
        counts(map(extractMergeData(inputs.getMatchEntries(), "/summary/fouls",
            DataEntry::getIntegers, Analyzer::mostCommon).stream()
                                                         .flatMap(List::stream)
                                                         .toList(),
            Fouls::of)),
        counts(map(extractMergeData(inputs.getMatchEntries(), "/summary/disabled",
            DataEntry::getInteger, Analyzer::mostCommon), Disabled::of)));
  }

  @Override
  protected List<StatisticsPage> generateStatistics(ReefscapeData data) {
    return List.of(
        new StatisticsPage("Summary",
            List.of(new WltStatistic(data.wlt()), new RankingPointsStatistic(data.rankingPoints()),
                new OprStatistic(data.opr()), driveTeamRadar(data))),
        new StatisticsPage("Specs",
            List.of(new StringStatistic("Drivetrain", data.drivetrain()),
                new StringStatistic("Weight", data.weight(), " lbs"),
                new StringStatistic("Size", data.size(), " in")
            // new StringStatistic("Speed", data.speed(), "
            // / 5")
            )),
        new StatisticsPage("Auto",
            List.of(new PieChartStatistic("Start Position", data.autoStartPositions()),
                new StringStatistic("Expected Score", data.autoPitAnticipatedScore()),
                new PieChartStatistic("Actions", data.autoScoring()))),
        new StatisticsPage("Teleop", List.of(
            // new
            // StringStatistic("Defense",
            // data.defense(), " / 5"),
            new PieChartStatistic("Coral Scored", data.teleopCoralScores()),
            new PieChartStatistic("Algae Scored", data.teleopAlgaeScores()),
            new PieChartStatistic("Ground Pickups", data.groundPickups()))),
        new StatisticsPage("Endgame",
            List.of(new PieChartStatistic("Final Status", data.endgameStatusCounts()))),
        new StatisticsPage("General",
            List.of(new StringStatistic("Fall Rate", data.fallRate(), "%"),
                new StringStatistic("Damage Rate", data.damageRate(), "%"),
                new PieChartStatistic("Fouls", data.fouls()),
                new PieChartStatistic("Disables", data.disables()))));
  }

  private static RadarStatistic driveTeamRadar(ReefscapeData data) {
    return new RadarStatistic("Drive Team", 5,
        nullableMap(List.of(nullableMapEntry("Communication", data.driveTeamCommunication()),
            nullableMapEntry("Strategy", data.driveTeamStrategy()),
            nullableMapEntry("Adaptability", data.driveTeamAdaptability()),
            nullableMapEntry("Professionalism", data.driveTeamProfessionalism()))));
  }
}
