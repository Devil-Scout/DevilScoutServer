package org.victorrobotics.devilscoutserver.cache;

import org.victorrobotics.bluealliance.Endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public abstract class ListCache<K, D, V extends Cacheable<D>> implements Cache<K, D, V> {
  private final ConcurrentMap<K, CacheValue<D, V>> cache;
  private final List<Endpoint<List<D>>>            endpoints;

  private volatile long timestamp;

  protected ListCache(List<Endpoint<List<D>>> endpointList) {
    endpoints = List.copyOf(endpointList);
    cache = new ConcurrentHashMap<>();
    refresh();
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
  public Stream<V> values() {
    return cache.values()
                .stream()
                .map(CacheValue::value);
  }

  @Override
  public void refresh() {
    List<K> keys = new ArrayList<>();
    boolean mods = endpoints.parallelStream()
                            .map(Endpoint::request)
                            .map(CompletableFuture::join)
                            .flatMap(List::stream)
                            .map(data -> {
                              K key = getKey(data);
                              keys.add(key);
                              return cache.computeIfAbsent(key,
                                                           k -> new CacheValue<>(createValue(k)))
                                          .update(data);
                            })
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
}