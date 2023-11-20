package org.victorrobotics.devilscoutserver.cache;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.bluealliance.Team;
import org.victorrobotics.devilscoutserver.data.EventInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EventInfoCache {
  private final Map<String, Entry> events;

  public EventInfoCache() {
    events = new HashMap<>();
  }

  public EventInfo get(String eventKey) {
    return events.computeIfAbsent(eventKey, Entry::new)
                 .value();
  }

  public CompletableFuture<Void> update() {
    return events.values()
                 .stream()
                 .map(Entry::update)
                 .reduce(CompletableFuture.completedFuture(null),
                         (f1, f2) -> f1.thenCombine(f2, (v1, v2) -> null));
  }

  static record Entry(Endpoint<List<Match.Simple>> scheduleEndpoint,
                      Endpoint<List<Team.Simple>> teamsEndpoint,
                      EventInfo value) {
    Entry(String eventKey) {
      this(Match.Simple.endpointForEvent(eventKey), Team.Simple.endpointForEvent(eventKey),
           new EventInfo(eventKey));
      update().join();
    }

    CompletableFuture<Void> update() {
      return scheduleEndpoint.request()
                             .thenAcceptBoth(teamsEndpoint.request(), value::update);
    }
  }
}
