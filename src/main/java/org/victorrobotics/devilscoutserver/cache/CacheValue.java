package org.victorrobotics.devilscoutserver.cache;

import com.fasterxml.jackson.annotation.JsonValue;

public class CacheValue<D, V extends Cacheable<D>> implements Comparable<CacheValue<?, V>> {
  private final V value;

  private volatile long lastModified;
  private volatile long lastAccess;

  public CacheValue(V value) {
    this.value = value;
  }

  public boolean update(D data) {
    boolean update = value.update(data);
    if (update) {
      lastModified = System.currentTimeMillis();
    }
    return update;
  }

  @JsonValue
  public V value() {
    lastAccess = System.currentTimeMillis();
    return value;
  }

  public long lastModified() {
    return lastModified;
  }

  public long lastAccess() {
    return lastAccess;
  }

  @Override
  @SuppressWarnings({ "unchecked", "java:S3740", "java:S1210" })
  public int compareTo(CacheValue<?, V> o) {
    if (value instanceof Comparable v) {
      return ((Comparable<V>) v).compareTo(o.value);
    }
    return 0;
  }
}
