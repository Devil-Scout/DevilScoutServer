package org.victorrobotics.devilscoutserver.years._2026;

import org.victorrobotics.bluealliance.ScoreBreakdown.UnknownScoreBreakdown;
import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.statistics.BooleanStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.NumberStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.OprStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.PieChartStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.RadarStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.analysis.statistics.StringStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.WltStatistic;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.OprsCache;
import org.victorrobotics.devilscoutserver.tba.RankingsCache;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltData.DriveTeamScoutingData;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltData.MatchScoutingData;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltData.PitScoutingData;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltData.TbaData;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.AutoAction;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.ClimbStatus;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.DisabledReason;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.DrivetrainType;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.FoulType;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.FuelPickup;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.FuelRate;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.ShooterAbility;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.ShootingAccuracy;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.StartPosition;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.TowerRung;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.TraversePath;

import java.util.List;

public class RebuiltAnalyzer extends Analyzer<UnknownScoreBreakdown, RebuiltData> {
  public RebuiltAnalyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
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
  protected RebuiltData computeData(Analyzer<UnknownScoreBreakdown, RebuiltData>.Data inputs) {
    // @formatter:off

    final var rankings = inputs.getRankings();
    TbaData tbaData = new TbaData(
      mapSingle(rankings, RankingsCache.Team::getRank),
      mapSingle(rankings, RankingsCache.Team::getWinLossRecord),
      inputs.getOpr()
    );

    final var driveTeamEntries = inputs.getDriveTeamEntries();
    DriveTeamScoutingData driveTeamData = new DriveTeamScoutingData(
      average(extractMergeData(driveTeamEntries, "/communication", DataEntry::getInteger, Analyzer::average)),
      average(extractMergeData(driveTeamEntries, "/strategy", DataEntry::getInteger, Analyzer::average)),
      average(extractMergeData(driveTeamEntries, "/adaptability", DataEntry::getInteger, Analyzer::average)),
      average(extractMergeData(driveTeamEntries, "/professionalism", DataEntry::getInteger, Analyzer::average))
    );

    final var pitEntries = inputs.getPitEntries();
    PitScoutingData pitData = new PitScoutingData(
      // Chassis
      mostCommon(map(extractData(pitEntries, "/chassis/drivetrain", DataEntry::getInteger), DrivetrainType::of)),
      mostCommon(extractData(pitEntries, "/chassis/weight", DataEntry::getInteger)),
      mostCommon(extractData(pitEntries, "/chassis/size", DataEntry::getInteger)),
      mapSet(setUnion(extractData(pitEntries, "/chassis/traverse", DataEntry::getIntegers)), TraversePath::of),
      // Shooter
      mapSet(setUnion(extractData(pitEntries, "/shooter/abilities", DataEntry::getIntegers)), ShooterAbility::of),
      mostCommon(map(extractData(pitEntries, "/shooter/rate", DataEntry::getInteger), FuelRate::of)),
      mostCommon(map(extractData(pitEntries, "/shooter/accuracy", DataEntry::getInteger), ShootingAccuracy::of)),
      // Intake
      mapSet(setUnion(extractData(pitEntries, "/intake/locations", DataEntry::getIntegers)), FuelPickup::of),
      mostCommon(map(extractData(pitEntries, "/intake/rate", DataEntry::getInteger), FuelRate::of)),
      mostCommon(extractData(pitEntries, "/intake/capacity", DataEntry::getInteger)),
      // Climber
      mapSet(setUnion(extractData(pitEntries, "/climber/levels", DataEntry::getIntegers)), TowerRung::of),
      mostCommon(extractData(pitEntries, "/climber/time", DataEntry::getInteger)),
      // Auto
      mapSet(setUnion(extractData(pitEntries, "/auto/start_pos", DataEntry::getIntegers)), StartPosition::of),
      mapSet(setUnion(extractData(pitEntries, "/auto/actions", DataEntry::getIntegers)), AutoAction::of),
      mostCommon(extractData(pitEntries, "/auto/score", DataEntry::getInteger))
    );

    final var matchEntries = inputs.getMatchEntries();
    MatchScoutingData matchData = new MatchScoutingData(
      // Pre-Match
      counts(map(
        extractMergeData(matchEntries, "/prematch/start_pos", DataEntry::getInteger, Analyzer::mostCommon),
        StartPosition::of)),
      rate(extractMergeData(matchEntries, "/prematch/preload", DataEntry::getBoolean, Analyzer::mostCommon)),
      // Auto
      counts(map(union(
        extractMergeData(matchEntries, "/auto/actions", DataEntry::getIntegers, Analyzer::setUnion)
      ), AutoAction::of)),
      // Teleop
      summarizeNumbers(
        extractMergeData(matchEntries, "/teleop/shoot_cycles", DataEntry::getInteger, Analyzer::average)
      ),
      summarizeNumbers(
        extractMergeData(matchEntries, "/teleop/ferry_cycles", DataEntry::getInteger, Analyzer::average)
      ),
      counts(map(union(
        extractMergeData(matchEntries, "/teleop/traverse", DataEntry::getIntegers, Analyzer::setUnion)
      ), TraversePath::of)),
      counts(map(union(
        extractMergeData(matchEntries, "/teleop/intake_locs", DataEntry::getIntegers, Analyzer::setUnion)
      ), FuelPickup::of)),
      rate(extractMergeData(matchEntries, "/teleop/defense", DataEntry::getBoolean, Analyzer::mostCommon)),
      // Endgame
      counts(map(
        extractMergeData(matchEntries, "/endgame/climb", DataEntry::getInteger, Analyzer::mostCommon),
        ClimbStatus::of)),
      // Opinions
      counts(map(union(
        extractMergeData(matchEntries, "/opinions/shoot_accuracy", DataEntry::getIntegers, Analyzer::union)
      ), ShootingAccuracy::of)),
      counts(map(union(
        extractMergeData(matchEntries, "/opinions/shoot_rate", DataEntry::getIntegers, Analyzer::union)
      ), FuelRate::of)),
      counts(map(union(
        extractMergeData(matchEntries, "/opinions/intake_rate", DataEntry::getIntegers, Analyzer::union)
      ), FuelRate::of)),
      average(extractMergeData(matchEntries, "/opinions/defense", DataEntry::getInteger, Analyzer::average)),
      average(extractMergeData(matchEntries, "/opinions/speed", DataEntry::getInteger, Analyzer::average)),
      // Mishaps
      rate(extractMergeData(matchEntries, "/mishaps/fall", DataEntry::getBoolean, Analyzer::mostCommon)),
      rate(extractMergeData(matchEntries, "/mishaps/damage", DataEntry::getBoolean, Analyzer::mostCommon)),
      counts(map(union(
        extractMergeData(matchEntries, "/mishaps/fouls", DataEntry::getIntegers, Analyzer::setUnion)
      ), FoulType::of)),
      counts(map(union(
        extractMergeData(matchEntries, "/mishaps/disables", DataEntry::getIntegers, Analyzer::setUnion)
      ), DisabledReason::of))
    );

    // @formatter:on

    return new RebuiltData(tbaData, driveTeamData, pitData, matchData);
  }

