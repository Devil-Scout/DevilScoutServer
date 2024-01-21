package org.victorrobotics.devilscoutserver.cache;

public interface Cache<K, D, V extends Cacheable<D>> {
  int size();

  long lastModified();

  CacheValue<D, V> get(K key);

  void refresh();
}
