package org.victorrobotics.devilscoutserver.cache;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.devilscoutserver.data.MatchSchedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MatchScheduleCache {
  private final Map<String, Entry> events;

  public MatchScheduleCache() {
    events = new HashMap<>();
  }

  public MatchSchedule get(String eventKey) {
    return events.computeIfAbsent(eventKey, Entry::new)
                 .schedule();
  }

  public void update() {
    events.values()
          .stream()
          .map(Entry::update)
          .reduce(CompletableFuture.completedFuture(null),
                  (f1, f2) -> f1.thenCombine(f2, (v1, v2) -> null))
          .join();
  }

  static record Entry(Endpoint<List<Match.Simple>> apiEndpoint,
                      MatchSchedule schedule) {
    Entry(String eventKey) {
      this(Match.Simple.endpointForEvent(eventKey), new MatchSchedule(eventKey));
      update().join();
    }

    CompletableFuture<Void> update() {
      return apiEndpoint.request()
                        .thenAccept(schedule::update);
    }
  }
}
