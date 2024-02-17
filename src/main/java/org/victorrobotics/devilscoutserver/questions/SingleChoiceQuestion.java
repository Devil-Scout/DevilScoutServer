package org.victorrobotics.devilscoutserver.questions;

import java.util.ArrayList;
import java.util.List;

public final class SingleChoiceQuestion extends Question {
  public final List<String> options;

  public <T> SingleChoiceQuestion(String prompt, String key, Iterable<T> options) {
    super(QuestionType.SINGLE, prompt, key);
    this.options = new ArrayList<>();
    for (T option : options) {
      this.options.add(option.toString());
    }
  }

  public <T> SingleChoiceQuestion(String prompt, String key, T[] options) {
    this(prompt, key, List.of(options));
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Integer i && i >= 0 && i < options.size();
  }
}
