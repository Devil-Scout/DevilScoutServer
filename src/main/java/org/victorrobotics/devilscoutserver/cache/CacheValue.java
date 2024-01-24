package org.victorrobotics.devilscoutserver.cache;

import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonEncode;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

public class CacheValue<D, V extends Cacheable<D>> implements Comparable<CacheValue<?, V>> {
  private final V        value;
  private final Runnable onModification;

  private volatile long lastModified;
  private volatile long lastAccess;

  private String jsonCache;

  public CacheValue(V value, Runnable onModification) {
    this.value = value;
    this.onModification = onModification;
  }

  void update(D data) {
    if (value.update(data)) {
      onModification.run();
    }
  }

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

  @JsonRawValue
  @JsonValue
  String toJson() {
    if (jsonCache == null) {
      jsonCache = jsonEncode(value());
    }
    return jsonCache;
  }
}
