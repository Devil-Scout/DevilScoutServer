package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MatchScheduleCache<S extends AllianceStatistics>
    extends BlueAllianceCache<String, List<Match>, MatchSchedule<S>> {
  private final Function<Match.ScoreBreakdown, S> statsFunction;
  private final Function<Collection<S>, S>        statsMergeFunction;

  public MatchScheduleCache(Function<Match.ScoreBreakdown, S> statsFunction,
                            Function<Collection<S>, S> statsMergeFunction) {
    super(TimeUnit.HOURS.toMillis(8));
    this.statsFunction = statsFunction;
    this.statsMergeFunction = statsMergeFunction;
  }

  @Override
  protected Endpoint<List<Match>> getEndpoint(String eventKey) {
    return Match.endpointForEvent(eventKey);
  }

  @Override
  protected MatchSchedule<S> createValue(String key, List<Match> data) {
    return new MatchSchedule<>(statsFunction, statsMergeFunction, data);
  }
}
