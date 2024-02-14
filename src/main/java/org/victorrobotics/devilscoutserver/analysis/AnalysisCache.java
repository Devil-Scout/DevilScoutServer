package org.victorrobotics.devilscoutserver.analysis;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisCache {
  private static record DelayedRefresh(String eventKey,
                                       Integer team,
                                       long delayEnd)
      implements Delayed {

    private static final long DELAY = TimeUnit.SECONDS.toMillis(5);

    DelayedRefresh(String eventKey, int team) {
      this(eventKey, team, System.currentTimeMillis() + DELAY);
    }

    @Override
    public long getDelay(TimeUnit unit) {
      return unit.convert(delayEnd - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
      return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisCache.class);

  private final Map<Integer, Analyzer<?>>     analyzers;
  private final BlockingQueue<DelayedRefresh> refreshQueue;

  private final ConcurrentMap<String, AnalysisData>               individualData;
  private final ConcurrentMap<String, Map<Integer, AnalysisData>> eventData;
  private final ConcurrentMap<Integer, Map<String, AnalysisData>> teamData;

  public AnalysisCache(Map<Integer, Analyzer<?>> analyzers) {
    this.analyzers = analyzers;
    this.refreshQueue = new DelayQueue<>();

    individualData = new ConcurrentHashMap<>(0);
    eventData = new ConcurrentHashMap<>(0);
    teamData = new ConcurrentHashMap<>(0);
  }

  public void scheduleRefresh(String eventKey, int team) {
    boolean removal = refreshQueue.removeIf(q -> q.eventKey.equals(eventKey) && q.team == team);
    refreshQueue.add(new DelayedRefresh(eventKey, team));
    if (!removal) {
      LOGGER.info("Scheduled refresh for {} at {}", team, eventKey);
    }
  }

  @SuppressWarnings("java:S2189") // intentional infinite loop
  public void refreshLoop() {
    while (true) {
      DelayedRefresh refresh;
      try {
        refresh = refreshQueue.take();
      } catch (InterruptedException e) {
        continue;
      }

      refresh(refresh.eventKey(), refresh.team());
    }
  }

  private void refresh(String eventKey, int team) {
    try {
      long start = System.currentTimeMillis();
      Analyzer<?> analyzer = analyzers.get(extractYear(eventKey));
      AnalysisData data = analyzer.computeData(eventKey, team);
      if (data == null) {
        individualData.remove(team + "@" + eventKey);
        Optional.ofNullable(eventData.get(eventKey))
                .ifPresent(m -> m.remove(team));
      } else {
        individualData.put(team + "@" + eventKey, data);
        eventData.computeIfAbsent(eventKey, x -> new ConcurrentHashMap<>(0))
                 .put(team, data);
      }
      LOGGER.info("Refreshed {} at {} in {}ms", team, eventKey, System.currentTimeMillis() - start);
    } catch (Exception e) {
      LOGGER.warn("Exception while refreshing {} at {}:", team, eventKey, e);
    }
  }

  public AnalysisData get(String eventKey, int team) {
    return individualData.get(team + "@" + eventKey);
  }

  public Map<Integer, AnalysisData> getEvent(String eventKey) {
    return unmodifiable(eventData.get(eventKey));
  }

  public Map<String, AnalysisData> getTeam(int team) {
    return unmodifiable(teamData.get(team));
  }

  private static int extractYear(String eventKey) {
    return Integer.parseInt(eventKey.substring(0, 4));
  }

  private static <K, V> Map<K, V> unmodifiable(Map<K, V> source) {
    return source == null ? null : Collections.unmodifiableMap(source);
  }
}
