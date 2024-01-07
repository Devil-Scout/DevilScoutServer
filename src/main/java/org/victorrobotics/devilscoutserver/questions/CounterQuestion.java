package org.victorrobotics.devilscoutserver.questions;

import java.util.Map;

public final class CounterQuestion extends Question {
  public CounterQuestion(String prompt, String key) {
    super(QuestionType.COUNTER, prompt, key);
  }

  @Override
  public QuestionType getType() {
    return QuestionType.BOOLEAN;
  }

  @Override
  public Map<String, Object> getConfig() {
    return null;
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Integer i && i >= 0;
  }
}
