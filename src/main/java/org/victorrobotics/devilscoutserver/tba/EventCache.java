package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event.Date;
import org.victorrobotics.bluealliance.Event.Simple;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.Objects;
import java.util.stream.IntStream;

public class EventCache extends BlueAllianceListCache<String, Simple, EventCache.Event> {
  public static class Event implements Cacheable<Simple>, Comparable<Event> {
    private final String key;

    private String name;
    private String location;
    private Date   start;
    private Date   end;

    public Event(String eventKey, Simple event) {
      this.key = eventKey;
      update(event);
    }

    @Override
    public boolean update(Simple event) {
      if (!Objects.equals(key, event.key())) {
        throw new IllegalArgumentException();
      }

      boolean changed = false;

      if (!Objects.equals(name, event.name())) {
        name = event.name();
        changed = true;
      }

      String eventLocation = event.city() + ", " + event.stateProv() + ", " + event.country();
      if (!Objects.equals(location, eventLocation)) {
        location = eventLocation;
        changed = true;
      }

      if (!Objects.equals(start, event.startDate())) {
        start = event.startDate();
        changed = true;
      }

      if (!Objects.equals(end, event.endDate())) {
        end = event.endDate();
        changed = true;
      }

      return changed;
    }

    public String getKey() {
      return key;
    }

    public String getName() {
      return name;
    }

    public String getLocation() {
      return location;
    }

    public Date getStart() {
      return start;
    }

    public Date getEnd() {
      return end;
    }

    @Override
    public int compareTo(Event other) {
      int compare = start.compareTo(other.start);
      if (compare != 0) return compare;

      compare = end.compareTo(other.end);
      if (compare != 0) return compare;

      compare = name.compareTo(other.name);
      if (compare != 0) return compare;

      compare = key.compareTo(other.key);
      if (compare != 0) return compare;

      return location.compareTo(other.location);
    }
  }

  public EventCache() {
    super(IntStream.rangeClosed(2024, 2024)
                   .mapToObj(Simple::endpointForYear)
                   .toList());
  }

  @Override
  protected Event createValue(String key, Simple data) {
    return new Event(key, data);
  }

  @Override
  protected String getKey(Simple data) {
    return data.key();
  }
}
