package org.victorrobotics.devilscoutserver.questions;

import java.util.Map;

public final class RangeQuestion extends Question {
  public final int min;
  public final int max;
  public final int increment;

  public RangeQuestion(String prompt, String key, int min, int max, int increment) {
    super(QuestionType.RANGE, prompt, key);
    this.min = min;
    this.max = max;
    this.increment = increment;
  }

  @Override
  public Map<String, Object> getConfig() {
    return Map.of("min", min, "max", max, "increment", increment);
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Integer i && i >= min && i <= max && (i - min) % increment == 0;
  }
}
