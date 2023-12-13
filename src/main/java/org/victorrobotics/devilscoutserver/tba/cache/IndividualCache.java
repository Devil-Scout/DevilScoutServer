package org.victorrobotics.devilscoutserver.tba.cache;

import org.victorrobotics.bluealliance.Endpoint;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class IndividualCache<K, D, V extends Cacheable<D>> implements Cache<K, D, V> {
  private final ConcurrentMap<K, CacheValue<D, V>> cache;

  private final long purgeTime;

  private volatile long timestamp;

  protected IndividualCache(long purgeTime) {
    cache = new ConcurrentHashMap<>();
    this.purgeTime = purgeTime;
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
  public void refresh() {
    boolean mods = cache.entrySet()
                        .parallelStream()
                        .map(entry -> entry.getValue()
                                           .startRefresh(getEndpoint(entry.getKey())))
                        .map(CompletableFuture::join)
                        .sequential()
                        .reduce(false, Boolean::logicalOr);
    long time = System.currentTimeMillis();
    boolean removals = cache.values()
                            .removeIf(value -> time - value.lastAccess() > purgeTime);
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
