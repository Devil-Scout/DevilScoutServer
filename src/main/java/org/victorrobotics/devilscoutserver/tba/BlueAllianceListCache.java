package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.CacheValue;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public abstract class BlueAllianceListCache<K, D, V extends Cacheable<D>>
    implements Cache<K, D, V> {
  private final ConcurrentMap<K, CacheValue<D, V>> cache;
  private final List<Endpoint<List<D>>>            endpoints;

  private volatile long timestamp;

  protected BlueAllianceListCache(List<Endpoint<List<D>>> endpointList) {
    cache = new ConcurrentHashMap<>();
    endpoints = List.copyOf(endpointList);
  }

  protected abstract V createValue(K key);

  protected abstract K getKey(D data);

  @Override
  public CacheValue<D, V> get(K key) {
    return cache.get(key);
  }

  @Override
  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  @Override
  @SuppressWarnings("java:S1941") // move start further down
  public void refresh() {
    List<K> keys = new ArrayList<>();
    boolean mods = endpoints.parallelStream()
                            .map(Endpoint::refresh)
                            .flatMap(List::stream)
                            .map(data -> {
                              K key = getKey(data);
                              keys.add(key);
                              return cache.computeIfAbsent(key,
                                                           k -> new CacheValue<>(createValue(k)))
                                          .refresh(data);
                            })
                            .sequential()
                            .reduce(false, Boolean::logicalOr);
    boolean removals = cache.keySet()
                            .retainAll(keys);
    if (mods || removals) {
      timestamp = System.currentTimeMillis();
    }
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public long timestamp() {
    return timestamp;
  }

  public Stream<CacheValue<D, V>> values() {
    return cache.values()
                .stream()
                .sorted();
  }
}
