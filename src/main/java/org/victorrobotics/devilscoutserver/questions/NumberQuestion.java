package org.victorrobotics.devilscoutserver.questions;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NumberQuestion extends Question {
  public final Integer min;
  public final Integer max;

  public NumberQuestion(String prompt, String key, Integer min, Integer max) {
    super(QuestionType.NUMBER, prompt, key);
    this.min = min;
    this.max = max;
  }

  @Override
  public Map<String, Object> getConfig() {
    Map<String, Object> map = new LinkedHashMap<>();

    if (min != null) {
      map.put("min", min);
    }

    if (max != null) {
      map.put("max", min);
    }

    return map;
  }

  @Override
  public boolean isValidResponse(Object response) {
    return response instanceof Integer i && (min == null || i >= min) && (max == null || i <= max);
  }
}
