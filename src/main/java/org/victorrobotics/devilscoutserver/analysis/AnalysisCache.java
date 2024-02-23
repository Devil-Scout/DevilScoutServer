package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisCache {
  private record DelayedRefresh(String eventKey,
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

  private final Map<Integer, Analyzer<?, ?>>  analyzers;
  private final BlockingQueue<DelayedRefresh> refreshQueue;

  private final ConcurrentMap<String, Object> individualData;

  private final ConcurrentMap<String, Map<Integer, List<StatisticsPage>>> eventTeamStatistics;

  public AnalysisCache(Map<Integer, Analyzer<?, ?>> analyzers) {
    this.analyzers = analyzers;
    this.refreshQueue = new DelayQueue<>();

    individualData = new ConcurrentHashMap<>();
    eventTeamStatistics = new ConcurrentHashMap<>();
  }

  public void scheduleRefresh(String eventKey, int team) {
    boolean removal = refreshQueue.removeIf(q -> q.eventKey.equals(eventKey) && q.team == team);
    refreshQueue.add(new DelayedRefresh(eventKey, team));
    if (!removal) {
      LOGGER.info("Scheduled refresh for team {} at event {}", team, eventKey);
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

  private <D> void refresh(String eventKey, int team) {
    try {
      long start = System.currentTimeMillis();
      @SuppressWarnings("unchecked") // SHOULD be safe
      Analyzer<?, D> analyzer = (Analyzer<?, D>) analyzers.get(extractYear(eventKey));
      D data = analyzer.computeData(eventKey, team);

      if (data == null) {
        individualData.remove(team + "@" + eventKey);

        Map<Integer, List<StatisticsPage>> teamStatistics = eventTeamStatistics.get(eventKey);
        if (teamStatistics != null) {
          teamStatistics.remove(team);
          if (teamStatistics.isEmpty()) {
            eventTeamStatistics.remove(eventKey);
          }
        }
      } else {
        individualData.put(team + "@" + eventKey, data);

        List<StatisticsPage> uiStats = analyzer.generateStatistics(data);
        eventTeamStatistics.computeIfAbsent(eventKey, x -> new ConcurrentSkipListMap<>())
                           .put(team, uiStats);
      }

      LOGGER.info("Refreshed team {} at event {} in {}ms", team, eventKey,
                  System.currentTimeMillis() - start);
    } catch (Exception e) {
      LOGGER.warn("Error while refreshing team {} at event {}", team, eventKey, e);
    }
  }

  public Object get(String eventKey, int team) {
    return individualData.get(team + "@" + eventKey);
  }

  public Map<Integer, List<StatisticsPage>> getStatistics(String eventKey) {
    return eventTeamStatistics.get(eventKey);
  }

  private static int extractYear(String eventKey) {
    return Integer.parseInt(eventKey.substring(0, 4));
  }
}
