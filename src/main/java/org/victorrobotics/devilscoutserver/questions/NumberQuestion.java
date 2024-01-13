package org.victorrobotics.devilscoutserver.questions;

public final class NumberQuestion extends Question {
  public final Integer min;
  public final Integer max;

  public NumberQuestion(String prompt, String key, Integer min, Integer max) {
    super(QuestionType.NUMBER, prompt, key);
    this.min = min;
    this.max = max;
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Integer i && (min == null || i >= min) && (max == null || i <= max);
  }
}