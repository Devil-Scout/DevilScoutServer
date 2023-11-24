package org.victorrobotics.devilscoutserver.cache;

import org.victorrobotics.bluealliance.Endpoint;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public abstract class DependentKeyCache<K, K2, D, I extends Cacheable<D>, V extends Cacheable<Collection<I>>>
    implements Cache<K, Collection<I>, V> {
  private final ConcurrentMap<K, CacheValue<Collection<I>, V>> cache;

  private final Cache<K2, D, I> source;

  private volatile long timestamp;

  protected DependentKeyCache(Cache<K2, D, I> source) {
    cache = new ConcurrentHashMap<>();
    this.source = source;
  }

  protected abstract Endpoint<List<K2>> getEndpoint(K key);

  protected abstract V createValue(K key);

  @Override
  public CacheValue<Collection<I>, V> get(K key) {
    return cache.computeIfAbsent(key, k -> {
      CacheValue<Collection<I>, V> entry = new CacheValue<>(createValue(k));
      entry.update(getData(k));
      timestamp = System.currentTimeMillis();
      return entry;
    });
  }

  @Override
  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public Stream<V> values() {
    return cache.values()
                .stream()
                .map(CacheValue::value);
  }

  @Override
  public void refresh() {
    boolean change = cache.entrySet()
                          .parallelStream()
                          .map(entry -> entry.getValue()
                                             .update(getData(entry.getKey())))
                          .reduce(false, Boolean::logicalOr);
    if (change) {
      timestamp = System.currentTimeMillis();
    }
  }

  @Override
  public long timestamp() {
    return timestamp;
  }

  private List<I> getData(K key) {
    return getEndpoint(key).request()
                           .join()
                           .stream()
                           .map(source::get)
                           .map(CacheValue::value)
                           .toList();
  }
}
