package org.victorrobotics.devilscoutserver.tba.data;

import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.devilscoutserver.tba.cache.Cacheable;

import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class EventInfo implements Cacheable<Event.Simple>, Comparable<EventInfo> {
  private final String eventKey;

  private String     name;
  private String     location;
  private Event.Date start;
  private Event.Date end;

  public EventInfo(String eventKey) {
    this.eventKey = eventKey;
  }

  @Override
  public boolean update(Event.Simple event) {
    if (!Objects.equals(eventKey, event.key)) {
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

  @OpenApiExample("2023nyrr")
  @OpenApiRequired
  public String getEventKey() {
    return eventKey;
  }

  @OpenApiExample("Rah Cha Cha Ruckus")
  @OpenApiRequired
  public String getName() {
    return name;
  }

  @OpenApiExample("Rochester, NY, USA")
  @OpenApiRequired
  public String getLocation() {
    return location;
  }

  @OpenApiExample("2023-10-21")
  @OpenApiRequired
  public String getStart() {
    return start.toString();
  }

  @OpenApiExample("2023-10-21")
  @OpenApiRequired
  public String getEnd() {
    return end.toString();
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

    return eventKey.compareTo(other.eventKey);
  }
}
