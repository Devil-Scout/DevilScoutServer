package org.victorrobotics.devilscoutserver.data;

import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class EventInfo implements Cacheable<Event.Simple>, Comparable<EventInfo> {
  private final String eventKey;

  private String name;
  private String location;
  private String start;
  private String end;

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

    String eventStart = event.startDate.toString();
    if (!Objects.equals(start, eventStart)) {
      start = eventStart;
      changed = true;
    }

    String eventEnd = event.endDate.toString();
    if (!Objects.equals(end, eventEnd)) {
      end = eventEnd;
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
    return start;
  }

  @OpenApiExample("2023-10-21")
  @OpenApiRequired
  public String getEnd() {
    return end;
  }

  @Override
  public int compareTo(EventInfo o) {
    return getStart().compareTo(o.getStart());
  }
}
