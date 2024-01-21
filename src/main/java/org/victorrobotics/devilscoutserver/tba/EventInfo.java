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

  public String getStart() {
    return String.format("%04d-%02d-%02d", start.year, start.month, start.day);
  }

  public String getEnd() {
    return String.format("%04d-%02d-%02d", end.year, end.month, end.day);
  }

  @Override
  public int compareTo(EventInfo other) {
    if (start.year != other.start.year) {
      return Integer.compare(start.year, other.start.year);
    }

    if (start.month != other.start.month) {
      return Integer.compare(start.month, other.start.month);
    }

    if (start.day != other.start.day) {
      return Integer.compare(start.day, other.start.day);
    }

    return key.compareTo(other.key);
  }
}
