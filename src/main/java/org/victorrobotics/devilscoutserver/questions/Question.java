package org.victorrobotics.devilscoutserver.questions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = Question.Deserializer.class)
public abstract class Question {
  public final QuestionType type;
  public final String       prompt;
  public final String       key;

  protected Question(QuestionType type, String prompt, String key) {
    this.type = type;
    this.prompt = prompt;
    this.key = key;
  }

  public static record Page(String title,
                            String key,
                            List<Question> questions) {}

  public abstract boolean isValidResponse(Object response);

  @SuppressWarnings("java:S2057") // serialVersionUID
  static class Deserializer extends StdDeserializer<Question> {
    public Deserializer() {
      super(Question.class);
    }

    @Override
    public Question deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
      JsonNode json = parser.readValueAsTree();

      String prompt = json.get("prompt")
                          .textValue();
      String key = json.get("key")
                       .textValue();

      QuestionType type = QuestionType.valueOf(json.get("type")
                                                   .asText());
      return switch (type) {
        case BOOLEAN -> new BooleanQuestion(prompt, key);
        case COUNTER -> new CounterQuestion(prompt, key);
        case NUMBER -> {
          int min = json.get("min")
                        .intValue();
          int max = json.get("max")
                        .intValue();
          int defaultValue = json.get("default")
                                 .intValue();
          yield new NumberQuestion(prompt, key, min, max, defaultValue);
        }
        case RANGE -> {
          int min = json.get("min")
                        .intValue();
          int max = json.get("max")
                        .intValue();
          JsonNode increment = json.get("increment");
          yield new RangeQuestion(prompt, key, min, max,
                                  increment == null ? 1 : increment.intValue());
        }
        case MULTIPLE, SEQUENCE, SINGLE -> {
          List<String> options = new ArrayList<>();
          json.get("options")
              .elements()
              .forEachRemaining(e -> options.add(e.textValue()));
          yield switch (type) {
            case MULTIPLE -> new MultipleChoiceQuestion(prompt, key, options);
            case SEQUENCE -> new SequenceQuestion(prompt, key, options);
            case SINGLE -> new SingleChoiceQuestion(prompt, key, options);
            default -> throw new IllegalStateException();
          };
        }
      };
    }
  }

}
