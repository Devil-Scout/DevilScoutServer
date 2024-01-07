package org.victorrobotics.devilscoutserver.cache;

public interface Cache<K, D, V extends Cacheable<D>> {
  CacheValue<D, V> get(K key);

  boolean containsKey(K key);

  void refresh();

  int size();

  long timestamp();
}
