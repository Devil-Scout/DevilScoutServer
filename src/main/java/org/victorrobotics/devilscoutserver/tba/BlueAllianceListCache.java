package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.cache.ListCache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BlueAllianceListCache<K, D, V extends Cacheable<D>>
    extends ListCache<K, D, V> {
  private final List<Endpoint<List<D>>> endpoints;

  protected BlueAllianceListCache(List<Endpoint<List<D>>> endpointList) {
    super(true);
    endpoints = List.copyOf(endpointList);
  }

  protected abstract K getKey(D data);

  @Override
  protected Map<K, D> getData() {
    List<D> dataList = endpoints.stream()
                                .map(Endpoint::refresh)
                                .flatMap(Collection::stream)
                                .toList();
    Map<K, D> map = new LinkedHashMap<>();
    for (D data : dataList) {
      map.put(getKey(data), data);
    }
    return map;
  }
}
