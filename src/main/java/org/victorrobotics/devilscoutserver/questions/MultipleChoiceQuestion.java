package org.victorrobotics.devilscoutserver.questions;

import java.util.List;

public final class MultipleChoiceQuestion extends Question {
  public final List<String> options;

  public MultipleChoiceQuestion(String prompt, String key, List<String> options) {
    super(QuestionType.MULTIPLE, prompt, key);
    this.options = options;
  }

  @Override
  public boolean isValidResponse(Object response) {
    if (!(response instanceof List<?> l)) return false;
    int optionCount = options.size();
    return l.stream()
            .allMatch(e -> e instanceof Integer i && i >= 0 && i < optionCount);
  }
}