  @Override
  protected List<StatisticsPage> generateStatistics(RebuiltData data) {
    return List.of(new StatisticsPage("Summary", new StringStatistic("Rank", data.tbaData()
                                                                                 .rank()),
        new WltStatistic(data.tbaData()
                             .wlt()),
        new OprStatistic(data.tbaData()
                             .opr()),
        driveTeamRadar(data)),
        new StatisticsPage("Chassis", new StringStatistic("Weight", data.pitData()
                                                                        .robotWeight(),
            " lbs"),
            new StringStatistic("Length", data.pitData()
                                              .robotLength(),
                " in"),
            new StringStatistic("Hopper Capacity", data.pitData()
                                                       .hopperCapacity(),
                " fuel")),
        new StatisticsPage("Driving", new StringStatistic("Drivetrain Type", data.pitData()
                                                                                 .drivetrain()),
            new StringStatistic("Speed Rating", data.matchData()
                                                    .avgSpeedRating(),
                " / 5"),
            new StringStatistic("Defense Rating", data.matchData()
                                                      .avgSpeedRating(),
                " / 5"),
            new BooleanStatistic("Defense Rate", data.matchData()
                                                     .defenseRate()),
            new StringStatistic("Traversing (claimed)", data.pitData()
                                                            .traversePaths()),
            new PieChartStatistic("Traversing (actual)", data.matchData()
                                                             .traversals())),
        new StatisticsPage("Shooter", new StringStatistic("Abilities (claimed)", data.pitData()
                                                                                     .shooterAbilities()),
            new NumberStatistic("Shooting Cycles", data.matchData()
                                                       .teleopCycles()),
            new NumberStatistic("Ferrying Cycles", data.matchData()
                                                       .teleopCycles()),
            new StringStatistic("Shot Rate (claimed)", data.pitData()
                                                           .shootingRate()),
            new PieChartStatistic("Shot Rate (actual)", data.matchData()
                                                            .shootingRates()),
            new StringStatistic("Accuracy (claimed)", data.pitData()
                                                          .shootingAccuracy()),
            new PieChartStatistic("Accuracy (actual)", data.matchData()
                                                           .shootingAccuracies())),
        new StatisticsPage("Intake", new StringStatistic("Pickups (claimed)", data.pitData()
                                                                                  .intakeLocations()),
            new PieChartStatistic("Pickups (actual)", data.matchData()
                                                          .pickups()),
            new StringStatistic("Pickup Rate (claimed)", data.pitData()
                                                             .intakeRate()),
            new PieChartStatistic("Pickup Rate (actual)", data.matchData()
                                                              .intakeRates())),
        new StatisticsPage("Climber", new StringStatistic("Tower Rungs (claimed)", data.pitData()
                                                                                       .climberRungs()),
            new StringStatistic("Climb Time (claimed)", data.pitData()
                                                            .climberTime(),
                " seconds"),
            new PieChartStatistic("Endgame Rungs (actual)", data.matchData()
                                                                .climbResults())),
        new StatisticsPage("Auto", new StringStatistic("Auto Actions (claimed)", data.pitData()
                                                                                     .autoActions()),
            new PieChartStatistic("Auto Actions (actual)", data.matchData()
                                                               .autoActions()),
            new StringStatistic("Starting Positions (claimed)", data.pitData()
                                                                    .startPositions()),
            new PieChartStatistic("Starting Positions (actual)", data.matchData()
                                                                     .startPositions()),
            new BooleanStatistic("Preload Rate", data.matchData()
                                                     .preloadRate())),
        new StatisticsPage("Miscellaneous", new BooleanStatistic("Fall Rate", data.matchData()
                                                                                  .fallRate()),
            new BooleanStatistic("Damage Rate", data.matchData()
                                                    .damageRate()),
            new PieChartStatistic("Fouls", data.matchData()
                                               .fouls()),
            new PieChartStatistic("Disables", data.matchData()
                                                  .disables())));
  }

  private static RadarStatistic driveTeamRadar(RebuiltData data) {
    return new RadarStatistic("Drive Team", 5,
        nullableMap(List.of(nullableMapEntry("Communication", data.driveTeamData()
                                                                  .communication()),
            nullableMapEntry("Strategy", data.driveTeamData()
                                             .strategy()),
            nullableMapEntry("Adaptability", data.driveTeamData()
                                                 .adaptability()),
            nullableMapEntry("Professionalism", data.driveTeamData()
                                                    .professionalism()))));
  }
}
