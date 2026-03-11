package org.victorrobotics.devilscoutserver.questions;

import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Encode;
import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonEncode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public abstract class Questions {
  private List<Question.Page> matchQuestions;
  private List<Question.Page> pitQuestions;
  private List<Question>      driveTeamQuestions;

  private String matchQuestionsJson;
  private String pitQuestionsJson;
  private String driveTeamQuestionsJson;

  private String matchQuestionsHash;
  private String pitQuestionsHash;
  private String driveTeamQuestionsHash;

  protected Questions() {
    reloadMatchQuestions();
    reloadPitQuestions();
    reloadDriveTeamQuestions();
  }

  protected abstract List<Question.Page> matchQuestions();

  protected abstract List<Question.Page> pitQuestions();

  protected abstract List<Question> driveTeamQuestions();

  public void reloadMatchQuestions() {
    matchQuestions = matchQuestions();
    matchQuestionsJson = jsonEncode(matchQuestions);
    matchQuestionsHash = hash(matchQuestionsJson);
  }

  public void reloadPitQuestions() {
    pitQuestions = pitQuestions();
    pitQuestionsJson = jsonEncode(pitQuestions);
    pitQuestionsHash = hash(pitQuestionsJson);
  }

  public void reloadDriveTeamQuestions() {
    driveTeamQuestions = driveTeamQuestions();
    driveTeamQuestionsJson = jsonEncode(driveTeamQuestions);
    driveTeamQuestionsHash = hash(driveTeamQuestionsJson);
  }

  public List<Question.Page> getMatchQuestions() {
    if (matchQuestions == null) {
      reloadMatchQuestions();
    }
    return matchQuestions;
  }

  public List<Question.Page> getPitQuestions() {
    if (pitQuestions == null) {
      reloadPitQuestions();
    }
    return pitQuestions;
  }

  public List<Question> getDriveTeamQuestions() {
    if (driveTeamQuestions == null) {
      reloadDriveTeamQuestions();
    }
    return driveTeamQuestions;
  }

  public String getMatchQuestionsJson() {
    if (matchQuestionsJson == null) {
      reloadMatchQuestions();
    }
    return matchQuestionsJson;
  }

  public String getPitQuestionsJson() {
    if (pitQuestionsJson == null) {
      reloadPitQuestions();
    }
    return pitQuestionsJson;
  }

  public String getDriveTeamQuestionsJson() {
    if (driveTeamQuestionsJson == null) {
      reloadDriveTeamQuestions();
    }
    return driveTeamQuestionsJson;
  }

  public String getMatchQuestionsHash() {
    if (matchQuestionsHash == null) {
      reloadMatchQuestions();
    }
    return matchQuestionsHash;
  }

  public String getPitQuestionsHash() {
    if (pitQuestionsHash == null) {
      reloadPitQuestions();
    }
    return pitQuestionsHash;
  }

  public String getDriveTeamQuestionsHash() {
    if (driveTeamQuestionsHash == null) {
      reloadDriveTeamQuestions();
    }
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
}
