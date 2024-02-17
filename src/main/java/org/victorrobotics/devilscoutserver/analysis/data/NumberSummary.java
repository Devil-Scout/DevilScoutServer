package org.victorrobotics.devilscoutserver.analysis.data;

public record NumberSummary(int count,
                            Double min,
                            Double max,
                            Double mean,
                            Double stddev) {
  public static final NumberSummary NO_DATA = new NumberSummary(0, null, null, null, null);
}
