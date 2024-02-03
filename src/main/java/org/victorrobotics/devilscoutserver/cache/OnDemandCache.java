package org.victorrobotics.devilscoutserver.cache;

import java.util.Map;

public abstract class OnDemandCache<K, D, V extends Cacheable<D>> extends Cache<K, D, V> {
  private final long purgeTime;

  protected OnDemandCache(long purgeTime) {
    this.purgeTime = purgeTime;
  }

  protected abstract D getData(K key);

  protected abstract V createValue(K key, D data);

  @Override
  protected Value<D, V> getValue(K key) {
    return cacheMap.computeIfAbsent(key, k -> {
      D data = getData(k);
      if (data == null) return null;

      Value<D, V> entry = new Value<>(createValue(k, data), this::modified);
      modified();
      return entry;
    });
  }

  @Override
  public void refresh() {
    long time = System.currentTimeMillis();
    if (cacheMap.values()
                .removeIf(value -> time - value.lastAccess() > purgeTime)) {
      modified();
    }

    for (Map.Entry<K, Value<D, V>> entry : cacheMap.entrySet()) {
      D data = getData(entry.getKey());
      if (data == null) continue;

      entry.getValue()
           .update(data);
    }
  }
}
