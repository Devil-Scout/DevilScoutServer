package org.victorrobotics.devilscoutserver.years._2025;

import org.victorrobotics.devilscoutserver.questions.BooleanQuestion;
import org.victorrobotics.devilscoutserver.questions.CounterQuestion;
import org.victorrobotics.devilscoutserver.questions.MultipleChoiceQuestion;
import org.victorrobotics.devilscoutserver.questions.NumberQuestion;
import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.questions.Question.Page;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.AlgaePickup;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.AlgaeScore;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.AutoAction;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.CageHeight;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.CoralLevel;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.CoralPickup;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.Disabled;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.DrivetrainType;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.FinalStatus;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.Fouls;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.GamePiece;
import org.victorrobotics.devilscoutserver.years._2025.ReefscapeEnums.StartPosition;
import org.victorrobotics.devilscoutserver.questions.Questions;
import org.victorrobotics.devilscoutserver.questions.RangeQuestion;
import org.victorrobotics.devilscoutserver.questions.SingleChoiceQuestion;

import java.util.List;

public class ReefscapeQuestions extends Questions {
  @Override
  protected List<Page> matchQuestions() {
    return List.of(
        new Page("Auto", "auto",
            new SingleChoiceQuestion("Robot Position", "start_pos", StartPosition.values()),
            new MultipleChoiceQuestion("Scoring", "scoring", AutoAction.values())),
        new Page("Teleop", "teleop", new CounterQuestion("Coral in Trough", "coral_l1"),
            new CounterQuestion("Coral on L2", "coral_l2"),
            new CounterQuestion("Coral on L3", "coral_l3"),
            new CounterQuestion("Coral on L4", "coral_l4"),
            new CounterQuestion("Algae in Processor", "algae_processor"),
            new CounterQuestion("Algae in Net (by robot)", "algae_net"),
            new MultipleChoiceQuestion("Ground Pickups", "pickups", GamePiece.values())),
        new Page("Endgame", "endgame",
            new SingleChoiceQuestion("Final Status", "status", FinalStatus.values())),
        new Page("Summary", "summary", new RangeQuestion("Speed", "speed", 1, 5, 1),
            new RangeQuestion("Defense", "defense", 1, 5, 1),
            new BooleanQuestion("Fell over?", "fall"),
            new BooleanQuestion("Damaged other robots?", "damage"),
            new MultipleChoiceQuestion("Fouls?", "fouls", Fouls.values()),
            new SingleChoiceQuestion("Disabled during match?", "disabled", Disabled.values())));
  }

  @Override
  protected List<Page> pitQuestions() {
    return List.of(
        new Page("Specs", "specs",
            new SingleChoiceQuestion("Drivetrain", "drivetrain", DrivetrainType.values()),
            new NumberQuestion("Chassis size (in)", "size", 0, 60, 28),
            new NumberQuestion("Weight (lbs, no battery/bumpers)", "weight", 0, 150, 100)),
        new Page("Auto", "auto",
            new MultipleChoiceQuestion("Start Positions", "start_pos", StartPosition.values()),
            new NumberQuestion("Typical Score", "score", 0, 30, 0)),
        new Page("Teleop", "teleop",
            new MultipleChoiceQuestion("Coral Pickups", "coral_pickup", CoralPickup.values()),
            new MultipleChoiceQuestion("Coral Scores", "coral_score", CoralLevel.values()),
            new MultipleChoiceQuestion("Algae Pickups", "algae_pickup", AlgaePickup.values()),
            new MultipleChoiceQuestion("Algae Scores", "algae_score", AlgaeScore.values())),
        new Page("Endgame", "endgame",
            new MultipleChoiceQuestion("Climb?", "climb", CageHeight.values())));
  }

  @Override
  protected List<Question> driveTeamQuestions() {
    return List.of(new RangeQuestion("Communication", "communication", 1, 5, 1),
        new RangeQuestion("Planning/Strategy", "strategy", 1, 5, 1),
        new RangeQuestion("Adaptability", "adaptability", 1, 5, 1),
        new RangeQuestion("Gracious Professionalism", "professionalism", 1, 5, 1));
  }
}
