package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.ListCache;
import org.victorrobotics.devilscoutserver.tba.EventOprs.EventTeamOprs;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TeamOprsCache extends ListCache<Integer, Map<String, EventTeamOprs>, TeamOprs> {
  private static final Collector<Entry<Integer, EventTeamOprs>, ?, Map<Integer, Map<String, EventTeamOprs>>> TO_MAP =
      Collectors.toMap(Map.Entry::getKey, entry -> Map.of(entry.getValue()
                                                               .getEventKey(),
                                                          entry.getValue()),
                       (m1, m2) -> {
                         Map<String, EventTeamOprs> map =
                             LinkedHashMap.newLinkedHashMap(m1.size() + m2.size());
                         map.putAll(m1);
                         map.putAll(m2);
                         return map;
                       });

  private final Cache<?, ?, EventOprs> source;

  public TeamOprsCache(Cache<?, ?, EventOprs> source) {
    super(true);
    this.source = source;
  }

  @Override
  protected Map<Integer, Map<String, EventTeamOprs>> getData() {
    return source.values()
                 .stream()
                 .map(Value::value)
                 .map(EventOprs::entrySet)
                 .flatMap(Collection::stream)
                 .collect(TO_MAP);
  }

  @Override
  protected TeamOprs createValue(Integer key, Map<String, EventTeamOprs> data) {
    return new TeamOprs(data);
  }
}
