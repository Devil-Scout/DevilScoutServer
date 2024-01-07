package org.victorrobotics.devilscoutserver.questions;

import java.util.Map;

public final class BooleanQuestion extends Question {
  public BooleanQuestion(String prompt, String key) {
    super(QuestionType.BOOLEAN, prompt, key);
  }

  @Override
  public Map<String, Object> getConfig() {
    return null;
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Boolean;
  }
}
