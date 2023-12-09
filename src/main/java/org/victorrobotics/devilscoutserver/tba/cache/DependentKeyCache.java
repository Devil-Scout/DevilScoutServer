package org.victorrobotics.devilscoutserver.tba.cache;

import org.victorrobotics.bluealliance.Endpoint;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class DependentKeyCache<K, K2, D, D2 extends Cacheable<D>, V extends Cacheable<Collection<D2>>>
    implements Cache<K, Collection<D2>, V> {
  private final ConcurrentMap<K, CacheValue<Collection<D2>, V>> cache;

  private final Cache<K2, D, D2> source;
  private final long             purgeTime;

  private volatile long timestamp;

  protected DependentKeyCache(Cache<K2, D, D2> source, long purgeTime) {
    cache = new ConcurrentHashMap<>();
    this.source = source;
    this.purgeTime = purgeTime;
  }

  protected abstract Endpoint<List<K2>> getEndpoint(K key);

  protected abstract V createValue(K key);

  @Override
  public CacheValue<Collection<D2>, V> get(K key) {
    return cache.computeIfAbsent(key, k -> {
      CacheValue<Collection<D2>, V> entry = new CacheValue<>(createValue(k));
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
  public void refresh() {
    long start = System.currentTimeMillis();
    boolean mods = cache.entrySet()
                        .parallelStream()
                        .map(entry -> entry.getValue()
                                           .update(getData(entry.getKey())))
                        .sequential()
                        .reduce(false, Boolean::logicalOr);
    boolean removals = cache.values()
                            .removeIf(value -> start - value.lastAccess() > purgeTime);
    if (mods || removals) {
      timestamp = System.currentTimeMillis();
    }
    System.out.printf("Refreshed %s (%d) in %dms%n", getClass().getSimpleName(), size(),
                      System.currentTimeMillis() - start);
  }

  @Override
  public long timestamp() {
    return timestamp;
  }

  private List<D2> getData(K key) {
    return getEndpoint(key).refresh()
                           .stream()
                           .map(source::get)
                           .map(CacheValue::value)
                           .toList();
  }
}
