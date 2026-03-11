package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.Collection;

public abstract class BlueAllianceCache<K, D, V extends Cacheable<D>> extends Cache<K, D, V> {
  protected abstract Endpoint<D> getEndpoint(K key);

  protected abstract V createValue(K key, D data);

  public void refresh(K key) {
    if (key == null) {
      getLogger().warn("Attempted to refresh a null key", new NullPointerException());
      return;
    }

    try {
      long start = System.currentTimeMillis();
      D data = getEndpoint(key).refresh()
                               .orElse(null);

      if (data == null) {
        boolean hadKey = containsKey(key);
        remove(key);
        if (hadKey) {
          modified();
          getLogger().info("Removed entry for key {} in {}ms", key,
                           System.currentTimeMillis() - start);
        }
        return;
      }

      Value<D, V> value = get(key);
      if (value == null) {
        cacheMap.put(key, new Value<>(createValue(key, data), this::modified));
        modified();
        getLogger().info("Added entry for key {} in {}ms", key, System.currentTimeMillis() - start);
      } else {
        value.update(data);
        getLogger().info("Refreshed entry for key {} in {}ms", key,
                         System.currentTimeMillis() - start);
      }
    } catch (Exception e) {
      getLogger().warn("Error while refreshing key {}", key, e);
    }
  }

  public void refreshAll(Collection<? extends K> keys) {
    keys.forEach(this::refresh);

    long start = System.currentTimeMillis();
    int size = size();
    if (keySet().retainAll(keys)) {
      getLogger().info("Removed {} stale entries in {}ms", size - size(),
                       System.currentTimeMillis() - start);
    }
  }
}
