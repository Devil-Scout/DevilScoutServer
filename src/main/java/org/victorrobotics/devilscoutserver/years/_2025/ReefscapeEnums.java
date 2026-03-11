package org.victorrobotics.devilscoutserver.years._2025;

public class ReefscapeEnums {
  private ReefscapeEnums() {}

  public enum CoralLevel {
    L1("L1"),
    L2("L2"),
    L3("L3"),
    L4("L4");

    static final CoralLevel[] VALUES = values();

    final String value;

    CoralLevel(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static CoralLevel of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum AlgaePickup {
    REEF("Reef"),
    GROUND("Ground");

    static final AlgaePickup[] VALUES = values();

    final String value;

    AlgaePickup(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static AlgaePickup of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum GamePiece {
    CORAL("Coral"),
    ALGAE("Algae");

    static final GamePiece[] VALUES = values();

    final String value;

    GamePiece(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static GamePiece of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum CoralPickup {
    STATION("Coral Station"),
    GROUND("Ground");

    static final CoralPickup[] VALUES = values();

    final String value;

    CoralPickup(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static CoralPickup of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum AlgaeScore {
    PROCESSOR("Processor"),
    BARGE_NET("Barge Net");

    static final AlgaeScore[] VALUES = values();

    final String value;

    AlgaeScore(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static AlgaeScore of(Integer index) {
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
    INSIDE("Inside (center of field)"),
    MIDDLE("Middle (in between)"),
    OUTSIDE("Outside (field boundary)");

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

  public enum AutoAction {
    LEAVE("Leave the Barge"),
    REMOVE_ALGAE("Remove Algae"),
    SCORE_CORAL("Score Coral on Reef"),
    SCORE_ALGAE_PROCESSOR("Score Algae in Processor"),
    SCORE_ALGAE_NET("Score Algae in Net");

    static final AutoAction[] VALUES = values();

    final String value;

    AutoAction(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static AutoAction of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum FinalStatus {
    NONE("None"),
    PARK("Parked"),
    CLIMB_FAIL("Failed Climb"),
    CLIMB_SHALLOW("Shallow Climb"),
    CLIMB_DEEP("Deep Climb");

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

  public enum FinalStatus2 {
    NONE("None"),
    PARK("Parked"),
    CLIMB_FAIL("Failed Climb"),
    CLIMB_SHALLOW("Shallow Climb"),
    CLIMB_DEEP("Deep Climb");

    static final FinalStatus2[] VALUES = values();

    final String value;

    FinalStatus2(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static FinalStatus2 of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum CageHeight {
    SHALLOW("Shallow"),
    DEEP("Deep");

    static final CageHeight[] VALUES = values();

    final String value;

    CageHeight(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static CageHeight of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum Fouls {
    MINOR("Minor Foul"),
    MAJOR("Major Foul"),
    YELLOW("Yellow Card"),
    RED("Red Card");

    static final Fouls[] VALUES = values();

    final String value;

    Fouls(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static Fouls of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum Disabled {
    NONE("Never Disabled"),
    FALL("Fell Over"),
    MECHANICAL("Mechanical Problem"),
    PENALTY("Egregious Behavior"),
    OTHER("Other reason");

    static final Disabled[] VALUES = values();

    final String value;

    Disabled(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static Disabled of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }
}
