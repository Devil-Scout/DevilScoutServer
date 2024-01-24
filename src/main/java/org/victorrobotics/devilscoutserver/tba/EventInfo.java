package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.Objects;

public class EventInfo implements Cacheable<Event.Simple>, Comparable<EventInfo> {
  private final String key;

  private String     name;
  private String     location;
  private Event.Date start;
  private Event.Date end;

  public EventInfo(String eventKey, Event.Simple event) {
    this.key = eventKey;
    update(event);
  }

  @Override
  public boolean update(Event.Simple event) {
    if (!Objects.equals(key, event.key)) {
      throw new IllegalArgumentException();
    }

    boolean changed = false;

    if (!Objects.equals(name, event.name)) {
      name = event.name;
      changed = true;
    }

    String eventLocation = event.city + ", " + event.stateProv + ", " + event.country;
    if (!Objects.equals(location, eventLocation)) {
      location = eventLocation;
      changed = true;
    }

    if (!Objects.equals(start, event.startDate)) {
      start = event.startDate;
      changed = true;
    }

    if (!Objects.equals(end, event.endDate)) {
      end = event.endDate;
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

  public Event.Date getStart() {
    return start;
  }

  public Event.Date getEnd() {
    return end;
  }

  @Override
  public int compareTo(EventInfo other) {
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
