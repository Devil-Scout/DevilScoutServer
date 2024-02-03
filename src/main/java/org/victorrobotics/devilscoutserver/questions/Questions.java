package org.victorrobotics.devilscoutserver.questions;

import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Encode;
import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonDecode;
import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonEncode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Questions {
  private List<Question.Page> matchQuestions;
  private List<Question.Page> pitQuestions;
  private List<Question>     driveTeamQuestions;

  private String matchQuestionsJson;
  private String pitQuestionsJson;
  private String driveTeamQuestionsJson;

  private String matchQuestionsHash;
  private String pitQuestionsHash;
  private String driveTeamQuestionsHash;

  public Questions() {
    reloadMatchQuestions();
    reloadPitQuestions();
    reloadDriveTeamQuestions();
  }

  public void reloadMatchQuestions() {
    matchQuestions = loadQuestions("/match_questions.json", Question.Page[].class);
    matchQuestionsJson = jsonEncode(matchQuestions);
    matchQuestionsHash = hash(matchQuestionsJson);
  }

  public void reloadPitQuestions() {
    pitQuestions = loadQuestions("/pit_questions.json", Question.Page[].class);
    pitQuestionsJson = jsonEncode(pitQuestions);
    pitQuestionsHash = hash(pitQuestionsJson);
  }

  public void reloadDriveTeamQuestions() {
    driveTeamQuestions = loadQuestions("/drive_team_questions.json", Question[].class);
    driveTeamQuestionsJson = jsonEncode(driveTeamQuestions);
    driveTeamQuestionsHash = hash(driveTeamQuestionsJson);
  }

  public List<Question.Page> getMatchQuestions() {
    return matchQuestions;
  }

  public List<Question.Page> getPitQuestions() {
    return pitQuestions;
  }

  public List<Question> getDriveTeamQuestions() {
    return driveTeamQuestions;
  }

  public String getMatchQuestionsJson() {
    return matchQuestionsJson;
  }

  public String getPitQuestionsJson() {
    return pitQuestionsJson;
  }

  public String getDriveTeamQuestionsJson() {
    return driveTeamQuestionsJson;
  }

  public String getMatchQuestionsHash() {
    return matchQuestionsHash;
  }

  public String getPitQuestionsHash() {
    return pitQuestionsHash;
  }

  public String getDriveTeamQuestionsHash() {
    return driveTeamQuestionsHash;
  }

  private static String hash(String json) {
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      return base64Encode(sha256.digest(json.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static <T> List<T> loadQuestions(String filename, Class<T[]> clazz) {
    return List.of(jsonDecode(Questions.class.getResourceAsStream(filename), clazz));
  }
}
