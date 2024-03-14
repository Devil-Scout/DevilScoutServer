package org.victorrobotics.devilscoutserver.years._2024;

import org.victorrobotics.bluealliance.Event.WinLossRecord;
import org.victorrobotics.devilscoutserver.analysis.data.NumberSummary;
import org.victorrobotics.devilscoutserver.tba.OprsCache.TeamOpr;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.DrivetrainType;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.FinalStatus;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.PickupLocation;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.ScoreLocation;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.StartPosition;

import java.util.Map;

public record CrescendoData(WinLossRecord wlt,
                            Map<String, Integer> rankingPoints,
                            TeamOpr opr,
                            Double driveTeamCommunication,
                            Double driveTeamStrategy,
                            Double driveTeamAdaptability,
                            Double driveTeamProfessionalism,
                            DrivetrainType drivetrain,
                            Integer weight,
                            Integer size,
                            Double speed,
                            Double defense,
                            Map<StartPosition, Integer> autoStartPositions,
                            NumberSummary autoNotes,
                            NumberSummary teleopCyclesPerMinute,
                            Double teleopScoreAccuracy,
                            Map<ScoreLocation, Integer> teleopScoreCounts,
                            Map<PickupLocation, Integer> teleopPickupCounts,
                            Map<FinalStatus, Integer> endgameStatusCounts,
                            Double trapRate) {}
