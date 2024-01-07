package org.victorrobotics.devilscoutserver.questions;

import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Encode;

import org.victorrobotics.devilscoutserver.controller.QuestionController;
import org.victorrobotics.devilscoutserver.controller.QuestionController.QuestionPage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.databind.json.JsonMapper;

public final class Questions {
  public static final QuestionPage[] MATCH_QUESTIONS;
  public static final QuestionPage[] PIT_QUESTIONS;
  public static final Question[]     DRIVE_TEAM_QUESTIONS;

  public static final String MATCH_QUESTIONS_JSON;
  public static final String PIT_QUESTIONS_JSON;
  public static final String DRIVE_TEAM_QUESTIONS_JSON;

  public static final String MATCH_QUESTIONS_HASH;
  public static final String PIT_QUESTIONS_HASH;
  public static final String DRIVE_TEAM_QUESTIONS_HASH;

  static {
    try {
      JsonMapper json = new JsonMapper();
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

      MATCH_QUESTIONS = json.readValue(openResource("/match_questions.json"), QuestionPage[].class);
      PIT_QUESTIONS = json.readValue(openResource("/pit_questions.json"), QuestionPage[].class);
      DRIVE_TEAM_QUESTIONS =
          json.readValue(openResource("/drive_team_questions.json"), Question[].class);

      MATCH_QUESTIONS_JSON = json.writeValueAsString(MATCH_QUESTIONS);
      PIT_QUESTIONS_JSON = json.writeValueAsString(PIT_QUESTIONS);
      DRIVE_TEAM_QUESTIONS_JSON = json.writeValueAsString(DRIVE_TEAM_QUESTIONS);

      MATCH_QUESTIONS_HASH =
          base64Encode(sha256.digest(MATCH_QUESTIONS_JSON.getBytes(StandardCharsets.UTF_8)));
      PIT_QUESTIONS_HASH =
          base64Encode(sha256.digest(PIT_QUESTIONS_JSON.getBytes(StandardCharsets.UTF_8)));
      DRIVE_TEAM_QUESTIONS_HASH =
          base64Encode(sha256.digest(DRIVE_TEAM_QUESTIONS_JSON.getBytes(StandardCharsets.UTF_8)));
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private Questions() {}

  private static InputStream openResource(String name) {
    return QuestionController.class.getResourceAsStream(name);
  }
}
