package org.victorrobotics.devilscoutserver.cache;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CacheValue<D, V extends Cacheable<D>> implements Comparable<CacheValue<?, V>> {
  private static final ObjectMapper JSON = new ObjectMapper();

  private final V value;

  private volatile long   lastRefresh;
  private volatile long   lastAccess;
  private volatile String jsonCache;

  public CacheValue(V value) {
    this.value = value;
  }

  public boolean update(D data) {
    if (data == null) return false;
    if (!value.update(data)) return false;

    lastRefresh = System.currentTimeMillis();
    jsonCache = null;
    return true;
  }

  public V value() {
    lastAccess = System.currentTimeMillis();
    return value;
  }

  public long lastRefresh() {
    return lastRefresh;
  }

  public long lastAccess() {
    return lastAccess;
  }

  @JsonValue
  @JsonRawValue
  public String toJson() {
    if (jsonCache == null) {
      try {
        jsonCache = JSON.writeValueAsString(value);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }

    lastAccess = System.currentTimeMillis();
    return jsonCache;
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
