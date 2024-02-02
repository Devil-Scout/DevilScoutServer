package org.victorrobotics.devilscoutserver.questions;

import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Encode;

import org.victorrobotics.devilscoutserver.controller.QuestionController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("java:S2386")
public final class Questions {
  public static final List<QuestionPage> MATCH_QUESTIONS;
  public static final List<QuestionPage> PIT_QUESTIONS;
  public static final List<Question> DRIVE_TEAM_QUESTIONS;

  public static final String MATCH_QUESTIONS_JSON;
  public static final String PIT_QUESTIONS_JSON;
  public static final String DRIVE_TEAM_QUESTIONS_JSON;

  public static final String MATCH_QUESTIONS_HASH;
  public static final String PIT_QUESTIONS_HASH;
  public static final String DRIVE_TEAM_QUESTIONS_HASH;

  static {
    try {
      ObjectMapper json = new ObjectMapper();
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

      MATCH_QUESTIONS = List.of(jsonResource(json, "/match_questions.json", QuestionPage[].class));
      PIT_QUESTIONS = List.of(jsonResource(json, "/pit_questions.json", QuestionPage[].class));
      DRIVE_TEAM_QUESTIONS =
          List.of(jsonResource(json, "/drive_team_questions.json", Question[].class));

      MATCH_QUESTIONS_JSON = json.writeValueAsString(MATCH_QUESTIONS);
      PIT_QUESTIONS_JSON = json.writeValueAsString(PIT_QUESTIONS);
      DRIVE_TEAM_QUESTIONS_JSON = json.writeValueAsString(DRIVE_TEAM_QUESTIONS);

      MATCH_QUESTIONS_HASH = hash(sha256, MATCH_QUESTIONS_JSON);
      PIT_QUESTIONS_HASH = hash(sha256, PIT_QUESTIONS_JSON);
      DRIVE_TEAM_QUESTIONS_HASH = hash(sha256, DRIVE_TEAM_QUESTIONS_JSON);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private Questions() {}

  private static <T> T jsonResource(ObjectMapper json, String name, Class<T> clazz)
      throws IOException {
    return json.readValue(QuestionController.class.getResourceAsStream(name), clazz);
  }

  private static String hash(MessageDigest digest, String json) {
    return base64Encode(digest.digest(json.getBytes(StandardCharsets.UTF_8)));
  }
}
