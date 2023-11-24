package org.victorrobotics.devilscoutserver.cache;

import java.util.stream.Stream;

public interface Cache<K, D, V extends Cacheable<D>> {
  CacheValue<D, V> get(K key);

  boolean containsKey(K key);

  Stream<V> values();

  void refresh();

  int size();

  long timestamp();
}
