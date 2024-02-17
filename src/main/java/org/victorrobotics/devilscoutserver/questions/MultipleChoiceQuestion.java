package org.victorrobotics.devilscoutserver.questions;

import java.util.ArrayList;
import java.util.List;

public final class MultipleChoiceQuestion extends Question {
  public final List<String> options;

  public <T> MultipleChoiceQuestion(String prompt, String key, Iterable<T> options) {
    super(QuestionType.SEQUENCE, prompt, key);
    this.options = new ArrayList<>();
    for (T option : options) {
      this.options.add(option.toString());
    }
  }

  public <T> MultipleChoiceQuestion(String prompt, String key, T[] options) {
    this(prompt, key, List.of(options));
  }

  @Override
  public boolean isValidResponse(Object response) {
    if (!(response instanceof List<?> l)) return false;
    int optionCount = options.size();
    return l.stream()
            .allMatch(e -> e instanceof Integer i && i >= 0 && i < optionCount);
  }
}
