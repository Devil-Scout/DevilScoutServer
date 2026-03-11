package org.victorrobotics.devilscoutserver.questions;

public final class BooleanQuestion extends Question {
  public BooleanQuestion(String prompt, String key) {
    super(QuestionType.BOOLEAN, prompt, key);
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Boolean;
  }
}
