package org.victorrobotics.devilscoutserver.questions;

import java.util.List;
import java.util.Map;

public final class SingleChoiceQuestion extends Question {
  public final List<String> options;

  public SingleChoiceQuestion(String prompt, String key, List<String> options) {
    super(QuestionType.SINGLE, prompt, key);
    this.options = options;
  }

  @Override
  public Map<String, Object> getConfig() {
    return Map.of("options", options);
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Integer i && i >= 0 && i < options.size();
  }
}
