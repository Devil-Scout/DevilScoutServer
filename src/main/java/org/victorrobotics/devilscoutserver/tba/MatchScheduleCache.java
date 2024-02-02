package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MatchScheduleCache<S extends ScoreBreakdown>
    extends BlueAllianceCache<String, List<Match>, MatchSchedule<S>> {
  private final BiFunction<Match.ScoreBreakdown, Boolean, S> statsFunction;
  private final Function<Collection<S>, S>                   statsMergeFunction;

  public MatchScheduleCache(BiFunction<Match.ScoreBreakdown, Boolean, S> statsFunction,
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
