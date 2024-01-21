package org.victorrobotics.devilscoutserver.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ListCache<K, D, V extends Cacheable<D>> implements Cache<K, D, V> {
  private final ConcurrentMap<K, CacheValue<D, V>> cacheMap;

  private final boolean implicitRemoval;
  private volatile long lastModified;

  protected ListCache(boolean implicitRemoval) {
    this.cacheMap = new ConcurrentHashMap<>();
    this.implicitRemoval = implicitRemoval;
  }

  protected abstract Map<K, D> getData();

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
    return cacheMap.get(key);
  }

  @Override
  public void refresh() {
    Map<K, D> refreshData = getData();

    boolean mods = false;
    if (implicitRemoval) {
      mods |= cacheMap.keySet()
                      .retainAll(refreshData.keySet());
    }

    for (Map.Entry<K, D> entry : refreshData.entrySet()) {
      K key = entry.getKey();
      D data = entry.getValue();

      if (data == null) {
        cacheMap.remove(key);
        continue;
      }

      CacheValue<D, V> value = cacheMap.get(key);
      if (value == null) {
        cacheMap.put(key, new CacheValue<>(createValue(key, data)));
        mods = true;
      } else {
        mods |= value.update(data);
      }
    }

    if (mods) {
      lastModified = System.currentTimeMillis();
    }
  }

  public Collection<CacheValue<D, V>> values() {
    return cacheMap.values();
  }
}
