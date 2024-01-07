package org.victorrobotics.devilscoutserver.questions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@SuppressWarnings("java:S2057") // serialVersionUID
public class QuestionDeserializer extends StdDeserializer<Question> {
  public QuestionDeserializer() {
    super((Class<?>) null);
  }

  @Override
  public Question deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
    JsonNode node = parser.readValueAsTree();

    String prompt = node.get("prompt")
                        .textValue();
    String key = node.get("key")
                     .textValue();
    JsonNode config = node.get("config");

    QuestionType type = QuestionType.valueOf(node.get("type")
                                                 .asText());
    return switch (type) {
      case BOOLEAN -> new BooleanQuestion(prompt, key);
      case COUNTER -> new CounterQuestion(prompt, key);
      case MULTIPLE, SEQUENCE, SINGLE -> {
        List<String> options = new ArrayList<>();
        config.get("options")
              .elements()
              .forEachRemaining(e -> options.add(e.textValue()));
        yield switch (type) {
          case MULTIPLE -> new MultipleChoiceQuestion(prompt, key, options);
          case SEQUENCE -> new SequenceQuestion(prompt, key, options);
          case SINGLE -> new SingleChoiceQuestion(prompt, key, options);
          default -> throw new IllegalStateException();
        };
      }
      case NUMBER -> {
        JsonNode min = config.get("min");
        JsonNode max = config.get("max");
        yield new NumberQuestion(prompt, key, min == null ? null : min.intValue(),
                                 max == null ? null : max.intValue());
      }
      case RANGE -> {
        int min = config.get("min")
                        .intValue();
        int max = config.get("max")
                        .intValue();
        JsonNode increment = config.get("increment");
        yield new RangeQuestion(prompt, key, min, max,
                                increment == null ? 1 : increment.intValue());
      }
    };
  }
}
