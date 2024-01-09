package org.victorrobotics.devilscoutserver.questions;

public final class CounterQuestion extends Question {
  public CounterQuestion(String prompt, String key) {
    super(QuestionType.COUNTER, prompt, key);
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Integer i && i >= 0;
  }
}
