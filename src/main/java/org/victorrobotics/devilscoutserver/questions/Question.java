package org.victorrobotics.devilscoutserver.questions;

import java.util.List;

public abstract class Question {
  public final QuestionType type;
  public final String       prompt;
  public final String       key;

  protected Question(QuestionType type, String prompt, String key) {
    this.type = type;
    this.prompt = prompt;
    this.key = key;
  }

  public record Page(String title,
                     String key,
                     List<Question> questions) {
    public Page(String title, String key, Question... questions) {
      this(title, key, List.of(questions));
    }
  }

  public abstract boolean isValidResponse(Object response);
}
