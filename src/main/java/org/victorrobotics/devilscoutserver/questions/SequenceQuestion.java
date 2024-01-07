package org.victorrobotics.devilscoutserver.questions;

import java.util.List;
import java.util.Map;

public final class SequenceQuestion extends Question {
  public final List<String> options;

  public SequenceQuestion(String prompt, String key, List<String> options) {
    super(QuestionType.SEQUENCE, prompt, key);
    this.options = options;
  }

  @Override
  public Map<String, Object> getConfig() {
    return Map.of("options", options);
  }

  @Override
  public boolean isValidResponse(Object response) {
    if (!(response instanceof List<?> l)) return false;
    int optionCount = options.size();
    return l.stream()
            .allMatch(e -> e instanceof Integer i && i >= 0 && i < optionCount);
  }
}
