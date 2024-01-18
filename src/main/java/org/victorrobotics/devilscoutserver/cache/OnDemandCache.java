package org.victorrobotics.devilscoutserver.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class OnDemandCache<K, D, V extends Cacheable<D>> implements Cache<K, D, V> {
  private final ConcurrentMap<K, CacheValue<D, V>> cacheMap;

  private final long purgeTime;

  private volatile long lastModified;

  protected OnDemandCache(long purgeTime) {
    this.cacheMap = new ConcurrentHashMap<>();
    this.purgeTime = purgeTime;
  }

  protected abstract D getData(K key);

  protected abstract V createValue(K key, D data);

  @Override
  public int size() {
    return cacheMap.size();
  }

  @Override
  public long lastModified() {
    return lastModified;
  }

  @Override
  public CacheValue<D, V> get(K key) {
    return cacheMap.computeIfAbsent(key, k -> {
      D data = getData(key);
      if (data == null) return null;

      CacheValue<D, V> entry = new CacheValue<>(createValue(k, data));
      // set entry lastRefresh to now
      lastModified = System.currentTimeMillis();
      return entry;
    });
  }

  @Override
  public void refresh() {
    long time = System.currentTimeMillis();
    boolean removals = cacheMap.values()
                               .removeIf(value -> time - value.lastAccess() > purgeTime);

    boolean mods = false;
    for (Map.Entry<K, CacheValue<D, V>> entry : cacheMap.entrySet()) {
      D data = getData(entry.getKey());
      if (data == null) continue;

      mods |= entry.getValue()
                   .update(data);
    }

    if (mods || removals) {
      lastModified = System.currentTimeMillis();
    }
  }
}
