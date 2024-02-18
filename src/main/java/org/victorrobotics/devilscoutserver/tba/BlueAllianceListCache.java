package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class BlueAllianceListCache<K, D, V extends Cacheable<D>> extends Cache<K, D, V> {
  private final List<Endpoint<List<D>>> endpoints;

  protected BlueAllianceListCache(List<Endpoint<List<D>>> endpointList) {
    endpoints = List.copyOf(endpointList);
  }

  protected abstract K getKey(D data);

  protected abstract V createValue(K key, D data);

  public void refresh() {
    try {
      long start = System.currentTimeMillis();
      List<D> dataList = endpoints.stream()
                                  .map(Endpoint::refresh)
                                  .flatMap(Optional::stream)
                                  .flatMap(Collection::stream)
                                  .toList();

      List<K> keys = new ArrayList<>();
      for (D data : dataList) {
        K key = getKey(data);

        if (data == null) {
          remove(key);
          continue;
        }

        keys.add(key);
        Value<D, V> value = cacheMap.get(key);
        if (value == null) {
          cacheMap.put(key, new Value<>(createValue(key, data), this::modified));
          modified();
        } else {
          value.update(data);
        }
      }

      if (cacheMap.keySet()
                  .retainAll(keys)) {
        modified();
      }

      getLogger().info("Refreshed in {}ms", System.currentTimeMillis() - start);
    } catch (Exception e) {
      getLogger().warn("Error while refreshing", e);
    }
  }
}
