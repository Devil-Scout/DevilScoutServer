package org.victorrobotics.devilscoutserver.years._2025;

import org.victorrobotics.bluealliance.Event.WinLossRecord;
import org.victorrobotics.devilscoutserver.tba.OprsCache.TeamOpr;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.AlgaeScore;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.AutoAction;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.CoralLevel;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.Disabled;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.DrivetrainType;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.FinalStatus;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.Fouls;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.GamePiece;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.StartPosition;

import java.util.Map;

public record ReefscapeData(WinLossRecord wlt,
                            Map<String, Integer> rankingPoints,
                            TeamOpr opr,
                            Double driveTeamCommunication,
                            Double driveTeamStrategy,
                            Double driveTeamAdaptability,
                            Double driveTeamProfessionalism,
                            DrivetrainType drivetrain,
                            Integer weight,
                            Integer size,
                            // Double speed,
                            // Double defense,
                            // year specific
                            // auto
                            Map<StartPosition, Integer> autoStartPositions,
                            Integer autoPitAnticipatedScore,
                            Map<AutoAction, Integer> autoScoring,
                            // teleop
                            Map<CoralLevel, Double> teleopCoralScores,
                            Map<AlgaeScore, Double> teleopAlgaeScores,
                            Map<GamePiece, Integer> groundPickups,
                            // endgame
                            Map<FinalStatus, Integer> endgameStatusCounts,
                            // general
                            Double fallRate,
                            Double damageRate,
                            Map<Fouls, Integer> fouls,
                            Map<Disabled, Integer> disables) {}
