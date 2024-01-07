package org.victorrobotics.devilscoutserver.questions;

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
