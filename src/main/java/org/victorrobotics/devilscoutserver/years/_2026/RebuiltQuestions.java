package org.victorrobotics.devilscoutserver.years._2026;

import org.victorrobotics.devilscoutserver.questions.BooleanQuestion;
import org.victorrobotics.devilscoutserver.questions.CounterQuestion;
import org.victorrobotics.devilscoutserver.questions.MultipleChoiceQuestion;
import org.victorrobotics.devilscoutserver.questions.NumberQuestion;
import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.questions.Question.Page;
import org.victorrobotics.devilscoutserver.questions.Questions;
import org.victorrobotics.devilscoutserver.questions.RangeQuestion;
import org.victorrobotics.devilscoutserver.questions.SingleChoiceQuestion;
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
import org.victorrobotics.devilscoutserver.years._2026.RebuiltEnums.TraversePaths;

import java.util.List;

public class RebuiltQuestions extends Questions {
  @Override
  protected List<Page> matchQuestions() {
    return List.of(
        new Page("Pre-Match", "prematch",
            new SingleChoiceQuestion("Robot Position", "start_pos", StartPosition.values()),
            new BooleanQuestion("Preloaded?", "preload")),
        new Page("Auto", "auto",
            new MultipleChoiceQuestion("Actions", "actions", AutoAction.values())),
        new Page("Teleop", "teleop",
            new CounterQuestion("Cycles (intake midfield, score)", "cycles"),
            new BooleanQuestion("Ferried from midfield", "ferried"),
            new BooleanQuestion("Played defense", "defense")),
        new Page("Endgame", "endgame",
            new SingleChoiceQuestion("Tower Climb", "climb", ClimbStatus.values())),
        new Page("Summary", "summary", new RangeQuestion("Speed", "speed", 1, 5, 1),
            new RangeQuestion("Defense", "defense", 1, 5, 1),
            new BooleanQuestion("Fell over?", "fall"),
            new BooleanQuestion("Damaged other robots?", "damage"),
            new MultipleChoiceQuestion("Fouls?", "fouls", FoulType.values()),
            new MultipleChoiceQuestion("Disabled during match?", "disabled", DisabledReason.values())));
  }

  @Override
  protected List<Page> pitQuestions() {
    return List.of(
        new Page("Specs", "specs",
            new SingleChoiceQuestion("Drivetrain", "drivetrain", DrivetrainType.values()),
            new NumberQuestion("Chassis size (in)", "size", 0, 60, 28),
            new NumberQuestion("Weight (lbs, no battery/bumpers)", "weight", 0, 150, 100),
            new MultipleChoiceQuestion("Traversal Method(s)", "traverse", TraversePaths.values())),
        new Page("Shooter", "shooter",
            new MultipleChoiceQuestion("Abilities", "abilities", ShooterAbility.values()),
            new SingleChoiceQuestion("Shooting Rate", "rate", FuelRate.values()),
            new SingleChoiceQuestion("Accuracy", "accuracy", ShootingAccuracy.values())),
        new Page("Intake", "intake",
            new MultipleChoiceQuestion("Intake Locations", "locations", FuelPickup.values()),
            new SingleChoiceQuestion("Intake Rate", "rate", FuelRate.values()),
            new NumberQuestion("Hopper Capacity", "capacity", 0, 100, 30)),
        new Page("Climber", "climber",
            new MultipleChoiceQuestion("Tower Rungs", "levels", TowerRung.values()),
            new NumberQuestion("Climb Time (seconds)", "time", 0, 30, 5)),
        new Page("Auto", "auto",
            new MultipleChoiceQuestion("Start Positions", "start_pos", StartPosition.values()),
            new MultipleChoiceQuestion("Possible Actions", "actions", AutoAction.values()),
            new NumberQuestion("Typical Score", "score", 0, 100, 0)));
  }

  @Override
  protected List<Question> driveTeamQuestions() {
    return List.of(new RangeQuestion("Communication", "communication", 1, 5, 1),
        new RangeQuestion("Planning/Strategy", "strategy", 1, 5, 1),
        new RangeQuestion("Adaptability", "adaptability", 1, 5, 1),
        new RangeQuestion("Gracious Professionalism", "professionalism", 1, 5, 1));
  }
}
