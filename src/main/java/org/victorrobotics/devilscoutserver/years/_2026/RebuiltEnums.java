package org.victorrobotics.devilscoutserver.years._2026;

public final class RebuiltEnums {
  private RebuiltEnums() {}

  public enum StartPosition {
    LEFT_TRENCH("Left Trench (near Depot)"),
    LEFT_BUMP("Left Bump (near Depot)"),
    HUB("Hub (near Tower)"),
    RIGHT_BUMP("Right Bump (near Outpost)"),
    RIGHT_TRENCH("Right Trench (near Outpost)");

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

  public enum TraversePath {
    TRENCH("Under the Trench"),
    BUMP("Over the Bump");

    static final TraversePath[] VALUES = values();

    final String value;

    TraversePath(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static TraversePath of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum ShooterAbility {
    SHOOT("Shoot into the Hub"),
    FERRY("Ferry Fuel from the middle"),
    AUTO_AIM("Automatically aim"),
    MOVING("Shoot (accurately) while moving");

    static final ShooterAbility[] VALUES = values();

    final String value;

    ShooterAbility(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static ShooterAbility of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum AutoAction {
    SHOOT("Shoot into Hub"),
    PICKUP_OUTPOST("Receive fuel from Outpost"),
    PICKUP_DEPOT("Pickup from Depot"),
    PICKUP_CENTER("Pickup from midfield"),
    FERRY("Ferry from midfield"),
    CLIMB("Climb L1");

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

  public enum FuelRate {
    SLOW("Slow (1-3/sec)"),
    MEDIUM("Medium (4-6/sec)"),
    FAST("Fast (7-9/sec)"),
    RAPID("Rapid (10+/sec)"),
    NONE("None");

    static final FuelRate[] VALUES = values();

    final String value;

    FuelRate(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static FuelRate of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum ShootingAccuracy {
    POOR("Poor (<40%)"),
    OKAY("Okay (40+%)"),
    GOOD("Good (60+%)"),
    GREAT("Great (80+%)"),
    AWESOME("Awesome (90+%)"),
    PERFECT("Perfect (100%)"),
    NONE("None");

    static final ShootingAccuracy[] VALUES = values();

    final String value;

    ShootingAccuracy(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static ShootingAccuracy of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum FuelPickup {
    GROUND("Ground"),
    DEPOT("Depot"),
    OUTPOST("Outpost");

    static final FuelPickup[] VALUES = values();

    final String value;

    FuelPickup(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static FuelPickup of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum ClimbStatus {
    NONE("No Attempt"),
    FAILURE("Failed Climb"),
    CLIMB_L1("L1 Climb"),
    CLIMB_L2("L2 Climb"),
    CLIMB_L3("L3 Climb");

    static final ClimbStatus[] VALUES = values();

    final String value;

    ClimbStatus(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static ClimbStatus of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum TowerRung {
    CLIMB_L1("L1 Climb"),
    CLIMB_L2("L2 Climb"),
    CLIMB_L3("L3 Climb");

    static final TowerRung[] VALUES = values();

    final String value;

    TowerRung(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static TowerRung of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum ShooterType {
    TURRET("Turret"),
    FIXED("Fixed"),
    DRUM("Drum");

    static final ShooterType[] VALUES = values();

    final String value;

    ShooterType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static ShooterType of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum DrivetrainType {
    SWERVE_SDS("Swerve (SDS)"),
    SWERVE_WCP("Swerve (WCP)"),
    SWERVE_REV("Swerve (Rev)"),
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

  public enum FoulType {
    MINOR("Minor Foul"),
    MAJOR("Major Foul"),
    YELLOW("Yellow Card"),
    RED("Red Card");

    static final FoulType[] VALUES = values();

    final String value;

    FoulType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static FoulType of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }

  public enum DisabledReason {
    FALL("Fell Over"),
    MECHANICAL("Mechanical Problem"),
    PENALTY("Egregious Behavior"),
    OTHER("Other reason");

    static final DisabledReason[] VALUES = values();

    final String value;

    DisabledReason(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    static DisabledReason of(Integer index) {
      return index == null ? null : VALUES[index];
    }
  }
}
