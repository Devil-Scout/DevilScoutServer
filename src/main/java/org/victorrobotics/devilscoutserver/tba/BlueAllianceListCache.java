package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BlueAllianceListCache<K, D, V extends Cacheable<D>> extends Cache<K, D, V> {
  private final List<Endpoint<List<D>>> endpoints;

  protected BlueAllianceListCache(List<Endpoint<List<D>>> endpointList) {
    endpoints = List.copyOf(endpointList);
  }

  protected abstract K getKey(D data);

  protected abstract V createValue(K key, D data);

  @Override
  protected Value<D, V> getValue(K key) {
    return cacheMap.get(key);
  }

  @Override
  public void refresh() {
    Map<K, D> refreshData = getData();

    if (cacheMap.keySet()
                .retainAll(refreshData.keySet())) {
      modified();
    }

    for (Map.Entry<K, D> entry : refreshData.entrySet()) {
      K key = entry.getKey();
      D data = entry.getValue();

      if (data == null) {
        remove(key);
        continue;
      }

      Value<D, V> value = cacheMap.get(key);
      if (value == null) {
        cacheMap.put(key, new Value<>(createValue(key, data), this::modified));
        modified();
      } else {
        value.update(data);
      }
    }
  }

  protected Map<K, D> getData() {
    List<D> dataList = endpoints.stream()
                                .map(Endpoint::refresh)
                                .flatMap(Optional::stream)
                                .flatMap(Collection::stream)
                                .toList();
    Map<K, D> map = new LinkedHashMap<>();
    for (D data : dataList) {
      map.put(getKey(data), data);
    }
    return map;
  }
}
