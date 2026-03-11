package org.victorrobotics.devilscoutserver.years._2024;

public final class CrescendoEnums {
  private CrescendoEnums() {}

  public enum AutoActions {
    SCORE_SPEAKER("Score note in speaker"),
    SCORE_AMP("Score note in amp"),
    FAIL_SCORE("Fail to score note"),
    PICKUP_NOTE("Pickup note from floor");

    static final AutoActions[] VALUES = values();

    final String value;

    AutoActions(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static AutoActions of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum DrivetrainType {
    SWERVE("Swerve"),
    TANK("Tank"),
    MECANUM("Mecanum"),
    OTHER("Other");

    static final DrivetrainType[] VALUES = values();

    final String value;

    DrivetrainType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static DrivetrainType of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum StartPosition {
    NEXT_TO_AMP("Next to amp"),
    FRONT_OF_SPEAKER("In front of speaker"),
    NEXT_TO_SPEAKER("Center, side of speaker"),
    NEXT_TO_SOURCE("Next to source");

    static final StartPosition[] VALUES = values();

    final String value;

    StartPosition(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static StartPosition of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum ScoreLocation {
    AMP("Amp"),
    SPEAKER("Speaker");

    static final ScoreLocation[] VALUES = values();

    final String value;

    ScoreLocation(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static ScoreLocation of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum PickupLocation {
    SOURCE("Source"),
    GROUND("Ground");

    static final PickupLocation[] VALUES = values();

    final String value;

    PickupLocation(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static PickupLocation of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum FinalStatus {
    NONE("None"),
    PARK("Parked"),
    ONSTAGE("Onstage"),
    HARMONY("Harmony");

    static final FinalStatus[] VALUES = values();

    final String value;

    FinalStatus(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static FinalStatus of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }
}
