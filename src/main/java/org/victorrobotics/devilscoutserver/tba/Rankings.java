package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.cache.ListValue;

import java.util.List;

class Rankings extends ListValue<Integer, Event.Rankings, Event.Rankings.Team, Rankings.Team> {
  public static class Team implements Cacheable<Event.Rankings.Team> {
    @Override
    public boolean update(Event.Rankings.Team data) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'update'");
    }
  }

  protected Rankings(Event.Rankings data) {
    super(null, data);
  }

  @Override
  protected Team createValue(Integer key, Event.Rankings.Team data) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'createValue'");
  }

  @Override
  protected Integer getKey(Event.Rankings.Team data) {
    return Integer.parseInt(data.teamKey.substring(3));
  }

  @Override
  protected List<Event.Rankings.Team> getList(Event.Rankings data) {
    return data.rankings;
  }
}
