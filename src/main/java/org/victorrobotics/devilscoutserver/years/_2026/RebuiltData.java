package org.victorrobotics.devilscoutserver.years._2026;

import org.victorrobotics.bluealliance.Event.WinLossRecord;
import org.victorrobotics.devilscoutserver.analysis.data.NumberSummary;
import org.victorrobotics.devilscoutserver.tba.OprsCache.TeamOpr;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.AutoAction;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.ClimbStatus;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.DisabledReason;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.DrivetrainType;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.FoulType;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.FuelPickup;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.FuelRate;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.ShooterAbility;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.ShooterType;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.ShootingAccuracy;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.StartPosition;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.TowerRung;
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.TraversePath;

import java.util.Map;
import java.util.Set;

public record RebuiltData(TbaData tbaData,
                          DriveTeamScoutingData driveTeamData,
                          PitScoutingData pitData,
                          MatchScoutingData matchData) {
  public record TbaData(Integer rank,
                        Integer teamCount,
                        WinLossRecord wlt,
                        TeamOpr opr) {}

  public record DriveTeamScoutingData(Double communication,
                                      Double strategy,
                                      Double adaptability,
                                      Double professionalism) {}

  public record PitScoutingData(// Chassis
                                DrivetrainType drivetrain,
                                Integer robotWeight,
                                Integer robotLength,
                                Set<TraversePath> traversePaths,
                                // Shooter
				ShooterType shooterType,
                                Set<ShooterAbility> shooterAbilities,
                                FuelRate shootingRate,
                                ShootingAccuracy shootingAccuracy,
                                // Intake
                                Set<FuelPickup> intakeLocations,
                                FuelRate intakeRate,
                                Integer hopperCapacity,
                                // Climber
                                Set<TowerRung> climberRungs,
                                Integer climberTime,
                                // Auto
                                Set<StartPosition> startPositions,
                                Set<AutoAction> autoActions,
                                Integer typicalScore,
				// Drive Team
				ShootingAccuracy humanPlayerAccuracy,
				Integer driverPracticeHours) {}

  public record MatchScoutingData(// Pre-Match
                                  Map<StartPosition, Integer> startPositions,
                                  // Auto
                                  Map<AutoAction, Integer> autoActions,
                                  // Teleop
                                  NumberSummary teleopCycles,
                                  NumberSummary ferryCycles,
                                  Map<TraversePath, Integer> traversals,
                                  Map<FuelPickup, Integer> pickups,
                                  Double stealRate,
                                  Double defenseRate,
                                  // Endgame
                                  Map<ClimbStatus, Integer> climbResults,
                                  // Opinions
                                  Map<ShootingAccuracy, Integer> shootingAccuracies,
                                  Map<FuelRate, Integer> shootingRates,
                                  Map<FuelRate, Integer> intakeRates,
                                  Double avgDefenseRating,
                                  Double avgSpeedRating,
                                  // Mishaps
                                  Double fallRate,
                                  Double damageRate,
                                  Map<FoulType, Integer> fouls,
                                  Map<DisabledReason, Integer> disables) {}
}
