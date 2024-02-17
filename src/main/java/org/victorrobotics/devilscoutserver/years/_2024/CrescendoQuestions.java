package org.victorrobotics.devilscoutserver.years._2024;

import org.victorrobotics.devilscoutserver.questions.BooleanQuestion;
import org.victorrobotics.devilscoutserver.questions.CounterQuestion;
import org.victorrobotics.devilscoutserver.questions.MultipleChoiceQuestion;
import org.victorrobotics.devilscoutserver.questions.NumberQuestion;
import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.questions.Questions;
import org.victorrobotics.devilscoutserver.questions.RangeQuestion;
import org.victorrobotics.devilscoutserver.questions.SequenceQuestion;
import org.victorrobotics.devilscoutserver.questions.SingleChoiceQuestion;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.AutoActions;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.DrivetrainType;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.FinalStatus;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.PickupLocation;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.ScoreLocation;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoEnums.StartPosition;

import java.util.List;

public class CrescendoQuestions extends Questions {
  @Override
  protected List<Question.Page> matchQuestions() {
    return List.of(new Question.Page("Auto", "auto",
                                     new SingleChoiceQuestion("Start Position", "start_pos",
                                                              StartPosition.values()),
                                     new SequenceQuestion("Auto Routine", "routine",
                                                          AutoActions.values())),
                   new Question.Page("Teleop", "teleop",
                                     new CounterQuestion("Pickups from ground", "pickup_ground"),
                                     new CounterQuestion("Pickups from source", "pickup_source"),
                                     new CounterQuestion("Scores in speaker", "score_speaker"),
                                     new CounterQuestion("Scores in amp", "score_amp")),
                   new Question.Page("Endgame", "endgame",
                                     new SingleChoiceQuestion("Final Status", "status",
                                                              FinalStatus.values()),
                                     new BooleanQuestion("Scored in trap?", "trap")),
                   new Question.Page("General", "general",
                                     new RangeQuestion("Speed", "speed", 1, 5, 1)));
  }

  @Override
  protected List<Question.Page> pitQuestions() {
    return List.of(new Question.Page("Specs", "specs",
                                     new SingleChoiceQuestion("Drivetrain", "drivetrain",
                                                              DrivetrainType.values()),
                                     new NumberQuestion("Chassis size (in)", "size", 18, 30, 28),
                                     new NumberQuestion("Weight (no battery/bumpers)", "weight", 80,
                                                        125, 100)),
                   new Question.Page("Auto", "auto",
                                     new MultipleChoiceQuestion("Start Position(s)", "start_pos",
                                                                StartPosition.values()),
                                     new NumberQuestion("Number of Notes", "notes", 0, 11, 1)),
                   new Question.Page("Teleop", "teleop",
                                     new MultipleChoiceQuestion("Pickup Location(s)", "pickup",
                                                                PickupLocation.values()),
                                     new MultipleChoiceQuestion("Score Location(s)", "score",
                                                                ScoreLocation.values())),
                   new Question.Page("Endgame", "endgame",
                                     new SingleChoiceQuestion("Final Status", "status",
                                                              FinalStatus.values()),
                                     new BooleanQuestion("Note in Trap?", "trap")));
  }

  @Override
  protected List<Question> driveTeamQuestions() {
    return List.of(new RangeQuestion("Communication", "communication", 1, 5, 1),
                   new RangeQuestion("Planning/Strategy", "strategy", 1, 5, 1),
                   new RangeQuestion("Adaptability", "adaptability", 1, 5, 1),
                   new RangeQuestion("Gracious Professionalism", "professionalism", 1, 5, 1));
  }
}
