package org.victorrobotics.devilscoutserver.cache;

import java.util.Map;

public abstract class ListCache<K, D, V extends Cacheable<D>> extends Cache<K, D, V> {
  private final boolean implicitRemoval;

  protected ListCache(boolean implicitRemoval) {
    this.implicitRemoval = implicitRemoval;
  }

  protected abstract Map<K, D> getData();

  protected abstract V createValue(K key, D data);

  @Override
  protected Value<D, V> getValue(K key) {
    return cacheMap.get(key);
  }

  @Override
  public void refresh() {
    Map<K, D> refreshData = getData();

    if (implicitRemoval && cacheMap.keySet()
                                   .retainAll(refreshData.keySet())) {
      modified();
    }

    for (Map.Entry<K, D> entry : refreshData.entrySet()) {
      K key = entry.getKey();
      D data = entry.getValue();

      if (data == null) {
        remove(key);
        continue;
      }

      Value<D, V> value = cacheMap.get(key);
      if (value == null) {
        cacheMap.put(key, new Value<>(createValue(key, data), this::modified));
        modified();
      } else {
        value.update(data);
      }
    }
  }
}
