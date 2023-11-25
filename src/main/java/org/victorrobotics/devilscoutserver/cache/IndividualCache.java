package org.victorrobotics.devilscoutserver.cache;

import org.victorrobotics.bluealliance.Endpoint;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public abstract class IndividualCache<K, D, V extends Cacheable<D>> implements Cache<K, D, V> {
  private final ConcurrentMap<K, CacheValue<D, V>> cache;

  private volatile long timestamp;

  protected IndividualCache() {
    cache = new ConcurrentHashMap<>();
  }

  protected abstract Endpoint<D> getEndpoint(K key);

  protected abstract V createValue(K key);

  @Override
  public CacheValue<D, V> get(K key) {
    return cache.computeIfAbsent(key, k -> {
      CacheValue<D, V> entry = new CacheValue<>(createValue(k));
      entry.refresh(getEndpoint(k));
      timestamp = System.currentTimeMillis();
      return entry;
    });
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
    long start = System.currentTimeMillis();
    boolean change = cache.entrySet()
                          .parallelStream()
                          .map(entry -> entry.getValue()
                                             .startRefresh(getEndpoint(entry.getKey())))
                          .map(CompletableFuture::join)
                          .sequential()
                          .reduce(false, Boolean::logicalOr);
    if (change) {
      timestamp = System.currentTimeMillis();
    }
    System.out.printf("Refreshed %s (%d) in %dms%n", getClass().getSimpleName(), size(),
                      System.currentTimeMillis() - start);
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
