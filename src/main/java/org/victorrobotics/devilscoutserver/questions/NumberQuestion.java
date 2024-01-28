package org.victorrobotics.devilscoutserver.questions;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class NumberQuestion extends Question {
  public final int min;
  public final int max;

  @JsonProperty("default")
  public final int defaultValue;

  public NumberQuestion(String prompt, String key, int min, int max, int defaultValue) {
    super(QuestionType.NUMBER, prompt, key);
    this.min = min;
    this.max = max;
    this.defaultValue = defaultValue;
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Integer i && i >= min && i <= max;
  }
}
