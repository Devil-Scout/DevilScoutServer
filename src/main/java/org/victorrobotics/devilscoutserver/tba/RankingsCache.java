package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.bluealliance.Event.WinLossRecord;
import org.victorrobotics.devilscoutserver.analysis.AnalysisCache;
import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.cache.ListValue;

import java.util.List;
import java.util.Objects;

public class RankingsCache
    extends BlueAllianceCache<String, Event.Rankings, RankingsCache.Rankings> {
  public class Team implements Cacheable<Event.Rankings.Team> {
    private final int    number;
    private final String eventKey;

    private WinLossRecord winLossRecord;
    private int           rank;

    public Team(String eventKey, Event.Rankings.Team data) {
      this.eventKey = eventKey;
      this.number = Integer.parseInt(data.teamKey()
                                         .substring(3));
      update(data);
    }

    @Override
    public boolean update(Event.Rankings.Team data) {
      boolean change = false;

      if (!Objects.equals(winLossRecord, data.winLossRecord())) {
        winLossRecord = data.winLossRecord();
        change = true;
      }

      if (rank != data.rank()) {
        rank = data.rank();
        change = true;
      }

      if (change) {
        analysis.scheduleRefresh(eventKey, number);
      }

      return change;
    }

    public int getNumber() {
      return number;
    }

    public String getEventKey() {
      return eventKey;
    }

    public WinLossRecord getWinLossRecord() {
      return winLossRecord;
    }

    public int getRank() {
      return rank;
    }
  }

  public class Rankings extends ListValue<Integer, Event.Rankings, Event.Rankings.Team, Team> {
    private final String eventKey;

    protected Rankings(String eventKey, Event.Rankings data) {
      super(null);
      this.eventKey = eventKey;
      update(data);
    }

    @Override
    protected Team createValue(Integer key, Event.Rankings.Team data) {
      return new Team(eventKey, data);
    }

    @Override
    protected Integer getKey(Event.Rankings.Team data) {
      return Integer.parseInt(data.teamKey()
                                  .substring(3));
    }

    @Override
    protected List<Event.Rankings.Team> getList(Event.Rankings data) {
      return data.rankings();
    }
  }

  private final AnalysisCache analysis;

  public RankingsCache(AnalysisCache analysis) {
    this.analysis = analysis;
  }

  @Override
  protected Endpoint<Event.Rankings> getEndpoint(String key) {
    return Event.Rankings.endpointForEvent(key);
  }

  @Override
  protected Rankings createValue(String key, Event.Rankings data) {
    return new Rankings(key, data);
  }
}
