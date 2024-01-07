package org.victorrobotics.devilscoutserver.questions;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

@JsonDeserialize(using = QuestionDeserializer.class)
public abstract sealed class Question
    permits BooleanQuestion, CounterQuestion, MultipleChoiceQuestion, NumberQuestion, RangeQuestion,
    SequenceQuestion, SingleChoiceQuestion {
  private final QuestionType type;
  private final String       prompt;
  private final String       key;

  protected Question(QuestionType type, String prompt, String key) {
    this.type = type;
    this.prompt = prompt;
    this.key = key;
  }

  @JsonInclude(Include.NON_EMPTY)
  public abstract Map<String, Object> getConfig();

  public abstract boolean isValidResponse(Object response);

  @OpenApiRequired
  public QuestionType getType() {
    return type;
  }

  @OpenApiRequired
  @OpenApiExample("Drivetrain Type")
  public String getPrompt() {
    return prompt;
  }

  @OpenApiRequired
  @OpenApiExample("drivetrain")
  public String getKey() {
    return key;
  }
}
