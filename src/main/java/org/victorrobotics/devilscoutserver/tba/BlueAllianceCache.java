package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.CacheValue;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class BlueAllianceCache<K, D, V extends Cacheable<D>> implements Cache<K, D, V> {
  private final ConcurrentMap<K, CacheValue<D, V>> cache;

  private final long purgeTime;

  private volatile long timestamp;

  protected BlueAllianceCache(long purgeTime) {
    cache = new ConcurrentHashMap<>();
    this.purgeTime = purgeTime;
  }

  protected abstract Endpoint<D> getEndpoint(K key);

  protected abstract V createValue(K key);

  @Override
  public CacheValue<D, V> get(K key) {
    return cache.computeIfAbsent(key, k -> {
      CacheValue<D, V> entry = new CacheValue<>(createValue(k));
      entry.update(getEndpoint(k).refresh());
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
                        .map(entry -> getEndpoint(entry.getKey()).refreshAsync()
                                                                 .thenApply(entry.getValue()::update))
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
