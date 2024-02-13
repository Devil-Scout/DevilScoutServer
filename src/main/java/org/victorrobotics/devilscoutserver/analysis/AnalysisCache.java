package org.victorrobotics.devilscoutserver.analysis;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class AnalysisCache {
  private static record DelayedRefresh(String eventKey,
                                       int team,
                                       long delayEnd)
      implements Delayed {

    private static final long DELAY = TimeUnit.SECONDS.toMillis(30);

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

  private final Map<Integer, Analyzer<?>>     analyzers;
  private final BlockingQueue<DelayedRefresh> refreshQueue;

  private final ConcurrentMap<String, AnalysisData>               individualData;
  private final ConcurrentMap<String, Map<Integer, AnalysisData>> eventData;
  private final ConcurrentMap<Integer, Map<String, AnalysisData>> teamData;

  public AnalysisCache(Map<Integer, Analyzer<?>> analyzers) {
    this.analyzers = Map.copyOf(analyzers);
    this.refreshQueue = new DelayQueue<>();

    individualData = new ConcurrentHashMap<>(0);
    eventData = new ConcurrentHashMap<>(0);
    teamData = new ConcurrentHashMap<>(0);
  }

  public void scheduleRefresh(String eventKey, int team) {
    refreshQueue.removeIf(q -> q.eventKey.equals(eventKey) && q.team == team);
    refreshQueue.add(new DelayedRefresh(eventKey, team));
  }

  @SuppressWarnings("java:S2189") // intentional infinite loop
  public void refreshLoop() {
    while (true) {
      try {
        DelayedRefresh refresh = refreshQueue.take();
        refresh(refresh.eventKey(), refresh.team());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private void refresh(String eventKey, int team) {
    Analyzer<?> analyzer = analyzers.get(extractYear(eventKey));
    AnalysisData data = analyzer.computeData(eventKey, team);
    individualData.put(team + "@" + eventKey, data);
    eventData.computeIfAbsent(eventKey, x -> new ConcurrentHashMap<>(0))
             .put(team, data);
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
