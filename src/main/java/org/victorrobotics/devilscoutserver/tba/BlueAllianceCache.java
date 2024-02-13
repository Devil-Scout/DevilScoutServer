package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

public abstract class BlueAllianceCache<K, D, V extends Cacheable<D>> extends Cache<K, D, V> {
  protected abstract Endpoint<D> getEndpoint(K key);

  protected abstract V createValue(K key, D data);

  public void refresh(K key) {
    D data = getEndpoint(key).refresh()
                             .orElse(null);

    if (data == null) {
      boolean hadKey = containsKey(key);
      remove(key);
      if (hadKey) {
        modified();
      }
      return;
    }

    Value<D, V> value = get(key);
    if (value == null) {
      cacheMap.put(key, new Value<>(createValue(key, data), this::onModification));
      modified();
    } else {
      value.update(data);
    }
  }

  public void refreshAll(Iterable<? extends K> keys) {
    keys.forEach(this::refresh);
  }
}
